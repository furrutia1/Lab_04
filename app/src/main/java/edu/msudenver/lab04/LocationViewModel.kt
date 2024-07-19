package edu.msudenver.lab04

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng

class LocationViewModel: ViewModel() {
    private val _coordinates = MutableLiveData<LatLng>()
    val coordinates : LiveData<LatLng> = _coordinates

    fun setCoordinates(userCoordinates: LatLng) {
        _coordinates.postValue(userCoordinates)
    }
    private val _total = MutableLiveData<Int>()
    val total: LiveData<Int> = _total
    init {
        _total.postValue(0)
    }
    fun increaseTotal() {
        _total.postValue((_total.value ?: 0) + 1)
    }

}