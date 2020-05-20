package com.wwm.todo.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.auth0.android.result.Credentials
import com.wwm.todoapp.auth.LoginResponse
import com.wwm.todoapp.auth.OAuthApiFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*

object AuthenticationServiceImpl :
    AuthenticationService {
    private var _loggedIn: MutableLiveData<Boolean> = MutableLiveData()
    override val loggedIn: LiveData<Boolean>
        get() = _loggedIn

    override var accessToken: String? = null

    override var refreshToken: String? = null

    override var idToken: String? = null

    private var oAuthResponse: LoginResponse? = null

    private var credentials: Credentials? = null


    override fun isExpiredStatusCode(code: Int): Boolean {
        if (code == 401 || code == 403) {
            return true
        }

        return false
    }

    override fun isTokenExpired(timeSpan: Int?, networkErrorCode: Int?): Boolean {
        networkErrorCode?.let {
            if (isExpiredStatusCode(it)) {
                return true
            }
        }

        val response = oAuthResponse
        val timeStamp = response?.timeStamp
        val expiresIn = timeSpan ?: response?.expires_in
        if (timeStamp == null || expiresIn == null) {
            return false
        }

        val currentTime = Date().time
        val expTime = timeStamp + expiresIn * 1000
        return currentTime < expTime
    }

    override suspend fun login(username: String, password: String) = withContext(Dispatchers.IO){
        logout()
        try {
            val response = OAuthApiFactory.oAuthApi.login(username, password)
            handleLoginResponse(response.await())
        } catch (e: Exception){
            Timber.e(e)
        }
    }

    override suspend fun refresh(){
        if (refreshToken == null) {
            val message = "Failed to refresh Epost access token, refresh token is null"
            throw Exception(message)
        }

        OAuthApiFactory.oAuthApi.refreshToken(refreshToken!!)
    }

    private fun handleLoginResponse(response: LoginResponse) {
        val oAuthResponseData = response
        updateOAuthResponse(oAuthResponseData)
        Timber.d("Epost login or access token refresh was successful")
    }

    private fun updateOAuthResponse(response: LoginResponse?) {
        oAuthResponse = response
        oAuthResponse?.let {
            it.timeStamp = Date().time
            accessToken = it.access_token
            refreshToken = it.refresh_token
        }
        val newLoginState = oAuthResponse?.refresh_token != null
        if (_loggedIn.value != newLoginState) {
            _loggedIn.postValue(newLoginState)
        }
    }

    override suspend fun logout() {
        updateOAuthResponse(null)
    }

    override fun updateOAuthData(data: LoginResponse?) {
        if (data == null) {
            updateOAuthResponse(null)
            return
        }
        updateOAuthResponse(data)
    }

    override fun updateOpenIdData(credentials: Credentials?) {
        this.credentials = credentials
        this.credentials?.let {
            accessToken = it.accessToken
            refreshToken = it.refreshToken
            idToken = it.idToken
        }
    }

    override fun clearOAuthData() {
        updateOAuthResponse(null)
    }
}

