package com.example.mad_day3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mad_day3.databinding.FragmentEmergencyContactsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase

class EmergencyContactsFragment : Fragment() {

    private var _binding: FragmentEmergencyContactsBinding? = null
    private val binding get() = _binding!!
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val currentUser = auth.currentUser
    private lateinit var adapter: EmergencyContactsAdapter
    private val contactsList = mutableListOf<EmergencyContact>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmergencyContactsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadEmergencyContacts()
        setupAddButton()
    }

    private fun setupRecyclerView() {
        adapter = EmergencyContactsAdapter(contactsList) { contact ->
            showContactOptionsDialog(contact)
        }
        binding.rvContacts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvContacts.adapter = adapter
    }

    private fun loadEmergencyContacts() {
        currentUser?.uid?.let { uid ->
            db.collection("userAccounts").document(uid)
                .collection("emergencyContacts")
                .get()
                .addOnSuccessListener { result ->
                    contactsList.clear()
                    for (document in result) {
                        val contact = EmergencyContact(
                            id = document.id,
                            name = document.getString("name") ?: "",
                            phone = document.getString("phone") ?: "",
                            relationship = document.getString("relationship") ?: ""
                        )
                        contactsList.add(contact)
                    }
                    adapter.notifyDataSetChanged()
                    binding.emptyState.visibility = if (contactsList.isEmpty()) View.VISIBLE else View.GONE
                }
                .addOnFailureListener { showError("Failed to load contacts") }
        }
    }

    private fun setupAddButton() {
        binding.fabAddContact.setOnClickListener {
            showAddContactDialog()
        }
    }

    private fun showAddContactDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_contact, null)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add Emergency Contact")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val name = dialogView.findViewById<android.widget.EditText>(R.id.et_name).text.toString()
                val phone = dialogView.findViewById<android.widget.EditText>(R.id.et_phone).text.toString()
                val relationship = dialogView.findViewById<android.widget.EditText>(R.id.et_relationship).text.toString()

                if (name.isNotEmpty() && phone.isNotEmpty()) {
                    addEmergencyContact(name, phone, relationship)
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun addEmergencyContact(name: String, phone: String, relationship: String) {
        currentUser?.uid?.let { uid ->
            val contact = hashMapOf(
                "name" to name,
                "phone" to phone,
                "relationship" to relationship
            )

            db.collection("userAccounts").document(uid)
                .collection("emergencyContacts")
                .add(contact)
                .addOnSuccessListener {
                    loadEmergencyContacts() // Refresh the list
                    showSuccess("Contact added successfully")
                }
                .addOnFailureListener { showError("Failed to add contact") }
        }
    }

    private fun showContactOptionsDialog(contact: EmergencyContact) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(contact.name)
            .setMessage("Phone: ${contact.phone}\nRelationship: ${contact.relationship}")
            .setPositiveButton("Edit") { _, _ -> showEditContactDialog(contact) }
            .setNegativeButton("Delete") { _, _ -> deleteContact(contact) }
            .setNeutralButton("Cancel", null)
            .show()
    }

    private fun showEditContactDialog(contact: EmergencyContact) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_contact, null).apply {
            findViewById<android.widget.EditText>(R.id.et_name).setText(contact.name)
            findViewById<android.widget.EditText>(R.id.et_phone).setText(contact.phone)
            findViewById<android.widget.EditText>(R.id.et_relationship).setText(contact.relationship)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Edit Contact")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val name = dialogView.findViewById<android.widget.EditText>(R.id.et_name).text.toString()
                val phone = dialogView.findViewById<android.widget.EditText>(R.id.et_phone).text.toString()
                val relationship = dialogView.findViewById<android.widget.EditText>(R.id.et_relationship).text.toString()

                if (name.isNotEmpty() && phone.isNotEmpty()) {
                    updateContact(contact.id, name, phone, relationship)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateContact(contactId: String, name: String, phone: String, relationship: String) {
        currentUser?.uid?.let { uid ->
            val updates = hashMapOf<String, Any>(
                "name" to name,
                "phone" to phone,
                "relationship" to relationship
            )

            db.collection("userAccounts").document(uid)
                .collection("emergencyContacts")
                .document(contactId)
                .update(updates)
                .addOnSuccessListener {
                    loadEmergencyContacts() // Refresh the list
                    showSuccess("Contact updated successfully")
                }
                .addOnFailureListener { showError("Failed to update contact") }
        }
    }

    private fun deleteContact(contact: EmergencyContact) {
        currentUser?.uid?.let { uid ->
            db.collection("userAccounts").document(uid)
                .collection("emergencyContacts")
                .document(contact.id)
                .delete()
                .addOnSuccessListener {
                    loadEmergencyContacts() // Refresh the list
                    showSuccess("Contact deleted successfully")
                }
                .addOnFailureListener { showError("Failed to delete contact") }
        }
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

    data class EmergencyContact(
        val id: String,
        val name: String,
        val phone: String,
        val relationship: String
    )
}