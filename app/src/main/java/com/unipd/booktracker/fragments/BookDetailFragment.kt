package com.unipd.booktracker.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
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
    private var readPages = -1

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

                binding.llReadPages.visibility = View.VISIBLE
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

            binding.llReadPages.visibility = View.GONE
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
            readPages = chosenBook.readPages!!
            updatePages(chosenBook.readPages!!)

            binding.etReadPages.filters = arrayOf<InputFilter>(MinMaxFilter(0, chosenBook.pages))
            binding.tvFirstPage.text = (0).toString()
            binding.tvLastPage.text = chosenBook.pages.toString()
            binding.slReadPages.valueTo = chosenBook.pages.toFloat()

            binding.etReadPages.addTextChangedListener(object : TextWatcher {

                override fun afterTextChanged(s: Editable) {
                    if (s.isBlank())
                        return
                    val newValue = s.toString().toInt()
                    if (newValue != readPages)
                        updatePages(newValue)
                }

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) { }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) { }
            })

            binding.btnAddPage.setOnClickListener {
                updatePages(readPages + 1)
            }

            binding.btnRemovePage.setOnClickListener {
                updatePages(readPages - 1)
            }

            binding.slReadPages.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {

                @SuppressLint("RestrictedApi")
                override fun onStartTrackingTouch(slider: Slider) { }

                @SuppressLint("RestrictedApi")
                override fun onStopTrackingTouch(slider: Slider) {
                    updatePages(slider.value.toInt())
                }
            })

            binding.slReadPages.addOnChangeListener { _, value, _ ->
                binding.etReadPages.setText(value.toInt().toString())
            }
        }
    }

    fun updatePages(newValue: Int) {
        if (newValue != readPages) {
            viewModel.addReadPages(chosenBook, newValue - readPages)
            readPages = newValue
        }

        binding.etReadPages.setText(readPages.toString())
        binding.btnAddPage.visibility =
            if (readPages < chosenBook.pages)
                View.VISIBLE
            else
                View.INVISIBLE
        binding.btnRemovePage.visibility =
            if (readPages > 0)
                View.VISIBLE
            else
                View.INVISIBLE
        binding.slReadPages.value = readPages.toFloat()
    }

    inner class MinMaxFilter() : InputFilter {
        private var intMin: Int = 0
        private var intMax: Int = 0

        constructor(minValue: Int, maxValue: Int) : this() {
            this.intMin = minValue
            this.intMax = maxValue
        }

        override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dStart: Int, dEnd: Int): CharSequence? {
            try {
                val input = Integer.parseInt(dest.toString() + source.toString())
                if (isInRange(intMin, intMax, input)) {
                    return null
                }
            } catch (e: NumberFormatException) {
                e.printStackTrace()
            }
            return ""
        }

        private fun isInRange(a: Int, b: Int, c: Int): Boolean {
            return if (b > a) c in a..b else c in b..a
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}