package com.example.uniplanner.ui.subjects

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.uniplanner.data.local.entity.Subject
import com.example.uniplanner.databinding.DialogAddSubjectBinding
import com.example.uniplanner.databinding.FragmentSubjectsBinding
import com.example.uniplanner.ui.adapter.SubjectAdapter
import com.example.uniplanner.ui.viewmodel.SubjectViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SubjectsFragment : Fragment() {

    private var _binding: FragmentSubjectsBinding? = null
    private val binding get() = _binding!!
    private val subjectViewModel: SubjectViewModel by viewModels()
    private lateinit var subjectAdapter: SubjectAdapter

    private val colors = listOf(
        "#1565C0", "#AD1457", "#2E7D32",
        "#E65100", "#6A1B9A", "#00838F",
        "#F9A825", "#4E342E"
    )
    private var selectedColor = colors[0]

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubjectsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeSubjects()

        binding.fabAddSubject.setOnClickListener {
            showAddSubjectDialog()
        }
    }

    private fun setupRecyclerView() {
        subjectAdapter = SubjectAdapter(
            onSubjectClicked = { },
            onSubjectDeleted = { subject ->
                subjectViewModel.deleteSubject(subject)
                Toast.makeText(
                    requireContext(),
                    "${subject.name} изтрит",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
        binding.rvSubjects.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSubjects.adapter = subjectAdapter
    }

    private fun observeSubjects() {
        viewLifecycleOwner.lifecycleScope.launch {
            subjectViewModel.subjects.collect { subjects ->
                subjectAdapter.submitList(subjects)
            }
        }
    }

    private fun showAddSubjectDialog() {
        val dialogBinding = DialogAddSubjectBinding.inflate(layoutInflater)

        // Добави цветни кръгчета
        colors.forEach { colorHex ->
            val colorView = View(requireContext())
            val params = LinearLayout.LayoutParams(80, 80).apply {
                marginEnd = 16
            }
            colorView.layoutParams = params
            colorView.setBackgroundColor(Color.parseColor(colorHex))
            colorView.alpha = 0.4f
            colorView.setOnClickListener {
                selectedColor = colorHex
                // Покажи избрания цвят
                for (i in 0 until dialogBinding.colorPicker.childCount) {
                    dialogBinding.colorPicker.getChildAt(i).alpha = 0.4f
                }
                colorView.alpha = 1f
            }
            dialogBinding.colorPicker.addView(colorView)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Добави предмет")
            .setView(dialogBinding.root)
            .setPositiveButton("Запази") { _, _ ->
                val name = dialogBinding.etSubjectName.text.toString().trim()
                if (name.isEmpty()) {
                    Toast.makeText(requireContext(), "Въведи име!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val subject = Subject(
                    name = name,
                    teacher = dialogBinding.etTeacher.text.toString().trim(),
                    color = Color.parseColor(selectedColor)
                )
                subjectViewModel.insertSubject(subject)
            }
            .setNegativeButton("Отказ", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}