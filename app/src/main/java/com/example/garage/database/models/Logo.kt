package com.example.garage.database.models

data class Logo (
    val name : String,
    val image: LogoImage
)

data class LogoImage(
    val source : String
)