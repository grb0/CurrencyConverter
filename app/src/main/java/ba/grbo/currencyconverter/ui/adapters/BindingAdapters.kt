package ba.grbo.currencyconverter.ui.adapters

import android.graphics.drawable.Drawable
import android.widget.ImageButton
import android.widget.TextView
import androidx.databinding.BindingAdapter
import ba.grbo.currencyconverter.R

@BindingAdapter("startDrawable")
fun TextView.bindStartDrawable(flag: Drawable?) {
    setCompoundDrawablesWithIntrinsicBounds(flag, null, null, null)
}

@BindingAdapter("favoritesIcon")
fun ImageButton.bindFavoritesIcon(favorite: Boolean?) {
    favorite?.let {
        setImageResource(if (it) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_empty)
    }
}