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

@AndroidEntryPoint
class AddEditTaskFragment : Fragment() {

    private var _binding: FragmentAddEditTaskBinding? = null
    private val binding get() = _binding!!

    private val taskViewModel: TaskViewModel by viewModels()
    private val subjectViewModel: SubjectViewModel by viewModels()

    private var selectedDeadline: Long = System.currentTimeMillis()
    private var selectedSubjectId: Long = -1L
    private var photoUri: Uri? = null
    private var photoPath: String? = null
    private var photoFile: File? = null // Пазим обекта на файла локално за по-сигурен път

    // Контракт за стартиране на камерата и обработка на резултата
    private val takePicture = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoUri?.let { uri ->
                // Показваме MaterialCard контейнера от новия заоблен дизайн
                binding.cardImageContainer.visibility = View.VISIBLE
                binding.ivTaskImage.load(uri) // Coil зарежда снимката асинхронно

                // Използваме абсолютния физически път към кеша за Room базата данни
                photoPath = photoFile?.absolutePath
            }
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

        setupSubjectDropdown()
        setupDatePicker()
        setupCamera()
        setupSaveButton()
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
                binding.spinnerSubject.setOnItemClickListener { _, _, position, _ ->
                    selectedSubjectId = subjects[position].id
                    // Изчистваме грешката при успешно избиране
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
            try {
                // Създаваме временен файл в сигурната кеш директория на UniPlanner
                val tempFile = File.createTempFile(
                    "task_image_", ".jpg",
                    requireContext().cacheDir
                )
                photoFile = tempFile

                // Генерираме защитено споделено URI чрез твоя FileProvider
                photoUri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.provider",
                    tempFile
                )

                // Стартираме системната камера
                takePicture.launch(photoUri)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Грешка при стартиране на камерата", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()

            // Валидация на задължителните полета
            if (title.isEmpty()) {
                binding.etTitle.error = "Заглавието е задължително"
                return@setOnClickListener
            }
            if (selectedSubjectId == -1L) {
                binding.spinnerSubject.error = "Избери предмет"
                return@setOnClickListener
            }

            // Извличане на избрания приоритет от Material 3 ChipGroup
            val priority = when (binding.chipGroupPriority.checkedChipId) {
                binding.chipLow.id -> Priority.LOW
                binding.chipHigh.id -> Priority.HIGH
                else -> Priority.MEDIUM // По подразбиране
            }

            // Създаваме новия обект за базата данни
            val task = Task(
                title = title,
                description = binding.etDescription.text.toString().trim(),
                subjectId = selectedSubjectId,
                deadline = selectedDeadline,
                priority = priority,
                imagePath = photoPath
            )

            // Записваме през ViewModel (това автоматично пуска и WorkManager нотификацията!)
            taskViewModel.insertTask(task)

            // Връщаме се обратно в предишния фрагмент (Dashboard или Tasks)
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}