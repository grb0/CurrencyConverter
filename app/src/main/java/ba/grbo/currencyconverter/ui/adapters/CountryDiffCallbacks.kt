package ba.grbo.currencyconverter.ui.adapters

import androidx.recyclerview.widget.DiffUtil
import ba.grbo.currencyconverter.data.models.domain.ExchangeableCurrency

class CountryDiffCallbacks : DiffUtil.ItemCallback<ExchangeableCurrency>() {
    override fun areItemsTheSame(
        oldItem: ExchangeableCurrency,
        newItem: ExchangeableCurrency
    ) = oldItem === newItem

    override fun areContentsTheSame(
        oldItem: ExchangeableCurrency,
        newItem: ExchangeableCurrency
    ) = oldItem == newItem
}