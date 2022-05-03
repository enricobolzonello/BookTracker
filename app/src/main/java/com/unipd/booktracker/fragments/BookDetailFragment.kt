package com.unipd.booktracker.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout.LayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.google.android.material.slider.Slider
import com.unipd.booktracker.BookViewModel
import com.unipd.booktracker.MainActivity
import com.unipd.booktracker.R
import com.unipd.booktracker.databinding.FragmentBookDetailBinding
import com.unipd.booktracker.db.Book


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

        binding.chLibrary.isChecked = viewModel.isBookInLibrary(chosenBook)
        binding.chWishlist.isChecked = viewModel.isBookInWishlist(chosenBook)

        if (!binding.chLibrary.isChecked && !binding.chWishlist.isChecked) {
            (binding.chLibrary.layoutParams as LayoutParams).weight = 1F
            (binding.chWishlist.layoutParams as LayoutParams).weight = 1F
        }
        else {
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
        }

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
            var readPages = -1

            viewModel.getObservableReadPages(chosenBook).observe(requireActivity()) {
                readPages = it
                binding.etReadPages.setText(it.toString())
                binding.btnAddPage.visibility =
                    if (it < chosenBook.pages)
                        View.VISIBLE
                    else
                        View.INVISIBLE
                binding.btnRemovePage.visibility =
                    if (it > 0)
                        View.VISIBLE
                    else
                        View.INVISIBLE
                binding.slReadPages.value = it.toFloat()
            }

            binding.tvFirstPage.text = (0).toString()
            binding.tvLastPage.text = chosenBook.pages.toString()
            binding.slReadPages.valueTo = chosenBook.pages.toFloat()

            binding.etReadPages.setOnEditorActionListener { textView, i, keyEvent ->
                if (i == EditorInfo.IME_ACTION_SEARCH || i == EditorInfo.IME_ACTION_DONE
                    || keyEvent == null || keyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                    viewModel.addReadPages(chosenBook, textView.text.toString().toInt() - readPages)
                }
                false
            }

            binding.btnAddPage.setOnClickListener {
                viewModel.addReadPages(chosenBook, 1)
            }

            binding.btnRemovePage.setOnClickListener {
                viewModel.addReadPages(chosenBook, -1)
            }

            binding.slReadPages.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {

                @SuppressLint("RestrictedApi")
                override fun onStartTrackingTouch(slider: Slider) { }

                @SuppressLint("RestrictedApi")
                override fun onStopTrackingTouch(slider: Slider) {
                    viewModel.addReadPages(chosenBook, slider.value.toInt() - readPages)
                }
            })

            binding.slReadPages.addOnChangeListener { _, value, _ ->
                binding.etReadPages.setText(value.toInt().toString())
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}