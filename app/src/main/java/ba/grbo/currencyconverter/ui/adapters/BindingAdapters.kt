package ba.grbo.currencyconverter.ui.adapters

import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.databinding.BindingAdapter
import ba.grbo.currencyconverter.R
import ba.grbo.currencyconverter.databinding.DropdownCurrencyChooserBinding
import kotlin.math.roundToInt

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

@BindingAdapter("customMarginEnd")
fun View.bindCustomMarginEnd(value: Float) {
    val layoutParams = layoutParams as ViewGroup.MarginLayoutParams
    layoutParams.marginEnd = value.roundToInt()
    this.layoutParams = layoutParams
}

@BindingAdapter("customMargin", "extended", requireAll = true)
fun View.bindCustomMargin(value: Float, extended: Boolean) {
    val layoutParams = layoutParams as ViewGroup.MarginLayoutParams
    val margin = value.roundToInt()
    layoutParams.run {
        bottomMargin = margin
        if (extended) {
            marginStart = margin
            marginEnd = margin
            topMargin = margin
        }
    }
    this.layoutParams = layoutParams
}

@BindingAdapter("targetView", "toView", "toBinding", "extended", requireAll = true)
fun ConstraintLayout.bindCustomConstraints(
    targetView: View,
    toView: View,
    toBinding: DropdownCurrencyChooserBinding,
    extended: Boolean
) {
    val view = if (extended) toView else toBinding.dropdownLayout
    ConstraintSet().apply {
        clone(this@bindCustomConstraints)
        connect(
            targetView.id,
            ConstraintSet.TOP,
            view.id,
            if (extended) ConstraintSet.TOP else ConstraintSet.BOTTOM
        )
        connect(
            targetView.id,
            ConstraintSet.BOTTOM,
            id,
            ConstraintSet.BOTTOM
        )
        connect(
            targetView.id,
            ConstraintSet.START,
            view.id,
            ConstraintSet.START
        )
        connect(
            targetView.id,
            ConstraintSet.END,
            view.id,
            ConstraintSet.END
        )
        applyTo(this@bindCustomConstraints)
    }
}