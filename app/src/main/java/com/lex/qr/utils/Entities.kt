package com.lex.qr.utils

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

enum class Role {
    ADMIN, STAFF, STUDENT
}

@Serializable
data class LoginRequest(
    var password: String,
    var email: String
)

@Serializable
data class RatingRequest(
    var id: String,
    var rating: Int
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
data class CreateClassRequest(
    @SerialName("staff_id") val staffId: String,
    @SerialName("subject_id")val subjectId: String,
    @SerialName("group_id") val groupId: String,
    val geolocation: String
)


@Serializable
data class CreateClassResponse(
    @SerialName("public_id") val publicId: String,
    @SerialName("subject_id") val subjectId: String,
    @SerialName("group_id") val groupId: String
)

@Serializable
data class JoinClassRequest(
    @SerialName("public_id") val publicId: String,
    @SerialName("student_id") val studentId: String,
    @SerialName("student_geolocation") val studentGeolocation: String
)

@Serializable
data class JoinClassResponse(
    val id: String?
)
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
    @SerialName("staff_id") val staffId: String,
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
    @SerialName("created_at") val createdAt: String
)
@Serializable
data class Group(
    val id: String,
    val name: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("is_deleted") val isDeleted: Boolean
)
@Serializable
data class Subject(
    val id: String,
    val name: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("is_deleted") val isDeleted: Boolean
)