package app.com.vaipo.fire;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import app.com.vaipo.BubbleVideoView;
import app.com.vaipo.CallStateHandler;
import app.com.vaipo.appState.Utils.Utils;

/**
 * Created by siddartha on 1/17/16.
 */
public class FirebaseListener {
    private final static String TAG = "FirebaseListener";

    private static final String SESSIONID = "sessionId";
    private static final String STATE = "state";
    private final static String LINK = "link";
    private static final String TOKEN = "token";
    private static final String APIKEY = "apikey";
    private static final String RECEIVEACK = "receiveack";

    private static Firebase myFirebaseRef = null;
    private static boolean isSetup = false;


    public static synchronized void setUp(final Context context, final String uuid) {
        if ( isSetup )
            return;
        isSetup = true;
        myFirebaseRef = new Firebase("https://vaipo.firebaseio.com/" + LINK + "/" + uuid);
        myFirebaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "There are " + dataSnapshot.getChildrenCount() + " values @ " + myFirebaseRef);
                String newSessionId = "-1", newToken = "-1", newApiKey = "-1";

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    if (postSnapshot.getKey().equalsIgnoreCase(STATE)) {
                       /* int state = (int) postSnapshot.getValue();
                        if (state == DialMsg.END)
                            Utils.endVaipoCall(MainActivity.this);*/
                    } else if (postSnapshot.getKey().equalsIgnoreCase(SESSIONID)) {
                        newSessionId = (String) postSnapshot.getValue();
                        if (newSessionId == null || newSessionId.equalsIgnoreCase("-1")) {
                            //ignore
                            newSessionId = "-1";
                        }
                        Log.d(TAG, "New SessionId Val " + newSessionId);
                        //SharedPreferences.Editor editor = sharedPreferences.edit();
                        //editor.putString("sessionId", newSessionId);
                        //editor.commit();
                    } else if (postSnapshot.getKey().equalsIgnoreCase(TOKEN)) {
                        newToken = (String) postSnapshot.getValue();
                        if (newToken == null || newToken.equalsIgnoreCase("-1")) {
                            //ignore
                            newToken = "-1";
                        }
                        Log.d(TAG, "New Token Val " + newToken);
                        //SharedPreferences.Editor editor = sharedPreferences.edit();
                        //editor.putString("token", newToken);
                        //editor.commit();
                    } else if (postSnapshot.getKey().equalsIgnoreCase(APIKEY)) {
                        newApiKey = (String) postSnapshot.getValue();
                        if (newApiKey == null || newApiKey.equalsIgnoreCase("-1")) {
                            //ignore
                            newApiKey = "-1";
                        }
                        Log.d(TAG, "New ApiKey Val " + newApiKey);
                    } else if (postSnapshot.getKey().equalsIgnoreCase(RECEIVEACK)) {
                        if ((boolean) postSnapshot.getValue())
                            Utils.receiveUserAck(context);
                    } else {
                        continue;
                    }

                }
                if (!newSessionId.equalsIgnoreCase("-1") &&
                        !newToken.equalsIgnoreCase("-1") &&
                        !newApiKey.equalsIgnoreCase("-1") &&
                        CallStateHandler.mCall) {

                    Intent i = new Intent(context, BubbleVideoView.class);
                    i.putExtra("sessionId", newSessionId);
                    i.putExtra("token", newToken);
                    i.putExtra("apikey", newApiKey);
                    context.startService(i);

                    myFirebaseRef.removeEventListener(this);
                    isSetup = false;

                    final Firebase ref = new Firebase("https://vaipo.firebaseio.com/" + LINK + "/" + uuid + "/" + RECEIVEACK);
                    ref.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue() == null)
                                return;
                            if ((boolean) dataSnapshot.getValue()) {
                                Utils.receiveUserAck(context);
                                ref.removeEventListener(this);
                            }
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }

    public static void destroy() {
        isSetup = false;
    }
}
