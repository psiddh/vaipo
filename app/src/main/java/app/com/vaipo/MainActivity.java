package app.com.vaipo;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.Locale;

import app.com.vaipo.appState.AppState;
import app.com.vaipo.Utils.Utils;
import app.com.vaipo.config.OpenTokConfig;
import app.com.vaipo.fire.FirebaseListener;
import app.com.vaipo.format.JsonFormatter;
import app.com.vaipo.messages.DialMsg;
import app.com.vaipo.messages.RegistrationMsg;
import app.com.vaipo.rest.RestAPI;


public class MainActivity extends Activity {

    private static String TAG = "MainActivity";

    private PhoneNumberFormattingTextWatcher mWatcher  ;
    private ImageButton imgButton;
    private EditText text1;
    private EditText text2;
    private EditText text3;

    private SharedPreferences sharedPreferences;
    private AppState appState;
    private RestAPI rest = new RestAPI();
    private JsonFormatter formatter = new JsonFormatter();

    private final String LINK = "link";

    public static String UUID = "";
    private Firebase myFirebaseRef;

    private RelativeLayout relativeLayout;
    public static boolean DEBUG_FAKE_UI = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        relativeLayout = (RelativeLayout) View.inflate(this, R.layout.activity_main, null);
        Firebase.setAndroidContext(this);
        setContentView(relativeLayout);

        appState = (AppState)getApplication();
        formatter.initialize();

        Locale locale = this.getResources().getConfiguration().locale;
        //String code = locale.getCountry();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Utils.setDebugInPrefs(this,false);

