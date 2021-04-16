package ba.grbo.currencyconverter.util

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.SearcherState.FOCUSING
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.SearcherState.UNFOCUSING
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

fun Context.getColorFromAttribute(@AttrRes id: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(id, typedValue, true)
    return ContextCompat.getColor(this, typedValue.resourceId)
}

// fun Float.toPixels(resources: Resources) = TypedValue.applyDimension(
//         TypedValue.COMPLEX_UNIT_DIP,
//         this,
//         resources.displayMetrics
// )

fun Boolean.toSearcherState() = if (this) FOCUSING else UNFOCUSING

private fun <T> Flow<T>.collect(
        lifecycle: Lifecycle,
        lifecycleState: Lifecycle.State,
        scope: LifecycleCoroutineScope,
        action: suspend (T) -> Unit
) {
    flowWithLifecycle(lifecycle, lifecycleState).onEach { action(it) }.launchIn(scope)
}

private fun <T> Fragment.collect(
        flow: Flow<T>,
        action: suspend (T) -> Unit,
        lifecycleState: Lifecycle.State
) {
    flow.collect(
            viewLifecycleOwner.lifecycle,
            lifecycleState,
            viewLifecycleOwner.lifecycleScope,
            action
    )

}

fun <T> Fragment.collectWhenStarted(flow: Flow<T>, action: suspend (T) -> Unit) {
    collect(flow, action, Lifecycle.State.STARTED)
}