package com.slightlynonprofessionalprogrammer.foodassistant.ui.login

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

import com.slightlynonprofessionalprogrammer.foodassistant.R
import com.slightlynonprofessionalprogrammer.foodassistant.ui.main.MainActivity
import com.slightlynonprofessionalprogrammer.foodassistant.ui.register.RegisterEmailPasswordActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private  var user: FirebaseUser? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        user = auth.currentUser
        if (user != null) {
            changeUItoMain()
        } else {
            val email = findViewById<EditText>(R.id.email_login_textfield)
            val password = findViewById<EditText>(R.id.password_login_textfield)
            val login = findViewById<Button>(R.id.login_button)
            val newAccount = findViewById<TextView>(R.id.login_create_account_text)


            login.setOnClickListener(){
                when {
                    email.text.toString() == "" -> email.setError("Required")
                    email.text.toString().contains("@") == false -> email.setError("Invalid Format")
                    password.text.toString() == "" -> password.setError("Required")
                    else -> signIn(email.text.toString(), password.text.toString())
                }
            }

            newAccount.setOnClickListener(){
                changeUItoRegister();
            }
        }
    }



    private fun signIn(email: String, password: String) {
        Log.d(TAG, "signIn:$email")
        if (!validateForm()) {
            return
        }
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {task ->
                if (!task.isSuccessful()) {
                    Log.e("Sign-in Error", "Exception: $task.exception", task.exception)
                    Toast.makeText(baseContext, "Authentication failed. ${task.exception}", Toast.LENGTH_LONG).show()
                    return@addOnCompleteListener
                } else {
                    user = auth.currentUser;
                    Log.d("Sign-in Success", "Successfully singed in user: $user");
                    changeUItoMain()
                }
            }
    }

    private fun changeUItoMain() {
        Log.d(TAG, "Changing Activity to Main")
        val intent = Intent(this,  MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun changeUItoRegister() {
        Log.d(TAG, "Changing Activity to Register")
        val intent = Intent(this,  RegisterEmailPasswordActivity::class.java)
        startActivity(intent)
    }


//    private fun sendEmailVerification() {
//        // Disable button
//        verifyEmailButton.isEnabled = false
//
//        // Send verification email
//        // [START send_email_verification]
//        val user = auth.currentUser
//        user?.sendEmailVerification()
//            ?.addOnCompleteListener(this) { task ->
//                // [START_EXCLUDE]
//                // Re-enable button
//                verifyEmailButton.isEnabled = true
//
//                if (task.isSuccessful) {
//                    Toast.makeText(baseContext,
//                        "Verification email sent to ${user.email} ",
//                        Toast.LENGTH_SHORT).show()
//                } else {
//                    Log.e(TAG, "sendEmailVerification", task.exception)
//                    Toast.makeText(baseContext,
//                        "Failed to send verification email.",
//                        Toast.LENGTH_SHORT).show()
//                }
//                // [END_EXCLUDE]
//            }
//        // [END send_email_verification]
//    }

    private fun validateForm(): Boolean {
        var valid = true
        val email = findViewById<EditText>(R.id.email_login_textfield)
        val password = findViewById<EditText>(R.id.password_login_textfield)

        if (TextUtils.isEmpty(email.text.toString())) {
            email.error = "Required."
            valid = false
        } else {
            email.error = null
        }

        if (TextUtils.isEmpty(password.text.toString())) {
            password.error = "Required."
            valid = false
        } else {
            password.error = null
        }

        return valid
    }
//
//    private fun updateUI(user: FirebaseUser?) {
//        hideProgressDialog()
//        if (user != null) {
//            status.text = getString(R.string.emailpassword_status_fmt,
//                user.email, user.isEmailVerified)
//            detail.text = getString(R.string.firebase_status_fmt, user.uid)
//
//            emailPasswordButtons.visibility = View.GONE
//            emailPasswordFields.visibility = View.GONE
//            signedInButtons.visibility = View.VISIBLE
//
//            verifyEmailButton.isEnabled = !user.isEmailVerified
//        } else {
//            status.setText(R.string.signed_out)
//            detail.text = null
//
//            emailPasswordButtons.visibility = View.VISIBLE
//            emailPasswordFields.visibility = View.VISIBLE
//            signedInButtons.visibility = View.GONE
//        }
//    }

    companion object {
        private const val TAG = "Login"
    }
}





//import android.app.Activity
//import androidx.lifecycle.Observer
//import androidx.lifecycle.ViewModelProviders
//import android.os.Bundle
//import androidx.annotation.StringRes
//import androidx.appcompat.app.AppCompatActivity
//import android.text.Editable
//import android.text.TextWatcher
//import android.view.View
//import android.view.inputmethod.EditorInfo
//import android.widget.Button
//import android.widget.EditText
//import android.widget.ProgressBar
//import android.widget.Toast
//
//import com.slightlynonprofessionalprogrammer.foodassistant.R
//
//class LoginActivity : AppCompatActivity() {
//
//    private lateinit var loginViewModel: LoginViewModel
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        setContentView(R.layout.activity_login)
//
//        val username = findViewById<EditText>(R.id.email_login_textfield)
//        val password = findViewById<EditText>(R.id.password_login_textfield)
//        val login = findViewById<Button>(R.id.login_button)
//        val loading = findViewById<ProgressBar>(R.id.loading)
//
//        loginViewModel = ViewModelProviders.of(this, LoginViewModelFactory())
//            .get(LoginViewModel::class.java)
//
//        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
//            val loginState = it ?: return@Observer
//
//            // disable login button unless both username / password is valid
//            login.isEnabled = loginState.isDataValid
//
//            if (loginState.usernameError != null) {
//                username.error = getString(loginState.usernameError)
//            }
//            if (loginState.passwordError != null) {
//                password.error = getString(loginState.passwordError)
//            }
//        })
//
//        loginViewModel.loginResult.observe(this@LoginActivity, Observer {
//            val loginResult = it ?: return@Observer
//
//            loading.visibility = View.GONE
//            if (loginResult.error != null) {
//                showLoginFailed(loginResult.error)
//            }
//            if (loginResult.success != null) {
//                updateUiWithUser(loginResult.success)
//            }
//            setResult(Activity.RESULT_OK)
//
//            //Complete and destroy login activity once successful
//            finish()
//        })
//
//        username.afterTextChanged {
//            loginViewModel.loginDataChanged(
//                username.text.toString(),
//                password.text.toString()
//            )
//        }
//
//        password.apply {
//            afterTextChanged {
//                loginViewModel.loginDataChanged(
//                    username.text.toString(),
//                    password.text.toString()
//                )
//            }
//
//            setOnEditorActionListener { _, actionId, _ ->
//                when (actionId) {
//                    EditorInfo.IME_ACTION_DONE ->
//                        loginViewModel.login(
//                            username.text.toString(),
//                            password.text.toString()
//                        )
//                }
//                false
//            }
//
//            login.setOnClickListener {
//                loading.visibility = View.VISIBLE
//                loginViewModel.login(username.text.toString(), password.text.toString())
//            }
//        }
//    }
//
//    private fun updateUiWithUser(model: LoggedInUserView) {
//        val welcome = getString(R.string.welcome)
//        val displayName = model.displayName
//        // TODO : initiate successful logged in experience
//        Toast.makeText(
//            applicationContext,
//            "$welcome $displayName",
//            Toast.LENGTH_LONG
//        ).show()
//    }
//
//    private fun showLoginFailed(@StringRes errorString: Int) {
//        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
//    }
//}
//
///**
// * Extension function to simplify setting an afterTextChanged action to EditText components.
// */
//fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
//    this.addTextChangedListener(object : TextWatcher {
//        override fun afterTextChanged(editable: Editable?) {
//            afterTextChanged.invoke(editable.toString())
//        }
//
//        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
//
//        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
//    })
//}
//
//
