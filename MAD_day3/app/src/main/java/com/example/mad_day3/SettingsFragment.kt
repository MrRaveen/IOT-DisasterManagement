package com.example.mad_day3

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mad_day3.databinding.FragmentSettingsBinding
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.example.mad_day3.R
import com.example.mad_day3.MainActivity
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase



class SettingsFragment: Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val auth: FirebaseAuth = Firebase.auth
    private val db = Firebase.firestore
    private val currentUser = auth.currentUser

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Initialize view binding
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadUserData()
        setupClickListeners()
    }

    private fun loadUserData() {
        currentUser?.uid?.let { uid ->
            db.collection("userAccounts").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Basic Info
                        binding.profileName.text = document.getString("userName") ?: "User"
                        binding.profileEmail.text = document.getString("email") ?: "No email"
                        binding.profileContact.text = document.getString("contactNo") ?: "No contact"
                        binding.profileAddress.text = "${document.getString("address")}, ${document.getString("city")}"

                        // Notification Preferences
                        val notifPrefs = document.get("notificationPrefs") as? Map<String, Boolean>
                        binding.switchLandslide.isChecked = notifPrefs?.get("landslideAlerts") ?: true
                        binding.switchFlood.isChecked = notifPrefs?.get("floodAlerts") ?: true
                        binding.switchEarthquake.isChecked = notifPrefs?.get("earthquakeAlerts") ?: false

                        // Theme Preference
                        when (document.getString("themePref")) {
                            "dark" -> binding.radioDark.isChecked = true
                            "system" -> binding.radioSystem.isChecked = true
                            else -> binding.radioLight.isChecked = true
                        }
                    }
                }
                .addOnFailureListener { e ->
                    // Handle error
                }
        }
    }

    private fun setupClickListeners() {
        // Notification Preferences
        binding.switchLandslide.setOnCheckedChangeListener { _, isChecked ->
            updateNotificationPref("landslideAlerts", isChecked)
        }
        binding.switchFlood.setOnCheckedChangeListener { _, isChecked ->
            updateNotificationPref("floodAlerts", isChecked)
        }
        binding.switchEarthquake.setOnCheckedChangeListener { _, isChecked ->
            updateNotificationPref("earthquakeAlerts", isChecked)
        }

        // Theme Selection
        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val theme = when (checkedId) {
                R.id.radio_dark -> "dark"
                R.id.radio_system -> "system"
                else -> "light"
            }
            updateThemePref(theme)
        }

        // Account Actions
        binding.btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(requireActivity(), MainActivity::class.java))
            requireActivity().finish()
        }

        binding.btnEditProfile.setOnClickListener {
            // Implement profile editing
        }
    }

    private fun updateNotificationPref(type: String, enabled: Boolean) {
        currentUser?.uid?.let { uid ->
            db.collection("userAccounts").document(uid)
                .update("notificationPrefs.$type", enabled)
                .addOnFailureListener { e ->
                    // Handle error
                }
        }
    }

    private fun updateThemePref(theme: String) {
        currentUser?.uid?.let { uid ->
            db.collection("userAccounts").document(uid)
                .update("themePref", theme)
                .addOnSuccessListener {
                    // Optionally apply theme immediately
                }
                .addOnFailureListener { e ->
                    // Handle error
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }





}