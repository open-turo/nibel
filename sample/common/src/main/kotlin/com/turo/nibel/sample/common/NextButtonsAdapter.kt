package com.turo.nibel.sample.common

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.turo.nibel.sample.common.databinding.ItemNextButtonBinding

class NextButtonsAdapter<NB : NextButton>(
    private val onItemClick: (NB) -> Unit
) : RecyclerView.Adapter<NextButtonsAdapter.ViewHolder>() {

    private val nextButtons = mutableListOf<NB>()

    class ViewHolder(
        val binding: ItemNextButtonBinding
    ) : RecyclerView.ViewHolder(binding.root)

    @SuppressLint("NotifyDataSetChanged")
    fun setNextButtons(nextButtons: List<NB>) {
        this.nextButtons.clear()
        this.nextButtons += nextButtons
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNextButtonBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = with(holder) {
        binding.nextButton.text = nextButtons[position].title
        binding.nextButton.setOnClickListener {
            onItemClick(nextButtons[position])
        }
    }

    override fun getItemCount() = nextButtons.size
}
