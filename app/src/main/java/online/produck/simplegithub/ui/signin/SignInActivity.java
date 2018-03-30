package online.produck.simplegithub.ui.signin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import online.produck.simplegithub.BuildConfig;
import online.produck.simplegithub.R;
import online.produck.simplegithub.api.AuthApi;
import online.produck.simplegithub.api.GithubApiProvider;
import online.produck.simplegithub.api.model.GithubAccessToken;
import online.produck.simplegithub.data.AuthTokenProvider;
import online.produck.simplegithub.ui.main.MainActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignInActivity extends AppCompatActivity {

    Button btnStart;
    ProgressBar progress;
    AuthApi api;
    AuthTokenProvider authTokenProvider;

    Call<GithubAccessToken> accessTokenCall;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        btnStart = findViewById(R.id.btnActivitySignInStart);
        progress = findViewById(R.id.pbActivitySignIn);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // create a URL which process user authentication
                Uri authUri = new Uri.Builder().scheme("https")
                        .authority("github.com")
                        .appendPath("login")
                        .appendPath("oauth")
                        .appendPath("authorize")
                        .appendQueryParameter("client_id", BuildConfig.GITHUB_CLIENT_ID).build();

                CustomTabsIntent intent = new CustomTabsIntent.Builder().build();
                intent.launchUrl(SignInActivity.this, authUri);
            }
        });

        api = GithubApiProvider.provideAuthApi();
        authTokenProvider = new AuthTokenProvider(this);

        if (null != authTokenProvider.getToken()) {
            launchMainActivity();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        showProgress();

        Uri uri = intent.getData();

        if (null == uri) {
            throw new IllegalArgumentException("No data exists");
        }

        String code = uri.getQueryParameter("code");
        if (null == code) {
            throw new IllegalStateException("No code exists");
        }

        getAccessToken(code);
    }

    private void getAccessToken(@NonNull String code) {
        showProgress();

        accessTokenCall = api.getAccessToken(
                BuildConfig.GITHUB_CLIENT_ID, BuildConfig.GITHUB_CLIENT_SECRET, code
        );

        accessTokenCall.enqueue(new Callback<GithubAccessToken>() {
            @Override
            public void onResponse(@NonNull Call<GithubAccessToken> call, @NonNull Response<GithubAccessToken> response) {
                hideProgress();

                GithubAccessToken token = response.body();
                if (response.isSuccessful() && null != token) {
                    authTokenProvider.updateToken(token.accessToken);

                    launchMainActivity();
                } else {
                    showError(new IllegalStateException("Not successful: " + response.message()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<GithubAccessToken> call, @NonNull Throwable t) {
                hideProgress();
                showError(t);
            }
        });
    }

    private void showProgress() {
        btnStart.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        btnStart.setVisibility(View.VISIBLE);
        progress.setVisibility(View.GONE);
    }

    private void showError(Throwable throwable) {
        Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_LONG).show();
    }

    private void launchMainActivity() {
        startActivity(
                new Intent(SignInActivity.this, MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        );
    }
}
