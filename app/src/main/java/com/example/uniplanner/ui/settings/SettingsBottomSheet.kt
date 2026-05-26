package com.example.uniplanner.ui.settings

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.viewModels
import com.example.uniplanner.databinding.BottomSheetSettingsBinding
import com.example.uniplanner.ui.viewmodel.TaskViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetSettingsBinding? = null
    private val binding get() = _binding!!
    private val taskViewModel: TaskViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupThemeSelector()
        setupReminderSelector() // Новата функция за управление на нотификациите
        setupClearButton()
    }

    private fun setupThemeSelector() {
        // Покажи текущата тема
        val isNightMode = resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

        if (isNightMode) {
            binding.chipDark.isChecked = true
        } else {
            binding.chipLight.isChecked = true
        }

        binding.chipLight.setOnClickListener {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        binding.chipDark.setOnClickListener {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }

    private fun setupReminderSelector() {
        val sharedPrefs = requireContext().getSharedPreferences("uniplanner_prefs", Context.MODE_PRIVATE)

        // Зареждаме последно записаната настройка (по подразбиране 1 час)
        val currentSavedHours = sharedPrefs.getInt("reminder_hours", 1)

        // Маркираме правилния Chip спрямо записаното в паметта
        when (currentSavedHours) {
            1 -> binding.chipReminder1h.isChecked = true
            3 -> binding.chipReminder3h.isChecked = true
            24 -> binding.chipReminder1d.isChecked = true
        }

        // Слушател: записваме промяната веднага щом потребителят кликне друг Chip
        binding.chipGroupReminder.setOnCheckedStateChangeListener { _, checkedIds ->
            val hoursToSave = when {
                checkedIds.contains(binding.chipReminder1h.id) -> 1
                checkedIds.contains(binding.chipReminder3h.id) -> 3
                checkedIds.contains(binding.chipReminder1d.id) -> 24
                else -> 1
            }

            sharedPrefs.edit().putInt("reminder_hours", hoursToSave).apply()
        }
    }

    private fun setupClearButton() {
        binding.btnClearOldTasks.setOnClickListener {
            taskViewModel.deleteOldCompletedTasks()
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "SettingsBottomSheet"
    }
}