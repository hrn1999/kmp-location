package com.example.kmplocation.geolocation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun MapView(
    modifier: Modifier,
    latitude: Double,
    longitude: Double
)
