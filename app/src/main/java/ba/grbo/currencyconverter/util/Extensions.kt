package ba.grbo.currencyconverter.util

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.core.content.ContextCompat

fun Context.getColorFromAttr(@AttrRes attrRes: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attrRes, typedValue, true)
    return ContextCompat.getColor(this, typedValue.resourceId)
}