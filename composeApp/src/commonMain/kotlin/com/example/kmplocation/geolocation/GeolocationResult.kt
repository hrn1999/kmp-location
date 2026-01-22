package com.example.kmplocation.geolocation

sealed interface GeolocationResult {
    data class Success(val location: LocationData) : GeolocationResult
    data class Error(val error: GeolocationError) : GeolocationResult
}
