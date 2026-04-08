package com.dani.userservice.service

import com.dani.userservice.domain.Role
import com.dani.userservice.domain.User
import com.dani.userservice.domain.UserStatus
import com.dani.userservice.exception.EmailAlreadyExistsException
import com.dani.userservice.exception.ForbiddenOperationException
import com.dani.userservice.exception.InvalidOperationException
import com.dani.userservice.exception.UserNotFoundException
import com.dani.userservice.messaging.UserEventPublisher
import com.dani.userservice.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val eventPublisher: UserEventPublisher
) {

    private val log = LoggerFactory.getLogger(UserService::class.java)

    @Transactional(readOnly = true)
    fun listUsers(role: Role?, status: UserStatus?): List<User> {
        log.debug("Listing users role={} status={}", role?.value, status?.value)
        return when {
            role != null && status != null -> userRepository.findByRoleAndStatus(role, status)
            role != null -> userRepository.findByRole(role)
            status != null -> userRepository.findByStatus(status)
            else -> userRepository.findAll()
        }
    }

    @Transactional(readOnly = true)
    fun getUser(id: UUID): User {
        log.debug("Fetching user id={}", id)
        return userRepository.findById(id).orElseThrow { UserNotFoundException(id) }
    }

    fun createUser(email: String, firstName: String, lastName: String, role: Role, requesterRole: Role): User {
        log.info("Creating user email={} role={}", email, role.value)
        checkCanManage(requesterRole, role)
        if (userRepository.existsByEmail(email)) throw EmailAlreadyExistsException(email)

        val user = User(email = email, firstName = firstName, lastName = lastName, role = role)
        val saved = userRepository.save(user)
        eventPublisher.publishUserCreated(saved)
        return saved
    }

    fun updateUser(id: UUID, firstName: String?, lastName: String?, newRole: Role?, requesterId: UUID, requesterRole: Role): User {
        log.info("Updating user id={}", id)
        val user = userRepository.findById(id).orElseThrow { UserNotFoundException(id) }

        if (newRole != null) checkNotSelf(requesterId, id, "change the role of")
        checkCanManage(requesterRole, user.role)
        if (newRole != null) checkCanManage(requesterRole, newRole)

        firstName?.let { user.firstName = it }
        lastName?.let { user.lastName = it }
        newRole?.let { user.role = it }

        val saved = userRepository.save(user)
        if (newRole != null) eventPublisher.publishUserRoleUpdated(saved)
        return saved
    }

    fun deleteUser(id: UUID, requesterId: UUID, requesterRole: Role) {
        log.info("Deleting user id={}", id)
        val user = userRepository.findById(id).orElseThrow { UserNotFoundException(id) }

        checkNotSelf(requesterId, id, "delete")
        checkCanManage(requesterRole, user.role)

        userRepository.delete(user)
        eventPublisher.publishUserDeleted(user.id)
    }

    fun deactivateUser(id: UUID, requesterId: UUID, requesterRole: Role) {
        log.info("Deactivating user id={}", id)
        val user = userRepository.findById(id).orElseThrow { UserNotFoundException(id) }

        checkNotSelf(requesterId, id, "deactivate")
        checkCanManage(requesterRole, user.role)

        if (user.status == UserStatus.DEACTIVATED) {
            throw InvalidOperationException("User is already deactivated")
        }

        user.status = UserStatus.DEACTIVATED
        userRepository.save(user)
    }

    fun resendInvite(id: UUID) {
        log.info("Resending invite for user id={}", id)
        val user = userRepository.findById(id).orElseThrow { UserNotFoundException(id) }

        if (user.status != UserStatus.PENDING) {
            throw InvalidOperationException("Invite can only be resent for pending users")
        }
        eventPublisher.publishUserInviteResent(user)
    }

    fun confirmInviteSent(userId: UUID) {
        log.info("Invite confirmed by auth-service for user id={}", userId)
        if (!userRepository.existsById(userId)) throw UserNotFoundException(userId)
    }

    fun updateStatus(userId: UUID, newStatus: UserStatus) {
        log.info("Updating status for user id={} to status={}", userId, newStatus.value)
        val user = userRepository.findById(userId).orElseThrow { UserNotFoundException(userId) }
        user.status = newStatus
        userRepository.save(user)
        log.info("User status updated id={} status={}", userId, newStatus.value)
    }

    private fun checkCanManage(requesterRole: Role, targetRole: Role) {
        when (requesterRole) {
            Role.SUPER_ADMIN -> return
            Role.ACCESS_ADMIN -> if (targetRole == Role.SUPER_ADMIN) {
                throw ForbiddenOperationException("access-admin cannot manage super-admin accounts")
            }
            else -> throw ForbiddenOperationException("Insufficient permissions to manage users")
        }
    }

    private fun checkNotSelf(requesterId: UUID, targetId: UUID, action: String) {
        if (requesterId == targetId) {
            throw ForbiddenOperationException("Users cannot $action their own account")
        }
    }
}