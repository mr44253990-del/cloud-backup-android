package cloud.rakib.backup.ui.editor

import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import cloud.rakib.backup.CloudBackupApp
import cloud.rakib.backup.R
import cloud.rakib.backup.data.RepositoryProvider
import cloud.rakib.backup.data.model.Category
import cloud.rakib.backup.databinding.DialogAddCategoryBinding

class AddCategoryDialogFragment : DialogFragment() {

    private var _binding: DialogAddCategoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            if (TextUtils.isEmpty(name)) {
                Toast.makeText(context, "Please enter a category name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val color = binding.colorPicker.text.toString().trim()
            val category = Category(
                name = name,
                color = if (color.isNotEmpty()) color else "#6366F1"
            )

            val categoryRepository = RepositoryProvider.provideCategoryRepository(requireContext())
            categoryRepository.insertCategory(category)

            dismiss()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = AddCategoryDialogFragment()
    }
}