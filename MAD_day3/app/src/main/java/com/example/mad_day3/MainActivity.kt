package com.example.mad_day3

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MainActivity : AppCompatActivity() {
    val db = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val editTextUsername = findViewById<EditText>(R.id.editTextUsername)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)
        val btnClick = findViewById<Button>(R.id.lgoInButton)
        btnClick.setOnClickListener{
            db.collection("userAccounts")
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        //document.getString("Address")
                    }
                }
                .addOnFailureListener { exception ->

                }
            //login process
            val intent2 = Intent(this,SecondActivity::class.java)
            startActivity(intent2)
        }
    }
}