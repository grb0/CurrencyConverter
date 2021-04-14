package ba.grbo.currencyconverter.ui.adapters

import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.databinding.BindingAdapter

@BindingAdapter("startDrawable")
fun TextView.bindStartDrawable(@DrawableRes flag: Int) {
    setCompoundDrawablesWithIntrinsicBounds(flag, 0, 0, 0)
}