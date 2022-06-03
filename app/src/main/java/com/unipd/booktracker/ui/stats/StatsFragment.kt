package com.unipd.booktracker.ui.stats

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.TextAppearanceSpan
import android.view.*
import androidx.core.content.ContextCompat
import androidx.core.text.set
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.transition.MaterialElevationScale
import com.unipd.booktracker.MainActivity
import com.unipd.booktracker.R
import com.unipd.booktracker.databinding.FragmentStatsBinding
import com.unipd.booktracker.util.getAttrId
import kotlin.math.abs

class StatsFragment : Fragment() {
    private lateinit var viewModel: StatsViewModel
    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(requireActivity() as MainActivity)[StatsViewModel::class.java]

        setHasOptionsMenu(true)

        // Entering transitions
        enterTransition = MaterialElevationScale(true).apply {
            duration = resources.getInteger(com.google.android.material.R.integer.material_motion_duration_short_2).toLong()
        }
        reenterTransition = MaterialElevationScale(true).apply {
            duration = resources.getInteger(com.google.android.material.R.integer.material_motion_duration_short_2).toLong()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // The entering transition need to be postponed to be visible to the user
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

        val spanTitleSmall = TextAppearanceSpan(
            requireContext(),
            requireContext().getAttrId(com.google.android.material.R.attr.textAppearanceTitleSmall)
        )
        val spanGreen = ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.green))
        val spanRed = ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.red))

        // Daily stats
        binding.tvPagesReadToday.text = getSpannedValueString(
            R.string.ph_read_pages,
            viewModel.countReadPagesToday().toString(),
            false,
            spanTitleSmall
        )

        val avgReadPagesByDay = viewModel.avgReadPagesByDay()
        val pagesPercentageIncrement =
            when {
                avgReadPagesByDay != 0 -> ((viewModel.countReadPagesToday() - avgReadPagesByDay) / avgReadPagesByDay.toDouble() * 100).toInt()
                viewModel.countReadPagesToday() != 0 -> 100
                else -> 0
            }
        binding.tvPagesReadTodayIncrement.text = getSpannedValueString(
            R.string.ph_than_usual,
            getFormattedIncrement(pagesPercentageIncrement),
            true,
            spanTitleSmall,
            if (pagesPercentageIncrement >= 0) spanGreen else spanRed
        )

        // Yearly stats
        binding.tvBooksReadYear.text = getSpannedValueString(
            R.string.ph_read_books,
            viewModel.countReadBooksThisYear().toString(),
            false,
            spanTitleSmall
        )

        val avgReadBooksByYear = viewModel.avgReadBooksByYear()
        val bookPercentageIncrement =
            when {
                avgReadBooksByYear != 0 -> ((viewModel.countReadBooksThisYear() - avgReadBooksByYear) / avgReadBooksByYear.toDouble() * 100).toInt()
                viewModel.countReadBooksThisYear() != 0 -> 100
                else -> 0
            }
        binding.tvBooksReadYearIncrement.text = getSpannedValueString(
            R.string.ph_than_usual,
            getFormattedIncrement(bookPercentageIncrement),
            true,
            spanTitleSmall,
            if (pagesPercentageIncrement >= 0) spanGreen else spanRed
        )

        // Author stats
        binding.tvMostReadAuthor.text = getSpannedValueString(
            R.string.ph_most_read,
            viewModel.mostReadAuthor() ?: "-",
            false,
            spanTitleSmall
        )

        binding.tvTotalAuthors.text = getSpannedValueString(
            R.string.ph_total,
            viewModel.countAuthors().toString(),
            false,
            spanTitleSmall
        )

        // Category stats
        binding.tvMostReadCategory.text = getSpannedValueString(
            R.string.ph_most_read,
            viewModel.mostReadCategory() ?: "-",
            false,
            spanTitleSmall
        )

        binding.tvTotalCategories.text = getSpannedValueString(
            R.string.ph_total,
            viewModel.countCategories().toString(),
            false,
            spanTitleSmall
        )

        // Total stats
        binding.tvTotalReadPages.text = getSpannedValueString(
            R.string.ph_read_pages,
            viewModel.countReadPages().toString(),
            false,
            spanTitleSmall
        )
        binding.tvTotalReadBooks.text = getSpannedValueString(
            R.string.ph_read_books,
            viewModel.countReadBooks().toString(),
            false,
            spanTitleSmall
        )
    }

    // This method applies multiple given modifiers to a Text-Value string
    private fun getSpannedValueString(
        stringId: Int,
        value: String,
        start: Boolean,
        vararg modifiers: Any
    ): SpannableStringBuilder {
        val spannable = SpannableStringBuilder(getString(stringId, value))
        val range =
            if (start)
                0..value.length
            else
                spannable.length - value.length..spannable.length
        modifiers.forEach {
            spannable[range] = it
        }
        return spannable
    }

    private fun getFormattedIncrement(value: Int): String {
        val incrementSign = if (value >= 0) '+' else '-'
        var increment = abs(value)
        if (increment > 999)
            increment = 999
        return getString(R.string.ph_percentage, incrementSign + increment.toString())
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.setGroupVisible(R.id.default_action_group, true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}