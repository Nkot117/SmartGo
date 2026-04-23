package com.nkot117.core.data.repository

import android.Manifest
import androidx.annotation.RequiresPermission
import com.nkot117.core.data.datastore.LocationDataSource
import com.nkot117.core.domain.model.Location
import com.nkot117.core.domain.repository.LocationRepository
import javax.inject.Inject

class LocationRepositoryImpl @Inject
constructor(private val dataSource: LocationDataSource) :
    LocationRepository {
    @RequiresPermission(
        allOf = [
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ]
    )
    override suspend fun getLastLocation(): Location? = dataSource.getLastLocation()
}
