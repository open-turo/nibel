package nibel.tests.codegen

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Test data classes for nibel compilation tests.
 *
 * These classes provide the necessary argument and result types for testing
 * the KSP annotation processor's code generation capabilities.
 */

/**
 * Test argument class used across various compilation tests.
 * Represents navigation arguments that can be passed between screens.
 */
@Parcelize
data class TestArgs(val value: String) : Parcelable

/**
 * Test result class used for testing result-based navigation entries.
 * Represents data that can be returned from a result-based screen.
 */
@Parcelize
data class TestResult(val data: String) : Parcelable
