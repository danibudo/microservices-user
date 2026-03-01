package com.dani.userservice.domain

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

enum class Role(@JsonValue val value: String) {
    MEMBER("member"),
    LIBRARIAN("librarian"),
    ACCESS_ADMIN("access-admin"),
    SUPER_ADMIN("super-admin");

    companion object {
        @JsonCreator
        @JvmStatic
        fun fromValue(value: String): Role =
            entries.firstOrNull { it.value == value }
                ?: throw IllegalArgumentException("Unknown role: $value")
    }
}

@Converter
class RoleConverter : AttributeConverter<Role, String> {
    override fun convertToDatabaseColumn(attribute: Role): String = attribute.value
    override fun convertToEntityAttribute(dbData: String): Role = Role.fromValue(dbData)
}