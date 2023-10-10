package org.thechance.api_gateway.endpoints

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import org.koin.ktor.ext.inject
import org.thechance.api_gateway.data.localizedMessages.LocalizedMessagesFactory
import org.thechance.api_gateway.data.model.taxi.TaxiDto
import org.thechance.api_gateway.data.model.taxi.TripDto
import org.thechance.api_gateway.data.service.IdentityService
import org.thechance.api_gateway.data.service.TaxiService
import org.thechance.api_gateway.endpoints.utils.WebSocketServerHandler
import org.thechance.api_gateway.endpoints.utils.authenticateWithRole
import org.thechance.api_gateway.endpoints.utils.extractLocalizationHeader
import org.thechance.api_gateway.endpoints.utils.respondWithResult
import org.thechance.api_gateway.util.Claim
import org.thechance.api_gateway.util.Role

fun Route.taxiRoutes() {
    val taxiService: TaxiService by inject()
    val identityService: IdentityService by inject()
    val localizedMessagesFactory by inject<LocalizedMessagesFactory>()
    val webSocketServerHandler: WebSocketServerHandler by inject()

    route("/taxi") {

        get {
            val language = extractLocalizationHeader()
            val page = call.parameters["page"]?.toInt() ?: 1
            val limit = call.parameters["limit"]?.toInt() ?: 20
            val result = taxiService.getAllTaxi(language, page, limit)
            respondWithResult(HttpStatusCode.OK, result)
        }

        post {
            val taxiDto = call.receive<TaxiDto>()

            val language = extractLocalizationHeader()
            val user = identityService.getUserByUsername(taxiDto.driverUsername, language)
            val result = taxiService.createTaxi(taxiDto.copy(driverId = user.id), language)
            val successMessage = localizedMessagesFactory.createLocalizedMessages(language).taxiCreatedSuccessfully
            respondWithResult(HttpStatusCode.Created, result, successMessage)
        }

        put("/{taxiId}") {
            val id = call.parameters["taxiId"] ?: ""
            val taxiDto = call.receive<TaxiDto>()
            val language = extractLocalizationHeader()
            val result = taxiService.editTaxi(id, taxiDto, language)
            val successMessage =
                localizedMessagesFactory.createLocalizedMessages(language).taxiUpdateSuccessfully
            respondWithResult(HttpStatusCode.OK, result, successMessage)
        }

        get("/{taxiId}") {
            val id = call.parameters["taxiId"] ?: ""
            val language = extractLocalizationHeader()
            val result = taxiService.getTaxiById(id, language)
            respondWithResult(HttpStatusCode.OK, result)
        }

        delete("/{taxiId}") {
            val language = extractLocalizationHeader()
            val id = call.parameters["taxiId"] ?: ""
            val result = taxiService.deleteTaxi(id, language)
            val successMessage =
                localizedMessagesFactory.createLocalizedMessages(language).taxiDeleteSuccessfully
            respondWithResult(HttpStatusCode.OK, result, successMessage)
        }
    }

    route("/taxis") {
        get("/search") {
            val language = extractLocalizationHeader()
            val page = call.parameters["page"]?.toInt() ?: 1
            val limit = call.parameters["limit"]?.toInt() ?: 20
            val query = call.request.queryParameters["query"]?.trim().orEmpty()
            val status = call.request.queryParameters["status"]?.trim()?.toBoolean()
            val color = call.request.queryParameters["color"]?.trim()?.toLongOrNull()
            val seats = call.request.queryParameters["seats"]?.trim()?.toIntOrNull()
            val result = taxiService.findTaxisByQuery(page, limit, status, color, seats, query, language)
            respondWithResult(HttpStatusCode.OK, result)
        }
    }

    route("/trip") {

        get("/{tripId}") {
            val language = extractLocalizationHeader()
            val tripId = call.parameters["tripId"]?.trim().orEmpty()
            val trip = taxiService.getTripById(tripId = tripId, language)
            respondWithResult(HttpStatusCode.OK, trip)
        }

        authenticateWithRole(Role.END_USER) {
            get("/history") {
                val tokenClaim = call.principal<JWTPrincipal>()
                val userId = tokenClaim?.get(Claim.USER_ID).toString()
                val page = call.parameters["page"]?.trim()?.toInt() ?: 1
                val limit = call.parameters["limit"]?.trim()?.toInt() ?: 10
                val language = extractLocalizationHeader()
                val result = taxiService.getTripsHistoryForUser(
                    userId = userId,
                    page = page,
                    limit = limit,
                    languageCode = language
                )
                respondWithResult(HttpStatusCode.OK, result)
            }
        }

        post {
            val language = extractLocalizationHeader()
            val successMessage = localizedMessagesFactory.createLocalizedMessages(language).tripCreatedSuccessfully
            val trip = call.receive<TripDto>()
            val createdTrip = taxiService.createTrip(trip, language)
            respondWithResult(HttpStatusCode.Created, createdTrip, successMessage)
        }

        webSocket("/taxi/{driverId}") {
            val driverId = call.parameters["driverId"]?.trim().orEmpty()
            val taxiTrips = taxiService.getTaxiTrips(driverId)
            webSocketServerHandler.sessions[driverId] = this
            webSocketServerHandler.sessions[driverId]?.let {
                webSocketServerHandler.tryToCollectFormWebSocket(taxiTrips, it)
            }
        }

        webSocket("/delivery/{deliveryId}") {
            val deliveryId = call.parameters["deliveryId"]?.trim().orEmpty()
            val deliveryTrips = taxiService.getDeliveryTrips(deliveryId)
            webSocketServerHandler.sessions[deliveryId] = this
            webSocketServerHandler.sessions[deliveryId]?.let {
                webSocketServerHandler.tryToCollectFormWebSocket(deliveryTrips, it)
            }
        }

        put("/approve") {
            val language = extractLocalizationHeader()
            val successMessage = localizedMessagesFactory.createLocalizedMessages(language).tripApproved
            val parameters = call.receiveParameters()
            val taxiId = parameters["taxiId"]?.trim().orEmpty()
            val driverId = parameters["driverId"]?.trim().orEmpty()
            val tripId = parameters["tripId"]?.trim().orEmpty()
            val approvedTrip = taxiService.approveTrip(taxiId = taxiId, tripId = tripId, driverId = driverId, language)
            respondWithResult(HttpStatusCode.OK, approvedTrip, successMessage)
        }

        put("/received") {
            val language = extractLocalizationHeader()
            val successMessage = localizedMessagesFactory.createLocalizedMessages(language).tripArrived
            val parameters = call.receiveParameters()
            val tripId = parameters["tripId"]?.trim().orEmpty()
            val driverId = parameters["driverId"]?.trim().orEmpty()
            val receivedTrip = taxiService.updateTripAsReceived(tripId = tripId, driverId = driverId, language)
            respondWithResult(HttpStatusCode.OK, receivedTrip, successMessage)
        }

        put("/finish") {
            val language = extractLocalizationHeader()
            val successMessage = localizedMessagesFactory.createLocalizedMessages(language).tripFinished
            val parameters = call.receiveParameters()
            val driverId = parameters["driverId"]?.trim().orEmpty()
            val tripId = parameters["tripId"]?.trim().orEmpty()
            val finishedTrip = taxiService.updateTripAsFinished(tripId = tripId, driverId = driverId, language)
            respondWithResult(HttpStatusCode.OK, finishedTrip, successMessage)
        }
    }
}