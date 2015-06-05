package app.com.vaipo;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;



public class VideoView extends Service {

    public static  int ID_NOTIFICATION = 2018;
    private String mUrl;

    private WindowManager windowManager;
    private WebView videoView;

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
        Bundle extras = intent.getExtras();

        if (intent.hasExtra("URL")){
            mUrl = intent.getStringExtra("URL");
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        videoView = new WebView(this);
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 100;
        params.height = 600;
        params.width = 600;

        LinearLayout view1 = new LinearLayout(this);
        view1.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));

        videoView .getSettings().setJavaScriptEnabled(true);
        videoView .getSettings().setDomStorageEnabled(true);
        videoView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        videoView.setWebChromeClient(new WebChromeClient() {

            @Override
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            public void onPermissionRequest(final PermissionRequest request) {
                Log.d("test", "test");
                request.grant(request.getResources());

            }

        });

        videoView.loadUrl(mUrl);
        //view1.addView(chatHead);
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
                //				Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                //				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                //				getApplicationContext().startActivity(intent);
            }
        });

        return flags;
    }

    @Override
    public void onCreate() {
        super.onCreate();
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
        Intent notificationIntent = new Intent(getApplicationContext(), VideoView.class);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, notificationIntent, 0);

        Notification notification = new Notification(R.drawable.ic_navigation_arrow_forward, "Click to start launcher",System.currentTimeMillis());
        notification.setLatestEventInfo(getApplicationContext(), "Start launcher" ,  "Click to start launcher", pendingIntent);
        notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_ONGOING_EVENT;

        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(ID_NOTIFICATION,notification);
    }

        @Override
    public void onDestroy() {
            Log.d("URL : ", "Service Destroyed");
        super.onDestroy();
        if (videoView != null) windowManager.removeView(videoView);
    }

}