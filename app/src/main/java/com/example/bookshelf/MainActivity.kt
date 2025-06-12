package com.example.bookshelf

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.TextView
import  android.widget.EditText
import android.widget.Button
import android.widget.*
import okhttp3.*
import kotlinx.coroutines.*
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private val client = OkHttpClient()
    val ipAddres = "10.199.99.81"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        val savedUsername = sharedPref.getString("username", null)

        if (savedUsername != null) {
            val intent = Intent(this, homepage::class.java)
            intent.putExtra("username", savedUsername)
            startActivity(intent)
            finish()
            return
        }
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val inputUsername = findViewById<EditText>(R.id.inputUsernameS)
        val inputPassword = findViewById<EditText>(R.id.inputPasswordS)
        val btnSignIn = findViewById<Button>(R.id.btnSignIn)
        val textSignUp = findViewById<TextView>(R.id.textSignUp)

        btnSignIn.setOnClickListener{
            val username = inputUsername.text.toString().trim()
            val password = inputPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Harap isi semua kolom", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginUser(username, password)
        }

        textSignUp.setOnClickListener {
            val destination = Intent(this@MainActivity, signup::class.java)
            startActivity(destination)
        }
    }

    private fun loginUser(username: String, password: String) {
        val formBody = FormBody.Builder()
            .add("name", username)
            .add("password", password)
            .build()

        val request = Request.Builder()
            .url("http://$ipAddres/backendBookshelf/login.php")
            .post(formBody)
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Gagal koneksi ke server", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseText = response.body?.string()
                    runOnUiThread {
                        if(response.isSuccessful && responseText?.contains("\"success\":true") == true) {
                            Toast.makeText(applicationContext, "Login berhasil", Toast.LENGTH_SHORT).show()

                            // Simpan riwayat login user
                            val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
                            val editor = sharedPref.edit()
                            editor.putString("username", username)
                            editor.apply()

                            // Navigasi ke halaman homepage
                            val intent = Intent(this@MainActivity, homepage::class.java)
                            intent.putExtra("username", username)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(applicationContext, "Login gagal: $responseText", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        }
    }
}