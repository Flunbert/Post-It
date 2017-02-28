package se.mah.flunbert.post_it;

import android.app.Activity;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnSnap = (Button) findViewById(R.id.btnSnap);
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.cameraHolder);
        RelativeLayout defaultView = (RelativeLayout) findViewById(R.id.defaultView);
        RelativeLayout cameraView = (RelativeLayout) findViewById(R.id.cameraView);
        TextView tvWeather = (TextView) findViewById(R.id.ivWeather);
        TextView tvLocation = (TextView) findViewById(R.id.ivLocation);
        Button btnSelfie = (Button) findViewById(R.id.btnSelfie);
        Button btnSend = (Button) findViewById(R.id.btnSend);
        View[] views = new View[]{btnSnap, surfaceView, defaultView, cameraView, tvLocation, tvWeather, btnSelfie, btnSend};
        new Controller(this, views);
    }
}
