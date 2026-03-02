package com.dani.userservice.dto

import com.dani.userservice.domain.Role
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateUserRequest(
    @field:NotBlank @field:Email val email: String,
    @field:NotBlank val firstName: String,
    @field:NotBlank val lastName: String,
    @field:NotNull val role: Role
)