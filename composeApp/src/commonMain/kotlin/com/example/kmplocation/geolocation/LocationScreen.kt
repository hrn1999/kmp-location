package com.example.kmplocation.geolocation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import kotlinx.coroutines.launch

@Composable
fun LocationScreen() {
    val factory = rememberPermissionsControllerFactory()
    val permissionsController = remember(factory) {
        factory.createPermissionsController()
    }
    val geolocation = rememberGeolocation(permissionsController)
    val scope = rememberCoroutineScope()

    var location by remember { mutableStateOf<LocationData?>(null) }
    var error by remember { mutableStateOf<GeolocationError?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    BindEffect(permissionsController)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Geolocation Demo",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Map area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            if (location != null) {
                MapView(
                    modifier = Modifier.fillMaxSize(),
                    latitude = location!!.latitude,
                    longitude = location!!.longitude
                )
            } else {
                Text(
                    text = "Clique no botao para obter sua localizacao",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Location info
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            }

            location?.let { loc ->
                Text(
                    text = "Latitude: ${loc.latitude}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Longitude: ${loc.longitude}",
                    style = MaterialTheme.typography.bodyLarge
                )
                loc.accuracy?.let { acc ->
                    Text(
                        text = "Precisao: ${acc.toInt()}m",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            error?.let { err ->
                Text(
                    text = when (err) {
                        GeolocationError.PERMISSION_DENIED -> "Permissao negada"
                        GeolocationError.LOCATION_DISABLED -> "Localizacao desabilitada"
                        GeolocationError.TIMEOUT -> "Tempo esgotado"
                        GeolocationError.UNKNOWN -> "Erro desconhecido"
                    },
                    color = Color.Red,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        error = null
                        when (val result = geolocation.getCurrentLocation()) {
                            is GeolocationResult.Success -> {
                                location = result.location
                            }
                            is GeolocationResult.Error -> {
                                error = result.error
                            }
                        }
                        isLoading = false
                    }
                },
                enabled = !isLoading
            ) {
                Text(if (isLoading) "Obtendo..." else "Obter Localizacao")
            }
        }
    }
}
