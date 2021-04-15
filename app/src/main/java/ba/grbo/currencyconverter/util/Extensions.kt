package ba.grbo.currencyconverter.util

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
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

fun <T> Fragment.collectWhenStarted(flow: Flow<T>, action: (T) -> Unit) {
    flow.flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { action(it) }
            .launchIn(lifecycleScope)
}