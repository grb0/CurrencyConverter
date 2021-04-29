package ba.grbo.currencyconverter.data.models

import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class Country(
    val currency: Currency,
    val flag: Drawable,
    val favorite: Boolean
) {
    data class Resources(
        @StringRes val currencyName: Int,
        val currencyCode: Code,
        @DrawableRes val flag: Int
    )
}