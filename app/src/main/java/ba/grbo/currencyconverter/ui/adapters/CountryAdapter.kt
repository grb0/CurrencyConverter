package ba.grbo.currencyconverter.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ba.grbo.currencyconverter.data.models.domain.ExchangeableCurrency
import ba.grbo.currencyconverter.databinding.DropdownItemBinding
import ba.grbo.currencyconverter.ui.miscs.FavoritesAnimator
import ba.grbo.currencyconverter.util.getScaleAndFadeAnimatorProducersPair

class CountryAdapter(
    private val uiName: ExchangeableCurrency.UiName,
    private val showScrollbar: Boolean,
    private val onClick: (ExchangeableCurrency) -> Unit,
    private val onCurrentListChanged: () -> Unit,
    private val onFavoritesAnimationEnd: (ExchangeableCurrency, Boolean, Int) -> Unit,
) : ListAdapter<ExchangeableCurrency, CountryAdapter.CountryHolder>(CountryDiffCallbacks()) {
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
            currency: ExchangeableCurrency,
            uiName: ExchangeableCurrency.UiName,
            onClick: (ExchangeableCurrency) -> Unit,
            onFavoritesAnimationEnd: (ExchangeableCurrency, Boolean, Int) -> Unit
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
        previousList: MutableList<ExchangeableCurrency>,
        currentList: MutableList<ExchangeableCurrency>
    ) {
        onCurrentListChanged()
    }
}