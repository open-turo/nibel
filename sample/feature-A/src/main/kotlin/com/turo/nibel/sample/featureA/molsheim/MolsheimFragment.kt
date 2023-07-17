package com.turo.nibel.sample.featureA.molsheim

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.turo.nibel.annotations.LegacyEntry
import com.turo.nibel.runtime.getNibelArgs
import com.turo.nibel.sample.common.CommonScreenFragment
import com.turo.nibel.sample.common.NextButtonsAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

@Parcelize
data class MolsheimArgs(val inputText: String) : Parcelable

@AndroidEntryPoint
@LegacyEntry(args = MolsheimArgs::class)
class MolsheimFragment : CommonScreenFragment() {

    private val viewModel: MolsheimViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setInputTextEditable(false)
        binding.nextButtons.adapter = NextButtonsAdapter(viewModel::onContinue)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect(::setState)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun setState(state: MolsheimState) {
        binding.topAppBar.title = state.title

        val inputText = arguments?.getNibelArgs<MolsheimArgs>()?.inputText.orEmpty()
        binding.inputText.setText(inputText)

        val adapter = binding.nextButtons.adapter as NextButtonsAdapter<MolsheimNextButton>
        adapter.setNextButtons(state.nextButtons)
    }
}
