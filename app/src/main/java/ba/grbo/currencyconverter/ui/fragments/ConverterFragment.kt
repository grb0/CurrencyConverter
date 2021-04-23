package ba.grbo.currencyconverter.ui.fragments

import android.animation.ArgbEvaluator
import android.animation.LayoutTransition
import android.animation.LayoutTransition.*
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Point
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Property
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.ColorRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import ba.grbo.currencyconverter.R
import ba.grbo.currencyconverter.data.models.Country
import ba.grbo.currencyconverter.data.models.Currency
import ba.grbo.currencyconverter.databinding.FragmentConverterBinding
import ba.grbo.currencyconverter.ui.activities.CurrencyConverterActivity
import ba.grbo.currencyconverter.ui.adapters.CountryAdapter
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.DropdownState
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.DropdownState.*
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.SearcherState
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.SearcherState.FOCUSING
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.SearcherState.UNFOCUSING
import ba.grbo.currencyconverter.util.collectWhenStarted
import ba.grbo.currencyconverter.util.getColorFromAttribute
import ba.grbo.currencyconverter.util.setUp
import ba.grbo.currencyconverter.util.toPixels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class ConverterFragment : Fragment() {
    private val viewModel: ConverterViewModel by viewModels()
    private lateinit var binding: FragmentConverterBinding

    @Inject
    lateinit var uiName: Currency.UiName

    @Inject
    @JvmField
    var autohideScroller: Boolean = false

    private lateinit var dropdownActionAnimator: ObjectAnimator
    private lateinit var converterLayoutAnimator: ObjectAnimator
    private lateinit var dropdownTitleAnimator: ObjectAnimator
    private lateinit var currencyLayoutAnimator: ObjectAnimator
    private lateinit var fadeIn: AlphaAnimation
    private lateinit var fadeOut: AlphaAnimation
    private var orientation = Int.MIN_VALUE
    private var recyclerViewHeight = Int.MIN_VALUE

    object Colors {
        var WHITE = 0
        var LIGHT_GRAY = 0
        var PRIMARY = 0
        var PRIMARY_VARIANT = 0
        var BORDER = 0
        var CONTROL_NORMAL = 0
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        orientation = resources.configuration.orientation
        initializeColors()
        binding = FragmentConverterBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
        }
        binding.fromCurrencyChooser.currencyLayout.background = getCurrencyLayoutDrawable()
        initializeAnimations()
        initializeAnimators()
        setUpDropdownLayoutTransition()
        setUpRecyclerView()
        setListeners()
        collectFlows()
        return binding.root
    }

    private fun getCurrencyLayoutDrawable(
        strokeWidth: Float = 1f,
        strokeColor: Int = Colors.BORDER,
        backgroundColor: Int = Colors.WHITE
    ) = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        setStroke(strokeWidth.toPixels(resources).roundToInt(), strokeColor)
        setColor(backgroundColor)
        cornerRadius = 5f.toPixels(resources)
    }

    private fun initializeFastScroller(recyclerView: RecyclerView) {
        val thumb = ContextCompat.getDrawable(
            requireContext(),
            R.drawable.afs_md2_thumb
        )!!.apply { setTintList(ColorStateList.valueOf(Colors.PRIMARY)) }

        FastScrollerBuilder(recyclerView)
            .useMd2Style()
            .setThumbDrawable(thumb).apply { if (!autohideScroller) disableScrollbarAutoHide() }
            .build()
    }

    private fun initializeAnimations() {
        fadeIn = AlphaAnimation(0f, 1f).apply {
            interpolator = LinearInterpolator()
            duration = resources.getInteger(R.integer.anim_time).toLong()
        }

        fadeOut = AlphaAnimation(1f, 0f).apply {
            interpolator = LinearInterpolator()
            duration = resources.getInteger(R.integer.anim_time).toLong()
        }
    }

    private fun initializeAnimators() {
        initializeCurrencyLayoutAnimator()
        initializeDropdownTitleAnimator()
        initializeDropdownActionAnimator()
        initializeConverterLayoutAnimator()
    }

    private fun setUpDropdownLayoutTransition() {
        binding.fromCurrencyChooser.dropdownLayout.layoutTransition = LayoutTransition().apply {
            setInterpolator(CHANGE_APPEARING, LinearInterpolator())
            setInterpolator(CHANGE_DISAPPEARING, LinearInterpolator())
            setInterpolator(CHANGING, LinearInterpolator())
            setInterpolator(APPEARING, LinearInterpolator())
            setInterpolator(DISAPPEARING, LinearInterpolator())
            setDuration(resources.getInteger(R.integer.anim_time).toLong())
        }
    }

    private fun initializeCurrencyLayoutAnimator() {
        currencyLayoutAnimator = ObjectAnimator.ofObject(
            binding.fromCurrencyChooser.currencyLayout,
            Property.of(LinearLayout::class.java, Drawable::class.java, "background"),
            { fraction, _, _ ->
                val colorEvaluator = ArgbEvaluator()
                val strokeColor = colorEvaluator.evaluate(
                    fraction,
                    Colors.BORDER,
                    Colors.PRIMARY_VARIANT
                ) as Int
                val backgroundColor = colorEvaluator.evaluate(
                    fraction,
                    Colors.WHITE,
                    Colors.LIGHT_GRAY
                ) as Int
                val strokeWidth = 1f + (fraction * (2f - 1f))
                val drawable = getCurrencyLayoutDrawable(
                    strokeWidth,
                    strokeColor,
                    backgroundColor
                )
                drawable
            },
            getCurrencyLayoutDrawable(),
            getCurrencyLayoutDrawable(2f, Colors.PRIMARY_VARIANT, Colors.LIGHT_GRAY)
        ).setUp(resources)
    }

    private fun initializeDropdownTitleAnimator() {
        val backgroundColorProperty = PropertyValuesHolder.ofObject(
            "backgroundColor",
            { fraction, startValue, endValue ->
                ArgbEvaluator().evaluate(fraction, startValue, endValue) as Int
            },
            Colors.WHITE,
            Colors.LIGHT_GRAY
        )

        val textColorProperty = PropertyValuesHolder.ofObject(
            "textColor",
            { fraction, startValue, endValue ->
                ArgbEvaluator().evaluate(fraction, startValue, endValue) as Int
            },
            Colors.BORDER,
            Colors.PRIMARY_VARIANT
        )

        val typeFaceProperty = PropertyValuesHolder.ofObject(
            Property.of(TextView::class.java, Typeface::class.java, "typeface"),
            { fraction, startValue, endValue ->
                if (fraction < 0.33) startValue
                else if (fraction > 0.33 && fraction < 0.66) ResourcesCompat.getFont(
                    requireContext(), R.font.roboto_medium
                ) else endValue
            },
            ResourcesCompat.getFont(requireContext(), R.font.roboto),
            ResourcesCompat.getFont(requireContext(), R.font.roboto_bold)
        )

        dropdownTitleAnimator = ObjectAnimator.ofPropertyValuesHolder(
            binding.fromCurrencyChooser.dropdownTitle,
            backgroundColorProperty,
            textColorProperty,
            typeFaceProperty
        ).setUp(resources)
    }

    private fun initializeDropdownActionAnimator() {
        val imageTintListProperty = PropertyValuesHolder.ofObject(
            "imageTintList",
            { fraction, _, _ ->
                val interpolatedColor = ArgbEvaluator().evaluate(
                    fraction,
                    Colors.CONTROL_NORMAL,
                    Colors.PRIMARY_VARIANT
                ) as Int
                ColorStateList.valueOf(interpolatedColor)
            },
            ColorStateList.valueOf(Colors.CONTROL_NORMAL),
            ColorStateList.valueOf(Colors.PRIMARY_VARIANT)
        )
        val rotation = PropertyValuesHolder.ofFloat(View.ROTATION, 0f, 180f)
        dropdownActionAnimator = ObjectAnimator.ofPropertyValuesHolder(
            binding.fromCurrencyChooser.dropdownAction,
            imageTintListProperty,
            rotation
        ).setUp(resources)
    }

    private fun initializeConverterLayoutAnimator() {
        converterLayoutAnimator = ObjectAnimator.ofArgb(
            binding.converterLayout,
            "backgroundColor",
            Colors.WHITE,
            Colors.LIGHT_GRAY
        ).setUp(resources)
    }

    private fun initializeColors() {
        Colors.WHITE = getColorFromResource(R.color.white)
        Colors.LIGHT_GRAY = getColorFromResource(R.color.light_gray)
        Colors.PRIMARY = getColorFromAttribute(R.attr.colorPrimary)
        Colors.PRIMARY_VARIANT = getColorFromAttribute(R.attr.colorPrimaryVariant)
        Colors.BORDER = getColorFromResource(R.color.border)
        Colors.CONTROL_NORMAL = getColorFromAttribute(R.attr.colorControlNormal)
    }

    private fun getColorFromResource(@ColorRes id: Int): Int {
        return ContextCompat.getColor(requireContext(), id)
    }

    @Suppress("SameParameterValue")
    private fun getColorFromAttribute(@AttrRes id: Int): Int {
        return requireContext().getColorFromAttribute(id)
    }

    private fun setListeners() {
        binding.fromCurrencyChooser.run {
            dropdownAction.setOnClickListener { viewModel.onExpandClicked() }
            currencyLayout.setOnClickListener { viewModel.onDropdownClicked() }
            resetSearcher.setOnClickListener { viewModel.onResetSearcherClicked() }
            dropdownTitle.setOnClickListener { viewModel.onTitleClicked() }
            currenciesSearcher.setOnFocusChangeListener { _, hasFocus ->
                viewModel.onSearcherFocusChanged(hasFocus)
            }
            currenciesSearcher.addTextChangedListener {
                it?.let { viewModel.onTextChanged(it.toString()) }
            }
            setOnScrollListener(currencies)
        }
        KeyboardVisibilityEvent.setEventListener(
            requireActivity(),
            viewLifecycleOwner
        ) {
            if (it) modifyCurrenciesCardHeight() else restoreOriginalCurrenciesCardHeight()
        }
    }

    private fun setUpRecyclerView() {
        assignAdapter()
        modifyHeightInPortraitMode(addVerticalDivider())
        initializeFastScroller(binding.fromCurrencyChooser.currencies)
    }

    private fun assignAdapter() {
        val adapter = CountryAdapter(viewLifecycleOwner, uiName) {
            viewModel.onCurrencyClicked(it)
        }
        binding.fromCurrencyChooser.currencies.adapter = adapter
    }

    private fun addVerticalDivider(): DividerItemDecoration {
        return DividerItemDecoration(
            requireContext(),
            DividerItemDecoration.VERTICAL
        ).also { binding.fromCurrencyChooser.currencies.addItemDecoration(it) }
    }

    private fun modifyHeightInPortraitMode(verticalDivider: DividerItemDecoration) {
        // Only in portrait mode
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            val countryHolder = binding.fromCurrencyChooser.currencies.adapter!!.createViewHolder(
                binding.fromCurrencyChooser.currencies,
                0
            )
            val verticalDividersHeight = ((verticalDivider.drawable?.intrinsicHeight ?: 3) * 4)
            val countryHoldersHeight = countryHolder.itemView.layoutParams.height * 4
            recyclerViewHeight = verticalDividersHeight + countryHoldersHeight
            binding.fromCurrencyChooser.currencies.layoutParams.height = recyclerViewHeight

            // Let it not be in vain :)
            binding.fromCurrencyChooser.currencies.recycledViewPool.putRecycledView(countryHolder)
        }
    }

    private fun setOnScrollListener(currencies: RecyclerView) {
        currencies.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                viewModel.onCountriesScrolled(!currencies.canScrollVertically(-1))
            }
        })
    }

    private fun collectFlows() {
        viewModel.run {
            collectWhenStarted(fromSelectedCurrency, ::onSelectedCurrencyChanged)
            collectWhenStarted(fromCurrencyDropdownState, ::onDropdownStateChanged)
            collectWhenStarted(showResetButton, ::showResetButton)
            collectWhenStarted(resetSearcher, { resetSearcher() }, false)
            collectWhenStarted(modifyDivider, ::onDividerHeightChanged)
            collectWhenStarted(countries, ::onCountriesUpdated)
            collectWhenStarted(searcherState, ::onSearcherStateChanged)
        }
    }

    private fun onCountriesUpdated(countries: List<Country>) {
        (binding.fromCurrencyChooser.currencies.adapter as CountryAdapter).submitList(countries)
    }

    private fun onSelectedCurrencyChanged(country: Country) {
        when {
            binding.fromCurrencyChooser.currencyMain.text.isEmpty() -> {
                binding.fromCurrencyChooser.currencyMain.run {
                    text = country.currency.getUiName(uiName)
                    setCompoundDrawablesWithIntrinsicBounds(country.flag, null, null, null)
                }
            }
            binding.fromCurrencyChooser.currencySecondary.isVisible -> {
                changeCurrencyState(
                    binding.fromCurrencyChooser.currencyMain,
                    binding.fromCurrencyChooser.currencySecondary,
                    country
                )
            }
            binding.fromCurrencyChooser.currencyMain.isVisible -> {
                changeCurrencyState(
                    binding.fromCurrencyChooser.currencySecondary,
                    binding.fromCurrencyChooser.currencyMain,
                    country
                )
            }
        }
    }

    private fun changeCurrencyState(
        fadedIn: TextView,
        fadedOut: TextView,
        country: Country
    ) {
        fadedIn.run {
            text = country.currency.getUiName(uiName)
            setCompoundDrawablesWithIntrinsicBounds(country.flag, null, null, null)
        }

        fadeIn.setAnimationListener(getAnimationListener(fadedIn, true))
        fadeOut.setAnimationListener(getAnimationListener(fadedOut, false))

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            launch { fadedIn.startAnimation(fadeIn) }
            launch { fadedOut.startAnimation(fadeOut) }
        }
    }

    private fun getAnimationListener(
        view: View,
        fadinIn: Boolean
    ) = object : Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation?) {
            if (!fadinIn && view is ImageButton) view.isEnabled = false
            else if (view is ImageButton) view.isEnabled = true
        }

        override fun onAnimationEnd(animation: Animation?) {
            if (fadinIn) {
                view.visibility = View.VISIBLE
                fadeIn.setAnimationListener(null)
            } else {
                view.visibility = View.INVISIBLE
                fadeOut.setAnimationListener(null)
            }
        }

        override fun onAnimationRepeat(animation: Animation?) {
        }
    }

    private fun onDropdownStateChanged(state: DropdownState) = when (state) {
        EXPANDING -> {
            expandDropdown()
            setOnScreenTouched(true)
        }
        COLLAPSING -> {
            collapseDropdown()
            setOnScreenTouched(false)
        }
        COLLAPSED -> {
        } // When collapsed do nothing
    }

    private fun showResetButton(showButton: Boolean) {
        binding.fromCurrencyChooser.resetSearcher.run {
            if (showButton) {
                fadeIn.setAnimationListener(getAnimationListener(this, true))
                startAnimation(fadeIn)
            } else {
                fadeOut.setAnimationListener(getAnimationListener(this, false))
                startAnimation(fadeOut)
            }
        }
    }

    private fun resetSearcher() {
        binding.fromCurrencyChooser.currenciesSearcher.setText("")
    }

    private fun onDividerHeightChanged(topReached: Boolean) {
        val drawable = if (topReached) R.drawable.divider_top else R.drawable.divider_bottom
        binding.fromCurrencyChooser.divider.setBackgroundResource(drawable)
    }

    private fun onSearcherStateChanged(state: SearcherState) = when (state) {
        FOCUSING -> onSearcherFocused()
        UNFOCUSING -> onSearcherUnfocused()
        else -> {
            // Do nothing
        }
    }

    private fun onSearcherFocused() {
        modifyOnScreenTouched()
    }

    private fun modifyOnScreenTouched() {
        val activity = (requireActivity() as CurrencyConverterActivity)
        val currentOnScreenTouched = activity.onScreenTouched
        activity.onScreenTouched = {
            val (isHandled, touchPoint) = currentOnScreenTouched!!.invoke(it)
            if (!isHandled) {
                val currenciesCardTouched = isPointInsideViewBounds(
                    binding.fromCurrencyChooser.currenciesCard,
                    touchPoint
                )
                val currenciesSearcherTouched = isPointInsideViewBounds(
                    binding.fromCurrencyChooser.currenciesSearcher,
                    touchPoint
                )

                val handled = viewModel.onScreenTouched(
                    currenciesCardTouched,
                    currenciesSearcherTouched
                )
                handled to touchPoint
            } else isHandled to touchPoint
        }
    }

    private fun modifyCurrenciesCardHeight() {
        setCurrenciesCardHeight(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT)
    }

    private fun onSearcherUnfocused() {
        releaseFocus()
        hideKeyboard()
        if (viewModel.fromCurrencyDropdownState.value != COLLAPSED) restoreOriginalOnScreenTouched()
        viewModel.onSearcherUnfocused()
    }

    private fun releaseFocus() {
        binding.fromCurrencyChooser.currenciesSearcher.clearFocus()
    }


    private fun hideKeyboard() {
        UIUtil.hideKeyboard(requireActivity())
    }

    private fun restoreOriginalCurrenciesCardHeight() {
        setCurrenciesCardHeight(ConstraintLayout.LayoutParams.WRAP_CONTENT, false)
    }

    private fun restoreOriginalOnScreenTouched() {
        setOnScreenTouched(true)
    }

    private fun setCurrenciesCardHeight(height: Int, onKeyboardShown: Boolean = true) {
        val isKeyboardShown = KeyboardVisibilityEvent.isKeyboardVisible(requireActivity())
        if (orientation == Configuration.ORIENTATION_PORTRAIT && isKeyboardShown == onKeyboardShown) {
            val cLP =
                binding.fromCurrencyChooser.currenciesCard.layoutParams as ConstraintLayout.LayoutParams
            cLP.height = height
            binding.fromCurrencyChooser.currenciesCard.layoutParams = cLP
        }
    }

    private fun startObjectAnimator(objectAnimator: ObjectAnimator) {
        if (objectAnimator.isRunning) objectAnimator.reverse()
        else objectAnimator.start()
    }

    private fun reverseObjectAnimator(objectAnimator: ObjectAnimator) {
        objectAnimator.reverse()
    }

    private fun startObjectAnimators() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            launch { startObjectAnimator(currencyLayoutAnimator) }
            launch { startObjectAnimator(dropdownActionAnimator) }
            launch { startObjectAnimator(converterLayoutAnimator) }
            launch { startObjectAnimator(dropdownTitleAnimator) }
        }
    }

    private fun reverseObjectAnimators() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            launch { reverseObjectAnimator(currencyLayoutAnimator) }
            launch { reverseObjectAnimator(dropdownActionAnimator) }
            launch { reverseObjectAnimator(converterLayoutAnimator) }
            launch { reverseObjectAnimator(dropdownTitleAnimator) }
        }
    }

    private fun expandDropdown() {
        startObjectAnimators()
        binding.fromCurrencyChooser.currenciesCard.visibility = View.VISIBLE
        viewModel.onDropdownExpanded()
    }

    private fun setOnScreenTouched(shouldSet: Boolean) {
        (requireActivity() as CurrencyConverterActivity).onScreenTouched = if (!shouldSet) null
        else { event -> getOnScreenTouched(event) }
    }

    private fun getOnScreenTouched(event: MotionEvent): Pair<Boolean, Point> {
        val touchPoint = Point(event.rawX.roundToInt(), event.rawY.roundToInt())
        val dropdownActionTouched = isPointInsideViewBounds(
            binding.fromCurrencyChooser.dropdownAction,
            touchPoint
        )
        val currencyLayoutTouched = isPointInsideViewBounds(
            binding.fromCurrencyChooser.currencyLayout,
            touchPoint
        )
        val dropdownTitleTouched = isPointInsideViewBounds(
            binding.fromCurrencyChooser.dropdownTitle,
            touchPoint
        )
        val currenciesCardTouched = isPointInsideViewBounds(
            binding.fromCurrencyChooser.currenciesCard,
            touchPoint
        )
        val isHandled = viewModel.onScreenTouched(
            dropdownActionTouched,
            currencyLayoutTouched,
            dropdownTitleTouched,
            currenciesCardTouched
        )
        return isHandled to touchPoint
    }

    private fun isPointInsideViewBounds(view: View, point: Point): Boolean = Rect().run {
        // Get view rectangle
        view.getDrawingRect(this)

        // Apply offset
        IntArray(2).also { locationOnScreen ->
            view.getLocationOnScreen(locationOnScreen)
            offset(locationOnScreen[0], locationOnScreen[1])
        }

        // Check if rectangle contains point
        contains(point.x, point.y)
    }

    private fun collapseDropdown() {
        reverseObjectAnimators()
        binding.fromCurrencyChooser.currenciesCard.visibility = View.INVISIBLE
        viewModel.onDropdownCollapsed()
    }
}