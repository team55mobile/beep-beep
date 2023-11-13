package domain.usecase

import data.remote.model.LocationDto
import domain.entity.Order
import domain.gateway.local.ILocalConfigurationGateway
import domain.gateway.remote.IMapRemoteGateway
import kotlinx.coroutines.flow.Flow

interface IManageOrderUseCase {
    suspend fun getOrders(): Flow<Order>
    suspend fun sendLocation(location: LocationDto, tripId: String)
    suspend fun updateTrip(tripId: String): Order
}

class ManageOrderUseCase(
    private val remoteGateway: IMapRemoteGateway,
    private val localGateWay: ILocalConfigurationGateway,
) : IManageOrderUseCase {
    override suspend fun getOrders(): Flow<Order> {
        return remoteGateway.getOrders()
    }

    override suspend fun sendLocation(location: LocationDto, tripId: String) {
        remoteGateway.sendLocation(location, tripId)
    }

    override suspend fun updateTrip(tripId: String): Order {
        val taxiId = getTaxiId()
        return remoteGateway.updateTrip(taxiId, tripId)
    }

    private suspend fun getTaxiId(): String {
        return localGateWay.getTaxiId()
    }

}