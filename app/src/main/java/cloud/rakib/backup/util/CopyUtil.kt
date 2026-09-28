package cloud.rakib.backup.util

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import cloud.rakib.backup.R

object CopyUtil {

    fun copyToClipboard(context: Context, label: String, text: String) {
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = android.content.ClipData.newPlainText(label, text)
        clipboardManager.setPrimaryClip(clip)
        Toast.makeText(context, context.getString(R.string.copied), Toast.LENGTH_SHORT).show()
    }
}