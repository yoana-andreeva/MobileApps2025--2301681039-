package com.example.uniplanner.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.uniplanner.data.local.entity.Subject
import com.example.uniplanner.databinding.ItemSubjectBinding

class SubjectAdapter(
    private val onSubjectClicked: (Subject) -> Unit,
    private val onSubjectDeleted: (Subject) -> Unit
) : ListAdapter<Subject, SubjectAdapter.SubjectViewHolder>(SubjectDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val binding = ItemSubjectBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SubjectViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SubjectViewHolder(
        private val binding: ItemSubjectBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(subject: Subject) {
            binding.tvSubjectName.text = subject.name
            binding.tvSubjectTeacher.text = subject.teacher.ifEmpty { "Няма преподавател" }
            binding.subjectColorIndicator.setBackgroundColor(subject.color)

            binding.root.setOnClickListener { onSubjectClicked(subject) }
            binding.btnDeleteSubject.setOnClickListener { onSubjectDeleted(subject) }
        }
    }

    class SubjectDiffCallback : DiffUtil.ItemCallback<Subject>() {
        override fun areItemsTheSame(oldItem: Subject, newItem: Subject) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Subject, newItem: Subject) =
            oldItem == newItem
    }
}