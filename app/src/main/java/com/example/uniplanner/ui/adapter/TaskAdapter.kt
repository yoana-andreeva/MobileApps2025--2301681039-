package com.example.uniplanner.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
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
    private var subjectColors: Map<Long, Int> = emptyMap(),
    private var subjectNames: Map<Long, String> = emptyMap(),
    private val onTaskChecked: (Task, Boolean) -> Unit,
    private val onTaskClicked: (Task) -> Unit,
    private val onTaskDeleted: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    // Метод за динамично обновяване на данните за предметите от фрагментите
    fun updateSubjectData(colors: Map<Long, Int>, names: Map<Long, String>) {
        this.subjectColors = colors
        this.subjectNames = names
        notifyDataSetChanged() // Преначертава списъка с новите визуални стилове
    }

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
            // 1. Заглавие на задачата + Динамична проверка за прикачена снимка (Критерий за 6-ца)
            if (!task.imagePath.isNullOrEmpty()) {
                binding.tvTaskTitle.text = "${task.title} 📷"
            } else {
                binding.tvTaskTitle.text = task.title
            }

            // 2. Форматиране на крайния срок
            val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            binding.tvTaskDeadline.text = "📅 ${formatter.format(Date(task.deadline))}"

            // 3. Динамично пастелно оцветяване на картата според предмета
            val subjectColor = subjectColors[task.subjectId] ?: Color.WHITE
            // Задаваме алфа прозрачност 40 (от общо 255) за нежен пастелен нюанс
            val pastelColor = ColorUtils.setAlphaComponent(subjectColor, 40)
            binding.root.setCardBackgroundColor(pastelColor)

            // 4. Поставяне на името на предмета в картата
            val subjectName = subjectNames[task.subjectId] ?: "Няма предмет"
            binding.tvTaskSubject.text = "📚 $subjectName"

            // 5. Цвят на тънкия ляв кант според приоритета (Material 3 палитра)
            val priorityColor = when (task.priority) {
                Priority.HIGH -> Color.parseColor("#FF6B6B")   // Свежо червено
                Priority.MEDIUM -> Color.parseColor("#FFB347") // Топло оранжево
                Priority.LOW -> Color.parseColor("#4CAF82")    // Градинско зелено
            }
            binding.priorityIndicator.setBackgroundColor(priorityColor)

            // 6. Управление на статуса (CheckBox)
            // Първо зануляваме лисънъра, за да предотвратим грешно тригърване при скролване (RecyclerView bug prevention)
            binding.cbTaskDone.setOnCheckedChangeListener(null)
            binding.cbTaskDone.isChecked = task.status == TaskStatus.DONE

            binding.cbTaskDone.setOnCheckedChangeListener { _, isChecked ->
                onTaskChecked(task, isChecked)
            }

            // 7. Клик върху цялата карта (за преглед или редакция)
            binding.root.setOnClickListener { onTaskClicked(task) }

            // Цвят на дедлайна - Червен ако е просрочен, сив иначе
            val now = System.currentTimeMillis()
            binding.tvTaskDeadline.setTextColor(
                if (task.deadline < now && task.status != TaskStatus.DONE)
                    Color.parseColor("#FF6B6B")
                else
                    Color.parseColor("#9095A6")
            )

            binding.btnEditTask.setOnClickListener {
                onTaskClicked(task)
            }
        }
    }

    class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Task, newItem: Task) =
            oldItem == newItem
    }
}