package com.lex.qr.utils

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class API {

    private val url: String = "https://qrcode-wva2.shuttle.app/api"

    private val client: HttpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }
    //Добавить проверки
    suspend fun login(request: LoginRequest): User? {
        return client.post("$url/profile/login") {
            headers {
                append(HttpHeaders.ContentType, "application/json")
            }
            setBody(request)
        }.body()
    }

    suspend fun createClass(request: CreateClassRequest): CreateClassResponse? {
        return client.post("$url/class/create") {
            headers {
                append(HttpHeaders.ContentType, "application/json")
            }
            setBody(request)
        }.body()
    }
    suspend fun joinClass(request: JoinClassRequest): JoinClassResponse? {
        return client.post("$url/class/join") {
            headers {
                append(HttpHeaders.ContentType, "application/json")
            }
            setBody(request)
        }.body()
    }
    suspend fun getStudents(key: String): List<Student>? {
        return client.get("$url/class/students/$key") {
                headers {
                    append(HttpHeaders.ContentType, "application/json")
                }
            }.body()
    }
    suspend fun getUsers(): List<User>? {
        return client.get("$url/admin/user") {
            headers {
                append(HttpHeaders.ContentType, "application/json")
            }
        }.body()
    }
    suspend fun getSubjects(): List<Subject>? {
         return client.get("$url/admin/subject") {
            headers {
                append(HttpHeaders.ContentType, "application/json")
            }
        }.body()
    }
    suspend fun getGroups(): List<Group>? {
        return client.get("$url/admin/group") {
            headers {
                append(HttpHeaders.ContentType, "application/json")
            }
        }.body()
    }
    suspend fun getClasses(request: GetClassRequest): List<GetClassResponse>? {
        return client.post("$url/class/all") {
            headers {
                append(HttpHeaders.ContentType, "application/json")
            }
            setBody(request)
        }.body()
    }
    suspend fun getVisits(id: String): List<ClassResponse>? {
        return try {
            val response: List<ClassResponse> = client.get("$url/class/visits/$id") {
                headers {
                    append(HttpHeaders.ContentType, "application/json")
                }
            }.body()
            response
        }
        catch (e: Exception) {
            Log.i("ERROR", e.toString())
            null
        }
    }
    suspend fun deactivateStudent(id: String): Student? {
        return try {
            val student: Student = client.delete("$url/class/deactivate/$id") {
                headers {
                    append(HttpHeaders.ContentType, "application/json")
                }
            }.body()

            student
        } catch (e: Exception) {
            Log.i("ERROR", e.toString())
            null
        }
    }
    suspend fun uploadAvatar(id: String, imageBytes: ByteArray): Boolean {
        return try {
            client.post("$url/profile/avatar/$id") {
                setBody(MultiPartFormDataContent(
                    formData {
                        append("avatar", imageBytes, Headers.build {
                            append(HttpHeaders.ContentType, "image/png")
                            append(HttpHeaders.ContentDisposition, "filename=avatar.png")
                        })
                    }
                ))
            }.status.isSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    //Тут будет ответ наверн
    //Не крашится но и валидации нет ведь нет модели
    suspend fun evaluate(request: RatingRequest): Boolean {
        return try {
            client.put("$url/class/evaluate") {
                headers {
                    append(HttpHeaders.ContentType, "application/json")
                }
                setBody(request)
            }.status.isSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}