package com.nkot117.core.data.datastore

import android.Manifest
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.nkot117.core.domain.model.Location
import javax.inject.Inject
import kotlinx.coroutines.tasks.await

class LocationDataSource @Inject constructor(
    private val fusedLocationClient: FusedLocationProviderClient
) {
    @RequiresPermission(
        allOf = [
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ]
    )
    suspend fun getLastLocation(): Location? = fusedLocationClient.lastLocation.await()?.let {
        Location(
            latitude = it.latitude,
            longitude = it.longitude
        )
    }
}
