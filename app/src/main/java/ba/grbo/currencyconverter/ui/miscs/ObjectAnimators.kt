package ba.grbo.currencyconverter.ui.miscs

import android.animation.ObjectAnimator
import androidx.lifecycle.LifecycleCoroutineScope
import ba.grbo.currencyconverter.databinding.FragmentConverterBinding
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.Dropdown
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.Dropdown.FROM
import ba.grbo.currencyconverter.util.*
import kotlinx.coroutines.launch

@Suppress("PrivatePropertyName", "PropertyName")
class ObjectAnimators(
    binding: FragmentConverterBinding,
    bottomNavigationAnimator: ObjectAnimator,
    Colors: Colors,
    landscape: Boolean,
    width: Float,
    onFavoritesAnimationEnd: (Boolean) -> Unit
) {
    private val fadeInAnimatorProducer =
        binding.dropdownMenu.dropdownMenuCard.getFadeInAnimatorProducer()
    private val fadeOutAnimatorProducer =
        binding.dropdownMenu.dropdownMenuCard.getFadeOutAnimatorProducer()

    private val favoritesAnimator = FavoritesAnimator(
        binding.dropdownMenu.favoritesAnimation.getRotateAroundYAnimatorProducersPair(),
        onFavoritesAnimationEnd
    )

    private var CURRENCIES_CARD_FADE_IN_DEFAULT = fadeInAnimatorProducer(0f)
    private var CURRENCIES_CARD_FADE_OUT_DEFAULT = fadeOutAnimatorProducer(1f)

    private val CONVERTER_LAYOUT = binding.converterLayout.getAnimator(
        Colors.WHITE,
        Colors.LIGHT_GRAY
    )
    private val DROPDOWN_SWAPPER = binding.dropdownSwapper.getBackgroundColorAnimator(
        Colors.CONTROL_NORMAL,
        Colors.DIVIDER
    )
    private val BOTTOM_NAVIGATION = bottomNavigationAnimator
    private val CURRENCY_SWAPPING = binding.dropdownSwapper.getRotationAnimatior()

    private var CURRENCIES_CARD_FADE_IN = fadeInAnimatorProducer(0f)
    private var CURRENCIES_CARD_FADE_OUT = fadeOutAnimatorProducer(1f)

    private val From = object : Base {
        override val DROPDOWN_ACTION: ObjectAnimator
        override val DROPDOWN_TITLE: ObjectAnimator
        override val CURRENCY_LAYOUT: ObjectAnimator
        override val CURRENCY_DOUBLE: ObjectAnimator

        init {
            binding.fromCurrencyChooser.run {
                DROPDOWN_ACTION = dropdownAction.getAnimator(
                    Colors.CONTROL_NORMAL,
                    Colors.PRIMARY_VARIANT
                )
                DROPDOWN_TITLE = dropdownTitle.getAnimator(
                    Colors.WHITE,
                    Colors.LIGHT_GRAY,
                    Colors.BORDER,
                    Colors.PRIMARY_VARIANT
                )
                CURRENCY_LAYOUT = currencyLayout.getAnimator(
                    Colors.BORDER,
                    Colors.PRIMARY_VARIANT,
                    Colors.WHITE,
                    Colors.LIGHT_GRAY
                )
                CURRENCY_DOUBLE = binding.fromCurrencyDouble.getTranslationAnimator(
                    true,
                    landscape,
                    width
                )
            }
        }
    }

    val To = object : Base {
        override val DROPDOWN_ACTION: ObjectAnimator
        override val DROPDOWN_TITLE: ObjectAnimator
        override val CURRENCY_LAYOUT: ObjectAnimator
        override val CURRENCY_DOUBLE: ObjectAnimator

        init {
            binding.toCurrencyChooser.run {
                DROPDOWN_ACTION = dropdownAction.getAnimator(
                    Colors.CONTROL_NORMAL,
                    Colors.PRIMARY_VARIANT
                )
                DROPDOWN_TITLE = dropdownTitle.getAnimator(
                    Colors.WHITE,
                    Colors.LIGHT_GRAY,
                    Colors.BORDER,
                    Colors.PRIMARY_VARIANT
                )
                CURRENCY_LAYOUT = currencyLayout.getAnimator(
                    Colors.BORDER,
                    Colors.PRIMARY_VARIANT,
                    Colors.WHITE,
                    Colors.LIGHT_GRAY
                )
                CURRENCY_DOUBLE = binding.toCurrencyDouble.getTranslationAnimator(
                    false,
                    landscape,
                    width
                )
            }
        }
    }

    private val FromTo = object : Extended {
        override val DROPDOWN_ACTION: ObjectAnimator
        override val DROPDOWN_TITLE: ObjectAnimator
        override val CURRENCY_LAYOUT: ObjectAnimator
        override val CURRENCY: ObjectAnimator
        override val CURRENCY_DOUBLE: ObjectAnimator

        init {
            binding.toCurrencyChooser.run {
                DROPDOWN_ACTION = dropdownAction.getBackgroundColorAnimator(
                    Colors.CONTROL_NORMAL,
                    Colors.DIVIDER
                )
                DROPDOWN_TITLE = dropdownTitle.getBackgroundAnimator(
                    Colors.WHITE,
                    Colors.LIGHT_GRAY
                )
                CURRENCY_LAYOUT = currencyLayout.getBackgroundAnimator(
                    Colors.BORDER,
                    Colors.WHITE,
                    Colors.LIGHT_GRAY
                )
                CURRENCY = currency.getCurrencyAnimator(Colors.BLACK, Colors.BORDER)
                CURRENCY_DOUBLE = currencyDouble.getCurrencyAnimator(
                    Colors.BLACK,
                    Colors.BORDER
                )
            }
        }
    }

    private val ToFrom = object : Extended {
        override val DROPDOWN_ACTION: ObjectAnimator
        override val DROPDOWN_TITLE: ObjectAnimator
        override val CURRENCY_LAYOUT: ObjectAnimator
        override val CURRENCY: ObjectAnimator
        override val CURRENCY_DOUBLE: ObjectAnimator

        init {
            binding.fromCurrencyChooser.run {
                DROPDOWN_ACTION = dropdownAction.getBackgroundColorAnimator(
                    Colors.CONTROL_NORMAL,
                    Colors.DIVIDER
                )
                DROPDOWN_TITLE = dropdownTitle.getBackgroundAnimator(
                    Colors.WHITE,
                    Colors.LIGHT_GRAY
                )
                CURRENCY_LAYOUT = currencyLayout.getBackgroundAnimator(
                    Colors.BORDER,
                    Colors.WHITE,
                    Colors.LIGHT_GRAY
                )
                CURRENCY = currency.getCurrencyAnimator(Colors.BLACK, Colors.BORDER)
                CURRENCY_DOUBLE = currencyDouble.getCurrencyAnimator(
                    Colors.BLACK,
                    Colors.BORDER
                )
            }
        }
    }

    interface Base {
        val DROPDOWN_ACTION: ObjectAnimator
        val DROPDOWN_TITLE: ObjectAnimator
        val CURRENCY_LAYOUT: ObjectAnimator
        val CURRENCY_DOUBLE: ObjectAnimator
    }

    interface Extended : Base {
        val CURRENCY: ObjectAnimator
    }


    private fun ObjectAnimator.begin() {
        if (isRunning) reverse()
        else start()
    }

    private fun fadeInCurrenciesCard() {
        CURRENCIES_CARD_FADE_IN = if (CURRENCIES_CARD_FADE_OUT.isRunning) {
            val time = CURRENCIES_CARD_FADE_OUT.currentPlayTime
            CURRENCIES_CARD_FADE_OUT.cancel()
            fadeInAnimatorProducer(0f).apply {
                currentPlayTime = duration - time
                start()
            }
        } else CURRENCIES_CARD_FADE_IN_DEFAULT.apply { start() }
    }

    private fun fadeOutCurrenciesCard() {
        CURRENCIES_CARD_FADE_OUT = if (CURRENCIES_CARD_FADE_IN.isRunning) {
            val time = CURRENCIES_CARD_FADE_IN.currentPlayTime
            CURRENCIES_CARD_FADE_IN.cancel()
            fadeOutAnimatorProducer(1f).apply {
                currentPlayTime = duration - time
                start()
            }
        } else CURRENCIES_CARD_FADE_OUT_DEFAULT.apply { start() }
    }

    private fun startFromObjectAnimators(scope: LifecycleCoroutineScope) {
        scope.launchWhenStarted {
            launch { fadeInCurrenciesCard() }
            launch { From.CURRENCY_LAYOUT.begin() }
            launch { From.DROPDOWN_ACTION.begin() }
            launch { From.DROPDOWN_TITLE.begin() }
            launch { CONVERTER_LAYOUT.begin() }
            launch { FromTo.CURRENCY_LAYOUT.begin() }
            launch { FromTo.DROPDOWN_TITLE.begin() }
            launch { FromTo.CURRENCY.begin() }
            launch { FromTo.CURRENCY_DOUBLE.begin() }
            launch { FromTo.DROPDOWN_ACTION.begin() }
            launch { BOTTOM_NAVIGATION.begin() }
            launch { DROPDOWN_SWAPPER.begin() }
        }
    }

    private fun startToObjectAnimators(scope: LifecycleCoroutineScope) {
        scope.launchWhenStarted {
            launch { fadeInCurrenciesCard() }
            launch { To.CURRENCY_LAYOUT.begin() }
            launch { To.DROPDOWN_ACTION.begin() }
            launch { To.DROPDOWN_TITLE.begin() }
            launch { CONVERTER_LAYOUT.begin() }
            launch { ToFrom.CURRENCY_LAYOUT.begin() }
            launch { ToFrom.DROPDOWN_TITLE.begin() }
            launch { ToFrom.CURRENCY.begin() }
            launch { ToFrom.CURRENCY_DOUBLE.begin() }
            launch { ToFrom.DROPDOWN_ACTION.begin() }
            launch { BOTTOM_NAVIGATION.begin() }
            launch { DROPDOWN_SWAPPER.begin() }
        }
    }

    private fun reverseFromObjectAnimators(scope: LifecycleCoroutineScope) {
        scope.launchWhenStarted {
            launch { fadeOutCurrenciesCard() }
            launch { From.CURRENCY_LAYOUT.reverse() }
            launch { From.DROPDOWN_ACTION.reverse() }
            launch { From.DROPDOWN_TITLE.reverse() }
            launch { CONVERTER_LAYOUT.reverse() }
            launch { FromTo.CURRENCY_LAYOUT.reverse() }
            launch { FromTo.DROPDOWN_TITLE.reverse() }
            launch { FromTo.CURRENCY.reverse() }
            launch { FromTo.CURRENCY_DOUBLE.reverse() }
            launch { FromTo.DROPDOWN_ACTION.reverse() }
            launch { BOTTOM_NAVIGATION.reverse() }
            launch { DROPDOWN_SWAPPER.reverse() }
        }
    }

    private fun reverseToObjectAnimators(scope: LifecycleCoroutineScope) {
        scope.launchWhenStarted {
            launch { fadeOutCurrenciesCard() }
            launch { To.CURRENCY_LAYOUT.reverse() }
            launch { To.DROPDOWN_ACTION.reverse() }
            launch { To.DROPDOWN_TITLE.reverse() }
            launch { CONVERTER_LAYOUT.reverse() }
            launch { ToFrom.CURRENCY_LAYOUT.reverse() }
            launch { ToFrom.DROPDOWN_TITLE.reverse() }
            launch { ToFrom.CURRENCY.reverse() }
            launch { ToFrom.CURRENCY_DOUBLE.reverse() }
            launch { ToFrom.DROPDOWN_ACTION.reverse() }
            launch { BOTTOM_NAVIGATION.reverse() }
            launch { DROPDOWN_SWAPPER.reverse() }
        }
    }

    fun animateCurrencySwapping(scope: LifecycleCoroutineScope) {
        scope.launchWhenStarted {
            launch { CURRENCY_SWAPPING.begin() }
            launch { From.CURRENCY_DOUBLE.begin() }
            launch { To.CURRENCY_DOUBLE.begin() }
        }
    }

    fun animateCurrencyReverseSwapping(scope: LifecycleCoroutineScope) {
        scope.launchWhenStarted {
            launch { CURRENCY_SWAPPING.begin() }
            launch { From.CURRENCY_DOUBLE.reverse() }
            launch { To.CURRENCY_DOUBLE.reverse() }
        }
    }

    fun startObjectAnimators(dropdown: Dropdown, scope: LifecycleCoroutineScope) {
        if (dropdown == FROM) startFromObjectAnimators(scope)
        else startToObjectAnimators(scope)
    }

    fun reverseObjectAnimators(dropdown: Dropdown, scope: LifecycleCoroutineScope) {
        if (dropdown == FROM) reverseFromObjectAnimators(scope)
        else reverseToObjectAnimators(scope)
    }

    fun onFavoritesClicked(favorites: Boolean) {
        favoritesAnimator.onFavoritesClicked(favorites)
    }
}
