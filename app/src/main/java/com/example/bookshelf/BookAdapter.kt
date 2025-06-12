package com.example.bookshelf

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

interface BookActionListener {
    fun onDelete(book: Book, position: Int)
}

class BookAdapter (
    private var bookList: MutableList<Book>,
    private val onDeleteClick: (Int) -> Unit,
    private val onEditClick: (Book) -> Unit
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {
    class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtTitle: TextView = itemView.findViewById(R.id.txtTitle)
        val txtAuthor: TextView = itemView.findViewById(R.id.txtAuthor)
        val txtYear: TextView = itemView.findViewById(R.id.txtYear)
        val txtGenre: TextView = itemView.findViewById(R.id.txtGenre)
        val txtUrl: TextView = itemView.findViewById(R.id.txtUrl)
        val btnEdit: ImageView = itemView.findViewById(R.id.btnEdit)
        val btnDelete: ImageView = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = bookList[position]

        holder.txtTitle.text = book.title
        holder.txtAuthor.text = "Penulis: ${book.author}"
        holder.txtYear.text = "Tahun: ${book.published_year}"
        holder.txtGenre.text = "Genre: ${book.genre}"
        holder.txtUrl.text = "Url: ${book.cover_url}"

        // Fungsi delete dan edit nanti
        holder.btnDelete.setOnClickListener {
            onDeleteClick(book.id)
        }

        holder.btnEdit.setOnClickListener {
            onEditClick(book)
        }
    }

    override fun getItemCount(): Int = bookList.size

    fun updateData(newBooks: List<Book>) {
        bookList = newBooks.toMutableList()
        notifyDataSetChanged()
    }

    fun removeAt(pos: Int) {
        bookList.removeAt(pos)
        notifyItemRemoved(pos)
    }
}