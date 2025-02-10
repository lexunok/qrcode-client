package com.lex.qr

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json

class API {

    private val url: String = "https://qrcode-wva2.shuttle.app/api"

    private val client: HttpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json()
        }
    }

    suspend fun createClass(request: CreateClassRequest): CreateClassResponse? {
        val response:CreateClassResponse? = client.post("$url/class/create") {
            headers {
                append(HttpHeaders.ContentType, "application/json")
            }
            setBody(request)
        }.body()
        return response
    }
    suspend fun joinClass(request: JoinClassRequest): JoinClassResponse? {
        val response:JoinClassResponse? = client.post("$url/class/join") {
            headers {
                append(HttpHeaders.ContentType, "application/json")
            }
            setBody(request)
        }.body()
        return response
    }
    suspend fun getStudents(key: String): List<Student>? {
        val response: List<Student>? = client.get("$url/class/students/$key") {
                headers {
                    append(HttpHeaders.ContentType, "application/json")
                }
            }.body()
        return response
    }
    suspend fun getUsers(): List<User>? {
        val response: List<User>? = client.get("$url/admin/user/all") {
            headers {
                append(HttpHeaders.ContentType, "application/json")
            }
        }.body()
        return response
    }
    suspend fun getSubjects(): List<Subject>? {
        val response: List<Subject>? = client.get("$url/admin/subject/all") {
            headers {
                append(HttpHeaders.ContentType, "application/json")
            }
        }.body()
        return response
    }
    suspend fun getGroups(): List<Group>? {
        val response: List<Group>? = client.get("$url/admin/group/all") {
            headers {
                append(HttpHeaders.ContentType, "application/json")
            }
        }.body()
        return response
    }
}