package ba.grbo.currencyconverter.ui.fragments

import android.animation.Animator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Point
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import ba.grbo.currencyconverter.R
import ba.grbo.currencyconverter.data.models.domain.ExchangeableCurrency
import ba.grbo.currencyconverter.databinding.DropdownCurrencyChooserBinding
import ba.grbo.currencyconverter.databinding.FragmentConverterBinding
import ba.grbo.currencyconverter.di.AutohideScrollbar
import ba.grbo.currencyconverter.di.ExtendDropdownMenuInLandscape
import ba.grbo.currencyconverter.di.ShowScrollbar
import ba.grbo.currencyconverter.ui.activities.CurrencyConverterActivity
import ba.grbo.currencyconverter.ui.adapters.CurrencyAdapter
import ba.grbo.currencyconverter.ui.miscs.Animations
import ba.grbo.currencyconverter.ui.miscs.ObjectAnimators
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.*
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.Dropdown.FROM
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.DropdownState.*
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.SearcherState.Focusing
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.SearcherState.Unfocusing
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.SwappingState.*
import ba.grbo.currencyconverter.util.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlin.system.exitProcess

@Suppress("PrivatePropertyName")
@AndroidEntryPoint
class ConverterFragment : Fragment() {
    private val viewModel: ConverterViewModel by viewModels()
    private lateinit var binding: FragmentConverterBinding

    @Inject
    lateinit var uiName: ExchangeableCurrency.UiName

    @Suppress("PropertyName")
    @Inject
    lateinit var Colors: Colors

    @Inject
    @ShowScrollbar
    @JvmField
    var showScrollbar: Boolean = true

    @Inject
    @AutohideScrollbar
    @JvmField
    var autohideScroller: Boolean = false

    @Inject
    @ExtendDropdownMenuInLandscape
    @JvmField
    var extendDropdownMenuInLandscape: Boolean = true

    private var orientation = Int.MIN_VALUE
    private var recyclerViewHeight = Int.MIN_VALUE

    private lateinit var Animators: ObjectAnimators
    private lateinit var Animations: Animations

