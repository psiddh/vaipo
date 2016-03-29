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
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListPopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.Publisher;
import com.opentok.android.Subscriber;

import app.com.vaipo.appState.AppState;
import app.com.vaipo.Utils.Utils;
import app.com.vaipo.format.JsonFormatter;
import app.com.vaipo.messages.DialMsg;
import app.com.vaipo.messages.UserMsg;
import app.com.vaipo.openTok.ITalkUICallbacks;
import app.com.vaipo.openTok.Talk;
import app.com.vaipo.rest.RestAPI;


public class BubbleVideoView extends Service implements ITalkUICallbacks {

    public static  int ID_NOTIFICATION = 2018;
    private WindowManager windowManager;

    private static String TAG = "VaipoView";

    private FrameLayout mSubscriberViewContainer;
    private FrameLayout mPublisherViewContainer;
    private FrameLayout mParentcontainer;

    private Talk mTalk = null;
    // Spinning wheel for loading subscriber view
    private ProgressBar mLoadingSub;

    private View videoView;

    private int mHeight, mWidth = 0;

    private boolean mUserAck = false;
    private boolean mReceiveAck = false;


    private Button mButtonYes;
    private Button  mButtonNo;
    private TextView mTextView;
    private RelativeLayout mViewImgLayout;
    private ProgressBar mProgressBar;

    private UserMsg mUsrAckMsg = new UserMsg();
    private AppState mAppState;
    private RestAPI mRestAPI = new RestAPI();
    private JsonFormatter mFormatter = new JsonFormatter();

    public static boolean flag = false;

    private static boolean NOTIFICATION_SUPPORT = true;
    private int mNotifyId = 963;

    String mSessionId = "";
    String mToken = "";
    String mApiKey = "";
    boolean mPeerDiscover = false;

    public static BroadcastReceiver mActionListener;

