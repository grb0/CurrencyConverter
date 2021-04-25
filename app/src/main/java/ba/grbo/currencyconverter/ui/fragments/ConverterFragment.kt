package ba.grbo.currencyconverter.ui.fragments

import android.animation.Animator
import android.animation.LayoutTransition
import android.animation.LayoutTransition.*
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Point
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
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
import ba.grbo.currencyconverter.di.AutohideScrollbar
import ba.grbo.currencyconverter.di.ExtendChooserLandscape
import ba.grbo.currencyconverter.ui.activities.CurrencyConverterActivity
import ba.grbo.currencyconverter.ui.adapters.CountryAdapter
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.*
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.Dropdown.FROM
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.DropdownState.*
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.SearcherState.Focusing
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.SearcherState.Unfocusing
import ba.grbo.currencyconverter.util.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class ConverterFragment : Fragment() {
    private val viewModel: ConverterViewModel by viewModels()
    private lateinit var binding: FragmentConverterBinding

    @Inject
    lateinit var uiName: Currency.UiName

    @Suppress("PropertyName")
    @Inject
    lateinit var Colors: Colors

    @Inject
    @AutohideScrollbar
    @JvmField
    var autohideScroller: Boolean = false

    @Inject
    @ExtendChooserLandscape
    @JvmField
    var extendChooserInLandscape: Boolean = false

    private lateinit var fromFadeIn: AlphaAnimation
    private lateinit var fromFadeOut: AlphaAnimation

    private lateinit var toFadeIn: AlphaAnimation
    private lateinit var toFadeOut: AlphaAnimation

    private var orientation = Int.MIN_VALUE
    private var recyclerViewHeight = Int.MIN_VALUE

    @Suppress("PropertyName")
    lateinit var Animators: ObjectAnimators

    @Suppress("PrivatePropertyName", "PropertyName")
    class ObjectAnimators(
        binding: FragmentConverterBinding,
        bottomNavigationAnimator: ObjectAnimator,
        Colors: Colors
    ) {
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
                    CURRENCY_DOUBLE = binding.fromCurrencyDouble.getDoubleAnimator(true)
                }
            }
        }

        private val To = object : Base {
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
                    CURRENCY_DOUBLE = binding.toCurrencyDouble.getDoubleAnimator()
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

        private fun startFromObjectAnimators(scope: CoroutineScope) {
            scope.launch {
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

        private fun startToObjectAnimators(scope: CoroutineScope) {
            scope.launch {
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

        private fun reverseFromObjectAnimators(scope: CoroutineScope) {
            scope.launch {
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

        private fun reverseToObjectAnimators(scope: CoroutineScope) {
            scope.launch {
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

        fun animateCurrencySwapping(scope: CoroutineScope) {
            scope.launch {
                launch { CURRENCY_SWAPPING.begin() }
                launch { From.CURRENCY_DOUBLE.begin() }
                launch { To.CURRENCY_DOUBLE }
            }
        }

        fun startObjectAnimators(dropdown: Dropdown, scope: CoroutineScope) {
            if (dropdown == FROM) startFromObjectAnimators(scope)
            else startToObjectAnimators(scope)
        }

        fun reverseObjectAnimators(dropdown: Dropdown, scope: CoroutineScope) {
            if (dropdown == FROM) reverseFromObjectAnimators(scope)
            else reverseToObjectAnimators(scope)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        orientation = resources.configuration.orientation
        binding = FragmentConverterBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
        }
        setDropdownsBackground()
        initializeAnimations()
        Animators = ObjectAnimators(
            binding,
            (requireActivity() as CurrencyConverterActivity).getBottomNavigationAnimator(),
            Colors
        )
        setUpConverterLayoutTransition()
        setUpRecyclerView()
        setListeners()
        collectFlows()
        return binding.root
    }

    override fun onCreateAnimator(transit: Int, enter: Boolean, nextAnim: Int): Animator {
        return getMaterialFadeThroughAnimator(binding.converterLayout, enter)
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

    private fun setDropdownsBackground() {
        setDropdownBackground(binding.fromCurrencyChooser.currencyLayout)
        setDropdownBackground(binding.toCurrencyChooser.currencyLayout)
    }

    private fun setDropdownBackground(dropdown: LinearLayout) {
        dropdown.background = dropdown.getGradientDrawable(
            strokeColor = Colors.BORDER,
            backgroundColor = Colors.WHITE
        )
    }

    private fun initializeAnimations() {
        fromFadeIn = AlphaAnimation(0f, 1f).setUp(resources)
        fromFadeOut = AlphaAnimation(1f, 0f).setUp(resources)

        toFadeIn = AlphaAnimation(0f, 1f).setUp(resources)
        toFadeOut = AlphaAnimation(1f, 0f).setUp(resources)
    }

    private fun setUpConverterLayoutTransition() {
        binding.converterLayout.layoutTransition = LayoutTransition().apply {
            setInterpolator(CHANGE_APPEARING, LinearInterpolator())
            setInterpolator(CHANGE_DISAPPEARING, LinearInterpolator())
            setInterpolator(CHANGING, LinearInterpolator())
            setInterpolator(APPEARING, LinearInterpolator())
            setInterpolator(DISAPPEARING, LinearInterpolator())
            setDuration(resources.getInteger(R.integer.anim_time).toLong())
        }
    }

    private fun setListeners() {
        binding.fromCurrencyChooser.run {
            dropdownAction.setOnClickListener { viewModel.onFromDropdownActionClicked() }
            currencyLayout.setOnClickListener { viewModel.onFromDropdownClicked() }
            dropdownTitle.setOnClickListener { viewModel.onFromTitleClicked() }

        }

        binding.toCurrencyChooser.run {
            dropdownAction.setOnClickListener { viewModel.onToDropdownActionClicked() }
            currencyLayout.setOnClickListener { viewModel.onToDropdownClicked() }
            dropdownTitle.setOnClickListener { viewModel.onToTitleClicked() }
        }

        binding.run {
            dropdownSwapper.setOnClickListener { viewModel.onDropdownSwapperClicked() }
            resetSearcher.setOnClickListener { viewModel.onResetSearcherClicked() }
            currenciesSearcher.run {
                setOnFocusChangeListener { _, hasFocus ->
                    viewModel.onSearcherFocusChanged(hasFocus)
                }
                addTextChangedListener {
                    it?.let { viewModel.onTextChanged(it.toString()) }
                }
            }
            setOnScrollListener(currencies)
        }
    }

    private fun setUpRecyclerView() {
        assignAdapter()
        modifyHeightInPortraitMode(addVerticalDivider())
        initializeFastScroller(binding.currencies)
    }

    private fun assignAdapter() {
        val adapter = CountryAdapter(viewLifecycleOwner, uiName) {
            viewModel.onCurrencyClicked(it)
        }
        binding.currencies.adapter = adapter
    }

    private fun addVerticalDivider(): DividerItemDecoration {
        return DividerItemDecoration(
            requireContext(),
            DividerItemDecoration.VERTICAL
        ).also { binding.currencies.addItemDecoration(it) }
    }

    private fun modifyHeightInPortraitMode(verticalDivider: DividerItemDecoration) {
        // Only in portrait mode
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            val countryHolder = binding.currencies.adapter!!.createViewHolder(
                binding.currencies,
                0
            )
            val verticalDividersHeight = ((verticalDivider.drawable?.intrinsicHeight ?: 3) * 4)
            val countryHoldersHeight = countryHolder.itemView.layoutParams.height * 4
            recyclerViewHeight = verticalDividersHeight + countryHoldersHeight
            binding.currencies.layoutParams.height = recyclerViewHeight

            // Let it not be in vain :)
            binding.currencies.recycledViewPool.putRecycledView(countryHolder)
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
            collectWhenStarted(fromSelectedCurrency, { onSelectedCurrencyChanged(it, true) })
            collectWhenStarted(toSelectedCurrency, { onSelectedCurrencyChanged(it, false) })
            collectWhenStarted(dropdownState, ::onDropdownStateChanged)
            collectWhenStarted(showResetButton, ::showResetButton)
            collectWhenStarted(resetSearcher, { resetSearcher() }, false)
            collectWhenStarted(modifyDivider, ::onDividerHeightChanged)
            collectWhenStarted(countries, ::onCountriesUpdated)
            collectWhenStarted(searcherState, ::onSearcherStateChanged)
            collectWhenStarted(
                animateCurrencySwapping,
                { Animators.animateCurrencySwapping(viewLifecycleOwner.lifecycleScope) },
                false
            )
        }
    }

    private fun onCountriesUpdated(countries: List<Country>) {
        (binding.currencies.adapter as CountryAdapter).submitList(countries)
    }

    private fun onSelectedCurrencyChanged(country: Country, from: Boolean) {
        if (from) onSelectedCurrencyChanged(
            country,
            binding.fromCurrencyChooser.currency,
            binding.fromCurrencyChooser.currencyDouble,
            fromFadeIn,
            fromFadeOut
        ) else onSelectedCurrencyChanged(
            country,
            binding.toCurrencyChooser.currency,
            binding.toCurrencyChooser.currencyDouble,
            toFadeIn,
            toFadeOut
        )
    }

    private fun onSelectedCurrencyChanged(
        country: Country,
        main: TextView,
        secondary: TextView,
        fadeIn: AlphaAnimation,
        fadeOut: AlphaAnimation
    ) {
        when {
            main.text.isEmpty() -> {
                main.text = country.currency.getUiName(uiName)
                main.setCompoundDrawablesWithIntrinsicBounds(country.flag, null, null, null)
            }
            secondary.isVisible -> changeCurrencyState(country, main, secondary, fadeIn, fadeOut)
            main.isVisible -> changeCurrencyState(country, secondary, main, fadeIn, fadeOut)
        }
    }

    private fun changeCurrencyState(
        country: Country,
        fadedIn: TextView,
        fadedOut: TextView,
        fadeIn: AlphaAnimation,
        fadeOut: AlphaAnimation
    ) {
        fadedIn.run {
            text = country.currency.getUiName(uiName)
            setCompoundDrawablesWithIntrinsicBounds(country.flag, null, null, null)
        }

        fadeIn.setAnimationListener(getAnimationListener(fadedIn, true, fadeIn, fadeOut))
        fadeOut.setAnimationListener(getAnimationListener(fadedOut, false, fadeIn, fadeOut))

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            launch { fadedIn.startAnimation(fadeIn) }
            launch { fadedOut.startAnimation(fadeOut) }
        }
    }

    private fun getAnimationListener(
        view: View,
        fadinIn: Boolean,
        fadeIn: AlphaAnimation,
        fadeOut: AlphaAnimation
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
        is Expanding -> {
            expandDropdown(state.dropdown)
            setOnScreenTouched(state.dropdown, true)
        }
        is Collapsing -> {
            collapseDropdown(state.dropdown)
            setOnScreenTouched(state.dropdown, false)
        }
        is Collapsed -> {
        } // When collapsed do nothing
    }

    private fun showResetButton(showButton: Boolean) {
        if (showButton) setAnimationListenerAndStartAnimation(fromFadeIn, true)
        else setAnimationListenerAndStartAnimation(fromFadeOut, false)
    }

    private fun setAnimationListenerAndStartAnimation(
        animation: AlphaAnimation,
        fadinIn: Boolean
    ) {
        animation.setAnimationListener(
            getAnimationListener(
                binding.resetSearcher,
                fadinIn,
                fromFadeIn,
                fromFadeOut
            )
        )
        binding.resetSearcher.startAnimation(animation)
    }

    private fun resetSearcher() {
        binding.currenciesSearcher.setText("")
    }

    private fun onDividerHeightChanged(topReached: Boolean) {
        val drawable = if (topReached) R.drawable.divider_top else R.drawable.divider_bottom
        binding.divider.setBackgroundResource(drawable)
    }

    private fun onSearcherStateChanged(state: SearcherState) = when (state) {
        is Focusing -> onSearcherFocused(state.dropdown)
        is Unfocusing -> onSearcherUnfocused(state.dropdown)
        else -> {
            // Do nothing
        }
    }

    private fun onSearcherFocused(dropdown: Dropdown) {
        modifyOnScreenTouched(dropdown)
    }

    private fun modifyOnScreenTouched(dropdown: Dropdown) {
        val activity = (requireActivity() as CurrencyConverterActivity)
        val currentOnScreenTouched = activity.onScreenTouched
        activity.onScreenTouched = {
            val (isHandled, touchPoint, shouldConsume) = currentOnScreenTouched!!.invoke(it)
            if (!isHandled) {
                val currenciesCardTouched = isPointInsideViewBounds(
                    binding.currenciesCard,
                    touchPoint
                )
                val currenciesSearcherTouched = isPointInsideViewBounds(
                    binding.currenciesSearcher,
                    touchPoint
                )

                val handled = viewModel.onScreenTouched(
                    dropdown,
                    currenciesCardTouched,
                    currenciesSearcherTouched
                )
                handled to touchPoint
                Triple(handled, touchPoint, false)
            } else Triple(isHandled, touchPoint, shouldConsume)
        }
    }

    private fun onSearcherUnfocused(dropdown: Dropdown) {
        releaseFocus()
        hideKeyboard()
        if (viewModel.dropdownState.value !is Collapsed) restoreOriginalOnScreenTouched(dropdown)
        viewModel.onSearcherUnfocused(dropdown)
    }

    private fun releaseFocus() {
        binding.currenciesSearcher.clearFocus()
    }


    private fun hideKeyboard() {
        val imm = getInputMethodManager()
        imm.hideSoftInputFromWindow(requireActivity().window.decorView.windowToken, 0)
    }

    private fun getInputMethodManager(): InputMethodManager {
        return requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    private fun restoreOriginalOnScreenTouched(dropdown: Dropdown) {
        setOnScreenTouched(dropdown, true)
    }

    private fun expandDropdown(dropdown: Dropdown) {
        Animators.startObjectAnimators(dropdown, viewLifecycleOwner.lifecycleScope)
        showCurrenciesCard(dropdown)
    }

    private fun showCurrenciesCard(dropdown: Dropdown) {
        modifyCurrenciesCardPosition(dropdown)
        binding.currenciesCard.visibility = View.VISIBLE
    }

    private fun modifyCurrenciesCardPosition(dropdown: Dropdown) {
        val viewId = if (dropdown == FROM) binding.fromCurrencyChooser.dropdownLayout.id
        else binding.toCurrencyChooser.dropdownLayout.id
        modifyCurrenciesCardPosition(viewId)
    }

    private fun modifyCurrenciesCardPosition(viewId: Int) {
        ConstraintSet().apply {
            clone(binding.converterLayout)

            if (extendChooserInLandscape && orientation == Configuration.ORIENTATION_LANDSCAPE) {
                connect(
                    binding.currenciesCard.id,
                    ConstraintSet.TOP,
                    binding.converterLayout.id,
                    ConstraintSet.TOP
                )
                connect(
                    binding.currenciesCard.id,
                    ConstraintSet.BOTTOM,
                    binding.converterLayout.id,
                    ConstraintSet.BOTTOM
                )
                connect(
                    binding.currenciesCard.id,
                    ConstraintSet.START,
                    binding.converterLayout.id,
                    ConstraintSet.START
                )
                connect(
                    binding.currenciesCard.id,
                    ConstraintSet.END,
                    binding.converterLayout.id,
                    ConstraintSet.END
                )
                applyTo(binding.converterLayout)
                binding.currenciesCard.setMargins(8f)
            } else {
                connect(
                    binding.currenciesCard.id,
                    ConstraintSet.TOP,
                    viewId,
                    ConstraintSet.BOTTOM
                )
                connect(
                    binding.currenciesCard.id,
                    ConstraintSet.BOTTOM,
                    binding.converterLayout.id,
                    ConstraintSet.BOTTOM
                )
                connect(
                    binding.currenciesCard.id,
                    ConstraintSet.START,
                    viewId,
                    ConstraintSet.START
                )
                connect(
                    binding.currenciesCard.id,
                    ConstraintSet.END,
                    viewId,
                    ConstraintSet.END
                )
                applyTo(binding.converterLayout)
            }
        }
    }

    private fun setOnScreenTouched(dropdown: Dropdown, shouldSet: Boolean) {
        (requireActivity() as CurrencyConverterActivity).onScreenTouched = if (!shouldSet) null
        else { event -> onScreenTouched(dropdown, event) }
    }

    private fun onScreenTouched(
        dropdown: Dropdown,
        event: MotionEvent
    )
            : Triple<Boolean, Point, Boolean> {
        val touchPoint = Point(event.rawX.roundToInt(), event.rawY.roundToInt())
        val handled = onScreenTouched(dropdown, touchPoint)
        return Triple(handled, touchPoint, handled)
    }

    private fun onScreenTouched(
        dropdown: Dropdown,
        touchPoint: Point
    ): Boolean {
        val currencyLayout: LinearLayout
        val dropdownTitle: TextView

        if (dropdown == FROM) {
            currencyLayout = binding.fromCurrencyChooser.currencyLayout
            dropdownTitle = binding.fromCurrencyChooser.dropdownTitle
        } else {
            currencyLayout = binding.toCurrencyChooser.currencyLayout
            dropdownTitle = binding.toCurrencyChooser.dropdownTitle
        }

        val currencyLayoutTouched = isPointInsideViewBounds(
            currencyLayout,
            touchPoint
        )

        val dropdownTitleTouched = isPointInsideViewBounds(
            dropdownTitle,
            touchPoint
        )

        val currenciesCardTouched = isPointInsideViewBounds(
            binding.currenciesCard,
            touchPoint
        )

        return viewModel.onScreenTouched(
            dropdown,
            currencyLayoutTouched,
            dropdownTitleTouched,
            currenciesCardTouched
        )
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

    private fun collapseDropdown(dropdown: Dropdown) {
        Animators.reverseObjectAnimators(dropdown, viewLifecycleOwner.lifecycleScope)
        hideCurrenciesCard()
        viewModel.onDropdownCollapsed()
    }

    private fun hideCurrenciesCard() {
        binding.currenciesCard.visibility = View.INVISIBLE
    }
}