package ba.grbo.currencyconverter.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ba.grbo.currencyconverter.R
import ba.grbo.currencyconverter.data.models.Country
import ba.grbo.currencyconverter.data.models.Currency
import ba.grbo.currencyconverter.data.models.preferences.FilterBy
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.DropdownState.*
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.SearcherState.UNFOCUSED
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.SearcherState.UNFOCUSING
import ba.grbo.currencyconverter.util.SingleSharedFlow
import ba.grbo.currencyconverter.util.toSearcherState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class ConverterViewModel @Inject constructor(
    private val plainCountries: List<Country>,
    filterBy: FilterBy,
) : ViewModel() {
    companion object {
        const val DIVIDER_START_HEIGHT = 1.8f
        const val DIVIDER_MODIFIED_HEIGHT = 3.5f
    }

    private val _countries = MutableStateFlow(plainCountries)
    val countries: StateFlow<List<Country>>
        get() = _countries

    private val _fromCurrencyDropdownState = MutableStateFlow(COLLAPSED)
    val fromCurrencyDropdownState: StateFlow<DropdownState>
        get() = _fromCurrencyDropdownState

    private val _fromSelectedCurrency = MutableStateFlow(plainCountries[0])
    val fromSelectedCurrency: StateFlow<Country>
        get() = _fromSelectedCurrency

    private val _searcherState = MutableStateFlow(UNFOCUSED)
    val searcherState: StateFlow<SearcherState>
        get() = _searcherState

    private val _modifyDivider = MutableStateFlow(DIVIDER_START_HEIGHT to R.drawable.divider_shadow)
    val modifyDivider: StateFlow<Pair<Float, Int>>
        get() = _modifyDivider

    private val _showResetButton = MutableStateFlow(false)
    val showResetButton: StateFlow<Boolean>
        get() = _showResetButton

    private val _resetSearcher = SingleSharedFlow<Unit>()
    val resetSearcher: SharedFlow<Unit>
        get() = _resetSearcher

    private val onTextChanged = MutableStateFlow("")

    private val filter: (Currency, String) -> Boolean

    // No Expanded state, since we want to preserve state upon rotation
    enum class DropdownState {
        EXPANDING,
        COLLAPSING,
        COLLAPSED
    }

    // No Focused state, since we want to preserve state upon rotation
    enum class SearcherState {
        FOCUSING,
        UNFOCUSING,
        UNFOCUSED
    }

    init {
        filter = initializeFilter(filterBy)
        onTextChanged
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

    fun onExpandClicked() {
        mutateDropdown()
    }

    fun onDropdownClicked() {
        mutateDropdown()
    }

    fun onTitleClicked() {
        mutateDropdown()
    }

    fun onResetSearcherClicked() {
        _resetSearcher.tryEmit(Unit)
    }

    fun onCurrencyClicked(country: Country) {
        updateSelectedCurrency(country)
        collapseDropdown()
    }

    fun onScreenTouched(
        dropdownActionTouched: Boolean,
        currencyLayoutTouched: Boolean,
        dropdownTitleTouched: Boolean,
        currenciesCardTouched: Boolean
    ): Boolean {
        val shouldCollapse = !dropdownActionTouched && !currencyLayoutTouched &&
                !dropdownTitleTouched && !currenciesCardTouched
        if (shouldCollapse) _fromCurrencyDropdownState.value = COLLAPSING
        return shouldCollapse
    }

    fun onScreenTouched(
        currenciesCardTouched: Boolean,
        currenciesSearcherTouched: Boolean
    ): Boolean {
        val shouldUnfocus = currenciesCardTouched && !currenciesSearcherTouched
        if (shouldUnfocus) _searcherState.value = UNFOCUSING
        return shouldUnfocus
    }

    private fun updateSelectedCurrency(country: Country) {
        _fromSelectedCurrency.value = country
    }

    fun onDropdownExpanded() {
        _fromCurrencyDropdownState.value = EXPANDING
    }

    fun onDropdownCollapsed() {
        _fromCurrencyDropdownState.value = COLLAPSED
    }

    fun onSearcherFocusChanged(focused: Boolean) {
        _searcherState.value = focused.toSearcherState()
    }

    fun onSearcherUnfocused() {
        _searcherState.value = UNFOCUSED
    }

    fun onTextChanged(query: String) {
        if (query.isEmpty()) {
            _countries.value = plainCountries
            setResetButton(false)
        } else {
            setResetButton(true)
            onTextChanged.value = query
        }
    }

    fun onCountriesScrolled(topReached: Boolean) {
        if (topReached) _modifyDivider.value = DIVIDER_START_HEIGHT to R.drawable.divider_shadow
        else _modifyDivider.value = DIVIDER_MODIFIED_HEIGHT to R.drawable.divider_shadow
    }

    private fun setResetButton(hasText: Boolean) {
        _showResetButton.value = hasText
    }

    private fun filterCountries(query: String) {
        _countries.value = plainCountries.filter { filter(it.currency, query) }
    }

    private fun mutateDropdown() {
        if (isDropdownCollapsed()) expandDropdown() else collapseDropdown()
    }

    private fun expandDropdown() {
        _fromCurrencyDropdownState.value = EXPANDING
    }

    private fun collapseDropdown() {
        _fromCurrencyDropdownState.value = COLLAPSING
    }

    private fun isDropdownCollapsed() = fromCurrencyDropdownState.value == COLLAPSED
}