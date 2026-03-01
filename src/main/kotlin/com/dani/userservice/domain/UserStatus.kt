package com.dani.userservice.domain

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

enum class UserStatus(@JsonValue val value: String) {
    PENDING("pending"),
    ACTIVE("active"),
    DEACTIVATED("deactivated");

    companion object {
        @JsonCreator
        @JvmStatic
        fun fromValue(value: String): UserStatus =
            entries.firstOrNull { it.value == value }
                ?: throw IllegalArgumentException("Unknown status: $value")
    }
}

@Converter
class UserStatusConverter : AttributeConverter<UserStatus, String> {
    override fun convertToDatabaseColumn(attribute: UserStatus): String = attribute.value
    override fun convertToEntityAttribute(dbData: String): UserStatus = UserStatus.fromValue(dbData)
}