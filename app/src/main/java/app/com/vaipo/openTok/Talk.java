package app.com.vaipo.openTok;


import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.util.Log;

import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.Connection;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.opentok.android.SubscriberKit;

import java.util.ArrayList;

import app.com.vaipo.CallStateHandler;
import app.com.vaipo.MainActivity;
import app.com.vaipo.Utils.Utils;


public class Talk implements Session.SessionListener, Session.ConnectionListener, PublisherKit.PublisherListener, SubscriberKit.SubscriberListener, Subscriber.VideoListener, Session.ReconnectionListener {

    private static String TAG = "TalkClass";

    private Session mSession;
    private ArrayList<Stream> mStreams = new ArrayList<Stream>();

    private Subscriber mSubscriber;
    private Publisher mPublisher;

    private Context mContext;
    private ITalkUICallbacks mCallback;
    private boolean mSessionStopInitiated = false;

    private boolean wasPublisherNotified = false;
    private boolean wasSubscriberNotified = false;

    public Talk() {
    }


    public Talk(Activity context, String apiKey, String sessionId, String token) {
        mContext = context;
        initializeSession(context, apiKey, sessionId, token);
        //initializePublisher(context);

        mCallback = (ITalkUICallbacks) context;
        mStreams = new ArrayList<Stream>();
    }

    public Talk(Service context, String apiKey, String sessionId, String token) {
        mContext = context;
        initializeSession(context, apiKey, sessionId, token);
        //initializePublisher(context);

        mCallback = (ITalkUICallbacks) context;
        mStreams = new ArrayList<Stream>();
    }


    private void initializeSession(Context context, String apiKey, String sessionId, String token) {
        if (mSession == null) {
            mSession = new Session(context, apiKey, sessionId);
            mSession.setSessionListener(this);
            mSession.setReconnectionListener(this);
            mSession.setConnectionListener(this);
            mSession.connect(token);
        }
    }

    private void initializePublisher(Context context) {
        mPublisher = new Publisher(context);
        mPublisher.setPublisherListener(this);
        if (CallStateHandler.mCall)
            mPublisher.setPublishAudio(false);
        mPublisher.getRenderer().setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE,
                BaseVideoRenderer.STYLE_VIDEO_FILL);

        //if (mCallback != null)
        //    mCallback.addPublisherView(mPublisher);
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
        if (mSessionStopInitiated)
            return;

        mSessionStopInitiated = true;

