package com.example

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.*
import kotlinx.coroutines.delay

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        get("/api-1") {
            delay(3000)
            call.respondText("api-1")
        }
        get("/api-2") {
            delay(3000)
            call.respondText("api-2")
        }
        get("/api-3") {
            delay(3000)
            call.respondText("api-3")
        }

        get("/api-all") {
            val client = HttpClient(CIO)

            val allData =
                coroutineScope {
                    val request1 = async { client.get("http://0.0.0.0:8080/api-1") }
                    val request2 = async { client.get("http://0.0.0.0:8080/api-2") }
                    val request3 = async { client.get("http://0.0.0.0:8080/api-3") }
                    val requestContents =
                        listOf(
                            request1.await().bodyAsText(),
                            request2.await().bodyAsText(),
                            request3.await().bodyAsText(),
                        )
                    requestContents.joinToString()
                }
            call.respondText(allData)
        }
    }
}
