package org.thechance.service_restaurant.api.usecases

import org.thechance.service_restaurant.entity.Restaurant

interface GetRestaurantsUseCase {
    suspend operator fun invoke(): List<Restaurant>
}

interface CreateRestaurantUseCase {
    suspend operator fun invoke(name: String): Boolean
}