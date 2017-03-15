package se.mah.flunbert.post_it;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.IntegerRes;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends Activity {
    private Switch assistanceSwitch, facebookSwitch, twitterSwitch;
    private TwitterLoginButton twitterLoginButton;
    private SharedPreferences sharedPreferences;
    private static String TAG = "MainActivity";
    private CallbackManager callbackManager;
    private Controller controller;
    private boolean loggedInFb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(String.valueOf(R.string.twitter_key), String.valueOf(R.string.twitter_secret));
        Fabric.with(this, new Twitter(authConfig));
        callbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_main);

        sharedPreferences = getPreferences(Context.MODE_PRIVATE);

        twitterLoginButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
        Button twitterLogoutButton = (Button) findViewById(R.id.twitter_logout_button);
        assistanceSwitch = (Switch) findViewById(R.id.assistance_switch);
        facebookSwitch = (Switch) findViewById(R.id.facebook_switch);
        twitterSwitch = (Switch) findViewById(R.id.twitter_switch);

        twitterLogoutButton.setText("Logout Twitter");
        if (Twitter.getSessionManager().getActiveSession() == null) {
            twitterLogoutButton.setVisibility(View.GONE);
            twitterSwitch.setEnabled(true);
        }
        else {
            twitterLoginButton.setVisibility(View.GONE);
            twitterSwitch.setEnabled(false);
        }
        LoginButton facebookLoginButton = (LoginButton) findViewById(R.id.fb_login_button);
        facebookLoginButton.setReadPermissions("email");
        facebookLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "onSuccess: " + loginResult.toString());
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException exception) {
            }
        });
        facebookLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(loggedInFb){
                    facebookSwitch.setEnabled(false);
                    facebookSwitch.setChecked(false);
                    loggedInFb = false;
                }
            }
        });

        controller = new Controller(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("assistanceSwitch", assistanceSwitch.isChecked());
        editor.putBoolean("facebookSwitch", facebookSwitch.isChecked());
        editor.putBoolean("twitterSwitch", twitterSwitch.isChecked());
        editor.apply();
        controller.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        assistanceSwitch.setChecked(sharedPreferences.getBoolean("assistanceSwitch", true));
        twitterSwitch.setChecked(sharedPreferences.getBoolean("twitterSwitch", false));
        twitterSwitch.setEnabled(sharedPreferences.getBoolean("twitterSwitch", false));
        facebookSwitch.setChecked(sharedPreferences.getBoolean("facebookSwitch", false));
        facebookSwitch.setEnabled(sharedPreferences.getBoolean("facebookSwitch",false));
        loggedInFb = sharedPreferences.getBoolean("facebookSwitch",false);
        controller.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(resultCode, resultCode, data);
        Log.e(TAG, "onActivityResult: " + requestCode);
        if (requestCode == 140) {
            twitterLoginButton.onActivityResult(requestCode, resultCode, data);
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    twitterSwitch.setEnabled(true);
                    twitterSwitch.setChecked(true);
                }
            });
        } else if (requestCode == 64206) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    loggedInFb=true;
                    facebookSwitch.setEnabled(true);
                    facebookSwitch.setChecked(true);
                }
            });
        }
    }
}
