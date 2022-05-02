package com.unipd.booktracker.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.unipd.booktracker.BookViewModel
import com.unipd.booktracker.MainActivity
import com.unipd.booktracker.R
import com.unipd.booktracker.databinding.FragmentBookDetailBinding
import com.unipd.booktracker.db.Book
import android.widget.LinearLayout.LayoutParams
import android.widget.Toast
import androidx.core.text.isDigitsOnly
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.MutableLiveData

class BookDetailFragment : Fragment() {
    private lateinit var viewModel: BookViewModel
    private var _binding: FragmentBookDetailBinding? = null
    private val binding get() = _binding!!

    private val args: BookDetailFragmentArgs by navArgs()
    private lateinit var chosenBook : Book
    private var curReadPages: Int = -1

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

    private fun updatePages() {
        binding.etReadPages.setText(curReadPages.toString())
        binding.slReadPages.value = curReadPages.toFloat()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.chLibrary.isChecked = viewModel.isBookInLibrary(chosenBook)
        (binding.chLibrary.layoutParams as LayoutParams).weight =
            if (binding.chLibrary.isChecked)
                1F
            else
                0F

        binding.chWishlist.isChecked = viewModel.isBookInWishlist(chosenBook)
        (binding.chWishlist.layoutParams as LayoutParams).weight =
            if (binding.chWishlist.isChecked)
                1F
            else
                0F

        binding.chLibrary.setOnClickListener {
            if (binding.chLibrary.isChecked) {
                if (!binding.chWishlist.isChecked)
                    viewModel.addBook(chosenBook)
                viewModel.moveToLibrary(chosenBook)

                binding.chLibrary.isClickable = false
                (binding.chLibrary.layoutParams as LayoutParams).weight = 1F

                binding.chWishlist.isChecked = false
                binding.chWishlist.isClickable = true
                (binding.chWishlist.layoutParams as LayoutParams).weight = 0F
            }
        }

        binding.chWishlist.setOnClickListener {
            if (binding.chWishlist.isChecked)
                if (!binding.chLibrary.isChecked)
                    viewModel.addBook(chosenBook)
                viewModel.moveToWishlist(chosenBook)

                binding.chWishlist.isClickable = false
                (binding.chWishlist.layoutParams as LayoutParams).weight = 1F

                binding.chLibrary.isChecked = false
                binding.chLibrary.isClickable = true
                (binding.chLibrary.layoutParams as LayoutParams).weight = 0F
        }

        if (chosenBook.thumbnail == null)
            binding.ivBookThumbnail.setBackgroundResource(R.drawable.default_thumbnail)
        else
            binding.ivBookThumbnail.setImageBitmap(chosenBook.thumbnail)

        binding.tvBookTitle.text = chosenBook.title
        binding.tvBookAuthor.text = chosenBook.mainAuthor
        binding.tvBookPages.text = chosenBook.pages.toString()
        binding.tvBookGenre.text = chosenBook.mainCategory ?: "-"
        binding.tvBookLanguage.text = chosenBook.language ?: "-"
        binding.tvBookDescription.text = chosenBook.description ?: "-"
        binding.tvBookPublisher.text = chosenBook.publisher ?: "-"
        binding.tvBookIsbn.text = chosenBook.isbn ?: "-"

        if (chosenBook.readPages == null)
            binding.llReadPages.visibility = View.GONE
        else {
            curReadPages = chosenBook.readPages!!

            binding.etReadPages.setText(curReadPages.toString())
            binding.tvFirstPage.text = (0).toString()
            binding.tvLastPage.text = chosenBook.pages.toString()
            binding.slReadPages.valueTo = chosenBook.pages.toFloat()
            binding.slReadPages.value = chosenBook.readPages!!.toFloat()

            binding.btnAddPage.setOnClickListener {
                if (curReadPages < chosenBook.pages) {
                    curReadPages += 1
                    updatePages()
                }
            }

            binding.btnRemovePage.setOnClickListener {
                if (curReadPages > 0) {
                    curReadPages -= 1
                    updatePages()
                }
            }

            binding.slReadPages.addOnChangeListener { _, value, _ ->
                curReadPages = value.toInt()
                updatePages()
            }

            binding.etReadPages.addTextChangedListener(object : TextWatcher {

                override fun afterTextChanged(s: Editable) { }

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    if (s.isBlank())
                        return
                    val value = s.toString().toInt()
                    if (value == curReadPages)
                        return
                    if (value in 0..chosenBook.pages) {
                        curReadPages = value
                        updatePages()
                    }
                    else
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.page_value_error),
                            Toast.LENGTH_SHORT
                        ).show()
                }
            })
        }
    }
}