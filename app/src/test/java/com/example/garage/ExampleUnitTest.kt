import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.garage.database.AppDatabase
import com.example.garage.database.dao.CarDao
import com.example.garage.database.models.Car
import com.example.garage.viewmodel.CarViewModel
import com.example.garage.viewmodel.CarViewModelFactory
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.AdditionalMatchers.not

@RunWith(JUnit4 ::class)
class ExampleUnitTest {

    private lateinit var carDao: CarDao
    private lateinit var database: AppDatabase
    private lateinit var application: Application
    private lateinit var viewModel: CarViewModel

    val car = Car(1, "595", "2.0L", "GAS", "Abarth", 50000.0, 2020)

    val updatedCar = Car(1, "595", "2.0L", "HYBRID", "Abarth", 99999.0, 2020)

    @Before
    fun setup()
    {
        application = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(application, AppDatabase::class.java).build()
        carDao = database.carDao()
        val factory = CarViewModelFactory(application, carDao)
        viewModel = ViewModelProvider(ViewModelStore(), factory)[CarViewModel::class.java]

    }
    @Test
    fun checkInsertedCar(){
        viewModel.addCar(car)
        Thread.sleep(1000)
        viewModel.carsList.value?.contains(car)?.let { assertTrue(it) }
    }

    @Test
    fun checkEditedCar(){
        viewModel.addCar(car)
        Thread.sleep(1000)
        viewModel.updateCar(updatedCar)
        Thread.sleep(1000)
        viewModel.carsList.value?.contains(updatedCar)?.let { assertTrue(it) }
    }

    @Test
    fun checkDeletedCar(){
        viewModel.addCar(car)
        Thread.sleep(1000)
        viewModel.deleteCar(updatedCar)
        Thread.sleep(1000)
        viewModel.carsList.value?.contains(car)?.let { not(assertTrue(it)) }
    }
}