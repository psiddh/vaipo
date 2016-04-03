package app.com.vaipo.Utils;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

import com.codinguser.android.contactpicker.ContactsPickerActivity;

import app.com.vaipo.BubbleVideoView;
import app.com.vaipo.CallStateHandler;
import app.com.vaipo.UIDialogFragment;

/**
 * Created by siddartha on 12/30/15.
 */
public class Utils {

    public static String END_VAIPO_CALL = "end-vaipo-call";
    public static String RECEIVE_USER_ACK = "receive-user-ack";
    public static String REGISTRATION_STATUS = "registartion-status";


    public static final String DEBUG_UI = "debug";

    public static final String ACTION_YES = "app.com.vaipo.YES";
    public static final String ACTION_NO = "app.com.vaipo.NO";

    public static final int REST_RESPONSE_OK = 200;
    public static final int REST_RESPONSE_FAILURE = 400;


    public static void endVaipoCall(Context context) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(END_VAIPO_CALL);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static void sendUserResponse(Context context, boolean response) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(RECEIVE_USER_ACK);
        intent.putExtra(RECEIVE_USER_ACK, response);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static void sendRegistartionStatus(Context context, boolean isRegistered) {
        Intent intent = new Intent(REGISTRATION_STATUS);
        intent.putExtra(REGISTRATION_STATUS, isRegistered);
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

    public static void showContact(Activity context, int CODE) {
        context.startActivityForResult(new Intent(context, ContactsPickerActivity.class), CODE);
    }

    public static boolean inCall() {
        return CallStateHandler.mCall || BubbleVideoView.flag;
    }
}
