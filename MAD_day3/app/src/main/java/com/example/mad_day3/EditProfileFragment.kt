package com.example.mad_day3

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.mad_day3.databinding.FragmentEditProfileBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private val db = Firebase.firestore
    private lateinit var sharedPref: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPref = requireActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getString("userID", null) ?: run {
            showLoginPrompt()
            return
        }

        loadUserData(userId)
        setupSaveButton(userId)
    }

    private fun loadUserData(userId: String) {
        db.collection("userAccounts").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    binding.etUsername.setText(document.getString("userName"))
                    binding.etEmail.setText(document.getString("email"))
                    binding.etContact.setText(document.getString("contactNo"))
                    binding.etAddress.setText(document.getString("address"))
                    binding.etCity.setText(document.getString("city"))
                    binding.etFamilyMembers.setText(document.getLong("familyMembers")?.toString() ?: "0")
                }
            }
            .addOnFailureListener { e ->
                showError("Failed to load profile: ${e.message}")
            }
    }

    private fun setupSaveButton(userId: String) {
        binding.btnSave.setOnClickListener {
            if (validateInputs()) {
                saveProfileChanges(userId)
            }
        }
    }

    private fun validateInputs(): Boolean {
        return when {
            binding.etUsername.text.isNullOrEmpty() -> {
                binding.etUsername.error = "Username is required"
                false
            }
            binding.etEmail.text.isNullOrEmpty() -> {
                binding.etEmail.error = "Email is required"
                false
            }
            binding.etContact.text.isNullOrEmpty() -> {
                binding.etContact.error = "Contact number is required"
                false
            }
            else -> true
        }
    }

    private fun saveProfileChanges(userId: String) {
        val familyMembers = try {
            binding.etFamilyMembers.text.toString().toLong()
        } catch (e: NumberFormatException) {
            0L
        }

        val updates = hashMapOf<String, Any>(
            "userName" to binding.etUsername.text.toString(),
            "email" to binding.etEmail.text.toString(),
            "contactNo" to binding.etContact.text.toString(),
            "address" to binding.etAddress.text.toString(),
            "city" to binding.etCity.text.toString(),
            "familyMembers" to familyMembers
        )

        db.collection("userAccounts").document(userId)
            .update(updates)
            .addOnSuccessListener {
                // Update city in SharedPreferences if changed
                if (sharedPref.getString("city", "") != binding.etCity.text.toString()) {
                    sharedPref.edit().putString("city", binding.etCity.text.toString()).apply()
                }
                showSuccess("Profile updated successfully!")
                parentFragmentManager.popBackStack()
            }
            .addOnFailureListener { e ->
                showError("Failed to update profile: ${e.message}")
            }
    }

    private fun showLoginPrompt() {
        Snackbar.make(binding.root, "You need to login first", Snackbar.LENGTH_LONG)
            .setAction("Login") {
                startActivity(Intent(requireActivity(), MainActivity::class.java))
                requireActivity().finish()
            }
            .show()
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun showSuccess(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}