package com.unipd.booktracker.ui.booklist

import android.os.Bundle
import android.view.*
import com.unipd.booktracker.R
import com.unipd.booktracker.db.OrderColumn
import com.unipd.booktracker.util.isSideBySideMode

class WishlistFragment : BookListFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // If side by side mode is active, read pages update from the book detail
        // need to be displayed immediately in the wishlist view
        if (requireContext().isSideBySideMode())
            viewModel.getObservableWishlist().observe(this) { updateFilters() }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        val orderColumn = prefs.getString(getString(R.string.sorting_column_key), OrderColumn.Title.name)
        // In wishlist page progress sorting it's not available,
        // if it has been selected from library it's changed to title sorting
        if (orderColumn == OrderColumn.Progress.name)
            menu.findItem(R.id.action_by_title).isChecked = true
    }

    override fun updateFilters() {
        var orderColumn = prefs.getString(getString(R.string.sorting_column_key), OrderColumn.Title.name)!!
        // In wishlist page progress sorting it's not available,
        // if it has been selected from library it's changed to title sorting
        if (orderColumn == OrderColumn.Progress.name)
            orderColumn = OrderColumn.Title.name
        val books = viewModel.getFilteredWishlist(
            query,
            orderColumn,
            prefs.getBoolean(getString(R.string.sorting_asc_key), true)
        )
        bookAdapter.setBooks(books)

        binding.tvEmptyListPlaceholder.visibility =
            if (books.isEmpty())
                View.VISIBLE
            else
                View.GONE
    }
}