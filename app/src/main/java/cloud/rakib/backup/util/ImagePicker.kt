package cloud.rakib.backup.util

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

object ImagePicker {
    
    private var onImagePicked: ((Uri) -> Unit)? = null
    
    fun pick(fragment: Fragment, onImage: (Uri) -> Unit) {
        onImagePicked = onImage
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        fragment.startActivityForResult(intent, 1001)
    }
    
    fun pick(activity: Activity, onImage: (Uri) -> Unit) {
        onImagePicked = onImage
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        activity.startActivityForResult(intent, 1001)
    }
    
    fun handleActivityResult(fragment: Fragment, requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                onImagePicked?.invoke(uri)
            }
        }
    }
    
    fun register(activity: Activity, callback: (Uri) -> Unit): ActivityResultLauncher<String> {
        onImagePicked = callback
        return (activity as? androidx.activity.ComponentActivity)?.registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let { onImagePicked?.invoke(it) }
        } ?: throw IllegalArgumentException("Activity must be ComponentActivity")
    }
}