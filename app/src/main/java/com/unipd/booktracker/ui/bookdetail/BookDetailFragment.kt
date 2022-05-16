package com.unipd.booktracker.ui.bookdetail

import android.content.res.ColorStateList
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.view.*
import android.widget.LinearLayout.LayoutParams
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import com.unipd.booktracker.*
import com.unipd.booktracker.databinding.FragmentBookDetailBinding
import com.unipd.booktracker.db.Book

class BookDetailFragment: Fragment() {
    private lateinit var viewModel: BookDetailViewModel
    private var _binding: FragmentBookDetailBinding? = null
    private val binding get() = _binding!!

    private val args: BookDetailFragmentArgs by navArgs()
    private lateinit var chosenBook: Book
    private var readPages = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.book_detail)
        }
        (requireActivity() as MainActivity).setBottomNavVisibility(View.GONE)

        viewModel = ViewModelProvider(requireActivity() as MainActivity)[BookDetailViewModel::class.java]
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

        binding.layout.backgroundTintList = ColorStateList.valueOf(resources.getColor(com.google.android.material.R.color.m3_ref_palette_dynamic_primary10)).withAlpha(220)

        if (chosenBook.thumbnail == null)
            binding.ivBookThumbnail.setBackgroundResource(R.drawable.default_thumbnail)
        else{
            binding.ivBookThumbnail.setImageBitmap(BookUtils.toBitmap(chosenBook.thumbnail))
            binding.layout.background = BitmapDrawable(BookUtils.toBitmap(chosenBook.thumbnail))
        }

        binding.tvBookTitle.text = chosenBook.title
        binding.tvBookAuthor.text = chosenBook.mainAuthor
        binding.tvBookPages.text = chosenBook.pages.toString()
        // binding.tvBookGenre.text = chosenBook.mainCategory ?: "-"
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
            // If the book is not already present, add it
            if (!binding.chWishlist.isChecked)
                viewModel.addBook(chosenBook)
            viewModel.moveToLibrary(chosenBook)
            // Either one of the two chips should always be checked, so it's not uncheckable
            binding.chLibrary.isClickable = false
            (binding.chLibrary.layoutParams as LayoutParams).weight = 1F
            // Make the other chip ready to be checked
            binding.chWishlist.isChecked = false
            binding.chWishlist.isClickable = true
            (binding.chWishlist.layoutParams as LayoutParams).weight = 0F
            // Present book interface
            setHasOptionsMenu(true)
            // Library book interface
            setupReadPagesModifiers()
            readPages = 0
            updateReadPages(readPages)
            binding.llReadPages.visibility = View.VISIBLE
        }

        binding.chWishlist.setOnClickListener {
            // If the book is not already present, add it
            if (!binding.chLibrary.isChecked)
                viewModel.addBook(chosenBook)
            viewModel.moveToWishlist(chosenBook)
            // Either one of the two chips should always be checked, so it's not uncheckable
            binding.chWishlist.isClickable = false
            (binding.chWishlist.layoutParams as LayoutParams).weight = 1F
            // Make the other chip ready to be checked
            binding.chLibrary.isChecked = false
            binding.chLibrary.isClickable = true
            (binding.chLibrary.layoutParams as LayoutParams).weight = 0F
            // Present book interface
            setHasOptionsMenu(true)
            // Wishlist book interface
            binding.llReadPages.visibility = View.GONE
        }

        binding.chLibrary.isChecked = viewModel.isBookInLibrary(chosenBook)
        binding.chLibrary.isClickable = !binding.chLibrary.isChecked

        binding.chWishlist.isChecked = viewModel.isBookInWishlist(chosenBook)
        binding.chWishlist.isClickable = !binding.chWishlist.isChecked

        // Not present book interface
        if (!binding.chLibrary.isChecked && !binding.chWishlist.isChecked) {
            setHasOptionsMenu(false)
            (binding.chLibrary.layoutParams as LayoutParams).weight = 1F
            (binding.chWishlist.layoutParams as LayoutParams).weight = 1F
        }
        // Present book interface
        else {
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

        binding.etReadPages.addTextChangedListener(object: TextWatcher {
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

        binding.slReadPages.addOnSliderTouchListener(object: Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) { }

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
        super.onCreateOptionsMenu(menu, inflater)

        menu.setGroupVisible(R.id.list_action_group, false)
        menu.setGroupVisible(R.id.default_action_group, false)
        menu.setGroupVisible(R.id.book_detail_action_group, true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_share_book -> {
                //todo share action
                true
            }
            R.id.action_delete_book -> {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(resources.getString(R.string.delete_book_dialog_title))
                    .setMessage(resources.getString(R.string.delete_book_dialog_message, chosenBook.title))
                    .setIcon(ResourcesCompat.getDrawable(resources, R.drawable.ic_delete, requireContext().theme))
                    .setNegativeButton(resources.getString(R.string.no)) { dialog, which ->
                        // Respond to negative button press
                    }
                    .setPositiveButton(resources.getString(R.string.yes)) { dialog, which ->
                        // Respond to positive button press
                        viewModel.removeBook(chosenBook)
                        Toast.makeText(activity, R.string.book_deleted, Toast.LENGTH_SHORT).show()
                        requireActivity().onBackPressed()
                    }
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(false)
            title = getString(R.string.app_name)
        }
        (requireActivity() as MainActivity).setBottomNavVisibility(View.VISIBLE)

        super.onDestroy()
        _binding = null
    }

    inner class MinMaxFilter(): InputFilter {
        private var intMin: Int = 0
        private var intMax: Int = 0

        constructor(minValue: Int, maxValue: Int): this() {
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