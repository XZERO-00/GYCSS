package com.gycss.app.ui.senior

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.gycss.app.data.model.HealthVital
import com.gycss.app.data.repository.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HealthVitalsViewModel @Inject constructor(
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _vitals = MutableLiveData<List<HealthVital>>()
    val vitals: LiveData<List<HealthVital>> = _vitals

    fun fetchVitals() {
        val uid = auth.currentUser?.uid ?: return
        FirestoreRepository.getHealthVitals(uid) {
            _vitals.postValue(it)
        }
    }

    fun addVital(sys: Int, dia: Int, hr: Int) {
        val uid = auth.currentUser?.uid ?: return
        val vital = HealthVital(
            seniorId = uid,
            bloodPressureSys = sys,
            bloodPressureDia = dia,
            heartRate = hr
        )
        FirestoreRepository.addHealthVital(vital, onSuccess = {
            fetchVitals()
        }, onFailure = {
            // Handle error
        })
    }
}
