package com.sample.calorease.util

object ValidationUtils {

    /**
     * Stricter email format validation for Sign-Up.
     * Requires:
     *  - Valid local part: letters, digits, +, _, ., - (no consecutive dots, no leading/trailing dot)
     *  - @ separator
     *  - Domain with at least one dot and a 2-6 char TLD (rejects .c, .123, etc.)
     */
    fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return false
        // Stricter regex: local part cannot start/end with a dot, no consecutive dots
        val emailRegex = Regex(
            "^[A-Za-z0-9+_-]+(\\.[A-Za-z0-9+_-]+)*" +  // local part
            "@" +
            "[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*" +        // domain
            "\\.[A-Za-z]{2,6}$"                          // TLD 2-6 chars
        )
        return emailRegex.matches(email)
    }

    /**
     * Rejects emails not conforming to a whitelist of major providers.
     * Always allows internal @calorease.com users (Test/Admin).
     */
    fun isAcceptedEmailProvider(email: String): Boolean {
        if (!isValidEmail(email)) return false
        val domain = email.substringAfterLast('@').lowercase()
        return domain == "calorease.com" || domain in listOf(
            "gmail.com", "yahoo.com", "outlook.com", "hotmail.com", "microsoft.com"
        )
    }

    /**
     * Validates password length (minimum 6 characters for login, 8 for signup)
     */
    fun isValidPassword(password: String, minLength: Int = 6): Boolean {
        return password.length >= minLength
    }
    
    /**
     * Checks if password and confirm password match
     */
    fun passwordsMatch(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword && password.isNotEmpty()
    }
    
    /**
     * Validates a numeric input is positive
     */
    fun isPositiveNumber(value: String): Boolean {
        return value.toDoubleOrNull()?.let { it > 0 } ?: false
    }
    
    /**
     * Validates name is not empty
     */
    fun isValidName(name: String): Boolean {
        return name.trim().isNotEmpty()
    }
    
    // Validation methods that return error messages for ViewModels
    
    fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "Email cannot be empty"
            !isValidEmail(email) -> "Invalid email format"
            !isAcceptedEmailProvider(email) -> "Please use a valid email provider (Gmail, Yahoo, Microsoft)"
            else -> null
        }
    }
    
    fun validatePassword(password: String, minLength: Int = 6): String? {
        return when {
            password.isBlank() -> "Password cannot be empty"
            password.length < minLength -> "Password must be at least $minLength characters"
            else -> null
        }
    }
    
    fun validateConfirmPassword(password: String, confirmPassword: String): String? {
        return when {
            confirmPassword.isBlank() -> "Please confirm your password"
            !passwordsMatch(password, confirmPassword) -> "Passwords do not match"
            else -> null
        }
    }
    
    fun validateName(name: String): String? {
        return when {
            name.trim().isEmpty() -> "Name cannot be empty"
            else -> null
        }
    }
}
