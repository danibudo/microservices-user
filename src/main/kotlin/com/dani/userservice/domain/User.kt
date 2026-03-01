package com.dani.userservice.domain

import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "users")
class User(
    @Id
    @Column(nullable = false, updatable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false, unique = true, updatable = false)
    val email: String,

    @Column(name = "first_name", nullable = false)
    var firstName: String,

    @Column(name = "last_name", nullable = false)
    var lastName: String,

    @Convert(converter = RoleConverter::class)
    @Column(nullable = false, length = 50)
    var role: Role,

    @Convert(converter = UserStatusConverter::class)
    @Column(nullable = false, length = 20)
    var status: UserStatus = UserStatus.PENDING,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false, updatable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now()
)