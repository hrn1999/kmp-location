package com.example.kmplocation.geolocation

import androidx.compose.runtime.Composable
import dev.icerock.moko.permissions.PermissionsController

interface Geolocation {
    suspend fun getCurrentLocation(): GeolocationResult
}

@Composable
expect fun rememberGeolocation(permissionsController: PermissionsController): Geolocation
