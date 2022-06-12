package com.unipd.booktracker.ui.bookdetail

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.view.*
import android.widget.LinearLayout.LayoutParams
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import com.google.android.material.transition.MaterialContainerTransform
import com.unipd.booktracker.*
import com.unipd.booktracker.databinding.FragmentBookDetailBinding
import com.unipd.booktracker.db.Book
import com.unipd.booktracker.util.getAttrId
import com.unipd.booktracker.util.toBitMap
import java.lang.StringBuilder

class BookDetailFragment : Fragment() {
    private lateinit var viewModel: BookDetailViewModel
    private var _binding: FragmentBookDetailBinding? = null
    private val binding get() = _binding!!

    private val args: BookDetailFragmentArgs by navArgs()
    private var _chosenBook: Book? = null
    private val chosenBook get() = _chosenBook!!
    private var readPages = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(requireActivity() as MainActivity)[BookDetailViewModel::class.java]

        // Transition from list card to book detail
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.nav_host_fragment
            duration = resources.getInteger(com.google.android.material.R.integer.material_motion_duration_long_1).toLong()
            scrimColor = Color.TRANSPARENT
            setAllContainerColors(
                ContextCompat.getColor(
                    requireContext(),
                    requireContext().getAttrId(com.google.android.material.R.attr.colorSurface)
                )
            )
        }

        // If the detail fragment has been opened by navigating, top and bottom bar need to be updated
        if (arguments != null) {
            (requireActivity() as MainActivity).apply {
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
                supportActionBar?.title = getString(R.string.book_detail)
                setNavVisibility(View.GONE)
                // On back button press navigate up in the nav graph instead of normal behaviour
                // The callback is associated with the fragment lifecycle so that it's removed when it gets destroyed
                onBackPressedDispatcher.addCallback(this@BookDetailFragment) {
                    onSupportNavigateUp()
                }
            }
        }
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

        binding.chLibrary.setOnClickListener {
            val moveNeeded = binding.chWishlist.isChecked
            // If the book is not already present, add it
            if (!moveNeeded)
                viewModel.addBook(chosenBook)
            viewModel.moveToLibrary(chosenBook)
            // Either one of the two chips should always be checked, so it's not uncheckable
            binding.chLibrary.isClickable = false
            (binding.chLibrary.layoutParams as LayoutParams).weight = 1F
            // Make the other chip ready to be checked
            binding.chWishlist.isChecked = false
            binding.chWishlist.isClickable = true
            (binding.chWishlist.layoutParams as LayoutParams).weight = 0F
            // Library book interface
            setupReadPagesModifiers()
            readPages = 0
            updateReadPages(readPages)
            binding.llReadPages.visibility = View.VISIBLE
            // If the fragment is displayed side by side with wishlist,
            // when the book gets moved to the library, the detail view needs to be cleared
            if (arguments == null && moveNeeded)
                setBook(null)
            setHasOptionsMenu(true)
        }

        binding.chWishlist.setOnClickListener {
            val moveNeeded = binding.chLibrary.isChecked
            // If the book is not already present, add it
            if (!moveNeeded)
                viewModel.addBook(chosenBook)
            else
                viewModel.moveToWishlist(chosenBook)
            // Either one of the two chips should always be checked, so it's not uncheckable
            binding.chWishlist.isClickable = false
            (binding.chWishlist.layoutParams as LayoutParams).weight = 1F
            // Make the other chip ready to be checked
            binding.chLibrary.isChecked = false
            binding.chLibrary.isClickable = true
            (binding.chLibrary.layoutParams as LayoutParams).weight = 0F
            // Wishlist book interface
            binding.llReadPages.visibility = View.GONE
            // If the fragment is displayed side by side with library,
            // when the book gets moved to the library, the detail view needs to be cleared
            if (arguments == null && moveNeeded)
                setBook(null)
            setHasOptionsMenu(true)
        }

        if (arguments != null)
            setBook(args.chosenBook)
        else
            setBook(_chosenBook)
    }

    fun setBook(book: Book?) {
        _chosenBook = book
        if (_chosenBook == null) {
            binding.tvBookDetailPlaceholder.visibility = View.VISIBLE
            binding.swBookDetail.visibility = View.GONE
        } else {
            binding.tvBookDetailPlaceholder.visibility = View.GONE
            binding.swBookDetail.visibility = View.VISIBLE
            setupBookInfo()
        }
        requireActivity().invalidateOptionsMenu()
    }

    private fun setupBookInfo() {
        if (chosenBook.thumbnail == null)
            binding.ivBookThumbnail.setBackgroundResource(R.drawable.default_thumbnail)
        else
            binding.ivBookThumbnail.setImageBitmap(chosenBook.thumbnail.toBitMap())

        binding.tvBookTitle.text = chosenBook.title
        binding.tvBookAuthor.text = chosenBook.mainAuthor
        binding.tvBookPages.text = chosenBook.pages.toString()

        binding.tvBookYear.text = if (chosenBook.year != null) chosenBook.year.toString() else "-"
        binding.tvBookLanguage.text = if (!chosenBook.language.isNullOrEmpty()) chosenBook.language else "-"
        binding.tvBookCategory.text = if (!chosenBook.mainCategory.isNullOrEmpty()) chosenBook.mainCategory else "-"
        binding.tvBookDescription.text = if (!chosenBook.description.isNullOrEmpty()) chosenBook.description else "-"
        binding.tvBookPublisher.text = if (!chosenBook.publisher.isNullOrEmpty()) chosenBook.publisher else "-"
        binding.tvBookIsbn.text = if (!chosenBook.isbn.isNullOrEmpty()) chosenBook.isbn else "-"

        if (chosenBook.readPages == null)
            binding.llReadPages.visibility = View.GONE
        else {
            readPages = chosenBook.readPages!!
            setupReadPagesModifiers()
        }

        binding.chLibrary.isChecked = viewModel.isBookInLibrary(chosenBook)
        binding.chLibrary.isClickable = !binding.chLibrary.isChecked

        binding.chWishlist.isChecked = viewModel.isBookInWishlist(chosenBook)
        binding.chWishlist.isClickable = !binding.chWishlist.isChecked

        // Already present book interface
        if (binding.chLibrary.isChecked || binding.chWishlist.isChecked) {
            setHasOptionsMenu(true)
            (binding.chLibrary.layoutParams as LayoutParams).weight = if (binding.chLibrary.isChecked) 1F else 0F
            (binding.chWishlist.layoutParams as LayoutParams).weight = if (binding.chWishlist.isChecked) 1F else 0F
        } else {
            (binding.chLibrary.layoutParams as LayoutParams).weight = 1F
            (binding.chWishlist.layoutParams as LayoutParams).weight = 1F
        }
    }

    private fun updateReadPages(newValue: Int) {
        if (newValue == readPages)
            return
        // Update database entry
        viewModel.addReadPages(chosenBook, newValue - readPages)
        readPages = newValue
        // Update interface
        binding.slReadPages.value = readPages.toFloat()
        binding.btnAddPage.visibility = if (readPages < chosenBook.pages) View.VISIBLE else View.INVISIBLE
        binding.btnRemovePage.visibility = if (readPages > 0) View.VISIBLE else View.INVISIBLE
    }

    private fun setupReadPagesModifiers() {
        binding.etReadPages.setText(readPages.toString())
        // The edit text only accepts valid page values
        binding.etReadPages.filters = arrayOf<InputFilter>(MinMaxFilter(0, chosenBook.pages))
        binding.etReadPages.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isNotBlank())
                    updateReadPages(s.toString().toInt())
            }
        })

        binding.btnAddPage.visibility = if (readPages < chosenBook.pages) View.VISIBLE else View.INVISIBLE
        binding.btnAddPage.setOnClickListener { updateReadPages(readPages + 1) }

        binding.btnRemovePage.visibility = if (readPages > 0) View.VISIBLE else View.INVISIBLE
        binding.btnRemovePage.setOnClickListener { updateReadPages(readPages - 1) }

        binding.tvFirstPage.text = (0).toString()
        binding.tvLastPage.text = chosenBook.pages.toString()

        binding.slReadPages.valueTo = chosenBook.pages.toFloat()
        binding.slReadPages.value = readPages.toFloat()
        // Every change of the Slider value updates the EditText
        binding.slReadPages.addOnChangeListener { _, value, _ ->
            value.toInt().toString().let {
                binding.etReadPages.setText(it)
                binding.etReadPages.setSelection(it.length)
            }
        }
        // Only when the slider move has ended the value is updated
        binding.slReadPages.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}
            override fun onStopTrackingTouch(slider: Slider) {
                updateReadPages(slider.value.toInt())
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        val isAlreadyPresent = !(arguments == null && _chosenBook == null)
        menu.setGroupVisible(R.id.book_detail_action_group, isAlreadyPresent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_share_book -> {
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_TEXT, chosenBook.toString())
                startActivity(Intent.createChooser(intent, getString(R.string.share_book)))
                true
            }
            R.id.action_delete_book -> {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.delete_book_dialog_title))
                    .setMessage(getString(R.string.delete_book_dialog_message, chosenBook.title))
                    .setIcon(ResourcesCompat.getDrawable(resources, R.drawable.ic_delete, requireContext().theme))
                    .setNegativeButton(getString(R.string.no)) { _, _ -> }
                    .setPositiveButton(getString(R.string.yes)) { _, _ ->
                        viewModel.removeBook(chosenBook)
                        setBook(null)
                        if (arguments != null)
                            requireActivity().onBackPressed()
                        Toast.makeText(requireActivity(), R.string.book_deleted, Toast.LENGTH_SHORT).show()
                    }
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        _chosenBook = null
    }

    inner class MinMaxFilter(private val min: Int, private val max: Int) : InputFilter {
        override fun filter(
            source: CharSequence,
            start: Int,
            end: Int,
            dest: Spanned,
            dStart: Int,
            dEnd: Int
        ): CharSequence? {
            try {
                val input = StringBuilder().append(dest).insert(dStart, source).toString()
                val value = Integer.parseInt(input)
                if (value in min..max)
                    return null
            } catch (e: NumberFormatException) {
                e.printStackTrace()
            }
            return ""
        }
    }
}