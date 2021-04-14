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
        private val currencyName: CurrencyName
) : ListAdapter<Country, CountryAdapter.ViewHolder>(CountryDiffCallbacks()) {
    class ViewHolder private constructor(
            private val binding: DropdownItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup, lifecycleOwner: LifecycleOwner) = ViewHolder(
                    DropdownItemBinding.inflate(
                            LayoutInflater.from(parent.context),
                            parent,
                            false
                    ).apply { this.lifecycleOwner = lifecycleOwner }
            )
        }

        fun bind(country: Country, currencyName: CurrencyName) {
            binding.country = country
            binding.currencyName = currencyName
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent, lifecycleOwner)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), currencyName)
    }
}