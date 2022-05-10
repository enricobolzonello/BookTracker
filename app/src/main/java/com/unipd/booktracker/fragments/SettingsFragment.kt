package com.unipd.booktracker.fragments

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.unipd.booktracker.BookViewModel
import com.unipd.booktracker.MainActivity
import com.unipd.booktracker.R

class SettingsFragment : PreferenceFragmentCompat() {
    private lateinit var viewModel: BookViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(requireActivity() as MainActivity)[BookViewModel::class.java]
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_settings, rootKey)
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        when(preference.key) {
            "export" -> {
                viewModel.exportDbToFile()
                return true
            }
            "import" -> {
                viewModel.importDbFromFile()
                return true
            }
            "clear" -> {
                viewModel.clearBooks()
                return true
            }
            else -> {
                return  super.onPreferenceTreeClick(preference)
            }
        }
    }
}