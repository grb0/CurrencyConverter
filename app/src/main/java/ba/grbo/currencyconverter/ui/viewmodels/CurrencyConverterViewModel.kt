package ba.grbo.currencyconverter.ui.viewmodels

import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import ba.grbo.currencyconverter.R
import ba.grbo.currencyconverter.data.models.Country
import ba.grbo.currencyconverter.util.SingleSharedFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class CurrencyConverterViewModel @Inject constructor(
    // Injecting, so we force first access in activi
    @Suppress("UNUSED_PARAMETER") countries: List<Country>
) : ViewModel() {
    private val _toFragment = SingleSharedFlow<@IdRes Int>()
    val toFragment: SharedFlow<Int>
        get() = _toFragment

    private val _actionBarTitle = MutableStateFlow<@StringRes Int>(R.string.title_converter)
    val actionBarTitle: StateFlow<Int>
        get() = _actionBarTitle

    fun onBottomNavigationClicked(@IdRes itemId: Int): Boolean {
        return _toFragment.tryEmit(
            when (itemId) {
                R.id.converter -> R.id.converterFragment
                R.id.history -> R.id.historyFragment
                R.id.settings -> R.id.settingsFragment
                else -> throw IllegalArgumentException("Unknown itemId: $itemId")
            }
        )
    }

    fun onDestinationChanged(@IdRes destinationId: Int) {
        _actionBarTitle.tryEmit(
            when (destinationId) {
                R.id.converterFragment -> R.string.title_converter
                R.id.historyFragment -> R.string.title_history
                R.id.settingsFragment -> R.string.title_settings
                else -> throw IllegalArgumentException("Unknown destinationId: $destinationId")
            }
        )
    }
}