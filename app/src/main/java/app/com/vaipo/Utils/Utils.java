package app.com.vaipo.Utils;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

import app.com.vaipo.UIDialogFragment;

/**
 * Created by siddartha on 12/30/15.
 */
public class Utils {

    public static String END_VAIPO_CALL = "end-vaipo-call";
    public static String RECEIVE_USER_ACK = "receive-user-ack";

    public static final String DEBUG_UI = "debug";

    public static final String ACTION_YES = "app.com.vaipo.YES";
    public static final String ACTION_NO = "app.com.vaipo.NO";


    public static void endVaipoCall(Context context) {
        Intent intent = new Intent(END_VAIPO_CALL);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static void receiveUserAck(Context context) {
        Intent intent = new Intent(RECEIVE_USER_ACK);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }


    public static void setDebugInPrefs(Context context, boolean value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(DEBUG_UI, value);
        editor.commit();
    }

    public static boolean getDebugFromPrefs(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(DEBUG_UI, false);
    }
}
