package com.unipd.booktracker.ui.booklist

import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.unipd.booktracker.BookAdapter
import com.unipd.booktracker.R
import com.unipd.booktracker.databinding.FragmentLibraryBinding

class LibraryFragment: BooklistFragment() {

    override lateinit var bookAdapter: BookAdapter
    override lateinit var rw: RecyclerView
    override lateinit var fab: ExtendedFloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        bookAdapter = BookAdapter(this)
        viewModel.getObservableLibrary().observe(this) {
            updateFilters()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)
        rw = (binding as FragmentLibraryBinding).rwLibrary
        fab = (binding as FragmentLibraryBinding).fab
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (binding as FragmentLibraryBinding).chNotRead.setOnClickListener { updateFilters() }
        (binding as FragmentLibraryBinding).chReading.setOnClickListener { updateFilters() }
        (binding as FragmentLibraryBinding).chRead.setOnClickListener { updateFilters() }
    }

    override fun updateFilters() {
        val books = viewModel.getFilteredLibrary(
            query,
            (binding as FragmentLibraryBinding).chNotRead.isChecked,
            (binding as FragmentLibraryBinding).chReading.isChecked,
            (binding as FragmentLibraryBinding).chRead.isChecked, orderColumn,
            asc
        )
        bookAdapter.setBooks(books)
    }

    override fun setMenuGroupVisibility(menu: Menu) {
        menu.setGroupVisible(R.id.library_sorting_group, true)
    }
}