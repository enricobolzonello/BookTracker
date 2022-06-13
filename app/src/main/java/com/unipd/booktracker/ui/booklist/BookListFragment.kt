package com.unipd.booktracker.ui.booklist

import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.marginBottom
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialElevationScale
import com.unipd.booktracker.ui.BookAdapter
import com.unipd.booktracker.MainActivity
import com.unipd.booktracker.R
import com.unipd.booktracker.databinding.FragmentBookListBinding
import com.unipd.booktracker.db.OrderColumn
import com.unipd.booktracker.ui.addbook.AddBookDialogFragment
import com.unipd.booktracker.ui.bookdetail.BookDetailFragment

/*
    This Fragment is an abstract base class that implements the shared behavior of LibraryFragment and WishlistFragment
 */
abstract class BookListFragment : Fragment() {
    private var _binding: FragmentBookListBinding? = null
    private var detailFragment: BookDetailFragment? = null
    private var searchItem: MenuItem? = null

    protected lateinit var viewModel: BookListViewModel
    protected lateinit var bookAdapter: BookAdapter
    protected lateinit var prefs: SharedPreferences
    protected val binding get() = _binding!!
    protected var query = ""

    abstract fun updateFilters()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(requireActivity() as MainActivity)[BookListViewModel::class.java]
        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext().applicationContext)

        setHasOptionsMenu(true)

        // Entering transitions
        enterTransition = MaterialElevationScale(true).apply {
            duration = resources.getInteger(com.google.android.material.R.integer.material_motion_duration_short_2).toLong()
        }
        reenterTransition = MaterialElevationScale(true).apply {
            duration = resources.getInteger(com.google.android.material.R.integer.material_motion_duration_short_2).toLong()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Make sure that the correct app and bottom bar are displayed
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(false)
            title = getString(R.string.app_name)
        }
        (requireActivity() as MainActivity).setNavVisibility(View.VISIBLE)

        // The entering transition need to be postponed to be visible to the user
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

        detailFragment = childFragmentManager.findFragmentById(R.id.detail_container) as BookDetailFragment?
        bookAdapter = BookAdapter(this, detailFragment)
        binding.rwBookList.adapter = bookAdapter
        updateFilters()

        // Set appropriate padding to recycler view's bottom, otherwise fab will cover the last item
        binding.fabAddBook.measure(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        binding.rwBookList.setPadding(0, 0, 0, binding.fabAddBook.measuredHeight + binding.fabAddBook.marginBottom)
        binding.rwBookList.clipToPadding = false

        // Extend and reduce FAB on scroll
        binding.rwBookList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!recyclerView.canScrollVertically(-1))
                    binding.fabAddBook.extend()
                else
                    binding.fabAddBook.shrink()
                super.onScrollStateChanged(recyclerView, newState)
            }
        })

        binding.fabAddBook.setOnClickListener {
            // If network is not available show a retry Snackbar
            if (!(requireActivity() as MainActivity).isNetworkAvailable())
                Snackbar.make(requireView(), getString(R.string.network_error), Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.retry)) { binding.fabAddBook.callOnClick() }
                    .show()
            else {
                val dialog = AddBookDialogFragment()
                dialog.show(childFragmentManager, getString(R.string.add_book))
            }
        }

        //  Swipe to delete item
        val swipeTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            var background: Drawable? = ColorDrawable(Color.RED)
            var icDelete: Drawable? = context?.let { ContextCompat.getDrawable(it, R.drawable.ic_delete) }
            var icDeleteMargin = resources.getDimension(R.dimen.content_margin)

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val book = bookAdapter.getBookAt(viewHolder.adapterPosition)
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(resources.getString(R.string.delete_book_dialog_title))
                    .setMessage(resources.getString(R.string.delete_book_dialog_message, book.title))
                    .setIcon(ResourcesCompat.getDrawable(resources, R.drawable.ic_delete, requireContext().theme))
                    .setNegativeButton(resources.getString(R.string.no)) { _, _ ->
                        updateFilters()
                    }
                    .setPositiveButton(resources.getString(R.string.yes)) { _, _ ->
                        viewModel.removeBook(book)
                        bookAdapter.clearBookDetail()
                        Toast.makeText(activity, R.string.book_deleted, Toast.LENGTH_SHORT).show()
                        updateFilters()
                    }
                    .show()
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView: View = viewHolder.itemView
                if (viewHolder.adapterPosition == -1)
                    return

                // Draw red background
                background!!.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                background!!.draw(c)

                // Draw x mark
                val itemHeight = itemView.bottom - itemView.top
                val intrinsicWidth = icDelete!!.intrinsicWidth
                val intrinsicHeight = icDelete!!.intrinsicWidth
                val icMarginLeft = (itemView.right - icDeleteMargin - intrinsicWidth).toInt()
                val icMarginRight = (itemView.right - icDeleteMargin).toInt()
                val icMarginTop = itemView.top + (itemHeight - intrinsicHeight) / 2
                val icMarginBottom = icMarginTop + intrinsicHeight
                icDelete!!.setBounds(icMarginLeft, icMarginTop, icMarginRight, icMarginBottom)
                icDelete!!.draw(c)

                // The dX parameter is divided by 4 to reduce the max swipe width
                super.onChildDraw(c, recyclerView, viewHolder, dX / 4, dY, actionState, isCurrentlyActive)
            }
        })
        swipeTouchHelper.attachToRecyclerView(binding.rwBookList)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        menu.setGroupVisible(R.id.list_action_group, true)
        menu.setGroupVisible(R.id.default_action_group, true)

        searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem!!.actionView as SearchView
        searchView.queryHint = getString(R.string.search_hint)
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

        searchView.setOnCloseListener {
            query = ""
            false
        }

        when (prefs.getString(getString(R.string.sorting_column_key), OrderColumn.Title.name)) {
            OrderColumn.Title.name -> menu.findItem(R.id.action_by_title).isChecked = true
            OrderColumn.Author.name -> menu.findItem(R.id.action_by_author).isChecked = true
            OrderColumn.Year.name -> menu.findItem(R.id.action_by_year).isChecked = true
            OrderColumn.Progress.name -> menu.findItem(R.id.action_by_progress).isChecked = true
        }

        when (prefs.getBoolean(getString(R.string.sorting_asc_key), true)) {
            true -> menu.findItem(R.id.action_asc).isChecked = true
            false -> menu.findItem(R.id.action_desc).isChecked = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        item.isChecked = true
        var orderColumn = prefs.getString(getString(R.string.sorting_column_key), OrderColumn.Title.name)
        var asc = prefs.getBoolean(getString(R.string.sorting_asc_key), true)
        when (item.itemId) {
            R.id.action_by_title -> orderColumn = OrderColumn.Title.name
            R.id.action_by_author -> orderColumn = OrderColumn.Author.name
            R.id.action_by_year -> orderColumn = OrderColumn.Year.name
            R.id.action_by_progress -> orderColumn = OrderColumn.Progress.name
            R.id.action_asc -> asc = true
            R.id.action_desc -> asc = false
            else -> super.onOptionsItemSelected(item)
        }
        prefs.edit().putString(getString(R.string.sorting_column_key), orderColumn).apply()
        prefs.edit().putBoolean(getString(R.string.sorting_asc_key), asc).apply()
        updateFilters()
        return false
    }

    override fun onPause() {
        super.onPause()
        // Preventing the search view to stay open when navigating to another fragment while searching
        searchItem?.collapseActionView()
    }

    override fun onResume() {
        super.onResume()
        updateFilters()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}