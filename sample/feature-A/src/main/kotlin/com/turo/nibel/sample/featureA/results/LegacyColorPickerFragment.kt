package com.turo.nibel.sample.featureA.results

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePaddingRelative
import com.turo.nibel.sample.common.CommonScreenFragment
import com.turo.nibel.sample.common.GenericNextButton
import com.turo.nibel.sample.common.NextButtonsAdapter
import nibel.annotations.LegacyEntry
import nibel.runtime.setNibelResult

/**
 * Plain fragment picker opened compose → fragment.
 */
@LegacyEntry(result = ColorResult::class)
class LegacyColorPickerFragment : CommonScreenFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.appBarLayout.updatePaddingRelative(top = bars.top)
            binding.scrollView.updatePaddingRelative(bottom = bars.bottom)
            insets
        }
        binding.topAppBar.title = "Color Picker (legacy fragment)"
        setInputTextVisible(false)

        val adapter = NextButtonsAdapter<GenericNextButton> { button ->
            val color = pickableColors.find { "Pick $it" == button.title }
            setNibelResult(color?.let(::ColorResult))
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        adapter.setNextButtons(pickableColors.map { GenericNextButton("Pick $it") } + GenericNextButton("Cancel"))
        binding.nextButtons.adapter = adapter
    }
}
