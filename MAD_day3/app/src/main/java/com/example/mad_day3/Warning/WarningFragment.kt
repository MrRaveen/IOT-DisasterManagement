package com.example.mad_day3.Warning

import com.example.mad_day3.Warning.WarningAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mad_day3.databinding.FragmentWarningBinding
import com.example.mad_day3.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.Firebase
import com.google.firebase.database.database


class WarningFragment : Fragment() {
    private var _binding: FragmentWarningBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: WarningAdapter
    private var currentFilter: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWarningBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = WarningAdapter { warning ->
            showWarningDetails(warning)
        }

        binding.warningsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@WarningFragment.adapter
            setHasFixedSize(true)
        }

        binding.filterButton.setOnClickListener {
            showLocationFilterDialog()
        }

        fetchLandslideWarnings()
    }

    private fun fetchLandslideWarnings() {
        Firebase.database.reference.child("Sensors/TiltReadings")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val warnings = snapshot.children.mapNotNull { data ->
                        data.getValue(Warning::class.java)?.copy(id = data.key ?: "")
                    }
                    adapter.submitList(warnings)
                    filterWarnings()
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.filterStatus.text = "Error loading data"
                }
            })
    }

    private fun showLocationFilterDialog() {
        val locations = adapter.currentList
            .map { it.location }
            .distinct()
            .sorted()

        if (locations.isEmpty()) {
            binding.filterStatus.text = "No locations available"
            return
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Filter by Location")
            .setItems(locations.toTypedArray()) { _, which ->
                currentFilter = locations[which]
                filterWarnings()
            }
            .setNeutralButton("Clear Filter") { _, _ ->
                currentFilter = null
                filterWarnings()
            }
            .show()
    }

    private fun filterWarnings() {
        val filtered = if (currentFilter != null) {
            adapter.getFilteredList().filter { it.location == currentFilter }
        } else {
            adapter.getFilteredList()
        }
        adapter.submitList(filtered)
        binding.filterStatus.text = currentFilter ?: "Showing all locations"
    }

    private fun showWarningDetails(warning: Warning) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Warning Details")
            .setMessage("""
                Location: ${warning.location}
                Time: ${warning.timestamp}
                Status: ${if (warning.value == 1) "DANGER" else "Normal"}
            """.trimIndent())
            .setPositiveButton("OK", null)
            .show()
    }

    data class Warning(
        val id: String = "",
        val location: String = "",
        val timestamp: String = "",
        val value: Int = 0
    ) {
        constructor() : this("", "", "", 0) // For Firebase
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}