package org.thechance.service_identity.api.endpoints

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import org.thechance.service_identity.domain.entity.User
import org.thechance.service_identity.domain.usecases.user.CreateUserUseCase

fun Route.userRoutes() {

    val createUserUseCase: CreateUserUseCase by inject()

    route("/user"){

        get {

        }

        post{
            try {
                val user = call.receive<User>()
                val result = createUserUseCase.invoke(user)
                call.respond(result)
            }catch (e: Exception){
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        put {

        }
    }


}