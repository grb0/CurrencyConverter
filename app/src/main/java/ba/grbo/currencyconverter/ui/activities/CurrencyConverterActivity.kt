package ba.grbo.currencyconverter.ui.activities

import android.graphics.Point
import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import ba.grbo.currencyconverter.R
import ba.grbo.currencyconverter.databinding.ActivityCurrencyConverterBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CurrencyConverterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCurrencyConverterBinding
    var onScreenTouched: ((event: MotionEvent) -> Pair<Boolean, Point>)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_CurrencyConverter)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_currency_converter)

        setUp()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) onScreenTouched?.invoke(ev)
        return super.dispatchTouchEvent(ev)
    }

    private fun setUp() {
        val fragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        val navController = (fragment as NavHostFragment).navController
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_converter,
                R.id.navigation_history,
                R.id.navigation_settings
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.bottomNavigation.setupWithNavController(navController)

        // Setting empty one, to avoid fragment reinstantiating
        binding.bottomNavigation.setOnNavigationItemReselectedListener { }
    }
}