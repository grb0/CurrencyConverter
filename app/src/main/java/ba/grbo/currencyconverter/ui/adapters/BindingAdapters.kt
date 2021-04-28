package ba.grbo.currencyconverter.ui.adapters

import android.graphics.drawable.Drawable
import android.widget.ImageButton
import android.widget.TextView
import androidx.databinding.BindingAdapter
import ba.grbo.currencyconverter.R
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.Favorites

@BindingAdapter("startDrawable")
fun TextView.bindStartDrawable(flag: Drawable?) {
    setCompoundDrawablesWithIntrinsicBounds(flag, null, null, null)
}

@BindingAdapter("customDrawableResource")
fun ImageButton.bindCustomDrawableResource(favorites: Favorites?) {
    favorites?.let {
        setImageResource(
            when (favorites) {
                Favorites.EMPTY -> R.drawable.ic_favorite_empty
                Favorites.FILLED -> R.drawable.ic_favorite_filled
                else -> throw IllegalArgumentException("$favorites is not allowed here")
            }
        )
    }
}