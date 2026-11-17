package com.turo.nibel.sample.results

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.turo.nibel.sample.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Drives the sample's results demo through its UI.
 */
@RunWith(AndroidJUnit4::class)
class SampleResultsTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun resultFromAnotherFeatureModule() {
        onView(withText("Results Demo (type.fragment, internal)")).perform(click())
        composeRule.onNodeWithText("Last size: none").assertExists()

        composeRule.onNodeWithText("Pick size (feature-B, external)").performClick()
        composeRule.onNodeWithText("Size Picker (feature-B, external)").assertExists()
        composeRule.onNodeWithText("Pick size M").performClick()
        composeRule.onNodeWithText("Last size: M").assertExists()

        composeRule.onNodeWithText("Pick size (feature-B, external)").performClick()
        composeRule.onNodeWithText("Size Picker (feature-B, external)").assertExists()
        pressBack()
        composeRule.onNodeWithText("Last size: cancelled").assertExists()
    }
}
