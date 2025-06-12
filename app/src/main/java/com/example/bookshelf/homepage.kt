package com.example.bookshelf

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.TextView
import android.widget.Button
import android.widget.ImageView
import android.view.View
import android.widget.EditText
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import android.view.animation.AnimationUtils
import android.util.Log
import androidx.cardview.widget.CardView

class homepage : AppCompatActivity() {
    private val client = OkHttpClient()
    val ipAddres = "10.199.99.81"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_homepage)

        val textUsername = findViewById<TextView>(R.id.textUsernameH)

        // Ambil data dari Intent
        val username = intent.getStringExtra("username")
        textUsername.text = username ?: "User"

        if(username != null) {
            fetchUserData(username)
        }

        val btnPreviewBooks = findViewById<CardView>(R.id.cardPreviewBook)

        // Popup form tambah buku
        val popupForm = findViewById<View>(R.id.popupForm)
        val btnAddBook = findViewById<CardView>(R.id.cardAddBook)
        val btnClose = findViewById<Button>(R.id.btnClose)

        // Input di form tambah buku
        val inputTitle = findViewById<EditText>(R.id.inputTitle)
        val inputAuthor = findViewById<EditText>(R.id.inputAuthor)
        val inputYear = findViewById<EditText>(R.id.inputYear)
        val inputGenre = findViewById<EditText>(R.id.inputGenre)
        val inputCoverUrl = findViewById<EditText>(R.id.inputCoverUrl)
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)

        // Button Logout
        val btnLogout = findViewById<ImageView>(R.id.btnLogOut)

        // Animasi transisi
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnLogout.setOnClickListener {
//            val prefs = getSharedPreferences("USER", MODE_PRIVATE)
//            val username = prefs.getString("USERNAME", "") ?: ""
            val logoutUrl = "http://$ipAddres/backendBookshelf/$username/logout"

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val request = Request.Builder().url(logoutUrl).build()
                    val response = client.newCall(request).execute()

                    val body = response.body?.string()
                    Log.d("LOGOUT", "Response: $body")

                    if(response.isSuccessful) {
                        runOnUiThread {
                            Toast.makeText(applicationContext, "Logout berhasil", Toast.LENGTH_SHORT).show()

                            // ✅ Hapus session lokal
                            val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
                            sharedPref.edit().clear().apply()

                            // Kembali ke Sign in page
                            val intent = Intent(this@homepage, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(applicationContext, "Gagal Logout", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("LOGOUT", "error: ${e.message}")
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Terjadi kesalahan di server", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        btnAddBook.setOnClickListener {
            popupForm.visibility = View.VISIBLE
            popupForm.startAnimation(fadeIn)
        }
        // Kirim form tambah buku
        btnSubmit.setOnClickListener {
            val title = inputTitle.text.toString()
            val author = inputAuthor.text.toString()
            val year = inputYear.text.toString()
            val genre = inputGenre.text.toString()
            val coverUrl = inputCoverUrl.text.toString()

            if(title.isBlank() || author.isBlank() || year.isBlank() || genre.isBlank() || coverUrl.isBlank()) {
                Toast.makeText(this, "Semua data harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val formBody = okhttp3.FormBody.Builder()
                        .add("title", title)
                        .add("author", author)
                        .add("published_year", year)
                        .add("genre", genre)
                        .add("cover_url", coverUrl)
                        .build()

                    val request = Request.Builder()
                        .url("http://$ipAddres/backendBookshelf/$username/books")
                        .post(formBody)
                        .build()

                    val response = client.newCall(request).execute()
                    val responseBody = response.body?.string()

                    if (response.isSuccessful && responseBody != null) {
                        val json = JSONObject(responseBody)
                        if(json.getBoolean("success")) {
                            runOnUiThread {
                                Toast.makeText(this@homepage, "Berhasil menambahkan buku", Toast.LENGTH_SHORT).show()
                                inputTitle.text.clear()
                                inputAuthor.text.clear()
                                inputYear.text.clear()
                                inputGenre.text.clear()
                                inputCoverUrl.text.clear()
                                popupForm.startAnimation(fadeOut)
                                popupForm.postDelayed({
                                    popupForm.visibility = View.GONE
                                }, 300)
                            }
                        } else {
                            runOnUiThread {
                                Toast.makeText(this@homepage, "Gagal: ${json.getString("message")}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@homepage, "Gagal menambahkan buku", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@homepage, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        btnClose.setOnClickListener {
            popupForm.startAnimation(fadeOut)
            popupForm.postDelayed({
                popupForm.visibility = View.GONE
            }, 300)
        }

        btnPreviewBooks.setOnClickListener {
            val destination = Intent(this@homepage, BooksUser::class.java)
            destination.putExtra("username", username)
            startActivity(destination)
        }
    }

    private fun fetchUserData(username: String) {
        val url = "http://$ipAddres/backendBookshelf/$username"

        val request = Request.Builder()
            .url(url)
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("USER_FETCH", "Requesting: $url")
                val response = client.newCall(request).execute()
                val body = response.body?.string()

                Log.d("USER_FETCH", "Response code: ${response.code}") // ⬅️ Log kode respon
                Log.d("USER_FETCH", "Response body: $body")
                if(response.isSuccessful && body != null) {
                    val json = JSONObject(body)
                    if(json.getBoolean("success")) {
                        val user = json.getJSONObject("message")
                        val name = user.getString("name")
                        runOnUiThread {
                            findViewById<TextView>(R.id.textUsernameH).text = name
                        }
                    }
                } else {
                    runOnUiThread { Toast.makeText(applicationContext, "Gagal memuat data user", Toast.LENGTH_SHORT).show() }
                }

            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(applicationContext, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}