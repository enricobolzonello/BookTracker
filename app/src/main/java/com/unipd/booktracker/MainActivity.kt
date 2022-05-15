package com.unipd.booktracker

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.unipd.booktracker.databinding.ActivityMainBinding

class MainActivity: AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navController = (binding.navHostFragment.getFragment() as NavHostFragment).navController
        NavigationUI.setupWithNavController(binding.navView, navController)
    }

    fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        return activeNetwork != null && (
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.action_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                //navController.navigate(R.id.navigation_settings)
                true
            }
            else -> super.onOptionsItemSelected(item)

        }
    }

    fun setBottomNavVisibility(navViewVisibility: Int) {
        if (navViewVisibility == View.VISIBLE || navViewVisibility == View.GONE)
            binding.navView.visibility = navViewVisibility
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}