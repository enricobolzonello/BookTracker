package com.unipd.booktracker.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceFragmentCompat
import com.unipd.booktracker.R
import com.unipd.booktracker.databinding.FragmentBookDetailBinding
import com.unipd.booktracker.databinding.FragmentLibraryBinding
import com.unipd.booktracker.databinding.FragmentSettingsBinding

class SettingsFragment : PreferenceFragmentCompat() {

    // https://developer.android.com/guide/topics/ui/settings <-- guida per la pagina delle impostazioni
    private lateinit var binding: FragmentSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = FragmentSettingsBinding.inflate(layoutInflater)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_settings, rootKey)
    }
}