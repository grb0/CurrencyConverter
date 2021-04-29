package ba.grbo.currencyconverter.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ba.grbo.currencyconverter.data.models.Country
import ba.grbo.currencyconverter.data.models.Currency
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.Dropdown.*
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.DropdownState.*
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.SearcherState.Unfocused
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.SearcherState.Unfocusing
import ba.grbo.currencyconverter.util.FilterBy
import ba.grbo.currencyconverter.util.SharedStateLikeFlow
import ba.grbo.currencyconverter.util.SingleSharedFlow
import ba.grbo.currencyconverter.util.toSearcherState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConverterViewModel @Inject constructor(
    private val plainCountries: MutableList<Country>,
    filterBy: FilterBy,
) : ViewModel() {
    private val _countries = MutableStateFlow(plainCountries)
    val countries: StateFlow<List<Country>>
        get() = _countries

    private val _dropdownState = MutableStateFlow<DropdownState>(Collapsed(NONE))
    val dropdownState: StateFlow<DropdownState>
        get() = _dropdownState

    private val _fromCurrency = MutableStateFlow(plainCountries[0])
    val fromCurrency: StateFlow<Country>
        get() = _fromCurrency

    private val _toCurrency = MutableStateFlow(plainCountries[1])
    val toCurrency: StateFlow<Country>
        get() = _toCurrency

    private val _fromSelectedCurrency = SingleSharedFlow<Country>()
    val fromSelectedCurrency: SharedFlow<Country>
        get() = _fromSelectedCurrency

    private val _toSelectedCurrency = SingleSharedFlow<Country>()
    val toSelectedCurrency: SharedFlow<Country>
        get() = _toSelectedCurrency

    private val _searcherState = MutableStateFlow<SearcherState>(Unfocused(NONE))
    val searcherState: StateFlow<SearcherState>
        get() = _searcherState

    private val _modifyDivider = MutableStateFlow(true)
    val modifyDivider: StateFlow<Boolean>
        get() = _modifyDivider

    private val _showResetButton = SharedStateLikeFlow<Boolean>()
    val showResetButton: SharedFlow<Boolean>
        get() = _showResetButton

    private val _resetSearcher = SingleSharedFlow<Unit>()
    val resetSearcher: SharedFlow<Unit>
        get() = _resetSearcher

    private val _swappingState = MutableStateFlow(SwappingState.None)
    val swappingState: SharedFlow<SwappingState>
        get() = _swappingState

    private val _scrollCurrenciesToTop = SingleSharedFlow<Unit>()
    val scrollCurrenciesToTop: SharedFlow<Unit>
        get() = _scrollCurrenciesToTop

    private val _onFavoritesClicked = SingleSharedFlow<Boolean>()
    val onFavoritesClicked: SharedFlow<Boolean>
        get() = _onFavoritesClicked

    private val _notifyAdapter = SingleSharedFlow<Int>()
    val notifyAdapter: SharedFlow<Int>
        get() = _notifyAdapter

    private val onSearcherTextChanged = MutableStateFlow("")

    private val filter: (Currency, String) -> Boolean

    private var lastClickedDropdown = NONE
    private var currenciesCardModifiedForLandscape = false
    private var initialSwappingState = SwappingState.None

    private var _showOnlyFavorites = false
    val showOnlyFavorites: Boolean
        get() = _showOnlyFavorites

    sealed class DropdownState {
        abstract val dropdown: Dropdown

        class Expanding(override val dropdown: Dropdown) : DropdownState()
        class Collapsing(override val dropdown: Dropdown) : DropdownState()
        data class Collapsed(override val dropdown: Dropdown) : DropdownState()
    }

    sealed class SearcherState {
        abstract val dropdown: Dropdown

        class Focusing(override val dropdown: Dropdown) : SearcherState()
        class Unfocusing(override val dropdown: Dropdown) : SearcherState()
        data class Unfocused(override val dropdown: Dropdown) : SearcherState()
    }

    enum class SwappingState {
        Swapping,
        SwappingInterrupted,
        Swapped,
        SwappedWithInterruption,
        ReverseSwapping,
        ReverseSwappingInterrupted,
        ReverseSwapped,
        ReverseSwappedWithInterruption,
        None
    }

    enum class Dropdown {
        FROM,
        TO,
        NONE
    }

    init {
        filter = initializeFilter(filterBy)
        onSearcherTextChanged
            .debounce(250)
            .onEach { filterCountries(it) }
            .flowOn(Dispatchers.Default)
            .launchIn(viewModelScope)
    }

    private fun initializeFilter(
        filterBy: FilterBy
    ): (Currency, String) -> Boolean = when (filterBy) {
        FilterBy.CODE -> { currency, query -> currency.name.code.contains(query, true) }
        FilterBy.NAME -> { currency, query -> currency.name.name.contains(query, true) }
        FilterBy.BOTH -> { currency, query -> currency.name.codeAndName.contains(query, true) }
    }

    fun onFromDropdownActionClicked() {
        mutateDropdown(FROM)
    }

    fun onFromDropdownClicked() {
        onFromDropdownActionClicked() // Delegate
    }

    fun onFromTitleClicked() {
        onFromDropdownActionClicked() // Delegate
    }

    fun onToDropdownActionClicked() {
        mutateDropdown(TO)
    }

    fun onToDropdownClicked() {
        mutateDropdown(TO)
    }

    fun onToTitleClicked() {
        mutateDropdown(TO)
    }

    fun onDropdownSwapperClicked() {
        _swappingState.value = when (_swappingState.value) {
            SwappingState.None,
            SwappingState.SwappedWithInterruption,
            SwappingState.ReverseSwapped -> {
                initialSwappingState = SwappingState.Swapping
                SwappingState.Swapping
            }
            SwappingState.ReverseSwappedWithInterruption,
            SwappingState.Swapped -> {
                initialSwappingState = SwappingState.ReverseSwapping
                SwappingState.ReverseSwapping
            }
            SwappingState.Swapping,
            SwappingState.ReverseSwappingInterrupted -> SwappingState.SwappingInterrupted
            SwappingState.ReverseSwapping,
            SwappingState.SwappingInterrupted -> SwappingState.ReverseSwappingInterrupted
        }
    }

    fun onSwapped() {
        _swappingState.value = SwappingState.Swapped
    }

    fun onReverseSwapped() {
        _swappingState.value = SwappingState.ReverseSwapped
    }

    fun onSwappingInterrupted() {
        _swappingState.value = SwappingState.SwappedWithInterruption
    }

    fun onReverseSwappingInterrupted() {
        _swappingState.value = SwappingState.ReverseSwappedWithInterruption
    }

    fun onSwappingCompleted() {
        if (initialSwappingState == SwappingState.Swapping) swapSelectedCurrencies()
    }

    fun onReverseSwappingCompleted() {
        if (initialSwappingState == SwappingState.ReverseSwapping) swapSelectedCurrencies()
    }

    fun resetInternalState() {
        _swappingState.value = SwappingState.None
        lastClickedDropdown = NONE
    }

    private fun swapSelectedCurrencies() {
        val fromCountry = _fromCurrency.value
        val toCountry = _toCurrency.value
        viewModelScope.launch {
            launch { _fromCurrency.value = toCountry }
            launch { _toCurrency.value = fromCountry }
        }
    }

    fun onResetSearcherClicked() {
        _resetSearcher.tryEmit(Unit)
    }

    fun onFavoritesClicked() {
        _onFavoritesClicked.tryEmit(!showOnlyFavorites)
    }

    fun onFavoritesAnimationEnd(isFavoritesOn: Boolean) {
        _showOnlyFavorites = isFavoritesOn
    }

    fun onFavoritesAnimationEnd(country: Country, isFavoritesOn: Boolean, position: Int) {
        if (country.favorite != isFavoritesOn) {
            updateCountry(country, isFavoritesOn)
            notifyAdapter(position)
        }
    }

    private fun notifyAdapter(position: Int) {
        _notifyAdapter.tryEmit(position)
    }

    private fun updateCountry(country: Country, isFavoritesOn: Boolean) {
        var index = plainCountries.indexOfFirst { it.currency.code == country.currency.code }
        plainCountries[index] = country.copy(favorite = isFavoritesOn)
        index = _countries.value.indexOfFirst { it.currency.code == country.currency.code }
        _countries.value[index] = country.copy(favorite = isFavoritesOn)
    }

    fun onCurrencyClicked(country: Country) {
        updateSelectedCurrency(country)
        collapseDropdown(_dropdownState.value.dropdown)
    }

    fun onScreenTouched(
        dropdown: Dropdown,
        currencyLayoutTouched: Boolean,
        dropdownTitleTouched: Boolean,
        currenciesCardTouched: Boolean
    ): Boolean {
        val shouldCollapse =
            !currencyLayoutTouched && !dropdownTitleTouched && !currenciesCardTouched
        return shouldCollapse.also { if (it) _dropdownState.value = Collapsing(dropdown) }
    }

    fun onScreenTouched(
        dropdown: Dropdown,
        currencyLayoutTouched: Boolean,
        dropdownTitleTouched: Boolean,
        currenciesCardTouched: Boolean,
        currenciesSearcherTouched: Boolean
    ): Boolean {
        val shouldUnfocus = currenciesCardTouched && !currenciesSearcherTouched
        return if (shouldUnfocus) {
            _searcherState.value = Unfocusing(dropdown)
            false
        } else {
            val shouldCollapse =
                !currencyLayoutTouched && !dropdownTitleTouched && !currenciesCardTouched
            shouldCollapse.also { if (it) _dropdownState.value = Collapsing(dropdown) }
        }
    }

    private fun updateSelectedCurrency(country: Country) {
        if (_dropdownState.value.dropdown == FROM) {
            _fromSelectedCurrency.tryEmit(country)
            _fromCurrency.value = country
        } else {
            _toSelectedCurrency.tryEmit(country)
            _toCurrency.value = country
        }
    }

    fun onDropdownCollapsed(dropdown: Dropdown) {
        _dropdownState.value = Collapsed(NONE)
        lastClickedDropdown = dropdown
    }

    fun onSearcherFocusChanged(focused: Boolean) {
        _searcherState.value = focused.toSearcherState(_dropdownState.value.dropdown)
    }

    fun onSearcherUnfocused(dropdown: Dropdown) {
        _searcherState.value = Unfocused(dropdown)
    }

    fun onSearcherTextChanged(query: String) {
        if (query.isEmpty()) {
            _countries.value = plainCountries
            setResetButton(false)
        } else {
            setResetButton(true)
            onSearcherTextChanged.value = query
        }
    }

    fun onCountriesScrolled(topReached: Boolean) {
        _modifyDivider.value = topReached
    }

    fun onCountriesChanged() {
        _scrollCurrenciesToTop.tryEmit(Unit)
    }

    fun shouldModifyCurrenciesCardPosition(dropdown: Dropdown): Boolean {
        return if (currenciesCardModifiedForLandscape) {
            currenciesCardModifiedForLandscape = false
            lastClickedDropdown = NONE
            true
        } else lastClickedDropdown != dropdown
    }

    fun shouldModifyCurrenciesCardPosition() = !currenciesCardModifiedForLandscape

    fun onCurrenciesCardPositionModified() {
        currenciesCardModifiedForLandscape = true
    }

    private fun setResetButton(hasText: Boolean) {
        _showResetButton.tryEmit(hasText)
    }

    private fun filterCountries(query: String) {
        _countries.value = plainCountries.filter { filter(it.currency, query) }.toMutableList()
    }

    private fun mutateDropdown(dropdown: Dropdown) {
        if (isDropdownCollapsed()) expandDropdown(dropdown) else collapseDropdown(dropdown)
    }

    private fun expandDropdown(dropdown: Dropdown) {
        _dropdownState.value = Expanding(dropdown)
    }

    private fun collapseDropdown(dropdown: Dropdown) {
        _dropdownState.value = Collapsing(dropdown)
    }

    private fun isDropdownCollapsed(): Boolean {
        return _dropdownState.value == Collapsed(NONE)
    }
}