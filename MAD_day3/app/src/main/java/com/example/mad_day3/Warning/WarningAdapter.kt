package com.example.mad_day3.Warning

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
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

    private val allWarnings = mutableListOf<WarningFragment.Warning>()

    fun addWarnings(newWarnings: List<WarningFragment.Warning>) {
        val current = currentList.toMutableList()
        current.addAll(newWarnings)
        submitList(current)
        Log.d("ADAPTER_DEBUG", "Submitted ${newWarnings.size} new warnings. Total: ${current.size}")
    }
    fun getFilteredList(): List<WarningFragment.Warning> {
        return currentList
    }

    inner class WarningViewHolder(private val binding: ItemWarningBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(warning: WarningFragment.Warning) {
            binding.apply {
                // Set icon and text based on warning type
                when (warning.type) {
                    "landslide" -> {
                        warningIcon.setImageResource(R.drawable.landslide_svgrepo_com)
                        warningTypeText.text = "Landslide Warning"
                        warningTypeText.setTextColor(Color.parseColor("#FF9800")) // Orange
                    }
                    "flood" -> {
                        warningIcon.setImageResource(R.drawable.flood_warning_svgrepo_com)
                        warningTypeText.text = "Flood Warning"
                        warningTypeText.setTextColor(Color.parseColor("#2196F3")) // Blue
                    }
                }

                tvLocation.text = warning.location
                tvTime.text = warning.timestamp

                // Set danger level
                if (warning.value == 1) {
                    tvDangerLevel.text = "HIGH RISK"
                    tvDangerLevel.setBackgroundResource(R.drawable.bg_danger_high)
                } else {
                    tvDangerLevel.text = "Normal"
                    tvDangerLevel.setBackgroundResource(R.drawable.bg_danger_low)
                }

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