        if (mSession != null) {
            if (null != mPublisher)
                mSession.unpublish(mPublisher);
            if (null != mSubscriber)
                mSession.unsubscribe(mSubscriber);
            wasPublisherNotified = wasSubscriberNotified = false;
            mSession.onPause();
            mSession.disconnect();
        }
    }

    public void swap() {
        if (mCallback != null && mSession != null)
            mCallback.swapCamera(mPublisher);
    }

    public void mute() {
        if (mSession != null && mPublisher != null) {
            if (mPublisher != null) {
                mPublisher.setPublishAudio(!mPublisher.getPublishAudio());
            }
        }
    }

    public void notifyPublisher() {
        if (wasPublisherNotified) {
            Log.d(TAG, "Publisher Already notified - Ignore this!!!");
            return;
        }
        if (mCallback != null)
            mCallback.addPreview(mPublisher);

        if (mPublisher != null) {
            mSession.publish(mPublisher);
        }

        wasPublisherNotified = true;
    }

    public void notifySubscriber() {
        if (wasSubscriberNotified) {
            Log.d(TAG, "Subscriber Already notified - Ignore this!!!");
            return;
        }
        if (mCallback != null)
            mCallback.addSubscribeView(mSubscriber);
    }
    /* Session Listener methods */

    @Override
    public void onConnected(Session session) {
        Log.d(TAG, "Session Connected");
        mSessionStopInitiated = false;
        if (mPublisher != null) {
            //mSession.publish(mPublisher);
        }

        initializePublisher(mContext);
    }

    @Override
    public void onDisconnected(Session session) {
        Log.d(TAG, "Session DisConnected");

        mSessionStopInitiated = false;
        if (mCallback != null) {
            mCallback.removePublisherView(mPublisher);
            mCallback.removeSubscribeView(mSubscriber);
        }

        mPublisher = null;
        mSubscriber = null;
        mStreams.clear();
        mSession = null;

        if (mCallback != null)
            mCallback.end();
        wasPublisherNotified = wasSubscriberNotified = false;


    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {

        Log.d(TAG, "Stream Received");
        /*mStreams.add(stream);

        if (mSubscriber == null) {
            subscribeToStream(stream);
        }*/
        if (mSubscriber == null) {
            mSubscriber = new Subscriber(mContext, stream);
            mSubscriber.setSubscriberListener(this);
            mSubscriber.getRenderer().setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE,
                    BaseVideoRenderer.STYLE_VIDEO_FILL);
            mSession.subscribe(mSubscriber);
            wasSubscriberNotified = true;
        }

    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.d(TAG, "Stream Dropped");

        /*mStreams.remove(stream);

        if (mSubscriber != null) {
            unsubscribeFromStream(stream);
        }*/
        //if (mSubscriber != null) {
        if (mCallback != null)
            mCallback.removeAllSubscribeView(mSubscriber);

        //mSubscriber = null;
        //}

    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        logOpenTokError(opentokError);
    }


    // PublisherKit.PublisherListener
    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {
        Log.d(TAG, "Stream Created");

        /*mStreams.add(stream);
        if (mSubscriber == null) {
            subscribeToStream(stream);
        }*/
    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {
        Log.d(TAG, "Stream Destroyed");
        //unsubscribeFromStream(stream);
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
        Log.d(TAG, "Subscriber Disconnected");

        if (mCallback != null) {
            mCallback.end();
        }
    }

    @Override
    public void onError(SubscriberKit subscriberKit, OpentokError opentokError) {
        logOpenTokError(opentokError);
    }

    private void logOpenTokError(OpentokError opentokError) {
        Log.e(TAG, "Error Domain: " + opentokError.getErrorDomain().name());
        Log.e(TAG, "Error Code: " + opentokError.getErrorCode().name());
    }

    private void subscribeToStream(Stream stream) {
        mSubscriber = new Subscriber(mContext, stream);
        mSubscriber.setVideoListener(this);
        mSession.subscribe(mSubscriber);

        if (mSubscriber.getSubscribeToVideo()) {
            // start loading spinning
            // mLoadingSub.setVisibility(View.VISIBLE);
        }
    }

    private void unsubscribeFromStream(Stream stream) {
        mStreams.remove(stream);
        if (mSubscriber.getStream().equals(stream)) {
            if (mCallback != null)
                mCallback.removeSubscribeView(mSubscriber);
            mSubscriber = null;
            if (!mStreams.isEmpty()) {
                subscribeToStream(mStreams.get(0));
            }
        }

    }

    @Override
    public void onVideoDataReceived(SubscriberKit subscriberKit) {
        Log.d(TAG, "Video Received - Subscriber");

        notifySubscriber();

    }

    @Override
    public void onVideoDisabled(SubscriberKit subscriberKit, String reason) {
        Log.d(TAG, "Video Disabled - Subscriber " + reason);

    }

    @Override
    public void onVideoEnabled(SubscriberKit subscriberKit, String reason) {
        Log.d(TAG, "Video Enabled - Subscriber " + reason);


    }

    @Override
    public void onVideoDisableWarning(SubscriberKit subscriberKit) {
        Log.d(TAG, "Video Disabled Warning - Subscriber");

    }

    @Override
    public void onVideoDisableWarningLifted(SubscriberKit subscriberKit) {
        Log.d(TAG, "Video Disabled Warning Lifted - Subscriber");

    }


    @Override
    public void onConnectionCreated(Session session, Connection connection) {
        Log.d(TAG, "Session Connection Created!");

    }

    @Override
    public void onConnectionDestroyed(Session session, Connection connection) {

        Log.d(TAG, "Session Connection Destroyed!");
        if (mCallback != null)
            mCallback.end();

    }

    @Override
    public void onReconnecting(Session session) {
        Log.d(TAG, "Session Reconnecting!");

    }

    @Override
    public void onReconnected(Session session) {
        Log.d(TAG, "Session Reconnected!");

    }
}
