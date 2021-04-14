package ba.grbo.currencyconverter.ui.viewmodels

import androidx.lifecycle.ViewModel
import ba.grbo.currencyconverter.data.models.Country
import ba.grbo.currencyconverter.data.source.local.static.Countries
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ConverterViewModel @Inject constructor() : ViewModel() {
    private val _countries = MutableStateFlow(Countries.value)
    val countries: StateFlow<List<Country>>
        get() = _countries



}