package app.com.vaipo.Utils;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.codinguser.android.contactpicker.ContactsPickerActivity;


import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Pattern;

import app.com.vaipo.ActivityEnterCodeDialog;
import app.com.vaipo.BubbleVideoView;
import app.com.vaipo.CallStateHandler;
import app.com.vaipo.UIActivity;
import app.com.vaipo.UIDialogFragment;
import app.com.vaipo.appState.AppState;
import app.com.vaipo.format.JsonFormatter;
import app.com.vaipo.messages.RegistrationMsg;
import app.com.vaipo.rest.RestAPI;

/**
 * Created by siddartha on 12/30/15.
 */
public class Utils {

    private static final String TAG = "Utils";

    public static String END_VAIPO_CALL = "end-vaipo-call";
    public static String RECEIVE_USER_ACK = "receive-user-ack";
    public static String REGISTRATION_STATUS = "registartion-status";

    public static String CALL_STATUS = "call-status";
    public static final String ACTION_CALL_START = "app.com.vaipo.call_start";
    public static final String ACTION_CALL_END = "app.com.vaipo.call_end";


    public static final String DEBUG_UI = "debug";

    public static final String ACTION_YES = "app.com.vaipo.YES";
    public static final String ACTION_NO = "app.com.vaipo.NO";

    public static final int REST_RESPONSE_OK = 200;
    public static final int REST_RESPONSE_FAILURE = 400;

    public static final int OK = 200;
    public static final int GENERIC_FAILURE = 400;


    public static final String ACTION_INCALL_END = "app.com.vaipo.incall.action.end";
    public static final String ACTION_INCALL_SWAP = "app.com.vaipo.incall.action.swap";
    public static final String ACTION_INCALL_SPKR = "app.com.vaipo.incall.action.spkr";
    public static final String ACTION_INCALL_MUTE = "app.com.vaipo.incall.action.mute";

    public static final int REGISTER_TYPE_NUMBER = 1;
    public static final int REGISTER_TYPE_EMAILID = 2;

    public static final Pattern EMAIL_ADDRESS_PATTERN = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
    );

    public  interface onRegCallback {
        void onDone(int status, Object result);
    }

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

    public static void sendInCallAction(Context context, String action, int option) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(action);
        intent.putExtra("option", option);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static void sendRegistartionStatus(Context context, boolean isRegistered) {
        Intent intent = new Intent(REGISTRATION_STATUS);
        intent.putExtra(REGISTRATION_STATUS, isRegistered);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static void sendCallStatus(Context context, boolean callEnd) {
        Intent intent = new Intent(CALL_STATUS);
        intent.putExtra(CALL_STATUS, callEnd);
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
        return CallStateHandler.mCall || BubbleVideoView.flag || UIActivity.inCall;
    }

    public static void putPref(Context ctx, String key, Object val) {
        SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = mySharedPreferences.edit();

        if (key.equals("checkbox_preference")) {
            editor.putBoolean(key, (Boolean) val);
        }
        if (key.equals("edittext_preference")) {
            editor.putString(key, (String) val);
        }

        if (key.equals("number")) {
            editor.putString(key, (String) val);
        }

        if (key.equals("reg_type")) {
            editor.putInt(key, (Integer) val);
        }

        if (key.equals("registered")) {
            editor.putBoolean(key, true);
        }

        editor.commit();
    }

    public static void putPrefs(Context ctx, HashMap<String, Object> vals) {
        SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = mySharedPreferences.edit();

        for (String key : vals.keySet()) {
            if (key.equals("checkbox_preference")) {
                editor.putBoolean(key, (Boolean) vals.get(key));
            }
            if (key.equals("edittext_preference")) {
                editor.putString(key, (String) vals.get(key));
            }

            if (key.equals("number")) {
                editor.putString(key, (String) vals.get(key));
            }

            if (key.equals("reg_type")) {
                editor.putInt(key, (Integer) vals.get(key));
            }

            if (key.equals("registered")) {
                editor.putBoolean(key, true);
            }
        }

        editor.commit();

    }

    public static Object getPref(Context ctx, String key) {
        SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);

        if (key.equals("checkbox_preference")) {
            return mySharedPreferences.getBoolean(key, true);
        }

        if (key.equals("edittext_preference")) {
            return mySharedPreferences.getString(key, "");
        }

        if (key.equals("number")) {
            return mySharedPreferences.getString(key, "");
        }

        if (key.equals("registered")) {
            return mySharedPreferences.getBoolean(key, true);
        }

        if (key.equals("reg_type")) {
            return mySharedPreferences.getInt(key, Utils.REGISTER_TYPE_NUMBER);
        }

        return null;
    }

    public static boolean isSimReady(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);  //gets the current TelephonyManager
        return (tm.getSimState() == TelephonyManager.SIM_STATE_READY);

    }

    public static void sendRegisterMessage(final Context ctx, final String registerID, final int type, final RestAPI rest, final JsonFormatter formatter, final onRegCallback callback) {
        String reg = (type == REGISTER_TYPE_NUMBER) ? registerID.replaceAll("[^0-9]","") : registerID;
        final String regID = reg;

        Activity context = (Activity) ctx;
        if (!(context instanceof Activity)) {
            Log.d(TAG, "Context must be of type 'Activity'");
            if (callback != null) {
                callback.onDone(GENERIC_FAILURE, null);
                return;
            }
        }

        AppState appState = (AppState) context.getApplication();;
        RegistrationMsg msg = new RegistrationMsg(appState.getID(), regID);
            rest.call(RestAPI.REGISTER, formatter.get(msg), new RestAPI.onPostCallBackDone() {
                @Override
                public void onResult(Integer result) {
                    Log.d(TAG, "Registered");

                    HashMap<String , Object> map = new HashMap<String, Object>();
                    map.put("checkbox_preference", (result == REST_RESPONSE_OK));
                    map.put("reg_type", type);
                    map.put("number", regID);
                    map.put("registered", true);
                    putPrefs(ctx, map);

                    if (callback != null) {
                        callback.onDone(result, null);
                }
            }
        });
    }

    public static String sanitizeRegId(String id) {
        if (! isValidEmailAddress(id))
            return id.replaceAll("[^0-9]","").trim();
        else
            return id.trim();
    }

    public static boolean isValidEmailAddress(CharSequence target) {
        return !TextUtils.isEmpty(target) && EMAIL_ADDRESS_PATTERN.matcher(target).matches();
    }
}
