package ba.grbo.currencyconverter.ui.fragments

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import ba.grbo.currencyconverter.R

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
    }
}