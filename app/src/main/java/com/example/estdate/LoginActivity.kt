package com.example.estdate

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        changeFragment(LoginMainFragment())

        // Initialize Firebase Auth
        auth = Firebase.auth

        supportActionBar?.hide()
    }

    fun onSignInBtnClick(view: View){
        val emailField = findViewById<EditText>(R.id.editTextTextEmailAddress)
        val passwordField = findViewById<EditText>(R.id.editTextTextPassword)

        auth.signInWithEmailAndPassword(emailField.text.toString(), passwordField.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    if(auth.currentUser?.isEmailVerified == true)
                        finish()

                    else
                        showErrorSnackbar(view, "E-posta doğrulaması yapılmamış. Giriş yapabilmek için lütfen e-posta adresinize gelen link üzerinden doğrulayınız.")
                } else {
                    showErrorSnackbar(view, "E-Posta veya şifre yanlış tekrar deneyiniz.")
                }
            }
    }

    fun onSignUpBtnClick(view: View){
        val intent = Intent(this, SignupActivity::class.java)
        startActivity(intent)
    }

    fun onForgotPasswordBtnClick(view: View){
        changeFragment(LoginForgotPasswordFragment())
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


    fun showSuccessSnackbar(view: View, message: String){
        val snack: Snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
        val view = snack.view
        val params = view.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.TOP
        view.layoutParams = params
        view.setBackgroundColor(Color.GREEN)
        snack.show()
    }

    fun changeFragment(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.loginFrameLayout, fragment)
        fragmentTransaction.commit()
    }

    fun onResetPasswordBtnClick(view: View){
        val emailText = findViewById<EditText>(R.id.editTextResetPasswordEmail).text.toString()
        auth.sendPasswordResetEmail(emailText).addOnCompleteListener(this){
            if(it.isSuccessful){
                showSuccessSnackbar(view, "E-postanıza şifre sıfırlama bağlantısı başarıyla gönderilmiştir.")
            }
            else
                showErrorSnackbar(view, "Şifre sıfırlama bağlantısı gönderirken bir hatayla karşılaştık. Lütfen daha sonra tekrar deneyiniz.")
        }
    }
}