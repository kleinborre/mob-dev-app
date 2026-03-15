package com.sample.calorease.data.remote.api

import com.google.gson.annotations.SerializedName

/**
 * Sprint 4 Phase 4: Data model parsing the Abstract API payload.
 * We primarily care about format, deliverability, and disposable flags.
 */
data class EmailValidationResponse(
    @SerializedName("email") val email: String?,
    @SerializedName("autocorrect") val autocorrect: String?,
    @SerializedName("deliverability") val deliverability: String?,
    @SerializedName("quality_score") val qualityScore: String?,
    @SerializedName("is_valid_format") val isValidFormat: BooleanWrapper?,
    @SerializedName("is_free_email") val isFreeEmail: BooleanWrapper?,
    @SerializedName("is_disposable_email") val isDisposableEmail: BooleanWrapper?,
    @SerializedName("is_role_email") val isRoleEmail: BooleanWrapper?,
    @SerializedName("is_catchall_email") val isCatchallEmail: BooleanWrapper?,
    @SerializedName("is_mx_found") val isMxFound: BooleanWrapper?,
    @SerializedName("is_smtp_valid") val isSmtpValid: BooleanWrapper?
) {
    data class BooleanWrapper(
        @SerializedName("value") val value: Boolean?,
        @SerializedName("text") val text: String?
    )
}
