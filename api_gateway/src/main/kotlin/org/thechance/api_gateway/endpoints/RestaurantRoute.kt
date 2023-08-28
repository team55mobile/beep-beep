package org.thechance.api_gateway.endpoints

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import org.thechance.api_gateway.data.mappers.toRestaurant
import org.thechance.api_gateway.endpoints.gateway.IRestaurantGateway
import org.thechance.api_gateway.endpoints.utils.extractLocalizationHeader
import org.thechance.api_gateway.endpoints.utils.respondWithResult
import java.util.*

fun Route.restaurantRoutes() {
    val restaurantGateway: IRestaurantGateway by inject()

    route("/restaurant") {
        get("/{id}") {
            val (language, countryCode) = extractLocalizationHeader()
            val restaurantId = call.parameters["id"]?.trim().toString()
            val restaurant =
                restaurantGateway.getRestaurantInfo(locale = Locale(language, countryCode), id = restaurantId)
            respondWithResult(HttpStatusCode.OK, restaurant.toRestaurant())
        }
    }
}