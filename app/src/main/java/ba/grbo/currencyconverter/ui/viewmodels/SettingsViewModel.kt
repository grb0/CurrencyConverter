package ba.grbo.currencyconverter.ui.viewmodels

import androidx.annotation.IdRes
import androidx.lifecycle.ViewModel
import ba.grbo.currencyconverter.R
import ba.grbo.currencyconverter.util.SingleSharedFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {
    private val _toChooserFragment = SingleSharedFlow<@IdRes Int>()
    val toChooserFragment: SharedFlow<Int>
        get() = _toChooserFragment

    private val _languageChanged = SingleSharedFlow<String>()
    val languageChanged: SharedFlow<String>
        get() = _languageChanged

    private val _themeChanged = SingleSharedFlow<String>()
    val themeChanged: SharedFlow<String>
        get() = _themeChanged

    fun onChooserClicked(): Boolean {
        _toChooserFragment.tryEmit(R.id.action_settingsFragment_to_chooserFragment)
        return true
    }

    fun onLanguageChanged(currentLanguage: String, newLanguage: String): Boolean {
        return if (currentLanguage != newLanguage) {
            _languageChanged.tryEmit(newLanguage)
            true
        } else false
    }

    fun onThemeChanged(currentTheme: String, newTheme: String): Boolean {
        return if (currentTheme != newTheme) {
            _themeChanged.tryEmit(newTheme)
            true
        } else false
    }
}