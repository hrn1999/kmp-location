package com.example.kmplocation.geolocation

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.location.LOCATION
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

class AndroidGeolocation(
    private val context: Context,
    private val permissionsController: PermissionsController
) : Geolocation {

    override suspend fun getCurrentLocation(): GeolocationResult {
        // 1. Verificar/solicitar permissão
        try {
            permissionsController.providePermission(Permission.LOCATION)
        } catch (e: Exception) {
            return GeolocationResult.Error(GeolocationError.PERMISSION_DENIED)
        }

        // 2. Obter LocationManager
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // 3. Verificar se GPS ou Network estão habilitados
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isGpsEnabled && !isNetworkEnabled) {
            return GeolocationResult.Error(GeolocationError.LOCATION_DISABLED)
        }

        // 4. Solicitar localização com timeout
        val location = withTimeoutOrNull(15_000L) {
            requestLocation(locationManager, isGpsEnabled, isNetworkEnabled)
        }

        return if (location != null) {
            GeolocationResult.Success(
                LocationData(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracy = location.accuracy
                )
            )
        } else {
            GeolocationResult.Error(GeolocationError.TIMEOUT)
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestLocation(
        locationManager: LocationManager,
        isGpsEnabled: Boolean,
        isNetworkEnabled: Boolean
    ): Location? = suspendCancellableCoroutine { continuation ->
        var hasResumed = false

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                if (!hasResumed) {
                    hasResumed = true
                    locationManager.removeUpdates(this)
                    continuation.resume(location)
                }
            }

            @Deprecated("Deprecated in API level 29")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        // Solicitar de ambos os providers para fallback
        if (isGpsEnabled) {
            locationManager.requestSingleUpdate(
                LocationManager.GPS_PROVIDER,
                listener,
                Looper.getMainLooper()
            )
        }

        if (isNetworkEnabled) {
            locationManager.requestSingleUpdate(
                LocationManager.NETWORK_PROVIDER,
                listener,
                Looper.getMainLooper()
            )
        }

        continuation.invokeOnCancellation {
            locationManager.removeUpdates(listener)
        }
    }
}

@Composable
actual fun rememberGeolocation(permissionsController: PermissionsController): Geolocation {
    val context = LocalContext.current.applicationContext
    return remember(context, permissionsController) {
        AndroidGeolocation(context, permissionsController)
    }
}
