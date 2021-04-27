package ba.grbo.currencyconverter.util

import android.animation.Animator
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.content.ContextWrapper
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.Property
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.LinearInterpolator
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionValues
import ba.grbo.currencyconverter.R
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.Dropdown
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.SearcherState
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.SearcherState.Focusing
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.SearcherState.Unfocusing
import ba.grbo.currencyconverter.util.Constants.ALPHA
import ba.grbo.currencyconverter.util.Constants.BACKGROUND
import ba.grbo.currencyconverter.util.Constants.BACKGROUND_COLOR
import ba.grbo.currencyconverter.util.Constants.IMAGE_TINT_LIST
import ba.grbo.currencyconverter.util.Constants.PLACEHOLDER
import ba.grbo.currencyconverter.util.Constants.TEXT_COLOR
import ba.grbo.currencyconverter.util.Constants.TYPEFACE
import com.google.android.material.card.MaterialCardView
import com.google.android.material.transition.MaterialFadeThrough
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import java.util.*
import kotlin.math.roundToInt

fun Context.getColorFromAttribute(@AttrRes id: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(id, typedValue, true)
    return ContextCompat.getColor(this, typedValue.resourceId)
}

fun Float.toPixels(resources: Resources) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    this,
    resources.displayMetrics
)

val Int.isLandscape
    get() = this == Configuration.ORIENTATION_LANDSCAPE

fun Boolean.toSearcherState(dropdown: Dropdown): SearcherState {
    return if (this) Focusing(dropdown) else Unfocusing(dropdown)
}

private fun <T> Flow<T>.collect(
    lifecycle: Lifecycle,
    lifecycleState: Lifecycle.State,
    scope: LifecycleCoroutineScope,
    action: suspend (T) -> Unit,
    distinctUntilChanged: Boolean = true
) {
    if (distinctUntilChanged) distinctUntilChanged()
        .flowWithLifecycle(lifecycle, lifecycleState)
        .onEach { action(it) }
        .launchIn(scope)
    else flowWithLifecycle(lifecycle, lifecycleState)
        .onEach { action(it) }
        .launchIn(scope)
}

private fun <T> Fragment.collect(
    flow: Flow<T>,
    action: suspend (T) -> Unit,
    lifecycleState: Lifecycle.State,
    distinctUntilChanged: Boolean = true,
) {
    flow.collect(
        viewLifecycleOwner.lifecycle,
        lifecycleState,
        viewLifecycleOwner.lifecycleScope,
        action,
        distinctUntilChanged,
    )
}

fun <T> Fragment.collectWhenStarted(
    flow: Flow<T>,
    action: suspend (T) -> Unit,
    distinctUntilChanged: Boolean
) {
    collect(flow, action, Lifecycle.State.STARTED, distinctUntilChanged)
}

fun <T> Fragment.collectWhenStarted(
    flow: Flow<T>,
    action: suspend (T) -> Unit
): Job {
    return flow.flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
        .onEach { action(it) }
        .launchIn(viewLifecycleOwner.lifecycleScope)
}

fun ObjectAnimator.setUp(resources: Resources): ObjectAnimator {
    interpolator = LinearInterpolator()
    duration = resources.getInteger(R.integer.anim_time).toLong()
    return this
}

fun AlphaAnimation.setUp(resources: Resources): AlphaAnimation {
    interpolator = LinearInterpolator()
    duration = resources.getInteger(R.integer.anim_time).toLong()
    return this
}

fun Context.updateLocale(language: Locale): ContextWrapper {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        resources.configuration.setLocale(language)
    } else resources.configuration.locale = language

    var context: Context = this
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
        context = context.createConfigurationContext(resources.configuration)
    } else resources.updateConfiguration(context.resources.configuration, resources.displayMetrics)

    return ContextWrapper(context)
}

@Suppress("FunctionName", "UNCHECKED_CAST")
fun <T> SingleSharedFlow() = MutableSharedFlow<T>(
    onBufferOverflow = BufferOverflow.DROP_OLDEST,
    extraBufferCapacity = 1
)

val <T> SharedFlow<T>.value
    get() = if (replayCache.isNotEmpty()) replayCache[0] else null

// StateFlow imitation without an initial value, so we avoid null as an initial value
@Suppress("FunctionName", "UNCHECKED_CAST")
fun <T> SharedStateLikeFlow() = MutableSharedFlow<T>(
    replay = 1,
    onBufferOverflow = BufferOverflow.DROP_OLDEST
)

fun TextView.getBackgroundAnimator(startColor: Int, endColor: Int) = ObjectAnimator.ofArgb(
    this,
    BACKGROUND_COLOR,
    startColor,
    endColor
).setUp(resources)

