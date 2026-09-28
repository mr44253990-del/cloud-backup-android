package cloud.rakib.backup.ui.mode

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cloud.rakib.backup.CloudBackupApp
import cloud.rakib.backup.databinding.ActivityModeSelectionBinding
import cloud.rakib.backup.ui.auth.GoogleSignInActivity
import cloud.rakib.backup.ui.main.MainActivity

class ModeSelectionActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityModeSelectionBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModeSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
    }
    
    private fun setupUI() {
        binding.btnOffline.setOnClickListener {
            selectOfflineMode()
        }
        
        binding.btnCloud.setOnClickListener {
            selectCloudMode()
        }
    }
    
    private fun selectOfflineMode() {
        CloudBackupApp.getPreferences().edit().apply {
            putBoolean("mode_selected", true)
            putBoolean("is_cloud_mode", false)
            apply()
        }
        
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
    
    private fun selectCloudMode() {
        CloudBackupApp.getPreferences().edit().apply {
            putBoolean("mode_selected", true)
            putBoolean("is_cloud_mode", true)
            apply()
        }
        
        startActivity(Intent(this, GoogleSignInActivity::class.java))
        finish()
    }
}
