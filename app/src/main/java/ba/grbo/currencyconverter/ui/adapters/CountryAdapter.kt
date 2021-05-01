package ba.grbo.currencyconverter.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ba.grbo.currencyconverter.data.models.Country
import ba.grbo.currencyconverter.data.models.Currency
import ba.grbo.currencyconverter.databinding.DropdownItemBinding
import ba.grbo.currencyconverter.ui.miscs.FavoritesAnimator
import ba.grbo.currencyconverter.util.getScaleAndFadeAnimatorProducersPair

class CountryAdapter(
    private val uiName: Currency.UiName,
    private val showScrollbar: Boolean,
    private val onClick: (Country) -> Unit,
    private val onCurrentListChanged: () -> Unit,
    private val onFavoritesAnimationEnd: (Country, Boolean, Int) -> Unit,
) : ListAdapter<Country, CountryAdapter.CountryHolder>(CountryDiffCallbacks()) {
    class CountryHolder private constructor(
        private val binding: DropdownItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        private lateinit var favoritesAnimator: FavoritesAnimator

        companion object {
            fun from(
                parent: ViewGroup,
                showScrollbar: Boolean,
            ) = CountryHolder(
                DropdownItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ).apply { this.showScrollbar = showScrollbar }
            )
        }

        fun bind(
            country: Country,
            uiName: Currency.UiName,
            onClick: (Country) -> Unit,
            onFavoritesAnimationEnd: (Country, Boolean, Int) -> Unit
        ) {
            favoritesAnimator = FavoritesAnimator(
                binding.favoritesAnimation.getScaleAndFadeAnimatorProducersPair(),
            ) { onFavoritesAnimationEnd(country, it, adapterPosition) }

            binding.favorites.setOnClickListener {
                favoritesAnimator.onFavoritesClicked(!country.favorite)
            }

            binding.root.setOnClickListener { onClick(country) }

            binding.country = country
            binding.uiName = uiName
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryHolder {
        return CountryHolder.from(parent, showScrollbar)
    }

    override fun onBindViewHolder(holder: CountryHolder, position: Int) {
        holder.bind(getItem(position), uiName, onClick, onFavoritesAnimationEnd)
    }

    override fun onCurrentListChanged(
        previousList: MutableList<Country>,
        currentList: MutableList<Country>
    ) {
        onCurrentListChanged()
    }
}