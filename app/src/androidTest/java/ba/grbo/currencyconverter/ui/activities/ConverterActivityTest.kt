package ba.grbo.currencyconverter.ui.activities

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import ba.grbo.currencyconverter.R
import ba.grbo.currencyconverter.util.OrientationChangeAction.Companion.changeOrientation
import ba.grbo.currencyconverter.util.isNotSelected
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@LargeTest
class ConverterActivityTest {
    private lateinit var activityScenario: ActivityScenario<CurrencyConverterActivity>

    companion object {
        const val PASSWAY_NUMBER = 40
        const val IDLE_TIME_MS = 200L
    }

    @Before
    fun init() {
        activityScenario = ActivityScenario.launch(CurrencyConverterActivity::class.java)
    }

    @After
    fun tearDown() {
        activityScenario.close()
    }

    // region BottomNavigation
    @Test
    fun bottomNavigation_initialState() {
        onView(withId(R.id.converter)).check(matches(isDisplayed()))
        onView(withId(R.id.converter)).check(matches(isSelected()))
        onView(withId(R.id.converter)).check(matches(isEnabled()))
        onView(withId(R.id.converter)).check(matches(isClickable()))
        onView(withId(R.id.converter)).check(matches(isFocusable()))

        onView(withId(R.id.history)).check(matches(isDisplayed()))
        onView(withId(R.id.history)).check(isNotSelected())
        onView(withId(R.id.history)).check(matches(isEnabled()))
        onView(withId(R.id.history)).check(matches(isClickable()))
        onView(withId(R.id.history)).check(matches(isFocusable()))

        onView(withId(R.id.settings)).check(matches(isDisplayed()))
        onView(withId(R.id.settings)).check(isNotSelected())
        onView(withId(R.id.settings)).check(matches(isEnabled()))
        onView(withId(R.id.settings)).check(matches(isClickable()))
        onView(withId(R.id.settings)).check(matches(isFocusable()))
    }

    @Test
    fun bottomNavigation_converterClicked() {
        onView(withId(R.id.converter)).perform(click())
        converterClicked()
    }

    @Test
    fun bottomNavigation_historyClicked() {
        onView(withId(R.id.history)).perform(click())
        historyClicked()
    }

    @Test
    fun bottomNavigation_settingsClicked() {
        onView(withId(R.id.settings)).perform(click())
        settingsClicked()
    }

    @Test
    fun bottomNavigation_initialStateDeviceRotated() {
        bottomNavigation_initialState()
        rotateDevice()
        bottomNavigation_initialState()
    }

    @Test
    fun bottomNavigation_converterClickedDeviceRotated() {
        bottomNavigation_converterClicked()
        rotateDevice()
        converterClicked()
    }

    @Test
    fun bottomNavigation_historyClickedDeviceRotated() {
        bottomNavigation_historyClicked()
        rotateDevice()
        historyClicked()
    }

    @Test
    fun bottomNavigation_settingsClickedDeviceRotated() {
        bottomNavigation_settingsClicked()
        rotateDevice()
        settingsClicked()
    }

    @Test
    fun bottomNavigation_initialStateDoubleRotation() {
        bottomNavigation_initialStateDeviceRotated()
        rotateDevice()
        bottomNavigation_initialState()
    }

    @Test
    fun bottomNavigation_converterClickedDoubleRotation() {
        bottomNavigation_converterClickedDeviceRotated()
        rotateDevice()
        converterClicked()
    }

    @Test
    fun bottomNavigation_historyClickedDoubleRotation() {
        bottomNavigation_historyClickedDeviceRotated()
        rotateDevice()
        historyClicked()
    }

    @Test
    fun bottomNavigation_settingsClickedDoubleRotation() {
        bottomNavigation_settingsClickedDeviceRotated()
        rotateDevice()
        settingsClicked()
    }

    @Test
    fun bottomNavigation_passwayThroughButtons() {
        onView(withId(R.id.converter)).perform(click())
        converterClicked()
        onView(withId(R.id.history)).perform(click())
        historyClicked()
        onView(withId(R.id.settings)).perform(click())
        settingsClicked()
    }

    @Test
    fun bottomNavigation_passwayThroughButtonsDeviceRotated() {
        bottomNavigation_passwayThroughButtons()
        rotateDevice()
        settingsClicked()
    }

    @Test
    fun bottomNavigation_passwayThroughButtonsDoubleRotation() {
        bottomNavigation_passwayThroughButtonsDeviceRotated()
        rotateDevice()
        settingsClicked()
    }

