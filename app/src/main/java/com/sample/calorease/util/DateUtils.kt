package com.sample.calorease.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

object DateUtils {
    
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    
    /**
     * Get today's date as String in format "yyyy-MM-dd"
     */
    fun getTodayString(): String {
        return LocalDate.now().format(dateFormatter)
    }
    
    /**
     * Get day of week from date string (e.g., "Mon", "Tue")
     */
    fun getDayOfWeek(dateString: String): String {
        return try {
            val date = LocalDate.parse(dateString, dateFormatter)
            date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        } catch (e: Exception) {
            ""
        }
    }
    
    /**
     * Get date n days ago as String
     */
    fun getDaysAgo(days: Long): String {
        return LocalDate.now().minusDays(days).format(dateFormatter)
    }
    
    /**
     * Get last n days as list of date strings
     */
    fun getLastNDays(n: Int): List<String> {
        return (n - 1 downTo 0).map { getDaysAgo(it.toLong()) }
    }
    
    /**
     * Format date string to readable format (e.g., "Jan 15, 2024")
     */
    fun formatReadable(dateString: String): String {
        return try {
            val date = LocalDate.parse(dateString, dateFormatter)
            val readableFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
            date.format(readableFormatter)
        } catch (e: Exception) {
            dateString
        }
    }
}
