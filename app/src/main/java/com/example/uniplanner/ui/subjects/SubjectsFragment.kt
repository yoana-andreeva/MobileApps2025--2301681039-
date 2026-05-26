package com.example.uniplanner.ui.subjects

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.uniplanner.R
import com.example.uniplanner.data.local.entity.Subject
import com.example.uniplanner.databinding.FragmentSubjectsBinding
import com.example.uniplanner.databinding.DialogAddSubjectBinding
import com.example.uniplanner.ui.adapter.SubjectAdapter
import com.example.uniplanner.ui.viewmodel.SubjectViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SubjectsFragment : Fragment() {

    private var _binding: FragmentSubjectsBinding? = null
    private val binding get() = _binding!!

    // Свързваме се с твоя съществуващ ViewModel
    private val viewModel: SubjectViewModel by viewModels()
    private lateinit var subjectAdapter: SubjectAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubjectsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeSubjects()

        // При натискане на FAB бутона отваряме диалога
        binding.fabAddSubject.setOnClickListener {
            showAddSubjectDialog()
        }
    }

    private fun setupRecyclerView() {
        subjectAdapter = SubjectAdapter { subject ->
            viewModel.deleteSubject(subject)
            Toast.makeText(requireContext(), "Предметът беше изтрит", Toast.LENGTH_SHORT).show()
        }
        binding.rvSubjects.adapter = subjectAdapter
    }

    private fun observeSubjects() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.subjects.collect { list ->
                subjectAdapter.submitList(list)
                binding.emptyStateSubjects.visibility =
                    if (list.isEmpty()) View.VISIBLE else View.GONE
                binding.rvSubjects.visibility =
                    if (list.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    private fun showAddSubjectDialog() {
        val dialogBinding = DialogAddSubjectBinding.inflate(layoutInflater)

        // 1. Вземаме точно твоите 8 пастелни цвята от colors.xml
        val colorResIds = listOf(
            R.color.subject_1, R.color.subject_2, R.color.subject_3, R.color.subject_4,
            R.color.subject_5, R.color.subject_6, R.color.subject_7, R.color.subject_8
        )

        // Превръщаме ги в реални Color стойности (Int)
        val colors = colorResIds.map { ContextCompat.getColor(requireContext(), it) }
        var selectedColor = colors[0] // По подразбиране е избран първият цвят

        // Списък, в който ще пазим визуалните кръгчета, за да им махаме контура при превключване
        val colorViews = mutableListOf<View>()

        // 2. Програмно генериране на пастелните кръгчета в colorPicker контейнера
        colors.forEachIndexed { index, color ->
            val colorView = View(requireContext()).apply {
                // Създаваме размери за кръгчето (36dp x 36dp)
                val size = (36 * resources.displayMetrics.density).toInt()
                val margin = (8 * resources.displayMetrics.density).toInt()

                val layoutParams = LinearLayout.LayoutParams(size, size).apply {
                    setMargins(margin, 0, margin, 0)
                }
                this.layoutParams = layoutParams

                // Задаваме му формата circle_shape, която създадохме
                setBackgroundResource(R.drawable.circle_shape)
                backgroundTintList = android.content.res.ColorStateList.valueOf(color)

                // Ако е първият цвят, му слагаме маркер за селекция (черен контур)
                if (index == 0) {
                    elevation = 6f
                    setPadding(4, 4, 4, 4)
                    // Тъй като използваме вграден shape, за контур можем леко да променим elevation или скала
                    scaleX = 1.1f
                    scaleY = 1.1f
                }

                // Логика при кликване върху цвят
                setOnClickListener {
                    selectedColor = color

                    colorViews.forEachIndexed { i, v ->
                        v.scaleX = 1.0f
                        v.scaleY = 1.0f
                        v.elevation = 0f
                        v.setBackgroundResource(R.drawable.circle_shape)
                        v.backgroundTintList = android.content.res.ColorStateList.valueOf(colors[i])
                    }

                    scaleX = 1.15f
                    scaleY = 1.15f
                    elevation = 8f
                    setBackgroundResource(R.drawable.circle_selected)
                    backgroundTintList = android.content.res.ColorStateList.valueOf(color)
                }
            }

            colorViews.add(colorView)
            dialogBinding.colorPicker.addView(colorView) // Добавяме го в хоризонталния LinearLayout
        }

        // 3. Показване на самия Material 3 Диалог
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Добавяне на предмет")
            .setView(dialogBinding.root)
            .setPositiveButton("Добави") { _, _ ->
                val name = dialogBinding.etSubjectName.text.toString().trim()
                val teacher = dialogBinding.etTeacher.text.toString().trim()

                if (name.isNotEmpty()) {
                    viewModel.insertSubject(Subject(name = name, teacher = teacher, color = selectedColor))
                } else {
                    Toast.makeText(requireContext(), "Името на предмета е задължително!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отказ", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}