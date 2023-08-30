package org.thechance.service_taxi.data.gateway

import com.mongodb.client.model.Updates
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.ne
import org.litote.kmongo.set
import org.litote.kmongo.setTo
import org.thechance.service_taxi.api.dto.taxi.toCollection
import org.thechance.service_taxi.api.dto.taxi.toEntity
import org.thechance.service_taxi.api.dto.trip.toCollection
import org.thechance.service_taxi.api.dto.trip.toEntity
import org.thechance.service_taxi.data.DataBaseContainer
import org.thechance.service_taxi.data.collection.TaxiCollection
import org.thechance.service_taxi.data.collection.TripCollection
import org.thechance.service_taxi.data.utils.paginate
import org.thechance.service_taxi.domain.entity.Taxi
import org.thechance.service_taxi.domain.entity.Trip
import org.thechance.service_taxi.domain.gateway.ITaxiGateway
import java.util.UUID

class TaxiGateway(private val container: DataBaseContainer) : ITaxiGateway {
    // region taxi curd
    override suspend fun addTaxi(taxi: Taxi): Taxi {
        val taxiCollection = taxi.toCollection()
        container.taxiCollection.insertOne(taxiCollection)
        return taxiCollection.toEntity()
    }

    override suspend fun getTaxiById(taxiId: String): Taxi? {
        return container.taxiCollection.findOneById(UUID.fromString(taxiId))
            ?.takeIf { !it.isDeleted }?.toEntity()
    }

    override suspend fun editTaxi(taxiId: String,taxi: Taxi): Taxi {
        val taxiCollection = taxi.toCollection()
        container.taxiCollection.updateOne(
            filter = TaxiCollection::id eq UUID.fromString(taxiId),
            update = Updates.combine(
                set(TaxiCollection::plateNumber setTo taxiCollection.plateNumber),
                set(TaxiCollection::color setTo taxiCollection.color),
                set(TaxiCollection::type setTo taxiCollection.type),
                set(TaxiCollection::driverId setTo taxiCollection.driverId),
                set(TaxiCollection::isAvailable setTo taxiCollection.isAvailable),
                set(TaxiCollection::seats setTo taxiCollection.seats),
            )
        )
        return taxiCollection.toEntity()
    }

    override suspend fun getAllTaxes(page: Int, limit: Int): List<Taxi> {
        return container.taxiCollection.find(TaxiCollection::isDeleted ne true)
            .paginate(page, limit).toList().toEntity()
    }

    override suspend fun deleteTaxi(taxiId: String): Taxi? {
        return container.taxiCollection.findOneAndUpdate(
            filter = TaxiCollection::id eq UUID.fromString(taxiId),
            update = set(TaxiCollection::isDeleted setTo true)
        )?.toEntity()
    }

    override suspend fun getNumberOfTaxis(): Long {
        return container.taxiCollection.countDocuments()
    }
    //endregion

    //region trip curd
    override suspend fun addTrip(trip: Trip): Trip? {
        container.tripCollection.insertOne(trip.toCollection())
        return getTripById(trip.id)
    }

    override suspend fun getTripById(tripId: String): Trip? {
        return container.tripCollection.findOne(TripCollection::isDeleted ne true)?.toEntity()
    }

    override suspend fun getAllTrips(page: Int, limit: Int): List<Trip> {
        return container.tripCollection.find(TripCollection::isDeleted ne true)
            .paginate(page, limit).toList()
            .toEntity()
    }

    override suspend fun getDriverTripsHistory(
        driverId: String,
        page: Int,
        limit: Int
    ): List<Trip> {
        return container.tripCollection.find(
            and(
                TripCollection::isDeleted ne true,
                TripCollection::driverId eq UUID.fromString(driverId)
            )
        ).paginate(page, limit).toList().toEntity()
    }

    override suspend fun getClientTripsHistory(
        clientId: String,
        page: Int,
        limit: Int
    ): List<Trip> {
        return container.tripCollection.find(
            and(
                TripCollection::isDeleted ne true,
                TripCollection::clientId eq UUID.fromString(clientId)
            )
        ).paginate(page, limit).toList().toEntity()
    }

    override suspend fun deleteTrip(tripId: String): Trip? {
        val trip = container.tripCollection.findOneById(UUID.fromString(tripId))
        container.tripCollection.updateOneById(
            id = UUID.fromString(tripId),
            update = Updates.set(TripCollection::isDeleted.name, true)
        )
        return trip?.toEntity()
    }

    override suspend fun approveTrip(tripId: String, taxiId: String, driverId: String): Trip? {
        return container.tripCollection.findOneAndUpdate(
            filter = and(
                TripCollection::isDeleted ne true,
                TripCollection::id eq UUID.fromString(tripId),
            ),
            update = Updates.combine(
                Updates.set(TripCollection::taxiId.name, UUID.fromString(taxiId)),
                Updates.set(TripCollection::driverId.name, UUID.fromString(driverId)),
                Updates.set(
                    TripCollection::startDate.name, Clock.System.now().toLocalDateTime(
                        TimeZone.currentSystemDefault()
                    ).toString()
                )
            )
        )?.toEntity()
    }

    override suspend fun finishTrip(tripId: String, driverId: String): Trip? {
        return container.tripCollection.findOneAndUpdate(
            filter = and(
                TripCollection::isDeleted ne true,
                TripCollection::id eq UUID.fromString(tripId),
                TripCollection::driverId eq UUID.fromString(driverId),
            ),
            update = Updates.set(
                TripCollection::endDate.name, Clock.System.now().toLocalDateTime(
                    TimeZone.currentSystemDefault()
                ).toString()
            )
        )?.toEntity()
    }

    override suspend fun rateTrip(tripId: String, rate: Double): Trip? {
        return container.tripCollection.findOneAndUpdate(
            filter = and(
                TripCollection::isDeleted ne true,
                TripCollection::id eq UUID.fromString(tripId),
            ),
            update = Updates.set(TripCollection::rate.name, rate)
        )?.toEntity()
    }
    //endregion
}