fun TextView.getAnimator(
    backgroundStartColor: Int,
    backgroundEndColor: Int,
    textStartColor: Int,
    textEndColor: Int
): ObjectAnimator {
    val backgroundColorProperty = getArgbPropertyValueHolderForProperty(
        BACKGROUND_COLOR,
        backgroundStartColor,
        backgroundEndColor
    )

    val textColorProperty = getArgbPropertyValueHolderForProperty(
        TEXT_COLOR,
        textStartColor,
        textEndColor
    )

    val typeFaceProperty = PropertyValuesHolder.ofObject(
        Property.of(TextView::class.java, Typeface::class.java, TYPEFACE),
        { fraction, startValue, endValue ->
            if (fraction < 0.33) startValue
            else if (fraction > 0.33 && fraction < 0.66) ResourcesCompat.getFont(
                context,
                R.font.roboto_medium
            ) else endValue
        },
        ResourcesCompat.getFont(context, R.font.roboto),
        ResourcesCompat.getFont(context, R.font.roboto_bold)
    )

    return ObjectAnimator.ofPropertyValuesHolder(
        this,
        backgroundColorProperty,
        textColorProperty,
        typeFaceProperty
    ).setUp(resources)
}

fun LinearLayout.getBackgroundAnimator(
    strokeColor: Int,
    backgroundStartColor: Int,
    backgroundEndColor: Int,
) = ObjectAnimator.ofObject(
    this,
    Property.of(LinearLayout::class.java, Drawable::class.java, BACKGROUND),
    { fraction, _, _ ->
        val colorEvaluator = ArgbEvaluator()
        val backgroundColor = colorEvaluator.evaluate(
            fraction,
            backgroundStartColor,
            backgroundEndColor
        ) as Int

        val drawable = getGradientDrawable(
            backgroundColor,
            strokeColor
        )
        drawable
    },
    getGradientDrawable(backgroundStartColor, strokeColor),
    getGradientDrawable(backgroundEndColor, strokeColor)
).setUp(resources)

fun LinearLayout.getAnimator(
    strokeStartColor: Int,
    strokeEndColor: Int,
    backgroundStartColor: Int,
    backgroundEndColor: Int
) = ObjectAnimator.ofObject(
    this,
    Property.of(LinearLayout::class.java, Drawable::class.java, BACKGROUND),
    { fraction, _, _ ->
        val colorEvaluator = ArgbEvaluator()

        val strokeColor = colorEvaluator.evaluate(
            fraction,
            strokeStartColor,
            strokeEndColor
        ) as Int

        val backgroundColor = colorEvaluator.evaluate(
            fraction,
            backgroundStartColor,
            backgroundEndColor
        ) as Int

        val strokeWidth = 1f + (fraction * (2f - 1f))

        val drawable = getGradientDrawable(
            backgroundColor,
            strokeColor,
            strokeWidth
        )

        drawable
    },
    getGradientDrawable(backgroundStartColor, strokeStartColor),
    getGradientDrawable(backgroundEndColor, strokeEndColor, 2f)
).setUp(resources)

fun ConstraintLayout.getAnimator(startColor: Int, endColor: Int) = ObjectAnimator.ofArgb(
    this,
    BACKGROUND_COLOR,
    startColor,
    endColor
).setUp(resources)

fun TextView.getCurrencyAnimator(startColor: Int, endColor: Int): ObjectAnimator {
    val alphaProperty = PropertyValuesHolder.ofObject(
        PLACEHOLDER,
        { fraction, _, _ ->
            val alpha = (255 + (fraction * (128 - 255))).roundToInt()
            val drawable = compoundDrawables[0]?.mutate()?.constantState?.newDrawable()?.apply {
                this.alpha = alpha
            }
            setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
            alpha
        },
        255,
        128
    )

    val textColorProperty = getArgbPropertyValueHolderForProperty(
        TEXT_COLOR,
        startColor,
        endColor
    )

    return ObjectAnimator.ofPropertyValuesHolder(
        this,
        alphaProperty,
        textColorProperty
    ).setUp(resources)
}

fun ImageButton.getBackgroundColorAnimator(
    startColor: Int,
    endColor: Int
): ObjectAnimator {
    val imageTintListProperty = getPropertyValueHolderForPropertyForImageTintList(
        startColor,
        endColor
    )

    return ObjectAnimator.ofPropertyValuesHolder(
        this,
        imageTintListProperty,
    ).setUp(resources)
}

fun ImageButton.getAnimator(
    startColor: Int,
    endColor: Int
): ObjectAnimator {
    val imageTintListProperty = getPropertyValueHolderForPropertyForImageTintList(
        startColor,
        endColor
    )

    val rotation = PropertyValuesHolder.ofFloat(View.ROTATION, 0f, 180f)

    return ObjectAnimator.ofPropertyValuesHolder(
        this,
        imageTintListProperty,
        rotation
    ).setUp(resources)
}

