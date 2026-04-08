package com.dani.userservice.config

import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.DirectExchange
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.QueueBuilder
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.databind.json.JsonMapper

@Configuration
class RabbitMQConfig {

    companion object {
        const val EXCHANGE = "user-service.events"
        const val AUTH_EXCHANGE = "auth-service.events"
        const val DLX = "dlx.user-service"

        const val QUEUE_USER_CREATED = "user-service.user.created"
        const val QUEUE_USER_INVITE_RESENT = "user-service.user.invite_resent"
        const val QUEUE_USER_ROLE_UPDATED = "user-service.user.role_updated"
        const val QUEUE_USER_DELETED = "user-service.user.deleted"
        const val QUEUE_AUTH_INVITE_TOKEN_GENERATED = "user-service.auth.invite_token_generated"

        const val ROUTING_KEY_USER_CREATED = "user.created"
        const val ROUTING_KEY_USER_INVITE_RESENT = "user.invite_resent"
        const val ROUTING_KEY_USER_ROLE_UPDATED = "user.role_updated"
        const val ROUTING_KEY_USER_DELETED = "user.deleted"
        const val ROUTING_KEY_AUTH_INVITE_TOKEN_GENERATED = "auth.invite_token_generated"
    }

    // --- Exchanges ---

    @Bean fun userEventsExchange(): TopicExchange = TopicExchange(EXCHANGE, true, false)
    @Bean fun authEventsExchange(): TopicExchange = TopicExchange(AUTH_EXCHANGE, true, false)
    @Bean fun deadLetterExchange(): DirectExchange = DirectExchange(DLX, true, false)

    // --- Queues ---

    @Bean fun userCreatedQueue(): Queue = buildQueue(QUEUE_USER_CREATED)
    @Bean fun userInviteResentQueue(): Queue = buildQueue(QUEUE_USER_INVITE_RESENT)
    @Bean fun userRoleUpdatedQueue(): Queue = buildQueue(QUEUE_USER_ROLE_UPDATED)
    @Bean fun userDeletedQueue(): Queue = buildQueue(QUEUE_USER_DELETED)
    @Bean fun authInviteTokenGeneratedQueue(): Queue = buildQueue(QUEUE_AUTH_INVITE_TOKEN_GENERATED)

    // --- Dead-letter queues ---

    @Bean fun userCreatedDlq(): Queue = QueueBuilder.durable("$QUEUE_USER_CREATED.dlq").build()
    @Bean fun userInviteResentDlq(): Queue = QueueBuilder.durable("$QUEUE_USER_INVITE_RESENT.dlq").build()
    @Bean fun userRoleUpdatedDlq(): Queue = QueueBuilder.durable("$QUEUE_USER_ROLE_UPDATED.dlq").build()
    @Bean fun userDeletedDlq(): Queue = QueueBuilder.durable("$QUEUE_USER_DELETED.dlq").build()
    @Bean fun authInviteTokenGeneratedDlq(): Queue = QueueBuilder.durable("$QUEUE_AUTH_INVITE_TOKEN_GENERATED.dlq").build()

    // --- Bindings: main queues → topic exchange ---

    @Bean fun userCreatedBinding() = BindingBuilder.bind(userCreatedQueue()).to(userEventsExchange()).with(ROUTING_KEY_USER_CREATED)
    @Bean fun userInviteResentBinding() = BindingBuilder.bind(userInviteResentQueue()).to(userEventsExchange()).with(ROUTING_KEY_USER_INVITE_RESENT)
    @Bean fun userRoleUpdatedBinding() = BindingBuilder.bind(userRoleUpdatedQueue()).to(userEventsExchange()).with(ROUTING_KEY_USER_ROLE_UPDATED)
    @Bean fun userDeletedBinding() = BindingBuilder.bind(userDeletedQueue()).to(userEventsExchange()).with(ROUTING_KEY_USER_DELETED)
    @Bean fun authInviteTokenGeneratedBinding() = BindingBuilder.bind(authInviteTokenGeneratedQueue()).to(authEventsExchange()).with(ROUTING_KEY_AUTH_INVITE_TOKEN_GENERATED)

    // --- Bindings: DLQs → DLX ---

    @Bean fun userCreatedDlqBinding() = BindingBuilder.bind(userCreatedDlq()).to(deadLetterExchange()).with(QUEUE_USER_CREATED)
    @Bean fun userInviteResentDlqBinding() = BindingBuilder.bind(userInviteResentDlq()).to(deadLetterExchange()).with(QUEUE_USER_INVITE_RESENT)
    @Bean fun userRoleUpdatedDlqBinding() = BindingBuilder.bind(userRoleUpdatedDlq()).to(deadLetterExchange()).with(QUEUE_USER_ROLE_UPDATED)
    @Bean fun userDeletedDlqBinding() = BindingBuilder.bind(userDeletedDlq()).to(deadLetterExchange()).with(QUEUE_USER_DELETED)
    @Bean fun authInviteTokenGeneratedDlqBinding() = BindingBuilder.bind(authInviteTokenGeneratedDlq()).to(deadLetterExchange()).with(QUEUE_AUTH_INVITE_TOKEN_GENERATED)

    // --- Message converter ---

    @Bean
    fun messageConverter(jsonMapper: JsonMapper): MessageConverter =
        JacksonJsonMessageConverter(jsonMapper)

    // --- Helpers ---

    private fun buildQueue(name: String): Queue = QueueBuilder.durable(name)
        .deadLetterExchange(DLX)
        .deadLetterRoutingKey(name)
        .build()
}