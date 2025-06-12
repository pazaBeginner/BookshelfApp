package com.example.bookshelf

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.View
import android.widget.TextSwitcher
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject

class BooksUser : AppCompatActivity() {
    private lateinit var recyclerBooks: RecyclerView
    private lateinit var txtEmpty: TextView
    private lateinit var bookAdapter: BookAdapter
    private val bookList = mutableListOf<Book>()
    private lateinit var username: String
    private lateinit var progressBar: View

    // Variable form edit
    private lateinit var popupFormEdit: View
    private lateinit var inputTitleD: TextView
    private lateinit var inputAuthorD: TextView
    private lateinit var inputYearD: TextView
    private lateinit var inputGenreD: TextView
    private lateinit var inputCoverUrlD: TextView
    private lateinit var btnSubmitD: View
    private lateinit var btnCloseD: View
    private var editingBookId: Int? = null
    val ipAddres = "10.199.99.81"

    private lateinit var inputSearch: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_books_user)
        recyclerBooks = findViewById(R.id.recyclerBooks)
        txtEmpty = findViewById(R.id.txtEmpty)

        bookAdapter = BookAdapter(
            bookList.toMutableList(),
            onDeleteClick = { bookId -> deleteBook(bookId) },
            onEditClick   = { book -> showEditPopup(book) }
        )
        recyclerBooks.layoutManager = LinearLayoutManager(this)
        recyclerBooks.adapter = bookAdapter

        progressBar = findViewById(R.id.progressBar)

        username = intent.getStringExtra("username") ?: ""

        // Variable form edit
        popupFormEdit = findViewById(R.id.popupFormEdit)
        inputTitleD = findViewById(R.id.inputTitleD)
        inputAuthorD = findViewById(R.id.inputAuthorD)
        inputGenreD = findViewById(R.id.inputGenreD)
        inputYearD = findViewById(R.id.inputYearD)
        inputCoverUrlD = findViewById(R.id.inputCoverUrlD)

        // Variable inputSearch
        inputSearch = findViewById(R.id.inputSearch)

        btnSubmitD = findViewById(R.id.btnSubmitD)
        btnCloseD = findViewById(R.id.btnCloseD)

        btnCloseD.setOnClickListener {
            popupFormEdit.visibility = View.GONE
        }

        btnSubmitD.setOnClickListener {
            updateBook()
        }

        inputSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                if (query.isEmpty()) {
                    fetchBooks() // Kembali tampilkan semua buku
                } else {
                    searchBooks(query) // Panggil fungsi pencarian
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        fetchBooks()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun showEditPopup(book: Book) {
        editingBookId = book.id
        inputTitleD.text = book.title
        inputAuthorD.text = book.author
        inputYearD.text = book.published_year.toString()
        inputGenreD.text = book.genre
        inputCoverUrlD.text = book.cover_url
        popupFormEdit.visibility = View.VISIBLE
    }

    private fun fetchBooks() {
        val url = "http://$ipAddres/backendBookshelf/$username/books"

        progressBar.visibility = View.VISIBLE
        txtEmpty.visibility = View.GONE
        recyclerBooks.visibility = View.GONE

        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                progressBar.visibility = View.GONE
                val success = response.getBoolean("success")
                if (success) {
                    val booksArray = response.getJSONArray("books")
                    bookList.clear()

                    for (i in 0 until booksArray.length()) {
                        val item = booksArray.getJSONObject(i)
                        val book = Book(
                            item.getInt("id"),
                            item.getString("title"),
                            item.getString("author"),
                            item.getInt("published_year"),
                            item.getString("genre"),
                            item.getString("cover_url"),
                            item.getString("created_at")
                        )
                        bookList.add(book)
                    }

                    bookAdapter.updateData(bookList)
                    recyclerBooks.visibility = View.VISIBLE
                    txtEmpty.visibility = if (bookList.isEmpty()) View.VISIBLE else View.GONE
                }
            },
            {
                progressBar.visibility = View.GONE
                txtEmpty.text = "Gagal memuat data"
                txtEmpty.visibility = View.VISIBLE
            })

        Volley.newRequestQueue(this).add(request)
    }

    private fun deleteBook(bookId: Int) {
        val url = "http://$ipAddres/backendBookshelf/$username/books/$bookId"

        val request = object : StringRequest(
            Method.DELETE, url,
            { response ->
                Toast.makeText(this, "Buku berhasil dihapus", Toast.LENGTH_SHORT).show()
                fetchBooks() // Refresh list setelah hapus
            },
            { error ->
                Toast.makeText(this, "Gagal menghapus buku", Toast.LENGTH_SHORT).show()
            }
        ) {}

        Volley.newRequestQueue(this).add(request)
    }

    private fun updateBook() {
        val id = editingBookId ?: return
        val url = "http://$ipAddres/backendBookshelf/$username/books/$id"

        val title = inputTitleD.text.toString()
        val author = inputAuthorD.text.toString()
        val yearText = inputYearD.text.toString()
        val genre = inputGenreD.text.toString()
        val coverUrl = inputCoverUrlD.text.toString()

        // Cek tahun valid
        val publishedYear = yearText.toIntOrNull()
        if (publishedYear == null) {
            Toast.makeText(this, "Tahun tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        val params = HashMap<String, String>()
        params["title"] = title
        params["author"] = author
        params["published_year"] = publishedYear.toString()
        params["genre"] = genre
        params["cover_url"] = coverUrl

        val request = object : StringRequest(
            Method.PUT, url, // Gunakan PUT jika backend kamu mendukung
            {
                Toast.makeText(this, "Buku berhasil diperbarui", Toast.LENGTH_SHORT).show()
                popupFormEdit.visibility = View.GONE
                fetchBooks()
            },
            {
                Toast.makeText(this, "Gagal memperbarui buku", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> = params
        }

        Volley.newRequestQueue(this).add(request)
    }

    private fun searchBooks(query: String) {
        val encodedQuery = Uri.encode(query)
        val url = "http://$ipAddres/backendBookshelf/$username/books?q=$encodedQuery"

        progressBar.visibility = View.VISIBLE
        txtEmpty.visibility = View.GONE
        recyclerBooks.visibility = View.GONE

        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                progressBar.visibility = View.GONE
                val success = response.getBoolean("success")
                if (success) {
                    val booksArray = response.getJSONArray("books")
                    bookList.clear()

                    for (i in 0 until booksArray.length()) {
                        val item = booksArray.getJSONObject(i)
                        val book = Book(
                            item.getInt("id"),
                            item.getString("title"),
                            item.getString("author"),
                            item.getInt("published_year"),
                            item.getString("genre"),
                            item.getString("cover_url"),
                            item.getString("created_at")
                        )
                        bookList.add(book)
                    }

                    bookAdapter.updateData(bookList)
                    recyclerBooks.visibility = View.VISIBLE
                    txtEmpty.visibility = if (bookList.isEmpty()) View.VISIBLE else View.GONE
                }
            },
            {
                progressBar.visibility = View.GONE
                txtEmpty.text = "Gagal memuat hasil pencarian"
                txtEmpty.visibility = View.VISIBLE
            })

        Volley.newRequestQueue(this).add(request)
    }

}