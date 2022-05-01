package com.unipd.booktracker.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.unipd.booktracker.BookViewModel
import com.unipd.booktracker.MainActivity
import com.unipd.booktracker.R
import com.unipd.booktracker.databinding.FragmentBookDetailBinding
import com.unipd.booktracker.db.Book
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BookDetailFragment : Fragment() {
    private lateinit var viewModel: BookViewModel
    private var _binding: FragmentBookDetailBinding? = null
    private val binding get() = _binding!!

    private val args: BookDetailFragmentArgs by navArgs()
    private lateinit var chosenBook : Book

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(requireActivity() as MainActivity)[BookViewModel::class.java]
        chosenBook = args.chosenBook
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentBookDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch(Dispatchers.IO) {
            binding.chLibrary.isChecked = viewModel.isBookInLibrary(chosenBook)
            binding.chWishlist.isChecked = viewModel.isBookInWishlist(chosenBook)
        }

        binding.chLibrary.setOnClickListener {
            if (binding.chLibrary.isChecked) {
                if (!binding.chWishlist.isChecked)
                    lifecycleScope.launch(Dispatchers.IO) { viewModel.addBook(chosenBook) }
                lifecycleScope.launch(Dispatchers.IO) { viewModel.moveToLibrary(chosenBook) }
                binding.chWishlist.isChecked = false
            }
            else
                lifecycleScope.launch(Dispatchers.IO) { viewModel.removeBook(chosenBook) }
        }

        binding.chWishlist.setOnClickListener {
            if (binding.chWishlist.isChecked)
                if (!binding.chLibrary.isChecked)
                    lifecycleScope.launch(Dispatchers.IO) { viewModel.addBook(chosenBook) }
                else {
                    lifecycleScope.launch(Dispatchers.IO) { viewModel.moveToWishlist(chosenBook) }
                    binding.chLibrary.isChecked = false
                }
            else
                lifecycleScope.launch(Dispatchers.IO) { viewModel.removeBook(chosenBook) }
        }

        binding.tvBookTitle.text = chosenBook.title
        binding.tvBookAuthor.text = chosenBook.mainAuthor
        binding.tvBookPages.text = chosenBook.pages.toString()
        binding.tvBookGenre.text = chosenBook.mainCategory ?: "-"
        binding.tvBookLanguage.text = chosenBook.language ?: "-"
        binding.tvBookDescription.text = chosenBook.description ?: "-"
        binding.tvBookPublisher.text = chosenBook.publisher ?: "-"
        binding.tvBookIsbn.text = chosenBook.isbn ?: "-"

        if (chosenBook.thumbnail == null)
            binding.ivBookThumbnail.setBackgroundResource(R.drawable.default_thumbnail)
        else
            binding.ivBookThumbnail.setImageBitmap(chosenBook.thumbnail)
    }
}