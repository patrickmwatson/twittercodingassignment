package com.example.patrickwatson.twittercodingassignment;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import io.fabric.sdk.android.Fabric;

public class LoginActivity extends AppCompatActivity implements Animation.AnimationListener {


    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
//    private static final String TWITTER_KEY = "ZtSZLnFx6rzoLuISy6kLEcdUd";
//    private static final String TWITTER_SECRET = "LUQ8em0ipspKCOFdoJ87zeSbEZNUv9q3onO3CASdY4dwm0JiGS";

//    private static final String TWITTER_KEY = "jpWyFrMNp6xaJmRq6ARlyniKy";
//    private static final String TWITTER_SECRET = "FAbk5ilSjAXkDRjmVFTfZo3rpqo7pkaaQQebnsQ8ou4NmdPmBG";

    private static final String TWITTER_KEY = "kknyX6zwr8j9IaIJ1GAZ4s7iQ";
    private static final String TWITTER_SECRET = "JeC3uGwVJJ8BphUA4oQjzbb6LL7dxce4I5TGYGocOSJeSz552T";

    private TwitterLoginButton loginButton;

    // Animation
    Animation animZonein;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));

        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        loginButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
        loginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                // The TwitterSession is also available through:
                // Twitter.getInstance().core.getSessionManager().getActiveSession()
                TwitterSession session = result.data;
                // TODO: Remove toast and use the TwitterSession's userID
                // with your app's user model
                String msg = "@" + session.getUserName() + " logged in! (#" + session.getUserId() + ")";
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                Intent mainActivity=new Intent(LoginActivity.this,TwitterCodingAssignment.class);
                startActivity(mainActivity);

            }
            @Override
            public void failure(TwitterException exception) {
                Log.d("TwitterKit", "Login with Twitter failure", exception);
            }
        });
        ImageView imageView=(ImageView)findViewById(R.id.imageView);
        imageView.setColorFilter(getResources().getColor(R.color.colorPrimaryDark), PorterDuff.Mode.LIGHTEN);
        // load the animation
        animZonein = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.zoom_in);

        // set animation listener
        animZonein.setAnimationListener(this);
        imageView.startAnimation(animZonein);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result to the login button.
        loginButton.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {

    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
}
