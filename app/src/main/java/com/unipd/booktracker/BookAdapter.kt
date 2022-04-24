package com.unipd.booktracker

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.unipd.booktracker.db.LibraryBook

class BookAdapter : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    private var library : List<LibraryBook> = listOf()

    // Describes an item view and its place within the RecyclerView
    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_book_title)
        private val tvAuthors: TextView = itemView.findViewById(R.id.tv_book_author)
        private val ivThumbnail: ImageView = itemView.findViewById(R.id.iv_book_thumbnail)

        fun bind(book: LibraryBook) {
            tvTitle.text = book.title
            tvAuthors.text = book.authors.joinToString(", ")
            ivThumbnail.setImageBitmap(BitmapFactory.decodeFile(book.thumbnailPath))
        }
    }

    // Returns a new ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.book_card, parent, false)
        return BookViewHolder(view)
    }

    // Returns size of data list
    override fun getItemCount(): Int {
        return library.size
    }

    // Displays data at a certain position
    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(library[position])
    }

    fun setBooks(books : List<LibraryBook>) {
        library = books
    }
}