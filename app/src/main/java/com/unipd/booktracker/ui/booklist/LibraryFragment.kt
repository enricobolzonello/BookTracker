package com.unipd.booktracker.ui.booklist

import android.os.Bundle
import android.view.*
import com.google.android.material.chip.Chip
import com.unipd.booktracker.R
import com.unipd.booktracker.db.OrderColumn
import com.unipd.booktracker.util.isSideBySideMode

class LibraryFragment : BookListFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // If side by side mode is active, read pages update from the book detail
        // need to be displayed immediately in the library view
        if (requireContext().isSideBySideMode())
            viewModel.getObservableLibrary().observe(this) { updateFilters() }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            chGroup.visibility = View.VISIBLE
            chNotRead.isChecked = prefs.getBoolean(getString(R.string.not_read_books_key), true)
            chNotRead.setOnClickListener {
                prefs.edit().putBoolean(getString(R.string.not_read_books_key), (it as Chip).isChecked).apply()
                updateFilters()
            }
            chReading.isChecked = prefs.getBoolean(getString(R.string.reading_books_key), true)
            chReading.setOnClickListener {
                prefs.edit().putBoolean(getString(R.string.reading_books_key), (it as Chip).isChecked).apply()
                updateFilters()
            }
            chRead.isChecked = prefs.getBoolean(getString(R.string.read_books_key), true)
            chRead.setOnClickListener {
                prefs.edit().putBoolean(getString(R.string.read_books_key), (it as Chip).isChecked).apply()
                updateFilters()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.action_by_progress).isVisible = true
    }

    override fun updateFilters() {
        val books = viewModel.getFilteredLibrary(
            query,
            prefs.getBoolean(getString(R.string.not_read_books_key), true),
            prefs.getBoolean(getString(R.string.reading_books_key), true),
            prefs.getBoolean(getString(R.string.read_books_key), true),
            prefs.getString(getString(R.string.sorting_column_key), OrderColumn.Title.name)!!,
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