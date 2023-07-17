package com.turo.nibel.sample.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.turo.nibel.sample.common.databinding.FragmentCommonScreenBinding

abstract class CommonScreenFragment : Fragment() {

    private var _binding: FragmentCommonScreenBinding? = null
    protected val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCommonScreenBinding.inflate(inflater, container, false)
        binding.topAppBar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        setInputTextEditable(true)
        return binding.root
    }

    fun setInputTextEditable(editable: Boolean) {
        binding.inputTextLayout.isEnabled = editable
        binding.inputTextLayout.helperText =
            if (editable) requireContext().getString(R.string.input_supporting_text)
            else requireContext().getString(R.string.input_read_only_text)
    }

    fun setInputTextVisible(visible: Boolean) {
        binding.inputTextLayoutContainer.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
