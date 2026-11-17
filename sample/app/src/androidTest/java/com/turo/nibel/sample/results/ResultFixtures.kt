package com.turo.nibel.sample.results

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.fragment.app.Fragment
import kotlinx.parcelize.Parcelize
import nibel.annotations.ImplementationType
import nibel.annotations.UiEntry
import nibel.runtime.NavigationController
import nibel.runtime.ResultLauncher
import nibel.runtime.registerForNibelResult
import nibel.runtime.rememberResultLauncher

@Parcelize
data class TestResult(val value: String) : Parcelable

@Parcelize
data class OtherResult(val value: String) : Parcelable

/**
 * Exposes the navigators and launchers of the screens below to tests, and records every result
 * their launchers receive.
 */
object TestHost {
    lateinit var callerNavigator: NavigationController
    lateinit var callerLauncher: ResultLauncher<TestResult>
    lateinit var pickerNavigator: NavigationController
    lateinit var pickerLauncher: ResultLauncher<TestResult>
    var plainPicker: TestPlainPickerFragment? = null
    val fragmentCallers = mutableMapOf<String, TestFragmentCaller>()

    val callerResults = mutableListOf<TestResult?>()
    val pickerResults = mutableListOf<TestResult?>()
    val fragmentCallerResults = mutableMapOf<String, MutableList<TestResult?>>()

    fun reset() {
        plainPicker = null
        fragmentCallers.clear()
        callerResults.clear()
        pickerResults.clear()
        fragmentCallerResults.clear()
    }
}

/** Compose caller, hosted in a fragment. */
@UiEntry(type = ImplementationType.Fragment)
@Composable
fun TestCallerScreen(navigator: NavigationController) {
    val launcher = rememberResultLauncher<TestResult> { TestHost.callerResults += it }
    SideEffect {
        TestHost.callerNavigator = navigator
        TestHost.callerLauncher = launcher
    }
    Text("Test caller")
}

/** Compose picker, which can itself open screens for a result. */
@UiEntry(type = ImplementationType.Composable)
@Composable
fun TestPickerScreen(navigator: NavigationController) {
    val launcher = rememberResultLauncher<TestResult> { TestHost.pickerResults += it }
    SideEffect {
        TestHost.pickerNavigator = navigator
        TestHost.pickerLauncher = launcher
    }
    Text("Test picker")
}

/** Compose picker implemented as a fragment, for fragment callers. */
@UiEntry(type = ImplementationType.Fragment)
@Composable
fun TestFragmentPickerScreen(navigator: NavigationController) {
    SideEffect { TestHost.pickerNavigator = navigator }
    Text("Test fragment picker")
}

/** Plain fragment picker that knows nothing about Nibel navigation. */
class TestPlainPickerFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TestHost.plainPicker = this
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        TextView(requireContext()).apply { text = "Test plain picker" }
}

/** Fragment caller. Several instances can coexist, told apart by [name]. */
class TestFragmentCaller : Fragment() {

    lateinit var launcher: ResultLauncher<TestResult>

    private val name: String get() = requireArguments().getString(NAME)!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        launcher = registerForNibelResult { TestHost.fragmentCallerResults.getOrPut(name) { mutableListOf() } += it }
        TestHost.fragmentCallers[name] = this
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        TextView(requireContext()).apply { text = "Test fragment caller $name" }

    companion object {
        private const val NAME = "name"

        fun newInstance(name: String) = TestFragmentCaller().apply {
            arguments = Bundle().apply { putString(NAME, name) }
        }
    }
}

/** Registers two launchers with the default name, which isn't allowed. */
class TestDuplicateLaunchersFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerForNibelResult<TestResult> {}
        registerForNibelResult<OtherResult> {}
    }
}
