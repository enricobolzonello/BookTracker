package com.unipd.booktracker.ui.booklist

import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.unipd.booktracker.BookAdapter
import com.unipd.booktracker.databinding.FragmentWishlistBinding

class WishlistFragment : BooklistFragment() {

    override lateinit var bookAdapter: BookAdapter
    override lateinit var rw: RecyclerView
    override lateinit var fab: ExtendedFloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bookAdapter = BookAdapter(this)
        viewModel.getObservableWishlist().observe(this) {
            updateFilters()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWishlistBinding.inflate(inflater, container, false)
        rw = (binding as FragmentWishlistBinding).rwWishlist
        fab = (binding as FragmentWishlistBinding).fab
        return binding.root
    }

    override fun updateFilters() {
        val books = viewModel.getFilteredWishlist(query, orderColumn, asc)
        bookAdapter.setBooks(books)
    }
}