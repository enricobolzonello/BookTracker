package com.unipd.booktracker

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.unipd.booktracker.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var navController : NavController
    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navController = (binding.navHostFragment.getFragment() as NavHostFragment).navController
        NavigationUI.setupWithNavController(binding.navView, navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when(destination.id) {
                R.id.navigation_settings -> {
                    binding.navView.visibility = View.GONE
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
                    supportActionBar?.setTitle(R.string.settings)
                }
                R.id.navigation_book_detail -> {
                    binding.navView.visibility = View.GONE
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
                    supportActionBar?.setTitle(R.string.book_detail)
                }
                else -> {
                    binding.navView.visibility = View.VISIBLE
                    supportActionBar?.setDisplayHomeAsUpEnabled(false)
                    supportActionBar?.setTitle(R.string.app_name)
                }
            }
        }
    }

    override fun onSupportNavigateUp() : Boolean {
        onBackPressed()
        return true
    }

    fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        return activeNetwork != null && (
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        )
    }
}