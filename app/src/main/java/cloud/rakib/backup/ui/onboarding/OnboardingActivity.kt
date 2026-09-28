package cloud.rakib.backup.ui.onboarding

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import cloud.rakib.backup.CloudBackupApp
import cloud.rakib.backup.R
import cloud.rakib.backup.databinding.ActivityOnboardingBinding
import cloud.rakib.backup.ui.mode.ModeSelectionActivity

class OnboardingActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityOnboardingBinding
    private var storagePermissionGranted = false
    private var notificationPermissionGranted = false
    
    companion object {
        private const val REQUEST_STORAGE = 100
        private const val REQUEST_NOTIFICATION = 101
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        checkPermissions()
    }
    
    private fun setupUI() {
        binding.btnGrantStorage.setOnClickListener {
            requestStoragePermission()
        }
        
        binding.btnGrantNotification.setOnClickListener {
            requestNotificationPermission()
        }
        
        binding.btnContinue.setOnClickListener {
            if (storagePermissionGranted) {
                completeOnboarding()
            } else {
                Toast.makeText(this, "Please grant storage permission", Toast.LENGTH_SHORT).show()
            }
        }
        
        binding.btnSkip.setOnClickListener {
            completeOnboarding()
        }
    }
    
    private fun checkPermissions() {
        storagePermissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
        
        notificationPermissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        
        updateUI()
    }
    
    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                REQUEST_STORAGE
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_STORAGE
            )
        }
    }
    
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_NOTIFICATION
            )
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            REQUEST_STORAGE -> {
                storagePermissionGranted = grantResults.isNotEmpty() && 
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
            }
            REQUEST_NOTIFICATION -> {
                notificationPermissionGranted = grantResults.isNotEmpty() && 
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
            }
        }
        
        updateUI()
    }
    
    private fun updateUI() {
        binding.btnGrantStorage.isEnabled = !storagePermissionGranted
        binding.btnGrantStorage.text = if (storagePermissionGranted) "✓ Granted" else getString(R.string.grant_permission)
        
        binding.btnGrantNotification.isEnabled = !notificationPermissionGranted
        binding.btnGrantNotification.text = if (notificationPermissionGranted) "✓ Granted" else getString(R.string.grant_permission)
    }
    
    private fun completeOnboarding() {
        CloudBackupApp.getPreferences().edit().apply {
            putBoolean("first_launch_done", true)
            apply()
        }
        
        startActivity(Intent(this, ModeSelectionActivity::class.java))
        finish()
    }
}
