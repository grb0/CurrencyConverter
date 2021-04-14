package ba.grbo.currencyconverter.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import ba.grbo.currencyconverter.data.models.CurrencyName
import ba.grbo.currencyconverter.databinding.FragmentConverterBinding
import ba.grbo.currencyconverter.ui.adapters.CountryAdapter
import ba.grbo.currencyconverter.ui.viewmodels.ConverterViewModel
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
        binding.fromCurrencyChooser.currencies.adapter = adapter
        viewModel.countries.onEach { adapter.submitList(it) }
                .launchIn(lifecycleScope) // just for testing, needs to be done differently

//        binding.dropdownCurrencyChooser.action.setOnClickListener {
//            if (!binding.dropdownCurrencyChooser.countryLayout.isSelected) {
//                binding.dropdownCurrencyChooser.action.setImageResource(R.drawable.ic_collapse)
//                binding.dropdownCurrencyChooser.action.imageTintList = ColorStateList.valueOf(
//                        ContextCompat.getColor(requireContext(), R.color.purple_500)
//                )
//                binding.dropdownCurrencyChooser.countryLayout.isSelected = true
//            } else {
//                binding.dropdownCurrencyChooser.action.setImageResource(R.drawable.ic_expand)
//                binding.dropdownCurrencyChooser.action.imageTintList = ColorStateList.valueOf(
//                        requireContext().getColorFromAttr(R.attr.colorControlNormal)
//                )
//                binding.dropdownCurrencyChooser.countryLayout.isSelected = false
//            }
//        }
//
//        binding.dropdownCurrencyChooser.countryLayout.setOnClickListener {
//            it as LinearLayout
//            if (it.isSelected) {
//                binding.dropdownCurrencyChooser.action.setImageResource(R.drawable.ic_expand)
//            } else {
//                binding.dropdownCurrencyChooser.action.setImageResource(R.drawable.ic_collapse)
//            }
//            it.isSelected = !it.isSelected
//        }

        return binding.root
    }
}