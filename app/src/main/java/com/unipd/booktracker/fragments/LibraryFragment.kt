package com.unipd.booktracker.fragments

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.core.view.marginBottom
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.unipd.booktracker.*
import com.unipd.booktracker.databinding.FragmentLibraryBinding
import com.unipd.booktracker.db.OrderColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class LibraryFragment: Fragment() {
    private lateinit var binding: FragmentLibraryBinding
    private lateinit var viewModel: BookViewModel
    private lateinit var bookAdapter : BookAdapter

    private var query = ""
    private var orderColumn = OrderColumns.title
    private var asc = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        binding = FragmentLibraryBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(requireActivity() as MainActivity)[BookViewModel::class.java]
        bookAdapter = BookAdapter(this)

        lifecycleScope.launch(Dispatchers.IO) {
            // Execute on IO thread because of network and database requests
            updateFilters()
            withContext(Dispatchers.Main) {
                // Execute on Main thread
                binding.rwLibrary.adapter = bookAdapter
                viewModel.getObservableLibrary().observe(requireActivity()) {
                    bookAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.rwLibrary.adapter = bookAdapter
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.chNotRead.setOnClickListener { updateFilters() }
        binding.chReading.setOnClickListener { updateFilters() }
        binding.chRead.setOnClickListener { updateFilters() }

        // Set appropriate padding to recycler view's bottom, otherwise fab will cover the last item
        binding.fab.measure(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        binding.rwLibrary.setPadding(0,0,0,binding.fab.measuredHeight + binding.fab.marginBottom)
        binding.rwLibrary.clipToPadding = false

        // Extend and reduce FAB on scroll
        binding.rwLibrary.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!recyclerView.canScrollVertically(-1))
                    binding.fab.extend()
                else
                    binding.fab.shrink()
                super.onScrollStateChanged(recyclerView, newState)
            }
        })

        binding.fab.setOnClickListener {
            val dialog = AddDialogFragment()
            dialog.show(childFragmentManager, R.string.title_add_book.toString())
        }
    }

    private fun updateFilters() {
        lifecycleScope.launch(Dispatchers.IO) {
            // Execute on IO thread because of database requests
            val books = viewModel.getFilteredLibrary(
                query,
                binding.chNotRead.isChecked,
                binding.chReading.isChecked,
                binding.chRead.isChecked, orderColumn,
                asc
            )
            withContext(Dispatchers.Main) {
                // Execute on Main thread
                bookAdapter.setBooks(books)
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

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(submittedText: String): Boolean {
                // Hiding the keyboard after typing has ended
                val imm = searchView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(searchView.windowToken, 0)
                searchView.clearFocus()

                query = submittedText
                updateFilters()
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                query = newText
                updateFilters()
                return true
            }
        })

        searchView.setOnCloseListener(object : SearchView.OnCloseListener {
            override fun onClose(): Boolean {
                query = ""
                return false
            }
        })
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