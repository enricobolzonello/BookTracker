package com.unipd.booktracker.ui.stats

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.unipd.booktracker.MainActivity
import com.unipd.booktracker.R
import com.unipd.booktracker.databinding.FragmentStatsBinding
import java.time.LocalDate
import kotlin.math.abs
import kotlin.math.sign

class StatsFragment : Fragment() {
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

        binding.tvPagesReadToday.text = viewModel.countReadPagesToday().toString()

        var pagesIncrement: Int = (viewModel.countReadPagesToday().toDouble() / viewModel.avgReadPagesByDay().toDouble() * 100).toInt()
        var pagesIncrementSign = '+'
        if (sign(pagesIncrement.toDouble()) == -1.0) {
            pagesIncrementSign = '-'
            binding.tvPagesReadTodayIncrement.setTextColor(resources.getColor(R.color.red, requireActivity().theme))
        }
        pagesIncrement = abs(pagesIncrement)
        if (pagesIncrement > 999)
            pagesIncrement = 999
        binding.tvPagesReadTodayIncrement.text = getString(R.string.ph_signed_percentage, pagesIncrementSign, pagesIncrement)

        binding.tvYear.text = LocalDate.now().year.toString()
        binding.tvBooksReadYear.text = viewModel.countReadBooksThisYear().toString()

        var bookIncrement: Int = (viewModel.countReadBooksThisYear().toDouble() / viewModel.avgReadBooksByYear().toDouble() * 100).toInt()
        var bookIncrementSign = '+'
        if (sign(bookIncrement.toDouble()) == -1.0) {
            bookIncrementSign = '-'
            binding.tvBooksReadYearIncrement.setTextColor(resources.getColor(R.color.red, requireActivity().theme))
        }
        bookIncrement = abs(bookIncrement)
        if (bookIncrement > 999)
            bookIncrement = 999
        binding.tvBooksReadYearIncrement.text = getString(R.string.ph_signed_percentage, bookIncrementSign, bookIncrement)

        val mostReadAuthor = viewModel.mostReadAuthor()
        binding.tvMostReadAuthor.text = mostReadAuthor ?: " "
        binding.tvTotalAuthors.text = getString(R.string.ph_total,viewModel.countAuthors())

        val mostReadGenre = viewModel.mostReadCategory()
        binding.tvMostReadGenre.text = mostReadGenre ?: "-"
        binding.tvTotalGenres.text = getString(R.string.ph_total,viewModel.countCategories())

        binding.tvTotalReadPages.text = getString(R.string.ph_total_pages, viewModel.countReadPages())
        binding.tvTotalReadBooks.text = getString(R.string.ph_total_books, viewModel.countReadBooks())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.default_action_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                findNavController().navigate(R.id.navigation_settings)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}