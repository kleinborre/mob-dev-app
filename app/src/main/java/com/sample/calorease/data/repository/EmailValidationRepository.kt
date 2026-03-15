package com.sample.calorease.data.repository

import com.sample.calorease.data.remote.api.AbstractEmailApi
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sprint 4 Phase 4: Abstracts network queries parsing the deliverability metrics
 * retrieved directly from the remote API into secure UI-level strings or successes.
 */
@Singleton
class EmailValidationRepository @Inject constructor(
    private val api: AbstractEmailApi
) {

    /**
     * Executes the API call and returns a localized Error Message if there is a domain failure,
     * or Null if the Email is perfectly valid.
     */
    suspend fun validateEmailLive(email: String): Result<String?> {
        return try {
            val response = api.validateEmail(apiKey = AbstractEmailApi.API_KEY, email = email)
            
            if (response.isSuccessful) {
                val body = response.body()
                when {
                    body == null -> 
                        Result.success("Unable to verify email structure at this time")
                        
                    body.isValidFormat?.value == false -> 
                        Result.success("Invalid email format")
                        
                    body.isDisposableEmail?.value == true -> 
                        Result.success("Disposable email addresses are not permitted")
                        
                    body.deliverability == "UNDELIVERABLE" -> 
                        Result.success("This email address cannot receive mail")
                        
                    // If everything passes cleanly, return null error indicator
                    else -> Result.success(null) 
                }
            } else {
                // Return success(null) to fallback and permit registration if API simply crashes (rate limits etc)
                Result.success(null)
            }
        } catch (e: Exception) {
            // Failsafe: if there are network crashes, don't block the user outright, fallback to Android regex format matching later.
            Result.success(null)
        }
    }
}
