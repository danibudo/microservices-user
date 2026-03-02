package com.dani.userservice.repository

import com.dani.userservice.domain.Role
import com.dani.userservice.domain.User
import com.dani.userservice.domain.UserStatus
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserRepository : JpaRepository<User, UUID> {
    fun findByRole(role: Role): List<User>
    fun findByStatus(status: UserStatus): List<User>
    fun findByRoleAndStatus(role: Role, status: UserStatus): List<User>
    fun existsByEmail(email: String): Boolean
}