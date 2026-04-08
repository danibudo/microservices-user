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
        caller: CallerContext,
        @RequestParam(required = false) role: String?,
        @RequestParam(required = false) status: String?
    ): List<UserResponse> {
        requireRole(caller.role, Role.LIBRARIAN, Role.ACCESS_ADMIN, Role.SUPER_ADMIN)

        return userService.listUsers(
            role = role?.let { Role.fromValue(it) },
            status = status?.let { UserStatus.fromValue(it) }
        ).map { UserResponse.from(it) }
    }

    @GetMapping("/{id}")
    fun getUser(
        @PathVariable id: UUID,
        caller: CallerContext
    ): UserResponse {
        requireRoleOrSelf(caller, id, Role.ACCESS_ADMIN, Role.SUPER_ADMIN)

        return UserResponse.from(userService.getUser(id))
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createUser(
        @RequestBody @Valid request: CreateUserRequest,
        caller: CallerContext
    ): UserResponse {
        requireRole(caller.role, Role.ACCESS_ADMIN, Role.SUPER_ADMIN)

        return UserResponse.from(
            userService.createUser(
                email = request.email,
                firstName = request.firstName,
                lastName = request.lastName,
                role = request.role,
                requesterRole = caller.role
            )
        )
    }

    @PatchMapping("/{id}")
    fun updateUser(
        @PathVariable id: UUID,
        @RequestBody @Valid request: UpdateUserRequest,
        caller: CallerContext
    ): UserResponse {
        requireRole(caller.role, Role.ACCESS_ADMIN, Role.SUPER_ADMIN)

        return UserResponse.from(
            userService.updateUser(
                id = id,
                firstName = request.firstName,
                lastName = request.lastName,
                newRole = request.role,
                requesterId = caller.id,
                requesterRole = caller.role
            )
        )
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteUser(
        @PathVariable id: UUID,
        caller: CallerContext
    ) {
        requireRole(caller.role, Role.ACCESS_ADMIN, Role.SUPER_ADMIN)
        userService.deleteUser(id, caller.id, caller.role)
    }

    @PostMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deactivateUser(
        @PathVariable id: UUID,
        caller: CallerContext
    ) {
        requireRole(caller.role, Role.ACCESS_ADMIN, Role.SUPER_ADMIN)
        userService.deactivateUser(id, caller.id, caller.role)
    }

    @PostMapping("/{id}/resend-invite")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun resendInvite(
        @PathVariable id: UUID,
        caller: CallerContext
    ) {
        requireRole(caller.role, Role.ACCESS_ADMIN, Role.SUPER_ADMIN)
        userService.resendInvite(id)
    }

    private fun requireRole(callerRole: Role, vararg allowed: Role) {
        if (callerRole !in allowed) throw ForbiddenOperationException("Insufficient permissions")
    }

    private fun requireRoleOrSelf(caller: CallerContext, targetId: UUID, vararg allowed: Role) {
        if (caller.id != targetId && caller.role !in allowed) throw ForbiddenOperationException("Insufficient permissions")
    }
}