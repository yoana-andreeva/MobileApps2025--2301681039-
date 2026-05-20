package com.example.uniplanner.ui.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.uniplanner.databinding.FragmentCalendarBinding
import com.example.uniplanner.ui.viewmodel.TaskViewModel
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

        binding.rvDayTasks.layoutManager = LinearLayoutManager(requireContext())

        binding.calendarView.setOnDateChangedListener { _, date, _ ->
            loadTasksForDate(date)
        }

        // Зареди задачите за днес при старт
        loadTasksForDate(CalendarDay.today())
    }

    private fun loadTasksForDate(date: CalendarDay) {
        val calendar = Calendar.getInstance().apply {
            set(date.year, date.month - 1, date.day, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis
        val endOfDay = startOfDay + 24 * 60 * 60 * 1000 - 1

        val formatter = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        binding.tvSelectedDate.text = formatter.format(calendar.time)

        viewLifecycleOwner.lifecycleScope.launch {
            taskViewModel.getTasksForDateRange(startOfDay, endOfDay).collect { tasks ->
                // ще добавим adapter по-късно
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}