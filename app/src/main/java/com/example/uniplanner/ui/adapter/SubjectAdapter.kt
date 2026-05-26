package com.example.uniplanner.ui.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.uniplanner.data.local.entity.Subject
import com.example.uniplanner.databinding.ItemSubjectBinding

class SubjectAdapter(
    private val onDeleteClicked: (Subject) -> Unit
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
            // 1. Попълваме името на предмета
            binding.tvSubjectName.text = subject.name

            // 2. Попълваме името на преподавателя (Запазено твое ID: tvSubjectTeacher)
            // Използваме твоята променлива от Entity класа ти (напр. subject.teacher)
            binding.tvSubjectTeacher.text = if (!subject.teacher.isNullOrEmpty()) {
                "Преподавател: ${subject.teacher}"
            } else {
                "Няма посочен преподавател"
            }

            // 3. Динамично оцветяваме кръгчето с пастелния цвят от базата (Запазено твое ID: subjectColorIndicator)
            binding.subjectColorIndicator.backgroundTintList = ColorStateList.valueOf(subject.color)

            // 4. Слушател за текстовия бутон за изтриване (Запазено твое ID: btnDeleteSubject)
            binding.btnDeleteSubject.setOnClickListener {
                onDeleteClicked(subject)
            }
        }
    }

    class SubjectDiffCallback : DiffUtil.ItemCallback<Subject>() {
        override fun areItemsTheSame(oldItem: Subject, newItem: Subject) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Subject, newItem: Subject) =
            oldItem == newItem
    }
}