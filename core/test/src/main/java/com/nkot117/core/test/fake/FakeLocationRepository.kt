package com.nkot117.core.test.fake

import com.nkot117.core.domain.model.Location
import com.nkot117.core.domain.repository.LocationRepository

class FakeLocationRepository : LocationRepository {
    private var location: Location? = null

    fun seed(location: Location?) {
        this.location = location
    }

    override suspend fun getLastLocation(): Location? = location
}
