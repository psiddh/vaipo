package app.com.vaipo;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.Publisher;
import com.opentok.android.Subscriber;

import java.util.ArrayList;

import app.com.vaipo.appState.Utils.Utils;
import app.com.vaipo.openTok.ITalkUICallbacks;
import app.com.vaipo.openTok.Talk;


public class BubbleVideoView extends Service implements ITalkUICallbacks {

    public static  int ID_NOTIFICATION = 2018;
    private WindowManager windowManager;

    private static String TAG = "VaipoView";

    private FrameLayout mSubscriberViewContainer;
    private FrameLayout mPublisherViewContainer;

    private Talk mTalk;
    // Spinning wheel for loading subscriber view
    private ProgressBar mLoadingSub;

    private View videoView;

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


    boolean mHasDoubleClicked = false;
    long lastPressTime;
    private Boolean _enable = true;

    ArrayList<String> myArray;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        String sessionId = intent.getStringExtra("sessionId");
        String token = intent.getStringExtra("token");

        mTalk = new Talk(this, sessionId, token);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        KeyguardManager myKM = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        videoView = inflater.inflate(R.layout.vaipo_view, null);

        mPublisherViewContainer = (FrameLayout) videoView.findViewById(R.id.publisher_container);
        mSubscriberViewContainer = (FrameLayout) videoView.findViewById(R.id.subscriber_container);

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                myKM.inKeyguardRestrictedInputMode() ? WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD ,
                PixelFormat.TRANSLUCENT);

        int width = getWidth(windowManager);
        int height = getHeight(windowManager);


        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = height/2;
        params.height = height/2;
        params.width = height/2;

        LinearLayout view1 = new LinearLayout(this);
        view1.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));


        windowManager.addView(videoView, params);
        try {
            videoView.setOnTouchListener(new View.OnTouchListener() {
                private WindowManager.LayoutParams paramsF = params;
                private int initialX;
                private int initialY;
                private float initialTouchX;
                private float initialTouchY;

                @Override public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:

                            // Get current time in nano seconds.
                            long pressTime = System.currentTimeMillis();


                            // If double click...
                            if (pressTime - lastPressTime <= 300) {
                                createNotification();
                                stopSelf();
                                mHasDoubleClicked = true;
                            }
                            else {     // If not double click....
                                mHasDoubleClicked = false;
                            }
                            lastPressTime = pressTime;
                            initialX = paramsF.x;
                            initialY = paramsF.y;
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();
                            break;
                        case MotionEvent.ACTION_UP:
                            break;
                        case MotionEvent.ACTION_MOVE:
                            paramsF.x = initialX + (int) (event.getRawX() - initialTouchX);
                            paramsF.y = initialY + (int) (event.getRawY() - initialTouchY);
                            windowManager.updateViewLayout(videoView, paramsF);
                            break;
                    }
                    return false;
                }
            });
        } catch (Exception e) {
            // TODO: handle exception
        }

        videoView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
            initiatePopupWindow(videoView);
            _enable = false;
            }
        });

        return flags;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String sessionId = prefs.getString("sessionId", "");

        // Register to receive messages.
        // We are registering an observer (mMessageReceiver) to receive Intents
        // with actions named "custom-event-name".
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(Utils.END_VAIPO_CALL));
        //mLoadingSub = (ProgressBar) findViewById(R.id.loadingSpinner);
    }


    private void initiatePopupWindow(View anchor) {
        try {
            Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            ListPopupWindow popup = new ListPopupWindow(this);
            popup.setAnchorView(anchor);
            popup.setWidth((int) (display.getWidth()/(1.5)));
            popup.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createNotification(){
        Intent notificationIntent = new Intent(getApplicationContext(), BubbleVideoView.class);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, notificationIntent, 0);

        Notification notification = new Notification(R.drawable.ic_navigation_arrow_forward, "Click to start launcher",System.currentTimeMillis());
        notification.setLatestEventInfo(getApplicationContext(), "Start launcher", "Click to start launcher", pendingIntent);
        notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_ONGOING_EVENT;

        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(ID_NOTIFICATION, notification);
    }

    @Override
    public void onDestroy() {
       Log.d("URL : ", "Service Destroyed");
        internalEnd();
        super.onDestroy();
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
        //mLoadingSub.setVisibility(View.GONE);

        if (publisher == null)
            return;
        // Add video preview
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        lp.gravity = Gravity.BOTTOM | Gravity.RIGHT;
        /*lp.x = 0;
        lp.y = height/2;
        lp.height = height/2;
        lp.width = height/2;*/
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
        internalEnd();
        stopSelf();
    }

    @Override
    public void attachSubscriberView(Subscriber subscriber) {

        //mLoadingSub.setVisibility(View.GONE);

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

    private void internalEnd() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        mTalk.stop();
        if (videoView != null) {
            windowManager.removeView(videoView);
            videoView = null;
        }
    }
    private int getWidth(WindowManager wm){
        int width=0;
        //WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        if(Build.VERSION.SDK_INT>12){
            Point size = new Point();
            display.getSize(size);
            width = size.x;
        }
        else{
            width = display.getWidth();  // Deprecated
        }
        return width;
    }

    private int getHeight(WindowManager wm){
        int height=0;
        //WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        if(Build.VERSION.SDK_INT>12){
            Point size = new Point();
            display.getSize(size);
            height = size.y;
        }
        else{
            height = display.getHeight();  // Deprecated
        }
        return height;
    }

}
