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
import com.unipd.booktracker.databinding.FragmentLibraryBinding
import com.unipd.booktracker.db.Book
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BookDetailFragment : Fragment() {
    private lateinit var viewModel: BookViewModel
    private lateinit var binding: FragmentBookDetailBinding

    private val args: BookDetailFragmentArgs by navArgs()
    private lateinit var chosenBook : Book

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity() as MainActivity)[BookViewModel::class.java]
        binding = FragmentBookDetailBinding.inflate(layoutInflater)

        lifecycleScope.launch(Dispatchers.IO) {
            // Execute on IO thread because of database requests

            chosenBook = viewModel.getBook(args.bookId)
            withContext(Dispatchers.Main) {
                // Execute on Main thread

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
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentBookDetailBinding.inflate(inflater, container, false)
        return binding.root
    }
}