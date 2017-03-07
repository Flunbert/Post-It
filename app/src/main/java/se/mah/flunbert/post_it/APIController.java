package se.mah.flunbert.post_it;


import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import org.json.JSONException;
import org.json.JSONObject;
import org.mortbay.util.ajax.JSON;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;

import io.fabric.sdk.android.Fabric;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.Media;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.MediaService;
import com.twitter.sdk.android.core.services.StatusesService;


/**
 * APIController class.
 *
 * @author Björn Svensson
 * @since 23/2/2017
 */
public class APIController {
    private final boolean canUseGps;
    private APIStorage apiStorage;
    private MainActivity activity;
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

        mLocationManager = (LocationManager) activity.getSystemService(activity.LOCATION_SERVICE);

    }


    public boolean RegisterAPI(APIs api) {
        switch (api) {
            case twitter:
                AuthorizeTwitter();
                return true;
            default:
                return false;
        }
    }

    public boolean DeregisterAPI(APIs api) {

        switch (api) {
            case twitter:
                return false;
            default:
                return false;
        }
    }

    /**
     * Recieves JSON object from an API
     *
     * @param api
     * @return JSON object
     */
    public String FetchAPI(APIs api, TextView tv) {

        switch (api) {
            case location:
                String localLocation = getLocationString();
                if (localLocation != null) {
                    Log.v("Got location: ", localLocation);
                    tv.setText(localLocation);
                }
                return null;
            case weather:
                String location = getWeatherLocation();
                new WeatherCall(location, tv).start();
                break;
            default:
                return null;
        }
        return null;
    }

    private String getWeatherLocation() {
        if (currentLoc == null) {
            currentLoc = getLocation();
        }
           String weatherLoc = "" + currentLoc.getLatitude() + "," + currentLoc.getLongitude();
        return weatherLoc;
    }

    private String getLocationString() {
        if (currentLoc == null) {
            currentLoc = getLocation();
        }
            String add = "";
            Geocoder geoCoder = new Geocoder(activity, Locale.getDefault());
            try {
                List<Address> addresses = geoCoder.getFromLocation(currentLoc.getLatitude(), currentLoc.getLongitude(), 1);


                if (addresses.size() > 0) {
                    add = addresses.get(0).getLocality();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return add;

    }

    private Location getLocation(){
        CurrentLocation listener = new CurrentLocation();
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,listener);
        Location currentLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        mLocationManager.removeUpdates(listener);
        return currentLocation;
    }


    public JSON SendToAPI(APIs api, final String bitmap) {
        switch (api) {
            case twitter:
                sendTweet(bitmap);
                return null;
            default:
                return null;
        }
    }

    private String getRealPathFromURI(Uri uri) {
        Cursor cursor = activity.getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }

    private void sendTweet(String bitmapURL) {
            final String string = "Sent by Post-it app project!";

            TwitterSession session = Twitter.getSessionManager().getActiveSession();
            TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient(session);
            final StatusesService service = twitterApiClient.getStatusesService();
            File file = new File(getRealPathFromURI(Uri.parse(bitmapURL)));

            MediaService ms = twitterApiClient.getMediaService();
            MediaType type = MediaType.parse("image/*");
            RequestBody body = RequestBody.create(type,file);
            Call<Media> mediaCall= ms.upload(body, null, null);

            mediaCall.enqueue(new Callback<Media>() {
                @Override
                public void failure(TwitterException exception) {
                    Toast.makeText(activity, "Media Call failed!", Toast.LENGTH_SHORT);

                }

                @Override
                public void success(Result<Media> result) {
                    Call<Tweet> call = service.update(string, null, null, null, null, null, null, null, String.valueOf(result.data.mediaId));

                    call.enqueue(new Callback<Tweet>() {

                        /**
                         * If sucess it procedes to send the result as a tweet.
                         *
                         * @param result
                         */


                        @Override
                        public void success(Result<Tweet> result) {
                            Tweet tweet = result.data;
                            Log.e("TwitterResult", tweet.text);
                        }

                        /**
                         * If fail, an exception is called upon and error message is displayed.
                         *
                         * @param exception
                         */

                        public void failure(TwitterException exception) {
                            Log.e("TwitterException", exception.getMessage());
                        }
                    });
                }
            });
        }


        private boolean AuthorizeTwitter() {
            //TODO: Kanske kan rensas undan komplett?
            TwitterAuthConfig authConfig = new TwitterAuthConfig(apiStorage.TWITTER_KEY, apiStorage.TWITTER_SECRET);
            Fabric.with(activity, new Twitter(authConfig));
            new Callback<TwitterSession>() {
                @Override
                public void success(Result<TwitterSession> result) {
                    TwitterSession session = result.data;
                    Toast.makeText(activity, "Connected to twitter!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void failure(TwitterException exception) {
                    Toast.makeText(activity, "Failure to connect", Toast.LENGTH_SHORT).show();
                }
            };
            return true;
        }
    private class APIStorage {
        private final String TWITTER_KEY = "9Wfs06IF2gRS7x7DnNiEBCmqZ";
        /**
         * Defines a private static of string for the twitter secret.
         */
        private final String TWITTER_SECRET = "ZycIA5Eyoet3zatWRuTsJ2yRDHUb4K2j7vpG2gIC1S2qZdcAh8";

        private final String WEATHER_KEY = "56fc4dd72aef4a05b9780638172802";
    }

    private class CurrentLocation implements LocationListener {
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

    private class WeatherCall extends Thread {
        private String weatherCallLocation;
        private TextView tv;

        public WeatherCall(String location, TextView tv) {
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
                    Log.d("LOCATION INFORMATION", json.getJSONObject("location").getString("name"));
                    Log.d("CURRENT TEMP", json.getJSONObject("current").getString("temp_c"));
                    Log.d("WEATHER INFORMATION: ", "" + stringBuilder.toString());

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                tv.setText(json.getJSONObject("current").getString("temp_c") + "°C\n" +
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



