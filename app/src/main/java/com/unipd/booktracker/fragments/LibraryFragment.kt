package com.unipd.booktracker.fragments

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.view.marginBottom
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.unipd.booktracker.*
import com.unipd.booktracker.db.LibraryBook
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LibraryFragment : Fragment() {
    private lateinit var viewModel: BookViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        viewModel = ViewModelProvider(requireActivity() as MainActivity)[BookViewModel::class.java]

        lifecycleScope.launch(Dispatchers.IO) {
            //viewModel.getBooksFromQuery("flowers")
            val libraryAdapter = BookAdapter(viewModel.getLibrary())
            withContext(Dispatchers.Main) {
                val recyclerView = view?.findViewById<RecyclerView>(R.id.rw_library)
                recyclerView?.adapter = libraryAdapter
                viewModel.getObservableLibrary().observe(requireActivity(), object : Observer<List<LibraryBook>> {
                    override fun onChanged(notes: List<LibraryBook>) {
                        libraryAdapter.notifyDataSetChanged()
                    }
                })
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_action_menu, menu)
        inflater.inflate(R.menu.default_action_menu, menu)

        // Associate searchable configuration with the SearchView
        val searchManager = requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menu.findItem(R.id.action_search).actionView as SearchView
        searchView.apply {
            setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_library, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // extend and reduce FAB on scroll
        val fab = view.findViewById<ExtendedFloatingActionButton>(R.id.fab)
        val recyclerView = view.findViewById<RecyclerView>(R.id.rw_library)

        /* Should set appropriate padding to recycler view, otherwise fab will cover last item
        recyclerView.setPadding(0,0,0,fab.height + fab.marginBottom)
        recyclerView.clipToPadding = false
         */

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                // If view is static extend the fab
                if (!recyclerView.canScrollVertically(-1))
                    fab.extend()
                else
                    fab.shrink()
                super.onScrollStateChanged(recyclerView, newState)
            }
        })
    }
}