package com.example.mad_day3.Warning

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.mad_day3.R
import com.example.mad_day3.databinding.ItemWarningBinding
import com.example.mad_day3.Warning.WarningFragment
import androidx.recyclerview.widget.ListAdapter


class WarningAdapter(
    private val onItemClick: (WarningFragment.Warning) -> Unit
) : ListAdapter<WarningFragment.Warning, WarningAdapter.WarningViewHolder>(DiffCallback()) {

    fun getFilteredList(): List<WarningFragment.Warning> {
        return currentList
    }

    inner class WarningViewHolder(private val binding: ItemWarningBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(warning: WarningFragment.Warning) {
            binding.apply {
                tvLocation.text = warning.location
                tvTime.text = warning.timestamp
                tvDangerLevel.text = if (warning.value == 1) "HIGH RISK" else "Normal"
                tvDangerLevel.setBackgroundResource(
                    if (warning.value == 1) R.drawable.bg_danger_high
                    else R.drawable.bg_danger_low
                )
                root.setOnClickListener { onItemClick(warning) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WarningViewHolder {
        val binding = ItemWarningBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return WarningViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WarningViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<WarningFragment.Warning>() {
        override fun areItemsTheSame(
            oldItem: WarningFragment.Warning,
            newItem: WarningFragment.Warning
        ) = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: WarningFragment.Warning,
            newItem: WarningFragment.Warning
        ) = oldItem == newItem
    }
}