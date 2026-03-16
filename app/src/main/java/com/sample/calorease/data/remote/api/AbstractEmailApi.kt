package com.sample.calorease.data.remote.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Sprint 4 Phase 4: Retrofit interface querying Abstract API.
 * Free tier uses exact GET signatures to check domain validity natively.
 */
interface AbstractEmailApi {

    @GET("v1/")
    suspend fun validateEmail(
        @Query("api_key") apiKey: String,
        @Query("email") email: String
    ): Response<EmailValidationResponse>

    companion object {
        const val BASE_URL = "https://emailvalidation.abstractapi.com/"
        // Add Deliverability API Key instructions to README.md
        const val API_KEY = "YOUR_ABSTRACT_API_KEY_HERE"
    }
}
