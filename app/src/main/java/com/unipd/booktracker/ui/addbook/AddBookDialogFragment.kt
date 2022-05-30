package com.unipd.booktracker.ui.addbook

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.unipd.booktracker.ui.BookAdapter
import com.unipd.booktracker.MainActivity
import com.unipd.booktracker.R
import com.unipd.booktracker.databinding.FragmentAddBookBinding
import com.unipd.booktracker.util.isSideBySideMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddBookDialogFragment : BottomSheetDialogFragment() {
    private lateinit var viewModel: AddBookViewModel
    private lateinit var bookAdapter: BookAdapter
    private var _binding: FragmentAddBookBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(requireActivity() as MainActivity)[AddBookViewModel::class.java]
        bookAdapter = BookAdapter(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBookBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val windowHeight = requireActivity().window.decorView.rootView.height
        val windowWidth = requireActivity().window.decorView.rootView.width

        val bottomSheetBehavior = (this.dialog as BottomSheetDialog).behavior
        bottomSheetBehavior.maxWidth =
            if (requireContext().isSideBySideMode())
                (windowWidth * 0.66).toInt()
            else
                (windowWidth * 0.95).toInt()
        bottomSheetBehavior.maxHeight = (windowHeight * 0.90).toInt()
        bottomSheetBehavior.peekHeight = windowHeight / 2
        view.minimumHeight = windowHeight / 2

        binding.rwAddBook.adapter = bookAdapter

        binding.rwAddBook.setPadding(0, 0, 0, (resources.getDimension(R.dimen.content_margin) * 2).toInt())
        binding.rwAddBook.clipToPadding = false

        binding.swAddBook.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                binding.rwAddBook.visibility = View.GONE
                binding.piLoading.visibility = View.VISIBLE
                binding.tvEmptyListPlaceholder.visibility = View.GONE
                lifecycleScope.launch(Dispatchers.IO) {
                    // Running suspend fun on IO thread
                    val books = viewModel.getBooksFromQuery(query)
                    withContext(Dispatchers.Main) {
                        // Updating the UI after the suspend fun has ended
                        binding.piLoading.visibility = View.GONE
                        if (!books.isNullOrEmpty()) {
                            bookAdapter.setBooks(books)
                            binding.rwAddBook.visibility = View.VISIBLE
                            binding.tvEmptyListPlaceholder.visibility = View.GONE
                        } else {
                            binding.tvEmptyListPlaceholder.visibility = View.VISIBLE
                            if (books == null)
                                Toast.makeText(requireContext(), getString(R.string.books_api_error), Toast.LENGTH_SHORT)
                                    .show()
                        }
                    }
                }
                return false
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}