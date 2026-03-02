package com.dani.userservice.dto

import com.dani.userservice.domain.Role
import com.dani.userservice.domain.User
import com.dani.userservice.domain.UserStatus
import java.time.OffsetDateTime
import java.util.UUID

data class UserResponse(
    val id: UUID,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: Role,
    val status: UserStatus,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
) {
    companion object {
        fun from(user: User) = UserResponse(
            id = user.id,
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName,
            role = user.role,
            status = user.status,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt
        )
    }
}