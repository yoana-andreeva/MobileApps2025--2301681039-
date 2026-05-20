package com.example.uniplanner.ui.tasks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.uniplanner.R
import com.example.uniplanner.data.local.entity.TaskStatus
import com.example.uniplanner.databinding.FragmentTasksBinding
import com.example.uniplanner.ui.adapter.TaskAdapter
import com.example.uniplanner.ui.viewmodel.TaskViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar

@AndroidEntryPoint
class TasksFragment : Fragment() {

    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!
    private val taskViewModel: TaskViewModel by viewModels()
    private lateinit var taskAdapter: TaskAdapter

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

        taskAdapter = TaskAdapter(
            onTaskChecked = { task, isChecked ->
                val status = if (isChecked) TaskStatus.DONE else TaskStatus.PENDING
                taskViewModel.updateTaskStatus(task, status)
            },
            onTaskClicked = { },
            onTaskDeleted = { task -> taskViewModel.deleteTask(task) }

        )

        binding.rvTasks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTasks.adapter = taskAdapter

        observeTasks()
        setupSwipeToDelete()

        binding.fabAddTask.setOnClickListener {
            findNavController().navigate(R.id.addEditTaskFragment)
        }
    }

    private fun observeTasks() {
        viewLifecycleOwner.lifecycleScope.launch {
            taskViewModel.allTasks.collect { tasks ->
                taskAdapter.submitList(tasks)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
}