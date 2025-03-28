package com.lex.qr.utils

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

enum class Role {
    ADMIN, STAFF, STUDENT
}



@Serializable
data class LoginRequest(
    var email: String,
    var password: String,
)
@Serializable
data class RecoveryPassword(
    val id: String
)
@Serializable
data class NewPasswordRequest(
    val id: String,
    val code: String,
    val password: String
)



@Serializable
data class Error(
    var status: Int,
    var message: String
)

@Serializable
data class Rating(
    var id: String,
    var rating: Int
)
@Serializable
data class CurrentClass(
    var id: String,
    var rating: Int,
    @SerialName("is_timer") val isTimer: Boolean
)

@Serializable
data class CreateUserRequest(
    val email: String,
    val password: String,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    val role: String,
    @SerialName("group_id") val groupId: String?
)

@Serializable
data class CreateGroupRequest(
    val name: String
)

@Serializable
data class CreateSubjectRequest(
    val name: String
)


@Serializable
data class CodeRequest(
    @SerialName("subject_id")val subjectId: String,
    @SerialName("group_id") val groupId: String,
    val lifetime: Int,
    val geolocation: String
)
@Serializable
data class CodeResponse(
    @SerialName("public_id") val publicId: String,
    @SerialName("subject_id") val subjectId: String,
    val lifetime: Int,
    @SerialName("group_id") val groupId: String
)


@Serializable
data class JoinClassRequest(
    @SerialName("public_id") val publicId: String,
    @SerialName("student_geolocation") val studentGeolocation: String,
    val device: String
)

@Serializable
data class JoinClassResponse(
    val id: String
)

//AVATAR
@Serializable
data class AvatarResponse(
    val data: AvatarData,
)
@Serializable
data class AvatarData(
    @SerialName("url") val url: String,
)
/////////////

@Serializable
data class GetClassResponse(
    @SerialName("created_at")
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime,
    @SerialName("public_id") val publicId: String,
    val rating: Double?
)
@Serializable
data class GetClassRequest(
    @SerialName("subject_id") val subjectId: String,
    @SerialName("group_id") val groupId: String
)
@Serializable
data class ClassResponse(
    val rating: Int?,
    @SerialName("created_at")
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime,
    @SerialName("subject_name") val subjectName: String,
    @SerialName("is_active") val isActive: Boolean,
    @SerialName("public_id") val publicId: String
)

@Serializable
data class UpdateUserRequest(
    val email: String,
    val password: String,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    val role: String,
    @SerialName("group_id") val groupId: String?
)

@Serializable
data class Student(
    val id: String,
    val rating: Int?,
    @SerialName("student_id") val studentId: String,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    @SerialName("avatar_url") val avatarUrl: String?,
    @SerialName("is_active") val isActive: Boolean,
    @SerialName("public_id") val publicId: String
)
@Serializable
data class User(
    val id: String,
    val email: String,
    val password: String,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    var role: Role,
    @SerialName("group_id") val groupId: String?,
    @SerialName("avatar_url") val avatarUrl: String?,
    @SerialName("created_at") val createdAt: String
)
@Serializable
data class Claims(
    val token: String,
    val id: String,
    val email: String,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    var role: Role,
    @SerialName("group_id") val groupId: String?,
    @SerialName("avatar_url") val avatarUrl: String?,
    @SerialName("fcm_token") val fcmToken: String?,
    @SerialName("created_at") val createdAt: String
)

interface BaseItem {
    val id: String
    val name: String
}
@Serializable
data class Group(
    override val id: String,
    override val name: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("is_deleted") val isDeleted: Boolean
): BaseItem

@Serializable
data class Subject(
    override val id: String,
    override val name: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("is_deleted") val isDeleted: Boolean
): BaseItem



//STATS
@Serializable
data class StudentStats(
    val id: String,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    @SerialName("avatar_url") val avatarUrl: String?
)
@Serializable
data class SubjectHist(val id: String,  val name: String, val total: Int, val count: Int)

@Serializable
data class Attendance(
    @SerialName("total_classes")
    val totalClasses: Int,
    @SerialName("active_classes")
    val activeClasses: Int,
)
@Serializable
data class LineChart(
    @SerialName("visit_count")
    val visitCount: Int,
    @Serializable(with = LocalDateTimeSerializer::class)
    val date: LocalDateTime,
)

@Serializable
data class StatisticRequest(
    var id: String,
    @SerialName("subject_id")
    val subjectId: String?,
    @SerialName("end_date")
    @Serializable(with = LocalDateTimeKotlinSerializer::class)
    val endDate: kotlinx.datetime.LocalDateTime?,
    @SerialName("start_date")
    @Serializable(with = LocalDateTimeKotlinSerializer::class)
    val startDate: kotlinx.datetime.LocalDateTime?,
)

@Serializable
data class GroupBar(val title: String, val count: Int)
