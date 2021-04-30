package ba.grbo.currencyconverter.ui.viewmodels

import androidx.annotation.IdRes
import androidx.lifecycle.ViewModel
import ba.grbo.currencyconverter.R
import ba.grbo.currencyconverter.util.SingleSharedFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

@HiltViewModel
class ChooserViewModel @Inject constructor() : ViewModel() {
    private val _toDropdownMenuFragment = SingleSharedFlow<@IdRes Int>()
    val toDropdownMenuFragment: SharedFlow<Int>
        get() = _toDropdownMenuFragment

    private val _uiNameChanged = SingleSharedFlow<String>()
    val uiNameChanged: SharedFlow<String>
        get() = _uiNameChanged

    fun onUiNameChanged(currentUiName: String, newUiName: String): Boolean {
        return if (currentUiName != newUiName) {
            _uiNameChanged.tryEmit(newUiName)
            true
        } else false
    }

    fun onDropdownMenuClicked(): Boolean {
        _toDropdownMenuFragment.tryEmit(R.id.action_chooserFragment_to_dropdownMenuFragment)
        return true
    }
}