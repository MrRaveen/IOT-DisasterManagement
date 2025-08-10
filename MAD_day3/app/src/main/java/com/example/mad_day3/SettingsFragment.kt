package com.example.mad_day3

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.mad_day3.databinding.FragmentSettingsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import android.net.Uri
import android.provider.Settings
import android.widget.Button

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val db = Firebase.firestore
    private lateinit var sharedPref: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPref = requireActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getString("userID", null)

        if (userId == null) {
            showLoginPrompt()
            return
        }

        loadUserData(userId)
        setupClickListeners(userId)
    }

    private fun loadUserData(userId: String) {
        db.collection("userAccounts").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // User Info
                    // Inside loadUserData:
                    binding.tvEmail.text = "Email: ${document.getString("email") ?: "No email"}"
                    binding.tvContact.text = "Contact: ${document.getString("contactNo") ?: "No contact"}"
                    val address = document.getString("address") ?: ""
                    val city = document.getString("city") ?: ""
                    binding.tvAddress.text = "Address: ${if (city.isNotEmpty()) "$address, $city" else address}"
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
                showError("Failed to load settings: ${e.message}")
            }
    }

    private fun setupClickListeners(userId: String) {

        binding.btnChangePassword.setOnClickListener {
            showChangePasswordDialog(userId)
        }
        // Notification Preferences
        binding.switchLandslide.setOnCheckedChangeListener { _, isChecked ->
            updateNotificationPref(userId, "landslideAlerts", isChecked)
        }
        binding.switchFlood.setOnCheckedChangeListener { _, isChecked ->
            updateNotificationPref(userId, "floodAlerts", isChecked)
        }
        binding.switchEarthquake.setOnCheckedChangeListener { _, isChecked ->
            updateNotificationPref(userId, "earthquakeAlerts", isChecked)
        }
        binding.btnHelpSupport.setOnClickListener {
            showHelpSupportDialog()
        }

        binding.btnAbout.setOnClickListener {
            showAboutDialog()
        }

        // Theme Selection
        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val theme = when (checkedId) {
                R.id.radio_dark -> "dark"
                R.id.radio_system -> "system"
                else -> "light"
            }
            updateThemePref(userId, theme)
        }

        // Account Actions
        binding.btnLogout.setOnClickListener {
            logoutUser()
        }

        binding.btnEditProfile.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, EditProfileFragment())
                .addToBackStack(null)
                .commit()
        }
    }
    private fun showHelpSupportDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_help_support, null)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.btn_call_hotline).setOnClickListener {
            makePhoneCall("+94112222222")
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btn_call_support).setOnClickListener {
            makePhoneCall("+94112333333")
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showAboutDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_about, null)

        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun makePhoneCall(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }

        // Check if device can handle phone calls
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        } else {
            showError("No phone app available")
        }
    }

    private fun showChangePasswordDialog(userId: String) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_change_password, null)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Change Password")
            .setView(dialogView)
            .setPositiveButton("Change") { _, _ ->
                val currentPassword = dialogView.findViewById<EditText>(R.id.et_current_password).text.toString()
                val newPassword = dialogView.findViewById<EditText>(R.id.et_new_password).text.toString()
                val confirmPassword = dialogView.findViewById<EditText>(R.id.et_confirm_password).text.toString()

                if (validatePasswordChange(currentPassword, newPassword, confirmPassword)) {
                    verifyAndChangePassword(userId, currentPassword, newPassword)
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun validatePasswordChange(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ): Boolean {
        return when {
            currentPassword.isEmpty() -> {
                showError("Current password is required")
                false
            }
            newPassword.length < 8 -> {
                showError("Password must be at least 8 characters")
                false
            }
            newPassword != confirmPassword -> {
                showError("New passwords don't match")
                false
            }
            else -> true
        }
    }

    private fun verifyAndChangePassword(userId: String, currentPassword: String, newPassword: String) {
        // First verify current password matches Firestore
        db.collection("userAccounts").document(userId)
            .get()
            .addOnSuccessListener { document ->
                val storedPassword = document.getString("password") ?: ""

                if (storedPassword == currentPassword) {
                    // Update password in Firestore
                    db.collection("userAccounts").document(userId)
                        .update("password", newPassword)
                        .addOnSuccessListener {
                            showSuccess("Password changed successfully")

                        }
                        .addOnFailureListener { e ->
                            showError("Failed to update password: ${e.message}")
                        }
                } else {
                    showError("Current password is incorrect")
                }
            }
            .addOnFailureListener { e ->
                showError("Failed to verify password: ${e.message}")
            }
    }



    private fun updateNotificationPref(userId: String, type: String, enabled: Boolean) {
        db.collection("userAccounts").document(userId)
            .update("notificationPrefs.$type", enabled)
            .addOnFailureListener { e ->
                showError("Failed to update notification preference")
            }
    }

    private fun updateThemePref(userId: String, theme: String) {
        // Apply immediately
        when (theme) {
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }

        // Save to Firestore
        db.collection("userAccounts").document(userId)
            .update("themePref", theme)
            .addOnFailureListener { e ->
                Log.e("Settings", "Failed to save theme", e)
            }
    }

    private fun logoutUser() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                // Clear shared preferences
                sharedPref.edit().clear().apply()
                // Go to login screen
                startActivity(Intent(requireActivity(), MainActivity::class.java))
                requireActivity().finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showLoginPrompt() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Not Logged In")
            .setMessage("You need to login to access settings")
            .setPositiveButton("Login") { _, _ ->
                startActivity(Intent(requireActivity(), MainActivity::class.java))
                requireActivity().finish()
            }
            .setNegativeButton("Cancel") { _, _ ->
                requireActivity().onBackPressed()
            }
            .show()
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun showSuccess(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}