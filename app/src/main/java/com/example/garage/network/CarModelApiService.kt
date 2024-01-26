package com.example.garage.network

import com.example.garage.database.models.CarModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

private val moshi = Moshi.Builder()
    .addLast(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .baseUrl("https://raw.githubusercontent.com/slendy98DX/car_models_json/main/")
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .build()



interface CarModelApiService {

    @GET("car_models.json")
    suspend fun getCarModelList(): List<CarModel>
}

object CarModelApi {
    val retrofitService: CarModelApiService by lazy { retrofit.create(CarModelApiService::class.java) }
}