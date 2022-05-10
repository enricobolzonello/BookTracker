package com.unipd.booktracker.fragments

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.LinearLayout.LayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.google.android.material.slider.Slider
import com.unipd.booktracker.*
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
        _binding = FragmentBookDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (chosenBook.thumbnail == null)
            binding.ivBookThumbnail.setBackgroundResource(R.drawable.default_thumbnail)
        else
            binding.ivBookThumbnail.setImageBitmap(BookUtils.toBitmap(chosenBook.thumbnail))

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
            setupReadPagesModifiers()
            readPages = chosenBook.readPages!!
            updateReadPages(readPages)
        }

        binding.chLibrary.setOnClickListener {
            if (binding.chLibrary.isChecked) {
                binding.chLibrary.isClickable = false
                (binding.chLibrary.layoutParams as LayoutParams).weight = 1F

                binding.chWishlist.isChecked = false
                binding.chWishlist.isClickable = true
                (binding.chWishlist.layoutParams as LayoutParams).weight = 0F

                if (!binding.chWishlist.isChecked)
                    viewModel.addBook(chosenBook)
                viewModel.moveToLibrary(chosenBook)

                setHasOptionsMenu(true)
                setupReadPagesModifiers()
                readPages = 0
                updateReadPages(readPages)
                binding.llReadPages.visibility = View.VISIBLE
            }
        }

        binding.chWishlist.setOnClickListener {
            if (binding.chWishlist.isChecked) {
                binding.chWishlist.isClickable = false
                (binding.chWishlist.layoutParams as LayoutParams).weight = 1F

                binding.chLibrary.isChecked = false
                binding.chLibrary.isClickable = true
                (binding.chLibrary.layoutParams as LayoutParams).weight = 0F

                if (!binding.chLibrary.isChecked)
                    viewModel.addBook(chosenBook)
                viewModel.moveToWishlist(chosenBook)

                setHasOptionsMenu(true)
                binding.llReadPages.visibility = View.GONE
            }
        }

        binding.chLibrary.isChecked = viewModel.isBookInLibrary(chosenBook)
        binding.chLibrary.isClickable = !binding.chLibrary.isChecked

        binding.chWishlist.isChecked = viewModel.isBookInWishlist(chosenBook)
        binding.chWishlist.isClickable = !binding.chWishlist.isChecked

        if (!binding.chLibrary.isChecked && !binding.chWishlist.isChecked) {
            setHasOptionsMenu(false)
            (binding.chLibrary.layoutParams as LayoutParams).weight = 1F
            (binding.chWishlist.layoutParams as LayoutParams).weight = 1F
        } else {
            setHasOptionsMenu(true)
            (binding.chLibrary.layoutParams as LayoutParams).weight = if (binding.chLibrary.isChecked) 1F else 0F
            (binding.chWishlist.layoutParams as LayoutParams).weight = if (binding.chWishlist.isChecked) 1F else 0F
        }
    }

    private fun setupReadPagesModifiers() {
        // The edit text only accepts valid page values
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
                    updateReadPages(newValue)
            }
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) { }
        })

        binding.btnAddPage.setOnClickListener { updateReadPages(readPages + 1) }
        binding.btnRemovePage.setOnClickListener { updateReadPages(readPages - 1) }

        // Slider move only changes the edit text value, the readPages value is changed only when sliding has ended
        binding.slReadPages.addOnChangeListener { _, value, _ ->
            binding.etReadPages.setText(value.toInt().toString())
        }

        binding.slReadPages.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                updateReadPages(slider.value.toInt())
            }
        })
    }

    private fun updateReadPages(newValue: Int) {
        if (newValue != readPages) {
            viewModel.addReadPages(chosenBook, newValue - readPages)
            readPages = newValue
        }
        binding.etReadPages.setText(readPages.toString())
        binding.slReadPages.value = readPages.toFloat()
        binding.btnAddPage.visibility = if (readPages < chosenBook.pages) View.VISIBLE else View.INVISIBLE
        binding.btnRemovePage.visibility = if (readPages > 0) View.VISIBLE else View.INVISIBLE
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.book_detail_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_share_book -> {
                true
            }
            R.id.action_delete_book -> {
                viewModel.removeBook(chosenBook)
                setHasOptionsMenu(false)
                requireActivity().onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
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
                if (isInRange(intMin, intMax, input))
                    return null
            } catch (e: NumberFormatException) {
                e.printStackTrace()
            }
            return ""
        }

        private fun isInRange(a: Int, b: Int, c: Int): Boolean {
            return if (b > a) c in a..b else c in b..a
        }
    }
}