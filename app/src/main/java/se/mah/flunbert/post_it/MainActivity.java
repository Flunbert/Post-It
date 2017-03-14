package se.mah.flunbert.post_it;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends Activity {
    private Switch assistanceSwitch, facebookSwitch, twitterSwitch;
    private TwitterLoginButton twitterLoginButton;
    private SharedPreferences sharedPreferences;
    private static String TAG = "MainActivity";
    private Controller controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig("9Wfs06IF2gRS7x7DnNiEBCmqZ", "ZycIA5Eyoet3zatWRuTsJ2yRDHUb4K2j7vpG2gIC1S2qZdcAh8");
        Fabric.with(this, new Twitter(authConfig));
        setContentView(R.layout.activity_main);

        sharedPreferences = getPreferences(Context.MODE_PRIVATE);

        twitterLoginButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
        Button twitterLogoutButton = (Button) findViewById(R.id.twitter_logout_button);
        assistanceSwitch = (Switch) findViewById(R.id.assistance_switch);
        facebookSwitch = (Switch) findViewById(R.id.facebook_switch);
        twitterSwitch = (Switch) findViewById(R.id.twitter_switch);

        twitterLogoutButton.setText("Logout Twitter");
        if (Twitter.getSessionManager().getActiveSession() == null)
            twitterLogoutButton.setVisibility(View.GONE);
        else
            twitterLoginButton.setVisibility(View.GONE);
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
        facebookSwitch.setChecked(sharedPreferences.getBoolean("facebookSwitch", false));
        controller.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(resultCode, resultCode, data);
        twitterLoginButton.onActivityResult(requestCode, resultCode, data);
        if (data.getExtras().containsKey("screen_name")) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    twitterSwitch.setChecked(true);
                }
            });
        }
    }
}
