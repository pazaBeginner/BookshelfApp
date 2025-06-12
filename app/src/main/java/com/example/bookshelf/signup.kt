package com.example.bookshelf

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.*
import kotlinx.coroutines.*
import okhttp3.*
import java.io.IOException
import android.widget.TextView
import android.widget.Toast
import android.widget.Button
import android.widget.EditText

class signup : AppCompatActivity() {
    private val client = OkHttpClient()
    val ipAddres = "10.199.99.81 "
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup)
        val nameInput = findViewById<EditText>(R.id.inputUsernameU)
        val emailInput = findViewById<EditText>(R.id.inputEmailU)
        val passwordInput = findViewById<EditText>(R.id.inputPasswordU)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)
        val textBtnSignIn = findViewById<TextView>(R.id.textSignIn)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Navigasi ke halaman Sign in
        textBtnSignIn.setOnClickListener {
            val destination = Intent(this@signup, MainActivity::class.java)
            startActivity(destination)
        }

        btnSignUp.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if(name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Form harus diisi semua", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerUser(name, email, password)
        }
    }
    private fun registerUser(name: String, email: String, password: String) {
        val formBody = FormBody.Builder()
            .add("name", name)
            .add("email", email)
            .add("password", password)
            .build()

        val request = Request.Builder()
            .url("http://$ipAddres/backendBookshelf/register.php")
            .post(formBody)
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            client.newCall(request).enqueue(object: Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Gagal koneksi ke server", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseText = response.body?.string()

                    runOnUiThread {
                        if(response.isSuccessful && responseText?.contains("success\":true") == true) {
                            Toast.makeText(applicationContext, "Buatkan akun berhasil", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@signup, MainActivity::class.java))
                        } else {
                            Toast.makeText(applicationContext, "Gagal membuat akun: $responseText", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        }
    }
}