package com.unipd.booktracker.ui.booklist

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import androidx.appcompat.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.marginBottom
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.unipd.booktracker.BookAdapter
import com.unipd.booktracker.MainActivity
import com.unipd.booktracker.R
import com.unipd.booktracker.db.OrderColumns
import com.unipd.booktracker.fragments.AddDialogFragment

abstract class BooklistFragment: Fragment() {

    abstract var bookAdapter: BookAdapter
    abstract var rw: RecyclerView
    abstract var fab: ExtendedFloatingActionButton

    abstract override fun onCreateView(inflater: LayoutInflater,container: ViewGroup?,savedInstanceState: Bundle?): View
    abstract fun updateFilters()

    protected lateinit var viewModel: BooklistViewModel
    protected var _binding: ViewBinding? = null
    protected val binding get() = _binding!!
    protected var query = ""
    protected var orderColumn = OrderColumns.title
    protected var asc = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
        viewModel = ViewModelProvider(requireActivity() as MainActivity)[BooklistViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rw.adapter = bookAdapter
        updateFilters()

        // Set appropriate padding to recycler view's bottom, otherwise fab will cover the last item
        fab.measure(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        rw.setPadding(0,0,0,fab.measuredHeight + fab.marginBottom)
        rw.clipToPadding = false

        // Extend and reduce FAB on scroll
        rw.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!recyclerView.canScrollVertically(-1))
                    fab.extend()
                else
                    fab.shrink()
                super.onScrollStateChanged(recyclerView, newState)
            }
        })

        fab.setOnClickListener {
            if (!(requireActivity() as MainActivity).isNetworkAvailable())
                Toast.makeText(requireActivity(),getString(R.string.network_error), Toast.LENGTH_SHORT).show()
            else {
                val dialog = AddDialogFragment()
                dialog.show(childFragmentManager, getString(R.string.add_book))
            }
        }

        //  swipe to delete
        ItemTouchHelper(object: ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT
        ) {
            var background: Drawable? = ColorDrawable(Color.RED)
            var xMark: Drawable? = context?.let { ContextCompat.getDrawable(it, R.drawable.ic_baseline_delete_24) }
            var xMarkMargin = resources.getDimension(R.dimen.content_margin)
            var initiated = true

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                viewModel.removeBook(bookAdapter.getBookAt(viewHolder.adapterPosition))
                Toast.makeText(activity, R.string.book_deleted, Toast.LENGTH_SHORT).show()
                updateFilters()
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
                if(viewHolder.adapterPosition == -1){
                    return
                }

                // draw red background
                background!!.setBounds(
                    itemView.right + dX.toInt(),
                    itemView.top,
                    itemView.right,
                    itemView.bottom
                )
                background!!.draw(c)

                // draw x mark
                val itemHeight = itemView.bottom - itemView.top
                val intrinsicWidth = xMark!!.intrinsicWidth
                val intrinsicHeight = xMark!!.intrinsicWidth

                val xMarkLeft = (itemView.right - xMarkMargin - intrinsicWidth).toInt()
                val xMarkRight = (itemView.right - xMarkMargin).toInt()
                val xMarkTop = itemView.top + (itemHeight - intrinsicHeight) / 2
                val xMarkBottom = xMarkTop + intrinsicHeight
                xMark!!.setBounds(xMarkLeft, xMarkTop, xMarkRight, xMarkBottom)

                xMark!!.draw(c)

                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }
        }).attachToRecyclerView(rw)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        menu.setGroupVisible(R.id.list_action_group, true)
        val searchView = (menu.findItem(R.id.action_search)?.actionView as SearchView)
        searchView.queryHint = getString(R.string.search_hint)
        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
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
    }

    protected open fun setMenuGroupVisibility(menu: Menu) { }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sort -> {
                val popupMenu = PopupMenu(requireActivity(), requireActivity().findViewById(item.itemId))
                popupMenu.menuInflater.inflate(R.menu.sorting_menu, popupMenu.menu)
                setMenuGroupVisibility(popupMenu.menu)
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
                popupMenu.show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}