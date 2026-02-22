package com.gycss.app.ui.senior

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gycss.app.data.model.MedicalRecord
import com.gycss.app.data.repository.AuthRepository
import com.gycss.app.data.repository.MedicalRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MedicalRecordViewModel @Inject constructor(
    private val repository: MedicalRecordRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _records = MutableLiveData<List<MedicalRecord>>()
    val records: LiveData<List<MedicalRecord>> = _records

    private val _operationResult = MutableLiveData<Result<Unit>>()
    val operationResult: LiveData<Result<Unit>> = _operationResult

    fun fetchRecords() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUser()?.uid ?: return@launch
            repository.getMedicalRecords(userId).addSnapshotListener { snapshot, _ ->
                _records.postValue(snapshot?.toObjects(MedicalRecord::class.java) ?: emptyList())
            }
        }
    }

    fun addRecord(description: String, doctorName: String) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUser()?.uid ?: return@launch
            val record = MedicalRecord(
                description = description,
                doctorName = doctorName,
                seniorId = userId
            )
            _operationResult.postValue(repository.addMedicalRecord(userId, record))
        }
    }

    fun deleteRecord(recordId: String) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUser()?.uid ?: return@launch
            _operationResult.postValue(repository.deleteMedicalRecord(userId, recordId))
        }
    }
}
