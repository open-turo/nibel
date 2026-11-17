package com.turo.nibel.sample.featureA.zeroscreen

import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePaddingRelative
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.turo.nibel.sample.common.CommonScreenFragment
import com.turo.nibel.sample.common.NextButtonsAdapter
import com.turo.nibel.sample.featureA.firstscreen.FirstScreenEntry
import com.turo.nibel.sample.featureA.results.ColorPickerFragmentScreenEntry
import com.turo.nibel.sample.featureA.results.ColorResult
import com.turo.nibel.sample.featureA.results.ResultsDemoScreenEntry
import com.turo.nibel.sample.navigation.ThirdArgs
import com.turo.nibel.sample.navigation.ThirdScreenDestination
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import nibel.annotations.ExternalDestination
import nibel.runtime.Nibel
import nibel.runtime.ResultLauncher
import nibel.runtime.prepareFragment
import nibel.runtime.registerForNibelResult

@AndroidEntryPoint
class ZeroFragment : CommonScreenFragment() {

    private val viewModel: ZeroViewModel by viewModels()

    private lateinit var colorLauncher: ResultLauncher<ColorResult>
    private var lastColor = "none"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lastColor = savedInstanceState?.getString(KEY_LAST_COLOR) ?: lastColor
        colorLauncher = registerForNibelResult { result ->
            lastColor = result?.color ?: "cancelled"
            showLastColor()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_LAST_COLOR, lastColor)
    }

    private fun showLastColor() {
        view ?: return
        binding.topAppBar.subtitle = "Last result: $lastColor"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.appBarLayout.updatePaddingRelative(top = bars.top)
            binding.scrollView.updatePaddingRelative(bottom = bars.bottom)
            insets
        }
        setInputTextVisible(false)
        binding.nextButtons.adapter = NextButtonsAdapter(viewModel::onContinue)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.state.collect(::setState)
                }
                launch {
                    viewModel.sideEffects.collect(::handleSideEffect)
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun setState(state: ZeroState) {
        binding.topAppBar.title = state.title
        showLastColor()

        val adapter = binding.nextButtons.adapter as NextButtonsAdapter<DearbornNextButton>
        adapter.setNextButtons(state.nextButtons)
    }

    private fun handleSideEffect(sideEffect: ZeroSideEffect) {
        val fragmentManager = requireActivity().supportFragmentManager

        when (sideEffect) {
            is ZeroSideEffect.NavigateToFirstScreen -> fragmentManager.commit {
                val fragment = FirstScreenEntry.newInstance().fragment
                replace(android.R.id.content, fragment)
                addToBackStack(fragment::class.qualifiedName)
            }

            is ZeroSideEffect.NavigateToThirdScreen -> fragmentManager.commit {
                val destination =
                    ThirdScreenDestination(ThirdArgs(input = com.turo.nibel.sample.navigation.Input.Nothing))
                val fragment = Nibel.newFragmentEntry(destination)?.fragment
                if (fragment == null) {
                    showErrorMessage(destination)
                    return
                }
                replace(android.R.id.content, fragment)
                addToBackStack(fragment::class.qualifiedName)
            }

            is ZeroSideEffect.NavigateToResultsDemo -> fragmentManager.commit {
                val fragment = ResultsDemoScreenEntry.newInstance().fragment
                replace(android.R.id.content, fragment)
                addToBackStack(fragment::class.qualifiedName)
            }

            is ZeroSideEffect.PickColor -> fragmentManager.commit {
                val fragment = colorLauncher.prepareFragment(ColorPickerFragmentScreenEntry.newInstanceForResult())
                replace(android.R.id.content, fragment)
                addToBackStack(fragment::class.qualifiedName)
            }
        }
    }

    private fun showErrorMessage(destination: ExternalDestination) {
        Toast.makeText(
            requireContext(),
            "${destination::class.simpleName} should be ImplementationType.Fragment",
            LENGTH_SHORT,
        ).show()
    }

    private companion object {
        const val KEY_LAST_COLOR = "last_color"
    }
}
