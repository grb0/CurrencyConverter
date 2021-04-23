package ba.grbo.currencyconverter.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ba.grbo.currencyconverter.data.models.Country
import ba.grbo.currencyconverter.data.models.Currency
import ba.grbo.currencyconverter.data.models.preferences.FilterBy
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.Dropdown.*
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.DropdownState.*
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.SearcherState.Unfocused
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.SearcherState.Unfocusing
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
    private val _countries = MutableStateFlow(plainCountries)
    val countries: StateFlow<List<Country>>
        get() = _countries

    private val _dropdownState = MutableStateFlow<DropdownState>(Collapsed(NONE))
    val dropdownState: StateFlow<DropdownState>
        get() = _dropdownState

    private val _fromSelectedCurrency = MutableStateFlow(plainCountries[0])
    val fromSelectedCurrency: StateFlow<Country>
        get() = _fromSelectedCurrency

    private val _toSelectedCurrency = MutableStateFlow(plainCountries[0])
    val toSelectedCurrency: StateFlow<Country>
        get() = _toSelectedCurrency

    private val _searcherState = MutableStateFlow<SearcherState>(Unfocused(NONE))
    val searcherState: StateFlow<SearcherState>
        get() = _searcherState

    private val _modifyDivider = MutableStateFlow(true)
    val modifyDivider: StateFlow<Boolean>
        get() = _modifyDivider

    private val _showResetButton = MutableStateFlow(false)
    val showResetButton: StateFlow<Boolean>
        get() = _showResetButton

    private val _resetSearcher = SingleSharedFlow<Unit>()
    val resetSearcher: SharedFlow<Unit>
        get() = _resetSearcher

    private val onTextChanged = MutableStateFlow("")

    private val filter: (Currency, String) -> Boolean

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

    enum class Dropdown {
        FROM,
        TO,
        NONE
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

    fun onFromDropdownActionClicked() {
        mutateDropdown(FROM)
    }

    fun onFromDropdownClicked() {
        mutateDropdown(FROM)
    }

    fun onFromTitleClicked() {
        mutateDropdown(FROM)
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

    fun onResetSearcherClicked() {
        _resetSearcher.tryEmit(Unit)
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
        currenciesCardTouched: Boolean,
        currenciesSearcherTouched: Boolean
    ): Boolean {
        val shouldUnfocus = currenciesCardTouched && !currenciesSearcherTouched
        if (shouldUnfocus) _searcherState.value = Unfocusing(dropdown)
        return shouldUnfocus
    }

    private fun updateSelectedCurrency(country: Country) {
        if (_dropdownState.value.dropdown == FROM) _fromSelectedCurrency.value = country
        else _toSelectedCurrency.value = country

    }

    fun onDropdownCollapsed() {
        _dropdownState.value = Collapsed(NONE)
    }

    fun onSearcherFocusChanged(focused: Boolean) {
        _searcherState.value = focused.toSearcherState(_dropdownState.value.dropdown)
    }

    fun onSearcherUnfocused(dropdown: Dropdown) {
        _searcherState.value = Unfocused(dropdown)
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
        _modifyDivider.value = topReached
    }

    private fun setResetButton(hasText: Boolean) {
        _showResetButton.value = hasText
    }

    private fun filterCountries(query: String) {
        _countries.value = plainCountries.filter { filter(it.currency, query) }
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