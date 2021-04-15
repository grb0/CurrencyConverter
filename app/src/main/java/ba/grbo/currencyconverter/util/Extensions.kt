package ba.grbo.currencyconverter.util

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.core.content.ContextCompat

fun Context.getColorFromAttribute(@AttrRes id: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(id, typedValue, true)
    return ContextCompat.getColor(this, typedValue.resourceId)
}