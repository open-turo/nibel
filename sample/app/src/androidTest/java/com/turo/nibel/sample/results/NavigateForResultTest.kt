package com.turo.nibel.sample.results

import android.content.Intent
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.commitNow
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.turo.nibel.sample.MainActivity
import nibel.runtime.ComposableEntry
import nibel.runtime.ComposeNavigationContext
import nibel.runtime.ComposeNavigationSpec
import nibel.runtime.FragmentEntry
import nibel.runtime.FragmentSpec
import nibel.runtime.FragmentTransactionSpec
import nibel.runtime.NibelNavigationController
import nibel.runtime.ResultEntry
import nibel.runtime.prepareFragment
import nibel.runtime.setNibelResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigateForResultTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private val a = TestResult("a")
    private val b = TestResult("b")

    @Before
    fun setUp() {
        TestHost.reset()
    }

    // region Directions (3.6)

    @Test
    fun composeToCompose() {
        showComposeCaller()
        openComposePicker()

        onUi { TestHost.pickerNavigator.setResultAndNavigateBack(a) }

        assertShown("Test caller")
        assertEquals(listOf(a), TestHost.callerResults)
    }

    @Test
    fun composeToFragment() {
        showComposeCaller()
        onUi {
            TestHost.callerNavigator.navigateForResult(
                ResultEntry(FragmentEntry(TestPlainPickerFragment())),
                TestHost.callerLauncher,
            )
        }
        assertViewShown("Test plain picker")

        onUi {
            TestHost.plainPicker!!.setNibelResult(a)
            composeRule.activity.onBackPressedDispatcher.onBackPressed()
        }

        assertShown("Test caller")
        assertEquals(listOf(a), TestHost.callerResults)
    }

    @Test
    fun fragmentToCompose() {
        showFragmentCaller("first")
        openFragmentPickerFrom("first")

        onUi { TestHost.pickerNavigator.setResultAndNavigateBack(a) }

        assertViewShown("Test fragment caller first")
        assertEquals(listOf(a), TestHost.fragmentCallerResults["first"])
    }

    @Test
    fun chainedRequestsReachTheirOwnCaller() {
        showComposeCaller()
        openComposePicker()
        onUi {
            TestHost.pickerNavigator.navigateForResult(
                ResultEntry(TestPickerScreenEntry.newInstance()),
                TestHost.pickerLauncher,
            )
        }
        idle()

        onUi { TestHost.pickerNavigator.setResultAndNavigateBack(a) }
        assertShown("Test picker")
        onUi { TestHost.pickerNavigator.setResultAndNavigateBack(b) }

        assertShown("Test caller")
        assertEquals(listOf(a), TestHost.pickerResults)
        assertEquals(listOf(b), TestHost.callerResults)
    }

    @Test
    fun regularNavigationInBetweenKeepsTheRequest() {
        showComposeCaller()
        openComposePicker()
        onUi { TestHost.pickerNavigator.navigateTo(FragmentEntry(TestPlainPickerFragment())) }
        assertViewShown("Test plain picker")
        pressBack()
        assertShown("Test picker")

        onUi { TestHost.pickerNavigator.setResultAndNavigateBack(b) }

        assertShown("Test caller")
        assertEquals(listOf(b), TestHost.callerResults)
    }

    @Test
    fun relaunchingALauncherReplacesItsRequest() {
        showComposeCaller()
        openComposePicker()
        onUi {
            TestHost.callerNavigator.navigateForResult(
                ResultEntry(FragmentEntry(TestPlainPickerFragment())),
                TestHost.callerLauncher,
            )
        }
        assertViewShown("Test plain picker")

        onUi { TestHost.plainPicker!!.setNibelResult(a) }
        pressBack()
        assertShown("Test picker")
        pressBack()

        assertShown("Test caller")
        assertEquals(listOf(a), TestHost.callerResults)
    }

    @Test
    fun resultIsDeliveredAfterActivityRecreation() {
        showComposeCaller()
        openComposePicker()

        composeRule.activityRule.scenario.recreate()
        assertShown("Test picker")
        onUi { TestHost.pickerNavigator.setResultAndNavigateBack(a) }

        assertShown("Test caller")
        assertEquals(listOf(a), TestHost.callerResults)
    }

    // endregion

    // region Cancellation (3.5)

    @Test
    fun explicitCancelDeliversNull() {
        showComposeCaller()
        openComposePicker()

        onUi { TestHost.pickerNavigator.cancelResultAndNavigateBack() }

        assertShown("Test caller")
        assertEquals(listOf(null), TestHost.callerResults)
    }

    @Test
    fun systemBackDeliversNull() {
        showComposeCaller()
        openComposePicker()

        pressBack()

        assertShown("Test caller")
        assertEquals(listOf(null), TestHost.callerResults)
    }

    @Test
    fun popBackStackDeliversNull() {
        showFragmentCaller("first")
        openFragmentPickerFrom("first")

        onUi { composeRule.activity.supportFragmentManager.popBackStack() }

        assertViewShown("Test fragment caller first")
        assertEquals(listOf(null), TestHost.fragmentCallerResults["first"])
    }

    @Test
    fun resultThenBackDeliversOnlyTheResult() {
        showComposeCaller()
        onUi {
            TestHost.callerNavigator.navigateForResult(
                ResultEntry(FragmentEntry(TestPlainPickerFragment())),
                TestHost.callerLauncher,
            )
        }
        assertViewShown("Test plain picker")

        onUi { TestHost.plainPicker!!.setNibelResult(a) }
        pressBack()

        assertShown("Test caller")
        assertEquals(listOf(a), TestHost.callerResults)
    }

    // endregion

    // region Errors and screens opened without a result (3.2, 3.3)

    @Test
    fun composeSpecThatDoesNotNavigateFails() {
        showComposeCaller()
        val notNavigating = object : ComposeNavigationSpec() {
            override fun ComposeNavigationContext.navigateTo(entry: ComposableEntry<*>) = Unit
        }

        val error = assertThrows(IllegalStateException::class.java) {
            onUi {
                TestHost.callerNavigator.navigateForResult(
                    ResultEntry(TestPickerScreenEntry.newInstance()),
                    TestHost.callerLauncher,
                    composeSpec = notNavigating,
                )
            }
        }

        assertTrue(error.message!!.contains("navigateForResult"))
        assertShown("Test caller")
    }

    @Test
    fun resultOfTheWrongTypeFails() {
        showComposeCaller()
        openComposePicker()

        val error = assertThrows(IllegalArgumentException::class.java) {
            onUi { TestHost.pickerNavigator.setResultAndNavigateBack(OtherResult("x")) }
        }

        assertTrue(error.message!!.contains(OtherResult::class.java.name))
        assertTrue(error.message!!.contains(TestResult::class.java.name))
        assertShown("Test picker")
        assertEquals(emptyList<TestResult?>(), TestHost.callerResults)
    }

    @Test
    fun resultOnAScreenOpenedWithoutAResultOnlyNavigatesBack() {
        showComposeCaller()
        onUi { TestHost.callerNavigator.navigateTo(TestPickerScreenEntry.newInstance()) }
        assertShown("Test picker")

        onUi { TestHost.pickerNavigator.setResultAndNavigateBack(a) }
        assertShown("Test caller")
        onUi { TestHost.callerNavigator.navigateTo(TestPickerScreenEntry.newInstance()) }
        assertShown("Test picker")
        onUi { TestHost.pickerNavigator.cancelResultAndNavigateBack() }

        assertShown("Test caller")
        assertEquals(emptyList<TestResult?>(), TestHost.callerResults)
    }

    // endregion

    // region Fragment launcher keys (3.4)

    @Test
    fun twoInstancesOfAFragmentCallerGetOnlyTheirOwnResult() {
        showFragmentCaller("first")
        openFragmentPickerFrom("first")
        onUi {
            TestHost.pickerNavigator.navigateTo(FragmentEntry(TestFragmentCaller.newInstance("second")))
        }
        assertViewShown("Test fragment caller second")
        pressBack()
        assertShown("Test fragment picker")

        onUi { TestHost.pickerNavigator.setResultAndNavigateBack(a) }

        assertViewShown("Test fragment caller first")
        assertEquals(listOf(a), TestHost.fragmentCallerResults["first"])
        assertEquals(null, TestHost.fragmentCallerResults["second"])
    }

    // endregion

    // region Callers that stay visible and failed navigation (review fixes)

    @Test
    fun backgroundingWithAnAddedPickerDoesNotCancel() {
        showComposeCaller()
        onUi {
            TestHost.callerNavigator.navigateForResult(
                ResultEntry(FragmentEntry(TestPlainPickerFragment())),
                TestHost.callerLauncher,
                fragmentSpec = FragmentTransactionSpec(replace = false),
            )
        }
        assertViewShown("Test plain picker")

        backgroundAndResume()
        assertEquals(emptyList<TestResult?>(), TestHost.callerResults)

        onUi { TestHost.plainPicker!!.setNibelResult(a) }
        pressBack()

        idle()
        assertEquals(listOf(a), TestHost.callerResults)
    }

    @Test
    fun backgroundingAFragmentCallerWithAnAddedPickerDoesNotCancel() {
        showFragmentCaller("first")
        onUi {
            val caller = TestHost.fragmentCallers.getValue("first")
            val picker = caller.launcher.prepareFragment(ResultEntry(FragmentEntry(TestPlainPickerFragment())))
            composeRule.activity.supportFragmentManager.commit {
                add(android.R.id.content, picker)
                addToBackStack(null)
            }
        }
        idle()

        backgroundAndResume()
        assertEquals(null, TestHost.fragmentCallerResults["first"])

        onUi { TestHost.plainPicker!!.setNibelResult(a) }

        idle()
        assertEquals(listOf(a), TestHost.fragmentCallerResults["first"])
    }

    @Test
    fun backgroundingWhileAReplacingPickerIsShownDoesNotCancel() {
        showComposeCaller()
        onUi {
            TestHost.callerNavigator.navigateForResult(
                ResultEntry(FragmentEntry(TestPlainPickerFragment())),
                TestHost.callerLauncher,
            )
        }
        assertViewShown("Test plain picker")

        backgroundAndResume()
        assertViewShown("Test plain picker")
        onUi { TestHost.plainPicker!!.setNibelResult(a) }
        pressBack()

        assertShown("Test caller")
        assertEquals(listOf(a), TestHost.callerResults)
    }

    @Test
    fun systemBackFromAFragmentPickerToAComposeCallerDeliversNull() {
        showComposeCaller()
        onUi {
            TestHost.callerNavigator.navigateForResult(
                ResultEntry(FragmentEntry(TestPlainPickerFragment())),
                TestHost.callerLauncher,
            )
        }
        assertViewShown("Test plain picker")

        pressBack()

        assertShown("Test caller")
        assertEquals(listOf(null), TestHost.callerResults)
    }

    @Test
    fun failedFragmentNavigationLeavesNoPendingRequest() {
        showComposeCaller()
        val unknownSpec = object : FragmentSpec<Unit> {
            override fun Unit.navigateTo(entry: FragmentEntry) = Unit
        }

        assertThrows(IllegalStateException::class.java) {
            onUi {
                TestHost.callerNavigator.navigateForResult(
                    ResultEntry(FragmentEntry(TestPlainPickerFragment())),
                    TestHost.callerLauncher,
                    fragmentSpec = unknownSpec,
                )
            }
        }
        backgroundAndResume()

        assertShown("Test caller")
        assertEquals(emptyList<TestResult?>(), TestHost.callerResults)
    }

    @Test
    fun screenOpenedInAnotherActivityDeliversNullOnReturn() {
        showComposeCaller()
        val newActivitySpec = object : FragmentSpec<Unit> {
            override fun Unit.navigateTo(entry: FragmentEntry) = Unit
        }
        val controller = TestHost.callerNavigator as NibelNavigationController
        val activityController = object : NibelNavigationController(
            controller.internalNavController,
            controller.fragmentManager,
            controller.onBackPressedDispatcher,
            controller.exploredEntries,
        ) {
            override fun navigateTo(entry: FragmentEntry, fragmentSpec: FragmentSpec<*>) {
                if (fragmentSpec === newActivitySpec) {
                    composeRule.activity.startActivity(Intent(composeRule.activity, MainActivity::class.java))
                } else {
                    super.navigateTo(entry, fragmentSpec)
                }
            }
        }
        onUi {
            activityController.navigateForResult(
                ResultEntry(FragmentEntry(TestPlainPickerFragment())),
                TestHost.callerLauncher,
                fragmentSpec = newActivitySpec,
            )
        }
        assertViewShown("Feature A | Zero Screen")

        pressBack()

        assertShown("Test caller")
        assertEquals(listOf(null), TestHost.callerResults)
    }

    @Test
    fun twoLaunchersWithTheSameNameFail() {
        val error = assertThrows(IllegalStateException::class.java) {
            onUi {
                composeRule.activity.supportFragmentManager.commitNow {
                    replace(android.R.id.content, TestDuplicateLaunchersFragment())
                }
            }
        }

        assertTrue(error.message!!.contains("A result launcher named 'default' is already registered"))
    }

    // endregion

    private fun showComposeCaller() {
        show(TestCallerScreenEntry.newInstance().fragment)
        assertShown("Test caller")
    }

    private fun showFragmentCaller(name: String) {
        show(TestFragmentCaller.newInstance(name))
        assertViewShown("Test fragment caller $name")
    }

    private fun openComposePicker() {
        onUi {
            TestHost.callerNavigator.navigateForResult(
                ResultEntry(TestPickerScreenEntry.newInstance()),
                TestHost.callerLauncher,
            )
        }
        assertShown("Test picker")
    }

    private fun openFragmentPickerFrom(callerName: String) {
        onUi {
            val caller = TestHost.fragmentCallers.getValue(callerName)
            val picker = caller.launcher.prepareFragment(ResultEntry(TestFragmentPickerScreenEntry.newInstance()))
            composeRule.activity.supportFragmentManager.commit {
                replace(android.R.id.content, picker)
                addToBackStack(null)
            }
        }
        assertShown("Test fragment picker")
    }

    private fun show(fragment: Fragment) {
        onUi {
            composeRule.activity.supportFragmentManager.commit {
                replace(android.R.id.content, fragment)
            }
        }
        idle()
    }

    private fun onUi(block: () -> Unit) = composeRule.runOnUiThread(block)

    private fun idle() = composeRule.waitForIdle()

    private fun backgroundAndResume() {
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        idle()
    }

    private fun assertShown(text: String) {
        idle()
        composeRule.onNodeWithText(text).assertExists()
    }

    private fun assertViewShown(text: String) {
        idle()
        onView(withText(text)).check(matches(isDisplayed()))
    }
}
