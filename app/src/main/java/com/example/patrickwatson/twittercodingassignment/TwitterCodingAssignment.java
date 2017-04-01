package com.example.patrickwatson.twittercodingassignment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import com.mancj.materialsearchbar.MaterialSearchBar;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.models.Search;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.SearchService;
import com.twitter.sdk.android.tweetui.CompactTweetView;
import com.twitter.sdk.android.tweetui.SearchTimeline;
import com.twitter.sdk.android.tweetui.TweetTimelineListAdapter;
import com.twitter.sdk.android.tweetui.TweetUtils;
import com.twitter.sdk.android.tweetui.TweetView;
import com.twitter.sdk.android.tweetui.UserTimeline;

import io.fabric.sdk.android.Fabric;
import retrofit2.Call;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class TwitterCodingAssignment extends AppCompatActivity
        implements  MaterialSearchBar.OnSearchActionListener {

    private String Search_result_type = "recent";
    private int Search_count = Integer.MAX_VALUE;
    private long maxId;
    private LinearLayout tweetsList;
    private MaterialSearchBar searchBar;
    private List<String> list;
    private Timer timer;
    private ProgressBar progressBar;
    private HashMap<String,Integer> usersHash;//<name,followers_count>
    private int mDelay = 0;
    private int mInterval = 10000; // 10 seconds by default, can be changed later
    private CharSequence searchTerm;
    private Toolbar toolbar;
    private boolean refreshDisabled=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twitter_coding_assignment);

        tweetsList=(LinearLayout)findViewById(R.id.tweetsList);
        progressBar=(ProgressBar) findViewById(R.id.progressBar);
        toolbar = (Toolbar) findViewById(R.id.toolbar);


        setSupportActionBar(toolbar);


        //Initialize Search Bar
        searchBar = (MaterialSearchBar) findViewById(R.id.searchBar);
        searchBar.setHint("#applicaster");
        searchBar.setOnSearchActionListener(this);
        searchBar.setSpeechMode(true);
        searchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Cancel continuous refresh when typing starts
                if(s.length()>0&&timer!=null){
                    timer.cancel();
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //Set previous search terms from preferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> set=preferences.getStringSet("list",null);
        if(set!=null&&set.size()>0){
            list = new ArrayList<String>(set);
        }else{
            list = new ArrayList<String>();
        }

        searchBar.setLastSuggestions(list);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_twitter_coding_assignment, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            refreshDisabled=!refreshDisabled;
            if(refreshDisabled){
                Toast.makeText(this,"Auto-Refresh Disabled", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this,"Auto-Refresh Enabled", Toast.LENGTH_SHORT).show();
            }
            timer.cancel();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSearchStateChanged(boolean enabled) {
        String s = enabled ? "enabled" : "disabled";
    }

    @Override
    public void onSearchConfirmed(CharSequence text) {
        scheduleRefreshSearch();
    }

    public void scheduleRefreshSearch(){

        searchTerm=searchBar.getText().toString();
        searchBar.disableSearch();//Just clears the text from the material design bar

        if(!list.contains(searchTerm.toString())){
            list.add(searchTerm.toString());

        }
        searchBar.hideSuggestionsList();
        searchBar.clearFocus();
        saveSearches();

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask()
        {
            public void run()
            {
                runOnUiThread (new Thread(new Runnable() {
                    public void run() {
                        performSearch(searchTerm);
                        if(refreshDisabled){
                            timer.cancel();
                        }
                    }
                }));
            }
        }, mDelay, mInterval);
    }

    @Override
    public void onButtonClicked(int buttonCode) {
        switch (buttonCode){
            case MaterialSearchBar.BUTTON_NAVIGATION:
                break;
            case MaterialSearchBar.BUTTON_SPEECH:
        }
    }


    public void performSearch(CharSequence text){
        progressBar.setVisibility(View.VISIBLE);
        tweetsList.removeAllViews();

        SearchService service=Twitter.getApiClient().getSearchService();
        Call<Search> call = service.tweets(text.toString(),null,null,null,Search_result_type,Search_count,null,null,maxId,true);
        call.enqueue(new Callback<Search>() {
            @Override
            public void success(Result<Search> result) {

                setProgressBarIndeterminateVisibility(false);
                final List<Tweet> tweets = result.data.tweets;

                usersHash=new HashMap<String, Integer>();

                for (Tweet tweet : tweets) {
                    usersHash.put(tweet.user.screenName,tweet.user.followersCount);
                }
                Object[] a = usersHash.entrySet().toArray();
                Arrays.sort(a, new Comparator() {
                    public int compare(Object o1, Object o2) {
                        return ((Map.Entry<String, Integer>) o2).getValue()
                                .compareTo(((Map.Entry<String, Integer>) o1).getValue());
                    }
                });

                int count=0;
                for(Object obj: a){
                    if(count>9){
                        break;
                    }
                    count++;
                    Map.Entry entry=(Map.Entry)obj;
                    String name=(String)entry.getKey();

                    final UserTimeline userTimeline = new UserTimeline.Builder()
                            .screenName(name)
                            .maxItemsPerRequest(1)
                            .build();

                    final TweetTimelineListAdapter adapter = new TweetTimelineListAdapter.Builder(TwitterCodingAssignment.this)
                            .setViewStyle(R.style.tw__TweetLightWithActionsStyle)
                            .setTimeline(userTimeline)
                            .build();


                    ListView lv =new ListView(TwitterCodingAssignment.this);
                    lv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));

                    tweetsList.addView(lv);
                    lv.setAdapter(adapter);

                }

                if (tweets.size() > 0) {
                    maxId = tweets.get(tweets.size() - 1).id - 1;
                } else {
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void failure(TwitterException exception) {
            }
        });

    }


    public void saveSearches(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        Set<String> alphaSet = new HashSet<String>(list);
        editor.putStringSet("list",alphaSet);
        editor.apply();
    }

}
