package com.unipd.booktracker.ui.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import androidx.preference.DropDownPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.unipd.booktracker.BookUtils
import com.unipd.booktracker.R
import com.unipd.booktracker.SettingsActivity

class SettingsFragment: PreferenceFragmentCompat() {
    private lateinit var viewModel: SettingsViewModel
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(requireActivity() as SettingsActivity)[SettingsViewModel::class.java]
        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data as Intent
                val path = intent.data?.path
                if (path != null)
                    if (viewModel.importDbFromFile(path.substringAfter(":")))
                        Toast.makeText(this.context, getString(R.string.file_imported), Toast.LENGTH_SHORT).show()
            }
        }

        val appThemeList = findPreference<DropDownPreference>(getString(R.string.app_theme_key))
        appThemeList?.setOnPreferenceChangeListener { _, newValue ->
            BookUtils.setModeNight(requireContext(), newValue as String)
            true
        }
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        return when(preference.key) {
            getString(R.string.export_books_key) -> {
                val path = (viewModel.exportDbToFile())
                path?.let {
                    Toast.makeText(this.context, getString(R.string.file_exported, it), Toast.LENGTH_SHORT).show()
                }
                true
            }
            getString(R.string.import_books_key) -> {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "application/octet-stream"
                resultLauncher.launch(intent)
                true
            }
            getString(R.string.clear_books_key) -> {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(resources.getString(R.string.clear_books_dialog_title))
                    .setMessage(resources.getString(R.string.clear_books_dialog_message))
                    .setIcon(ResourcesCompat.getDrawable(resources, R.drawable.ic_clear, requireContext().theme))
                    .setNegativeButton(resources.getString(R.string.no)) { dialog, which ->
                        // Respond to negative button press
                    }
                    .setPositiveButton(resources.getString(R.string.yes)) { dialog, which ->
                        // Respond to positive button press
                        viewModel.clearBooks()
                    }
                    .show()
                true
            }
            else -> super.onPreferenceTreeClick(preference)
        }
    }
}