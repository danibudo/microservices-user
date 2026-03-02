package com.dani.userservice.controller

import com.dani.userservice.domain.Role
import com.dani.userservice.domain.UserStatus
import com.dani.userservice.dto.CreateUserRequest
import com.dani.userservice.dto.UpdateUserRequest
import com.dani.userservice.dto.UserResponse
import com.dani.userservice.exception.ForbiddenOperationException
import com.dani.userservice.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/users")
class UserController(private val userService: UserService) {

    @GetMapping
    fun listUsers(
        @RequestHeader("X-User-Id") requesterId: String,
        @RequestHeader("X-User-Role") requesterRole: String,
        @RequestParam(required = false) role: String?,
        @RequestParam(required = false) status: String?
    ): List<UserResponse> {
        val callerRole = Role.fromValue(requesterRole)
        requireRole(callerRole, Role.LIBRARIAN, Role.ACCESS_ADMIN, Role.SUPER_ADMIN)

        return userService.listUsers(
            role = role?.let { Role.fromValue(it) },
            status = status?.let { UserStatus.fromValue(it) }
        ).map { UserResponse.from(it) }
    }

    @GetMapping("/{id}")
    fun getUser(
        @PathVariable id: UUID,
        @RequestHeader("X-User-Id") requesterId: String,
        @RequestHeader("X-User-Role") requesterRole: String
    ): UserResponse {
        val callerId = UUID.fromString(requesterId)
        val callerRole = Role.fromValue(requesterRole)
        requireRoleOrSelf(callerId, id, callerRole, Role.ACCESS_ADMIN, Role.SUPER_ADMIN)

        return UserResponse.from(userService.getUser(id))
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createUser(
        @RequestBody @Valid request: CreateUserRequest,
        @RequestHeader("X-User-Id") requesterId: String,
        @RequestHeader("X-User-Role") requesterRole: String
    ): UserResponse {
        val callerRole = Role.fromValue(requesterRole)
        requireRole(callerRole, Role.ACCESS_ADMIN, Role.SUPER_ADMIN)

        return UserResponse.from(
            userService.createUser(
                email = request.email,
                firstName = request.firstName,
                lastName = request.lastName,
                role = request.role,
                requesterRole = callerRole
            )
        )
    }

    @PatchMapping("/{id}")
    fun updateUser(
        @PathVariable id: UUID,
        @RequestBody @Valid request: UpdateUserRequest,
        @RequestHeader("X-User-Id") requesterId: String,
        @RequestHeader("X-User-Role") requesterRole: String
    ): UserResponse {
        val callerId = UUID.fromString(requesterId)
        val callerRole = Role.fromValue(requesterRole)
        requireRole(callerRole, Role.ACCESS_ADMIN, Role.SUPER_ADMIN)

        return UserResponse.from(
            userService.updateUser(
                id = id,
                firstName = request.firstName,
                lastName = request.lastName,
                newRole = request.role,
                requesterId = callerId,
                requesterRole = callerRole
            )
        )
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteUser(
        @PathVariable id: UUID,
        @RequestHeader("X-User-Id") requesterId: String,
        @RequestHeader("X-User-Role") requesterRole: String
    ) {
        val callerId = UUID.fromString(requesterId)
        val callerRole = Role.fromValue(requesterRole)
        requireRole(callerRole, Role.ACCESS_ADMIN, Role.SUPER_ADMIN)

        userService.deleteUser(id, callerId, callerRole)
    }

    @PostMapping("/{id}/resend-invite")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun resendInvite(
        @PathVariable id: UUID,
        @RequestHeader("X-User-Id") requesterId: String,
        @RequestHeader("X-User-Role") requesterRole: String
    ) {
        val callerRole = Role.fromValue(requesterRole)
        requireRole(callerRole, Role.ACCESS_ADMIN, Role.SUPER_ADMIN)

        userService.resendInvite(id)
    }

    private fun requireRole(callerRole: Role, vararg allowed: Role) {
        if (callerRole !in allowed) throw ForbiddenOperationException("Insufficient permissions")
    }

    private fun requireRoleOrSelf(callerId: UUID, targetId: UUID, callerRole: Role, vararg allowed: Role) {
        if (callerId != targetId && callerRole !in allowed) throw ForbiddenOperationException("Insufficient permissions")
    }
}