        sharedPreferences.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equalsIgnoreCase(Utils.DEBUG_UI)) {
                    DEBUG_FAKE_UI = sharedPreferences.getBoolean(key, false);
                    if (DEBUG_FAKE_UI) {
                        setupFakeUI();
                    }
                }
            }
        });

        TelephonyManager tm = (TelephonyManager)getSystemService(getApplicationContext().TELEPHONY_SERVICE);
        String countryCode = tm.getNetworkCountryIso();
        String code = "";
        if (countryCode.equalsIgnoreCase("us")) {
            code = "+1";
        } else if (countryCode.equalsIgnoreCase("pol")) {
            code = "+48";
        }

        mWatcher = new PhoneNumberFormattingTextWatcher(countryCode) {

        };

        EditText prefix = (EditText) findViewById(R.id.editText3);
        prefix.setText(code);

        EditText number = (EditText) findViewById(R.id.editText);

        StringBuffer simNumber = (null == tm.getLine1Number()) ?  new StringBuffer(""): new StringBuffer(tm.getLine1Number());
        if (simNumber != null && simNumber.length() > 0 && simNumber.charAt(0) == '+') {
            if (code != null)
                simNumber.replace(0,code.length(), "");
        }
        number.setText(simNumber);
        number.setSelection(simNumber.length());

        text1 = (EditText) findViewById(R.id.editText);
        text2 = (EditText) findViewById(R.id.editText2);
        text3 = (EditText) findViewById(R.id.editText3);

        if (simNumber.length() == 0 || simNumber.equals("")) {
            String persistedNum = sharedPreferences.getString("number","");
            number.setText(persistedNum);
            number.setSelection(persistedNum.length());
        }

        text2.setId(R.id.imgYes); //fake!
        imgButton = (ImageButton) findViewById(R.id.imageButton);
        imgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String number = text1.getText().toString();
                String intlCode = text3.getText().toString();
                text1.setVisibility(View.GONE);
                text2.setText("Thank You. You can now use Vaipo using the number " + intlCode + " " + FormatStringAsPhoneNumber(number));
                text3.setVisibility(View.GONE);
                imgButton.setVisibility(View.GONE);

                if (number != null || number != "") {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("number", number + "");
                    editor.commit();
                }

                RegistrationMsg msg = new RegistrationMsg(appState.getID(), number);
                rest.call(RestAPI.REGISTER, formatter.get(msg), new RestAPI.onPostCallBackDone() {
                    @Override
                    public void onResult(Integer result) {
                        Log.d(TAG, "Hurrah");
                        appState.setNumber(number);
                        UUID = appState.getID();

                        if (DEBUG_FAKE_UI || Utils.getDebugFromPrefs(MainActivity.this))
                            setupFakeUI();
                    }
                });

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseListener.destroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("text1Gone", (text1.getVisibility() == View.GONE));
        outState.putBoolean("text2Gone", (text2.getVisibility() == View.GONE));
        outState.putBoolean("text3Gone", (text3.getVisibility() == View.GONE));
        outState.putBoolean("imgGone", (imgButton.getVisibility() == View.GONE));


    }

    @Override
    public void onRestoreInstanceState(Bundle inState) {
        super.onRestoreInstanceState(inState);

        text1.setVisibility((inState.getBoolean("text1Gone") == true) ? View.GONE : View.VISIBLE);
        text2.setVisibility((inState.getBoolean("text2Gone") == true) ? View.GONE : View.VISIBLE);
        text3.setVisibility((inState.getBoolean("text3Gone") == true) ? View.GONE : View.VISIBLE);
        imgButton.setVisibility((inState.getBoolean("imgGone") == true) ? View.GONE : View.VISIBLE);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.menu_debug) {
            setupFakeUI();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem debug = menu.findItem(R.id.menu_debug);
        debug.setVisible(getResources().getBoolean(R.bool.debug));
        return true;
    }

    private static String FormatStringAsPhoneNumber(String input) {
        String output;
        switch (input.length()) {
            case 7:
                output = String.format("%s-%s", input.substring(0,3), input.substring(3,7));
                break;
            case 9:
                output = String.format("(%s) %s-%s", input.substring(0,3), input.substring(3,6), input.substring(6,9));
                break;
            case 10:
                output = String.format("(%s) %s-%s", input.substring(0,3), input.substring(3,6), input.substring(6,10));
                break;
            case 11:
                output = String.format("%s (%s) %s-%s", input.substring(0,1) ,input.substring(1,4), input.substring(4,7), input.substring(7,11));
                break;
            case 12:
                output = String.format("+%s (%s) %s-%s", input.substring(0,2) ,input.substring(2,5), input.substring(5,8), input.substring(8,12));
                break;
            default:
                return null;
        }
        return output;
    }

    private void setupFakeUI() {

        setUpFirebaseListnerWithoutInCall();

        text2.setVisibility(View.GONE);

        View rootFakeUI = getLayoutInflater().inflate(R.layout.fake_ui, null);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.BELOW, text2.getId());


        relativeLayout.addView(rootFakeUI);

        final Button startButton = (Button) rootFakeUI.findViewById(R.id.dialTo);
        final Button stopButton = (Button) rootFakeUI.findViewById(R.id.end);
        final Button incButton = (Button) rootFakeUI.findViewById(R.id.incFrom);

        final AutoCompleteTextView fakeDialNum = (AutoCompleteTextView) rootFakeUI.findViewById(R.id.dialText);
        final AutoCompleteTextView fakeIncNum = (AutoCompleteTextView) rootFakeUI.findViewById(R.id.incText);
        final EditText curNum = (EditText) rootFakeUI.findViewById(R.id.curNumber);


        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String number = sharedPreferences.getString("number", "");

                final String fakeNumberToDial = fakeDialNum.getText().toString();
                final String fakeNumberInc = fakeIncNum.getText().toString();

                if (fakeNumberToDial.isEmpty() && fakeNumberInc.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Enter atleast Dial or Incoming number", Toast.LENGTH_SHORT);
                    return;
                }

                if (!fakeNumberToDial.isEmpty() && !fakeNumberInc.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Cannot have both Dial or Incoming number at the same time", Toast.LENGTH_SHORT);
                    return;
                }

                if (!fakeNumberToDial.isEmpty())
                    setupFakeDialMsg(fakeNumberToDial, number);
                else if (!fakeNumberInc.isEmpty())
                    setupFakeIncMsg(fakeNumberInc, number);

                startButton.setEnabled(false);
                incButton.setEnabled(false);
            }
        });

        incButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String number = sharedPreferences.getString("number", "");

                final String fakeNumberToDial = fakeDialNum.getText().toString();
                final String fakeNumberInc = fakeIncNum.getText().toString();

                if (fakeNumberToDial.isEmpty() && fakeNumberInc.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Enter atleast Dial or Incoming number", Toast.LENGTH_SHORT);
                    return;
                }

                if (!fakeNumberToDial.isEmpty() && !fakeNumberInc.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Cannot have both Dial or Incoming number at the same time", Toast.LENGTH_SHORT);
                    return;
                }

                if (!fakeNumberToDial.isEmpty())
                    setupFakeDialMsg(fakeNumberToDial, number);
                else if (!fakeNumberInc.isEmpty())
                    setupFakeIncMsg(fakeNumberInc, number);

                startButton.setEnabled(false);
                incButton.setEnabled(false);
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialMsg message = new DialMsg();
                message.setId(appState.getID());
                message.setCallee(appState.getCallee());
                message.setCaller(appState.getCaller());
                message.setState(DialMsg.END);
                formatter.destroy();
                formatter.initialize();
                rest.call(RestAPI.CALL, formatter.get(message), null);

                Intent i = new Intent(MainActivity.this, BubbleVideoView.class);
                MainActivity.this.stopService(i);
                Utils.endVaipoCall(MainActivity.this);

                startButton.setEnabled(true);
                incButton.setEnabled(true);

            }
        });

        final String number = text1.getText().toString();
        curNum.setText(curNum.getText() + (number.isEmpty() ? "" : " " + number));

    }
    private void setupFakeDialMsg(String outgoingNumber, String myNumber) {
        DialMsg message = new DialMsg();
        message.setId(appState.getID());
        message.setCaller(myNumber);
        message.setCallee(outgoingNumber);
        message.setState(DialMsg.DIALING);
        message.setPeerautodiscover(true);

        appState.setCallee(outgoingNumber);
        appState.setCaller(myNumber);

        formatter.destroy();
        formatter.initialize();
        rest.call(RestAPI.CALL, formatter.get(message), null);
    }

    private void setupFakeIncMsg(String incNumber, String myNumber) {
        DialMsg message = new DialMsg();
        message.setId(appState.getID());
        message.setCaller(incNumber);
        message.setCallee(myNumber);
        message.setState(DialMsg.INCOMING);

        appState.setCallee(myNumber);
        appState.setCaller(incNumber);

        formatter.destroy();
        formatter.initialize();
        rest.call(RestAPI.CALL, formatter.get(message), null);
    }

    private void setUpFirebaseListnerWithoutInCall() {
        myFirebaseRef = new Firebase("https://vaipo.firebaseio.com/" + LINK + "/" + appState.getID());
        myFirebaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "There are " + dataSnapshot.getChildrenCount() + " values @ " + myFirebaseRef);
                String newSessionId = "-1", newToken = "-1", newApiKey = "-1";
                boolean peerAutoDiscover = false;

                DialMsg dialMsg = null;
                try {
                    dialMsg = dataSnapshot.getValue(DialMsg.class);
                } catch (NullPointerException e) {
                    return;
                } catch (Exception e) {
                    Log.d(TAG, "Oops error ! " + e.getMessage() + e.toString());
                    return;
                }
                if (dialMsg == null)
                    return;

                if (dialMsg.getState() == DialMsg.END) {
                    Utils.endVaipoCall(MainActivity.this);

                } else {
                    newSessionId = dialMsg.getSessionId();
                    newToken = dialMsg.getToken();
                    newApiKey = dialMsg.getApikey();
                    peerAutoDiscover = dialMsg.getPeerautodiscover();

                    // TBD: Fix this hack!!
                    boolean response = dialMsg.getResponse();
                    if (response)
                        Utils.sendUserResponse(MainActivity.this, response);
                }

                if (!newSessionId.equalsIgnoreCase("-1") &&
                        !newToken.equalsIgnoreCase("-1") &&
                        !newApiKey.equalsIgnoreCase("-1")) {

                    /*Intent i = new Intent(MainActivity.this, BubbleVideoView.class);
                    i.putExtra("sessionId", newSessionId);
                    i.putExtra("token", newToken);
                    i.putExtra("apikey", newApiKey);
                    i.putExtra("peerautodiscover", peerAutoDiscover);

                    if (false)
                        MainActivity.this.startService(i);
                    else {
                        OpenTokConfig.API_KEY = newApiKey;
                        OpenTokConfig.SESSION_ID = newSessionId;
                        OpenTokConfig.TOKEN = newToken;

                        Intent intent = new Intent(MainActivity.this, UIActivity.class);
                        startActivity(intent);
                    }*/
                    OpenTokConfig.API_KEY = newApiKey;
                    OpenTokConfig.SESSION_ID = newSessionId;
                    OpenTokConfig.TOKEN = newToken;
                    Utils.startUI(MainActivity.this);

                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }

}
