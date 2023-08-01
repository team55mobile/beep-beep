package org.thechance.service_restaurant.api.endpoints

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import org.thechance.service_restaurant.api.models.RestaurantDto
import org.thechance.service_restaurant.api.usecases.RestaurantUseCasesContainer
import org.thechance.service_restaurant.entity.toDto

fun Route.restaurantRoutes() {

    val restaurantUseCases: RestaurantUseCasesContainer by inject()

    route("/restaurant") {

        get {
            val restaurants = restaurantUseCases.getRestaurants().toDto()
            call.respond(HttpStatusCode.OK, restaurants)
        }

        get("/{id}") {
            val restaurantId = call.parameters["id"] ?: ""
            val restaurant = restaurantUseCases.getRestaurantDetails(restaurantId).toDto()
            call.respond(HttpStatusCode.OK, restaurant)
        }

        post {
            val restaurant = call.receive<RestaurantDto>()
            val result = restaurantUseCases.addRestaurant(restaurant.toEntity())
            call.respond(HttpStatusCode.Created, result)
        }

        put {
            val restaurant = call.receive<RestaurantDto>()
            val result = restaurantUseCases.updateRestaurant(restaurant.toEntity())
            call.respond(HttpStatusCode.OK, result)
        }

        delete("/{id}") {
            val restaurantId = call.parameters["id"] ?: ""
            val result = restaurantUseCases.deleteRestaurant(restaurantId)
            call.respond(HttpStatusCode.OK, result)
        }

    }
}