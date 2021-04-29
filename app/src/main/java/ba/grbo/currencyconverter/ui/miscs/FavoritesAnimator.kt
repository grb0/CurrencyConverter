package ba.grbo.currencyconverter.ui.miscs

import android.animation.ObjectAnimator
import ba.grbo.currencyconverter.R
import ba.grbo.currencyconverter.util.Constants
import ba.grbo.currencyconverter.util.IncomingAnimatorProducer
import ba.grbo.currencyconverter.util.OutgoingAnimatorProducer
import kotlin.math.roundToLong

@Suppress("PrivatePropertyName", "PropertyName")
class FavoritesAnimator(
    animatorProducersPair: Pair<OutgoingAnimatorProducer, IncomingAnimatorProducer>,
    private val onAnimationEnd: (Boolean) -> Unit,
) {
    private var animating = false

    private val outgoingAnimatorProducer = animatorProducersPair.first
    private val incomingAnimatorProducer = animatorProducersPair.second

    private val Empty: Base = object : Base {
        override var OUTGOING = getEmptyOutgoingDefaultAnimator()
        override var INCOMING = getEmptyIncomingDefaultAnimator()
    }

    private val Filled: Base = object : Base {
        override var OUTGOING = getFilledOutgoingDefaultAnimator()
        override var INCOMING = getFilledIncomingDefaultAnimator()
    }

    private interface Base {
        var OUTGOING: ObjectAnimator
        var INCOMING: ObjectAnimator
    }

    private fun getEmptyOutgoingDefaultAnimator() = outgoingAnimatorProducer {
        Filled.INCOMING.start()
        R.drawable.ic_favorite_filled
    }

    private fun getEmptyIncomingDefaultAnimator() = incomingAnimatorProducer {
        animating = false
        onAnimationEnd(false)
    }

    private fun getFilledOutgoingDefaultAnimator() = outgoingAnimatorProducer {
        Empty.INCOMING.start()
        R.drawable.ic_favorite_empty
    }

    private fun getFilledIncomingDefaultAnimator() = incomingAnimatorProducer {
        animating = false
        onAnimationEnd(true)
    }

    private fun ObjectAnimator.removeListenersAndCancel() {
        removeAllListeners()
        cancel()
    }

    private fun ObjectAnimator.setupOutgoingAnimator(
        time: Long
    ): ObjectAnimator = this.apply {
        currentPlayTime = duration - (time / Constants.ANIM_TIME_DIFFERENTIATOR).roundToLong()
        start()
    }

    private fun ObjectAnimator.setupIncomingAnimator(time: Long): ObjectAnimator = this.apply {
        currentPlayTime = duration - (time * Constants.ANIM_TIME_DIFFERENTIATOR).roundToLong()
        start()
    }

    private fun reverseEmptyOutgoingAnimator() {
        val time = Empty.OUTGOING.currentPlayTime
        Empty.OUTGOING.removeListenersAndCancel()
        Empty.OUTGOING = getEmptyOutgoingDefaultAnimator()
        Empty.INCOMING = getEmptyIncomingDefaultAnimator().setupIncomingAnimator(time)
    }

    private fun reverseEmptyIncomingAnimator() {
        val time = Empty.INCOMING.currentPlayTime
        Empty.INCOMING.removeListenersAndCancel()
        Empty.INCOMING = getEmptyIncomingDefaultAnimator()
        Empty.OUTGOING = getEmptyOutgoingDefaultAnimator().setupOutgoingAnimator(time)
    }

    private fun reverseFilledOutgoingAnimator() {
        val time = Filled.OUTGOING.currentPlayTime
        Filled.OUTGOING.removeListenersAndCancel()
        Filled.OUTGOING = getFilledOutgoingDefaultAnimator()
        Filled.INCOMING =
            getFilledIncomingDefaultAnimator().setupIncomingAnimator(time)
    }

    private fun reverseFilledIncomingAnimator() {
        val time = Filled.INCOMING.currentPlayTime
        Filled.INCOMING.removeListenersAndCancel()
        Filled.INCOMING = getFilledIncomingDefaultAnimator()
        Filled.OUTGOING = getFilledOutgoingDefaultAnimator().setupOutgoingAnimator(time)
    }

    fun onFavoritesClicked(favorites: Boolean) {
        if (animating) {
            when {
                Empty.INCOMING.isRunning -> {
                    reverseEmptyIncomingAnimator()
                }
                Empty.OUTGOING.isRunning -> {
                    reverseEmptyOutgoingAnimator()
                }
                Filled.INCOMING.isRunning -> {
                    reverseFilledIncomingAnimator()
                }
                Filled.OUTGOING.isRunning -> {
                    reverseFilledOutgoingAnimator()
                }
            }
        } else when (favorites) {
            true -> {
                Empty.OUTGOING.start()
                animating = true
            }
            else -> {
                Filled.OUTGOING.start()
                animating = true
            }
        }
    }
}