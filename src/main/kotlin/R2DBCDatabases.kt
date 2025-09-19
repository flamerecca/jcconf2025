package com.example

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.r2dbc.spi.IsolationLevel.READ_COMMITTED
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.r2dbc.*
import org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.transactions.*
import kotlin.text.toInt

fun Application.r2dbcDatabases() {
    val r2dbcDatabase =
        R2dbcDatabase.connect {
            defaultMaxAttempts = 1
            defaultR2dbcIsolationLevel = READ_COMMITTED

            setUrl("r2dbc:h2:mem:///test;DB_CLOSE_DELAY=-1;")
        }
    routing {
        // Read user
        get("r2dbc/users/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            runBlocking {
                suspendTransaction(db = r2dbcDatabase) {
                    SchemaUtils.create(Users)
                    Users.insert {
                        it[name] = "Bob"
                        it[age] = 30
                    }[Users.id]
                }
            }
            val user =
                suspendTransaction {
                    Users
                        .selectAll()
                        .where { Users.id eq id }
                        .map { ExposedUser(it[Users.name], it[Users.age]) }
                        .singleOrNull()
                }
            if (user != null) {
                call.respond(HttpStatusCode.OK, user)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}
