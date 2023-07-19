package com.turo.nibel.sample.featureA.dearborn

import android.os.Bundle
import android.view.View
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import nibel.runtime.Nibel
import com.turo.nibel.sample.common.CommonScreenFragment
import com.turo.nibel.sample.common.NextButtonsAdapter
import com.turo.nibel.sample.featureA.yokohama.YokohamaArgs
import com.turo.nibel.sample.featureA.yokohama.YokohamaScreenEntry
import com.turo.nibel.sample.navigation.StuttgartDestination
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DearbornFragment : CommonScreenFragment() {

    private val viewModel: DearbornViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
    private fun setState(state: DearbornState) {
        binding.topAppBar.title = state.title

        val adapter = binding.nextButtons.adapter as NextButtonsAdapter<DearbornNextButton>
        adapter.setNextButtons(state.nextButtons)
    }

    private fun handleSideEffect(sideEffect: DearbornSideEffect) {
        when (sideEffect) {
            is DearbornSideEffect.NavigateToYokohama ->
                requireActivity().supportFragmentManager.commit {
                    val args = YokohamaArgs("Hi from Dearborn")
                    replace(android.R.id.content, YokohamaScreenEntry.newInstance(args).fragment)
                    addToBackStack(YokohamaScreenEntry::class.qualifiedName)
                }

            is DearbornSideEffect.NavigateToStuttgart ->
                requireActivity().supportFragmentManager.commit {
                    val entry = Nibel.newFragmentEntry(StuttgartDestination)!!
                    replace(android.R.id.content, entry.fragment)
                    addToBackStack(StuttgartDestination::class.qualifiedName)
                }
        }
    }
}
