package cloud.rakib.backup.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import cloud.rakib.backup.CloudBackupApp
import cloud.rakib.backup.R
import cloud.rakib.backup.databinding.ActivityGoogleSignInBinding
import cloud.rakib.backup.ui.main.MainActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.launch

class GoogleSignInActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityGoogleSignInBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var signInLauncher: ActivityResultLauncher<Intent>
    
    companion object {
        private const val RC_SIGN_IN = 9001
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoogleSignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupGoogleSignIn()
        setupUI()
    }
    
    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE), Scope(DriveScopes.DRIVE_APPDATA))
            .build()
        
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }
    
    private fun setupUI() {
        binding.btnSignIn.setOnClickListener {
            signIn()
        }
    }
    
    private fun signIn() {
        binding.btnSignIn.isEnabled = false
        binding.btnSignIn.text = getString(R.string.signing_in)
        
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                handleSignInSuccess(account)
            } catch (e: ApiException) {
                handleSignInFailure()
            }
        }
    }
    
    private fun handleSignInSuccess(account: GoogleSignInAccount) {
        lifecycleScope.launch {
            // Save user info
            CloudBackupApp.getPreferences().edit().apply {
                putBoolean("is_signed_in", true)
                putString("user_email", account.email)
                putString("user_name", account.displayName)
                putString("user_photo", account.photoUrl?.toString())
                putString("google_id", account.id)
                apply()
            }
            
            Toast.makeText(this@GoogleSignInActivity, "Signed in successfully", Toast.LENGTH_SHORT).show()
            
            startActivity(Intent(this@GoogleSignInActivity, MainActivity::class.java))
            finish()
        }
    }
    
    private fun handleSignInFailure() {
        binding.btnSignIn.isEnabled = true
        binding.btnSignIn.text = getString(R.string.sign_in_with_google)
        Toast.makeText(this, getString(R.string.sign_in_failed), Toast.LENGTH_SHORT).show()
    }
}