package cloud.rakib.backup.ui.settings

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import cloud.rakib.backup.CloudBackupApp
import cloud.rakib.backup.R
import cloud.rakib.backup.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupUI()
    }

    private fun setupUI() {
        val prefs = CloudBackupApp.getPreferences()
        val isCloudMode = prefs.getBoolean("is_cloud_mode", false)
        val isSignedIn = prefs.getBoolean("is_signed_in", false)

        // Account section
        if (isCloudMode && isSignedIn) {
            binding.tvAccountStatus.text = prefs.getString("user_email", "Signed in") ?: "Signed in"
        } else if (isCloudMode) {
            binding.tvAccountStatus.text = "Cloud Mode"
        } else {
            binding.tvAccountStatus.text = "Offline Mode"
        }

        binding.btnSwitchMode.setOnClickListener {
            showModeSwitchDialog()
        }

        binding.btnSignOut.setOnClickListener {
            if (isSignedIn) {
                showSignOutConfirmDialog()
            } else {
                Toast.makeText(this, "Not signed in", Toast.LENGTH_SHORT).show()
            }
        }

        // Appearance section
        binding.switchDarkMode.isChecked = prefs.getString("theme", "system") == "dark"
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            val theme = if (isChecked) "dark" else "light"
            prefs.edit().putString("theme", theme).apply()
        }

        // Backup section
        binding.switchAutoBackup.isChecked = prefs.getBoolean("auto_backup", true)
        binding.switchAutoBackup.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("auto_backup", isChecked).apply()
        }

        binding.switchWifiOnly.isChecked = prefs.getBoolean("wifi_only", true)
        binding.switchWifiOnly.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("wifi_only", isChecked).apply()
        }

        binding.btnClearCache.setOnClickListener {
            Toast.makeText(this, "Cache cleared", Toast.LENGTH_SHORT).show()
        }

        binding.btnExportData.setOnClickListener {
            Toast.makeText(this, "Export feature coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showModeSwitchDialog() {
        val prefs = CloudBackupApp.getPreferences()
        val currentMode = if (prefs.getBoolean("is_cloud_mode", false)) "cloud" else "offline"

        val modes = arrayOf("Offline Mode", "Cloud Mode")
        val selectedIndex = if (currentMode == "cloud") 1 else 0

        AlertDialog.Builder(this)
            .setTitle("Switch Mode")
            .setSingleChoiceItems(modes, selectedIndex) { dialog, which ->
                val isCloud = which == 1
                prefs.edit().apply {
                    putBoolean("is_cloud_mode", isCloud)
                    putBoolean("mode_selected", true)
                    apply()
                }
                dialog.dismiss()
                
                if (isCloud && !prefs.getBoolean("is_signed_in", false)) {
                    // Redirect to sign in
                    val signInIntent = Intent(this, cloud.rakib.backup.ui.auth.GoogleSignInActivity::class.java)
                    startActivity(signInIntent)
                }
                
                Toast.makeText(this, if (isCloud) "Switched to Cloud Mode" else "Switched to Offline Mode", Toast.LENGTH_SHORT).show()
                recreate()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSignOutConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("Confirm Sign Out")
            .setMessage("Are you sure you want to sign out?")
            .setPositiveButton("Sign Out") { _, _ ->
                val prefs = CloudBackupApp.getPreferences()
                prefs.edit().apply {
                    putBoolean("is_signed_in", false)
                    remove("user_email")
                    remove("user_name")
                    remove("user_photo")
                    remove("google_id")
                    apply()
                }
                Toast.makeText(this, "Signed out", Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

