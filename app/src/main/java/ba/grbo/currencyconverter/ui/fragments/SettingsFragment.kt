package ba.grbo.currencyconverter.ui.fragments

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import ba.grbo.currencyconverter.R
import ba.grbo.currencyconverter.data.source.local.static.Countries
import kotlinx.coroutines.*

class SettingsFragment : PreferenceFragmentCompat() {
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
        observeCurrencyName()
    }

    private fun observeCurrencyName() {
        findPreference<ListPreference>(getString(R.string.key_currency_name))?.run {
            setOnPreferenceChangeListener { _, newValue ->
                scope.launch {
                    Countries.value.forEach {
                        while (isActive) it.currency.getCurrencyName(
                                newValue.toString(),
                                requireContext()
                        )
                    }
                }
                true
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}