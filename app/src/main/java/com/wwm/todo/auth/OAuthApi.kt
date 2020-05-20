package com.wwm.todoapp.auth

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.wwm.todo.BuildConfig
import kotlinx.coroutines.Deferred
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

interface OAuthApi {
    @FormUrlEncoded
    @POST("/mga/sps/oauth/oauth20/token")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    fun login(@Field("username") username: String,
              @Field("password") password: String,
              @Field("grant_type") type: String = "password",
              @Field("client_secret") clientSecret: String = BuildConfig.OAUTH_CLIENT_SECRET,
              @Field("client_id") clientId: String = BuildConfig.OAUTH_CLIENT_ID,
              @Field("scope") scope: String = "openid"
    )
            : Deferred<Response<LoginResponse>>

    @FormUrlEncoded
    @POST("/mga/sps/oauth/oauth20/token")
    fun refreshToken(@Field("refresh_token") refreshToken: String,
                             @Field("grant_type") type: String = "refresh_token",
                             @Field("client_secret") clientSecret: String = BuildConfig.OAUTH_CLIENT_SECRET,
                             @Field("client_id") clientId: String = BuildConfig.OAUTH_CLIENT_ID)
            : Deferred<Response<LoginResponse>>
}

private val retrofit = setupRetrofit(BuildConfig.OAUTH_ROOT)

object OAuthApiFactory {
    val oAuthApi: OAuthApi by lazy {
        retrofit.create(OAuthApi::class.java)
    }
}

fun setupRetrofit(baseUrl: String): Retrofit {
    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val logging = HttpLoggingInterceptor()
    logging.level = HttpLoggingInterceptor.Level.BODY
    val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()
    val retrofit = Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .client(client)
        .baseUrl(baseUrl)
        .build()
    return retrofit
}