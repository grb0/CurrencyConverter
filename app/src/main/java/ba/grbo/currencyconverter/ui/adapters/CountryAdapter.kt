package ba.grbo.currencyconverter.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ba.grbo.currencyconverter.data.models.domain.Currency
import ba.grbo.currencyconverter.databinding.DropdownItemBinding
import ba.grbo.currencyconverter.ui.miscs.FavoritesAnimator
import ba.grbo.currencyconverter.util.getScaleAndFadeAnimatorProducersPair

class CountryAdapter(
    private val uiName: Currency.UiName,
    private val showScrollbar: Boolean,
    private val onClick: (Currency) -> Unit,
    private val onCurrentListChanged: () -> Unit,
    private val onFavoritesAnimationEnd: (Currency, Boolean, Int) -> Unit,
) : ListAdapter<Currency, CountryAdapter.CountryHolder>(CountryDiffCallbacks()) {
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
            currency: Currency,
            uiName: Currency.UiName,
            onClick: (Currency) -> Unit,
            onFavoritesAnimationEnd: (Currency, Boolean, Int) -> Unit
        ) {
            favoritesAnimator = FavoritesAnimator(
                binding.favoritesAnimation.getScaleAndFadeAnimatorProducersPair(),
            ) { onFavoritesAnimationEnd(currency, it, adapterPosition) }

            binding.favorites.setOnClickListener {
                favoritesAnimator.onFavoritesClicked(!currency.isFavorite)
            }

            binding.root.setOnClickListener { onClick(currency) }

            binding.curr = currency
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
        previousList: MutableList<Currency>,
        currentList: MutableList<Currency>
    ) {
        onCurrentListChanged()
    }
}