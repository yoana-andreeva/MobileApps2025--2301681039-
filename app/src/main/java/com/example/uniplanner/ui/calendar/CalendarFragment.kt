package com.example.uniplanner.ui.calendar

import android.graphics.Color
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
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape

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

        // Задаваме цвят на календара според темата
        val textColor = if (resources.configuration.uiMode and
            android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
            android.content.res.Configuration.UI_MODE_NIGHT_YES) {
            android.graphics.Color.WHITE
        } else {
            android.graphics.Color.BLACK
        }
        binding.calendarView.setDateTextAppearance(android.R.style.TextAppearance)
        binding.calendarView.setWeekDayTextAppearance(android.R.style.TextAppearance)
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

        // Добави точки върху календара
        viewLifecycleOwner.lifecycleScope.launch {
            taskViewModel.allTasks.collect { tasks ->
                binding.calendarView.removeDecorators()
                val dates = tasks.map { task ->
                    val cal = Calendar.getInstance().apply {
                        timeInMillis = task.deadline
                    }
                    CalendarDay.from(
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH) + 1,
                        cal.get(Calendar.DAY_OF_MONTH)
                    )
                }.toSet()

                binding.calendarView.addDecorator(
                    TaskDotDecorator(dates, Color.parseColor("#7DA8E6"))
                )
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
                return@collect
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class TaskDotDecorator(
    private val dates: Set<CalendarDay>,
    private val color: Int
) : DayViewDecorator {

    private val drawable = ShapeDrawable(OvalShape()).apply {
        paint.color = color
        intrinsicWidth = 16
        intrinsicHeight = 16
    }

    override fun shouldDecorate(day: CalendarDay): Boolean = dates.contains(day)

    override fun decorate(view: DayViewFacade) {
        view.addSpan(DotSpan(8f, color))
    }
}