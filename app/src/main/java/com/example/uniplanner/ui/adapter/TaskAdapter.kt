package com.example.uniplanner.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.uniplanner.data.local.entity.Priority
import com.example.uniplanner.data.local.entity.Task
import com.example.uniplanner.data.local.entity.TaskStatus
import com.example.uniplanner.databinding.ItemTaskBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TaskAdapter(
    private val onTaskChecked: (Task, Boolean) -> Unit,
    private val onTaskClicked: (Task) -> Unit,
    private val onTaskDeleted: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TaskViewHolder(
        private val binding: ItemTaskBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            binding.tvTaskTitle.text = task.title

            val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            binding.tvTaskDeadline.text = "📅 ${formatter.format(Date(task.deadline))}"

            // Цвят според приоритет
            val priorityColor = when (task.priority) {
                Priority.HIGH -> Color.parseColor("#EF5350")
                Priority.MEDIUM -> Color.parseColor("#FFA726")
                Priority.LOW -> Color.parseColor("#66BB6A")
            }
            binding.priorityIndicator.setBackgroundColor(priorityColor)

            // Статус
            binding.cbTaskDone.isChecked = task.status == TaskStatus.DONE
            binding.cbTaskDone.setOnCheckedChangeListener { _, isChecked ->
                onTaskChecked(task, isChecked)
            }

            binding.root.setOnClickListener { onTaskClicked(task) }

            // Swipe to delete — ще добавим после
        }
    }

    class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Task, newItem: Task) =
            oldItem == newItem
    }
}