package com.unipd.booktracker.fragments

import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.unipd.booktracker.BookAdapter
import com.unipd.booktracker.BookViewModel
import com.unipd.booktracker.MainActivity
import com.unipd.booktracker.R
import com.unipd.booktracker.databinding.FragmentAddBookBinding
import com.unipd.booktracker.databinding.FragmentBookDetailBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AddDialogFragment : DialogFragment() {
    private lateinit var viewModel: BookViewModel
    private lateinit var binding: FragmentAddBookBinding
    private lateinit var bookAdapter : BookAdapter

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        viewModel = ViewModelProvider(requireActivity() as MainActivity)[BookViewModel::class.java]
        binding = FragmentAddBookBinding.inflate(layoutInflater)
        bookAdapter = BookAdapter(this)

        val dialog = BottomSheetDialog(requireContext())

        // Pass null as the parent view because its going in the dialog layout (https://developer.android.com/guide/topics/ui/dialogs)
        val dialogView = layoutInflater.inflate(R.layout.fragment_add_book, null)
        dialog.setContentView(dialogView)
        dialogView.layoutParams.height = Resources.getSystem().displayMetrics.heightPixels

        val rwAddBook = dialogView.findViewById<RecyclerView>(R.id.rw_add_book)
        rwAddBook.adapter = bookAdapter
        val swAddBook = dialogView.findViewById<SearchView>(R.id.sw_add_book)

        swAddBook.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                lifecycleScope.launch(Dispatchers.IO) {
                    // Running coroutine with network usage
                    val books = viewModel.getBooksFromQuery(query)
                    withContext(Dispatchers.Main) {
                        // Updating the UI after the coroutine has ended
                        bookAdapter.setBooks(books)
                    }
                }
                return false
            }
        })
        return dialog
    }

    override fun onResume() {
        super.onResume()
        binding.rwAddBook.adapter = bookAdapter
    }
}