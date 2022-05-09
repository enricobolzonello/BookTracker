package com.unipd.booktracker.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.unipd.booktracker.BookViewModel
import com.unipd.booktracker.MainActivity
import com.unipd.booktracker.R
import com.unipd.booktracker.databinding.FragmentStatsBinding
import java.time.LocalDate

class StatsFragment : Fragment() {
    private lateinit var viewModel: BookViewModel
    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        viewModel = ViewModelProvider(requireActivity() as MainActivity)[BookViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvPagesReadToday.text = viewModel.countReadPagesToday().toString()
        var pagesReadTodayIncrement: Int = (viewModel.countReadPagesToday().toDouble() / viewModel.avgReadPagesByDay().toDouble() * 100).toInt()
        if(pagesReadTodayIncrement > 999){
            pagesReadTodayIncrement = 999
        }
        binding.tvPagesReadTodayIncrement.text = getString(R.string.ph_percentage, pagesReadTodayIncrement)
        binding.tvYear.text = LocalDate.now().year.toString()
        binding.tvBooksReadYear.text = viewModel.countReadBooksThisYear().toString()
        val booksReadYearIncrement: Int = (viewModel.countReadBooksThisYear().toDouble() / viewModel.avgReadBooksByYear().toDouble() * 100).toInt()
        binding.tvBooksReadYearIncrement.text = getString(R.string.ph_percentage, booksReadYearIncrement)
        binding.tvMostReadAuthor.text = viewModel.mostReadAuthor()
        binding.tvTotalAuthors.text = viewModel.countAuthors().toString()
        binding.tvMostReadGenre.text = viewModel.mostReadCategory()
        binding.tvTotalGenres.text = viewModel.countCategories().toString()
        binding.tvTotalReadPages.text = viewModel.countReadPages().toString()
        binding.tvTotalReadBooks.text = viewModel.countReadBooks().toString()
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