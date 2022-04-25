package com.unipd.booktracker.fragments

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import androidx.appcompat.widget.SearchView
import androidx.core.view.marginBottom
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.unipd.booktracker.*
import com.unipd.booktracker.db.OrderColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LibraryFragment : Fragment() {
    private lateinit var viewModel: BookViewModel
    private lateinit var bookAdapter : BookAdapter
    private var readFilter = false
    private var readingFilter = false
    private var notReadFilter = false
    private var orderColumn = OrderColumns.title
    private var asc = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
        viewModel = ViewModelProvider(requireActivity() as MainActivity)[BookViewModel::class.java]
        bookAdapter = BookAdapter()

        lifecycleScope.launch(Dispatchers.IO) {
            // Execute on IO thread because of network and database requests
            if (viewModel.librarySize() == 0)
                viewModel.getBooksFromQuery("flowers")
            updateFilters()
            withContext(Dispatchers.Main) {
                // Execute on Main thread
                val recyclerView = view?.findViewById<RecyclerView>(R.id.rw_library)
                recyclerView?.adapter = bookAdapter
                viewModel.getObservableLibrary().observe(requireActivity()) {
                    bookAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val chRead = view?.findViewById<Chip>(R.id.ch_read)
        chRead?.let { readFilter = it.isChecked }
        val chReading = view?.findViewById<Chip>(R.id.ch_reading)
        chReading?.let { readingFilter = it.isChecked }
        val chNotRead = view?.findViewById<Chip>(R.id.ch_not_read)
        chNotRead?.let { notReadFilter = it.isChecked }

        val rwLibrary = view?.findViewById<RecyclerView>(R.id.rw_library)
        updateFilters()
        rwLibrary?.let { it.adapter = bookAdapter }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_library, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fab = view.findViewById<ExtendedFloatingActionButton>(R.id.fab)
        val rwLibrary = view.findViewById<RecyclerView>(R.id.rw_library)

        // Set appropriate padding to recycler view's bottom, otherwise fab will cover the last item
        fab.measure(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        rwLibrary.setPadding(0,0,0,fab.measuredHeight + fab.marginBottom)
        rwLibrary.clipToPadding = false

        // Extend and reduce FAB on scroll
        rwLibrary.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!recyclerView.canScrollVertically(-1))
                    fab.extend()
                else
                    fab.shrink()
                super.onScrollStateChanged(recyclerView, newState)
            }
        })

        val chRead = view.findViewById<Chip>(R.id.ch_read)
        readFilter = chRead.isChecked
        chRead.setOnClickListener {
            readFilter = (it as Chip).isChecked
            updateFilters()
        }

        val chReading = view.findViewById<Chip>(R.id.ch_reading)
        readingFilter = chReading.isChecked
        chReading.setOnClickListener {
            readingFilter = (it as Chip).isChecked
            updateFilters()
        }

        val chNotRead = view.findViewById<Chip>(R.id.ch_not_read)
        notReadFilter = chNotRead.isChecked
        chNotRead.setOnClickListener {
            notReadFilter = (it as Chip).isChecked
            updateFilters()
        }
    }

    private fun updateFilters() {
        lifecycleScope.launch(Dispatchers.IO) {
            bookAdapter.setBooks(viewModel.getFilteredLibrary(readFilter, readingFilter, notReadFilter, orderColumn, asc))
            withContext(Dispatchers.Main){
                bookAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.list_action_menu, menu)
        inflater.inflate(R.menu.default_action_menu, menu)
        // Associate searchable configuration with the SearchView
        val searchManager = requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menu.findItem(R.id.action_search).actionView as SearchView
        searchView.apply {
            setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                true
            }
            R.id.action_sort -> {
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