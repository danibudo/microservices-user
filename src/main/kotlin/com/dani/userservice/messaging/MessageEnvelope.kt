package com.dani.userservice.messaging

import java.time.Instant
import java.util.UUID

data class MessageEnvelope<T>(
    val event: String,
    val data: T,
    val metadata: EventMetadata
) {
    companion object {
        fun <T> of(event: String, data: T): MessageEnvelope<T> = MessageEnvelope(
            event = event,
            data = data,
            metadata = EventMetadata(
                timestamp = Instant.now().toString(),
                correlationId = UUID.randomUUID().toString()
            )
        )
    }
}

data class EventMetadata(
    val timestamp: String,
    val correlationId: String
)

data class UserCreatedPayload(val userId: UUID, val email: String, val role: String)
data class UserInviteResentPayload(val userId: UUID, val email: String)
data class UserRoleUpdatedPayload(val userId: UUID, val role: String)
data class UserDeletedPayload(val userId: UUID)