package org.thechance.service_restaurant.api.endpoints

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import org.koin.ktor.ext.inject
import org.thechance.service_restaurant.api.models.RestaurantInfo
import org.thechance.service_restaurant.api.models.mappers.toDto
import org.thechance.service_restaurant.api.utils.OrderSocketHandler
import org.thechance.service_restaurant.domain.usecase.IManageOrderUseCase
import org.thechance.service_restaurant.domain.utils.OrderStatus
import org.thechance.service_restaurant.domain.utils.exceptions.INVALID_REQUEST_PARAMETER
import org.thechance.service_restaurant.domain.utils.exceptions.MultiErrorException
import org.thechance.service_restaurant.domain.utils.exceptions.NOT_FOUND
import kotlin.collections.set


fun Route.orderRoutes() {
    val manageOrder: IManageOrderUseCase by inject()
    val orderSocketHandler: OrderSocketHandler by inject()

    route("/order") {

        get("/{id}") {
            val id = call.parameters["id"] ?: throw MultiErrorException(listOf(NOT_FOUND))
            val result = manageOrder.getOrderById(orderId = id)
            call.respond(HttpStatusCode.OK, result.toDto())
        }

        post("/{id}/status") {
            val id = call.parameters["id"] ?: throw MultiErrorException(listOf(NOT_FOUND))
            val status = call.receiveParameters()["status"]?.toInt() ?: throw MultiErrorException(
                listOf(INVALID_REQUEST_PARAMETER)
            )
            val result = manageOrder.updateOrderStatus(
                orderId = id,
                state = OrderStatus.getOrderStatus(status)
            )
            call.respond(HttpStatusCode.OK, result)
        }

        get("/history/{id}") {
            val id = call.parameters["id"] ?: throw MultiErrorException(listOf(NOT_FOUND))
            val page = call.parameters["page"]?.toInt() ?: 1
            val limit = call.parameters["limit"]?.toInt() ?: 10

            val result = manageOrder.getOrdersHistory(restaurantId = id, page = page, limit = limit)
            call.respond(HttpStatusCode.OK, result.map { it.toDto() })
        }

        get("/{restaurantId}/orders") {
            val id = call.parameters["restaurantId"] ?: throw MultiErrorException(listOf(NOT_FOUND))
            val result = manageOrder.getActiveOrdersByRestaurantId(restaurantId = id)
            call.respond(HttpStatusCode.OK, result.map { it.toDto() })
        }

        webSocket("/{restaurantId}/{userId?}") {
            val restaurantId = call.parameters["restaurantId"]?.trim().orEmpty()
            val userId = call.parameters["userId"]?.trim().orEmpty()

            if (userId.isEmpty()) {
                orderSocketHandler.openedRestaurants[restaurantId] = RestaurantInfo(owner = this, mutableListOf())
                for (frame in incoming) return@webSocket

            } else if (manageOrder.isRestaurantOpened(restaurantId)) {
                orderSocketHandler.openedRestaurants[restaurantId]?.users?.add(mutableMapOf(userId to this))
                orderSocketHandler.broadcastOrder(
                    receiveChannel = incoming,
                    restaurantId = restaurantId,
                    manageOrder = manageOrder
                )
            }
        }

    }
}

