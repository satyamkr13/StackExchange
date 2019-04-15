package com.instinotices.satyam.stackexchange;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {
    public final static String CLIENT_ID = "14911";
    View webLoginLayout;
    Button signInButton;
    WebView webView;
    View progressBar;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Setting up views
        webLoginLayout = findViewById(R.id.layoutWebLogin);
        signInButton = findViewById(R.id.signInButton);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startWebLogin();
            }
        });
        webView = findViewById(R.id.web_view);
        progressBar = findViewById(R.id.progressBar);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    private void startWebLogin() {
        // Build authentication URL
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority("stackoverflow.com")
                .appendPath("oauth")
                .appendPath("dialog")
                .appendQueryParameter("client_id", CLIENT_ID)
                .appendQueryParameter("scope", "no_expiry")
                .appendQueryParameter("redirect_uri", "https://stackexchangeapiexample.blogspot.com/finishLogin");
        String url = builder.build().toString();

        // Make WebView visible and start loading url
        webLoginLayout.setVisibility(View.VISIBLE);
        webView.loadUrl(url);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        // Setting a custom webview client to override loading when authentication completed
        webView.setWebViewClient(new LoginWebViewClient());
    }

    private void onLoginSucess() {
        Intent intent = new Intent(this, UserInterestActivity.class);
        sharedPreferences.edit().putString(QuestionListActivity.LOGIN_STATUS, QuestionListActivity.STATUS_LOGGED_IN).commit();
        startActivity(intent);
        Toast.makeText(this, "Thank you for signing in", Toast.LENGTH_SHORT).show();
        //finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        webLoginLayout.setVisibility(View.GONE);
        return super.onKeyDown(keyCode, event);
    }

    private class LoginWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            try {
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (Uri.parse(url).getHost() != null && Uri.parse(url).getHost().equals("stackexchangeapiexample.blogspot.com")) {
                // This is my website, so we've got some result from StackExchange Implicit Login
                Uri uri = Uri.parse(url);
                String accessToken = uri.getFragment();
                onLoginSucess();
                return true;
            }
            return false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            progressBar.setVisibility(View.VISIBLE);
        }
    }


}

