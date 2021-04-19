package ba.grbo.currencyconverter.ui.activities

import android.graphics.Point
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import ba.grbo.currencyconverter.R
import ba.grbo.currencyconverter.databinding.ActivityCurrencyConverterBinding
import ba.grbo.currencyconverter.ui.viewmodels.CurrencyConverterViewModel
import ba.grbo.currencyconverter.util.collectWhenStarted
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CurrencyConverterActivity : AppCompatActivity() {
    private val viewModel: CurrencyConverterViewModel by viewModels()
    private lateinit var binding: ActivityCurrencyConverterBinding
    private lateinit var navController: NavController
    var onScreenTouched: ((event: MotionEvent) -> Pair<Boolean, Point>)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        collectFlows()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_currency_converter)
        setUpBottomNavigation()
        setUpNavController()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) onScreenTouched?.invoke(ev)
        return super.dispatchTouchEvent(ev)
    }

    private fun setUpBottomNavigation() {
        binding.bottomNavigation.setOnNavigationItemSelectedListener {
            viewModel.onBottomNavigationClicked(it.itemId)
        }

        // Set an empty lambda to avoid calling the listener above
        binding.bottomNavigation.setOnNavigationItemReselectedListener { }
    }

    private fun setUpNavController() {
        val fragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        navController = (fragment as NavHostFragment).navController.apply {
            addOnDestinationChangedListener { _, destination, _ ->
                viewModel.onDestinationChanged(destination.id)
            }
        }
    }

    private fun collectFlows() {
        collectWhenStarted(viewModel.toFragment, ::navigateToDestination)
        collectWhenStarted(viewModel.actionBarTitle, ::changeActionBarTitle)
    }

    private fun navigateToDestination(@IdRes destinationId: Int) {
        navController.popBackStack()
        navController.navigate(destinationId)
    }

    private fun changeActionBarTitle(@StringRes title: Int) {
        supportActionBar?.title = getString(title)
    }
}