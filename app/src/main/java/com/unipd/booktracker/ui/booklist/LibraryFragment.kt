package com.unipd.booktracker.ui.booklist

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.PopupMenu
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.unipd.booktracker.BookAdapter
import com.unipd.booktracker.R
import com.unipd.booktracker.databinding.FragmentLibraryBinding
import com.unipd.booktracker.db.OrderColumns

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
        // Inflate the layout for this fragment
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                true
            }
            R.id.action_sort -> {
                // Initializing the popup menu and giving the reference as current context
                val popupMenu = PopupMenu(requireActivity(), requireActivity().findViewById(item.itemId))
                // Inflating popup menu from popup_menu.xml file
                popupMenu.menuInflater.inflate(R.menu.sorting_menu, popupMenu.menu)
                popupMenu.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.action_byTitleAsc -> {
                            orderColumn = OrderColumns.title
                            asc = true
                            updateFilters()
                            true
                        }
                        R.id.action_byTitleDesc -> {
                            orderColumn = OrderColumns.title
                            asc = false
                            updateFilters()
                            true
                        }
                        R.id.action_byAuthorAsc -> {
                            orderColumn = OrderColumns.author
                            asc = true
                            updateFilters()
                            true
                        }
                        R.id.action_byAuthorDesc -> {
                            orderColumn = OrderColumns.author
                            asc = false
                            updateFilters()
                            true
                        }
                        R.id.action_byYearAsc -> {
                            orderColumn = OrderColumns.year
                            asc = true
                            updateFilters()
                            true
                        }
                        R.id.action_byYearDesc -> {
                            orderColumn = OrderColumns.year
                            asc = false
                            updateFilters()
                            true
                        }
                        R.id.action_byReadProgressAsc -> {
                            orderColumn = OrderColumns.progress
                            asc = true
                            updateFilters()
                            true
                        }
                        R.id.action_byReadProgressDesc -> {
                            orderColumn = OrderColumns.progress
                            asc = false
                            updateFilters()
                            true
                        }
                        else -> super.onOptionsItemSelected(item)
                    }
                }
                // Showing the popup menu
                popupMenu.show()
                true
            }
            R.id.action_settings -> {
                findNavController().navigate(R.id.navigation_settings)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}