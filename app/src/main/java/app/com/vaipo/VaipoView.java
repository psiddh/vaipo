package app.com.vaipo;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.FrameLayout;



import com.opentok.android.Publisher;
import com.opentok.android.Subscriber;


import app.com.vaipo.openTok.ITalkUICallbacks;
import app.com.vaipo.openTok.Talk;


public class VaipoView extends Activity implements ITalkUICallbacks {

    private static String TAG = "VaipoView";

    private FrameLayout mSubscriberViewContainer;
    private FrameLayout mPublisherViewContainer;

    private Talk mTalk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vaipo_view);

        mPublisherViewContainer = (FrameLayout)findViewById(R.id.publisher_container);
        mSubscriberViewContainer = (FrameLayout)findViewById(R.id.subscriber_container);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String sessionId = prefs.getString("sessionId", "");

        Intent intent = getIntent();
        sessionId = intent.getStringExtra("sessionId");
        String token = intent.getStringExtra("token");

        mTalk = new Talk(this, sessionId, token);


    }

    @Override
    public void onPause() {
        super.onPause();
        mTalk.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mTalk.resume();
    }

    @Override
    public void onStop() {
        super.onStop();
        mTalk.stop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void addSubscribeView(Subscriber subscriber) {
        mSubscriberViewContainer.addView(subscriber.getView());
    }

    @Override
    public void removeSubscribeView(Subscriber subscriber) {
        mSubscriberViewContainer.removeView(subscriber.getView());
    }

    @Override
    public void removeAllSubscribeView(Subscriber subscriber) {
        mSubscriberViewContainer.removeAllViews();
    }

    @Override
    public void addPublisherView(Publisher publisher) {
        mPublisherViewContainer.addView(publisher.getView());
    }

    @Override
    public void removePublisherView(Publisher publisher) {
        mPublisherViewContainer.removeView(publisher.getRenderer().getView());
    }

    @Override
    public void removeAllPublisherView(Publisher publisher) {
        mPublisherViewContainer.removeAllViews();
    }
}
