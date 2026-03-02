package com.dani.userservice.controller

import com.dani.userservice.domain.Role
import org.springframework.core.MethodParameter
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import java.util.UUID

class CallerContextArgumentResolver : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean =
        parameter.parameterType == CallerContext::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): CallerContext {
        val userId = webRequest.getHeader("X-User-Id")
            ?: throw IllegalArgumentException("Missing required header: X-User-Id")
        val userRole = webRequest.getHeader("X-User-Role")
            ?: throw IllegalArgumentException("Missing required header: X-User-Role")

        return CallerContext(
            id = UUID.fromString(userId),
            role = Role.fromValue(userRole)
        )
    }
}