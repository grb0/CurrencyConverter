package ba.grbo.currencyconverter.util

import android.animation.Animator
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
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
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionValues
import ba.grbo.currencyconverter.R
import ba.grbo.currencyconverter.data.models.database.ExchangeableCurrency
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.Dropdown
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.SearcherState
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.SearcherState.Focusing
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.SearcherState.Unfocusing
import ba.grbo.currencyconverter.util.Constants.ANIM_TIME
import ba.grbo.currencyconverter.util.Constants.ANIM_TIME_DIFFERENTIATOR
import ba.grbo.currencyconverter.util.Constants.BACKGROUND
import ba.grbo.currencyconverter.util.Constants.BACKGROUND_COLOR
import ba.grbo.currencyconverter.util.Constants.IMAGE_TINT_LIST
import ba.grbo.currencyconverter.util.Constants.PLACEHOLDER
import ba.grbo.currencyconverter.util.Constants.SCALE_DOWN_END
import ba.grbo.currencyconverter.util.Constants.SCALE_UP_START
import ba.grbo.currencyconverter.util.Constants.TEXT_COLOR
import ba.grbo.currencyconverter.util.Constants.TYPEFACE
import com.google.android.material.card.MaterialCardView
import com.google.android.material.transition.MaterialFadeThrough
import com.google.android.material.transition.MaterialSharedAxis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.roundToInt
import kotlin.math.roundToLong

fun SharedPreferences.putString(key: String, value: String) {
    with(edit()) {
        putString(key, value)
        apply()
    }
}

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
    distinctUntilChanged: Boolean,
    onEach: suspend (T) -> Unit
) {
    if (distinctUntilChanged) distinctUntilChanged()
        .flowWithLifecycle(lifecycle, lifecycleState)
        .onEach { onEach(it) }
        .launchIn(scope)
    else flowWithLifecycle(lifecycle, lifecycleState)
        .onEach { onEach(it) }
        .launchIn(scope)
}

fun <T> Fragment.collectWhenStarted(
    flow: Flow<T>,
    distinctUntilChanged: Boolean,
    onEach: suspend (T) -> Unit
) {
    flow.collect(
        viewLifecycleOwner.lifecycle,
        Lifecycle.State.STARTED,
        viewLifecycleOwner.lifecycleScope,
        distinctUntilChanged,
        onEach
    )
}

fun <T> Fragment.collectWhenStarted(
    flow: Flow<T>,
    distinctUntilChanged: Boolean,
    onEach: suspend () -> Unit
) {
    collectWhenStarted(flow, distinctUntilChanged) { _ -> onEach()}
}

fun <T> Fragment.collectWhenStarted(
    flow: Flow<T>,
    onEach: suspend (T) -> Unit
): Job {
    return flow
        .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
        .onEach { onEach(it) }
        .launchIn(viewLifecycleOwner.lifecycleScope)
}

fun <T> AppCompatActivity.collectWhenStarted(
    flow: Flow<T>,
    onEach: suspend (T) -> Unit
) {
    flow.collect(
        lifecycle,
        Lifecycle.State.STARTED,
        lifecycleScope,
        false,
        onEach
    )
}

fun <T> AppCompatActivity.collectWhenStarted(
    flow: Flow<T>,
    onEach: suspend () -> Unit
) {
    collectWhenStarted(flow) { _ -> onEach() }
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

fun Context.getLocale(): Locale {
    return getLanguage().toLocale()
}

fun Context.getLanguage(): Language {
    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
    val language = sharedPreferences.getString(
        this.getString(R.string.key_language),
        Language.ENGLISH.name
    ) ?: Language.ENGLISH.name

    return Language.valueOf(language)
}

fun Context.updateLocale(): Context {
    return updateLocale(getLocale())
}

@Suppress("DEPRECATION")
fun Context.updateLocale(locale: Locale): Context {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        resources.configuration.setLocale(locale)
    } else resources.configuration.locale = locale

    Locale.setDefault(locale)

    var context: Context = this
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
        context = createConfigurationContext(resources.configuration)
    } else context.resources.updateConfiguration(
        context.resources.configuration,
        context.resources.displayMetrics
    )

    return context
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

@SuppressLint("Recycle")
fun TextView.getBackgroundAnimator(startColor: Int, endColor: Int) = ObjectAnimator.ofArgb(
    this,
    BACKGROUND_COLOR,
    startColor,
    endColor
).setUp(resources)

@SuppressLint("Recycle")
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

