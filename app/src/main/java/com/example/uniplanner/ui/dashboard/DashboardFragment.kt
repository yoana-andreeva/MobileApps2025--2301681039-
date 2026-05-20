package com.example.uniplanner.ui.dashboard

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
import com.example.uniplanner.databinding.FragmentDashboardBinding
import com.example.uniplanner.ui.adapter.TaskAdapter
import com.example.uniplanner.ui.viewmodel.TaskViewModel
import com.example.uniplanner.ui.viewmodel.SubjectViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val taskViewModel: TaskViewModel by viewModels()
    private val subjectViewModel: SubjectViewModel by viewModels()
    private lateinit var taskAdapter: TaskAdapter

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
            onTaskChecked = { task, isChecked ->
                val status = if (isChecked) TaskStatus.DONE else TaskStatus.PENDING
                taskViewModel.updateTaskStatus(task, status)
            },
            onTaskClicked = { },
            onTaskDeleted = { task ->
                taskViewModel.deleteTask(task)
            }
        )
        binding.rvUpcomingTasks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUpcomingTasks.adapter = taskAdapter
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            taskViewModel.pendingTasks.collect { tasks ->
                taskAdapter.submitList(tasks)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}