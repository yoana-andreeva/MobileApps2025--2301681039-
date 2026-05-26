package com.example.uniplanner.ui.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.uniplanner.data.local.entity.Task
import com.example.uniplanner.databinding.ItemUpcomingTaskBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class UpcomingTaskAdapter(
    private var subjectNames: Map<Long, String> = emptyMap(),
    private var subjectColors: Map<Long, Int> = emptyMap()
) : ListAdapter<Task, UpcomingTaskAdapter.ViewHolder>(DiffCallback()) {

    fun updateSubjectData(names: Map<Long, String>, colors: Map<Long, Int>) {
        subjectNames = names
        subjectColors = colors
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            ItemUpcomingTaskBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    inner class ViewHolder(
        private val binding: ItemUpcomingTaskBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            binding.tvUpcomingTitle.text = task.title
            binding.tvUpcomingSubject.text = subjectNames[task.subjectId] ?: "Без предмет"

            val formatter = SimpleDateFormat("dd MMM", Locale.getDefault())
            binding.tvUpcomingDeadline.text = "📅 ${formatter.format(Date(task.deadline))}"

            // Urgency badge
            val daysLeft = TimeUnit.MILLISECONDS.toDays(
                task.deadline - System.currentTimeMillis()
            )
            when {
                daysLeft <= 0 -> {
                    binding.chipUrgency.text = "🔴 Днес!"
                    binding.chipUrgency.chipBackgroundColor =
                        ColorStateList.valueOf(Color.parseColor("#FFE8E8"))
                }
                daysLeft == 1L -> {
                    binding.chipUrgency.text = "🟡 Утре"
                    binding.chipUrgency.chipBackgroundColor =
                        ColorStateList.valueOf(Color.parseColor("#FFF3E0"))
                }
                else -> {
                    binding.chipUrgency.text = "🟢 $daysLeft дни"
                    binding.chipUrgency.chipBackgroundColor =
                        ColorStateList.valueOf(Color.parseColor("#E8F5EE"))
                }
            }

            // Пастелен фон според предмета
            val subjectColor = subjectColors[task.subjectId]
            if (subjectColor != null) {
                binding.root.setCardBackgroundColor(
                    ColorUtils.setAlphaComponent(subjectColor, 50)
                )
            } else {
                binding.root.setCardBackgroundColor(Color.parseColor("#F8F9FC"))
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Task, newItem: Task) = oldItem == newItem
    }
}