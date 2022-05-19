package com.unipd.booktracker.ui.booklist

import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.unipd.booktracker.BookAdapter
import com.unipd.booktracker.R
import com.unipd.booktracker.databinding.FragmentLibraryBinding
import com.unipd.booktracker.databinding.FragmentWishlistBinding
import com.unipd.booktracker.db.OrderColumn

class WishlistFragment: BooklistFragment() {

    override lateinit var rw: RecyclerView
    override lateinit var fab: ExtendedFloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
        fab = (binding as FragmentWishlistBinding).fabAddBook
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        val orderColumn = prefs.getString(getString(R.string.sorting_column_key), OrderColumn.title.name)
        // In wishlist page progress sorting it's not available,
        // if it has been selected from library it's changed to title sorting
        if (orderColumn == OrderColumn.progress.name)
            menu.findItem(R.id.action_by_title).isChecked = true
    }

    override fun updateFilters() {
        var orderColumn = prefs.getString(getString(R.string.sorting_column_key), OrderColumn.title.name)!!
        // In wishlist page progress sorting it's not available,
        // if it has been selected from library it's changed to title sorting
        if (orderColumn == OrderColumn.progress.name)
            orderColumn = OrderColumn.title.name
        val books = viewModel.getFilteredWishlist(
            query,
            orderColumn,
            prefs.getBoolean(getString(R.string.sorting_asc_key), true)
        )
        bookAdapter.setBooks(books)

        (binding as FragmentWishlistBinding).tvEmptyListPlaceholder.visibility =
            if (books.isEmpty())
                View.VISIBLE
            else
                View.GONE
    }
}