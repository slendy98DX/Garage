package com.example.garage.network

import com.example.garage.database.models.Logo
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

private val moshi = Moshi.Builder()
    .addLast(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .baseUrl("https://raw.githubusercontent.com/slendy98DX/car_logos/main/")
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .build()



interface LogoApiService {

    @GET("car_logos.json")
    suspend fun getLogoList() : List<Logo>
}

object LogoApi {
    val retrofitService: LogoApiService by lazy { retrofit.create(LogoApiService::class.java) }
}