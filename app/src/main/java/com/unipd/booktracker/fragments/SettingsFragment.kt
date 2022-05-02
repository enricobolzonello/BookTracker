package com.unipd.booktracker.fragments

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.unipd.booktracker.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_settings, rootKey)
    }

}