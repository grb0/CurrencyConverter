package ba.grbo.currencyconverter.util

import android.animation.ObjectAnimator
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import android.os.Build
import android.util.TypedValue
import android.view.animation.LinearInterpolator
import androidx.annotation.AttrRes
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
import java.util.*

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
    distinctUntilChanged: Boolean = true
) {
    if (distinctUntilChanged) distinctUntilChanged()
        .flowWithLifecycle(lifecycle, lifecycleState)
        .onEach { action(it) }
        .launchIn(scope)
    else flowWithLifecycle(lifecycle, lifecycleState)
        .onEach { action(it) }
        .launchIn(scope)
}

private fun <T> Fragment.collect(
    flow: Flow<T>,
    action: suspend (T) -> Unit,
    lifecycleState: Lifecycle.State,
    distinctUntilChanged: Boolean = true,
) {
    flow.collect(
        viewLifecycleOwner.lifecycle,
        lifecycleState,
        viewLifecycleOwner.lifecycleScope,
        action,
        distinctUntilChanged,
    )
}

fun <T> Fragment.collectWhenStarted(
    flow: Flow<T>,
    action: suspend (T) -> Unit,
    distinctUntilChanged: Boolean = true
) {
    collect(flow, action, Lifecycle.State.STARTED, distinctUntilChanged)
}

fun ObjectAnimator.setUp(resources: Resources): ObjectAnimator {
    interpolator = LinearInterpolator()
    duration = resources.getInteger(R.integer.anim_time).toLong()
    return this
}

fun Context.updateLocale(language: Locale): ContextWrapper {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        resources.configuration.setLocale(language)
    } else resources.configuration.locale = language

    var context: Context = this
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
        context = context.createConfigurationContext(resources.configuration)
    } else resources.updateConfiguration(context.resources.configuration, resources.displayMetrics)

    return ContextWrapper(context)
}

@Suppress("FunctionName", "UNCHECKED_CAST")
fun <T> SingleSharedFlow() = MutableSharedFlow<T>(
    onBufferOverflow = BufferOverflow.DROP_OLDEST,
    extraBufferCapacity = 1
)