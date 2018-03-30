package online.produck.simplegithub.api;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import online.produck.simplegithub.data.AuthTokenProvider;
import online.produck.simplegithub.ui.search.SearchActivity;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class GithubApiProvider {

    public static AuthApi provideAuthApi() {
        return new Retrofit.Builder()
                .baseUrl("https://github.com")
                .client(provideOkHttpClient(provideLoggingInterceptor(), null))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(AuthApi.class);
    }

    private static OkHttpClient provideOkHttpClient(
            @NonNull HttpLoggingInterceptor httpLoggingInterceptor,
            @Nullable AuthInterceptor authInterceptor) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        if (null != authInterceptor) {
            builder.addInterceptor(authInterceptor);
        }

        builder.addInterceptor(httpLoggingInterceptor);
        return builder.build();
    }

    private static HttpLoggingInterceptor provideLoggingInterceptor() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return interceptor;
    }

    public static GithubApi provideGithubApi(@NonNull Context context) {
        return new Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .client(provideOkHttpClient(provideLoggingInterceptor(),
                        provideAuthInterceptor(provideAuthTokenProvider(context))))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GithubApi.class);

    }

    private static AuthInterceptor provideAuthInterceptor(AuthTokenProvider provider) {
        String token = provider.getToken();
        if (null == token) {
            throw new IllegalStateException("Authtoken cannot be null");
        }

        return new AuthInterceptor(token);
    }

    private static AuthTokenProvider provideAuthTokenProvider(@NonNull Context context) {
        return new AuthTokenProvider(context.getApplicationContext());
    }

    static class AuthInterceptor implements Interceptor {

        private final String token;

        AuthInterceptor(String token) {
            this.token = token;
        }

        @Override
        public Response intercept(@NonNull Chain chain) throws IOException {
            Request original = chain.request();

            Request.Builder b = original.newBuilder().addHeader("Authorization", "token " + token);

            return chain.proceed(b.build());
        }
    }
}
