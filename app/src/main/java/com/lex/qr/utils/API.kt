package com.lex.qr.utils

import android.util.Log
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

const val url: String = "https://qrcode-wva2.shuttle.app/api"
const val avatarUrl: String = "https://api.imgbb.com/1/upload?key=48015623eba651182514366c617e96d7"

class API {

    private var jwtToken: String? = null

    private val client: HttpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        defaultRequest {
            jwtToken?.let {
                header("Authorization", "Bearer $it")
            }
        }
    }
    fun updateToken(newToken: String?) {
        jwtToken = newToken
    }
    fun getToken() = jwtToken

    suspend fun login(request: LoginRequest): Result<Claims> {
        return try {
            val response = client.post("$url/auth/login") {
                headers {
                    append(HttpHeaders.ContentType, "application/json")
                }
                setBody(request)
            }
            if (response.status.isSuccess()) {
                val data = response.body<Claims>()
                Result.success(data)
            }
            else {
                val error = response.body<Error>()
                Result.failure(Exception(error.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createClass(request: CreateClassRequest): Result<CreateClassResponse> {
        return try {
            val response = client.post("$url/class/create") {
                headers {
                    append(HttpHeaders.ContentType, "application/json")
                }
                setBody(request)
            }
            if (response.status.isSuccess()) {
                val data = response.body<CreateClassResponse>()
                Result.success(data)
            }
            else {
                val error = response.body<Error>()
                Result.failure(Exception(error.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun createUser(request: CreateUserRequest): Result<User> {
        return try {
            val response = client.post("$url/admin/user") {
                headers {
                    append(HttpHeaders.ContentType, "application/json")
                }
                setBody(request)
            }
            if (response.status.isSuccess()) {
                val data = response.body<User>()
                Result.success(data)
            }
            else {
                val error = response.body<Error>()
                Result.failure(Exception(error.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun createGroup(request: CreateGroupRequest): Result<Group> {
        return try {
            val response = client.post("$url/admin/group") {
                headers {
                    append(HttpHeaders.ContentType, "application/json")
                }
                setBody(request)
            }
            if (response.status.isSuccess()) {
                val data = response.body<Group>()
                Result.success(data)
            }
            else {
                val error = response.body<Error>()
                Result.failure(Exception(error.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun createSubject(request: CreateSubjectRequest): Result<Subject> {
        return try {
            val response = client.post("$url/admin/subject") {
                headers {
                    append(HttpHeaders.ContentType, "application/json")
                }
                setBody(request)
            }
            if (response.status.isSuccess()) {
                val data = response.body<Subject>()
                Result.success(data)
            }
            else {
                val error = response.body<Error>()
                Result.failure(Exception(error.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun joinClass(request: JoinClassRequest): Result<JoinClassResponse> {
        return try {
            val response = client.post("$url/class/join") {
                headers {
                    append(HttpHeaders.ContentType, "application/json")
                }
                setBody(request)
            }
            if (response.status.isSuccess()) {
                val data = response.body<JoinClassResponse>()
                Result.success(data)
            }
            else {
                val error = response.body<Error>()
                Result.failure(Exception(error.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun getStudents(key: String): Result<List<Student>> {
        return try {
            val response = client.get("$url/class/students/$key") {
                headers {
                    append(HttpHeaders.ContentType, "application/json")
                }
            }
            if (response.status.isSuccess()) {
                val data = response.body<List<Student>>()
                Result.success(data)
            }
            else {
                val error = response.body<Error>()
                Result.failure(Exception(error.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun getUsers(): Result<List<User>> {
        return try {
            val response = client.get("$url/admin/user") {
                headers {
                    append(HttpHeaders.ContentType, "application/json")
                }
            }
            if (response.status.isSuccess()) {
                val data = response.body<List<User>>()
                Result.success(data)
            }
            else {
                val error = response.body<Error>()
                Result.failure(Exception(error.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun getSubjects(): Result<List<Subject>> {
        return try {
            val response = client.get("$url/admin/subject") {
                headers {
                    append(HttpHeaders.ContentType, "application/json")
                }
            }
            if (response.status.isSuccess()) {
                val data = response.body<List<Subject>>()
                Result.success(data)
            }
            else {
                val error = response.body<Error>()
                Result.failure(Exception(error.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun getGroups(): Result<List<Group>> {
        return try {
            val response = client.get("$url/admin/group") {
                headers {
                    append(HttpHeaders.ContentType, "application/json")
                }
            }
            if (response.status.isSuccess()) {
                val data = response.body<List<Group>>()
                Result.success(data)
            }
            else {
                val error = response.body<Error>()
                Result.failure(Exception(error.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun getClasses(request: GetClassRequest): Result<List<GetClassResponse>> {
        return try {
            val response = client.post("$url/class/all") {
                headers {
                    append(HttpHeaders.ContentType, "application/json")
                }
                setBody(request)
            }
            if (response.status.isSuccess()) {
                val data = response.body<List<GetClassResponse>>()
                Result.success(data)
            }
            else {
                val error = response.body<Error>()
                Result.failure(Exception(error.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun getVisits(id: String): Result<List<ClassResponse>> {
        return try {
            val response = client.get("$url/class/visits/$id") {
                headers {
                    append(HttpHeaders.ContentType, "application/json")
                }
            }
            if (response.status.isSuccess()) {
                val data = response.body<List<ClassResponse>>()
                Result.success(data)
            }
            else {
                val error = response.body<Error>()
                Result.failure(Exception(error.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun deactivateStudent(id: String): Result<Student> {
        return try {
            val response = client.delete("$url/class/deactivate/$id") {
                headers {
                    append(HttpHeaders.ContentType, "application/json")
                }
            }
            if (response.status.isSuccess()) {
                val data = response.body<Student>()
                Result.success(data)
            }
            else {
                val error = response.body<Error>()
                Result.failure(Exception(error.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun deleteGroup(id: String): Result<Group> {
        return try {
            val response = client.delete("$url/admin/group/$id") {
                headers {
                    append(HttpHeaders.ContentType, "application/json")
                }
            }
            if (response.status.isSuccess()) {
                val data = response.body<Group>()
                Result.success(data)
            }
            else {
                val error = response.body<Error>()
                Result.failure(Exception(error.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun deleteSubject(id: String): Result<Subject> {
        return try {
            val response = client.delete("$url/admin/subject/$id") {
                headers {
                    append(HttpHeaders.ContentType, "application/json")
                }
            }
            if (response.status.isSuccess()) {
                val data = response.body<Subject>()
                Result.success(data)
            }
            else {
                val error = response.body<Error>()
                Result.failure(Exception(error.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun deleteUser(id: String): Result<Boolean> {
        return try {
            val response = client.delete("$url/admin/user/$id") {
                headers {
                    append(HttpHeaders.ContentType, "application/json")
                }
            }
            if (response.status.isSuccess()) {
                Result.success(true)
            }
            else {
                val error = response.body<Error>()
                Result.failure(Exception(error.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun uploadAvatar(imageBytes: ByteArray): Result<String> {
        return try {
            val response = client.post(avatarUrl) {
                headers {
                    append(HttpHeaders.ContentType, "application/json")
                }
                setBody(MultiPartFormDataContent(
                    formData {
                        append("image", imageBytes, Headers.build {
                            append(HttpHeaders.ContentType, "image/png")
                            append(HttpHeaders.ContentDisposition, "filename=avatar.png")
                        })
                    }
                ))
            }
            if (response.status.isSuccess()) {
                val avatarInfo = response.body<AvatarResponse>()
                val path = avatarInfo.data.url
                val res = client.post("$url/profile/avatar?path=$path") {
                    headers {
                        append(HttpHeaders.ContentType, "application/json")
                    }
                }
                if (res.status.isSuccess()) {
                    Result.success(res.bodyAsText())
                }
                else {
                    val error = res.body<Error>()
                    Result.failure(Exception(error.message))
                }
            } else {
                Result.failure(Exception(response.bodyAsText()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun evaluate(request: Rating): Result<Rating> {
        return try {
            val response = client.put("$url/class/evaluate") {
                headers {
                    append(HttpHeaders.ContentType, "application/json")
                }
                setBody(request)
            }
            if (response.status.isSuccess()) {
                val data = response.body<Rating>()
                Result.success(data)
            }
            else {
                val error = response.body<Error>()
                Result.failure(Exception(error.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUser(id: String, request: UpdateUserRequest): Result<User> {
        return try {
            val response = client.put("$url/admin/user/$id") {
                headers {
                    append(HttpHeaders.ContentType, "application/json")
                }
                setBody(request)
            }
            if (response.status.isSuccess()) {
                val data = response.body<User>()
                Result.success(data)
            }
            else {
                val error = response.body<Error>()
                Result.failure(Exception(error.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun updateGroup(id: String, request: CreateGroupRequest): Result<Group> {
        return try {
            val response = client.put("$url/admin/group/$id") {
                headers {
                    append(HttpHeaders.ContentType, "application/json")
                }
                setBody(request)
            }
            if (response.status.isSuccess()) {
                val data = response.body<Group>()
                Result.success(data)
            }
            else {
                val error = response.body<Error>()
                Result.failure(Exception(error.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun updateSubject(id: String, request: CreateSubjectRequest): Result<Subject> {
        return try {
            val response = client.put("$url/admin/subject/$id") {
                headers {
                    append(HttpHeaders.ContentType, "application/json")
                }
                setBody(request)
            }
            if (response.status.isSuccess()) {
                val data = response.body<Subject>()
                Result.success(data)
            }
            else {
                val error = response.body<Error>()
                Result.failure(Exception(error.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun createUsersByFile(file: ByteArray): Result<Boolean> {
        return try {
            val response = client.post("$url/admin/user/file") {
                headers {
                    append(HttpHeaders.ContentType, "application/json")
                }
                setBody(MultiPartFormDataContent(
                    formData {
                        append("file", file)
                    }
                ))
            }
            if (response.status.isSuccess()) {
                Result.success(true)
            } else {
                val error = response.body<Error>()
                Result.failure(Exception(error.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun getCurrent(): Result<Rating> {
        return try {
            val response = client.get("$url/class/current") {
                headers {
                    append(HttpHeaders.ContentType, "application/json")
                }
            }
            if (response.status.isSuccess()) {
                val data = response.body<Rating>()
                Result.success(data)
            }
            else {
                val error = response.body<Error>()
                Result.failure(Exception(error.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}