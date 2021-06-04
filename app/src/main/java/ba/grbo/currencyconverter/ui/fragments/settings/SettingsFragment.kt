package ba.grbo.currencyconverter.ui.fragments.settings

import android.animation.Animator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import ba.grbo.currencyconverter.CurrencyConverterApplication
import ba.grbo.currencyconverter.R
import ba.grbo.currencyconverter.data.source.CurrenciesRepository
import ba.grbo.currencyconverter.ui.viewmodels.SettingsViewModel
import ba.grbo.currencyconverter.util.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {
    private val viewModel: SettingsViewModel by viewModels()
    private val scope = CoroutineScope(Job())

    @Inject
    lateinit var repository: CurrenciesRepository

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
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
            { _, materialFadeThroughAnimator -> materialFadeThroughAnimator() }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    private fun setListeners() {
        observerChooser()
        observeLanguage()
        observeTheme()
    }

    private fun collectFlows() {
        viewModel.run {
            collectWhenStarted(toChooserFragment, false, ::navigateToChooserFragment)
            collectWhenStarted(languageChanged, true, ::onLanguageChanged)
            collectWhenStarted(themeChanged, true, ::onThemeChanged)
        }
    }

    private fun navigateToChooserFragment(@IdRes id: Int) {
        findNavController().navigate(id)
    }

    private fun observerChooser() {
        findPreference<Preference>(getString(R.string.key_chooser))?.run {
            setOnPreferenceClickListener { viewModel.onChooserClicked() }
        }
    }

    private fun observeLanguage() {
        findPreference<ListPreference>(getString(R.string.key_language))?.run {
            setOnPreferenceChangeListener { _, newValue ->
                viewModel.onLanguageChanged(value, newValue as String)
            }
        }
    }

    private fun observeTheme() {
        findPreference<ListPreference>(getString(R.string.key_theme))?.run {
            setOnPreferenceChangeListener { _, newValue ->
                viewModel.onThemeChanged(value, newValue as String)
            }
        }
    }

    private fun onLanguageChanged(language: String) {
        viewModel.viewModelScope.launch {
            syncCurrenciesWithLocale(language)
            recreateActivity()
        }
    }

    private suspend fun syncCurrenciesWithLocale(language: String) {
        withContext(Dispatchers.Default) {
            val modifiedContext =
                requireActivity().baseContext.updateLocale(Language.valueOf(language).toLocale())
            repository.syncExchangeableCurrenciesWithLocale(scope, modifiedContext)
        }
    }

    private fun onThemeChanged(theme: String) {
        (requireActivity().application as CurrencyConverterApplication).setTheme(
            Theme.valueOf(theme)
        )
    }

    private fun recreateActivity() {
        requireActivity().recreate()
    }
}