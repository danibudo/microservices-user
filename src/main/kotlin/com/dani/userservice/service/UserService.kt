package com.dani.userservice.service

import com.dani.userservice.domain.Role
import com.dani.userservice.domain.User
import com.dani.userservice.domain.UserStatus
import com.dani.userservice.exception.EmailAlreadyExistsException
import com.dani.userservice.exception.ForbiddenOperationException
import com.dani.userservice.exception.InvalidOperationException
import com.dani.userservice.exception.UserNotFoundException
import com.dani.userservice.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class UserService(private val userRepository: UserRepository) {

    @Transactional(readOnly = true)
    fun listUsers(role: Role?, status: UserStatus?): List<User> = when {
        role != null && status != null -> userRepository.findByRoleAndStatus(role, status)
        role != null -> userRepository.findByRole(role)
        status != null -> userRepository.findByStatus(status)
        else -> userRepository.findAll()
    }

    @Transactional(readOnly = true)
    fun getUser(id: UUID): User =
        userRepository.findById(id).orElseThrow { UserNotFoundException(id) }

    fun createUser(email: String, firstName: String, lastName: String, role: Role, requesterRole: Role): User {
        checkCanManage(requesterRole, role)
        if (userRepository.existsByEmail(email)) throw EmailAlreadyExistsException(email)

        val user = User(email = email, firstName = firstName, lastName = lastName, role = role)
        return userRepository.save(user)
        // TODO publish user.created event
    }

    fun updateUser(id: UUID, firstName: String?, lastName: String?, newRole: Role?, requesterId: UUID, requesterRole: Role): User {
        val user = userRepository.findById(id).orElseThrow { UserNotFoundException(id) }

        if (newRole != null) checkNotSelf(requesterId, id, "change the role of")
        checkCanManage(requesterRole, user.role)
        if (newRole != null) checkCanManage(requesterRole, newRole)

        firstName?.let { user.firstName = it }
        lastName?.let { user.lastName = it }
        newRole?.let { user.role = it }

        return userRepository.save(user)
        // TODO publish user.role_updated event if role changed
    }

    fun deleteUser(id: UUID, requesterId: UUID, requesterRole: Role) {
        val user = userRepository.findById(id).orElseThrow { UserNotFoundException(id) }

        checkNotSelf(requesterId, id, "delete")
        checkCanManage(requesterRole, user.role)

        userRepository.delete(user)
        // TODO publish user.deleted event
    }

    fun resendInvite(id: UUID) {
        val user = userRepository.findById(id).orElseThrow { UserNotFoundException(id) }

        if (user.status != UserStatus.PENDING) {
            throw InvalidOperationException("Invite can only be resent for pending users")
        }
        // TODO publish user.invite_resent event
    }

    fun updateStatus(userId: UUID, newStatus: UserStatus) {
        val user = userRepository.findById(userId).orElseThrow { UserNotFoundException(userId) }
        user.status = newStatus
        userRepository.save(user)
    }

    private fun checkCanManage(requesterRole: Role, targetRole: Role) {
        if (requesterRole == Role.ACCESS_ADMIN && targetRole == Role.SUPER_ADMIN) {
            throw ForbiddenOperationException("access-admin cannot manage super-admin accounts")
        }
    }

    private fun checkNotSelf(requesterId: UUID, targetId: UUID, action: String) {
        if (requesterId == targetId) {
            throw ForbiddenOperationException("Users cannot $action their own account")
        }
    }
}