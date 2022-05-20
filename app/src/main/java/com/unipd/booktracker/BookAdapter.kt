package com.unipd.booktracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.unipd.booktracker.db.Book
import com.unipd.booktracker.databinding.BookCardBinding
import com.unipd.booktracker.ui.bookdetail.BookDetailFragment

class BookAdapter(
    val listFragment: Fragment,
    val detailFragment: BookDetailFragment? = null
): RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    private var library: List<Book> = listOf()

    // Describes an item view and its place within the RecyclerView
    inner class BookViewHolder(private val binding: BookCardBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(book: Book) {
            binding.tvBookTitle.text = book.title
            binding.tvBookAuthor.text = book.mainAuthor

            if (book.thumbnail == null)
                binding.ivBookThumbnail.setBackgroundResource(R.drawable.default_thumbnail)
            else
                binding.ivBookThumbnail.setImageBitmap(BookTrackerUtils.toBitmap(book.thumbnail))

            if (book.readPages == null)
                binding.llReadProgress.visibility = View.GONE
            else {
                val progress = (book.readPages.toDouble() / book.pages.toDouble() * 100).toInt()
                binding.piReadProgress.progress = progress
                binding.tvReadProgress.text = listFragment.getString(R.string.ph_percentage, progress)
            }

            binding.cvBook.setOnClickListener {
                if (detailFragment != null)
                    detailFragment.setBook(book)
                else {
                    val bundle = Bundle()
                    bundle.putSerializable("chosenBook", book)
                    listFragment.findNavController().navigate(R.id.navigation_book_detail, bundle)
                }
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

    fun setBooks(books: List<Book>) {
        library = books
        notifyDataSetChanged() // Needed to update the adapter list
    }

    fun getBookAt(position: Int): Book {
        return library[position]
    }

    fun clearBookDetail() {
        detailFragment?.clearBookInfo()
    }
}