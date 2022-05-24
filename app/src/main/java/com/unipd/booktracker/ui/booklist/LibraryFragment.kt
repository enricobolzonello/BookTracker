package com.unipd.booktracker.ui.booklist

import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.unipd.booktracker.R
import com.unipd.booktracker.databinding.FragmentLibraryBinding
import com.unipd.booktracker.db.OrderColumn
import com.unipd.booktracker.util.isSideBySideMode

class LibraryFragment : BooklistFragment() {
    override lateinit var rw: RecyclerView
    override lateinit var fab: ExtendedFloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // If side by side mode is active, read pages update from the book detail
        // need to be displayed immediately in the library view
        if (requireContext().isSideBySideMode())
            viewModel.getObservableLibrary().observe(this) { updateFilters() }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)
        rw = (binding as FragmentLibraryBinding).rwLibrary
        fab = (binding as FragmentLibraryBinding).fabAddBook
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        (binding as FragmentLibraryBinding).apply {
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
            prefs.getString(getString(R.string.sorting_column_key), OrderColumn.title.name)!!,
            prefs.getBoolean(getString(R.string.sorting_asc_key), true)
        )
        bookAdapter.setBooks(books)

        (binding as FragmentLibraryBinding).tvEmptyListPlaceholder.visibility =
            if (books.isEmpty())
                View.VISIBLE
            else
                View.GONE
    }
}