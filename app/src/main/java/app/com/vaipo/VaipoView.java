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
import android.widget.RelativeLayout;


import com.opentok.android.BaseVideoRenderer;
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
        if (subscriber != null)
            mSubscriberViewContainer.addView(subscriber.getView());
    }

    @Override
    public void removeSubscribeView(Subscriber subscriber) {
        if (subscriber != null)
            mSubscriberViewContainer.removeView(subscriber.getView());
    }

    @Override
    public void removeAllSubscribeView(Subscriber subscriber) {
        if (subscriber != null)
            mSubscriberViewContainer.removeAllViews();
    }

    @Override
    public void addPublisherView(Publisher publisher) {
        if (publisher != null)
            mPublisherViewContainer.addView(publisher.getView());
    }

    @Override
    public void removePublisherView(Publisher publisher) {
        if (publisher != null)
            mPublisherViewContainer.removeView(publisher.getRenderer().getView());
    }

    @Override
    public void removeAllPublisherView(Publisher publisher) {
        if (publisher != null)
            mPublisherViewContainer.removeAllViews();
    }

    public void addPreview(Publisher publisher) {
        if (publisher == null)
            return;
        // Add video preview
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        mPublisherViewContainer.addView(publisher.getView(), lp);
        publisher.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
    }
}
