package ba.grbo.currencyconverter.ui.miscs

import android.content.res.Resources
import android.view.animation.AlphaAnimation
import ba.grbo.currencyconverter.util.setUp

@Suppress("PrivatePropertyName", "PropertyName")
class Animations(resources: Resources) {
    val From = object : Base {
        override val FADE_IN = AlphaAnimation(0f, 1f).setUp(resources)
        override val FADE_OUT = AlphaAnimation(1f, 0f).setUp(resources)
    }

    val To = object : Base {
        override val FADE_IN = AlphaAnimation(0f, 1f).setUp(resources)
        override val FADE_OUT = AlphaAnimation(1f, 0f).setUp(resources)
    }

    interface Base {
        val FADE_IN: AlphaAnimation
        val FADE_OUT: AlphaAnimation
    }
}