package ba.grbo.currencyconverter.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ba.grbo.currencyconverter.R
import ba.grbo.currencyconverter.data.models.domain.ExchangeableCurrency
import ba.grbo.currencyconverter.data.models.domain.Miscellaneous
import ba.grbo.currencyconverter.data.source.CurrenciesRepository
import ba.grbo.currencyconverter.di.IgnoreFailedDbUpdates
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.Dropdown.*
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.DropdownState.*
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.SearcherState.Unfocused
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.SearcherState.Unfocusing
import ba.grbo.currencyconverter.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConverterViewModel @Inject constructor(
    private val repository: CurrenciesRepository,
    filterBy: FilterBy,
    @IgnoreFailedDbUpdates private var ignoreFailedDbUpdates: Boolean
) : ViewModel() {
    private lateinit var plainCurrencies: StateFlow<List<ExchangeableCurrency>>

    private val _databaseExceptionCaught =
        SharedStateLikeFlow<DialogInfo>()
    val databaseExceptionCaught: SharedFlow<DialogInfo>
        get() = _databaseExceptionCaught

    private val _databaseExceptionAcknowledged = SharedStateLikeFlow<Unit>()
    val databaseExceptionAcknowledged: SharedFlow<Unit>
        get() = _databaseExceptionAcknowledged

    private val _databaseUpdateFailed = SingleSharedFlow<ExtendedDialogInfo>()
    val databaseUpdateFailed: SharedFlow<ExtendedDialogInfo>
        get() = _databaseUpdateFailed

    private val _ignoreDatabaseUpdateFailed = SingleSharedFlow<Unit>()
    val ignoreDatebaseUpdateFailed: SharedFlow<Unit>
        get() = _ignoreDatabaseUpdateFailed

    private lateinit var _currencies: MutableStateFlow<MutableList<ExchangeableCurrency>>
    val currencies: StateFlow<List<ExchangeableCurrency>?>
        get() = if (::_currencies.isInitialized) _currencies else MutableStateFlow(null)

    private lateinit var _fromCurrency: MutableStateFlow<ExchangeableCurrency>
    val fromCurrency: StateFlow<ExchangeableCurrency?>
        get() = if (::_fromCurrency.isInitialized) _fromCurrency else MutableStateFlow(null)

    private lateinit var _toCurrency: MutableStateFlow<ExchangeableCurrency>
    val toCurrency: StateFlow<ExchangeableCurrency?>
        get() = if (::_toCurrency.isInitialized) _toCurrency else MutableStateFlow(null)

    private val _currenciesEmpty = MutableStateFlow(false)
    val currenciesEmpty: SharedFlow<Boolean>
        get() = _currenciesEmpty

    private var _showOnlyFavorites = false
    val showOnlyFavorites: Boolean
        get() = _showOnlyFavorites

    init {
        repository.exception
            .onEach {
                it?.let {
                    _databaseExceptionCaught.tryEmit(
                        DialogInfo(
                            R.string.database_exception_caught_title,
                            R.string.database_exception_caught_message,
                            Pair(R.string.database_exception_caught_positive_button, null),
                            ::onDatabaseExceptionAcknowledged
                        )
                    )
                }
            }
            .launchIn(viewModelScope)

        if (repository.converterDataIsReady()) {
            plainCurrencies = repository.exchangeableCurrencies
            _currencies = MutableStateFlow(plainCurrencies.value.toMutableList())
            _fromCurrency = MutableStateFlow(repository.miscellaneous.lastUsedFromCurrency)
            _toCurrency = MutableStateFlow(repository.miscellaneous.lastUsedToCurrency)
            if (repository.miscellaneous.showOnlyFavorites) {
                _showOnlyFavorites = true
                filterCurrentCurrencies()
            }
            repository.observeCurrenciesAndMiscellaneous(viewModelScope)
        }
    }

    private val _dropdownState = MutableStateFlow<DropdownState>(Collapsed(NONE))
    val dropdownState: StateFlow<DropdownState>
        get() = _dropdownState

    private val _fromSelectedCurrency = SingleSharedFlow<ExchangeableCurrency>()
    val fromSelectedCurrency: SharedFlow<ExchangeableCurrency>
        get() = _fromSelectedCurrency

    private val _toSelectedCurrency = SingleSharedFlow<ExchangeableCurrency>()
    val toSelectedCurrency: SharedFlow<ExchangeableCurrency>
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

    private val _scrollRecyclerViewToTop = SingleSharedFlow<Unit>()
    val scrollRecyclerViewToTop: SharedFlow<Unit>
        get() = _scrollRecyclerViewToTop

    private val _onFavoritesClicked = SingleSharedFlow<Boolean>()
    val onFavoritesClicked: SharedFlow<Boolean>
        get() = _onFavoritesClicked

    private val _notifyItemChanged = SingleSharedFlow<Int>()
    val notifyItemChanged: SharedFlow<Int>
        get() = _notifyItemChanged

    private val _notifyItemRemoved = SingleSharedFlow<Int>()
    val notifyItemRemoved: SharedFlow<Int>
        get() = _notifyItemRemoved

    private val onSearcherTextChanged: MutableStateFlow<String?> = MutableStateFlow(null)

    private val filter: (ExchangeableCurrency, String) -> Boolean

    private var lastClickedDropdown = NONE
    private var initialSwappingState = SwappingState.None

    companion object {
        const val DEBOUNCE_PERIOD = 250L
    }

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
        viewModelScope.collectOnSearcherTextChanged()
    }

    private fun CoroutineScope.collectOnSearcherTextChanged() = launch(Dispatchers.Default) {
        onSearcherTextChanged.collectLatest {
            it?.let {
                if (it.isEmpty()) resetCountries()
                else {
                    setResetButton(true)
                    delay(DEBOUNCE_PERIOD)
                    filterCurrencies(it)
                }
            }
        }
    }


    private fun initializeFilter(
        filterBy: FilterBy
    ): (ExchangeableCurrency, String) -> Boolean = when (filterBy) {
        FilterBy.CODE -> { currency, query -> currency.name.code.contains(query, true) }
        FilterBy.NAME -> { currency, query -> currency.name.name.contains(query, true) }
        FilterBy.BOTH -> { currency, query -> currency.name.codeAndName.contains(query, true) }
    }

    private fun onDatabaseExceptionAcknowledged() {
        _databaseExceptionAcknowledged.tryEmit(Unit)
    }

    private fun onIgnoreDatabaseUpdateFailed() {
        _ignoreDatabaseUpdateFailed.tryEmit(Unit)
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
        _searcherState.value = Unfocused(NONE)
    }

    fun wasLastClickedTo() = lastClickedDropdown == TO

    private fun swapSelectedCurrencies() {
        val fromCountry = _fromCurrency.value
        _fromCurrency.value = _toCurrency.value
        _toCurrency.value = fromCountry

        viewModelScope.updateMiscellaneous()
    }

    fun onResetSearcherClicked() {
        _resetSearcher.tryEmit(Unit)
    }

    fun onFavoritesClicked() {
        _onFavoritesClicked.tryEmit(!showOnlyFavorites)
    }

    fun onFavoritesAnimationEnd(isFavoritesOn: Boolean) {
        _showOnlyFavorites = isFavoritesOn
        if (isFavoritesOn) filterCurrentCurrencies() // Since they're already filtered
        else filterCurrencies(onSearcherTextChanged.value)

        viewModelScope.updateMiscellaneous()
    }

    private fun filterCurrentCurrencies() {
        setCurrencies(_currencies.value.filter { it.isFavorite }.toMutableList())
    }

    private fun setCurrencies(currencies: MutableList<ExchangeableCurrency>) {
        _currencies.value = currencies
        checkIfCurrenciesAreEmpty()
    }

    private fun checkIfCurrenciesAreEmpty() {
        if (_currencies.value.isEmpty() && !_currenciesEmpty.value) _currenciesEmpty.value = true
        else if (_currencies.value.isNotEmpty() && _currenciesEmpty.value) {
            _currenciesEmpty.value = false
        }
    }

    fun onFavoritesAnimationEnd(
        currency: ExchangeableCurrency,
        isFavoritesOn: Boolean,
        position: Int
    ) {
        if (currency.isFavorite != isFavoritesOn) {
            val updatedCurrency = currency.copy(isFavorite = isFavoritesOn)
            viewModelScope.updateCurrency(updatedCurrency)
            updateCurrencies(updatedCurrency, isFavoritesOn, position)
        }
    }

    private fun notifyItemChanged(position: Int) {
        _notifyItemChanged.tryEmit(position)
    }

    private fun removeUnfavoritedCurrency(index: Int, position: Int) {
        _currencies.value.removeAt(index)
        notifyItemRemoved(position)
        checkIfCurrenciesAreEmpty()
    }

    private fun notifyItemRemoved(position: Int) {
        _notifyItemRemoved.tryEmit(position)
    }

    private fun updateCurrencies(
        updatedCurrency: ExchangeableCurrency,
        isFavoritesOn: Boolean,
        position: Int
    ) {
        val index = _currencies.value.indexOfFirst { it.code == updatedCurrency.code }
        if (index != -1) {
            if (showOnlyFavorites && !isFavoritesOn) removeUnfavoritedCurrency(index, position)
            else updateCurrencies(
                updatedCurrency,
                index,
                position
            )
        }
    }

    private fun CoroutineScope.updateCurrency(currency: ExchangeableCurrency) = launch {
        val updated = repository.updateCurrency(currency)
        if (!updated && !ignoreFailedDbUpdates) onDatabaseUpdateFailed()
    }

    private fun onDatabaseUpdateFailed() {
        _databaseUpdateFailed.tryEmit(
            ExtendedDialogInfo(
                R.string.database_update_failed_title,
                R.string.database_update_failed_message,
                Pair(R.string.database_update_failed_positive_button, null),
                null,
                Pair(
                    R.string.database_update_failed_negative_button,
                    ::onIgnoreDatabaseUpdateFailed
                )
            )
        )
    }

    fun onFailedDbUpdatedNotificationsIgnored() {
        ignoreFailedDbUpdates = true
    }

    private fun updateCurrencies(currency: ExchangeableCurrency, index: Int, position: Int) {
        _currencies.value[index] = currency
        notifyItemChanged(position)
    }

    fun onCurrencyClicked(currency: ExchangeableCurrency) {
        updateSelectedCurrency(currency)
        collapseDropdown(_dropdownState.value.dropdown)
    }

    fun onScreenTouched(
        dropdown: Dropdown,
        currencyLayoutTouched: Boolean,
        dropdownTitleTouched: Boolean,
        currenciesCardTouched: Boolean
    ): Boolean {
        return (!currencyLayoutTouched && !dropdownTitleTouched && !currenciesCardTouched).also {
            if (it) collapseDropdown(dropdown)
        }
    }

    private fun updateSelectedCurrency(currency: ExchangeableCurrency) {
        if (_dropdownState.value.dropdown == FROM) {
            _fromSelectedCurrency.tryEmit(currency)
            _fromCurrency.value = currency
        } else {
            _toSelectedCurrency.tryEmit(currency)
            _toCurrency.value = currency
        }

        viewModelScope.updateMiscellaneous()
    }

    private fun CoroutineScope.updateMiscellaneous() = launch {
        val updated = repository.updateMiscellaneous(
            Miscellaneous(
                showOnlyFavorites,
                _fromCurrency.value,
                _toCurrency.value
            ).toDatabase(plainCurrencies.value)
        )

        if (!updated && !ignoreFailedDbUpdates) onDatabaseUpdateFailed()
    }

    fun onDropdownCollapsed() {
        _dropdownState.value = Collapsed(NONE)
    }

    fun onDropdownExpanded(dropdown: Dropdown) {
        lastClickedDropdown = dropdown
    }

    fun onSearcherFocusChanged(focused: Boolean) {
        _searcherState.value = focused.toSearcherState(_dropdownState.value.dropdown)
    }

    fun onSearcherUnfocused(dropdown: Dropdown) {
        _searcherState.value = Unfocused(dropdown)
    }

    fun onSearcherTextChanged(query: String) {
        onSearcherTextChanged.value = query
    }

    private fun resetCountries() {
        filterCurrencies("")
        setResetButton(false)
    }

    fun onRecyclerViewScrolled(topReached: Boolean) {
        _modifyDivider.value = topReached
    }

    fun onCurrenciesChanged() {
        _scrollRecyclerViewToTop.tryEmit(Unit)
    }

    fun shouldModifyDropdownMenuPosition(
        dropdown: Dropdown,
        landscapeModificator: Boolean
    ): Boolean {
        return !landscapeModificator && lastClickedDropdown != dropdown
    }

    private fun setResetButton(hasText: Boolean) {
        _showResetButton.tryEmit(hasText)
    }

    private fun filterCurrencies(query: String?) {
        if (!showOnlyFavorites) filterAllCurrencies(query ?: "")
        else filterFavoriteCurrencies(query ?: "")
    }

    private fun filterAllCurrencies(query: String) {
        setCurrencies(
            if (query.isEmpty()) plainCurrencies.value.toMutableList()
            else plainCurrencies.value.filter { filter(it, query) }.toMutableList()
        )
    }

    private fun filterFavoriteCurrencies(query: String) {
        setCurrencies(
            if (query.isEmpty()) plainCurrencies.value.filter { it.isFavorite }.toMutableList()
            else plainCurrencies.value.filter { it.isFavorite && filter(it, query) }.toMutableList()
        )
    }

    private fun mutateDropdown(dropdown: Dropdown) {
        if (isDropdownCollapsed()) expandDropdown(dropdown) else collapseDropdown(dropdown)
    }

    private fun expandDropdown(dropdown: Dropdown) {
        _dropdownState.value = Expanding(dropdown)
    }

    private fun collapseDropdown(dropdown: Dropdown) {
        if (_searcherState.value !is Unfocused) _searcherState.value = Unfocusing(dropdown)
        _dropdownState.value = Collapsing(dropdown)
    }

    private fun isDropdownCollapsed(): Boolean {
        return _dropdownState.value == Collapsed(NONE)
    }
}