package com.example.uniplanner.ui.tasks

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.uniplanner.R
import com.example.uniplanner.data.local.entity.Priority
import com.example.uniplanner.data.local.entity.Task
import com.example.uniplanner.databinding.FragmentAddEditTaskBinding
import com.example.uniplanner.ui.viewmodel.SubjectViewModel
import com.example.uniplanner.ui.viewmodel.TaskViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat


@AndroidEntryPoint
class AddEditTaskFragment : Fragment() {

    private var _binding: FragmentAddEditTaskBinding? = null
    private val binding get() = _binding!!

    private val taskViewModel: TaskViewModel by viewModels()
    private val subjectViewModel: SubjectViewModel by viewModels()

    private var selectedDeadline: Long = System.currentTimeMillis()
    private var selectedSubjectId: Long = -1L
    private var taskToEdit: Long = -1L
    private var photoUri: Uri? = null
    private var photoPath: String? = null
    private var photoFile: File? = null

    private val takePicture = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoUri?.let { uri ->
                binding.cardImageContainer.visibility = View.VISIBLE
                binding.ivTaskImage.load(uri)
                photoPath = photoFile?.absolutePath
            }
        }
    }

    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(
                requireContext(),
                "Необходимо е разрешение за камера",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        taskToEdit = arguments?.getLong("taskId", -1L) ?: -1L

        if (taskToEdit != -1L) {
            // Edit режим
            binding.tvScreenTitle.text = "Редактирай задача"
            binding.btnSave.text = "Запази промените"
            loadTaskForEdit(taskToEdit)
        } else {
            // Create режим
            binding.tvScreenTitle.text = "Нова задача"
            binding.btnSave.text = "Създай задача"
            binding.btnBack.setOnClickListener {
                findNavController().navigateUp()
            }
        }

        setupSubjectDropdown()
        setupDatePicker()
        setupCamera()
        setupSaveButton()
    }

    private fun loadTaskForEdit(taskId: Long) {
        viewLifecycleOwner.lifecycleScope.launch {
            val task = taskViewModel.getTaskById(taskId) ?: return@launch

            binding.etTitle.setText(task.title)
            binding.etDescription.setText(task.description)
            selectedDeadline = task.deadline
            selectedSubjectId = task.subjectId
            photoPath = task.imagePath

            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            binding.etDeadline.setText(formatter.format(Date(task.deadline)))

            when (task.priority) {
                Priority.LOW -> binding.chipLow.isChecked = true
                Priority.HIGH -> binding.chipHigh.isChecked = true
                else -> binding.chipMedium.isChecked = true
            }

            if (!task.imagePath.isNullOrEmpty()) {
                binding.cardImageContainer.visibility = View.VISIBLE
                binding.ivTaskImage.load(task.imagePath)
            }
        }
    }

    private fun setupSubjectDropdown() {
        viewLifecycleOwner.lifecycleScope.launch {
            subjectViewModel.subjects.collect { subjects ->
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    subjects.map { it.name }
                )
                binding.spinnerSubject.setAdapter(adapter)

                // Ако редактираме — покажи текущия предмет
                if (taskToEdit != -1L) {
                    val task = taskViewModel.getTaskById(taskToEdit)
                    val subjectIndex = subjects.indexOfFirst { it.id == task?.subjectId }
                    if (subjectIndex >= 0) {
                        binding.spinnerSubject.setText(subjects[subjectIndex].name, false)
                    }
                }

                binding.spinnerSubject.setOnItemClickListener { _, _, position, _ ->
                    selectedSubjectId = subjects[position].id
                    binding.spinnerSubject.error = null
                }
            }
        }
    }

    private fun setupDatePicker() {
        binding.etDeadline.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    selectedDeadline = calendar.timeInMillis
                    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    binding.etDeadline.setText(formatter.format(Date(selectedDeadline)))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupCamera() {
        binding.btnPickImage.setOnClickListener {
            // Проверяваме дали имаме разрешение
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                launchCamera()
            } else {
                requestCameraPermission.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun launchCamera() {
        try {
            val tempFile = File.createTempFile(
                "task_image_", ".jpg",
                requireContext().cacheDir
            )
            photoFile = tempFile
            photoUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                tempFile
            )
            takePicture.launch(photoUri)
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Грешка при стартиране на камерата",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()

            if (title.isEmpty()) {
                binding.etTitle.error = "Заглавието е задължително"
                return@setOnClickListener
            }
            if (selectedSubjectId == -1L) {
                binding.spinnerSubject.error = "Избери предмет"
                return@setOnClickListener
            }

            val priority = when (binding.chipGroupPriority.checkedChipId) {
                binding.chipLow.id -> Priority.LOW
                binding.chipHigh.id -> Priority.HIGH
                else -> Priority.MEDIUM
            }

            if (taskToEdit != -1L) {
                viewLifecycleOwner.lifecycleScope.launch {
                    val existing = taskViewModel.getTaskById(taskToEdit) ?: return@launch
                    val updated = existing.copy(
                        title = title,
                        description = binding.etDescription.text.toString().trim(),
                        subjectId = selectedSubjectId,
                        deadline = selectedDeadline,
                        priority = priority,
                        imagePath = photoPath ?: existing.imagePath
                    )
                    taskViewModel.updateTask(updated)
                    findNavController().navigateUp()
                }
            } else {
                val task = Task(
                    title = title,
                    description = binding.etDescription.text.toString().trim(),
                    subjectId = selectedSubjectId,
                    deadline = selectedDeadline,
                    priority = priority,
                    imagePath = photoPath
                )
                taskViewModel.insertTask(task)
                findNavController().navigateUp()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}