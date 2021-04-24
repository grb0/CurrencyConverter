package ba.grbo.currencyconverter.ui.activities

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Point
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import ba.grbo.currencyconverter.CurrencyConverterApplication
import ba.grbo.currencyconverter.R
import ba.grbo.currencyconverter.databinding.ActivityCurrencyConverterBinding
import ba.grbo.currencyconverter.ui.viewmodels.CurrencyConverterViewModel
import ba.grbo.currencyconverter.util.getAnimator
import ba.grbo.currencyconverter.util.updateLocale
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class CurrencyConverterActivity : AppCompatActivity() {
    private val viewModel: CurrencyConverterViewModel by viewModels()
    private lateinit var binding: ActivityCurrencyConverterBinding
    private lateinit var navController: NavController
    var onScreenTouched: ((event: MotionEvent) -> Triple<Boolean, Point, Boolean>)? = null
    private lateinit var iconColorStateList: ColorStateList
    private lateinit var textColorStateList: ColorStateList

    @Inject
    lateinit var language: Locale

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_CurrencyConverter)
        super.onCreate(savedInstanceState)
        collectFlows()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_currency_converter)
        setUp()
    }

    override fun attachBaseContext(newBase: Context?) {
        val language = (newBase?.applicationContext as CurrencyConverterApplication).language
        super.attachBaseContext(newBase.updateLocale(language))
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        return if (ev.action == MotionEvent.ACTION_DOWN) {
            val shouldConsume = onScreenTouched?.invoke(ev)?.third
            if (shouldConsume == true) true
            else super.dispatchTouchEvent(ev)
        } else super.dispatchTouchEvent(ev)
    }

    private fun initColorStates() {
        iconColorStateList = binding.bottomNavigation.itemIconTintList!!
        textColorStateList = binding.bottomNavigation.itemTextColor!!
    }

    private fun setUp() {
        initColorStates()

        val fragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        navController = (fragment as NavHostFragment).navController.apply {
            addOnDestinationChangedListener { _, destination, _ ->
                viewModel.onDestinationChanged(destination.id)
            }
        }

        binding.bottomNavigation.run {
            setOnNavigationItemSelectedListener {
                // Since ids are same, we can directly navigate
                onNavigationItemSelected(it.itemId)
            }

            // Setting empty one, to avoid fragment reinstantiating
            setOnNavigationItemReselectedListener {}
        }
    }

    override fun onBackPressed() {
        // Intercept Back, so we go to start destination
        if (navController.currentDestination?.id != R.id.navigation_converter) returnToConverter()
        else super.onBackPressed()
    }

    private fun returnToConverter() {
        binding.bottomNavigation.selectedItemId = R.id.navigation_converter
        onNavigationItemSelected(R.id.navigation_converter)
    }

    private fun onNavigationItemSelected(@IdRes itemId: Int): Boolean {
        navController.popBackStack() // Remove current
        navController.navigate(itemId)
        return true
    }

    private fun collectFlows() {
        viewModel.actionBarTitleId
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach { setActionBarTitle(it) }
            .launchIn(lifecycleScope)
    }

    private fun setActionBarTitle(@StringRes titleId: Int) {
        supportActionBar?.title = getString(titleId)
    }

    fun getBottomNavigationAnimator() = binding.bottomNavigation.getAnimator(
        iconColorStateList,
        textColorStateList
    )
}