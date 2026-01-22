package com.example.kmplocation.geolocation

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float? = null
)