    // Our handler for received Intents. This will be called whenever an Intent
    // with an action named "end-vaipo-call" is broadcasted.
    public BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Utils.END_VAIPO_CALL)) {
                end();
            } else if (action.equals(Utils.RECEIVE_USER_ACK)) {
                if (mUserAck == false) {
                    String incoming_vid_req = getResources().getString(R.string.incoming_vid_req);

                    if (NOTIFICATION_SUPPORT) {
                        showNotification(incoming_vid_req, true, true, true, "Accept", "Decline");
                    } else {
                        mTextView.setText(incoming_vid_req);
                    }

                } else {

                    mParentcontainer.setVisibility(View.VISIBLE);
                    if (NOTIFICATION_SUPPORT) {
                        addVideoView();
                    } else {
                        mViewImgLayout.setVisibility(View.GONE);
                    }

                    if (mTalk == null)
                        mTalk = new Talk(BubbleVideoView.this, mApiKey, mSessionId, mToken );
                    mTalk.notifyPublisher();
                    mTalk.notifySubscriber();
                }
                mReceiveAck = true;

            }
        }
    };


    boolean mHasDoubleClicked = false;
    long lastPressTime;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {

        String sessionId = intent.getStringExtra("sessionId");
        String token = intent.getStringExtra("token");
        String apiKey = intent.getStringExtra("apikey");
        boolean peerDiscover = intent.getBooleanExtra("peerautodiscover", false);

        if (isSameOpenTokSession(sessionId, token, apiKey)) {
            Log.d(TAG, "Session Initiated. Ignore duplicate request with same session " + startId);
            return flags;
        }

        if (flag) {
            Log.d(TAG, "Start UI with startId - return " + startId);
            return flags;
        }

        mSessionId = sessionId;
        mToken = token;
        mApiKey = apiKey;
        mPeerDiscover = peerDiscover;
        Log.d(TAG, "Start UI with startId " + startId);

        mTalk = new Talk(BubbleVideoView.this, mApiKey, mSessionId, mToken );
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        setupUI();

        mButtonYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleYes();

            }
        });
        mButtonNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleNo();
            }
        });

        if (NOTIFICATION_SUPPORT) {
            if (mPeerDiscover) {
                //String wait_for_other_party = getResources().getString(R.string.wait_for_other_party);
                //showNotification(wait_for_other_party, false, true, false,"", "End");
                handleYes();
            } else {
                String enable_video = getResources().getString(R.string.enable_video);
                showNotification(enable_video, true, true, true, "Accept", "Decline");
            }
        } else {
            addVideoView();
        }

        flag= true;
        return flags;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String sessionId = prefs.getString("sessionId", "");

        mAppState = (AppState) this.getApplicationContext();
        // Register to receive messages.
        // We are registering an observer (mMessageReceiver) to receive Intents
        // with actions named "custom-event-name".
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(Utils.END_VAIPO_CALL));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(Utils.RECEIVE_USER_ACK));

        // TBD: secure this broadcast message!
        mActionListener = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "OnReceive " + intent.getAction());
                if (intent.getAction().equalsIgnoreCase(Utils.ACTION_YES)) {
                    handleYes();
                } else if (intent.getAction().equalsIgnoreCase(Utils.ACTION_NO)) {
                    handleNo();
                }
            }
        };

        this.registerReceiver(mActionListener, new IntentFilter(Utils.ACTION_YES));
        this.registerReceiver(mActionListener, new IntentFilter(Utils.ACTION_NO));
    }

    private void setupUI() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        if (NOTIFICATION_SUPPORT) {
            View uiConfView = inflater.inflate(R.layout.userconfirmation_view, null);
            mButtonYes = (Button) uiConfView.findViewById(R.id.imgYes);
            mButtonNo = (Button) uiConfView.findViewById(R.id.imgNo);
            mViewImgLayout = (RelativeLayout) uiConfView.findViewById(R.id.viewImg);
            mTextView = (TextView) uiConfView.findViewById(R.id.textView);

            videoView = inflater.inflate(R.layout.video_view, null);
            mPublisherViewContainer = (FrameLayout) videoView.findViewById(R.id.publisher_container);
            mSubscriberViewContainer = (FrameLayout) videoView.findViewById(R.id.subscriber_container);
            mParentcontainer = (FrameLayout) videoView.findViewById(R.id.parentcontainer);
            mProgressBar = (ProgressBar) videoView.findViewById(R.id.progressbar);
        } else {
            videoView = inflater.inflate(R.layout.vaipo_view, null);
            mButtonYes = (Button) videoView.findViewById(R.id.imgYes);
            mButtonNo = (Button) videoView.findViewById(R.id.imgNo);
            mViewImgLayout = (RelativeLayout) videoView.findViewById(R.id.viewImg);
            mTextView = (TextView) videoView.findViewById(R.id.textView);

            mPublisherViewContainer = (FrameLayout) videoView.findViewById(R.id.publisher_container);
            mSubscriberViewContainer = (FrameLayout) videoView.findViewById(R.id.subscriber_container);
            mParentcontainer = (FrameLayout) videoView.findViewById(R.id.parentcontainer);
            mProgressBar = (ProgressBar) videoView.findViewById(R.id.progressbar);
        }
    }

    private void addVideoView() {
        if (videoView.isShown()) {
            Log.d(TAG, "VideoView already added - return!");
            return;
        }

        KeyguardManager myKM = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                myKM.inKeyguardRestrictedInputMode() ? WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD ,
                PixelFormat.TRANSLUCENT);

        mWidth = getWidth(windowManager);
        mHeight  = getHeight(windowManager);

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = mHeight/2;
        params.height = mHeight/2;
        params.width = mHeight/2;

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
                                //createNotification();
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
                            if (videoView != null)
                                windowManager.updateViewLayout(videoView, paramsF);
                            break;
                    }
                    return false;
                }
            });
        } catch (Exception e) {
            // TODO: handle exception
        }

        Log.d(TAG, "VideoView Added !");

    }

    private void removeVideoView() {
        if (videoView != null && videoView.isShown()) {
            windowManager.removeView(videoView);
            videoView = null;
        }
    }

    private void initiatePopupWindow(View anchor) {
        try {
            Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            ListPopupWindow popup = new ListPopupWindow(this);
            popup.setAnchorView(anchor);
            popup.setWidth((int) (getWidth(windowManager)/(1.5)));
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
       flag = mPeerDiscover = false;
       Log.d(TAG, "Service Destroyed");
        mUserAck = false;
        try {
            mFormatter.destroy();
        } catch (Exception ex) {

        }
        internalEnd();
        //mTalk = null;
        super.onDestroy();
    }

    @Override
    public void addSubscribeView(Subscriber subscriber) {
        if (!mUserAck) {
            return;
        }
        if (NOTIFICATION_SUPPORT)
            cancelNotification();
        if (subscriber != null) {
            mProgressBar.setVisibility(View.GONE);
            mSubscriberViewContainer.removeView(subscriber.getView());
            mSubscriberViewContainer.addView(subscriber.getView());
            //mSubscriberViewContainer.setPadding(3,3,3,3);
        }
        //mParentcontainer.setBackgroundResource(R.drawable.bubbleview_bg);
    }

    @Override
    public void removeSubscribeView(Subscriber subscriber) {
        mProgressBar.setVisibility(View.GONE);
        if (subscriber != null)
            mSubscriberViewContainer.removeView(subscriber.getView());
    }

    @Override
    public void removeAllSubscribeView(Subscriber subscriber) {
        mProgressBar.setVisibility(View.GONE);
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

        if (!mUserAck)
            return;
        if (publisher == null)
            return;

        if (NOTIFICATION_SUPPORT)
            cancelNotification();

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(mHeight / 8, mHeight / 8);
        mPublisherViewContainer.removeView(publisher.getView());
        mPublisherViewContainer.addView(publisher.getView(), lp);
        //mPublisherViewContainer.setBackgroundResource(R.color.publisher_border_color);
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
        mProgressBar.setVisibility(View.GONE);
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
        try {
            this.unregisterReceiver(mActionListener);
        } catch (Exception e) {
            Log.d(TAG , e.getMessage() + " - ");
        }
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        if (mTalk != null)
            mTalk.stop();
        if (videoView != null) {
            removeVideoView();
        }
        if (NOTIFICATION_SUPPORT) {
            cancelNotification();
        }
        mPeerDiscover = false;
        mAppState.setCallee("");
        mAppState.setCaller("");
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

    private Drawable getBGDrawable(int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return getResources().getDrawable(id, this.getTheme());
        } else {
            return getResources().getDrawable(id);
        }
    }

    private void showNotification(String update, boolean showYes, boolean showNo, boolean useDefault, String yesButtonText, String noButtonText) {

        //this is the intent that is supposed to be called when the
        //button is clicked
        Intent yesIntent = new Intent(Utils.ACTION_YES);//this, BubbleVideoView.class);
        //yesIntent.setClass(BubbleVideoView.this, BroadcastReceiver.class);
        PendingIntent yesPendingIntent = PendingIntent.getBroadcast(this, 0,
                yesIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        Intent noIntent = new Intent(Utils.ACTION_NO);
        //noIntent.setClass(BubbleVideoView.this, mActionListener.getClass());
        PendingIntent noPendingIntent = PendingIntent.getBroadcast(this, 0,
                noIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        Intent notifyIntent = new Intent(getApplicationContext(), ActivityDialog.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent clickIntent = PendingIntent.getActivity(BubbleVideoView.this, 51,
                notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(ns);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_vaipo)
                .setContentTitle(update)
                .setCategory(Notification.CATEGORY_CALL)
                .setPriority(Notification.PRIORITY_MAX)
                .setContentIntent(clickIntent)
                .setOngoing(true)
                .setAutoCancel(false)
                .setWhen(0);
        if (showYes)
            mBuilder.addAction(new NotificationCompat.Action(R.drawable.ic_yes,
                    yesButtonText, yesPendingIntent));
        if (showNo)
            mBuilder.addAction(new NotificationCompat.Action(R.drawable.ic_no,
                    noButtonText, noPendingIntent));

        //if (useDefault)
        //    mBuilder.setDefaults(Notification.DEFAULT_ALL);

        // Sets a title for the Inbox in expanded layout
        mBuilder.setStyle(new NotificationCompat.BigTextStyle(mBuilder)
        .bigText("Vaipo"));


        notificationManager.notify(mNotifyId, mBuilder.build());
    }

    private void cancelNotification() {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.cancel(mNotifyId);

        try {
            this.unregisterReceiver(mActionListener);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }


    private void handleYes() {
        sendAck(true);
        mButtonYes.setVisibility(View.GONE);
        mButtonNo.setVisibility(View.GONE);
        if (mReceiveAck) {
            if (NOTIFICATION_SUPPORT) {
                addVideoView();
            } else {
                mViewImgLayout.setVisibility(View.GONE);
            }
            mParentcontainer.setVisibility(View.VISIBLE);
            //mProgressBar.setVisibility(View.VISIBLE);

            if (mTalk == null)
                mTalk = new Talk(BubbleVideoView.this, mApiKey, mSessionId, mToken );

            mTalk.notifyPublisher();
            mTalk.notifySubscriber();
        } else {
            if (getResources() == null)
                return;
            String wait_for_other_party = getResources().getString(R.string.wait_for_other_party);
            if (NOTIFICATION_SUPPORT) {
                showNotification(wait_for_other_party, false, true, false,"", "End");
            } else {
                mTextView.setText(wait_for_other_party);
            }
        }

    }

    private void handleNo() {
        boolean test = true;
        sendAck(false);
        mViewImgLayout.setVisibility(View.GONE);
        if (NOTIFICATION_SUPPORT) {
            cancelNotification();
        }
        Utils.endVaipoCall(BubbleVideoView.this);
        //sendEndMsg();
    }

    private void sendAck(boolean isYes) {
        mUserAck = isYes;
        mUsrAckMsg.setId(mAppState.getID());
        mUsrAckMsg.setCaller(mAppState.getCaller());
        mUsrAckMsg.setCallee(mAppState.getCallee());
        mUsrAckMsg.setAck(mUserAck);
        mFormatter.initialize();
        mRestAPI.call(RestAPI.USERACK, mFormatter.get(mUsrAckMsg), null);
    }

    private void sendEndMsg() {
        DialMsg message = new DialMsg();
        message.setId(mAppState.getID());
        message.setCallee(mAppState.getCallee());
        message.setCaller(mAppState.getCaller());
        message.setState(DialMsg.END);
        mFormatter.destroy();
        mFormatter.initialize();
        mRestAPI.call(RestAPI.CALL, mFormatter.get(message), null);
    }

    private boolean isSameOpenTokSession(String sessionId, String token, String apikKey) {
        return mSessionId.equals(sessionId) && mToken.equals(token) && mApiKey.equals(apikKey);
    }

}
