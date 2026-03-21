package com.dani.userservice.messaging

import com.dani.userservice.config.RabbitMQConfig
import com.dani.userservice.domain.User
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class UserEventPublisher(private val rabbitTemplate: RabbitTemplate) {

    private val log = LoggerFactory.getLogger(UserEventPublisher::class.java)

    fun publishUserCreated(user: User) {
        val payload = UserCreatedPayload(userId = user.id, email = user.email, role = user.role.value)
        publish(RabbitMQConfig.ROUTING_KEY_USER_CREATED, payload)
    }

    fun publishUserInviteResent(user: User) {
        val payload = UserInviteResentPayload(userId = user.id, email = user.email)
        publish(RabbitMQConfig.ROUTING_KEY_USER_INVITE_RESENT, payload)
    }

    fun publishUserRoleUpdated(user: User) {
        val payload = UserRoleUpdatedPayload(userId = user.id, role = user.role.value)
        publish(RabbitMQConfig.ROUTING_KEY_USER_ROLE_UPDATED, payload)
    }

    fun publishUserDeleted(userId: UUID) {
        val payload = UserDeletedPayload(userId = userId)
        publish(RabbitMQConfig.ROUTING_KEY_USER_DELETED, payload)
    }

    private fun <T> publish(routingKey: String, data: T) {
        val envelope = MessageEnvelope.of(routingKey, data)
        log.info("Publishing event routingKey={} correlationId={}", routingKey, envelope.metadata.correlationId)
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, routingKey, envelope)
    }
}