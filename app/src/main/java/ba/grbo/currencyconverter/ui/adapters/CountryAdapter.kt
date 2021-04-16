package ba.grbo.currencyconverter.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ba.grbo.currencyconverter.data.models.Country
import ba.grbo.currencyconverter.data.models.CurrencyName
import ba.grbo.currencyconverter.databinding.DropdownItemBinding

class CountryAdapter(
        private val lifecycleOwner: LifecycleOwner,
        private val currencyName: CurrencyName,
        private val onClick: (Country) -> Unit
) : ListAdapter<Country, CountryAdapter.CountryHolder>(CountryDiffCallbacks()) {
    class CountryHolder private constructor(
            private val binding: DropdownItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup, lifecycleOwner: LifecycleOwner) = CountryHolder(
                    DropdownItemBinding.inflate(
                            LayoutInflater.from(parent.context),
                            parent,
                            false
                    ).apply { this.lifecycleOwner = lifecycleOwner }
            )
        }

        fun bind(country: Country, currencyName: CurrencyName, onClick: (Country) -> Unit) {
            binding.root.setOnClickListener { onClick(country) }
            binding.country = country
            binding.currencyName = currencyName
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryHolder {
        return CountryHolder.from(parent, lifecycleOwner)
    }

    override fun onBindViewHolder(holder: CountryHolder, position: Int) {
        holder.bind(getItem(position), currencyName, onClick)
    }
}