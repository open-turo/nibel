package com.turo.nibel.sample.featureA.zeroscreen

import android.os.Bundle
import android.view.View
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.turo.nibel.sample.common.CommonScreenFragment
import com.turo.nibel.sample.common.NextButtonsAdapter
import com.turo.nibel.sample.featureA.firstscreen.FirstScreenEntry
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ZeroFragment : CommonScreenFragment() {

    private val viewModel: ZeroViewModel by viewModels()

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
    private fun setState(state: ZeroState) {
        binding.topAppBar.title = state.title

        val adapter = binding.nextButtons.adapter as NextButtonsAdapter<DearbornNextButton>
        adapter.setNextButtons(state.nextButtons)
    }

    private fun handleSideEffect(sideEffect: ZeroSideEffect) {
        when (sideEffect) {
            is ZeroSideEffect.NavigateToFirstScreen ->
                requireActivity().supportFragmentManager.commit {
                    val fragment = FirstScreenEntry.newInstance().fragment
                    replace(android.R.id.content, fragment)
                    addToBackStack(fragment::class.qualifiedName)
                }
        }
    }
}
