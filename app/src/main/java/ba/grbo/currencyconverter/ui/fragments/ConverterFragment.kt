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
import androidx.annotation.AttrRes
import androidx.annotation.ColorRes
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

    @Inject
    @AutohideScrollbar
    @JvmField
    var autohideScroller: Boolean = false

    @Inject
    @ExtendChooserLandscape
    @JvmField
    var extendChooserInLandscape: Boolean = false

    private lateinit var fromDropdownActionAnimator: ObjectAnimator
    private lateinit var fromDropdownTitleAnimator: ObjectAnimator
    private lateinit var fromCurrencyLayoutAnimator: ObjectAnimator

    private lateinit var toFromDropdownTitleAnimator: ObjectAnimator
    private lateinit var toFromCurrencyLayoutAnimator: ObjectAnimator
    private lateinit var toFromCurrencyAnimatorMain: ObjectAnimator
    private lateinit var toFromCurrencyAnimatorSecondary: ObjectAnimator
    private lateinit var toFromDropdownActionAnimator: ObjectAnimator

    private lateinit var fromToDropdownTitleAnimator: ObjectAnimator
    private lateinit var fromToCurrencyLayoutAnimator: ObjectAnimator
    private lateinit var fromToCurrencyAnimatorMain: ObjectAnimator
    private lateinit var fromToCurrencyAnimatorSecondary: ObjectAnimator
    private lateinit var fromToDropdownActionAnimator: ObjectAnimator

    private lateinit var toDropdownActionAnimator: ObjectAnimator
    private lateinit var toDropdownTitleAnimator: ObjectAnimator
    private lateinit var toCurrencyLayoutAnimator: ObjectAnimator

    private lateinit var converterLayoutAnimator: ObjectAnimator
    private lateinit var bottomNavigationAnimator: ObjectAnimator

    private lateinit var fadeIn: AlphaAnimation
    private lateinit var fadeOut: AlphaAnimation
    private var orientation = Int.MIN_VALUE
    private var recyclerViewHeight = Int.MIN_VALUE

    object Colors {
        var WHITE = 0
        var BLACK = 0
        var LIGHT_GRAY = 0
        var PRIMARY = 0
        var PRIMARY_VARIANT = 0
        var BORDER = 0
        var DIVIDER = 0
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
        setDropdownsBackground()
        initializeAnimations()
        initializeAnimators()
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
        dropdown.background = dropdown.getGradientDrawable()
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
        initializeCurrencyLayoutAnimators()
        initializeDropdownTitleAnimators()
        initializeDropdownActionAnimators()
        initializeCurrencyAnimators()
        initializeConverterLayoutAnimator()
        initializeBottomNavigationAnimator()
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

    private fun initializeCurrencyLayoutAnimators() {
        binding.fromCurrencyChooser.currencyLayout.run {
            fromCurrencyLayoutAnimator = getAnimator()
            toFromCurrencyLayoutAnimator = getBackgroundAnimator()
        }

        binding.toCurrencyChooser.currencyLayout.run {
            toCurrencyLayoutAnimator = getAnimator()
            fromToCurrencyLayoutAnimator = getBackgroundAnimator()
        }
    }

    private fun initializeDropdownTitleAnimators() {
        binding.fromCurrencyChooser.dropdownTitle.run {
            fromDropdownTitleAnimator = getAnimator()
            toFromDropdownTitleAnimator = getBackgroundAnimator()
        }

        binding.toCurrencyChooser.dropdownTitle.run {
            toDropdownTitleAnimator = getAnimator()
            fromToDropdownTitleAnimator = getBackgroundAnimator()
        }
    }

    private fun initializeCurrencyAnimators() {
        binding.fromCurrencyChooser.run {
            toFromCurrencyAnimatorMain = currencyMain.getCurrencyAnimator()
            toFromCurrencyAnimatorSecondary = currencySecondary.getCurrencyAnimator()
        }

        binding.toCurrencyChooser.run {
            fromToCurrencyAnimatorMain = currencyMain.getCurrencyAnimator()
            fromToCurrencyAnimatorSecondary = currencySecondary.getCurrencyAnimator()
        }
    }

    private fun initializeDropdownActionAnimators() {
        binding.fromCurrencyChooser.dropdownAction.run {
            fromDropdownActionAnimator = getAnimator()
            toFromDropdownActionAnimator = getBackgroundColorAnimator()
        }

        binding.toCurrencyChooser.dropdownAction.run {
            toDropdownActionAnimator = getAnimator()
            fromToDropdownActionAnimator = getBackgroundColorAnimator()
        }
    }

    private fun initializeConverterLayoutAnimator() {
        converterLayoutAnimator = binding.converterLayout.getAnimator()
    }

    private fun initializeBottomNavigationAnimator() {
        bottomNavigationAnimator =
            (requireActivity() as CurrencyConverterActivity).getBottomNavigationAnimator()
    }

    private fun initializeColors() {
        Colors.WHITE = getColorFromResource(R.color.white)
        Colors.BLACK = getColorFromResource(R.color.black)
        Colors.LIGHT_GRAY = getColorFromResource(R.color.light_gray)
        Colors.PRIMARY = getColorFromAttribute(R.attr.colorPrimary)
        Colors.PRIMARY_VARIANT = getColorFromAttribute(R.attr.colorPrimaryVariant)
        Colors.BORDER = getColorFromResource(R.color.border)
        Colors.DIVIDER = getColorFromResource(R.color.divider)
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
        }
    }

    private fun onCountriesUpdated(countries: List<Country>) {
        (binding.currencies.adapter as CountryAdapter).submitList(countries)
    }

    private fun onSelectedCurrencyChanged(country: Country, from: Boolean) {
        if (from) onSelectedCurrencyChanged(
            country,
            binding.fromCurrencyChooser.currencyMain,
            binding.fromCurrencyChooser.currencySecondary
        ) else onSelectedCurrencyChanged(
            country,
            binding.toCurrencyChooser.currencyMain,
            binding.toCurrencyChooser.currencySecondary
        )
    }

    private fun onSelectedCurrencyChanged(
        country: Country,
        main: TextView,
        secondary: TextView
    ) {
        when {
            main.text.isEmpty() -> {
                main.text = country.currency.getUiName(uiName)
                main.setCompoundDrawablesWithIntrinsicBounds(country.flag, null, null, null)
            }
            secondary.isVisible -> changeCurrencyState(main, secondary, country)
            main.isVisible -> changeCurrencyState(secondary, main, country)
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
        binding.resetSearcher.run {
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

    private fun startObjectAnimator(objectAnimator: ObjectAnimator) {
        if (objectAnimator.isRunning) objectAnimator.reverse()
        else objectAnimator.start()
    }

    private fun reverseObjectAnimator(objectAnimator: ObjectAnimator) {
        objectAnimator.reverse()
    }

    private fun startObjectAnimators(dropdown: Dropdown) {
        if (dropdown == FROM) startFromObjectAnimators()
        else startToObjectAnimators()
    }

    private fun startFromObjectAnimators() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            launch { startObjectAnimator(fromCurrencyLayoutAnimator) }
            launch { startObjectAnimator(fromDropdownActionAnimator) }
            launch { startObjectAnimator(converterLayoutAnimator) }
            launch { startObjectAnimator(fromDropdownTitleAnimator) }
            launch { startObjectAnimator(fromToCurrencyLayoutAnimator) }
            launch { startObjectAnimator(fromToDropdownTitleAnimator) }
            launch { startObjectAnimator(fromToCurrencyAnimatorMain) }
            launch { startObjectAnimator(fromToCurrencyAnimatorSecondary) }
            launch { startObjectAnimator(fromToDropdownActionAnimator) }
            launch { startObjectAnimator(bottomNavigationAnimator) }
        }
    }

    private fun startToObjectAnimators() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            launch { startObjectAnimator(toCurrencyLayoutAnimator) }
            launch { startObjectAnimator(toDropdownActionAnimator) }
            launch { startObjectAnimator(converterLayoutAnimator) }
            launch { startObjectAnimator(toDropdownTitleAnimator) }
            launch { startObjectAnimator(toFromCurrencyLayoutAnimator) }
            launch { startObjectAnimator(toFromDropdownTitleAnimator) }
            launch { startObjectAnimator(toFromCurrencyAnimatorMain) }
            launch { startObjectAnimator(toFromCurrencyAnimatorSecondary) }
            launch { startObjectAnimator(toFromDropdownActionAnimator) }
            launch { startObjectAnimator(bottomNavigationAnimator) }
        }
    }

    private fun reverseObjectAnimators(dropdown: Dropdown) {
        if (dropdown == FROM) reverseFromObjectAnimators()
        else reverseToObjectAnimators()
    }

    private fun reverseFromObjectAnimators() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            launch { reverseObjectAnimator(fromCurrencyLayoutAnimator) }
            launch { reverseObjectAnimator(fromDropdownActionAnimator) }
            launch { reverseObjectAnimator(converterLayoutAnimator) }
            launch { reverseObjectAnimator(fromDropdownTitleAnimator) }
            launch { reverseObjectAnimator(fromToCurrencyLayoutAnimator) }
            launch { reverseObjectAnimator(fromToDropdownTitleAnimator) }
            launch { reverseObjectAnimator(fromToCurrencyAnimatorMain) }
            launch { reverseObjectAnimator(fromToCurrencyAnimatorSecondary) }
            launch { reverseObjectAnimator(fromToDropdownActionAnimator) }
            launch { reverseObjectAnimator(bottomNavigationAnimator) }
        }
    }

    private fun reverseToObjectAnimators() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            launch { reverseObjectAnimator(toCurrencyLayoutAnimator) }
            launch { reverseObjectAnimator(toDropdownActionAnimator) }
            launch { reverseObjectAnimator(converterLayoutAnimator) }
            launch { reverseObjectAnimator(toDropdownTitleAnimator) }
            launch { reverseObjectAnimator(toFromCurrencyLayoutAnimator) }
            launch { reverseObjectAnimator(toFromDropdownTitleAnimator) }
            launch { reverseObjectAnimator(toFromCurrencyAnimatorMain) }
            launch { reverseObjectAnimator(toFromCurrencyAnimatorSecondary) }
            launch { reverseObjectAnimator(toFromDropdownActionAnimator) }
            launch { reverseObjectAnimator(bottomNavigationAnimator) }
        }
    }

    private fun expandDropdown(dropdown: Dropdown) {
        startObjectAnimators(dropdown)
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
        reverseObjectAnimators(dropdown)
        hideCurrenciesCard()
        viewModel.onDropdownCollapsed()
    }

    private fun hideCurrenciesCard() {
        binding.currenciesCard.visibility = View.INVISIBLE
    }
}