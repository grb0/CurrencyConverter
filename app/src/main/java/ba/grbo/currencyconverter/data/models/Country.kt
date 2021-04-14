package ba.grbo.currencyconverter.data.models

import androidx.annotation.DrawableRes

data class Country(
        val currency: Currency,
        @DrawableRes val flag: Int
)