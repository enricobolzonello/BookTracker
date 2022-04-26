package com.unipd.booktracker

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.unipd.booktracker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val viewModel : BookViewModel by viewModels()
    private lateinit var binding : ActivityMainBinding
    private lateinit var navController : NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navController = (binding.navHostFragment.getFragment() as NavHostFragment).navController
        NavigationUI.setupWithNavController(binding.navView, navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when(destination.id) {
                R.id.navigation_settings -> {
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
                    supportActionBar?.setTitle(R.string.title_settings)
                    binding.navView.visibility = View.GONE
                }
                R.id.navigation_book_detail -> {
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
                    supportActionBar?.setTitle(R.string.title_bookdetail)
                    binding.navView.visibility = View.GONE
                }
                else -> {
                    supportActionBar?.setDisplayHomeAsUpEnabled(false)
                    supportActionBar?.setTitle(R.string.app_name)
                    binding.navView.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onSupportNavigateUp() : Boolean {
        onBackPressed()
        return true
    }
}