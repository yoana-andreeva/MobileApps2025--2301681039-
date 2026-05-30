package com.example.uniplanner.ui.subjects

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.uniplanner.R
import com.example.uniplanner.data.local.entity.Subject
import com.example.uniplanner.databinding.DialogAddSubjectBinding
import com.example.uniplanner.databinding.FragmentSubjectsBinding
import com.example.uniplanner.ui.adapter.SubjectAdapter
import com.example.uniplanner.ui.viewmodel.SubjectViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SubjectsFragment : Fragment() {

    private var _binding: FragmentSubjectsBinding? = null
    private val binding get() = _binding!!

    // Свързваме се с твоя съществуващ ViewModel
    private val viewModel: SubjectViewModel by viewModels()
    private lateinit var subjectAdapter: SubjectAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubjectsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSwipeToDelete()
        observeSubjects()

        // При натискане на FAB бутона отваряме диалога за добавяне
        binding.fabAddSubject.setOnClickListener {
            showSubjectDialog(null)
        }
    }

    private fun setupRecyclerView() {
        subjectAdapter = SubjectAdapter(
            onEditClicked = { subject ->
                // Отваряме диалога в режим редактиране
                showSubjectDialog(subject)
            },
            onDeleteClicked = { subject ->
                viewModel.deleteSubject(subject)
            }
        )
        binding.rvSubjects.adapter = subjectAdapter
    }

    // Swipe за изтриване
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
                val subject = subjectAdapter.currentList[viewHolder.adapterPosition]
                viewModel.deleteSubject(subject)
                Snackbar.make(
                    binding.root,
                    "\"${subject.name}\" изтрит",
                    Snackbar.LENGTH_LONG
                ).setAction("Отмени") {
                    viewModel.insertSubject(subject)
                }.show()
            }
        }
        ItemTouchHelper(swipeCallback).attachToRecyclerView(binding.rvSubjects)
    }

    private fun observeSubjects() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.subjects.collect { list ->
                subjectAdapter.submitList(list)
                binding.emptyStateSubjects.visibility =
                    if (list.isEmpty()) View.VISIBLE else View.GONE
                binding.rvSubjects.visibility =
                    if (list.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    // Един диалог за добавяне И редактиране
    private fun showSubjectDialog(subjectToEdit: Subject?) {
        val dialogBinding = DialogAddSubjectBinding.inflate(layoutInflater)
        val isEditing = subjectToEdit != null

        // Ако редактираме — попълни съществуващите данни
        if (isEditing) {
            dialogBinding.etSubjectName.setText(subjectToEdit!!.name)
            dialogBinding.etTeacher.setText(subjectToEdit.teacher)
            dialogBinding.etRoom.setText(subjectToEdit.room)
        }

        // 1. Вземаме точно твоите 8 пастелни цвята от colors.xml
        val colorResIds = listOf(
            R.color.subject_1, R.color.subject_2, R.color.subject_3, R.color.subject_4,
            R.color.subject_5, R.color.subject_6, R.color.subject_7, R.color.subject_8
        )

        // Превръщаме ги в реални Color стойности (Int)
        val colors = colorResIds.map { ContextCompat.getColor(requireContext(), it) }
        var selectedColor = subjectToEdit?.color ?: colors[0]

        // Списък, в който ще пазим визуалните кръгчета
        val colorViews = mutableListOf<View>()

        // 2. Програмно генериране на пастелните кръгчета в colorPicker контейнера
        colors.forEachIndexed { index, color ->
            val colorView = View(requireContext()).apply {
                val size = (36 * resources.displayMetrics.density).toInt()
                val margin = (8 * resources.displayMetrics.density).toInt()

                val layoutParams = LinearLayout.LayoutParams(size, size).apply {
                    setMargins(margin, 0, margin, 0)
                }
                this.layoutParams = layoutParams

                setBackgroundResource(R.drawable.circle_shape)
                backgroundTintList = ColorStateList.valueOf(color)

                // Маркирай текущо избрания цвят
                if (color == selectedColor) {
                    scaleX = 1.2f
                    scaleY = 1.2f
                    elevation = 10f
                }

                // Логика при кликване върху цвят
                setOnClickListener {
                    selectedColor = color

                    // Нулираме мащаба на всички кръгчета
                    colorViews.forEach { v ->
                        v.scaleX = 1.0f
                        v.scaleY = 1.0f
                        v.elevation = 0f
                    }

                    // Анимираме леко избраното пастелно кръгче
                    animate().scaleX(1.2f).scaleY(1.2f).setDuration(150).start()
                    elevation = 10f
                }
            }

            colorViews.add(colorView)
            dialogBinding.colorPicker.addView(colorView)
        }

        // 3. Показване на Material 3 Диалог
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (isEditing) "Редактирай предмет" else "Добавяне на предмет")
            .setView(dialogBinding.root)
            .setPositiveButton(if (isEditing) "Запази" else "Добави") { _, _ ->
                val name = dialogBinding.etSubjectName.text.toString().trim()
                val teacher = dialogBinding.etTeacher.text.toString().trim()

                if (name.isNotEmpty()) {
                    if (isEditing) {
                        // Обновяваме съществуващия предмет
                        val updated = subjectToEdit!!.copy(
                            name = name,
                            teacher = teacher,
                            color = selectedColor,
                            room = dialogBinding.etRoom.text.toString().trim()
                        )
                        viewModel.updateSubject(updated)
                    } else {
                        // Добавяме нов предмет
                        viewModel.insertSubject(
                            Subject(
                                name = name,
                                teacher = teacher,
                                color = selectedColor,
                                room = dialogBinding.etRoom.text.toString().trim()
                            )
                        )
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Името на предмета е задължително!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Отказ", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}