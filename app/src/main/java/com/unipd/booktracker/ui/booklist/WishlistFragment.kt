package com.unipd.booktracker.ui.booklist

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.PopupMenu
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.unipd.booktracker.BookAdapter
import com.unipd.booktracker.R
import com.unipd.booktracker.databinding.FragmentWishlistBinding
import com.unipd.booktracker.db.OrderColumns

class WishlistFragment : BooklistFragment() {

    override lateinit var bookAdapter: BookAdapter
    override lateinit var rw: RecyclerView
    override lateinit var fab: ExtendedFloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bookAdapter = BookAdapter(this)
        viewModel.getObservableWishlist().observe(this) {
            updateFilters()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWishlistBinding.inflate(inflater, container, false)
        rw = (binding as FragmentWishlistBinding).rwWishlist
        fab = (binding as FragmentWishlistBinding).fab
        return binding.root
    }

    override fun updateFilters() {
        val books = viewModel.getFilteredWishlist(query, orderColumn, asc)
        bookAdapter.setBooks(books)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                true
            }
            R.id.action_sort -> {
                // Initializing the popup menu and giving the reference as current context
                val popupMenu =
                    PopupMenu(requireActivity(), requireActivity().findViewById(item.itemId))
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