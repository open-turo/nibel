package com.turo.nibel.sample.savedstate

import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.fragment.app.commit
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.turo.nibel.sample.MainActivity
import nibel.annotations.ImplementationType
import nibel.annotations.UiEntry
import nibel.runtime.NavigationController
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

object SavedStateHost {
    lateinit var navigator: NavigationController
}

@UiEntry(type = ImplementationType.Fragment)
@Composable
fun SavedStateRootScreen(navigator: NavigationController) {
    SideEffect { SavedStateHost.navigator = navigator }
    BasicText("Root")
}

/** Counts taps in saved state. */
@UiEntry(type = ImplementationType.Composable)
@Composable
fun SavedStateCounterScreen(navigator: NavigationController) {
    var count by rememberSaveable { mutableIntStateOf(0) }
    SideEffect { SavedStateHost.navigator = navigator }
    BasicText("Counter $count", Modifier.clickable { count++ })
}

@UiEntry(type = ImplementationType.Composable)
@Composable
fun SavedStateOtherScreen() {
    BasicText("Other screen")
}

/**
 * Compose screens keep their saved state when the activity is recreated, the first time in a
 * session, whether or not they are on top of the back stack.
 */
@RunWith(AndroidJUnit4::class)
class SavedStateAcrossRecreationTest {

    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    @Test
    fun topScreenKeepsItsState() {
        openCounterAndTapItThreeTimes()

        recreate()

        rule.onNodeWithText("Counter 3").assertExists()
    }

    @Test
    fun screenUnderAnotherScreenKeepsItsState() {
        openCounterAndTapItThreeTimes()
        navigateTo { SavedStateOtherScreenEntry.newInstance() }
        rule.onNodeWithText("Other screen").assertExists()

        recreate()
        pressBack()

        rule.onNodeWithText("Counter 3").assertExists()
    }

    @Test
    fun screenUnderAnotherInstanceOfItselfKeepsItsState() {
        openCounterAndTapItThreeTimes()
        navigateTo { SavedStateCounterScreenEntry.newInstance() }
        rule.onNodeWithText("Counter 0").assertExists()

        recreate()
        pressBack()

        rule.onNodeWithText("Counter 3").assertExists()
    }

    @Test
    fun screenUnderAnotherScreenKeepsItsStateWithoutRecreation() {
        openCounterAndTapItThreeTimes()
        navigateTo { SavedStateOtherScreenEntry.newInstance() }
        rule.onNodeWithText("Other screen").assertExists()

        pressBack()

        rule.onNodeWithText("Counter 3").assertExists()
    }

    private fun openCounterAndTapItThreeTimes() {
        rule.runOnUiThread {
            rule.activity.supportFragmentManager.commit {
                replace(android.R.id.content, SavedStateRootScreenEntry.newInstance().fragment)
            }
        }
        rule.waitForIdle()
        navigateTo { SavedStateCounterScreenEntry.newInstance() }
        repeat(3) { count -> rule.onNodeWithText("Counter $count").performClick() }
        rule.onNodeWithText("Counter 3").assertExists()
    }

    private fun navigateTo(entry: () -> nibel.runtime.Entry) {
        rule.runOnUiThread { SavedStateHost.navigator.navigateTo(entry()) }
        rule.waitForIdle()
    }

    private fun recreate() {
        rule.activityRule.scenario.recreate()
        rule.waitForIdle()
    }
}
