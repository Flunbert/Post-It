package se.mah.flunbert.post_it;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends Activity {
    private Controller controller;
    private SharedPreferences sharedPreferences;
    private Switch assistanceSwitch;
    private Switch twitterSwitch;
    private Switch facebookSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        assistanceSwitch.setChecked(sharedPreferences.getBoolean("assistanceSwitch", false));
        twitterSwitch.setChecked(sharedPreferences.getBoolean("twitterSwitch", false));
        facebookSwitch.setChecked(sharedPreferences.getBoolean("facebookSwitch", false));
        controller.onResume();
    }
}
