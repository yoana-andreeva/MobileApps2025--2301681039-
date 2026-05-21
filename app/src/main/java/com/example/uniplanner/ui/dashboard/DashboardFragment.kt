package com.example.uniplanner.ui.dashboard

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.uniplanner.R
import com.example.uniplanner.data.local.entity.Subject
import com.example.uniplanner.data.local.entity.TaskStatus
import com.example.uniplanner.databinding.FragmentDashboardBinding
import com.example.uniplanner.ui.adapter.TaskAdapter
import com.example.uniplanner.ui.viewmodel.TaskViewModel
import com.example.uniplanner.ui.viewmodel.SubjectViewModel
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val taskViewModel: TaskViewModel by viewModels()
    private val subjectViewModel: SubjectViewModel by viewModels()
    private lateinit var taskAdapter: TaskAdapter

    // Пазим текущо избрания предмет за филтриране (null означава "Всички")
    private var selectedSubjectId: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeData()

        binding.fabAddTask.setOnClickListener {
            findNavController().navigate(R.id.addEditTaskFragment)
        }
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            subjectColors = emptyMap(),
            subjectNames = emptyMap(),
            onTaskChecked = { task, isChecked ->
                val status = if (isChecked) TaskStatus.DONE else TaskStatus.PENDING
                taskViewModel.updateTaskStatus(task, status)
            },
            onTaskClicked = { /* За преглед/редакция */ },
            onTaskDeleted = { task -> taskViewModel.deleteTask(task) }
        )
        binding.rvUpcomingTasks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUpcomingTasks.adapter = taskAdapter
    }

    private fun observeData() {
        // 1. Следим предметите, за да генерираме чиповете и да дадем цвят на задачите
        viewLifecycleOwner.lifecycleScope.launch {
            subjectViewModel.subjects.collect { subjectsList ->
                val colorMap = subjectsList.associate { it.id to it.color }
                val nameMap = subjectsList.associate { it.id to it.name }
                taskAdapter.updateSubjectData(colorMap, nameMap)

                // Генерираме динамичните чипове най-отгоре
                setupSubjectChips(subjectsList)
            }
        }

        // 2. Статистика за горните две карти (Изпълнени/Изчакващи)
        viewLifecycleOwner.lifecycleScope.launch {
            taskViewModel.allTasks.collect { allTasks ->
                val completedCount = allTasks.count { it.status == TaskStatus.DONE }
                val pendingCount = allTasks.count { it.status != TaskStatus.DONE }

                binding.tvCountCompleted.text = completedCount.toString()
                binding.tvCountPending.text = pendingCount.toString()
            }
        }

        // 3. Зареждаме и филтрираме активните задачи в реално време
        viewLifecycleOwner.lifecycleScope.launch {
            taskViewModel.pendingTasks.collect { tasks ->
                filterAndSubmitTasks(tasks)
            }
        }
    }

    // Метод за динамично създаване на Material 3 чипове
    private fun setupSubjectChips(subjects: List<Subject>) {
        binding.chipGroupSubjects.removeAllViews()

        // 1. Създаваме първия главен чип "Всички"
        val allChip = Chip(requireContext()).apply {
            text = "Всички"
            isCheckable = true
            isChecked = selectedSubjectId == null
            setOnClickListener {
                selectedSubjectId = null
                refreshTasksList()
            }
        }
        binding.chipGroupSubjects.addView(allChip)

        // 2. Генерираме чип за всеки предмет от базата данни
        subjects.forEach { subject ->
            val chip = Chip(requireContext()).apply {
                text = subject.name
                isCheckable = true
                isChecked = selectedSubjectId == subject.id

                // Правим пастелен фон на чипа на базата на неговия оригинален цвят
                val pastelBg = ColorUtils.setAlphaComponent(subject.color, 45)
                chipBackgroundColor = ColorStateList.valueOf(pastelBg)
                setTextColor(ColorStateList.valueOf(subject.color))
                chipStrokeColor = ColorStateList.valueOf(subject.color)
                chipStrokeWidth = 2f

                setOnClickListener {
                    selectedSubjectId = subject.id
                    refreshTasksList()
                }
            }
            binding.chipGroupSubjects.addView(chip)
        }
    }

    // Помощен метод, който презарежда текущия списък със задачи според филтъра
    private fun refreshTasksList() {
        viewLifecycleOwner.lifecycleScope.launch {
            taskViewModel.pendingTasks.collect { tasks ->
                filterAndSubmitTasks(tasks)
            }
        }
    }

    // Реалното филтриране на списъка
    private fun filterAndSubmitTasks(tasks: List<com.example.uniplanner.data.local.entity.Task>) {
        val filteredList = if (selectedSubjectId == null) {
            tasks // Ако е "Всички", показваме целия списък
        } else {
            tasks.filter { it.subjectId == selectedSubjectId } // Иначе филтрираме по ID
        }
        taskAdapter.submitList(filteredList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}