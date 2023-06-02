package com.prodev.muslimq.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.prodev.muslimq.core.data.preference.DataStorePreference
import com.prodev.muslimq.core.utils.uitheme.UITheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DataStoreViewModel @Inject constructor(
    private val dataStorePref: DataStorePreference
) : ViewModel() {

    fun saveSurah(
        surahId: Int,
        surahNameArabic: String,
        surahName: String,
        surahDesc: String,
        ayahNumber: Int
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStorePref.saveSurah(surahId, surahNameArabic, surahName, surahDesc, ayahNumber)
        }
    }

    fun saveProvinceData(provinceId: String, provinceName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStorePref.saveProvinceData(provinceId, provinceName)
        }
    }

    fun saveAreaData(cityName: String, countryName: String) {
        viewModelScope.launch {
            dataStorePref.saveCityAndCountryData(cityName, countryName)
        }
    }

    fun saveAyahSize(ayahSize: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStorePref.saveAyahSize(ayahSize)
        }
    }

    fun saveSwitchDarkMode(uiTheme: UITheme) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStorePref.saveSwitchDarkModeState(uiTheme)
        }
    }

    fun saveSwitchState(switchName: String, isChecked: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStorePref.saveSwitchState(switchName, isChecked)
        }
    }

    val getSurah = dataStorePref.getSurah.asLiveData()

    val getDetailSurahAyah = dataStorePref.getDetailSurahAyah.asLiveData()

    val getProvinceData = dataStorePref.getProvinceData.asLiveData()

    val getAreaData = dataStorePref.getCityAndCountryData

    val getAyahSize = dataStorePref.getAyahSize.asLiveData()

    val getSwitchDarkMode = dataStorePref.getSwitchDarkMode.asLiveData()

    fun getSwitchState(switchName: String) = dataStorePref.getSwitchState(switchName).asLiveData()
}