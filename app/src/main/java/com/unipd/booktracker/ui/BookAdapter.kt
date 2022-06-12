package com.unipd.booktracker.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.unipd.booktracker.R
import com.unipd.booktracker.db.Book
import com.unipd.booktracker.databinding.BookItemBinding
import com.unipd.booktracker.ui.bookdetail.BookDetailFragment
import com.unipd.booktracker.util.toBitMap

class BookAdapter(
    val listFragment: Fragment,
    val detailFragment: BookDetailFragment? = null
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {
    private var books: List<Book> = listOf()

    inner class BookViewHolder(private val binding: BookItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(book: Book) {
            binding.tvBookTitle.text = book.title
            binding.tvBookAuthor.text = book.mainAuthor

            if (book.thumbnail == null)
                binding.ivBookThumbnail.setBackgroundResource(R.drawable.default_thumbnail)
            else
                binding.ivBookThumbnail.setImageBitmap(book.thumbnail.toBitMap())

            if (book.readPages == null)
                binding.llReadProgress.visibility = View.GONE
            else {
                val progress = (book.readPages.toDouble() / book.pages.toDouble() * 100).toInt()
                binding.piReadProgress.progress = progress
                binding.tvReadProgress.text = listFragment.getString(R.string.ph_percentage, progress.toString())
            }

            // Setting unique transition name for the item
            binding.cvBook.transitionName = listFragment.resources.getString(R.string.book_item_transition, book.id)

            binding.cvBook.setOnClickListener {
                if (detailFragment != null)
                    detailFragment.setBook(book)
                else {
                    val bundle = Bundle()
                    bundle.putSerializable("chosenBook", book)
                    val bookDetailTransitionName = listFragment.getString(R.string.book_detail_transition)
                    // Setting the shared element transition from CardView to BookDetailFragment
                    val extras = FragmentNavigatorExtras(binding.cvBook to bookDetailTransitionName)
                    listFragment.findNavController().navigate(R.id.navigation_book_detail, bundle, null, extras)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = BookItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return books.size
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(books[position])
    }

    // NotifyDataSetChanged needed in order to update the whole list of books
    @SuppressLint("NotifyDataSetChanged")
    fun setBooks(newBooks: List<Book>) {
        books = newBooks
        notifyDataSetChanged()
    }

    fun getBookAt(position: Int): Book {
        return books[position]
    }

    // If the list is displayed side by side with the book detail, if the book is deleted by swiping the card
    // the book detail view needs to be cleared
    fun clearBookDetail() {
        detailFragment?.setBook(null)
    }
}