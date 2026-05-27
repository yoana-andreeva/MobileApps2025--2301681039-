package com.example.uniplanner.ui.dashboard

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
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
import com.example.uniplanner.ui.viewmodel.SubjectViewModel
import com.example.uniplanner.ui.viewmodel.TaskViewModel
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val taskViewModel: TaskViewModel by viewModels()
    private val subjectViewModel: SubjectViewModel by viewModels()
    private lateinit var taskAdapter: TaskAdapter
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
        setupCalendarStrip()

        binding.cardCalendarStrip.setOnClickListener {
            findNavController().navigate(R.id.calendarFragment)
        }

        binding.cardCompleted.setOnClickListener {
            val done = taskViewModel.allTasks.value.filter { it.status == TaskStatus.DONE }
            taskAdapter.submitList(done)
            binding.tvUpcomingTitle.text = "Изпълнени задачи"
        }

        binding.cardPending.setOnClickListener {
            val pending = taskViewModel.pendingTasks.value
            taskAdapter.submitList(pending)
            binding.tvUpcomingTitle.text = "Предстоящи задачи"
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
            onTaskClicked = { },
            onTaskDeleted = { task -> taskViewModel.deleteTask(task) }
        )
        binding.rvUpcomingTasks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUpcomingTasks.adapter = taskAdapter
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            subjectViewModel.subjects.collect { subjectsList ->
                val colorMap = subjectsList.associate { it.id to it.color }
                val nameMap = subjectsList.associate { it.id to it.name }
                taskAdapter.updateSubjectData(colorMap, nameMap)
                setupSubjectChips(subjectsList)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            taskViewModel.allTasks.collect { allTasks ->
                val completedCount = allTasks.count { it.status == TaskStatus.DONE }
                val pendingCount = allTasks.count { it.status != TaskStatus.DONE }
                binding.tvCountCompleted.text = completedCount.toString()
                binding.tvCountPending.text = pendingCount.toString()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            taskViewModel.pendingTasks.collect { tasks ->
                filterAndSubmitTasks(tasks)
            }
        }
    }

    private fun setupCalendarStrip() {
        val calendar = Calendar.getInstance()
        val monthFormatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        binding.tvCalendarMonth.text = "${monthFormatter.format(calendar.time)} ›"

        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)

        val dayNames = listOf("Нд", "Пн", "Вт", "Ср", "Чт", "Пт", "Сб")
        val today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

        binding.llWeekDays.removeAllViews()

        repeat(7) { i ->
            val dayCalendar = calendar.clone() as Calendar
            dayCalendar.add(Calendar.DAY_OF_WEEK, i)
            val dayNum = dayCalendar.get(Calendar.DAY_OF_MONTH)
            val dayName = dayNames[dayCalendar.get(Calendar.DAY_OF_WEEK) - 1]
            val isToday = dayNum == today

            val dayView = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                gravity = android.view.Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                )
            }

            val dayNameView = TextView(requireContext()).apply {
                text = dayName
                textSize = 11f
                setTextColor(
                    if (isToday) android.graphics.Color.WHITE
                    else android.graphics.Color.parseColor("#CCFFFFFF")
                )
                gravity = android.view.Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val dayNumView = TextView(requireContext()).apply {
                text = dayNum.toString()
                textSize = 15f
                gravity = android.view.Gravity.CENTER
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = 4 }

                if (isToday) {
                    setBackgroundResource(R.drawable.today_circle)
                    setTextColor(android.graphics.Color.parseColor("#A1C5FA"))
                } else {
                    setTextColor(android.graphics.Color.WHITE)
                }
            }

            dayView.addView(dayNameView)
            dayView.addView(dayNumView)
            binding.llWeekDays.addView(dayView)
        }
    }

    private fun setupSubjectChips(subjects: List<Subject>) {
        binding.chipGroupSubjects.removeAllViews()

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

        subjects.forEach { subject ->
            val chip = Chip(requireContext()).apply {
                text = subject.name
                isCheckable = true
                isChecked = selectedSubjectId == subject.id

                val pastelBg = ColorUtils.setAlphaComponent(subject.color, 45)
                val selectedBg = subject.color // Пълен цвят когато е избран

                // Selector за фон
                chipBackgroundColor = android.content.res.ColorStateList(
                    arrayOf(
                        intArrayOf(android.R.attr.state_checked),
                        intArrayOf(-android.R.attr.state_checked)
                    ),
                    intArrayOf(selectedBg, pastelBg)
                )

                // Selector за текст
                val darkColor = android.graphics.Color.parseColor("#1A1A2E")
                setTextColor(
                    android.content.res.ColorStateList(
                        arrayOf(
                            intArrayOf(android.R.attr.state_checked),
                            intArrayOf(-android.R.attr.state_checked)
                        ),
                        intArrayOf(
                            android.graphics.Color.WHITE, // бял текст когато е избран
                            subject.color // цветен текст когато не е избран
                        )
                    )
                )

                // Контур само когато не е избран
                chipStrokeColor = android.content.res.ColorStateList(
                    arrayOf(
                        intArrayOf(android.R.attr.state_checked),
                        intArrayOf(-android.R.attr.state_checked)
                    ),
                    intArrayOf(subject.color, subject.color)
                )
                chipStrokeWidth = 2f

                setOnClickListener {
                    selectedSubjectId = subject.id
                    refreshTasksList()
                }
            }
            binding.chipGroupSubjects.addView(chip)
        }
    }

    private fun refreshTasksList() {
        val tasks = taskViewModel.pendingTasks.value
        filterAndSubmitTasks(tasks)
    }

    private fun filterAndSubmitTasks(tasks: List<com.example.uniplanner.data.local.entity.Task>) {
        val filteredList = if (selectedSubjectId == null) {
            tasks
        } else {
            tasks.filter { it.subjectId == selectedSubjectId }
        }
        taskAdapter.submitList(filteredList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}