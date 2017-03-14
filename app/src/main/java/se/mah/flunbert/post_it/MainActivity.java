package se.mah.flunbert.post_it;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

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
    private static String TAG = "MainActivity";
    private Controller controller;
    private SharedPreferences sharedPreferences;
    private Switch assistanceSwitch;
    private Switch twitterSwitch;
    private Switch facebookSwitch;
    private TwitterLoginButton twitterLoginButton;
    private CallbackManager callbackManager;
    private LoginButton facebookLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig("9Wfs06IF2gRS7x7DnNiEBCmqZ", "ZycIA5Eyoet3zatWRuTsJ2yRDHUb4K2j7vpG2gIC1S2qZdcAh8");
        Fabric.with(this, new Twitter(authConfig));
        callbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_main);

        sharedPreferences = getPreferences(Context.MODE_PRIVATE);

        Button btnSnap = (Button) findViewById(R.id.btnSnap);
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.cameraHolder);
        RelativeLayout defaultView = (RelativeLayout) findViewById(R.id.defaultView);
        RelativeLayout cameraView = (RelativeLayout) findViewById(R.id.cameraView);
        TextView tvWeather = (TextView) findViewById(R.id.ivWeather);
        TextView tvLocation = (TextView) findViewById(R.id.ivLocation);
        Button btnSelfie = (Button) findViewById(R.id.btnSelfie);
        Button btnSend = (Button) findViewById(R.id.btnSend);
        ImageView visualHeight = (ImageView) findViewById(R.id.visualHeight);
        //Settings
        RelativeLayout assistanceView = (RelativeLayout) findViewById(R.id.assistanceView);
        assistanceSwitch = (Switch) findViewById(R.id.help_switch);
        twitterSwitch = (Switch) findViewById(R.id.twitter_switch);
        facebookSwitch = (Switch) findViewById(R.id.facebook_switch);

        twitterLoginButton = (TwitterLoginButton) findViewById(R.id.login_button);
        Button twitterLogoutButton = (Button) findViewById(R.id.logout_button);
        twitterLogoutButton.setText("Logout Twitter");
        if (Twitter.getSessionManager().getActiveSession() == null) {
            twitterLogoutButton.setVisibility(View.GONE);
        } else {
            twitterLoginButton.setVisibility(View.GONE);
        }

        facebookLoginButton = (LoginButton)findViewById(R.id.fb_login_button);
        facebookLoginButton.setReadPermissions("email");

        facebookLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });
        View[] views = new View[]{btnSnap, surfaceView, defaultView, cameraView, tvLocation,
                tvWeather, btnSelfie, btnSend, visualHeight, assistanceView, assistanceSwitch,
                twitterSwitch, facebookSwitch, twitterLoginButton, twitterLogoutButton};
        controller = new Controller(this, views);
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
        facebookSwitch.setChecked(sharedPreferences.getBoolean("facebookSwitch", false));
        controller.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(resultCode, resultCode, data);
        twitterLoginButton.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TwitterAuthConfig.DEFAULT_AUTH_REQUEST_CODE) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    twitterSwitch.setChecked(true);
                }
            });
        }else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    facebookSwitch.setChecked(true);
                }
            });
        }
    }
}
