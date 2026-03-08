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
    private var isThemeChanging = false


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
                    val userName = document.getString("userName") ?: ""
                    binding.tvUsername.text = userName
                    binding.tvEmail.text = "Email: ${document.getString("email") ?: "No email"}"
                    // Notification Preferences
                    val notifPrefs = document.get("notificationPrefs") as? Map<String, Boolean>
                    binding.switchLandslide.isChecked = notifPrefs?.get("landslideAlerts") ?: true
                    binding.switchFlood.isChecked = notifPrefs?.get("floodAlerts") ?: true



                    binding.radioGroup.setOnCheckedChangeListener(null)
                    // Theme Preference
                    when (document.getString("themePref")) {
                        "dark" -> binding.radioDark.isChecked = true
                        "system" -> binding.radioSystem.isChecked = true
                        else -> binding.radioLight.isChecked = true
                    }
                    // Restore listener after setting initial state
                    setupThemeListener(userId)

                }
            }
            .addOnFailureListener { e ->
                showError("Failed to load settings: ${e.message}")
            }
    }
    private fun setupThemeListener(userId: String) {
        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (isThemeChanging) return@setOnCheckedChangeListener

            isThemeChanging = true
            val theme = when (checkedId) {
                R.id.radio_dark -> "dark"
                R.id.radio_system -> "system"
                else -> "light"
            }

                updateThemePref(userId, theme)

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
        binding.btnHelpSupport.setOnClickListener {
            showHelpSupportDialog()
        }

        binding.btnAbout.setOnClickListener {
            showAboutDialog()
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


        isThemeChanging = true
        // Save to Firestore
        db.collection("userAccounts").document(userId)
            .update("themePref", theme)
            .addOnSuccessListener {
                // Save theme locally
                ThemeUtils.saveTheme(requireContext(), theme)

                showSuccess("Theme changed to ${theme.replaceFirstChar { it.uppercase() }}")

                // Apply theme without immediate activity recreation
                applyThemeSafely(theme)
            }
            .addOnFailureListener { e ->
                Log.e("Settings", "Failed to save theme", e)
                showError("Failed to save theme preference")
                revertThemeSelection()
            }
            .addOnCompleteListener {
                isThemeChanging = false
            }
    }

    private fun applyThemeSafely(theme: String) {
        // Apply the theme for future activities
        ThemeUtils.applyTheme(theme)

        // Use a delayed approach to restart the activity safely
        binding.root.postDelayed({
            if (isAdded && !requireActivity().isFinishing) {
                restartActivitySafely()
            }
        }, 1000) // 1 second delay to allow UI to settle
    }

    private fun restartActivitySafely() {
        try {
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            requireActivity().finish()
        } catch (e: Exception) {
            Log.e("Settings", "Failed to restart activity", e)
            // If restart fails, just continue - theme will apply on next app launch
        }
    }

    private fun revertThemeSelection() {
        // Get current theme from shared preferences and revert UI
        val currentTheme = sharedPref.getString("themePref", "light") ?: "light"
        binding.radioGroup.setOnCheckedChangeListener(null)

        when (currentTheme) {
            "dark" -> binding.radioDark.isChecked = true
            "system" -> binding.radioSystem.isChecked = true
            else -> binding.radioLight.isChecked = true
        }
        // Restore listener
        val userId = sharedPref.getString("userID", "") ?: ""
        if (userId.isNotEmpty()) {
            setupThemeListener(userId)
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