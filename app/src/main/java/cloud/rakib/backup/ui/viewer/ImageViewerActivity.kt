package cloud.rakib.backup.ui.viewer

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cloud.rakib.backup.R
import cloud.rakib.backup.databinding.ActivityImageViewerBinding
import cloud.rakib.backup.util.CopyUtil

class ImageViewerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageViewerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val imageUri = intent.getStringExtra("image_uri")
        if (imageUri != null) {
            // Load image using Glide
            com.bumptech.glide.Glide.with(this)
                .load(imageUri)
                .placeholder(R.drawable.ic_image_24)
                .into(binding.ivImage)
        }

        binding.btnCopy.setOnClickListener {
            if (imageUri != null) {
                CopyUtil.copyToClipboard(this, "image_uri", imageUri)
                Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnDownload.setOnClickListener {
            Toast.makeText(this, "Download feature coming soon", Toast.LENGTH_SHORT).show()
        }

        // Fullscreen mode
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}