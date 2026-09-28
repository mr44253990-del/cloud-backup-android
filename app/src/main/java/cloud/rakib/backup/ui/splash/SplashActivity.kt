package cloud.rakib.backup.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import cloud.rakib.backup.CloudBackupApp
import cloud.rakib.backup.databinding.ActivitySplashBinding
import cloud.rakib.backup.ui.main.MainActivity
import cloud.rakib.backup.ui.mode.ModeSelectionActivity
import cloud.rakib.backup.ui.onboarding.OnboardingActivity

class SplashActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySplashBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Navigate after delay
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToNextScreen()
        }, 2000)
    }
    
    private fun navigateToNextScreen() {
        val prefs = CloudBackupApp.getPreferences()
        val isFirstLaunch = !prefs.getBoolean("first_launch_done", false)
        val hasSelectedMode = prefs.getBoolean("mode_selected", false)
        
        val intent = when {
            isFirstLaunch -> Intent(this, OnboardingActivity::class.java)
            !hasSelectedMode -> Intent(this, ModeSelectionActivity::class.java)
            else -> Intent(this, MainActivity::class.java)
        }
        
        startActivity(intent)
        finish()
    }
}
