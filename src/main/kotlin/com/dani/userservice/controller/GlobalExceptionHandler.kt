package com.dani.userservice.controller

import com.dani.userservice.dto.ErrorResponse
import com.dani.userservice.exception.EmailAlreadyExistsException
import com.dani.userservice.exception.ForbiddenOperationException
import com.dani.userservice.exception.InvalidOperationException
import com.dani.userservice.exception.UserNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(UserNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFound(ex: UserNotFoundException): ErrorResponse =
        ErrorResponse(HttpStatus.NOT_FOUND, ex.message ?: "Not found")

    @ExceptionHandler(EmailAlreadyExistsException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleConflict(ex: EmailAlreadyExistsException): ErrorResponse =
        ErrorResponse(HttpStatus.CONFLICT, ex.message ?: "Conflict")

    @ExceptionHandler(ForbiddenOperationException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun handleForbidden(ex: ForbiddenOperationException): ErrorResponse =
        ErrorResponse(HttpStatus.FORBIDDEN, ex.message ?: "Forbidden")

    @ExceptionHandler(InvalidOperationException::class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    fun handleInvalidOperation(ex: InvalidOperationException): ErrorResponse =
        ErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.message ?: "Unprocessable entity")

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidation(ex: MethodArgumentNotValidException): ErrorResponse {
        val message = ex.bindingResult.fieldErrors
            .joinToString("; ") { "${it.field}: ${it.defaultMessage}" }
        return ErrorResponse(HttpStatus.BAD_REQUEST, message)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleIllegalArgument(ex: IllegalArgumentException): ErrorResponse =
        ErrorResponse(HttpStatus.BAD_REQUEST, ex.message ?: "Bad request")

    @ExceptionHandler(HttpMessageNotReadableException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleUnreadableMessage(ex: HttpMessageNotReadableException): ErrorResponse =
        ErrorResponse(HttpStatus.BAD_REQUEST, "Malformed request body")

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleGeneric(ex: Exception): ErrorResponse {
        log.error("Unhandled exception", ex)
        return ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred")
    }
}