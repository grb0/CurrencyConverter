package ba.grbo.currencyconverter.ui.adapters

import androidx.recyclerview.widget.DiffUtil
import ba.grbo.currencyconverter.data.models.Country

class CountryDiffCallbacks : DiffUtil.ItemCallback<Country>() {
    override fun areItemsTheSame(oldItem: Country, newItem: Country) = oldItem === newItem

    override fun areContentsTheSame(oldItem: Country, newItem: Country) = oldItem == newItem
}