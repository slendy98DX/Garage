package com.example.garage.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.garage.database.models.Car
import kotlinx.coroutines.flow.Flow

@Dao
interface CarDao {

    @Query("SELECT * FROM car ORDER BY id ASC")
    fun getAll(): Flow<List<Car>>

    @Query("SELECT * FROM car where id = :id")
    fun getCar(id: Long) : Flow<Car>

    @Query("SELECT * FROM car WHERE brand = :brand ORDER BY id ASC")
    fun getCarsByBrand(brand: String): Flow<List<Car>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(car: Car)

    @Update
    suspend fun update(car: Car)

    @Delete
    suspend fun delete(car: Car)
}