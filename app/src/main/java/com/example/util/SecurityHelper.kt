package com.example.util

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

object SecurityHelper {

    private val emailPattern = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return Base64.getEncoder().encodeToString(salt)
    }

    fun hashPassword(password: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(salt.toByteArray(Charsets.UTF_8))
        val digest = md.digest(password.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(digest)
    }

    fun verifyPassword(password: String, salt: String, expectedHash: String): Boolean {
        val calculated = hashPassword(password, salt)
        return calculated == expectedHash
    }

    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && emailPattern.matches(email.trim())
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }
}
