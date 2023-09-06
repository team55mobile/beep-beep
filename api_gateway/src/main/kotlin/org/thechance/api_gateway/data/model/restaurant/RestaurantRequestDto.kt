package org.thechance.api_gateway.data.model.restaurant

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.thechance.api_gateway.data.model.LocationDto

@Serializable
data class RestaurantRequestDto(
    @SerialName("id") val id: String? = null,
    @SerialName("ownerId") val ownerId: String? = null,
    @SerialName("requestedRestaurantId") val requestedRestaurantId: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("priceLevel") val priceLevel: String? = null,
    @SerialName("rate") val rate: Double? = null,
    @SerialName("phone") val phone: String? = null,
    @SerialName("openingTime") val openingTime: String? = null,
    @SerialName("closingTime") val closingTime: String? = null,
    @SerialName("address") val address: String? = null,
    @SerialName("location") val locationDto: LocationDto? = null,
)

fun RestaurantDto.toRestaurantRequestDto(): RestaurantRequestDto {
    return RestaurantRequestDto(
        id = id,
        ownerId = ownerId,
        requestedRestaurantId = requestedRestaurantId,
        name = name,
        description = description,
        priceLevel = priceLevel,
        rate = rate,
        phone = phone,
        openingTime = openingTime,
        closingTime = closingTime,
        address = address,
        locationDto = locationDto
    )
}