@SuppressLint("Recycle")
fun ConstraintLayout.getBackgroundAnimator(
    strokeColor: Int,
    backgroundStartColor: Int,
    backgroundEndColor: Int,
) = ObjectAnimator.ofObject(
    this,
    Property.of(ConstraintLayout::class.java, Drawable::class.java, BACKGROUND),
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

@SuppressLint("Recycle")
fun ConstraintLayout.getAnimator(
    strokeStartColor: Int,
    strokeEndColor: Int,
    backgroundStartColor: Int,
    backgroundEndColor: Int
) = ObjectAnimator.ofObject(
    this,
    Property.of(ConstraintLayout::class.java, Drawable::class.java, BACKGROUND),
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

@SuppressLint("Recycle")
fun ConstraintLayout.getAnimator(startColor: Int, endColor: Int) = ObjectAnimator.ofArgb(
    this,
    BACKGROUND_COLOR,
    startColor,
    endColor
).setUp(resources)

@SuppressLint("Recycle")
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

@SuppressLint("Recycle")
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

@SuppressLint("Recycle")
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

fun ConstraintLayout.getGradientDrawable(
    backgroundColor: Int,
    strokeColor: Int,
    strokeWidth: Float = 1f
) = GradientDrawable().apply {
    shape = GradientDrawable.RECTANGLE
    setStroke(strokeWidth.toPixels(resources).roundToInt(), strokeColor)
    setColor(backgroundColor)
    cornerRadius = 7.5f.toPixels(resources)
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

fun Fragment.getExitAndPopEnterMaterialSharedXAnimators(
    nextAnim: Int,
    onElse: (Int) -> Animator
) = when (nextAnim) {
    android.R.anim.fade_out -> view.getMaterialSharedXAxisExitBackwardAnimator()
    android.R.anim.slide_in_left -> {
        view.getMaterialSharedXAxisEnterForwardAnimator(viewLifecycleOwner.lifecycleScope)
    }
    else -> onElse(nextAnim)
}

fun Fragment.getEnterAndPopExitMaterialSharedXAnimators(nextAnim: Int) = when (nextAnim) {
    android.R.anim.fade_in -> {
        view.getMaterialSharedXAxisEnterBackwardAnimator(viewLifecycleOwner.lifecycleScope)
    }
    android.R.anim.slide_out_right -> view.getMaterialSharedXAxisExitForwardAnimator()
    else -> throw IllegalArgumentException("Unknown nextAnim: $nextAnim")
}

fun View?.getMaterialSharedXAxisEnterForwardAnimator(scope: CoroutineScope): Animator {
    return getMaterialSharedXAxisEnterAnimator(this, true, scope)
}

fun View?.getMaterialSharedXAxisEnterBackwardAnimator(scope: CoroutineScope): Animator {
    return getMaterialSharedXAxisEnterAnimator(this, false, scope)
}

private fun getMaterialSharedXAxisEnterAnimator(
    view: View?,
    forward: Boolean,
    scope: CoroutineScope
): Animator {
    val recyclerView = view.getRecyclerView()
    return getMaterialSharedXAxisAnimator(
        view as ViewGroup,
        enter = true,
        forward = forward,
        onAnimationStart = { recyclerView.onCustomAnimationStart(scope) },
        onAnimationEnd = recyclerView::onCustomAnimationEnd
    )
}

fun View?.getMaterialSharedXAxisExitForwardAnimator(): Animator {
    return getMaterialSharedXAxisExitAnimator(this, true)
}

fun View?.getMaterialSharedXAxisExitBackwardAnimator(): Animator {
    return getMaterialSharedXAxisExitAnimator(this, false)
}

private fun getMaterialSharedXAxisExitAnimator(view: View?, forward: Boolean): Animator {
    return getMaterialSharedXAxisAnimator(
        view as ViewGroup,
        false,
        forward = forward,
        {},
        {}
    )
}

private fun getMaterialSharedXAxisAnimator(
    viewGroup: ViewGroup,
    enter: Boolean,
    forward: Boolean,
    onAnimationStart: () -> Unit,
    onAnimationEnd: () -> Unit
): Animator {
    fun getMaterialSharedXAxisAnimator(
        function: (ViewGroup, View, TransitionValues, TransitionValues) -> Animator
    ): Animator = function(
        viewGroup,
        viewGroup,
        TransitionValues(viewGroup),
        TransitionValues(viewGroup)
    ).apply {
        addListener(getAnimatorListener(onAnimationStart, onAnimationEnd))
    }

    return getMaterialSharedXAxisAnimator(
        if (enter) MaterialSharedAxis(MaterialSharedAxis.X, forward)::onAppear
        else MaterialSharedAxis(MaterialSharedAxis.X, forward)::onDisappear
    )
}

private fun View?.getRecyclerView(): RecyclerView {
    return ((this as LinearLayout).getChildAt(0) as FrameLayout).getChildAt(0) as RecyclerView
}

private fun RecyclerView.onCustomAnimationStart(scope: CoroutineScope) {
    scope.launch {
        while (childCount == 0) {
            delay(1)
        }
        setChildrenClickability(false)
    }
}

private fun RecyclerView.onCustomAnimationEnd() {
    setChildrenClickability(true)
}

private fun RecyclerView.setChildrenClickability(clickable: Boolean) {
    for (i in 0 until childCount) {
        getChildAt(i).isClickable = clickable
    }
}

@SuppressLint("Recycle")
private fun TextView.getVerticalTranslationAnimator(
    from: Boolean
): ObjectAnimator = ObjectAnimator.ofFloat(
    this,
    View.TRANSLATION_Y,
    translationY,
    if (from) 114f.toPixels(resources) else -(114f.toPixels(resources))
).setUp(resources)

@SuppressLint("Recycle")
private fun TextView.getHorizontalTranslationAnimator(
    from: Boolean,
    width: Float
): ObjectAnimator = ObjectAnimator.ofFloat(
    this,
    View.TRANSLATION_X,
    translationX,
    if (from) (width / 2) + 8f.toPixels(resources) else -((width / 2) + 8f.toPixels(resources))
).setUp(resources)

fun TextView.getTranslationAnimator(
    from: Boolean,
    landscape: Boolean,
    width: Float
): ObjectAnimator {
    return if (!landscape) getVerticalTranslationAnimator(from)
    else getHorizontalTranslationAnimator(from, width)
}

@SuppressLint("Recycle")
fun ImageButton.getRotationAnimatior(): ObjectAnimator {
    return ObjectAnimator.ofFloat(
        this,
        View.ROTATION,
        rotation - 180f,
        rotation
    ).setUp(resources)
}

@SuppressLint("Recycle")
private fun getFadeAnimator(
    view: View,
    startAlpha: Float,
    endAlpha: Float,
    onAnimationStart: () -> Unit,
    onAnimationEnd: () -> Unit
): ObjectAnimator = ObjectAnimator.ofFloat(
    view,
    View.ALPHA,
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

private fun ObjectAnimator.setupOutgoingAnimator(onAnimationEnd: () -> Unit): ObjectAnimator {
    duration = ANIM_TIME
    interpolator = AccelerateInterpolator()
    addListener(getAnimatorListener({}, onAnimationEnd))
    return this
}

private fun ObjectAnimator.setupIncomingAnimator(onAnimationEnd: () -> Unit): ObjectAnimator {
    duration = (ANIM_TIME * ANIM_TIME_DIFFERENTIATOR).roundToLong()
    interpolator = DecelerateInterpolator()
    addListener(getAnimatorListener({}, onAnimationEnd))
    return this
}

typealias OutgoingAnimatorProducer = (() -> Int) -> ObjectAnimator
typealias IncomingAnimatorProducer = (() -> Unit) -> ObjectAnimator

fun ImageButton.getScaleAndFadeAnimatorProducersPair() = Pair(
    getScaleDownFadeOutAnimatorProducer(),
    getScaleUpFadeInAnimatorProducer()
)

fun ImageButton.getRotateAroundYAnimatorProducersPair() = Pair(
    getRotationY0To90AnimatorProducer(),
    getRotationY90To180AnimatorProducer()
)

private fun ImageButton.getScaleDownFadeOutAnimatorProducer(): OutgoingAnimatorProducer {
    return { onAnimationEnd ->
        getScaleAndFadeAnimator(
            this,
            1f,
            SCALE_DOWN_END,
            1f,
            0f
        ).setupOutgoingAnimator { setImageResource(onAnimationEnd()) }
    }
}

private fun ImageButton.getScaleUpFadeInAnimatorProducer(): IncomingAnimatorProducer {
    return { onAnimationEnd ->
        getScaleAndFadeAnimator(
            this,
            SCALE_UP_START,
            1f,
            0f,
            1f
        ).setupIncomingAnimator(onAnimationEnd)
    }
}

private fun ImageButton.getRotationY0To90AnimatorProducer(): OutgoingAnimatorProducer {
    return { onAnimationEnd ->
        getRotationYAnimator(
            this,
            0f,
            90f
        ).setupOutgoingAnimator { setImageResource(onAnimationEnd()) }
    }
}

private fun ImageButton.getRotationY90To180AnimatorProducer(): IncomingAnimatorProducer {
    return { onAnimationEnd ->
        getRotationYAnimator(
            this,
            90f,
            180f
        ).setupIncomingAnimator(onAnimationEnd)
    }
}

private fun getRotationYAnimator(
    button: ImageButton,
    startAngle: Float,
    endAngle: Float
): ObjectAnimator = ObjectAnimator.ofFloat(
    button,
    View.ROTATION_Y,
    startAngle,
    endAngle
)

private fun getScaleAndFadeAnimator(
    button: ImageButton,
    scaleStart: Float,
    scaleEnd: Float,
    fadeStart: Float,
    fadeEnd: Float
): ObjectAnimator {
    val scaleX = PropertyValuesHolder.ofFloat(
        View.SCALE_X,
        scaleStart,
        scaleEnd
    )

    val scaleY = PropertyValuesHolder.ofFloat(
        View.SCALE_Y,
        scaleStart,
        scaleEnd
    )

    val fade = PropertyValuesHolder.ofFloat(
        View.ALPHA,
        fadeStart,
        fadeEnd
    )

    return ObjectAnimator.ofPropertyValuesHolder(
        button,
        scaleX,
        scaleY,
        fade
    )
}

fun List<ExchangeableCurrency>.toDomain(context: Context) = map {
    it.toDomain(context)
}