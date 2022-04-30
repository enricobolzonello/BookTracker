package com.unipd.booktracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.unipd.booktracker.db.Book
import com.unipd.booktracker.databinding.BookCardBinding
import com.unipd.booktracker.fragments.LibraryFragmentDirections

class BookAdapter(val parentFragment: Fragment): RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    private var library: List<Book> = listOf()

    fun setBooks(books: List<Book>) {
        library = books
        notifyDataSetChanged()
    }

    // Describes an item view and its place within the RecyclerView
    inner class BookViewHolder(private val binding: BookCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(book: Book) {
            binding.tvBookTitle.text = book.title
            binding.tvBookAuthor.text = book.mainAuthor
            if (book.thumbnail == null)
                binding.ivBookThumbnail.setBackgroundResource(R.drawable.default_thumbnail)
            else
                binding.ivBookThumbnail.setImageBitmap(book.thumbnail)
            if (book.readPages == null)
                binding.pbRead.visibility = View.GONE
            else
                binding.pbRead.progress = (book.readPages.toDouble() / book.pages.toDouble() * 100).toInt()

            binding.cvBook.setOnClickListener {
                val bundle = Bundle()
                bundle.putSerializable("chosenBook",book)
                parentFragment.findNavController().navigate(R.id.navigation_book_detail, bundle)
            }
        }
    }

    // Returns a new ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        //navController = (parent.context as AppCompatActivity).findNavController()
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