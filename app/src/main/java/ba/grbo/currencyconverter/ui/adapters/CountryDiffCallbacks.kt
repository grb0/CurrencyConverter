package ba.grbo.currencyconverter.ui.adapters

import androidx.recyclerview.widget.DiffUtil
import ba.grbo.currencyconverter.data.models.domain.Currency

class CountryDiffCallbacks : DiffUtil.ItemCallback<Currency>() {
    override fun areItemsTheSame(oldItem: Currency, newItem: Currency) = oldItem === newItem

    override fun areContentsTheSame(oldItem: Currency, newItem: Currency) = oldItem == newItem
}