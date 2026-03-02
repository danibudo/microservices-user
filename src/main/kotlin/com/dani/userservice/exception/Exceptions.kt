package com.dani.userservice.exception

import java.util.UUID

class UserNotFoundException(id: UUID) : RuntimeException("User not found: $id")
class EmailAlreadyExistsException(email: String) : RuntimeException("Email already in use: $email")
class ForbiddenOperationException(message: String) : RuntimeException(message)
class InvalidOperationException(message: String) : RuntimeException(message)