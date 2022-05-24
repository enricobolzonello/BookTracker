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
import com.unipd.booktracker.R
import com.unipd.booktracker.SettingsActivity
import com.unipd.booktracker.util.setModeNight

class SettingsFragment : PreferenceFragmentCompat() {
    private lateinit var viewModel: SettingsViewModel
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(requireActivity() as SettingsActivity)[SettingsViewModel::class.java]

        // Catching the selected file when reentering app after the import Intent.ACTION_GET_CONTENT
        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data as Intent
                val path = intent.data?.path
                if (path != null)
                    if (viewModel.importDbFromFile(path.substringAfter(":")))
                        Toast.makeText(requireContext(), getString(R.string.file_imported), Toast.LENGTH_SHORT).show()
            }
        }

        findPreference<DropDownPreference>(getString(R.string.app_theme_key))?.apply {
            setOnPreferenceChangeListener { _, newValue ->
                requireContext().setModeNight(newValue as String)
                true
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        return when (preference.key) {
            getString(R.string.export_books_key) -> {
                val path = (viewModel.exportDbToFile())
                path?.let {
                    Toast.makeText(requireContext(), getString(R.string.file_exported, it), Toast.LENGTH_SHORT).show()
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
                    .setNegativeButton(resources.getString(R.string.no)) { _, _ -> }
                    .setPositiveButton(resources.getString(R.string.yes)) { _, _ ->
                        viewModel.clearBooks()
                        Toast.makeText(requireContext(), getString(R.string.all_books_deleted), Toast.LENGTH_SHORT).show()
                    }
                    .show()
                true
            }
            else -> super.onPreferenceTreeClick(preference)
        }
    }
}