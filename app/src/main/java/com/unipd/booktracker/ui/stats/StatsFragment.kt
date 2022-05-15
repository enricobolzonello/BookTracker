package com.unipd.booktracker.ui.stats

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.unipd.booktracker.MainActivity
import com.unipd.booktracker.R
import com.unipd.booktracker.databinding.FragmentStatsBinding
import java.time.LocalDate
import kotlin.math.abs
import kotlin.math.sign

class StatsFragment: Fragment() {
    private lateinit var viewModel: StatsViewModel
    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
        viewModel = ViewModelProvider(requireActivity() as MainActivity)[StatsViewModel::class.java]
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

        // Daily stats
        binding.tvPagesReadToday.text = viewModel.countReadPagesToday().toString()
        val pagesIncrement = viewModel.countReadPagesToday() - viewModel.avgReadPagesByDay()
        val pagesPercentageIncrement: Int = (pagesIncrement.toDouble() / viewModel.avgReadPagesByDay().toDouble() * 100).toInt()
        binding.tvPagesReadTodayIncrement.text = getFormattedIncrement(pagesPercentageIncrement)
        if (pagesIncrement < 0)
            binding.tvPagesReadTodayIncrement.setTextColor(resources.getColor(R.color.red, requireActivity().theme))

        // Yearly stats
        binding.tvYear.text = LocalDate.now().year.toString()
        binding.tvBooksReadYear.text = viewModel.countReadBooksThisYear().toString()
        val bookIncrement = viewModel.countReadBooksThisYear() - viewModel.avgReadBooksByYear()
        val bookPercentageIncrement: Int = (bookIncrement.toDouble() / viewModel.avgReadBooksByYear().toDouble() * 100).toInt()
        binding.tvBooksReadYearIncrement.text = getFormattedIncrement(bookPercentageIncrement)
        if (bookIncrement < 0)
            binding.tvBooksReadYearIncrement.setTextColor(resources.getColor(R.color.red, requireActivity().theme))

        // Author stats
        val mostReadAuthor = viewModel.mostReadAuthor()
        binding.tvMostReadAuthor.text = mostReadAuthor ?: " "
        binding.tvTotalAuthors.text = getString(R.string.ph_total, viewModel.countAuthors())

        // Genre stats
        val mostReadGenre = viewModel.mostReadCategory()
        binding.tvMostReadGenre.text = mostReadGenre ?: "-"
        binding.tvTotalGenres.text = getString(R.string.ph_total, viewModel.countCategories())

        // Total stats
        binding.tvTotalReadPages.text = getString(R.string.ph_total_pages, viewModel.countReadPages())
        binding.tvTotalReadBooks.text = getString(R.string.ph_total_books, viewModel.countReadBooks())
    }

    private fun getFormattedIncrement(value: Int): String {
        val incrementSign =
            if (sign(value.toDouble()) == 1.0)
                '+'
            else
                '-'
        var increment = abs(value)
        if (increment > 999)
            increment = 999
        return getString(R.string.ph_signed_percentage, incrementSign, increment)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        menu.setGroupVisible(R.id.list_action_group, false)
        menu.setGroupVisible(R.id.default_action_group, true)
        menu.setGroupVisible(R.id.book_detail_action_group, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}