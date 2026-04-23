package com.nkot117.core.domain.repository

import com.nkot117.core.domain.model.Location

interface LocationRepository {
    suspend fun getLastLocation(): Location?
}
