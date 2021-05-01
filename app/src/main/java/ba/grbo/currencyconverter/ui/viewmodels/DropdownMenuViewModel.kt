package ba.grbo.currencyconverter.ui.viewmodels

import androidx.lifecycle.ViewModel
import ba.grbo.currencyconverter.util.SingleSharedFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

@HiltViewModel
class DropdownMenuViewModel @Inject constructor() : ViewModel() {
    private val _showScrollbarChanged = SingleSharedFlow<Boolean>()
    val showScrollbarChanged: SharedFlow<Boolean>
        get() = _showScrollbarChanged

    private val _filterByChanged = SingleSharedFlow<String>()
    val filterBy: SharedFlow<String>
        get() = _filterByChanged

    fun onShowScrollbarChanged(currentShowScrollbar: Boolean, newShowScrollbar: Boolean): Boolean {
        return if (currentShowScrollbar != newShowScrollbar) {
            _showScrollbarChanged.tryEmit(newShowScrollbar)
            true
        } else false
    }

    fun onFilterByChanged(currentFilterBy: String, newFilterBy: String): Boolean {
        return if (currentFilterBy != newFilterBy) {
            _filterByChanged.tryEmit(newFilterBy)
            true
        } else false
    }
}