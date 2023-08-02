package org.thechance.service_restaurant.api.usecases

import org.thechance.service_restaurant.entity.Meal

interface UpdateMealUseCase {

    suspend operator fun invoke(meal: Meal) : Boolean

}