fun LinearLayout.getGradientDrawable(
    backgroundColor: Int,
    strokeColor: Int,
    strokeWidth: Float = 1f
) = GradientDrawable().apply {
    shape = GradientDrawable.RECTANGLE
    setStroke(strokeWidth.toPixels(resources).roundToInt(), strokeColor)
    setColor(backgroundColor)
    cornerRadius = 5f.toPixels(resources)
}

fun getMaterialFadeThroughAnimator(viewGroup: ViewGroup, enter: Boolean): Animator {
    fun getMaterialFadeThroughAnimator(
        function: (ViewGroup, View, TransitionValues, TransitionValues) -> Animator
    ): Animator = function(
        viewGroup,
        viewGroup,
        TransitionValues(viewGroup),
        TransitionValues(viewGroup)
    )

    return getMaterialFadeThroughAnimator(
        if (enter) MaterialFadeThrough()::onAppear else MaterialFadeThrough()::onDisappear
    )
}

fun View.setMargins(size: Float) {
    if (layoutParams is ViewGroup.MarginLayoutParams) {
        val params = layoutParams as ViewGroup.MarginLayoutParams
        val margin = size.toPixels(resources).roundToInt()
        params.leftMargin = margin
        params.topMargin = margin
        params.rightMargin = margin
        params.bottomMargin = margin
        requestLayout()
    }
}

private fun TextView.getVerticalTranslationAnimator(
    from: Boolean
): ObjectAnimator = ObjectAnimator.ofFloat(
    this,
    View.TRANSLATION_Y,
    translationY,
    if (from) 112f.toPixels(resources) else -(112f.toPixels(resources))
).setUp(resources)

private fun TextView.getHorizontalTranslationAnimator(
    from: Boolean,
    width: Float
): ObjectAnimator = ObjectAnimator.ofFloat(
    this,
    View.TRANSLATION_X,
    translationX,
    if (from) (width / 2) + 12f.toPixels(resources) else -((width / 2) + 12f.toPixels(resources))
).setUp(resources)

fun TextView.getTranslationAnimator(
    from: Boolean,
    landscape: Boolean,
    width: Float
): ObjectAnimator {
    return if (!landscape) getVerticalTranslationAnimator(from)
    else getHorizontalTranslationAnimator(from, width)
}

fun ImageButton.getRotationAnimatior(): ObjectAnimator {
    return ObjectAnimator.ofFloat(
        this,
        View.ROTATION,
        rotation - 180f,
        rotation
    ).setUp(resources)
}

private fun getFadeAnimator(
    view: View,
    startAlpha: Float,
    endAlpha: Float,
    onAnimationStart: () -> Unit,
    onAnimationEnd: () -> Unit
): ObjectAnimator = ObjectAnimator.ofFloat(
    view,
    ALPHA,
    startAlpha,
    endAlpha
).setUp(view.resources).apply {
    addListener(getAnimatorListener(onAnimationStart, onAnimationEnd))
}

private fun getAnimatorListener(
    onAnimationStart: () -> Unit,
    onAnimationEnd: () -> Unit
) = object : Animator.AnimatorListener {
    override fun onAnimationStart(animation: Animator?) {
        onAnimationStart()
    }

    override fun onAnimationEnd(animation: Animator?) {
        onAnimationEnd()
    }

    override fun onAnimationCancel(animation: Animator?) {
    }

    override fun onAnimationRepeat(animation: Animator?) {
    }
}

fun MaterialCardView.getFadeInAnimatorProducer(): (Float) -> ObjectAnimator = { startAlpha ->
    getFadeAnimator(
        this,
        startAlpha,
        1f,
        { visibility = View.VISIBLE },
        {}
    )
}

fun MaterialCardView.getFadeOutAnimatorProducer(): (Float) -> ObjectAnimator = { startAlpha ->
    getFadeAnimator(
        this,
        startAlpha,
        0f,
        {},
        { visibility = View.INVISIBLE }
    )
}

fun getArgbPropertyValueHolderForProperty(
    property: String,
    startColor: Int,
    endColor: Int
): PropertyValuesHolder = PropertyValuesHolder.ofObject(
    property,
    { fraction, startValue, endValue ->
        ArgbEvaluator().evaluate(fraction, startValue, endValue) as Int
    },
    startColor,
    endColor
)

private fun getPropertyValueHolderForPropertyForImageTintList(
    startColor: Int,
    endColor: Int
): PropertyValuesHolder = PropertyValuesHolder.ofObject(
    IMAGE_TINT_LIST,
    { fraction, _, _ ->
        val interpolatedColor = ArgbEvaluator().evaluate(
            fraction,
            startColor,
            endColor
        ) as Int
        ColorStateList.valueOf(interpolatedColor)
    },
    ColorStateList.valueOf(startColor),
    ColorStateList.valueOf(endColor)
)