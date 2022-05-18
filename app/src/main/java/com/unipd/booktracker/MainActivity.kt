package com.unipd.booktracker

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
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

        if (BookUtils.isLargeScreen(this))
            binding.railNav?.let { NavigationUI.setupWithNavController(it, navController) }
        else
            binding.bottomNav?.let { NavigationUI.setupWithNavController(it, navController) }
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
                true
            }
            else -> super.onOptionsItemSelected(item)

        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    fun setNavVisibility(navViewVisibility: Int) {
        if (navViewVisibility == View.VISIBLE || navViewVisibility == View.GONE) {
            binding.railNav?.visibility = navViewVisibility
            binding.bottomNav?.visibility = navViewVisibility
        }
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