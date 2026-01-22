package com.example.kmplocation.geolocation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.location.LOCATION
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLLocationAccuracyBest
import platform.Foundation.NSError
import platform.darwin.NSObject
import kotlin.coroutines.resume

class IosGeolocation(
    private val permissionsController: PermissionsController
) : Geolocation {

    override suspend fun getCurrentLocation(): GeolocationResult {
        // 1. Verificar/solicitar permissao
        try {
            permissionsController.providePermission(Permission.LOCATION)
        } catch (e: Exception) {
            return GeolocationResult.Error(GeolocationError.PERMISSION_DENIED)
        }

        // 2. Solicitar localizacao com timeout
        val result = withTimeoutOrNull(15_000L) {
            requestLocationInternal()
        }

        return result ?: GeolocationResult.Error(GeolocationError.TIMEOUT)
    }

    @OptIn(ExperimentalForeignApi::class)
    private suspend fun requestLocationInternal(): GeolocationResult =
        suspendCancellableCoroutine { continuation ->
            val locationManager = CLLocationManager()
            var hasResumed = false

            val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
                override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
                    if (hasResumed) return
                    hasResumed = true
                    manager.delegate = null

                    val clLocation = didUpdateLocations.lastOrNull() as? CLLocation
                    if (clLocation != null) {
                        val locationData = clLocation.coordinate.useContents {
                            LocationData(
                                latitude = latitude,
                                longitude = longitude,
                                accuracy = clLocation.horizontalAccuracy.toFloat()
                            )
                        }
                        continuation.resume(GeolocationResult.Success(locationData))
                    } else {
                        continuation.resume(GeolocationResult.Error(GeolocationError.UNKNOWN))
                    }
                }

                override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
                    if (hasResumed) return
                    hasResumed = true
                    manager.delegate = null

                    val error = when (didFailWithError.code) {
                        1L -> GeolocationError.PERMISSION_DENIED  // kCLErrorDenied
                        else -> GeolocationError.UNKNOWN
                    }
                    continuation.resume(GeolocationResult.Error(error))
                }
            }

            locationManager.delegate = delegate
            locationManager.desiredAccuracy = kCLLocationAccuracyBest
            locationManager.requestLocation()

            continuation.invokeOnCancellation {
                if (!hasResumed) {
                    locationManager.delegate = null
                }
            }
        }
}

@Composable
actual fun rememberGeolocation(permissionsController: PermissionsController): Geolocation {
    return remember(permissionsController) {
        IosGeolocation(permissionsController)
    }
}
