package com.sample.calorease.util

object ValidationUtils {
    
    /**
     * Validates email format
     * @return true if email contains @ and has at least one character before and after @
     */
    fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return false
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        return email.matches(emailRegex.toRegex())
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
