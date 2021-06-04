package ba.grbo.currencyconverter.ui.fragments.settings

import android.animation.Animator
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import ba.grbo.currencyconverter.R
import ba.grbo.currencyconverter.data.models.domain.ExchangeableCurrency
import ba.grbo.currencyconverter.ui.viewmodels.ChooserViewModel
import ba.grbo.currencyconverter.util.*

class ChooserFragment : PreferenceFragmentCompat() {
    private val viewModel: ChooserViewModel by viewModels()
    private lateinit var sharedPreferences: SharedPreferences
    private var filterKey = ""
    private var filterCode = ""
    private var filterName = ""
    private lateinit var currency: ListPreference
    private var currencyKey = ""

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_chooser, rootKey)
        initVariables()
        setListeners()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        collectFlows()
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreateAnimator(transit: Int, enter: Boolean, nextAnim: Int): Animator {
        return getExitAndPopEnterMaterialSharedXAnimators(
            nextAnim,
            { getMaterialFadeThroughAnimator(view as ViewGroup, enter) },
            ::getEnterAndPopExitMaterialSharedXAnimators
        )
    }

    override fun onStart() {
        super.onStart()
        currency.value = sharedPreferences.getString(currencyKey, null)
    }

    private fun initVariables() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        currencyKey = getString(R.string.key_ui_name)
        currency = findPreference(currencyKey)!!
        initFilterValues()
    }

    private fun initFilterValues() {
        filterKey = getString(R.string.key_filter_by)
        filterCode = getString(R.string.code_value)
        filterName = getString(R.string.name_value)
    }

    private fun setListeners() {
        observeUiName()
        observeDropdownMenu()
    }

    private fun observeUiName() {
        currency.run {
            setOnPreferenceChangeListener { _, newValue ->
                viewModel.onUiNameChanged(value, newValue as String)
            }
        }
    }

    private fun observeDropdownMenu() {
        findPreference<Preference>(getString(R.string.key_dropdown_menu))?.run {
            setOnPreferenceClickListener { viewModel.onDropdownMenuClicked() }
        }
    }

    private fun syncFilterBy(uiName: String) {
        val filterByValue = sharedPreferences.getString(filterKey, null)
        sharedPreferences.edit().apply {
            putString(filterKey, filterCode)
            apply()
        }
        when (uiName) {
            ExchangeableCurrency.UiName.CODE.name -> {
                if (filterByValue != filterCode) modifyFilterBy(filterCode)
            }
            ExchangeableCurrency.UiName.NAME.name -> {
                if (filterByValue != filterName) modifyFilterBy(filterName)
            }
        }
    }

    private fun collectFlows() {
        viewModel.run {
            collectWhenStarted(toDropdownMenuFragment, false, ::navigateToDropdownMenuFragment)
            collectWhenStarted(uiNameChanged, true, ::onUiNameChanged)
        }
    }

    private fun onUiNameChanged(uiName: String) {
        syncFilterBy(uiName)
    }

    private fun navigateToDropdownMenuFragment(@IdRes id: Int) {
        findNavController().navigate(id)
    }

    private fun modifyFilterBy(value: String) {
        sharedPreferences.putString(filterKey, value)
    }
}