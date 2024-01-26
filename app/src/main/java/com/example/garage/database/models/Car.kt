package com.example.garage.database.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "car")
data class Car (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "model")
    val model: String,

    @ColumnInfo(name = "displacement")
    val displacement: String,

    @ColumnInfo(name = "power_supply")
    val powerSupply: String,

    @ColumnInfo(name = "brand")
    val brand: String,

    @ColumnInfo(name = "km")
    val km : Double,

    @ColumnInfo(name = "year")
    val year : Int
)