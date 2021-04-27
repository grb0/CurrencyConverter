package ba.grbo.currencyconverter.ui.miscs

import android.content.res.Resources
import android.view.animation.AlphaAnimation
import ba.grbo.currencyconverter.util.setUp

@Suppress("PrivatePropertyName", "PropertyName")
class Animations(resources: Resources) {
    val FADE_IN = AlphaAnimation(0f, 1f).setUp(resources)
    val FADE_OUT = AlphaAnimation(1f, 0f).setUp(resources)
}