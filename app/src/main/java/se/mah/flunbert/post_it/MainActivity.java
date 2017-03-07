package se.mah.flunbert.post_it;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends Activity {
    private TwitterLoginButton loginButton;
    private TwitterSession session;
    public boolean loggedIntoTwitter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig("9Wfs06IF2gRS7x7DnNiEBCmqZ", "ZycIA5Eyoet3zatWRuTsJ2yRDHUb4K2j7vpG2gIC1S2qZdcAh8");
        Fabric.with(this, new Twitter(authConfig));
        setContentView(R.layout.activity_main);

        Button btnSnap = (Button) findViewById(R.id.btnSnap);
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.cameraHolder);
        RelativeLayout defaultView = (RelativeLayout) findViewById(R.id.defaultView);
        RelativeLayout cameraView = (RelativeLayout) findViewById(R.id.cameraView);
        TextView tvWeather = (TextView) findViewById(R.id.ivWeather);
        TextView tvLocation = (TextView) findViewById(R.id.ivLocation);
        Button btnSelfie = (Button) findViewById(R.id.btnSelfie);
        Button btnSend = (Button) findViewById(R.id.btnSend);
        loginButton = (TwitterLoginButton)findViewById(R.id.login_button);
        loginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                session = result.data;
                Toast.makeText(MainActivity.this, "Connected to twitter!", Toast.LENGTH_SHORT).show();
                loginButton.setVisibility(8);
                loggedIntoTwitter=true;
            }

            @Override
            public void failure(TwitterException exception) {
                Toast.makeText(MainActivity.this, "Failure to connect", Toast.LENGTH_SHORT).show();
            }
        });
        View[] views = new View[]{btnSnap, surfaceView, defaultView, cameraView, tvLocation, tvWeather, btnSelfie, btnSend,loginButton};
        new Controller(this, views);

    }
    @Override
    protected void onActivityResult(int requestCode,int resultCode, Intent data){
        super.onActivityResult(resultCode,resultCode,data);
        loginButton.onActivityResult(requestCode,resultCode,data);
    }

    public TwitterSession getSession() {
        return session;
    }
}
