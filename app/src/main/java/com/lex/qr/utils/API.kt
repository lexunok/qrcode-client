package com.lex.qr.utils

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
import com.lex.qr.BuildConfig
import io.ktor.client.statement.HttpResponse

const val url: String = BuildConfig.BASE_URL
const val avatarUrl: String = BuildConfig.AVATAR_URL

class API {

    var jwtToken: String? = null

    private val client: HttpClient = HttpClient(Android) {
        engine {
            connectTimeout = 10_000
            socketTimeout = 10_000
        }
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
            headers {
                append(HttpHeaders.ContentType, "application/json")
            }
        }
    }

    private suspend inline fun <reified T> handleApiCall(
        crossinline call: suspend () -> HttpResponse
    ): Result<T> {
        return try {
            val response = call()
            if (response.status.isSuccess()) {
                val data = response.body<T>()
                Result.success(data)
            } else {
                val error = response.body<Error>()
                Result.failure(Exception(error.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(request: LoginRequest): Result<Claims> {
        return handleApiCall {
            client.post("$url/auth/login") {
                setBody(request)
            }
        }
    }

    suspend fun createClass(request: CodeRequest): Result<CodeResponse> {
        return handleApiCall {
            client.post("$url/class/create") {
                setBody(request)
            }
        }
    }
    suspend fun createUser(request: CreateUserRequest): Result<User> {
        return handleApiCall {
            client.post("$url/admin/user") {
                setBody(request)
            }
        }
    }
    suspend fun createUsers(fileContent: String): Result<Unit> {
        return try {
            val response = client.post("$url/admin/user/file") {
                headers {
                    append(HttpHeaders.ContentType, "multipart/form-data")
                }
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("file", fileContent.toByteArray(), Headers.build {
                                append(HttpHeaders.ContentType, "text/csv")
                                append(HttpHeaders.ContentDisposition, "filename=\"users.csv\"")
                            })
                        }
                    )
                )
            }
            if (response.status.isSuccess()) {
                Result.success(Unit)
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
        return handleApiCall {
            client.post("$url/admin/group") {
                setBody(request)
            }
        }
    }
    suspend fun createSubject(request: CreateSubjectRequest): Result<Subject> {
        return handleApiCall {
            client.post("$url/admin/subject") {
                setBody(request)
            }
        }
    }
    suspend fun joinClass(request: JoinClassRequest): Result<JoinClassResponse> {
        return handleApiCall {
            client.post("$url/class/join") {
                setBody(request)
            }
        }
    }
    suspend fun getStudents(key: String): Result<List<Student>> {
        return handleApiCall {
            client.get("$url/class/students/$key")
        }
    }
    suspend fun getUsers(): Result<List<User>> {
        return handleApiCall {
            client.get("$url/admin/user")
        }
    }
    suspend fun getSubjects(): Result<List<Subject>> {
        return handleApiCall {
            client.get("$url/admin/subject")
        }
    }
    suspend fun getGroups(): Result<List<Group>> {
        return handleApiCall {
            client.get("$url/admin/group")
        }
    }
    suspend fun getGroup(id: String): Result<Group> {
        return handleApiCall {
            client.get("$url/admin/group/$id")
        }
    }
    suspend fun getClasses(request: GetClassRequest): Result<List<GetClassResponse>> {
        return handleApiCall {
            client.post("$url/class/all") {
                setBody(request)
            }
        }
    }
    suspend fun getVisits(id: String): Result<List<ClassResponse>> {
        return handleApiCall {
            client.get("$url/class/visits/$id")
        }
    }
    suspend fun deactivateStudent(id: String): Result<Student> {
        return handleApiCall {
            client.delete("$url/class/deactivate/$id")
        }
    }
    suspend fun activateStudent(id: String): Result<Student> {
        return handleApiCall {
            client.put("$url/class/activate/$id")
        }
    }
    suspend fun deleteGroup(id: String): Result<Group> {
        return handleApiCall {
            client.delete("$url/admin/group/$id")
        }
    }
    suspend fun deleteSubject(id: String): Result<Subject> {
        return handleApiCall {
            client.delete("$url/admin/subject/$id")
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
                val res = client.post("$url/profile/avatar?path=$path")
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
        return handleApiCall {
            client.put("$url/class/evaluate") {
                setBody(request)
            }
        }
    }

    suspend fun updateUser(id: String, request: UpdateUserRequest): Result<User> {
        return handleApiCall {
            client.put("$url/admin/user/$id") {
                setBody(request)
            }
        }
    }
    suspend fun updateGroup(id: String, request: CreateGroupRequest): Result<Group> {
        return handleApiCall {
            client.put("$url/admin/group/$id") {
                setBody(request)
            }
        }
    }
    suspend fun updateSubject(id: String, request: CreateSubjectRequest): Result<Subject> {
        return handleApiCall {
            client.put("$url/admin/subject/$id") {
                setBody(request)
            }
        }
    }
    suspend fun getCurrent(): Result<CurrentClass> {
        return handleApiCall {
            client.get("$url/class/current")
        }
    }
    suspend fun sendCode(email: String): Result<RecoveryPassword> {
        return handleApiCall {
            client.post("$url/auth/send/$email")
        }
    }
    suspend fun updatePassword(request: NewPasswordRequest): Result<String> {
        return handleApiCall {
            client.post("$url/auth/password") {
                setBody(request)
            }
        }
    }
    ////////////
    //STATISTICS
    ////////////
    suspend fun getStudentsByGroup(id: String): Result<List<StudentStats>> {
        return handleApiCall {
            client.get("$url/stats/students/$id")
        }
    }
    suspend fun getGroupBars(request: StatisticRequest): Result<List<GroupBar>> {
        return handleApiCall {
            client.post("$url/stats/bar/group") {
                setBody(request)
            }
        }
    }
    suspend fun getGroupSubjectHist(request: StatisticRequest): Result<List<SubjectHist>> {
        return handleApiCall {
            client.post("$url/stats/hist/group") {
                setBody(request)
            }
        }
    }
    suspend fun getStudentSubjectHist(request: StatisticRequest): Result<List<SubjectHist>> {
        return handleApiCall {
            client.post("$url/stats/hist/student") {
                setBody(request)
            }
        }
    }
    suspend fun getStudentAttendance(request: StatisticRequest): Result<Attendance> {
        return handleApiCall {
            client.post("$url/stats/attendance/student") {
                setBody(request)
            }
        }
    }
    suspend fun getGroupAttendance(request: StatisticRequest): Result<Attendance> {
        return handleApiCall {
            client.post("$url/stats/attendance/group") {
                setBody(request)
            }
        }
    }
    suspend fun getStudentLineChart(request: StatisticRequest): Result<List<LineChart>> {
        return handleApiCall {
            client.post("$url/stats/line/student") {
                setBody(request)
            }
        }
    }
    suspend fun getGroupLineChart(request: StatisticRequest): Result<List<LineChart>> {
        return handleApiCall {
            client.post("$url/stats/line/group") {
                setBody(request)
            }
        }
    }
    ////////////
    //ARCHIVE
    ////////////
    suspend fun getGroupsInArchive(id: String): Result<List<Group>>{
        return handleApiCall {
            client.get("$url/admin/group/archive/$id")
        }
    }
    suspend fun restoreGroup(id: String): Result<Group>{
        return handleApiCall {
            client.put("$url/admin/group/restore/$id")
        }
    }
    suspend fun getSubjectsInArchive(id: String): Result<List<Subject>>{
        return handleApiCall {
            client.get("$url/admin/subject/archive/$id")
        }
    }
    suspend fun restoreSubject(id: String): Result<Subject>{
        return handleApiCall {
            client.put("$url/admin/subject/restore/$id")
        }
    }
    suspend fun getSemesters(): Result<List<Semester>>{
        return handleApiCall {
            client.get("$url/admin/semester")
        }
    }
    suspend fun openSemester(): Result<Semester>{
        return handleApiCall {
            client.post("$url/admin/semester")
        }
    }
    suspend fun closeSemester(): Result<Semester>{
        return handleApiCall {
            client.delete("$url/admin/semester")
        }
    }
}