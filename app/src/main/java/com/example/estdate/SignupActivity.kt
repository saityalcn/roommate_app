package com.example.estdate

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignupActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        changeFragment(SingupGeneralInfosFragment())
        // Initialize Firebase Auth
        auth = Firebase.auth

        supportActionBar?.hide()


    }

    fun onSignUpBtnClick(view: View){
        val emailField = findViewById<EditText>(R.id.editTextTextEmailAddress)
        val passwordField = findViewById<EditText>(R.id.editTextTextPassword)

        auth.createUserWithEmailAndPassword(emailField.text.toString(), passwordField.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    if(auth.currentUser?.isEmailVerified != true)
                        auth.currentUser?.sendEmailVerification()

                    else
                        showErrorSnackbar(view, "E-posta doğrulaması yapılmamış. Giriş yapabilmek için lütfen e-posta adresinize gelen link üzerinden doğrulayınız.")
                } else {
                    showErrorSnackbar(view, "E-Posta veya şifre yanlış tekrar deneyiniz.")
                }
            }
        auth.currentUser?.sendEmailVerification()
    }

    fun showErrorSnackbar(view: View, message: String){
        val snack: Snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
        val view = snack.view
        val params = view.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.TOP
        view.layoutParams = params
        view.setBackgroundColor(Color.RED)
        snack.show()
    }

    fun changeFragment(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.signupFrameLayout, fragment)
        fragmentTransaction.commit()
    }
}