package ba.grbo.currencyconverter.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ba.grbo.currencyconverter.data.models.Country
import ba.grbo.currencyconverter.data.models.Currency
import ba.grbo.currencyconverter.databinding.DropdownItemBinding

class CountryAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val uiName: Currency.UiName,
    private val onClick: (Country) -> Unit,
    private val onCurrentListChanged: () -> Unit
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

        fun bind(country: Country, uiName: Currency.UiName, onClick: (Country) -> Unit) {
            binding.root.setOnClickListener { onClick(country) }
            binding.country = country
            binding.uiName = uiName
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryHolder {
        return CountryHolder.from(parent, lifecycleOwner)
    }

    override fun onBindViewHolder(holder: CountryHolder, position: Int) {
        holder.bind(getItem(position), uiName, onClick)
    }

    override fun onCurrentListChanged(
        previousList: MutableList<Country>,
        currentList: MutableList<Country>
    ) {
        onCurrentListChanged()
    }
}