package ba.grbo.currencyconverter.ui.fragments

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Point
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.AttrRes
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import ba.grbo.currencyconverter.R
import ba.grbo.currencyconverter.data.models.Country
import ba.grbo.currencyconverter.data.models.CurrencyName
import ba.grbo.currencyconverter.databinding.FragmentConverterBinding
import ba.grbo.currencyconverter.ui.activities.ConverterActivity
import ba.grbo.currencyconverter.ui.adapters.CountryAdapter
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.DropdownState
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel.DropdownState.*
import ba.grbo.currencyconverter.util.collectWhenStarted
import ba.grbo.currencyconverter.util.getColorFromAttribute
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.roundToInt

@AndroidEntryPoint
class ConverterFragment : Fragment() {
    private val viewModel: ConverterViewModel by viewModels()
    private lateinit var binding: FragmentConverterBinding
    private lateinit var currencyName: CurrencyName

    object Colors {
        var WHITE = 0
        var LIGHT_GRAY = 0
        var PURPLE_500 = 0
        var PURPLE_700 = 0
        var BORDER = 0
        var CONTROL_NORMAL = 0
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        currencyName = CurrencyName.BOTH
        binding = FragmentConverterBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
        }
        initializeColors()
        setOnClickListeners()
        setUpRecyclerViewAndViewModel()
        return binding.root
    }

    private fun initializeColors() {
        Colors.WHITE = getColorFromResource(R.color.white)
        Colors.LIGHT_GRAY = getColorFromResource(R.color.light_gray)
        Colors.PURPLE_500 = getColorFromResource(R.color.purple_500)
        Colors.PURPLE_700 = getColorFromResource(R.color.purple_700)
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

    private fun setOnClickListeners() {
        binding.fromCurrencyChooser.run {
            dropdownAction.setOnClickListener {
                viewModel.onExpandClicked()
            }
            currencyLayout.setOnClickListener {
                viewModel.onDropdownClicked()
            }

            dropdownTitle.setOnClickListener {
                viewModel.onTitleClicked()
            }
        }
    }

    private fun setUpRecyclerViewAndViewModel() {
        viewModel.collectFlows(binding.fromCurrencyChooser.currencies.setUp())
    }

    private fun RecyclerView.setUp(): CountryAdapter {
        val adapter = CountryAdapter(viewLifecycleOwner, currencyName) {
            viewModel.onCurrencyClicked(it)
        }
        this.adapter = adapter

        val verticalDivider = DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
        )
        addItemDecoration(verticalDivider)

        // Only in portrait mode
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            val viewHolder = adapter.createViewHolder(
                    binding.fromCurrencyChooser.currencies,
                    0
            )
            val verticalDividersHeight = ((verticalDivider.drawable?.intrinsicHeight ?: 3) * 4)
            val viewHolderHeight = viewHolder.itemView.layoutParams.height * 4
            val height = verticalDividersHeight + viewHolderHeight
            binding.fromCurrencyChooser.currenciesFastScroller.layoutParams.height = height

            // Let it not be in vain :)
            recycledViewPool.putRecycledView(viewHolder)
        }
        return adapter
    }

    private fun ConverterViewModel.collectFlows(adapter: CountryAdapter) {
        collectWhenStarted(fromSelectedCurrency, ::onSelectedCurrencyChanged)
        collectWhenStarted(fromCurrencyDropdownState, ::onDropdownStateChanged)
        collectWhenStarted(countries, adapter::submitList)
    }

    private fun onSelectedCurrencyChanged(country: Country) {
        binding.fromCurrencyChooser.currency.run {
            text = country.currency.getCurrencyName(currencyName, requireContext())
            setCompoundDrawablesWithIntrinsicBounds(country.flag, 0, 0, 0)
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

    private fun expandDropdown() {
        binding.converterLayout.setBackgroundColor(Colors.LIGHT_GRAY)
        binding.fromCurrencyChooser.run {
            currenciesCard.visibility = View.VISIBLE
            dropdownAction.run {
                setImageResource(R.drawable.ic_collapse)
                imageTintList = ColorStateList.valueOf(Colors.PURPLE_700)
            }
            currencyLayout.isSelected = true
            dropdownTitle.run {
                setBackgroundColor(Colors.LIGHT_GRAY)
                setTextColor(Colors.PURPLE_700)
                setTypeface(typeface, Typeface.BOLD)
            }
        }
        viewModel.onDropdownExpanded()
    }

    private fun setOnScreenTouched(shouldSet: Boolean) {
        (requireActivity() as ConverterActivity).onScreenTouched = if (!shouldSet) null
        else { event -> getOnScreenTouched(event) }
    }

    private fun getOnScreenTouched(event: MotionEvent) {
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
        viewModel.onScreenTouched(
                dropdownActionTouched,
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

    private fun collapseDropdown() {
        binding.converterLayout.setBackgroundColor(Colors.WHITE)
        binding.fromCurrencyChooser.run {
            currenciesCard.visibility = View.GONE
            dropdownAction.run {
                setImageResource(R.drawable.ic_expand)
                imageTintList = ColorStateList.valueOf(Colors.CONTROL_NORMAL)
            }
            currencyLayout.isSelected = false
            dropdownTitle.run {
                setBackgroundColor(Colors.WHITE)
                setTextColor(Colors.BORDER)
                setTypeface(Typeface.create(typeface, Typeface.NORMAL), Typeface.NORMAL)
            }
        }
        viewModel.onDropdownCollapsed()
    }
}