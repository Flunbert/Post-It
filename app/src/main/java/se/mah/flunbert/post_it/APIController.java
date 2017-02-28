package se.mah.flunbert.post_it;
/*
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.*;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;
*/
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.mortbay.util.ajax.JSON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


/**
 * APIController class.
 *
 * @author Björn Svensson
 * @since 23/2/2017
 */
public class APIController {
    private final boolean canUseGps;
    private APIStorage apiStorage;
    private Activity activity;
    private LocationManager mLocationManager;
    private Location currentLoc;

    public enum APIs {
        facebook, twitter, weather, location
    }

    public APIController(MainActivity activity) {
        this.activity = activity;
        this.apiStorage = new APIStorage();

        if (activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            canUseGps = false;
        } else canUseGps = true;

        mLocationManager = (LocationManager)activity.getSystemService(activity.LOCATION_SERVICE);

    }


    public boolean RegisterAPI(APIs api) {
        switch (api) {
            case twitter:
               // AuthorizeTwitter();
                return true;
            default:
                return false;
        }
    }

    public boolean DeregisterAPI(APIs api) {

        switch (api) {
            case twitter:
               // AuthorizeTwitter();
                return true;
            default:
                return false;
        }
    }

    /**
     * Recieves JSON object from an API
     * @param api
     * @return JSON object
     */
    public String FetchAPI(APIs api, TextView tv) {

        switch (api) {
            case location:
                String localLocation = getLocationString();
                if(localLocation !=null) {
                    Log.v("Got location: ", localLocation);
                    tv.setText(localLocation);
                }
                return null;
            case weather:
                String location = getWeatherLocation();
                new WeatherCall(location,tv).start();
                break;
            default:
                return null;
        }
        return null;
    }

    private String getWeatherLocation() {
        String weatherLoc = null;
        if(currentLoc==null) {
            currentLoc = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        if(currentLoc != null && currentLoc.getTime() > Calendar.getInstance().getTimeInMillis() - 2 * 60 * 1000) {
            // Do something with the recent location fix
            //  otherwise wait for the update below
        }
        else {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new CurrentLocation());
            weatherLoc = "" + currentLoc.getLatitude() + "," + currentLoc.getLongitude();
        }
        return weatherLoc;
    }

    private String getLocationString() {
        //TODO: closer specification on current location
        if(currentLoc==null) {
            currentLoc = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        if(currentLoc != null && currentLoc.getTime() > Calendar.getInstance().getTimeInMillis() - 2 * 60 * 1000) {
            // Do something with the recent location fix
            //  otherwise wait for the update below
        }
        else {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new CurrentLocation());
            String add = "";
            Geocoder geoCoder = new Geocoder(activity, Locale.getDefault());
            try {
                List<Address> addresses = geoCoder.getFromLocation(currentLoc.getLatitude(), currentLoc.getLongitude(), 1);


                if (addresses.size() > 0){
                    add = addresses.get(0).getLocality();
                }
            }
            catch (IOException e1) {
                e1.printStackTrace();
            }
            return add;
        }
        return null;
    }



    public JSON SendToAPI(APIs api){
        switch(api) {
            case twitter:
               // AuthorizeTwitter();
                return null;
            default:
                return null;
        }
    }

/*
    private void sendTweet(String tweetText) {
        TwitterSession session = Twitter.getSessionManager().getActiveSession();
        TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient(session);
        StatusesService service = twitterApiClient.getStatusesService();

        Call<Tweet> call = service.update(tweetText, null, null, null, null, null, null, null, null);
        call.enqueue(new Callback<Tweet>() {

            /**
             * If sucess it procedes to send the result as a tweet.
             * @param result

            @Override
            public void success(Result<Tweet> result) {
                Tweet tweet = result.data;
                Log.e("TwitterResult", tweet.text);
            }

            /**
             * If fail, an exception is called upon and error message is displayed.
             * @param exception

            public void failure(TwitterException exception) {
                Log.e("TwitterException", exception.getMessage());
            }
        });
    }
    private boolean AuthorizeTwitter() {
        TwitterAuthConfig authConfig = new TwitterAuthConfig(apiStorage.TWITTER_KEY, apiStorage.TWITTER_SECRET);
        Fabric.with(activity, new Twitter(authConfig));
        return false;
    } */
    private class APIStorage{
        private final String TWITTER_KEY = "9Wfs06IF2gRS7x7DnNiEBCmqZ";
        /**
         * Defines a private static of string for the twitter secret.
         */
        private final String TWITTER_SECRET = "ZycIA5Eyoet3zatWRuTsJ2yRDHUb4K2j7vpG2gIC1S2qZdcAh8";

        private final String WEATHER_KEY = "56fc4dd72aef4a05b9780638172802";
    }

    private class CurrentLocation implements LocationListener{
        @Override
        public void onLocationChanged(Location location) {
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();
            Log.v("Location Changed", latitude + " and " + longitude);
            mLocationManager.removeUpdates(this);

        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    }

    private class WeatherCall extends Thread{
        private String weatherCallLocation;
        private TextView tv;

        public WeatherCall(String location,TextView tv) {
            this.weatherCallLocation = location;

            this.tv = tv;
        }

        @Override
        public void run() {
                try {
                    URL url = new URL("http://api.apixu.com/v1/current.json?key=" + apiStorage.WEATHER_KEY + "&q=" + weatherCallLocation);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    try {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            stringBuilder.append(line).append("\n");
                        }
                        bufferedReader.close();
                        final JSONObject json = new JSONObject(stringBuilder.toString());
                        Log.d("LOCATION INFORMATION",json.getJSONObject("location").getString("name"));
                        Log.d("CURRENT TEMP",json.getJSONObject("current").getString("temp_c"));
                        Log.d("WEATHER INFORMATION: ", "" + stringBuilder.toString());

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    tv.setText("temperature: " + json.getJSONObject("current").getString("temp_c") + " Current weather: " +
                                    json.getJSONObject("current").getJSONObject("condition").getString("text"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } finally {
                        urlConnection.disconnect();
                    }
                } catch (Exception e) {
                    Log.e("ERROR", e.getMessage(), e);
                }
            }
        }
    }



