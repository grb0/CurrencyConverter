package ba.grbo.currencyconverter.ui.fragments

import android.content.res.ColorStateList
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
import ba.grbo.currencyconverter.data.models.CurrencyName
import ba.grbo.currencyconverter.databinding.FragmentConverterBinding
import ba.grbo.currencyconverter.ui.adapters.CountryAdapter
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel
import ba.grbo.currencyconverter.util.getColorFromAttr
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class ConverterFragment : Fragment() {
    private val viewModel: ConverterViewModel by viewModels()

    //    @Inject lateinit var countryAdapter: CountryAdapter
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val binding = FragmentConverterBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
        }
        val adapter = CountryAdapter(viewLifecycleOwner, CurrencyName.BOTH)
        binding.fromCurrencyChooser.currencies.addItemDecoration(DividerItemDecoration(
                requireContext(), DividerItemDecoration.VERTICAL
        ))
        binding.fromCurrencyChooser.currencies.adapter = adapter
        viewModel.countries.onEach { adapter.submitList(it) }
                .launchIn(lifecycleScope) // just for testing, needs to be done differently

        binding.fromCurrencyChooser.dropdownAction.setOnClickListener {
            if (!binding.fromCurrencyChooser.currencyLayout.isSelected) {
                binding.fromCurrencyChooser.dropdownAction.setImageResource(R.drawable.ic_collapse)
                binding.fromCurrencyChooser.dropdownAction.imageTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), R.color.purple_500)
                )
                binding.fromCurrencyChooser.currencyLayout.isSelected = true
            } else {
                binding.fromCurrencyChooser.dropdownAction.setImageResource(R.drawable.ic_expand)
                binding.fromCurrencyChooser.dropdownAction.imageTintList = ColorStateList.valueOf(
                        requireContext().getColorFromAttr(R.attr.colorControlNormal)
                )
                binding.fromCurrencyChooser.currencyLayout.isSelected = false
            }
        }

        binding.fromCurrencyChooser.currencyLayout.setOnClickListener {
            it as LinearLayout
            if (it.isSelected) {
                binding.fromCurrencyChooser.dropdownAction.setImageResource(R.drawable.ic_expand)
            } else {
                binding.fromCurrencyChooser.dropdownAction.setImageResource(R.drawable.ic_collapse)
            }
            it.isSelected = !it.isSelected
        }

        return binding.root
    }
}