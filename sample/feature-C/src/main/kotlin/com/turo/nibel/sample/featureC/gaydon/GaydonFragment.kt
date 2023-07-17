package com.turo.nibel.sample.featureC.gaydon

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.turo.nibel.annotations.LegacyExternalEntry
import com.turo.nibel.sample.common.CommonScreenFragment
import com.turo.nibel.sample.common.NextButtonsAdapter
import com.turo.nibel.sample.navigation.GaydonDestination
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
@LegacyExternalEntry(destination = GaydonDestination::class)
class GaydonFragment : CommonScreenFragment() {

    private val viewModel: GaydonViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setInputTextVisible(false)
        binding.nextButtons.adapter = NextButtonsAdapter(viewModel::onContinue)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect(::setState)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun setState(state: GaydonState) {
        binding.topAppBar.title = state.title

        val adapter = binding.nextButtons.adapter as NextButtonsAdapter<GaydonNextButton>
        adapter.setNextButtons(state.nextButtons)
    }
}
