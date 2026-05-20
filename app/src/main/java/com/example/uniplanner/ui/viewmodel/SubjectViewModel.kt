package com.example.uniplanner.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uniplanner.data.local.entity.Subject
import com.example.uniplanner.data.repository.UniPlannerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubjectViewModel @Inject constructor(
    private val repository: UniPlannerRepository
) : ViewModel() {

    val subjects: StateFlow<List<Subject>> = repository.getAllSubjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insertSubject(subject: Subject) = viewModelScope.launch {
        repository.insertSubject(subject)
    }

    fun updateSubject(subject: Subject) = viewModelScope.launch {
        repository.updateSubject(subject)
    }

    fun deleteSubject(subject: Subject) = viewModelScope.launch {
        repository.deleteSubject(subject)
    }
}