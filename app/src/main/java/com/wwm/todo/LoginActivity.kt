package com.wwm.todo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.wwm.todo.auth.AuthenticationServiceImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val loginButton =
            findViewById<Button>(R.id.loginButton)
        loginButton.setOnClickListener {
            // login()
            cpcLogin()
        }
    }

    private fun cpcLogin() {
        lifecycleScope.launch(Dispatchers.Main) {
            AuthenticationServiceImpl.login("sbstg2napp", "Happy123!")
            Timber.d("${AuthenticationServiceImpl.accessToken} ${AuthenticationServiceImpl.refreshToken}")
            goToMainActivity(AuthenticationServiceImpl.accessToken!!)
        }
    }


    private fun goToMainActivity(accessToken: String){
        val intent =
            Intent(this@LoginActivity, MainActivity::class.java)
        intent.putExtra(
            EXTRA_ACCESS_TOKEN,
            accessToken
        )
        startActivity(intent)
        finish()
    }

    companion object {
        const val EXTRA_CLEAR_CREDENTIALS = "com.auth0.CLEAR_CREDENTIALS"
        const val EXTRA_ACCESS_TOKEN = "com.auth0.ACCESS_TOKEN"
    }
}