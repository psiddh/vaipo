package app.com.vaipo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;


import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.Publisher;
import com.opentok.android.Subscriber;


import app.com.vaipo.appState.Utils.Utils;
import app.com.vaipo.openTok.ITalkUICallbacks;
import app.com.vaipo.openTok.Talk;


public class VaipoView extends Activity implements ITalkUICallbacks {

    private static String TAG = "VaipoView";

    private FrameLayout mSubscriberViewContainer;
    private FrameLayout mPublisherViewContainer;

    private Talk mTalk;
    // Spinning wheel for loading subscriber view
    private ProgressBar mLoadingSub;

    // Our handler for received Intents. This will be called whenever an Intent
    // with an action named "end-vaipo-call" is broadcasted.
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            //String message = intent.getStringExtra("message");
            //Log.d("receiver", "Got message: " + message);
            end();
        }
    };

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
        String apiKey = intent.getStringExtra("apikey");


        mTalk = new Talk(this, sessionId, token, apiKey);

        // Register to receive messages.
        // We are registering an observer (mMessageReceiver) to receive Intents
        // with actions named "custom-event-name".
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(Utils.END_VAIPO_CALL));
        //mLoadingSub = (ProgressBar) findViewById(R.id.loadingSpinner);

    }

    @Override
    public void onPause() {
        super.onPause();
        //mTalk.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        //mTalk.resume();
    }

    @Override
    public void onStop() {
        super.onStop();
        //mTalk.stop();
    }

    @Override
    protected void onDestroy() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        mTalk.stop();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        end();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_vaipoview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_swap) {
            if (mTalk != null)
                mTalk.swap();
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
            attachPublisherView(publisher);
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

    @Override
    public void addPreview(Publisher publisher) {
        mLoadingSub.setVisibility(View.GONE);

        if (publisher == null)
            return;
        // Add video preview
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        mPublisherViewContainer.addView(publisher.getView(), lp);
        publisher.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
    }

    @Override
    public void swapCamera(Publisher publisher) {
        if (publisher != null) {
            publisher.swapCamera();
        }
    }

    @Override
    public void end() {
        mTalk.stop();
        finish();
    }

    @Override
    public void attachSubscriberView(Subscriber subscriber) {

        mLoadingSub.setVisibility(View.GONE);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                getResources().getDisplayMetrics().widthPixels, getResources()
                .getDisplayMetrics().heightPixels);
        mSubscriberViewContainer.removeView(subscriber.getView());
        mSubscriberViewContainer.addView(subscriber.getView(), layoutParams);
        subscriber.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE,
                BaseVideoRenderer.STYLE_VIDEO_FILL);
    }

    @Override
    public void attachPublisherView(Publisher publisher) {
        publisher.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE,
                BaseVideoRenderer.STYLE_VIDEO_FILL);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                320, 240);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,
                RelativeLayout.TRUE);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,
                RelativeLayout.TRUE);
        layoutParams.bottomMargin = dpToPx(8);
        layoutParams.rightMargin = dpToPx(8);
        mPublisherViewContainer.addView(publisher.getView(), layoutParams);
    }

    /**
     * Converts dp to real pixels, according to the screen density.
     *
     * @param dp A number of density-independent pixels.
     * @return The equivalent number of real pixels.
     */
    private int dpToPx(int dp) {
        double screenDensity = this.getResources().getDisplayMetrics().density;
        return (int) (screenDensity * (double) dp);
    }
}
