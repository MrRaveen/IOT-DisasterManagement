package com.example.mad_day3.Warning

import android.app.Dialog
import com.example.mad_day3.Warning.WarningAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
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
import com.google.firebase.firestore.FirebaseFirestore


class WarningFragment : Fragment() {
    private var _binding: FragmentWarningBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: WarningAdapter
    private var currentFilter: String? = null
    private val firestore = FirebaseFirestore.getInstance()
    private val locationCategories = mutableMapOf<String, String>() // Map of location to category
    private var loadingDialog: Dialog? = null
    private var completedRequests = 0
    private var totalRequests = 0

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

        // First load location categories from Firestore
        showLoadingDialog()
        loadLocationCategories {
            fetchAllSensorWarnings()
        }

        fetchLandslideWarnings()
        checkFirebaseConnection()
    }


    private fun checkFirebaseConnection() {
        val connectedRef = Firebase.database.getReference(".info/connected")
        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                Log.d("FIREBASE_CONNECTION", "Connected: $connected")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FIREBASE_CONNECTION", "Connection listener cancelled")
            }
        })
    }


    private fun showLoadingDialog() {
        loadingDialog = Dialog(requireContext())
        loadingDialog?.setContentView(R.layout.custom_dialog)
        loadingDialog?.window?.setLayout(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        loadingDialog?.setCancelable(false)
        loadingDialog?.show()
    }

    private fun hideLoadingDialog() {
        loadingDialog?.dismiss()
        loadingDialog = null
    }
    private fun loadLocationCategories(onComplete: () -> Unit) {
        firestore.collection("locationInfo")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val name = document.getString("name") ?: ""
                    val category = document.getString("category") ?: ""
                    locationCategories[name] = category
                }
                onComplete()
            }
            .addOnFailureListener { exception ->
                Log.w("WarningFragment", "Error getting location categories", exception)
                onComplete()
            }
    }
    private fun fetchAllSensorWarningsWithTimeout() {
        showLoadingDialog()

        // Set a timeout to hide the loading dialog
        binding.root.postDelayed({
            if (loadingDialog?.isShowing == true) {
                hideLoadingDialog()
                showError("Loading timed out. Please check your connection.")
            }
        }, 15000) // 15 seconds timeout

        fetchAllSensorWarnings()
    }

    private fun showError(message: String) {
        // You can use a Snackbar or Toast to show the error
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_LONG).show()
    }

    private fun fetchAllSensorWarnings() {
        val database = Firebase.database.reference.child("Sensors")
        Log.d("FIREBASE_DEBUG", "Starting to fetch all sensor warnings")

        adapter.clearWarnings() // Clear existing warnings before fetching new ones
        // Map each sensor type to its processor and warning type
        val sensorHandlers = listOf(
            SensorHandler("TiltReadings", ::processLandslideWarnings, "landslide"),
            SensorHandler("SoilReadings", ::processFloodWarnings, "flood"),
            SensorHandler("RainReadings", ::processFloodWarnings, "flood"),
            SensorHandler("MPU6050Readings", ::processLandslideWarnings, "landslide"),
            SensorHandler("BMP180Readings", ::processFloodWarnings, "flood")
        )

        sensorHandlers.forEach { handler ->
            database.child(handler.sensorType).addValueEventListener(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (!snapshot.exists()) {
                            Log.d("FIREBASE_DEBUG", "No data found for ${handler.sensorType}")
                            return
                        }

                        val warnings = snapshot.children.mapNotNull { data ->
                            try {
                                handler.processor(data)?.apply {
                                    type = handler.warningType
                                    // Ensure ID is unique across all sensor types
                                    id = "${handler.sensorType}_${data.key ?: ""}"
                                }
                            } catch (e: Exception) {
                                Log.e("PROCESS_ERROR", "Error processing ${handler.sensorType}", e)
                                null
                            }

                        }

                        Log.d("FIREBASE_DEBUG", "Processed ${warnings.size} warnings from ${handler.sensorType}")
                        adapter.addWarnings(warnings)
                        filterWarnings()
                        completedRequests++
                        // Hide loading dialog when all requests are completed
                        if (completedRequests >= totalRequests) {
                            hideLoadingDialog()
                        }

                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("FIREBASE_ERROR", "Error reading ${handler.sensorType}: ${error.message}")
                        completedRequests++
                        // Hide loading dialog even if there's an error
                        if (completedRequests >= totalRequests) {
                            hideLoadingDialog()
                        }
                    }
                }
            )
        }
        // Also set a timeout to hide the dialog in case of no response
        binding.root.postDelayed({
            hideLoadingDialog()
        }, 10000) // 10 seconds timeout
    }
    private fun processLandslideWarnings(data: DataSnapshot): WarningFragment.Warning? {
        return try {
            val location = data.child("location").getValue(String::class.java) ?: ""
            val timestamp = data.child("timestamp").getValue(String::class.java) ?: ""

            // For Tilt Sensor
            if (data.hasChild("value")) {
                val value = data.child("value").getValue(Int::class.java) ?: 0
                if (value == 1) {
                    return WarningFragment.Warning(
                        id = data.key ?: "",
                        type = "landslide",
                        location = location,
                        timestamp = timestamp,
                        value = 1
                    )
                }
            }

            // For MPU6050 Sensor
            if (data.hasChild("accelX")) {
                val accelX = data.child("accelX").getValue(Double::class.java) ?: 0.0
                val accelY = data.child("accelY").getValue(Double::class.java) ?: 0.0
                val accelZ = data.child("accelZ").getValue(Double::class.java) ?: 0.0

                // Customize these thresholds based on your needs
                if (accelX > 0.5 || accelY > 0.5 || accelZ > 1.5) {
                    return WarningFragment.Warning(
                        id = data.key ?: "",
                        type = "landslide",
                        location = location,
                        timestamp = timestamp,
                        value = 1
                    )
                }
            }

            null
        } catch (e: Exception) {
            Log.e("WarningFragment", "Error processing landslide warning", e)
            null
        }
    }

    private fun processFloodWarnings(data: DataSnapshot): WarningFragment.Warning? {
        return try {
            val location = data.child("location").getValue(String::class.java) ?: ""
            val timestamp = data.child("timestamp").getValue(String::class.java) ?: ""

            // For Soil and Rain Sensors
            if (data.hasChild("digital")) {
                val digital = data.child("digital").getValue(Int::class.java) ?: 0
                if (digital == 1) {
                    return WarningFragment.Warning(
                        id = data.key ?: "",
                        type = "flood",
                        location = location,
                        timestamp = timestamp,
                        value = 1
                    )
                }
            }

            // For BMP180 Sensor
            if (data.hasChild("pressure_hPa")) {
                val pressure = data.child("pressure_hPa").getValue(Double::class.java) ?: 0.0
                // Customize this threshold based on your needs
                if (pressure < 1000.0) {
                    return WarningFragment.Warning(
                        id = data.key ?: "",
                        type = "flood",
                        location = location,
                        timestamp = timestamp,
                        value = 1
                    )
                }
            }

            null
        } catch (e: Exception) {
            Log.e("WarningFragment", "Error processing flood warning", e)
            null
        }
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
            .setNeutralButton("Clear Filter") { dialog, _ ->
            currentFilter = null
            Log.d("FILTER_DEBUG", "Filter cleared")
            filterWarnings()
            dialog.dismiss()
        }
            .setOnDismissListener {
                Log.d("FILTER_DEBUG", "Dialog dismissed. Current filter: $currentFilter")
            }
            .show()
    }

    private fun filterWarnings() {
        val filtered = if (currentFilter != null) {
            adapter.getFilteredList().filter { it.location == currentFilter }
        } else {
            adapter.getFilteredList()
        }
        adapter.submitList(filtered.toMutableList()) // Ensure mutable list is passed

        // Update filter status text
        binding.filterStatus.text = currentFilter?.let { "Showing: $it" } ?: "Showing all locations"

        // Force refresh the RecyclerView
        binding.warningsRecyclerView.adapter?.notifyDataSetChanged()
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

    data class SensorHandler(
        val sensorType: String,
        val processor: (DataSnapshot) -> WarningFragment.Warning?,
        val warningType: String
    )
    data class Warning(
        var id: String = "",
        var type: String = "", // "landslide" or "flood"
        val location: String = "",
        val timestamp: String = "",
        val value: Int = 0,
        val category: String = "" // From locationInfo
    ) {
        constructor() : this("", "", "", "", 0, "") // For Firebase
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hideLoadingDialog()
        _binding = null
    }
}