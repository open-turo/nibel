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
import com.turo.nibel.sample.navigation.ThirdArgs
import com.turo.nibel.sample.navigation.ThirdScreenDestination
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import nibel.annotations.ExternalDestination
import nibel.runtime.Nibel

@AndroidEntryPoint
class ZeroFragment : CommonScreenFragment() {

    private val viewModel: ZeroViewModel by viewModels()

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
                val destination = ThirdScreenDestination(ThirdArgs(inputText = ""))
                val fragment = Nibel.newFragmentEntry(destination)?.fragment
                if (fragment == null) {
                    showErrorMessage(destination)
                    return
                }
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
}
