package ba.grbo.currencyconverter.util

import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isSelected
import androidx.test.espresso.util.HumanReadables


fun isNotDisplayed(): ViewAssertion {
    return ViewAssertion { view, _ ->
        if (view != null && isDisplayed().matches(view)) {
            throw AssertionError(
                "View is present in the hierarchy and Displayed: " + HumanReadables.describe(view)
            )
        }
    }
}

fun isNotSelected(): ViewAssertion {
    return ViewAssertion { view, _ ->
        if (view != null && isSelected().matches(view)) {
            throw AssertionError("View Selected: " + HumanReadables.describe(view))
        }
    }
}