package com.example.kmplocation.geolocation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKCoordinateRegionMakeWithDistance
import platform.MapKit.MKMapView
import platform.MapKit.MKPointAnnotation

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun MapView(
    modifier: Modifier,
    latitude: Double,
    longitude: Double
) {
    val coordinate = remember(latitude, longitude) {
        CLLocationCoordinate2DMake(latitude, longitude)
    }

    UIKitView(
        modifier = modifier,
        factory = {
            MKMapView().apply {
                val region = MKCoordinateRegionMakeWithDistance(coordinate, 1000.0, 1000.0)
                setRegion(region, animated = false)

                val annotation = MKPointAnnotation().apply {
                    setCoordinate(coordinate)
                    setTitle("Sua localizacao")
                }
                addAnnotation(annotation)
            }
        },
        update = { mapView ->
            val region = MKCoordinateRegionMakeWithDistance(coordinate, 1000.0, 1000.0)
            mapView.setRegion(region, animated = true)

            mapView.removeAnnotations(mapView.annotations)
            val annotation = MKPointAnnotation().apply {
                setCoordinate(coordinate)
                setTitle("Sua localizacao")
            }
            mapView.addAnnotation(annotation)
        }
    )
}
