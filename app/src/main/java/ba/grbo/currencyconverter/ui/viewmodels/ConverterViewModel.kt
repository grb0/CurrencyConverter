package ba.grbo.currencyconverter.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ba.grbo.currencyconverter.R
import ba.grbo.currencyconverter.data.models.Country
import ba.grbo.currencyconverter.data.models.Currency
import ba.grbo.currencyconverter.data.models.CurrencyName
import ba.grbo.currencyconverter.data.models.FilterBy
import ba.grbo.currencyconverter.data.source.local.static.Countries
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.CurrencyNamesState.*
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.DropdownState.*
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.SearcherState.UNFOCUSED
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.SearcherState.UNFOCUSING
import ba.grbo.currencyconverter.util.toSearcherState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ConverterViewModel @Inject constructor(
        @ApplicationContext context: Context,
        @ba.grbo.currencyconverter.di.CurrencyName currencyName: String,
        @ba.grbo.currencyconverter.di.FilterBy filterBy: String
) : ViewModel() {
    companion object {
        const val DIVIDER_START_HEIGHT = 1.8f
        const val DIVIDER_MODIFIED_HEIGHT = 3.5f
    }

    private val _countries = MutableStateFlow(Countries.value)
    val countries: StateFlow<List<Country>>
        get() = _countries

    private val _fromCurrencyDropdownState = MutableStateFlow(COLLAPSED)
    val fromCurrencyDropdownState: StateFlow<DropdownState>
        get() = _fromCurrencyDropdownState

    private val _fromSelectedCurrency = MutableStateFlow(Countries.value[0])
    val fromSelectedCurrency: StateFlow<Country>
        get() = _fromSelectedCurrency

    private val _searcherState = MutableStateFlow(UNFOCUSED)
    val searcherState: StateFlow<SearcherState>
        get() = _searcherState

    private val _modifyDivider = MutableStateFlow(DIVIDER_START_HEIGHT to R.drawable.divider_shadow)
    val modifyDivider: StateFlow<Pair<Float, Int>>
        get() = _modifyDivider

    private val onTextChanged = MutableStateFlow("")

    private var currencyNamesState = UNINITIALIZED

    private val filter: (Currency, String) -> Boolean

    // No Expanded state, since we want to preserve state upon rotation
    enum class DropdownState {
        EXPANDING,
        COLLAPSING,
        COLLAPSED
    }

    enum class CurrencyNamesState {
        UNINITIALIZED,
        INITIALIZING,
        INITIALIZED
    }

    // No Focused state, since we want to preserve state upon rotation
    enum class SearcherState {
        FOCUSING,
        UNFOCUSING,
        UNFOCUSED
    }

    init {
        initializeCurrencyNames(currencyName, context)
        filter = initializeFilter(currencyName, filterBy)
        onTextChanged
                .debounce(500)
                .onEach { filterCountries(it) }
                .launchIn(viewModelScope)
    }

    private fun initializeCurrencyNames(currencyName: String, context: Context) {
        viewModelScope.launch(Dispatchers.Default) {
            currencyNamesState = INITIALIZING
            Countries.value.forEach { it.currency.initializeName(currencyName, context) }
            currencyNamesState = INITIALIZED
        }
    }

    private fun initializeFilter(
            currencyName: String,
            filterBy: String
    ): (Currency, String) -> Boolean {
        return when (filterBy) {
            FilterBy.CODE -> { currency, query -> currency.code.name.contains(query, true) }
            FilterBy.NAME -> when (currencyName) {
                CurrencyName.NAME -> { currency, query -> currency.name.contains(query, true) }
                CurrencyName.BOTH_CODE -> { currency, query ->
                    currency.name.substring(6).contains(query, true)
                }
                CurrencyName.BOTH_NAME -> { currency, query ->
                    currency.name.substring(0, currency.name.lastIndex - 5).contains(query, true)
                }
                else -> throw IllegalArgumentException("Wrong currencyName: $currencyName")
            }
            FilterBy.BOTH -> { currency, query -> currency.name.contains(query, true) }
            else -> throw IllegalArgumentException("Unknown filterBy: $filterBy")
        }
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
        onTextChanged.value = query
    }

    fun onCountriesScrolled(topReached: Boolean) {
        if (topReached) _modifyDivider.value = DIVIDER_START_HEIGHT to R.drawable.divider_shadow
        else _modifyDivider.value = DIVIDER_MODIFIED_HEIGHT to R.drawable.divider_shadow
    }

    private fun filterCountries(query: String) {
        if (query.isEmpty()) _countries.value = Countries.value
        else viewModelScope.launch {
            if (currencyNamesState == INITIALIZED) {
                _countries.value = withContext(Dispatchers.Default) {
                    Countries.value.filter { filter(it.currency, query) }
                }
            } // TODO else notify the user
        }
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