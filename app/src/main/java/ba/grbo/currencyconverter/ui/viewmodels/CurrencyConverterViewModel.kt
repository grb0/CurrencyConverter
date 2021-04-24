package ba.grbo.currencyconverter.ui.viewmodels

import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import ba.grbo.currencyconverter.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class CurrencyConverterViewModel @Inject constructor() : ViewModel() {
    private val _actionBarTitleId = MutableStateFlow<@StringRes Int>(R.string.title_converter)
    val actionBarTitleId: StateFlow<Int>
        get() = _actionBarTitleId

    fun onDestinationChanged(@IdRes destinationId: Int) {
        when (destinationId) {
            R.id.navigation_converter -> _actionBarTitleId.tryEmit(R.string.title_converter)
            R.id.navigation_history -> _actionBarTitleId.tryEmit(R.string.title_history)
            R.id.navigation_settings -> _actionBarTitleId.tryEmit(R.string.title_settings)
        }
    }
}