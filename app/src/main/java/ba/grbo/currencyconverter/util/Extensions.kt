package ba.grbo.currencyconverter.util

import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import android.view.animation.LinearInterpolator
import androidx.annotation.AttrRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import ba.grbo.currencyconverter.R
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.SearcherState.FOCUSING
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.SearcherState.UNFOCUSING
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*

fun Context.getColorFromAttribute(@AttrRes id: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(id, typedValue, true)
    return ContextCompat.getColor(this, typedValue.resourceId)
}

fun Float.toPixels(resources: Resources) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    this,
    resources.displayMetrics
)

fun Boolean.toSearcherState() = if (this) FOCUSING else UNFOCUSING

private fun <T> Flow<T>.collect(
    lifecycle: Lifecycle,
    lifecycleState: Lifecycle.State,
    scope: LifecycleCoroutineScope,
    action: suspend (T) -> Unit,
    shouldDebounce: Boolean,
    debounceTimeout: Long
) {
    if (!shouldDebounce) distinctUntilChanged()
        .flowWithLifecycle(lifecycle, lifecycleState)
        .onEach { action(it) }
        .launchIn(scope)
    else debounce(debounceTimeout)
        .distinctUntilChanged()
        .flowWithLifecycle(lifecycle, lifecycleState)
        .onEach { action(it) }
        .launchIn(scope)

}

private fun <T> Fragment.collect(
    flow: Flow<T>,
    action: suspend (T) -> Unit,
    lifecycleState: Lifecycle.State,
    shouldDebounce: Boolean = false,
    debounceTimeout: Long = 500
) {
    flow.collect(
        viewLifecycleOwner.lifecycle,
        lifecycleState,
        viewLifecycleOwner.lifecycleScope,
        action,
        shouldDebounce,
        debounceTimeout
    )

}

fun <T> Fragment.collectWhenStarted(
    flow: Flow<T>,
    action: suspend (T) -> Unit,
    shouldDebounce: Boolean = false,
    debounceTimeout: Long = 500
) {
    collect(flow, action, Lifecycle.State.STARTED, shouldDebounce, debounceTimeout)
}

private fun <T> AppCompatActivity.collect(
    flow: Flow<T>,
    action: suspend (T) -> Unit,
    lifecycleState: Lifecycle.State,
    shouldDebounce: Boolean = false,
    debounceTimeout: Long = 500
) {
    flow.collect(
        lifecycle,
        lifecycleState,
        lifecycleScope,
        action,
        shouldDebounce,
        debounceTimeout
    )
}

fun <T> AppCompatActivity.collectWhenStarted(
    flow: Flow<T>,
    action: suspend (T) -> Unit,
    shouldDebounce: Boolean = false,
    debounceTimeout: Long = 500
) {
    collect(flow, action, Lifecycle.State.STARTED, shouldDebounce, debounceTimeout)
}

fun ObjectAnimator.setUp(resources: Resources): ObjectAnimator {
    interpolator = LinearInterpolator()
    duration = resources.getInteger(R.integer.anim_time).toLong()
    return this
}

@Suppress("FunctionName", "UNCHECKED_CAST")
fun <T> SingleSharedFlow() = MutableSharedFlow<T>(
    onBufferOverflow = BufferOverflow.DROP_OLDEST,
    extraBufferCapacity = 1
)