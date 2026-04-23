package com.nkot117.core.domain.usecase.weather

import com.nkot117.core.domain.repository.LocationRepository
import javax.inject.Inject

class GetLocationUseCase @Inject
constructor(private val repository: LocationRepository) {
    suspend operator fun invoke() = repository.getLastLocation()
}
