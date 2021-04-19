package ba.grbo.currencyconverter.ui.adapters

import android.graphics.drawable.Drawable
import android.widget.TextView
import androidx.databinding.BindingAdapter

@BindingAdapter("startDrawable")
fun TextView.bindStartDrawable(flag: Drawable?) {
    setCompoundDrawablesWithIntrinsicBounds(flag, null, null, null)
}