    private lateinit var activity: CurrencyConverterActivity
    private lateinit var fromCurrencyJob: Job
    private lateinit var toCurrencyJob: Job

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return setup(inflater, container)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.resetInternalState()
    }

    override fun onCreateAnimator(transit: Int, enter: Boolean, nextAnim: Int): Animator {
        return getMaterialFadeThroughAnimator(binding.converterLayout, enter)
    }

    private fun setup(inflater: LayoutInflater, container: ViewGroup?): View {
        initActivity()
        initOrientation()
        val root = initBinding(inflater, container)
        setDropdownsBackground()
        initMotions()
        setUpRecyclerView()
        setListeners()
        collectFlows()
        return root
    }

    private fun initActivity() {
        activity = requireActivity() as CurrencyConverterActivity
    }

    private fun initOrientation() {
        orientation = resources.configuration.orientation
    }

    private fun initBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentConverterBinding.inflate(inflater, container, false).also {
        it.lastUpdate = "03 May 2021 14:20"
        it.from = getString(R.string.from_label)
        it.to = getString(R.string.to_label)
        it.showOnlyFavorites = viewModel.showOnlyFavorites
        it.showScrollbar = showScrollbar
        it.extended = orientation.isLandscape && extendDropdownMenuInLandscape
        it.lastTo = viewModel.wasLastClickedTo()
        binding = it
    }.root

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

    private fun setDropdownBackground(dropdown: ConstraintLayout) {
        dropdown.background = dropdown.getGradientDrawable(Colors.WHITE, Colors.BORDER)
    }

    private fun initMotions() {
        initAnimations()
        initAnimators()
    }

    private fun initAnimations() {
        Animations = Animations(resources)
    }

    private fun initAnimators() {
        Animators = ObjectAnimators(
            binding,
            activity.getBottomNavigationAnimator(),
            Colors,
            orientation.isLandscape,
            resources.displayMetrics.widthPixels.toFloat(),
            viewModel::onFavoritesAnimationEnd,
        )
    }

    private fun setUpRecyclerView() {
        disableItemChangedAnimation()
        assignAdapter()
        modifyHeightInPortraitMode(addVerticalDivider())
        if (showScrollbar) initializeFastScroller(binding.dropdownMenu.currencies)
    }

    private fun assignAdapter() {
        binding.dropdownMenu.currencies.adapter = CurrencyAdapter(
            uiName,
            showScrollbar,
            viewModel::onCurrencyClicked,
            viewModel::onCurrenciesChanged,
            viewModel::onFavoritesAnimationEnd
        )
    }

    private fun addVerticalDivider(): DividerItemDecoration {
        return DividerItemDecoration(
            requireContext(),
            DividerItemDecoration.VERTICAL
        ).also { binding.dropdownMenu.currencies.addItemDecoration(it) }
    }

    private fun modifyHeightInPortraitMode(verticalDivider: DividerItemDecoration) {
        // Only in portrait mode
        if (!orientation.isLandscape) {
            val countryHolder = binding.dropdownMenu.currencies.adapter!!.createViewHolder(
                binding.dropdownMenu.currencies,
                0
            )
            val verticalDividersHeight = ((verticalDivider.drawable?.intrinsicHeight ?: 3) * 4)
            val countryHoldersHeight = countryHolder.itemView.layoutParams.height * 4
            recyclerViewHeight = verticalDividersHeight + countryHoldersHeight
            binding.dropdownMenu.currencies.layoutParams.height = recyclerViewHeight

            // Let it not be in vain :)
            binding.dropdownMenu.currencies.recycledViewPool.putRecycledView(countryHolder)
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
            dropdownMenu.run {
                resetSearcher.setOnClickListener { viewModel.onResetSearcherClicked() }
                currenciesSearcher.run {
                    setOnFocusChangeListener { _, hasFocus ->
                        viewModel.onSearcherFocusChanged(hasFocus)
                    }
                    addTextChangedListener {
                        it?.let { viewModel.onSearcherTextChanged(it.toString()) }
                    }
                }
                currencies.addOnScrollListener(getOnScrollListener { !it.canScrollVertically(-1) })
                favorites.setOnClickListener { viewModel.onFavoritesClicked() }
            }
        }
    }

    private fun getOnScrollListener(
        onScrolled: (RecyclerView) -> Boolean
    ) = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            viewModel.onRecyclerViewScrolled(onScrolled(recyclerView))
        }
    }

    private fun collectFlows() {
        viewModel.run {
            collectWhenStarted(databaseExceptionCaught, ::onDatabaseExceptionCaught, false)
            collectWhenStarted(databaseExceptionAcknowledged, { shutDown() }, false)
            collectWhenStarted(databaseUpdateFailed, ::onDatabaseUpdateFailed, false)
            collectWhenStarted(ignoreDatebaseUpdateFailed, { ignoreDatabaseUpdateFailed() }, false)
            fromCurrencyJob = collectWhenStarted(fromCurrency) {
                it?.let { onCurrencyChanged(it, true) }
            }
            toCurrencyJob = collectWhenStarted(toCurrency) {
                it?.let { onCurrencyChanged(it, false) }
            }
            collectWhenStarted(fromSelectedCurrency, { onSelectedCurrencyChanged(it, true) }, true)
            collectWhenStarted(toSelectedCurrency, { onSelectedCurrencyChanged(it, false) }, true)
            collectWhenStarted(dropdownState, ::onDropdownStateChanged, true)
            collectWhenStarted(showResetButton, ::showResetButton, true)
            collectWhenStarted(resetSearcher, { resetSearcher() }, false)
            collectWhenStarted(modifyDivider, ::modifyDividerDrawable, true)
            collectWhenStarted(currencies, { it?.let { onCurrenciesUpdated(it) } }, true)
            collectWhenStarted(searcherState, ::onSearcherStateChanged, true)
            collectWhenStarted(swappingState, ::onSwappingStateChanged, false)
            collectWhenStarted(scrollRecyclerViewToTop, { scrollCurrenciesToTop() }, false)
            collectWhenStarted(onFavoritesClicked, ::animateFavorites, false)
            collectWhenStarted(notifyItemChanged, ::onItemChanged, false)
            collectWhenStarted(notifyItemRemoved, ::onItemRemoved, false)
        }
    }

    private fun ignoreDatabaseUpdateFailed() {
        lifecycleScope.launch {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
            sharedPreferences.edit().run {
                putBoolean(getString(R.string.key_ignore_failed_db_update), true)
                apply()
            }
            viewModel.onFailedDbUpdatedNotificationsIgnored()
        }
    }

    private fun onDatabaseExceptionCaught(dialogInfo: DialogInfo) {
        hideEverything()
        createAlertDialog(dialogInfo)
            .setOnDismissListener { dialogInfo.onDismiss?.invoke() }
            .show()
    }

    private fun onDatabaseUpdateFailed(dialogInfo: ExtendedDialogInfo) {
        createAlertDialog(dialogInfo)
            .setNegativeButton(dialogInfo.negativeButton.first) { _, _ ->
                dialogInfo.negativeButton.second?.invoke()
            }
            .show()
    }

    private fun hideEverything() {
        binding.converterLayout.visibility = View.GONE
        activity.hideBottomNavigationAndActionBar()
    }

    private fun createAlertDialog(dialogInfo: DialogInfo) =
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(dialogInfo.title)
            .setMessage(dialogInfo.message)
            .setPositiveButton(dialogInfo.positiveButton.first) { _, _ ->
                dialogInfo.positiveButton.second?.invoke()
            }

    private fun shutDown() {
        exitProcess(0)
    }

    private fun onItemChanged(position: Int) {
        (binding.dropdownMenu.currencies.adapter as CurrencyAdapter).notifyItemChanged(position)
    }

    private fun onItemRemoved(position: Int) {
        (binding.dropdownMenu.currencies.adapter as CurrencyAdapter).notifyItemRemoved(position)
    }

    private fun disableItemChangedAnimation() {
        (binding.dropdownMenu.currencies.itemAnimator as SimpleItemAnimator).supportsChangeAnimations =
            false
    }

    private fun onCurrencyChanged(currency: ExchangeableCurrency, from: Boolean) {
        fun updateCurrency(view: TextView) {
            view.run {
                if (text.isEmpty()) {
                    text = currency.getUiName(uiName)
                    setCompoundDrawablesWithIntrinsicBounds(currency.flag, null, null, null)
                }
            }
        }

        if (from) {
            updateCurrency(binding.fromCurrencyChooser.currency)
            fromCurrencyJob.cancel()
        } else {
            updateCurrency(binding.toCurrencyChooser.currency)
            toCurrencyJob.cancel()
        }
    }

    private fun onCurrenciesUpdated(currencies: List<ExchangeableCurrency>) {
        (binding.dropdownMenu.currencies.adapter as CurrencyAdapter).submitList(currencies)
    }

    private fun onSelectedCurrencyChanged(currency: ExchangeableCurrency, from: Boolean) {
        if (from) onSelectedCurrencyChanged(
            currency,
            binding.fromCurrencyChooser.currency,
            binding.fromCurrencyChooser.currencyDouble,
            Animations.FADE_IN,
            Animations.FADE_OUT
        ) else onSelectedCurrencyChanged(
            currency,
            binding.toCurrencyChooser.currency,
            binding.toCurrencyChooser.currencyDouble,
            Animations.FADE_IN,
            Animations.FADE_OUT
        )
    }

    private fun onSelectedCurrencyChanged(
        currency: ExchangeableCurrency,
        main: TextView,
        secondary: TextView,
        fadeIn: AlphaAnimation,
        fadeOut: AlphaAnimation
    ) {
        when {
            main.isVisible -> changeCurrencyState(currency, secondary, main, fadeIn, fadeOut)
            secondary.isVisible -> changeCurrencyState(currency, main, secondary, fadeIn, fadeOut)
        }
    }

    private fun changeCurrencyState(
        currency: ExchangeableCurrency,
        fadedIn: TextView,
        fadedOut: TextView,
        fadeIn: AlphaAnimation,
        fadeOut: AlphaAnimation
    ) {
        fadedIn.run {
            text = currency.getUiName(uiName)
            setCompoundDrawablesWithIntrinsicBounds(currency.flag, null, null, null)
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
        is Expanding -> onDropdownExpanding(state.dropdown)
        is Collapsing -> onDropdownCollapsing(state.dropdown)
        is Collapsed -> {
        } // When collapsed do nothing
    }

    private fun showResetButton(showButton: Boolean) {
        if (showButton) setAnimationListenerAndStartAnimation(Animations.FADE_IN, true)
        else setAnimationListenerAndStartAnimation(Animations.FADE_OUT, false)
    }

    private fun setAnimationListenerAndStartAnimation(
        animation: AlphaAnimation,
        fadinIn: Boolean
    ) {
        animation.setAnimationListener(
            getAnimationListener(
                binding.dropdownMenu.resetSearcher,
                fadinIn,
                Animations.FADE_IN,
                Animations.FADE_OUT
            )
        )
        binding.dropdownMenu.resetSearcher.startAnimation(animation)
    }

    private fun resetSearcher() {
        binding.dropdownMenu.currenciesSearcher.setText("")
    }

    private fun modifyDividerDrawable(topReached: Boolean) {
        val drawable = if (topReached) R.drawable.divider_top else R.drawable.divider_bottom
        binding.dropdownMenu.divider.setBackgroundResource(drawable)
    }

    private fun onSearcherStateChanged(state: SearcherState) = when (state) {
        is Focusing -> onSearcherFocused(state.dropdown)
        is Unfocusing -> onSearcherUnfocused(state.dropdown)
        else -> {
            // Do nothing
        }
    }

    private fun onSearcherFocused(dropdown: Dropdown) {
        setOnScreenTouched(dropdown, true)
    }

    private fun onScreenTouched(
        event: MotionEvent,
        dropdown: Dropdown,
        includeSearcher: Boolean
    ): Boolean {
        val touchPoint = Point(event.rawX.roundToInt(), event.rawY.roundToInt())

        val currencyLayout: ConstraintLayout
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
            binding.dropdownMenu.currenciesCard,
            touchPoint
        )

        return if (includeSearcher) {
            val currenciesSearcherTouched = isPointInsideViewBounds(
                binding.dropdownMenu.currenciesSearcher,
                touchPoint
            )
            viewModel.onScreenTouched(
                dropdown,
                currencyLayoutTouched,
                dropdownTitleTouched,
                currenciesCardTouched,
                currenciesSearcherTouched
            )
        } else {
            viewModel.onScreenTouched(
                dropdown,
                currencyLayoutTouched,
                dropdownTitleTouched,
                currenciesCardTouched
            )
        }
    }

    private fun onSearcherUnfocused(dropdown: Dropdown) {
        releaseFocus()
        hideKeyboard()
        if (viewModel.dropdownState.value !is Collapsed) restoreOriginalOnScreenTouched(dropdown)
        viewModel.onSearcherUnfocused(dropdown)
    }

    private fun releaseFocus() {
        binding.dropdownMenu.currenciesSearcher.clearFocus()
    }


    private fun hideKeyboard() {
        val imm = getInputMethodManager()
        imm.hideSoftInputFromWindow(activity.window.decorView.windowToken, 0)
    }

    private fun getInputMethodManager(): InputMethodManager {
        return requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    private fun restoreOriginalOnScreenTouched(dropdown: Dropdown) {
        setOnScreenTouched(dropdown, false)
    }

    private fun scrollCurrenciesToTop() {
        binding.dropdownMenu.currencies.scrollToPosition(0)
    }

    private fun animateFavorites(favorites: Boolean) {
        Animators.onFavoritesClicked(favorites)
    }

    private fun onSwappingStateChanged(state: SwappingState) {
        when (state) {
            Swapping -> onSwapping()
            SwappingInterrupted -> onSwappingInterrupted()
            Swapped -> onSwapped()
            SwappedWithInterruption -> onSwappedWithInterruption()
            ReverseSwapping -> onReverseSwapping()
            ReverseSwappingInterrupted -> onReverseSwappingInterrupted()
            ReverseSwapped -> onReverseSwapped()
            ReverseSwappedWithInterruption -> onReverseSwappedWithInterruption()
            None -> {
            } // Do nothing
        }
    }

    private fun onSwapping() {
        prepareForSwapping()
        startSwappingAnimation()
    }

    private fun onSwappingInterrupted() {
        prepareForReversingSwap()
        startSwappingAnimation()
    }

    private fun prepareForReversingSwap() {
        setAnimationListener(SwappingInterrupted)
    }

    private fun onSwapped() {
        swapDoubleWithReal(binding.fromCurrencyChooser, binding.toCurrencyDouble)
        swapDoubleWithReal(binding.toCurrencyChooser, binding.fromCurrencyDouble)
        viewModel.onSwappingCompleted()
    }

    private fun onSwappedWithInterruption() {
        onReverseSwapped()
    }

    private fun swapDoubleWithReal(binding: DropdownCurrencyChooserBinding, double: TextView) {
        // becase main and double are both invisible it doesn't matter which one we use
        binding.currency.text = double.text
        binding.currency.setCompoundDrawablesWithIntrinsicBounds(
            double.compoundDrawables[0], null, null, null
        )

        double.visibility = View.INVISIBLE
        binding.currency.visibility = View.VISIBLE
    }

    private fun onReverseSwapping() {
        prepareForReverseSwapping()
        startReverseSwappingAnimation()
    }

    private fun onReverseSwappingInterrupted() {
        prepareForReReversingSwap()
        startReverseSwappingAnimation()
    }

    private fun prepareForReReversingSwap() {
        setAnimationListener(ReverseSwappingInterrupted)
    }

    private fun onReverseSwapped() {
        swapDoubleWithReal(binding.fromCurrencyChooser, binding.fromCurrencyDouble)
        swapDoubleWithReal(binding.toCurrencyChooser, binding.toCurrencyDouble)
        viewModel.onReverseSwappingCompleted()
    }

    private fun onReverseSwappedWithInterruption() {
        onSwapped()
    }

    private fun prepareForReverseSwapping() {
        swapCurrentlyVisibleWithDouble(binding.fromCurrencyChooser, binding.toCurrencyDouble)
        swapCurrentlyVisibleWithDouble(binding.toCurrencyChooser, binding.fromCurrencyDouble)
        setAnimationListener(ReverseSwapping)
    }

    private fun startReverseSwappingAnimation() {
        Animators.animateCurrencyReverseSwapping(viewLifecycleOwner.lifecycleScope)
    }

    private fun prepareForSwapping() {
        swapCurrentlyVisibleWithDouble(binding.fromCurrencyChooser, binding.fromCurrencyDouble)
        swapCurrentlyVisibleWithDouble(binding.toCurrencyChooser, binding.toCurrencyDouble)
        setAnimationListener(Swapping)
    }

    private fun startSwappingAnimation() {
        Animators.animateCurrencySwapping(viewLifecycleOwner.lifecycleScope)
    }

    private fun swapCurrentlyVisibleWithDouble(
        binding: DropdownCurrencyChooserBinding,
        double: TextView
    ) {
        val data = if (binding.currency.isVisible) {
            Pair(
                binding.currency.text,
                binding.currency.compoundDrawables[0]
            )
        } else {
            Pair(
                binding.currencyDouble.text,
                binding.currencyDouble.compoundDrawables[0]
            )
        }

        double.run {
            text = data.first
            setCompoundDrawablesWithIntrinsicBounds(data.second, null, null, null)
        }


        if (binding.currency.isVisible) {
            binding.currency.visibility = View.INVISIBLE
        } else binding.currencyDouble.visibility = View.INVISIBLE
        double.visibility = View.VISIBLE
    }

    private fun setAnimationListener(state: SwappingState) {
        Animators.To.CURRENCY_DOUBLE.removeAllListeners()
        Animators.To.CURRENCY_DOUBLE.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {
            }

            @Suppress("NON_EXHAUSTIVE_WHEN")
            override fun onAnimationEnd(animation: Animator?) {
                when (state) {
                    Swapping -> viewModel.onSwapped()
                    ReverseSwapping -> viewModel.onReverseSwapped()
                    SwappingInterrupted -> viewModel.onSwappingInterrupted()
                    ReverseSwappingInterrupted -> viewModel.onReverseSwappingInterrupted()
                }
                Animators.To.CURRENCY_DOUBLE.removeListener(this)
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationRepeat(animation: Animator?) {
            }
        })
    }

    private fun onDropdownExpanding(dropdown: Dropdown) {
        expandDropdown(dropdown)
        setOnScreenTouched(dropdown, false)
        viewModel.onDropdownExpanded(dropdown)
    }

    private fun expandDropdown(dropdown: Dropdown) {
        modifyCurrenciesCardPosition(dropdown)
        Animators.startObjectAnimators(dropdown, viewLifecycleOwner.lifecycleScope)
    }

    private fun modifyCurrenciesCardPosition(dropdown: Dropdown) {
        val landscapeModificator = orientation.isLandscape && extendDropdownMenuInLandscape
        if (viewModel.shouldModifyCurrenciesCardPosition(dropdown, landscapeModificator)) {
            val viewId = if (dropdown == FROM) binding.fromCurrencyChooser.dropdownLayout.id
            else binding.toCurrencyChooser.dropdownLayout.id
            modifyCurrenciesCardPosition(viewId)
        }
    }

    private fun modifyCurrenciesCardPosition(viewId: Int) {
        ConstraintSet().apply {
            clone(binding.converterLayout)
            connect(
                binding.dropdownMenu.currenciesCard.id,
                ConstraintSet.TOP,
                viewId,
                ConstraintSet.BOTTOM
            )
            connect(
                binding.dropdownMenu.currenciesCard.id,
                ConstraintSet.BOTTOM,
                binding.converterLayout.id,
                ConstraintSet.BOTTOM
            )
            connect(
                binding.dropdownMenu.currenciesCard.id,
                ConstraintSet.START,
                viewId,
                ConstraintSet.START
            )
            connect(
                binding.dropdownMenu.currenciesCard.id,
                ConstraintSet.END,
                viewId,
                ConstraintSet.END
            )
            applyTo(binding.converterLayout)
        }
    }

    private fun setOnScreenTouched(dropdown: Dropdown, includeSearcher: Boolean) {
        activity.onScreenTouched = { event -> onScreenTouched(event, dropdown, includeSearcher) }
    }

    private fun removeOnScreenTouched() {
        activity.onScreenTouched = null
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

    private fun onDropdownCollapsing(dropdown: Dropdown) {
        collapseDropdown(dropdown)
        removeOnScreenTouched()
        viewModel.onDropdownCollapsed()
    }

    private fun collapseDropdown(dropdown: Dropdown) {
        Animators.reverseObjectAnimators(dropdown, viewLifecycleOwner.lifecycleScope)
    }
}