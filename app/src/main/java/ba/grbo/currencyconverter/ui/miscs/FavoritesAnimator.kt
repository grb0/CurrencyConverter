package ba.grbo.currencyconverter.ui.miscs

import android.animation.ObjectAnimator
import android.widget.ImageButton
import ba.grbo.currencyconverter.R
import ba.grbo.currencyconverter.util.Constants
import ba.grbo.currencyconverter.util.getScaleDownFadeOutAnimatorProducer
import ba.grbo.currencyconverter.util.getScaleUpFadeInAnimatorProducer
import kotlin.math.roundToLong

@Suppress("PrivatePropertyName", "PropertyName")
class FavoritesAnimator(
    button: ImageButton,
    private val onAnimationEnd: (Boolean) -> Unit,
) {
    private var animating = false

    private val scaleUpFadeInAnimatorProducer = button.getScaleUpFadeInAnimatorProducer()
    private val scaleDownFadeOutAnimatorProducer = button.getScaleDownFadeOutAnimatorProducer()

    private val Empty: Base = object : Base {
        override var SCALE_UP_FADE_IN = getFavoriteEmptyScaleUpFadeInDefaultAnimator()
        override var SCALE_DOWN_FADE_OUT = getFavoriteEmptyScaleDownFadeOutDefaultAnimator()
    }

    private val Filled: Base = object : Base {
        override var SCALE_UP_FADE_IN = getFavoriteFilledScaleUpFadeInDefaultAnimator()
        override var SCALE_DOWN_FADE_OUT = getFavoriteFilledScaleDownFadeOutDefaultAnimator()
    }

    private interface Base {
        var SCALE_UP_FADE_IN: ObjectAnimator
        var SCALE_DOWN_FADE_OUT: ObjectAnimator
    }

    private fun getFavoriteEmptyScaleUpFadeInDefaultAnimator() = scaleUpFadeInAnimatorProducer {
        animating = false
        onAnimationEnd(false)
    }

    private fun getFavoriteEmptyScaleDownFadeOutDefaultAnimator() =
        scaleDownFadeOutAnimatorProducer {
            Filled.SCALE_UP_FADE_IN.start()
            R.drawable.ic_favorite_filled
        }

    private fun getFavoriteFilledScaleUpFadeInDefaultAnimator() = scaleUpFadeInAnimatorProducer {
        animating = false
        onAnimationEnd(true)
    }

    private fun getFavoriteFilledScaleDownFadeOutDefaultAnimator() =
        scaleDownFadeOutAnimatorProducer {
            Empty.SCALE_UP_FADE_IN.start()
            R.drawable.ic_favorite_empty
        }

    private fun ObjectAnimator.removeListenersAndCancel() {
        removeAllListeners()
        cancel()
    }

    private fun ObjectAnimator.setupScaleDownFadeOutAnimator(
        time: Long
    ): ObjectAnimator = this.apply {
        currentPlayTime = duration - (time / Constants.ANIM_TIME_DIFFERENTIATOR).roundToLong()
        start()
    }

    private fun ObjectAnimator.setupScaleUpFadeInAnimator(time: Long): ObjectAnimator = this.apply {
        currentPlayTime = duration - (time * Constants.ANIM_TIME_DIFFERENTIATOR).roundToLong()
        start()
    }

    private fun reverseFavoritesEmptyScaleUpFadeIn() {
        val time = Empty.SCALE_UP_FADE_IN.currentPlayTime
        Empty.SCALE_UP_FADE_IN.removeListenersAndCancel()
        Empty.SCALE_UP_FADE_IN = getFavoriteEmptyScaleUpFadeInDefaultAnimator()
        Empty.SCALE_DOWN_FADE_OUT =
            getFavoriteEmptyScaleDownFadeOutDefaultAnimator().setupScaleDownFadeOutAnimator(time)
    }

    private fun reverseFavoritesEmptyScaleDownFadeOut() {
        val time = Empty.SCALE_DOWN_FADE_OUT.currentPlayTime
        Empty.SCALE_DOWN_FADE_OUT.removeListenersAndCancel()
        Empty.SCALE_DOWN_FADE_OUT = getFavoriteEmptyScaleDownFadeOutDefaultAnimator()
        Empty.SCALE_UP_FADE_IN =
            getFavoriteEmptyScaleUpFadeInDefaultAnimator().setupScaleUpFadeInAnimator(time)
    }

    private fun reverseFavoritesFilledScaleUpFadeIn() {
        val time = Filled.SCALE_UP_FADE_IN.currentPlayTime
        Filled.SCALE_UP_FADE_IN.removeListenersAndCancel()
        Filled.SCALE_UP_FADE_IN = getFavoriteFilledScaleUpFadeInDefaultAnimator()
        Filled.SCALE_DOWN_FADE_OUT =
            getFavoriteFilledScaleDownFadeOutDefaultAnimator().setupScaleDownFadeOutAnimator(time)
    }

    private fun reverseFavoritesFilledScaleDownFadeOut() {
        val time = Filled.SCALE_DOWN_FADE_OUT.currentPlayTime
        Filled.SCALE_DOWN_FADE_OUT.removeListenersAndCancel()
        Filled.SCALE_DOWN_FADE_OUT = getFavoriteFilledScaleDownFadeOutDefaultAnimator()
        Filled.SCALE_UP_FADE_IN =
            getFavoriteFilledScaleUpFadeInDefaultAnimator().setupScaleUpFadeInAnimator(time)
    }

    fun onFavoritesClicked(favorites: Boolean) {
        if (animating) {
            when {
                Empty.SCALE_UP_FADE_IN.isRunning -> {
                    reverseFavoritesEmptyScaleUpFadeIn()
                }
                Empty.SCALE_DOWN_FADE_OUT.isRunning -> {
                    reverseFavoritesEmptyScaleDownFadeOut()
                }
                Filled.SCALE_UP_FADE_IN.isRunning -> {
                    reverseFavoritesFilledScaleUpFadeIn()
                }
                Filled.SCALE_DOWN_FADE_OUT.isRunning -> {
                    reverseFavoritesFilledScaleDownFadeOut()
                }
            }
        } else when (favorites) {
            true -> {
                Empty.SCALE_DOWN_FADE_OUT.start()
                animating = true
            }
            else -> {
                Filled.SCALE_DOWN_FADE_OUT.start()
                animating = true
            }
        }
    }
}