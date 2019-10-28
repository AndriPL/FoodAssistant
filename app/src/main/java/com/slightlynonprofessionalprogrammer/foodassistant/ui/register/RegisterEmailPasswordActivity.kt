package com.slightlynonprofessionalprogrammer.foodassistant.ui.register

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.slightlynonprofessionalprogrammer.foodassistant.R
import com.slightlynonprofessionalprogrammer.foodassistant.ui.login.LoginActivity
import kotlinx.android.synthetic.main.activity_register_email_password.*
import com.slightlynonprofessionalprogrammer.foodassistant.ui.main.MainActivity


class RegisterEmailPasswordActivity : AppCompatActivity(){

    private lateinit var auth: FirebaseAuth

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_email_password)

        auth = FirebaseAuth.getInstance()
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            changeUItoMain()
        }else {
            val email = findViewById<EditText>(R.id.email_register_textfield)
            val password = findViewById<EditText>(R.id.password_register_textfield)
            val register = findViewById<Button>(R.id.register_button)

            register.setOnClickListener(){
                when {
                    email.text.toString() == "" -> email.setError("Required")
                    email.text.toString().contains("@") == false -> email.setError("Invalid Format")
                    password.text.toString() == "" -> password.setError("Required")
                    password.text.toString().length < 8 -> password.setError("Must be >7 characters")
                    else -> createAccount(email.text.toString(), password.text.toString())
                }
                //TODO - progress bar
            }
        }
    }

    private fun createAccount(email: String, password: String) {
        Log.d(TAG, "createAccount:$email")
        if (!validateForm()) {
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { task ->
                Log.d(TAG, "Successfully singed up user: ${auth.currentUser!!.uid}");
                addUserToDatabase(auth.currentUser!!)
                changeUItoMain()
            }
            .addOnFailureListener {e ->
                Log.w(TAG, "Signup Error", e)
                Toast.makeText(baseContext,"Authentication failed. ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun addUserToDatabase(user: FirebaseUser) {
        val database = FirebaseFirestore.getInstance()
        val userData = hashMapOf(
            "uid" to user.uid,
            "username" to username_register_textfield.text.toString(),
            "email" to email_register_textfield.text.toString()
        )
        database.collection("users").document("${user.uid}")
            .set(userData, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "Successfully saved user ${user.uid} to database")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failure when saving user to database", e)
            }
    }

    class User(val UID: String, val username: String)

    private fun changeUItoMain() {
        Log.d(TAG, "Changing Activity to Main")
        val intent = Intent(this,  MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun changeUItoLogin() {
        Log.d(TAG, "Changing Activity to Register")
        val intent = Intent(this,  LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun validateForm(): Boolean {
        var valid = true

        val email = email_register_textfield.text.toString()
        if (TextUtils.isEmpty(email)) {
            email_register_textfield.error = "Required."
            valid = false
        } else {
            email_register_textfield.error = null
        }

        val password = password_register_textfield.text.toString()
        if (TextUtils.isEmpty(password)) {
            password_register_textfield.error = "Required."
            valid = false
        } else {
            password_register_textfield.error = null
        }

        return valid
    }

    companion object {
        private const val TAG = "RegisterEmailPassword"
    }
}