package com.wwm.todo

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.auth0.android.Auth0
import com.auth0.android.Auth0Exception
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.provider.AuthCallback
import com.auth0.android.provider.VoidCallback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import com.wwm.todo.auth.AuthenticationServiceImpl
import com.wwm.todo.auth.AuthenticationServiceImpl.updateOpenIdData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class Auth0LoginActivity : AppCompatActivity() {
    private lateinit var auth0: Auth0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val loginButton =
            findViewById<Button>(R.id.loginButton)
        loginButton.setOnClickListener { login() }
        auth0 = Auth0(this)
        auth0.isOIDCConformant = true

        //Check if the activity was launched to log the user out
        if (intent.getBooleanExtra(
                EXTRA_CLEAR_CREDENTIALS,
                false)) {
            logout()
        }
    }

    private fun cpcLogin() {
        lifecycleScope.launch{
            AuthenticationServiceImpl.login("sbstg2napp", "Happy123!")
            Timber.d("${AuthenticationServiceImpl.accessToken} ${AuthenticationServiceImpl.refreshToken}")
            goToMainActivity(AuthenticationServiceImpl.accessToken!!)
        }
    }

    private fun login() {
        WebAuthProvider.login(auth0)
            .withScheme("demo")
            .withAudience(
                String.format(
                    "https://%s/userinfo",
                    getString(R.string.com_auth0_domain)
                )
            )
            .start(this, object : AuthCallback {
                override fun onFailure(dialog: Dialog) {
                    runOnUiThread { dialog.show() }
                }

                override fun onFailure(exception: AuthenticationException) {
                    runOnUiThread {
                        Toast.makeText(
                            this@Auth0LoginActivity,
                            "Error: " + exception.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onSuccess(credentials: Credentials) {
                    runOnUiThread {
                        updateOpenIdData(credentials)
                        goToMainActivity(credentials.accessToken!!)
                    }
                }
            })
    }

    private fun goToMainActivity(accessToken: String){
        val intent =
            Intent(this@Auth0LoginActivity, MainActivity::class.java)
        intent.putExtra(
            EXTRA_ACCESS_TOKEN,
            accessToken
        )
        startActivity(intent)
        finish()
    }

    private fun logout() {
        WebAuthProvider.logout(auth0!!)
            .withScheme("demo")
            .start(this, object : VoidCallback {
                override fun onSuccess(payload: Void) {}
                override fun onFailure(error: Auth0Exception) {
                    //Log out canceled, keep the user logged in
                    showNextActivity()
                }
            })
    }

    private fun showNextActivity() {
        val intent = Intent(this@Auth0LoginActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    companion object {
        const val EXTRA_CLEAR_CREDENTIALS = "com.auth0.CLEAR_CREDENTIALS"
        const val EXTRA_ACCESS_TOKEN = "com.auth0.ACCESS_TOKEN"
    }
}