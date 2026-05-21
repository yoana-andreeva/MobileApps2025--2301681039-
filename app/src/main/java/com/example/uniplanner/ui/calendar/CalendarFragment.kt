package com.example.uniplanner.ui.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.uniplanner.data.local.entity.TaskStatus
import com.example.uniplanner.databinding.FragmentCalendarBinding
import com.example.uniplanner.ui.adapter.TaskAdapter
import com.example.uniplanner.ui.viewmodel.TaskViewModel
import com.example.uniplanner.ui.viewmodel.SubjectViewModel
import com.prolificinteractive.materialcalendarview.CalendarDay
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private val taskViewModel: TaskViewModel by viewModels()
    private val subjectViewModel: SubjectViewModel by viewModels() // Добавено за пастелните цветове
    private lateinit var taskAdapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeSubjectData()

        // Слушател за промяна на датата в календара
        binding.calendarView.setOnDateChangedListener { _, date, _ ->
            loadTasksForDate(date)
        }

        // Автоматично зареждане на задачите за днешния ден при стартиране
        loadTasksForDate(CalendarDay.today())
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            subjectColors = emptyMap(),
            subjectNames = emptyMap(),
            onTaskChecked = { task, isChecked ->
                val status = if (isChecked) TaskStatus.DONE else TaskStatus.PENDING
                taskViewModel.updateTaskStatus(task, status)
            },
            onTaskClicked = { /* За бъдеща детайлна редакция */ },
            onTaskDeleted = { task -> taskViewModel.deleteTask(task) }
        )
        binding.rvDayTasks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDayTasks.adapter = taskAdapter
    }

    // Извличаме цветовете и имената на предметите, за да оцветим правилно задачите в календара
    private fun observeSubjectData() {
        viewLifecycleOwner.lifecycleScope.launch {
            subjectViewModel.subjects.collect { subjectsList ->
                val colorMap = subjectsList.associate { it.id to it.color }
                val nameMap = subjectsList.associate { it.id to it.name }
                taskAdapter.updateSubjectData(colorMap, nameMap)
            }
        }
    }

    private fun loadTasksForDate(date: CalendarDay) {
        // Изчисляваме началото на избрания ден (00:00:00.000)
        val calendar = Calendar.getInstance().apply {
            set(date.year, date.month - 1, date.day, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis
        // Изчисляваме края на същия ден (23:59:59.999)
        val endOfDay = startOfDay + 24 * 60 * 60 * 1000 - 1

        // Форматираме текста над списъка (напр. "21 Май 2026")
        val formatter = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        binding.tvSelectedDate.text = formatter.format(calendar.time)

        // Събираме задачите от Room базата данни, които попадат в този времеви диапазон
        viewLifecycleOwner.lifecycleScope.launch {
            taskViewModel.getTasksForDateRange(startOfDay, endOfDay).collect { tasks ->
                taskAdapter.submitList(tasks)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}