package ba.grbo.currencyconverter.data.models.managers

import android.content.Context
import androidx.annotation.AttrRes
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import ba.grbo.currencyconverter.R
import ba.grbo.currencyconverter.util.getColorFromAttribute

class Colors(private val context: Context) {
    val WHITE = getColorFromResource(R.color.white)
    val BLACK = getColorFromResource(R.color.black)
    val LIGHT_GRAY = getColorFromResource(R.color.light_gray)
    val BORDER = getColorFromResource(R.color.border)
    val DIVIDER = getColorFromResource(R.color.divider)
    val PRIMARY = getColorFromAttribute(R.attr.colorPrimary)
    val PRIMARY_VARIANT = getColorFromAttribute(R.attr.colorPrimaryVariant)
    val ON_PRIMARY = getColorFromAttribute(R.attr.colorOnPrimary)
    val CONTROL_NORMAL = getColorFromAttribute(R.attr.colorControlNormal)


    private fun getColorFromResource(@ColorRes id: Int): Int {
        return ContextCompat.getColor(context, id)
    }

    private fun getColorFromAttribute(@AttrRes id: Int): Int {
        return context.getColorFromAttribute(id)
    }
}