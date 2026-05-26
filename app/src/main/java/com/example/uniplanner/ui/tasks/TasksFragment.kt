package com.example.uniplanner.ui.tasks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uniplanner.R
import com.example.uniplanner.data.local.entity.Task
import com.example.uniplanner.data.local.entity.TaskStatus
import com.example.uniplanner.databinding.FragmentTasksBinding
import com.example.uniplanner.ui.adapter.TaskAdapter
import com.example.uniplanner.ui.viewmodel.SubjectViewModel
import com.example.uniplanner.ui.viewmodel.TaskViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar

@AndroidEntryPoint
class TasksFragment : Fragment() {

    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!

    private val taskViewModel: TaskViewModel by viewModels()
    private val subjectViewModel: SubjectViewModel by viewModels()
    private lateinit var taskAdapter: TaskAdapter

    // Пазим последно заредените задачи за филтриране
    private var allTasks: List<Task> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeData()
        setupSwipeToDelete()
        setupFilterChips()

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
            onTaskClicked = { task ->
                val bundle = Bundle().apply { putLong("taskId", task.id) }
                findNavController().navigate(R.id.action_tasks_to_addEdit, bundle)
            },
            onTaskDeleted = { task -> taskViewModel.deleteTask(task) }
        )
        binding.rvTasks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTasks.adapter = taskAdapter
    }

    private fun setupFilterChips() {
        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, _ ->
            applyFilter(allTasks)
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            subjectViewModel.subjects.collect { subjectsList ->
                val colorMap = subjectsList.associate { it.id to it.color }
                val nameMap = subjectsList.associate { it.id to it.name }
                taskAdapter.updateSubjectData(colorMap, nameMap)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            taskViewModel.allTasks.collect { tasks ->
                allTasks = tasks
                applyFilter(tasks)
            }
        }
    }

    private fun applyFilter(tasks: List<Task>) {
        val now = System.currentTimeMillis()

        // Край на днешния ден
        val endOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.timeInMillis

        // След 3 дни
        val endOf3Days = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 3)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.timeInMillis

        // Край на тази седмица (неделя)
        val endOfWeek = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            add(Calendar.WEEK_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.timeInMillis

        val filtered = when (binding.chipGroupFilter.checkedChipId) {
            R.id.chipToday -> tasks.filter {
                it.deadline <= endOfToday &&
                        it.status != TaskStatus.DONE
            }
            R.id.chipNext3Days -> tasks.filter {
                it.deadline in now..endOf3Days &&
                        it.status != TaskStatus.DONE
            }
            R.id.chipThisWeek -> tasks.filter {
                it.deadline in now..endOfWeek &&
                        it.status != TaskStatus.DONE
            }
            else -> tasks
        }

        taskAdapter.submitList(filtered)
        binding.emptyStateTasks.visibility =
            if (filtered.isEmpty()) View.VISIBLE else View.GONE
        binding.rvTasks.visibility =
            if (filtered.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun setupSwipeToDelete() {
        val swipeCallback = object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val task = taskAdapter.currentList[viewHolder.adapterPosition]
                taskViewModel.deleteTask(task)
                Snackbar.make(
                    binding.root,
                    "\"${task.title}\" изтрита",
                    Snackbar.LENGTH_LONG
                ).setAction("Отмени") {
                    taskViewModel.insertTask(task)
                }.show()
            }
        }
        ItemTouchHelper(swipeCallback).attachToRecyclerView(binding.rvTasks)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}