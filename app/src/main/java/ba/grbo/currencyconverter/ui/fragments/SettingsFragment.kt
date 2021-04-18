package ba.grbo.currencyconverter.ui.fragments

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import ba.grbo.currencyconverter.R
import ba.grbo.currencyconverter.data.models.CurrencyName
import ba.grbo.currencyconverter.data.models.FilterBy
import ba.grbo.currencyconverter.data.source.local.static.Countries
import kotlinx.coroutines.*

class SettingsFragment : PreferenceFragmentCompat() {
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Default + job)
    private lateinit var currencyNamePreference: ListPreference
    private lateinit var filterByPreference: ListPreference
    private var currencyCode = ""
    private var currencyName = ""
    private var currencyBothCode = ""
    private var filterCode = ""
    private var filterName = ""

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
        observeCurrencyName()
        observeSearchBy()
    }

    override fun onResume() {
        super.onResume()
        initializeValues()
    }

    private fun observeCurrencyName() {
        findPreference<ListPreference>(getString(R.string.key_currency_name))?.run {
            currencyNamePreference = this
            setOnPreferenceChangeListener { _, newValue ->
                newValue as String
                if (currencyNamePreference.value != newValue) {
                    redefineCountryNames(newValue)
                    syncSearchBy(newValue)
                    true
                } else false
            }
        }
    }

    private fun observeSearchBy() {
        findPreference<ListPreference>(getString(R.string.key_filter_by))?.run {
            filterByPreference = this
            setOnPreferenceChangeListener { _, newValue ->
                newValue as String
                if (filterByPreference.value != newValue) {
                    synchCurrencyName(newValue)
                    true
                } else false
            }
        }
    }

    private fun redefineCountryNames(currencyName: String) {
        scope.launch {
            Countries.value.forEach {
                while (isActive) it.currency.initializeName(
                        currencyName,
                        requireContext()
                )
            }
        }
    }

    private fun syncSearchBy(value: String) {
        when (value) {
            CurrencyName.CODE -> {
                if (filterByPreference.value != filterCode) filterByPreference.value = filterCode
            }
            CurrencyName.NAME -> {
                if (filterByPreference.value != filterName) filterByPreference.value = filterName
            }
        }
    }

    private fun synchCurrencyName(value: String) {
        when (value) {
            FilterBy.CODE -> {
                if (currencyNamePreference.value == currencyName) {
                    currencyNamePreference.value = currencyCode
                }
            }
            FilterBy.NAME -> {
                if (currencyNamePreference.value == currencyCode) {
                    currencyNamePreference.value = currencyName
                }
            }
            FilterBy.BOTH -> {
                if (currencyNamePreference.value == currencyCode ||
                        currencyNamePreference.value == currencyName
                ) {
                    currencyNamePreference.value = currencyBothCode
                }
            }
        }
    }

    private fun initializeValues() {
        currencyCode = getString(R.string.code_only_value)
        currencyName = getString(R.string.name_only_value)
        currencyBothCode = getString(R.string.both_code_and_name_value)
        filterCode = getString(R.string.code_value)
        filterName = getString(R.string.name_value)
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}