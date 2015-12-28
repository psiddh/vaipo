package app.com.vaipo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.Method;

import app.com.vaipo.appState.AppState;
import app.com.vaipo.format.JsonFormatter;
import app.com.vaipo.messages.DialMsg;
import app.com.vaipo.rest.RestAPI;

public class CallStateHandler extends BroadcastReceiver {

    private static String TAG = "CallStateHandler";
    private static String last6Nums = null;
    private static boolean bIncoming = false;
    private static boolean bOutgoing = false;
    private static boolean bAnswerForIncoming = false;
    private static boolean bDisplayed = false;
    private int LAST_N_NUMS = 7;
    private String serverUrl = "https://apprtc.appspot.com/r/";
    private String fixedServerUrl = "http://10.0.0.14:4567/";

    private static boolean alreadyLaunched = false;
    private  static boolean mCall = false;

    private AppState appState  = new AppState();

    private RestAPI rest = new RestAPI();
    private JsonFormatter formatter = new JsonFormatter();

    private DialMsg message = new DialMsg();


    @Override
    public void onReceive(final Context context, Intent intent) {
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        String msg = "Phone state changed to " + state;
        TelephonyManager mTelMgr = (TelephonyManager)context.getSystemService(context.TELEPHONY_SERVICE);
        formatter.initialize();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            String number = prefs.getString("number", "");
        Log.d("URL : ",msg );
        if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {

            // get phone number from bundle
            String outgoingNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            Log.d("URL : ", "OutgoingNumber number - " + outgoingNumber);

            message = new DialMsg();
            message.setId(appState.getID());
            message.setCaller(number);
            message.setCallee(outgoingNumber);
            message.setState(DialMsg.DIALING);

            rest.call(RestAPI.CALL, formatter.get(message), null);


            last6Nums = (outgoingNumber == null || outgoingNumber.length() < LAST_N_NUMS) ?
                    outgoingNumber : outgoingNumber.substring(outgoingNumber.length() - LAST_N_NUMS);
            Log.d("URL : ", "Computed last 6 OutgoingNumber number - " + last6Nums);

            setResultData(outgoingNumber);
            launchVideo(context, last6Nums, false);
            bOutgoing = true;
            mCall = true;


            return;
        }

            Log.d("CallStateHandler: ", "state - " + state);
        if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {

            String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            msg += ". Incoming number is " + incomingNumber;
            Log.d("URL : ", "Incoming number - " + incomingNumber);

            if (!mCall) {
                message = new DialMsg();
                message.setId(appState.getID());
                message.setCaller(incomingNumber);
                message.setCallee(number);
                message.setState(DialMsg.INCOMING);
                rest.call(RestAPI.CALL, formatter.get(message), new RestAPI.onPostCallBackDone() {
                    @Override
                    public void onResult(Integer result) {
                        if (result == 200) {
                        }
                    }
                });
            }

            last6Nums = (incomingNumber == null || incomingNumber.length() < LAST_N_NUMS) ?
                    incomingNumber : incomingNumber.substring(incomingNumber.length() - LAST_N_NUMS);
            Log.d("URL : ", "COMPUTED last 6 Incoming number - " + last6Nums);
            launchVideo(context, last6Nums, true);
            bIncoming = true;

        }

        if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING)) {
            mCall = true;  //fire the flag that there is call ongoing
        } else if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_IDLE)) {
            if (mCall) {
                if (message != null)
                    message.setState(DialMsg.IDLE);

                mCall = false; //Reverting the flag, indicating you are aware that there was call
                // Here do the rest of your operation you want
                Intent i = new Intent(context, VideoView.class);
                context.stopService(i);
                alreadyLaunched = false;
            }
        }

    }

    private void launchVideo(Context context, String IncomingOrOutgoingNum, boolean isIncoming) {

        if (alreadyLaunched) return;

        alreadyLaunched = true;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String persistedNum = prefs.getString("my_number","");


        String myPhoneNumber = (persistedNum == null || persistedNum.length() < LAST_N_NUMS) ?
                persistedNum : persistedNum.substring(persistedNum.length() - LAST_N_NUMS);

        String roomNumber = isIncoming ?  (IncomingOrOutgoingNum + myPhoneNumber) : (myPhoneNumber + IncomingOrOutgoingNum) ;
        String url = serverUrl + roomNumber;
        if (!fixedServerUrl.equals(""))
            url = fixedServerUrl;
        Log.d("URL : xxxxxxxxxxxxx ", url);
        Intent i = new Intent(context, VideoView.class);
        i.putExtra("URL", url);
        context.startService(i);

        //endCallIfBlocked(context);
    }

    private void endCallIfBlocked(Context context) {
        try {
            TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
            Class clazz = Class.forName(telephonyManager.getClass().getName());
            Method method = clazz.getDeclaredMethod("getITelephony");
            method.setAccessible(true);
            ITelephony telephonyService = (ITelephony) method.invoke(telephonyManager);
            telephonyService.endCall();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