    @Test
    fun bottomNavigation_randomPasswayThroughButtons() {
        for (i in 1..PASSWAY_NUMBER) clickRandomBottomNavigationButtonAndCheckIfClicked()
    }

    @Test
    fun bottomNavigation_randomPasswayThroughButtonsWithDeviceRotations() {
        for (i in 1..PASSWAY_NUMBER) clickRandomBottomNavigationButtonAndCheckIfClickedWithRotation(
                ::converterClicked,
                ::historyClicked,
                ::settingsClicked
        )
    }

    @Test
    fun bottomNavigation_randomPasswayThroughButtonsWithRandomRotations() {
        for (i in 1..PASSWAY_NUMBER) clickRandomBottomNavigationButtonAndCheckIfClickedWithRandomRotations(
                ::converterClicked,
                ::historyClicked,
                ::settingsClicked
        )
    }

    @Test
    fun bottomNavigation_passwaysThroughButtonsStartingLandscape() {
        rotateDevice()
        bottomNavigation_randomPasswayThroughButtons()
    }

    @Test
    fun bottomNavigation_randomPasswaysWithRotationsStartingLandscape() {
        rotateDevice()
        bottomNavigation_randomPasswayThroughButtonsWithDeviceRotations()
    }

    @Test
    fun bottomNavigation_randomPasswaysWithRandomRotationsStartingLandscape() {
        rotateDevice()
        bottomNavigation_randomPasswayThroughButtonsWithRandomRotations()
    }

    private fun clickRandomBottomNavigationButtonAndCheckIfClickedWithRandomRotations(
            converter: () -> Unit = {},
            history: () -> Unit = {},
            settings: () -> Unit = {}
    ) {
        // 33% to rotate
        val shouldRotate = when ((1..3).random()) {
            1 -> true
            else -> false
        }

        fun rotateAndCheck(check: () -> Unit) {
            rotateDevice()
            check()
        }

        when ((1..3).random()) {
            1 -> {
                clickConverterAndCheckIfClicked()
                if (shouldRotate) rotateAndCheck(converter)
            }
            2 -> {
                clickHistoryAndCheckIfClicked()
                if (shouldRotate) rotateAndCheck(history)
            }
            3 -> {
                clickSettingsAndCheckIfClicked()
                if (shouldRotate) rotateAndCheck(settings)
            }
        }
    }

    private fun clickRandomBottomNavigationButtonAndCheckIfClickedWithRotation(
            converter: () -> Unit = {},
            history: () -> Unit = {},
            settings: () -> Unit = {}
    ) {
        when ((1..3).random()) {
            1 -> {
                clickConverterAndCheckIfClicked()
                rotateDevice()
                converter()
            }
            2 -> {
                clickHistoryAndCheckIfClicked()
                rotateDevice()
                history()
            }
            3 -> {
                clickSettingsAndCheckIfClicked()
                rotateDevice()
                settings()
            }
        }
    }

    private fun rotateDevice() {
        onView(isRoot()).perform(changeOrientation())
        // Give it time to render the layout before clicking again, otherwise it's flaky.
        Thread.sleep(IDLE_TIME_MS)
    }

    private fun clickRandomBottomNavigationButtonAndCheckIfClicked() {
        when ((1..3).random()) {
            1 -> clickConverterAndCheckIfClicked()
            2 -> clickHistoryAndCheckIfClicked()
            3 -> clickSettingsAndCheckIfClicked()
        }
    }

    private fun clickConverterAndCheckIfClicked() {
        onView(withId(R.id.converter)).perform(click())
        converterClicked()
    }

    private fun clickHistoryAndCheckIfClicked() {
        onView(withId(R.id.history)).perform(click())
        historyClicked()
    }

    private fun clickSettingsAndCheckIfClicked() {
        onView(withId(R.id.settings)).perform(click())
        settingsClicked()
    }

    private fun converterClicked() {
        onView(withId(R.id.converter)).check(matches(isSelected()))
        onView(withId(R.id.history)).check(isNotSelected())
        onView(withId(R.id.settings)).check(isNotSelected())
    }

    private fun historyClicked() {
        onView(withId(R.id.history)).check(matches(isSelected()))
        onView(withId(R.id.converter)).check(isNotSelected())
        onView(withId(R.id.settings)).check(isNotSelected())
    }

    private fun settingsClicked() {
        onView(withId(R.id.settings)).check(matches(isSelected()))
        onView(withId(R.id.converter)).check(isNotSelected())
        onView(withId(R.id.history)).check(isNotSelected())
    }
    // endregion
}