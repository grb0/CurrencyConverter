package ba.grbo.currencyconverter.util

import android.animation.Animator
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.content.ContextWrapper
import android.content.res.ColorStateList
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
import ba.grbo.currencyconverter.ui.fragments.ConverterFragment
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.Dropdown
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.SearcherState
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.SearcherState.Focusing
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.SearcherState.Unfocusing
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.transition.MaterialFadeThrough
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
    distinctUntilChanged: Boolean = true
) {
    collect(flow, action, Lifecycle.State.STARTED, distinctUntilChanged)
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

fun TextView.getBackgroundAnimator() = ObjectAnimator.ofArgb(
    this,
    "backgroundColor",
    ConverterFragment.Colors.WHITE,
    ConverterFragment.Colors.LIGHT_GRAY
).setUp(resources)

fun TextView.getAnimator(): ObjectAnimator {
    val backgroundColorProperty = PropertyValuesHolder.ofObject(
        "backgroundColor",
        { fraction, startValue, endValue ->
            ArgbEvaluator().evaluate(fraction, startValue, endValue) as Int
        },
        ConverterFragment.Colors.WHITE,
        ConverterFragment.Colors.LIGHT_GRAY
    )

    val textColorProperty = PropertyValuesHolder.ofObject(
        "textColor",
        { fraction, startValue, endValue ->
            ArgbEvaluator().evaluate(fraction, startValue, endValue) as Int
        },
        ConverterFragment.Colors.BORDER,
        ConverterFragment.Colors.PRIMARY_VARIANT
    )

    val typeFaceProperty = PropertyValuesHolder.ofObject(
        Property.of(TextView::class.java, Typeface::class.java, "typeface"),
        { fraction, startValue, endValue ->
            if (fraction < 0.33) startValue
            else if (fraction > 0.33 && fraction < 0.66) ResourcesCompat.getFont(
                context, R.font.roboto_medium
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

fun LinearLayout.getBackgroundAnimator() = ObjectAnimator.ofObject(
    this,
    Property.of(LinearLayout::class.java, Drawable::class.java, "background"),
    { fraction, _, _ ->
        val colorEvaluator = ArgbEvaluator()
        val backgroundColor = colorEvaluator.evaluate(
            fraction,
            ConverterFragment.Colors.WHITE,
            ConverterFragment.Colors.LIGHT_GRAY
        ) as Int

        val drawable = getGradientDrawable(backgroundColor = backgroundColor)
        drawable
    },
    getGradientDrawable(),
    getGradientDrawable(backgroundColor = ConverterFragment.Colors.LIGHT_GRAY)
).setUp(resources)

fun LinearLayout.getAnimator() = ObjectAnimator.ofObject(
    this,
    Property.of(LinearLayout::class.java, Drawable::class.java, "background"),
    { fraction, _, _ ->
        val colorEvaluator = ArgbEvaluator()
        val strokeColor = colorEvaluator.evaluate(
            fraction,
            ConverterFragment.Colors.BORDER,
            ConverterFragment.Colors.PRIMARY_VARIANT
        ) as Int
        val backgroundColor = colorEvaluator.evaluate(
            fraction,
            ConverterFragment.Colors.WHITE,
            ConverterFragment.Colors.LIGHT_GRAY
        ) as Int
        val strokeWidth = 1f + (fraction * (2f - 1f))
        val drawable = getGradientDrawable(
            strokeWidth,
            strokeColor,
            backgroundColor
        )
        drawable
    },
    getGradientDrawable(),
    getGradientDrawable(
        2f,
        ConverterFragment.Colors.PRIMARY_VARIANT,
        ConverterFragment.Colors.LIGHT_GRAY
    )
).setUp(resources)

fun ConstraintLayout.getAnimator() = ObjectAnimator.ofArgb(
    this,
    "backgroundColor",
    ConverterFragment.Colors.WHITE,
    ConverterFragment.Colors.LIGHT_GRAY
).setUp(resources)

fun TextView.getCurrencyAnimator(): ObjectAnimator {
    val alphaType = PropertyValuesHolder.ofObject(
        "placeholder",
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

    val colorType = PropertyValuesHolder.ofObject(
        "textColor",
        { fraction, startValue, endValue ->
            ArgbEvaluator().evaluate(fraction, startValue, endValue) as Int
        },
        ConverterFragment.Colors.BLACK,
        ConverterFragment.Colors.BORDER
    )

    return ObjectAnimator.ofPropertyValuesHolder(
        this,
        alphaType,
        colorType
    ).setUp(resources)
}

fun ImageButton.getBackgroundColorAnimator(): ObjectAnimator {
    val imageTintListProperty = PropertyValuesHolder.ofObject(
        "imageTintList",
        { fraction, _, _ ->
            val interpolatedColor = ArgbEvaluator().evaluate(
                fraction,
                ConverterFragment.Colors.CONTROL_NORMAL,
                ConverterFragment.Colors.DIVIDER
            ) as Int
            ColorStateList.valueOf(interpolatedColor)
        },
        ColorStateList.valueOf(ConverterFragment.Colors.CONTROL_NORMAL),
        ColorStateList.valueOf(ConverterFragment.Colors.DIVIDER)
    )

    return ObjectAnimator.ofPropertyValuesHolder(
        this,
        imageTintListProperty,
    ).setUp(resources)
}

fun ImageButton.getAnimator(): ObjectAnimator {
    val imageTintListProperty = PropertyValuesHolder.ofObject(
        "imageTintList",
        { fraction, _, _ ->
            val interpolatedColor = ArgbEvaluator().evaluate(
                fraction,
                ConverterFragment.Colors.CONTROL_NORMAL,
                ConverterFragment.Colors.PRIMARY_VARIANT
            ) as Int
            ColorStateList.valueOf(interpolatedColor)
        },
        ColorStateList.valueOf(ConverterFragment.Colors.CONTROL_NORMAL),
        ColorStateList.valueOf(ConverterFragment.Colors.PRIMARY_VARIANT)
    )

    val rotation = PropertyValuesHolder.ofFloat(View.ROTATION, 0f, 180f)

    return ObjectAnimator.ofPropertyValuesHolder(
        this,
        imageTintListProperty,
        rotation
    ).setUp(resources)
}

fun LinearLayout.getGradientDrawable(
    strokeWidth: Float = 1f,
    strokeColor: Int = ConverterFragment.Colors.BORDER,
    backgroundColor: Int = ConverterFragment.Colors.WHITE
) = GradientDrawable().apply {
    shape = GradientDrawable.RECTANGLE
    setStroke(strokeWidth.toPixels(resources).roundToInt(), strokeColor)
    setColor(backgroundColor)
    cornerRadius = 5f.toPixels(resources)
}

fun BottomNavigationView.getAnimator(
    startStateIcon: ColorStateList,
    startStateText: ColorStateList
): ObjectAnimator {
    val cSL = ColorStateList(
        arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf(-android.R.attr.checked)),
        intArrayOf(ConverterFragment.Colors.BORDER, ConverterFragment.Colors.DIVIDER)
    )

    val backgroundColorProperty = PropertyValuesHolder.ofObject(
        "backgroundColor",
        { fraction, startValue, endValue ->
            ArgbEvaluator().evaluate(fraction, startValue, endValue) as Int
        },
        ConverterFragment.Colors.PRIMARY,
        ConverterFragment.Colors.DIVIDER
    )

    val itemIconTintListProperty = PropertyValuesHolder.ofObject(
        "itemIconTintList",
        { fraction, _, _ ->
            val evaluator = ArgbEvaluator()
            val checkedColor = evaluator.evaluate(
                fraction,
                startStateIcon.getColorForState(
                    intArrayOf(android.R.attr.state_checked),
                    ConverterFragment.Colors.PRIMARY_VARIANT
                ),
                ConverterFragment.Colors.BORDER,
            ) as Int

            val uncheckedColor = evaluator.evaluate(
                fraction,
                startStateIcon.getColorForState(
                    intArrayOf(-android.R.attr.state_checked),
                    ConverterFragment.Colors.PRIMARY_VARIANT
                ),
                ConverterFragment.Colors.DIVIDER
            ) as Int

            ColorStateList(
                arrayOf(
                    intArrayOf(android.R.attr.state_checked),
                    intArrayOf(-android.R.attr.state_checked)
                ),
                intArrayOf(checkedColor, uncheckedColor)
            )
        },
        startStateIcon,
        cSL
    )

    val itemTextColorProperty = PropertyValuesHolder.ofObject(
        "itemTextColor",
        { fraction, _, _ ->
            val evaluator = ArgbEvaluator()
            val checkedColor = evaluator.evaluate(
                fraction,
                startStateText.getColorForState(
                    intArrayOf(android.R.attr.state_checked),
                    ConverterFragment.Colors.PRIMARY_VARIANT
                ),
                ConverterFragment.Colors.BORDER
            ) as Int

            val uncheckedColor = evaluator.evaluate(
                fraction,
                startStateText.getColorForState(
                    intArrayOf(-android.R.attr.state_checked),
                    ConverterFragment.Colors.PRIMARY_VARIANT
                ),
                ConverterFragment.Colors.DIVIDER,
            ) as Int

            ColorStateList(
                arrayOf(
                    intArrayOf(android.R.attr.state_checked),
                    intArrayOf(-android.R.attr.state_checked)
                ),
                intArrayOf(checkedColor, uncheckedColor)
            )
        },
        startStateText,
        cSL
    )

    return ObjectAnimator.ofPropertyValuesHolder(
        this,
        backgroundColorProperty,
        itemIconTintListProperty,
        itemTextColorProperty
    ).setUp(resources)
}

fun getMaterialFadeThroughAnimator(viewGroup: ViewGroup, enter: Boolean): Animator {
    return if (enter) MaterialFadeThrough().onAppear(
        viewGroup,
        viewGroup,
        TransitionValues(viewGroup),
        TransitionValues(viewGroup)
    ) else MaterialFadeThrough().onDisappear(
        viewGroup,
        viewGroup,
        TransitionValues(viewGroup),
        TransitionValues(viewGroup)
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

fun getPropertyValueHolderForBackgroundColor(
    startColor: Int,
    endColor: Int
): PropertyValuesHolder = PropertyValuesHolder.ofObject(
    "backgroundColor",
    { fraction, startValue, endValue ->
        ArgbEvaluator().evaluate(fraction, startValue, endValue) as Int
    },
    startColor,
    endColor
)