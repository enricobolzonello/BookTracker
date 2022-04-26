package com.unipd.booktracker

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.unipd.booktracker.db.Book
import com.unipd.booktracker.databinding.BookCardBinding

class BookAdapter : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    private var library : List<Book> = listOf()

    fun setBooks(books : List<Book>) {
        library = books
    }

    // Describes an item view and its place within the RecyclerView
    inner class BookViewHolder(private val binding: BookCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(book: Book) {
            binding.tvBookTitle.text = book.title
            binding.tvBookAuthor.text = book.author
            binding.ivBookThumbnail.setImageBitmap(BitmapFactory.decodeFile(book.thumbnailPath))
            if (book.readPages == null)
                binding.pbRead.visibility = View.GONE
            else {
                binding.pbRead.progress = (book.readPages.toDouble() / book.pages.toDouble() * 100).toInt()
            }

            binding.cvBook.setOnClickListener {
                //TODO: put safeArgs
                binding.root.findNavController().navigate(R.id.navigation_book_detail)
            }
        }
    }

    // Returns a new ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = BookCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookViewHolder(binding)
    }

    // Returns size of data list
    override fun getItemCount(): Int {
        return library.size
    }

    // Displays data at a certain position
    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(library[position])
    }
}