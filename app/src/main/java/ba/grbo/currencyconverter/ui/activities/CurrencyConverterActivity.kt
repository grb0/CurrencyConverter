package ba.grbo.currencyconverter.ui.activities

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Point
import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import ba.grbo.currencyconverter.CurrencyConverterApplication
import ba.grbo.currencyconverter.R
import ba.grbo.currencyconverter.databinding.ActivityCurrencyConverterBinding
import ba.grbo.currencyconverter.util.getAnimator
import ba.grbo.currencyconverter.util.updateLocale
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class CurrencyConverterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCurrencyConverterBinding
    var onScreenTouched: ((event: MotionEvent) -> Triple<Boolean, Point, Boolean>)? = null
    private lateinit var iconColorStateList: ColorStateList
    private lateinit var textColorStateList: ColorStateList

    @Inject
    lateinit var language: Locale

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_CurrencyConverter)
        super.onCreate(savedInstanceState)
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

    fun getBottomNavigationAnimator() = binding.bottomNavigation.getAnimator(
        iconColorStateList,
        textColorStateList
    )
}