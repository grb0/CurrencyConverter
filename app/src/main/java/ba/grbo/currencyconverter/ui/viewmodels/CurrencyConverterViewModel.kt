package ba.grbo.currencyconverter.ui.viewmodels

import androidx.annotation.IdRes
import androidx.lifecycle.ViewModel
import ba.grbo.currencyconverter.R
import ba.grbo.currencyconverter.util.SharedStateLikeFlow
import ba.grbo.currencyconverter.util.SingleSharedFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

@HiltViewModel
class CurrencyConverterViewModel @Inject constructor() : ViewModel() {
    private val _destinationId = SingleSharedFlow<@IdRes Int>()
    val destinationId: SharedFlow<Int>
        get() = _destinationId

    private val _selectedItemId = SharedStateLikeFlow<@IdRes Int>()
    val selectedItemId: SharedFlow<Int>
        get() = _selectedItemId

    private val _actionBarTitle = SharedStateLikeFlow<String>()
    val actionBarTitle: SharedFlow<String>
        get() = _actionBarTitle

    private val _exitApp = SingleSharedFlow<Unit>()
    val exitApp: SharedFlow<Unit>
        get() = _exitApp

    private val _pressBack = SingleSharedFlow<Unit>()
    val pressBack: SharedFlow<Unit>
        get() = _pressBack

    private var currentDestinationId: Int = Int.MIN_VALUE

    fun onNavigationItemSelected(@IdRes itemId: Int): Boolean {
        _destinationId.tryEmit(
            when (itemId) {
                R.id.converterNavigation -> R.id.converterFragment
                R.id.popularNavigation -> R.id.popularFragment
                R.id.historyNavigation -> R.id.historyFragment
                R.id.settingsNavigation -> R.id.settingsFragment
                else -> throw IllegalArgumentException("Unknown itemId: $itemId")
            }
        )
        return true
    }

    @Suppress("UNUSED_PARAMETER")
    fun onNavigationItemReselected(@IdRes itemId: Int) {
        // Do nothing
    }

    fun onBackPressed() {
        when {
            currentDestinationId == R.id.chooserFragment ||
                    currentDestinationId == R.id.dropdownMenuFragment -> _pressBack.tryEmit(Unit)
            currentDestinationId != R.id.converterFragment -> {
                _selectedItemId.tryEmit(R.id.converterNavigation)
            }
            else -> _exitApp.tryEmit(Unit)
        }
    }

    fun onDestinationChanged(destinationId: Int, actionBarTitle: String) {
        currentDestinationId = destinationId
        _actionBarTitle.tryEmit(actionBarTitle)
    }
}