package online.produck.simplegithub.api

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import online.produck.simplegithub.data.AuthTokenProvider
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

object GithubApiProvider {

    fun provideAuthApi(): AuthApi {
        return Retrofit.Builder()
                .baseUrl("https://github.com")
                .client(provideOkHttpClient(provideLoggingInterceptor(), null))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(AuthApi::class.java)
    }

    private fun provideOkHttpClient(
            httpLoggingInterceptor: HttpLoggingInterceptor,
            authInterceptor: AuthInterceptor?): OkHttpClient {
        val builder = OkHttpClient.Builder()

        if (null != authInterceptor) {
            builder.addInterceptor(authInterceptor)
        }

        builder.addInterceptor(httpLoggingInterceptor)
        return builder.build()
    }

    private fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        return interceptor
    }

    fun provideGithubApi(context: Context): GithubApi {
        return Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .client(provideOkHttpClient(provideLoggingInterceptor(),
                        provideAuthInterceptor(provideAuthTokenProvider(context))))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GithubApi::class.java)

    }

    private fun provideAuthInterceptor(provider: AuthTokenProvider): AuthInterceptor {
        val token = provider.token ?: throw IllegalStateException("Authtoken cannot be null")

        return AuthInterceptor(token)
    }

    private fun provideAuthTokenProvider(context: Context): AuthTokenProvider {
        return AuthTokenProvider(context.applicationContext)
    }

    internal class AuthInterceptor(private val token: String) : Interceptor {

        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val original = chain.request()

            val b = original.newBuilder().addHeader("Authorization", "token $token")

            return chain.proceed(b.build())
        }
    }
}
