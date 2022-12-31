package com.kaiqkt.services.authregistryservice.resources.communication.entities

enum class EmailType(val title: String) {
    WELCOME_TEMPLATE("Welcome to us site"),
    NEW_ACCESS("New device logged"),
    PASSWORD_RESET_REQUEST("Password reset request"),
    PASSWORD_UPDATED("Your password has been updated")
}