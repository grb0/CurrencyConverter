package ba.grbo.currencyconverter.ui.viewmodels

import androidx.lifecycle.ViewModel
import ba.grbo.currencyconverter.util.SingleSharedFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

@HiltViewModel
class DropdownMenuViewModel @Inject constructor() : ViewModel() {
    private val _filterByChanged = SingleSharedFlow<String>()
    val filterBy: SharedFlow<String>
        get() = _filterByChanged

    fun onFilterByChanged(currentFilterBy: String, newFilterBy: String): Boolean {
        return if (currentFilterBy != newFilterBy) {
            _filterByChanged.tryEmit(newFilterBy)
            true
        } else false
    }
}