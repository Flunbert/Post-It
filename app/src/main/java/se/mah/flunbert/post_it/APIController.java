package se.mah.flunbert.post_it;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.*;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;

import android.content.Intent;
import android.media.Image;

import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

import org.mortbay.util.ajax.JSON;

import io.fabric.sdk.android.Fabric;
import retrofit2.Call;

/**
  0       * APIController class.
        *
        * @author Björn Svensson
        * @since 23/2/2017
        */
public class APIController {
    private APIStorage apiStorage;
    private Controller controller;

    public APIController(Controller controller){
        this.controller = controller;
        this.apiStorage = new APIStorage();
    }



    public boolean RegisterAPI(Enum theEnum){
        TwitterAuthConfig authConfig = new TwitterAuthConfig(apiStorage.TWITTER_KEY, apiStorage.TWITTER_SECRET);
        //Fabric.with(this, new Twitter(authConfig));
        return false;
    }

    public boolean DeregisterAPI(Enum theEnum){
        return false;
    }

    public JSON FetchAPI(Enum theEnum){
        return null;
    }

    public JSON SendToAPI(Enum theEnum, Image image){
        return null;
    }


    private void sendTweet(String tweetText) {
        TwitterSession session = Twitter.getSessionManager().getActiveSession();
        TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient(session);
        StatusesService service = twitterApiClient.getStatusesService();

        Call<Tweet> call = service.update(tweetText, null, null, null, null, null, null, null, null);
        call.enqueue(new Callback<Tweet>() {

            /**
             * If sucess it procedes to send the result as a tweet.
             * @param result
             */
            @Override
            public void success(Result<Tweet> result) {
                Tweet tweet = result.data;
                Log.e("TwitterResult", tweet.text);
            }

            /**
             * If fail, an exception is called upon and error message is displayed.
             * @param exception
             */
            public void failure(TwitterException exception) {
                Log.e("TwitterException", exception.getMessage());
            }
        });
    }
    private boolean AuthorizeTwitter() {
        TwitterAuthConfig authConfig = new TwitterAuthConfig(apiStorage.TWITTER_KEY, apiStorage.TWITTER_SECRET);
        Fabric.with(controller.getActivity(), new Twitter(authConfig));
        return false;
    }

    private void UploadPicture(){

    }

}


class APIStorage{
    protected static final String TWITTER_KEY = "9Wfs06IF2gRS7x7DnNiEBCmqZ";
    /**
     * Defines a private static of string for the twitter secret.
     */
    protected static final String TWITTER_SECRET = "ZycIA5Eyoet3zatWRuTsJ2yRDHUb4K2j7vpG2gIC1S2qZdcAh8";
}


