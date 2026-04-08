package com.dani.userservice.messaging

import com.dani.userservice.config.RabbitMQConfig
import com.dani.userservice.service.UserService
import com.rabbitmq.client.Channel
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.support.AmqpHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Component

@Component
class AuthEventConsumer(private val userService: UserService) {

    private val log = LoggerFactory.getLogger(AuthEventConsumer::class.java)

    @RabbitListener(queues = [RabbitMQConfig.QUEUE_AUTH_INVITE_TOKEN_GENERATED])
    fun onInviteTokenGenerated(
        envelope: MessageEnvelope<AuthInviteTokenGeneratedPayload>,
        channel: Channel,
        @Header(AmqpHeaders.DELIVERY_TAG) deliveryTag: Long
    ) {
        val payload = envelope.data
        log.info("Received auth.invite_token_generated userId={} correlationId={}", payload.userId, envelope.metadata.correlationId)
        try {
            userService.confirmInviteSent(payload.userId)
            channel.basicAck(deliveryTag, false)
        } catch (e: Exception) {
            log.error("Failed to process auth.invite_token_generated userId={} error={}", payload.userId, e.message, e)
            channel.basicNack(deliveryTag, false, false)
        }
    }
}