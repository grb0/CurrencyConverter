package ba.grbo.currencyconverter.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ba.grbo.currencyconverter.data.models.Country
import ba.grbo.currencyconverter.data.source.local.static.Countries
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.DropdownState.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConverterViewModel @Inject constructor() : ViewModel() {
    companion object {
        const val DROPDOWN_CURRENCY_CLICKED_DELAY_MS = 200L
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

    // No Expanded state, since we want to preserve state upon rotation
    enum class DropdownState {
        EXPANDING,
        COLLAPSING,
        COLLAPSED
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
        viewModelScope.launch {
            delay(DROPDOWN_CURRENCY_CLICKED_DELAY_MS)
            updateSelectedCurrency(country)
            collapseDropdown()
        }
    }

    fun onScreenTouched(
        dropdownActionTouched: Boolean,
        currencyLayoutTouched: Boolean,
        dropdownTitleTouched: Boolean,
        currenciesCardTouched: Boolean
    ) {
        val shouldCollapse = !dropdownActionTouched && !currencyLayoutTouched &&
                !dropdownTitleTouched && !currenciesCardTouched
        if (shouldCollapse) _fromCurrencyDropdownState.value = COLLAPSING
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