package com.dani.userservice.dto

import com.dani.userservice.domain.Role

data class UpdateUserRequest(
    val firstName: String? = null,
    val lastName: String? = null,
    val role: Role? = null
)