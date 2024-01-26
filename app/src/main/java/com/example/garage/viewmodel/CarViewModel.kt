package com.example.garage.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.garage.database.models.Car
import com.example.garage.database.dao.CarDao
import com.example.garage.database.models.Logo
import com.example.garage.network.LogoApi
import com.example.garage.worker.CarReminderWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class CarViewModel(application: Application, private val carDao: CarDao) : ViewModel(){

    private val workManager = WorkManager.getInstance(application.applicationContext)

    val carsList : LiveData<List<Car>> = collectCars().asLiveData()

    fun collectCars() : Flow<List<Car>> = carDao.getAll()

    private val _logos = MutableLiveData<MutableList<Logo>>()
    val logos: LiveData<MutableList<Logo>> get() = _logos

    private val _selectedLogo = MutableLiveData<String>()
    val selectedLogo : LiveData<String> get() = _selectedLogo

    private val _car = MutableLiveData<Car>()
    val car: LiveData<Car> get() = _car

    private val _selectedCarModel = MutableLiveData<String>()
    val selectedCarModel: LiveData<String> get() = _selectedCarModel

    private val _selectedBrand = MutableLiveData<String>()
    val selectedBrand: LiveData<String> get() = _selectedBrand

    private val _selectedDisplacement = MutableLiveData<String>()
    val selectedDisplacement: LiveData<String> get() = _selectedDisplacement

    private val _selectedPowerSupply = MutableLiveData<String>()
    val selectedPowerSupply: LiveData<String> get() = _selectedPowerSupply

    fun setCar(car: Car) {
        _car.value = car
    }

    fun setSelectedCarModel(model: String) {
        _selectedCarModel.value = model
    }


    fun setSelectedBrand(brand: String) {
        _selectedBrand.value = brand
    }


    fun setSelectedLogo(logo : String) {
        _selectedLogo.value = logo
    }



    fun setSelectedPowerSupply(powerSupply: String) {
        _selectedPowerSupply.value = powerSupply
    }

    fun setSelectedDisplacement(displacement: String) {
        _selectedDisplacement.value = displacement
    }

    fun retrieveCar(id : Long) : LiveData<Car> {
        return carDao.getCar(id).asLiveData()
    }

    fun addCar(car: Car) {
        setCar(car)
        viewModelScope.launch {
            carDao.insert(car)
        }
    }

    fun updateCar(car: Car) {
        viewModelScope.launch {
            carDao.update(car)
        }
    }

    fun deleteCar(car: Car) {
        viewModelScope.launch {
            carDao.delete(car)
        }
    }

    private fun fetchLogoList() {
        viewModelScope.launch {
            try {
                val logosFromApi = LogoApi.retrofitService.getLogoList()

                val simplifiedLogos = logosFromApi.map { logo ->
                    Logo(name = logo.name, image = logo.image)
                }

                _logos.value = simplifiedLogos.toMutableList()
            } catch (e: Exception) {
                Log.e("CarViewModel", "Error fetching logos: ${e.message}")
            }
        }
    }

    internal fun scheduleReminder(
        carModel: String
    ) {
        val data = Data.Builder()

        data.putString(CarReminderWorker.nameKey, carModel)

        val notification = OneTimeWorkRequestBuilder<CarReminderWorker>()
            .setInputData(data.build())
            .addTag("WORKER")
            .build()

        workManager.enqueueUniqueWork(
            carModel,
            ExistingWorkPolicy.KEEP,
            notification
        )
    }

    init {
        fetchLogoList()
    }
}

class CarViewModelFactory(private val application: Application, private val carDao: CarDao) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(CarViewModel::class.java)) {
            CarViewModel(application,carDao) as T
        } else {
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}