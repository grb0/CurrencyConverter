package ba.grbo.currencyconverter.ui.fragments

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import ba.grbo.currencyconverter.CurrencyConverterApplication
import ba.grbo.currencyconverter.R
import ba.grbo.currencyconverter.data.models.Currency
import ba.grbo.currencyconverter.data.models.preferences.FilterBy
import ba.grbo.currencyconverter.data.models.preferences.Language
import ba.grbo.currencyconverter.data.models.preferences.Theme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel

class SettingsFragment : PreferenceFragmentCompat() {
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Default + job)
    private lateinit var uiNamePreference: ListPreference
    private lateinit var filterByPreference: ListPreference
    private var currencyCode = ""
    private var currencyName = ""
    private var currencyBothCode = ""
    private var filterCode = ""
    private var filterName = ""

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
        observeLanguage()
        observeDarkMode()
        observeUiName()
        observeSearchBy()
    }

    override fun onResume() {
        super.onResume()
        initializeValues()
    }

    private fun observeLanguage() {
        findPreference<ListPreference>(getString(R.string.key_language))?.run {
            setOnPreferenceChangeListener { _, newValue ->
                if (value != newValue as String) {
                    onLanguageChanged(newValue)
                    true
                } else false
            }
        }
    }

    private fun observeDarkMode() {
        findPreference<ListPreference>(getString(R.string.key_theme))?.run {
            setOnPreferenceChangeListener { _, newValue ->
                (requireActivity().application as CurrencyConverterApplication).setTheme(
                    Theme.valueOf(newValue as String)
                )
            }
        }
    }

    private fun observeUiName() {
        findPreference<ListPreference>(getString(R.string.key_ui_name))?.run {
            uiNamePreference = this
            setOnPreferenceChangeListener { _, newValue ->
                newValue as String
                if (uiNamePreference.value != newValue) {
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

    private fun syncSearchBy(uiName: String) {
        when (uiName) {
            Currency.UiName.CODE.name -> {
                if (filterByPreference.value != filterCode) filterByPreference.value = filterCode
            }
            Currency.UiName.NAME.name -> {
                if (filterByPreference.value != filterName) filterByPreference.value = filterName
            }
        }
    }

    private fun synchCurrencyName(value: String) {
        when (value) {
            FilterBy.CODE.name -> {
                if (uiNamePreference.value == currencyName) {
                    uiNamePreference.value = currencyCode
                }
            }
            FilterBy.NAME.name -> {
                if (uiNamePreference.value == currencyCode) {
                    uiNamePreference.value = currencyName
                }
            }
            FilterBy.BOTH.name -> {
                if (uiNamePreference.value == currencyCode ||
                    uiNamePreference.value == currencyName
                ) {
                    uiNamePreference.value = currencyBothCode
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

    private fun onLanguageChanged(language: String) {
        (requireActivity().application as CurrencyConverterApplication).setLanguage(
            Language.valueOf(language)
        )
        recreateActivity()
    }

    private fun recreateActivity() {
        requireActivity().recreate()
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}