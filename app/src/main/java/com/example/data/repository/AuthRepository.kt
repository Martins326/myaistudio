package com.example.data.repository

import com.example.data.local.UserAccountDao
import com.example.data.model.UserAccountEntity
import com.example.util.SecurityHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class AuthRepository(private val userDao: UserAccountDao) {

    private val _currentUser = MutableStateFlow<UserAccountEntity?>(null)
    val currentUser: StateFlow<UserAccountEntity?> = _currentUser.asStateFlow()

    init {
        // Create default Administrator and Guest user if database is new
        // and auto-login initial session
    }

    suspend fun register(
        username: String,
        email: String,
        password: String
    ): Result<UserAccountEntity> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        val cleanUsername = username.trim()

        if (cleanUsername.length < 3) {
            return@withContext Result.failure(IllegalArgumentException("Username must be at least 3 characters."))
        }
        if (!SecurityHelper.isValidEmail(cleanEmail)) {
            return@withContext Result.failure(IllegalArgumentException("Please enter a valid email address."))
        }
        if (!SecurityHelper.isValidPassword(password)) {
            return@withContext Result.failure(IllegalArgumentException("Password must be at least 6 characters."))
        }

        val existingEmail = userDao.getUserByEmail(cleanEmail)
        if (existingEmail != null) {
            return@withContext Result.failure(IllegalArgumentException("An account with this email already exists."))
        }

        val existingName = userDao.getUserByUsername(cleanUsername)
        if (existingName != null) {
            return@withContext Result.failure(IllegalArgumentException("Username is already taken."))
        }

        val salt = SecurityHelper.generateSalt()
        val hash = SecurityHelper.hashPassword(password, salt)

        // Give the very first user the admin role, otherwise standard user
        val isFirst = userDao.getUserByEmail("admin@nexa.ai") == null
        val role = if (cleanEmail == "admin@nexa.ai" || isFirst) "admin" else "user"

        val newUser = UserAccountEntity(
            username = cleanUsername,
            email = cleanEmail,
            passwordHash = hash,
            salt = salt,
            role = role,
            avatarColor = 0xFF8B5CF6
        )

        try {
            val newId = userDao.insertUser(newUser)
            val created = newUser.copy(id = newId)
            _currentUser.value = created
            Result.success(created)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(
        email: String,
        password: String
    ): Result<UserAccountEntity> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        val user = userDao.getUserByEmail(cleanEmail)
            ?: return@withContext Result.failure(IllegalArgumentException("Invalid email or password."))

        val isMatch = SecurityHelper.verifyPassword(password, user.salt, user.passwordHash)
        if (!isMatch) {
            return@withContext Result.failure(IllegalArgumentException("Invalid email or password."))
        }

        userDao.updateLastLogin(user.id)
        val refreshed = user.copy(lastLoginAt = System.currentTimeMillis())
        _currentUser.value = refreshed
        Result.success(refreshed)
    }

    fun logout() {
        _currentUser.value = null
    }

    suspend fun deleteCurrentAccount(): Result<Unit> = withContext(Dispatchers.IO) {
        val user = _currentUser.value ?: return@withContext Result.failure(IllegalStateException("Not logged in"))
        userDao.deleteUser(user.id)
        _currentUser.value = null
        Result.success(Unit)
    }

    suspend fun autoLoginDefaultUser() = withContext(Dispatchers.IO) {
        if (_currentUser.value == null) {
            val admin = userDao.getUserByEmail("admin@nexa.ai")
            if (admin != null) {
                _currentUser.value = admin
            } else {
                // Initialize default Admin profile
                val salt = SecurityHelper.generateSalt()
                val hash = SecurityHelper.hashPassword("nexaAdmin2026!", salt)
                val newAdmin = UserAccountEntity(
                    username = "NexaAdmin",
                    email = "admin@nexa.ai",
                    passwordHash = hash,
                    salt = salt,
                    role = "admin",
                    avatarColor = 0xFF8B5CF6
                )
                val id = userDao.insertUser(newAdmin)
                _currentUser.value = newAdmin.copy(id = id)
            }
        }
    }
}
