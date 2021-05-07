package ba.grbo.currencyconverter.ui.activities

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.ColorUtils
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import ba.grbo.currencyconverter.R
import ba.grbo.currencyconverter.data.models.domain.ExchangeableCurrency
import ba.grbo.currencyconverter.databinding.ActivityCurrencyConverterBinding
import ba.grbo.currencyconverter.ui.viewmodels.CurrencyConverterViewModel
import ba.grbo.currencyconverter.util.*
import ba.grbo.currencyconverter.util.Constants.BACKGROUND_COLOR
import ba.grbo.currencyconverter.util.Constants.ITEM_ICON_TINT_LIST
import ba.grbo.currencyconverter.util.Constants.ITEM_TEXT_COLOR
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class CurrencyConverterActivity : AppCompatActivity() {
    private val viewModel: CurrencyConverterViewModel by viewModels()
    private lateinit var binding: ActivityCurrencyConverterBinding
    private lateinit var navController: NavController
    var onScreenTouched: ((event: MotionEvent) -> Boolean)? = null

    @Suppress("PropertyName")
    @Inject
    lateinit var Colors: Colors

    @Inject // Injecting so we force first initialization upon activity creation
    lateinit var currencies: MutableStateFlow<List<ExchangeableCurrency>>

    override fun onCreate(savedInstanceState: Bundle?) {
        preSuperOnCreateSetup()
        super.onCreate(savedInstanceState)
        postSuperOnCreateSetup()


        // val symbol = Char(36)

        /*val locale = Locale("en", "IN")
        val decimalFormat: DecimalFormat =
            DecimalFormat.getCurrencyInstance(locale) as DecimalFormat
        val dfs: DecimalFormatSymbols = DecimalFormatSymbols.getInstance(locale)
        dfs.currencySymbol = "\u20B9"
        decimalFormat.setDecimalFormatSymbols(dfs)
        System.out.println(decimalFormat.format(12324.13))*/

        // var name = resources.getResourceEntryName(R.drawable.ic_flag_afghanistan)
        // Timber.i("name: $name")
        // val aa = resources.getIdentifier(name, "drawable", packageName)
        // Timber.i("a: ${R.drawable.ic_flag_afghanistan}")
        // Timber.i("b: $aa")
        // name = resources.getResourceEntryName(R.string.currency_afghanistan)
        // Timber.i("name: $name")


        // Timber.i("date: ${Date().time}")

        // val myDate = "13.03.2003 00:00:00"
        // val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", getLocale())

        /*val myDate = "2021-05-05"
        val sdf = SimpleDateFormat("yyyy-MM-dd", getLocale())
        val date = sdf.parse(myDate)!!
        val timeInMillis = date.time

        Timber.i("mil: $timeInMillis")

        val curernt = getCurrentDate(date)
        Timber.i("sdda: $curernt")*/

        // val calendar = Calendar.getInstance()
        // val day = calendar.get(Calendar.DATE)
        // val month = calendar.get(Calendar.MONTH)
        // val year = calendar.get(Calendar.YEAR)
        // val hour = calendar.get(Calendar.HOUR)
        // val minute = calendar.get(Calendar.MINUTE)
        //
        // val localDate = LocalDateTime.of(
        //     year,
        //     Month.of(month + 1),
        //     day,
        //     hour,
        //     minute
        // )
        //
        // val date = "$day $month $year $hour:$minute"
        // Timber.i("date: $date")
        // Timber.i("localDate: $localDate")
        // Timber.i("currenData: ${getCurrentDate()}")
    }

    fun getCurrentDate(date: Date): String {

        val a = SimpleDateFormat.getDateTimeInstance(1, 3, getLocale())
        return a.format(date)
        // val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("bs"))
        // return sdf.format(Date())

    }


    override fun attachBaseContext(newBase: Context) {
        val newContext = newBase.updateLocale()
        applyOverrideConfiguration(newContext.resources.configuration)
        super.attachBaseContext(newContext)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        return if (ev.action == MotionEvent.ACTION_DOWN) {
            val shouldConsume = onScreenTouched?.invoke(ev)
            if (shouldConsume == true) true
            else super.dispatchTouchEvent(ev)
        } else super.dispatchTouchEvent(ev)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp()
    }

    private fun preSuperOnCreateSetup() {
        setTheme(R.style.Theme_CurrencyConverter)
    }

    private fun postSuperOnCreateSetup() {
        initBinding()
        initNavController()
        setupActionBarWithNavController()
        setListeners()
        collectFlows()
    }

    private fun initBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_currency_converter)
    }

    private fun initNavController() {
        val fragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        navController = (fragment as NavHostFragment).navController
    }

    private fun setupActionBarWithNavController() {
        setupActionBarWithNavController(
            navController,
            AppBarConfiguration(
                setOf(
                    R.id.converterFragment,
                    R.id.popularFragment,
                    R.id.historyFragment,
                    R.id.settingsFragment
                )
            )
        )
    }

    private fun setListeners() {
        addBottomNavigatonListeners()
        addNavControllerListeners()
    }

    private fun addBottomNavigatonListeners() {
        binding.bottomNavigation.run {
            setOnNavigationItemSelectedListener {
                viewModel.onNavigationItemSelected(it.itemId)
            }

            // Setting one to avoid calling OnNavigationItemSelectedListener we set up above
            setOnNavigationItemReselectedListener {
                viewModel.onNavigationItemReselected(it.itemId)
            }
        }
    }

    private fun addNavControllerListeners() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            viewModel.onDestinationChanged(
                destination.id,
                destination.label.toString()
            )
        }
    }

    // Intercept back button, so we go can implement custom behavior
    override fun onBackPressed() {
        viewModel.onBackPressed()
    }

    private fun collectFlows() {
        viewModel.run {
            destinationId.collect(::onDestinationIdChanged)
            actionBarTitle.collect(::setActionBarTitle)
            selectedItemId.collect(::onSelectedItemIdChanged)
            pressBack.collect { pressBack() }
            exitApp.collect { exitApp() }
        }
    }

    private fun onDestinationIdChanged(@IdRes destinationId: Int) {
        navController.popBackStack() // Remove current destination
        navController.navigate(destinationId)
    }

    private fun setActionBarTitle(title: String) {
        supportActionBar?.title = title
    }

    private fun onSelectedItemIdChanged(@IdRes itemId: Int) {
        // Triggers OnNavigationItemSelectedListener
        binding.bottomNavigation.selectedItemId = itemId
    }

    private fun pressBack() {
        super.onBackPressed()
    }

    private fun exitApp() {
        finish()
    }

    private fun <T> Flow<T>.collect(onEach: (T) -> Unit) {
        flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach { onEach(it) }
            .launchIn(lifecycleScope)
    }

    @SuppressLint("Recycle")
    private fun BottomNavigationView.getAnimator(): ObjectAnimator {
        val alphaSixty = (0.6f * 255).roundToInt()
        val alphaTwenty = (0.2f * 255).roundToInt()
        val startColorUnchecked = ColorUtils.setAlphaComponent(Colors.ON_PRIMARY, alphaSixty)
        val endColorChecked = ColorUtils.setAlphaComponent(Colors.BLACK, alphaSixty)
        val endColorUnchecked = ColorUtils.setAlphaComponent(Colors.BLACK, alphaTwenty)
        val backgroundColorProperty = getArgbPropertyValueHolderForProperty(
            BACKGROUND_COLOR,
            Colors.PRIMARY,
            Colors.DIVIDER
        )
        val itemIconTintListProperty = getPropertyValueHolderForPropertyWithColorStateList(
            ITEM_ICON_TINT_LIST,
            Colors.ON_PRIMARY,
            startColorUnchecked,
            endColorChecked,
            endColorUnchecked
        )
        val itemTextColorProperty = getPropertyValueHolderForPropertyWithColorStateList(
            ITEM_TEXT_COLOR,
            Colors.ON_PRIMARY,
            startColorUnchecked,
            endColorChecked,
            endColorUnchecked
        )

        return ObjectAnimator.ofPropertyValuesHolder(
            this,
            backgroundColorProperty,
            itemIconTintListProperty,
            itemTextColorProperty
        ).setUp(resources)
    }

    private fun getPropertyValueHolderForPropertyWithColorStateList(
        property: String,
        startColorChecked: Int,
        startColorUnchecked: Int,
        endColorChecked: Int,
        endColorUnchecked: Int
    ): PropertyValuesHolder = PropertyValuesHolder.ofObject(
        property,
        { fraction, _, _ ->
            val evaluator = ArgbEvaluator()

            val colorChecked = evaluator.evaluate(
                fraction,
                startColorChecked,
                endColorChecked,
            ) as Int

            val colorUnchecked = evaluator.evaluate(
                fraction,
                startColorUnchecked,
                endColorUnchecked
            ) as Int

            getCheckedUncheckedColorStateList(colorChecked, colorUnchecked)
        },
        getCheckedUncheckedColorStateList(startColorChecked, startColorUnchecked),
        getCheckedUncheckedColorStateList(endColorChecked, endColorUnchecked)
    )

    private fun getCheckedUncheckedColorStateList(
        colorChecked: Int,
        colorUnchecked: Int
    ) = ColorStateList(
        arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_checked)
        ),
        intArrayOf(colorChecked, colorUnchecked)
    )

    fun getBottomNavigationAnimator() = binding.bottomNavigation.getAnimator()
}