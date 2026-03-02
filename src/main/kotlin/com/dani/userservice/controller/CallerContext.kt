package com.dani.userservice.controller

import com.dani.userservice.domain.Role
import java.util.UUID

data class CallerContext(
    val id: UUID,
    val role: Role
)