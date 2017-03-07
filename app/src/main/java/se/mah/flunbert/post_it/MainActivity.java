package se.mah.flunbert.post_it;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends Activity {
    private Controller controller;
    private SharedPreferences sharedPreferences;
    private Switch assistanceSwitch;
    private Switch twitterSwitch;
    private Switch facebookSwitch;
    private TwitterLoginButton loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig("9Wfs06IF2gRS7x7DnNiEBCmqZ", "ZycIA5Eyoet3zatWRuTsJ2yRDHUb4K2j7vpG2gIC1S2qZdcAh8");
        Fabric.with(this, new Twitter(authConfig));
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
        loginButton = (TwitterLoginButton) findViewById(R.id.login_button);
        loginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                //session = result.data;
                Toast.makeText(MainActivity.this, "Connected to twitter!", Toast.LENGTH_SHORT).show();
                twitterSwitch.setChecked(true);
                loginButton.setVisibility(View.GONE);
            }

            @Override
            public void failure(TwitterException exception) {
                Toast.makeText(MainActivity.this, "Failure to connect", Toast.LENGTH_SHORT).show();
            }
        });

        View[] views = new View[]{btnSnap, surfaceView, defaultView, cameraView, tvLocation,
                tvWeather, btnSelfie, btnSend, visualHeight, assistanceView, assistanceSwitch,
                twitterSwitch, facebookSwitch};
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
        loginButton.onActivityResult(requestCode, resultCode, data);
    }
}
