package com.unipd.booktracker.ui.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.unipd.booktracker.MainActivity
import com.unipd.booktracker.R

class SettingsFragment : PreferenceFragmentCompat() {
    private lateinit var viewModel: SettingsViewModel
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(requireActivity() as MainActivity)[SettingsViewModel::class.java]

        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data as Intent
                val path = intent.data?.path
                if (path != null)
                    if (viewModel.importDbFromFile(path.substringAfter(":")))
                        Toast.makeText(this.context, getString(R.string.file_imported), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_settings, rootKey)
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        when(preference.key) {
            "export" -> {
                val path = (viewModel.exportDbToFile())
                path?.let {
                    Toast.makeText(this.context, getString(R.string.file_exported, it), Toast.LENGTH_SHORT).show()
                }
                return true
            }
            "import" -> {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "application/octet-stream"
                resultLauncher.launch(intent)
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