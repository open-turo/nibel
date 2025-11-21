package com.turo.nibel.sample.featureC.fourthscreen

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.turo.nibel.sample.common.CommonScreenFragment
import com.turo.nibel.sample.common.NextButtonsAdapter
import com.turo.nibel.sample.navigation.FourthArgs
import com.turo.nibel.sample.navigation.FourthScreenDestination
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import nibel.annotations.LegacyExternalEntry
import nibel.runtime.getNibelArgs

@AndroidEntryPoint
@LegacyExternalEntry(destination = FourthScreenDestination::class)
class FourthFragment : CommonScreenFragment() {

    private val viewModel: FourthViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setInputTextEditable(false)
        binding.nextButtons.adapter = NextButtonsAdapter(onItemClick = viewModel::onContinue)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect(::setState)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun setState(state: FourthState) {
        binding.topAppBar.title = state.title

        val inputText = (arguments?.getNibelArgs<FourthArgs>() as? FourthArgs.WithText)?.inputText ?: ""
        binding.inputText.setText(inputText)

        val adapter = binding.nextButtons.adapter as NextButtonsAdapter<FourthNextButton>
        adapter.setNextButtons(state.nextButtons)
    }
}
