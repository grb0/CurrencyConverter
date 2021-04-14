package ba.grbo.currencyconverter.ui.fragments

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import ba.grbo.currencyconverter.R
import ba.grbo.currencyconverter.data.models.Code
import ba.grbo.currencyconverter.data.models.Currency
import ba.grbo.currencyconverter.data.models.CurrencyName
import ba.grbo.currencyconverter.databinding.FragmentConverterBinding
import ba.grbo.currencyconverter.ui.adapters.CountryAdapter
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel
import ba.grbo.currencyconverter.util.getColorFromAttr
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ConverterFragment : Fragment() {
    private val viewModel: ConverterViewModel by viewModels()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val currencyName = CurrencyName.BOTH
        val binding = FragmentConverterBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            this.currencyName = currencyName
            fromCurrency = Currency(R.string.currency_united_arab_emirates, Code.AED)
        }
        val adapter = CountryAdapter(viewLifecycleOwner, currencyName) {
            lifecycleScope.launch {
                delay(200)
                binding.fromCurrencyChooser.currency.text = it.currency.getCurrencyName(
                        currencyName, requireContext()
                )
                binding.fromCurrencyChooser.currency.setCompoundDrawablesWithIntrinsicBounds(
                        it.flag, 0, 0, 0
                )

                binding.fromCurrencyChooser.currenciesCard.visibility = View.GONE
                binding.fromCurrencyChooser.dropdownAction.setImageResource(R.drawable.ic_expand)
                binding.fromCurrencyChooser.dropdownAction.imageTintList = ColorStateList.valueOf(
                        requireContext().getColorFromAttr(R.attr.colorControlNormal)
                )
                binding.fromCurrencyChooser.currencyLayout.isSelected = false
                binding.converterLayout.setBackgroundColor(Color.WHITE)
                binding.fromCurrencyChooser.currencyLayout.setBackgroundResource(R.drawable.border_linear_layout)
                binding.fromCurrencyChooser.dropdownTitle.setBackgroundColor(Color.WHITE)
            }
        }
        binding.fromCurrencyChooser.currencies.addItemDecoration(DividerItemDecoration(
                requireContext(), DividerItemDecoration.VERTICAL
        ))
        binding.fromCurrencyChooser.currencies.adapter = adapter
        viewModel.countries.onEach { adapter.submitList(it) }
                .launchIn(lifecycleScope) // just for testing, needs to be done differently

        binding.fromCurrencyChooser.dropdownAction.setOnClickListener {
            if (!binding.fromCurrencyChooser.currencyLayout.isSelected) {
                binding.fromCurrencyChooser.currenciesCard.visibility = View.VISIBLE
                binding.fromCurrencyChooser.dropdownAction.setImageResource(R.drawable.ic_collapse)
                binding.fromCurrencyChooser.dropdownAction.imageTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), R.color.purple_500)
                )
                binding.fromCurrencyChooser.currencyLayout.isSelected = true
                binding.converterLayout.setBackgroundColor(Color.LTGRAY)
                binding.fromCurrencyChooser.currencyLayout.setBackgroundResource(R.drawable.border_linear_layout_inactive)
                binding.fromCurrencyChooser.dropdownTitle.setBackgroundColor(Color.LTGRAY)
            } else {
                binding.fromCurrencyChooser.currenciesCard.visibility = View.GONE
                binding.fromCurrencyChooser.dropdownAction.setImageResource(R.drawable.ic_expand)
                binding.fromCurrencyChooser.dropdownAction.imageTintList = ColorStateList.valueOf(
                        requireContext().getColorFromAttr(R.attr.colorControlNormal)
                )
                binding.fromCurrencyChooser.currencyLayout.isSelected = false
                binding.converterLayout.setBackgroundColor(Color.WHITE)
                binding.fromCurrencyChooser.currencyLayout.setBackgroundResource(R.drawable.border_linear_layout)
                binding.fromCurrencyChooser.dropdownTitle.setBackgroundColor(Color.WHITE)
            }
        }

        binding.fromCurrencyChooser.currencyLayout.setOnClickListener {
            it as LinearLayout
            if (it.isSelected) {
                binding.fromCurrencyChooser.currenciesCard.visibility = View.GONE
                binding.fromCurrencyChooser.dropdownAction.setImageResource(R.drawable.ic_expand)
            } else {
                binding.fromCurrencyChooser.currenciesCard.visibility = View.VISIBLE
                binding.fromCurrencyChooser.dropdownAction.setImageResource(R.drawable.ic_collapse)
            }
            it.isSelected = !it.isSelected
        }

        return binding.root
    }
}