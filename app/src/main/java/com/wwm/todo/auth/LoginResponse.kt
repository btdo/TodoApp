package com.wwm.todoapp.auth

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json

// Be careful what Json converter library is used and what library field annotations are used.
// Here we use field annotations for Gson and Moshi libraries.
@JsonIgnoreProperties(ignoreUnknown = true)
data class LoginResponse(

    val access_token: String?,

    val refresh_token: String?,

    val AUTHENTICATION_LEVEL: String?,

    val scope: String?,

    val token_type: String?,

    val expires_in: Int?
) {
    var timeStamp: Long = 0

    fun toJsonString(): String {
        return Gson()
                .toJson(this, LoginResponse::class.java)
    }

    companion object {
        fun formJsonString(string: String?): LoginResponse? {
            return Gson()
                    .fromJson(string, LoginResponse::class.java)
        }
    }
}