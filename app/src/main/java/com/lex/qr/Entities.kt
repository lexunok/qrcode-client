package com.lex.qr

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class Role {
    STAFF, STUDENT
}

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
    @SerialName("class_id") val classId: String,
    @SerialName("student_id") val studentId: String,
    @SerialName("student_geolocation") val studentGeolocation: String
)

@Serializable
data class JoinClassResponse(
    @SerialName("is_success") val isSuccess: Boolean
)

@Serializable
data class Student(
    @SerialName("student_id") val studentId: String,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    @SerialName("is_active") val isActive: Boolean,
    @SerialName("public_id") val publicId: String
)
@Serializable
data class User(
    val id: String,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    val role: Role,
    @SerialName("group_id") val groupId: String?,
    @SerialName("created_at") val createdAt: String
)
@Serializable
data class Group(
    val id: String,
    val name: String,
    @SerialName("created_at") val createdAt: String
)
@Serializable
data class Subject(
    val id: String,
    val name: String,
    @SerialName("created_at") val createdAt: String
)