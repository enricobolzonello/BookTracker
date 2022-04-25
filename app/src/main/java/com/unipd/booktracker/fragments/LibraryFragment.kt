package com.unipd.booktracker.fragments

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
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.unipd.booktracker.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LibraryFragment : Fragment() {
    private lateinit var viewModel: BookViewModel
    private lateinit var libraryAdapter : BookAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
        viewModel = ViewModelProvider(requireActivity() as MainActivity)[BookViewModel::class.java]
        libraryAdapter = BookAdapter()

        lifecycleScope.launch(Dispatchers.IO) {
            // Execute on IO thread because of network and database requests

            if (viewModel.librarySize() == 0)
                viewModel.getBooksFromQuery("flowers")
            libraryAdapter.setBooks(viewModel.getLibrary())
            withContext(Dispatchers.Main) {
                // Execute on Main thread

                val recyclerView = view?.findViewById<RecyclerView>(R.id.rw_library)
                recyclerView?.adapter = libraryAdapter
                viewModel.getObservableLibrary().observe(requireActivity()) {
                    libraryAdapter.notifyItemInserted(it.size - 1)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val recyclerView = view?.findViewById<RecyclerView>(R.id.rw_library)
        recyclerView?.adapter = libraryAdapter
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_library, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fab = view.findViewById<ExtendedFloatingActionButton>(R.id.fab)
        val recyclerView = view.findViewById<RecyclerView>(R.id.rw_library)

        // Set appropriate padding to recycler view's bottom, otherwise fab will cover the last item
        fab.measure(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        recyclerView.setPadding(0,0,0,fab.measuredHeight + fab.marginBottom)
        recyclerView.clipToPadding = false

        // Extend and reduce FAB on scroll
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!recyclerView.canScrollVertically(-1))
                    fab.extend()
                else
                    fab.shrink()
                super.onScrollStateChanged(recyclerView, newState)
            }
        })

        // tapping card gets to fragment_book_detail
        val card = view.findViewById<MaterialCardView>(R.id.cv_book)
        card.setOnClickListener { findNavController().navigate(R.id.action_navigation_library_to_navigation_book_detail) }
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