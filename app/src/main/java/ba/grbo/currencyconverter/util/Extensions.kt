package ba.grbo.currencyconverter.util

import android.animation.ObjectAnimator
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
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

fun ObjectAnimator.setUp(resources: Resources): ObjectAnimator {
    interpolator = LinearInterpolator()
    duration = resources.getInteger(R.integer.anim_time).toLong()
    return this
}

fun Context.updateLocale(locale: Locale): ContextWrapper {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        val localeList = LocaleList(locale)
        LocaleList.setDefault(localeList)
        resources.configuration.setLocales(localeList)
    } else resources.configuration.locale = locale

    var context: Context = this
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
        context = context.createConfigurationContext(resources.configuration)
    } else resources.updateConfiguration(resources.configuration, resources.displayMetrics)

    return ContextWrapper(context)
}