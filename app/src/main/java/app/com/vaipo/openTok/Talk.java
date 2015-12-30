package app.com.vaipo.openTok;


import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.opentok.android.SubscriberKit;

import java.util.ArrayList;


public class Talk implements Session.SessionListener, PublisherKit.PublisherListener, SubscriberKit.SubscriberListener {
    private static String TAG = "Talk";
    private String mSessionId;
    private String mApiKey = "45425702";
    private String mApiSecret = "77abf733ad26b255a85453922fea62ebc1fee654";
    private String mToken = "T1==cGFydG5lcl9pZD00NTQyNTcwMiZzaWc9ZWY5ZmY1MjFmNWM4YTdkMGIzNDQxMzg3ZTU5NTdmYTM4MzU5NGJlNzpyb2xlPXB1Ymxpc2hlciZzZXNzaW9uX2lkPTJfTVg0ME5UUXlOVGN3TW41LU1UUTFNRGN5TXpneE5UWTFNSDQ1VkU5dmEwdHlSREpKVUdSekwxTlpRMjV0ZGpCaFdWaC1mZyZjcmVhdGVfdGltZT0xNDUwNzIzOTA2Jm5vbmNlPTAuMDU0NTM1NjA2NTAwMDM3NzUmZXhwaXJlX3RpbWU9MTQ1MTMyODQ1NyZjb25uZWN0aW9uX2RhdGE9";



    private Session mSession;
    private ArrayList<Stream> mStreams = new ArrayList<Stream>();

    private Subscriber mSubscriber;
    private Publisher mPublisher;

    private Context mContext;
    private ITalkUICallbacks mCallback;

    public Talk() {
    }


    public Talk(Activity context, String sessionId, String token) {
        mContext = context;
        mSessionId = sessionId;
        mToken = token;
        initializeSession(context);
        initializePublisher(context);

        mCallback = (ITalkUICallbacks) context;
    }


    private void initializeSession(Context context) {
        if (mSession == null) {
            mSession = new Session(context, mApiKey, mSessionId);
            mSession.setSessionListener(this);
            mSession.connect(mToken);
        }
    }

    private void initializePublisher(Context context) {
        mPublisher = new Publisher(context);
        mPublisher.setPublisherListener(this);
        mPublisher.getRenderer().setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE,
                BaseVideoRenderer.STYLE_VIDEO_FILL);

        if (mCallback != null)
            mCallback.addPublisherView(mPublisher);
    }

    public void pause() {
        if (mSession != null) {
            mSession.onPause();

            if (mCallback != null)
                mCallback.removeSubscribeView(mSubscriber);
        }
    }

    public void resume() {
        if (mSession != null) {
            mSession.onResume();
        }
    }

    public void stop() {
        if (mSession != null) {
            mSession.disconnect();
        }
    }

    /* Session Listener methods */

    @Override
    public void onConnected(Session session) {
        Log.d(TAG, "Session Connected");

        if (mPublisher != null) {
            mSession.publish(mPublisher);
        }

        if (mCallback != null)
            mCallback.addPreview(mPublisher);
    }

    @Override
    public void onDisconnected(Session session) {
        if (mCallback != null) {
            mCallback.removePublisherView(mPublisher);
            mCallback.removeSubscribeView(mSubscriber);
        }

        mPublisher = null;
        mSubscriber = null;
        mStreams.clear();
        mSession = null;

    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {

        Log.d(TAG, "Stream Received");

        if (mSubscriber == null) {
            mSubscriber = new Subscriber(mContext, stream);
            mSubscriber.setSubscriberListener(this);
            mSubscriber.getRenderer().setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE,
                    BaseVideoRenderer.STYLE_VIDEO_FILL);
            mSession.subscribe(mSubscriber);
        }

    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.d(TAG, "Stream Dropped");

        //if (mSubscriber != null) {
        if (mCallback != null)
            mCallback.removeAllSubscribeView(mSubscriber);

        mSubscriber = null;
        //}

    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        logOpenTokError(opentokError);
    }


    // PublisherKit.PublisherListener
    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {
        logOpenTokError(opentokError);
    }

    // SubscriberKit.SubscribeListener
    @Override
    public void onConnected(SubscriberKit subscriberKit) {
        Log.d(TAG, "Subscriber Connected");

        if (mCallback != null) {
            mCallback.addSubscribeView(mSubscriber);
        }
    }

    @Override
    public void onDisconnected(SubscriberKit subscriberKit) {

    }

    @Override
    public void onError(SubscriberKit subscriberKit, OpentokError opentokError) {
        logOpenTokError(opentokError);
    }

    private void logOpenTokError(OpentokError opentokError) {
        Log.e(TAG, "Error Domain: " + opentokError.getErrorDomain().name());
        Log.e(TAG, "Error Code: " + opentokError.getErrorCode().name());
    }
}
