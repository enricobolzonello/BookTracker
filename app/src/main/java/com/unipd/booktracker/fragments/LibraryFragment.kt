package com.unipd.booktracker.fragments

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.core.view.marginBottom
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.unipd.booktracker.BookAdapter
import com.unipd.booktracker.BookViewModel
import com.unipd.booktracker.MainActivity
import com.unipd.booktracker.R
import com.unipd.booktracker.databinding.FragmentLibraryBinding
import com.unipd.booktracker.db.OrderColumns
import java.time.LocalDate

class LibraryFragment: Fragment() {
    private lateinit var viewModel: BookViewModel
    private lateinit var bookAdapter : BookAdapter
    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    private var query = ""
    private var orderColumn = OrderColumns.title
    private var asc = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        viewModel = ViewModelProvider(requireActivity() as MainActivity)[BookViewModel::class.java]
        bookAdapter = BookAdapter(this)

        viewModel.getObservableLibrary().observe(requireActivity()) {
            bookAdapter.notifyDataSetChanged()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rwLibrary.adapter = bookAdapter
        updateFilters()

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
            if (!(requireActivity() as MainActivity).isNetworkAvailable())
                Toast.makeText(requireActivity(),getString(R.string.network_errror), Toast.LENGTH_SHORT).show()
            else {
                val dialog = AddDialogFragment()
                dialog.show(childFragmentManager, getString(R.string.title_add_book))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateFilters() {
        val books = viewModel.getFilteredLibrary(
            query,
            binding.chNotRead.isChecked,
            binding.chReading.isChecked,
            binding.chRead.isChecked, orderColumn,
            asc
        )
        bookAdapter.setBooks(books)
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
                query = submittedText
                updateFilters()
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                query = newText
                updateFilters()
                return false
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