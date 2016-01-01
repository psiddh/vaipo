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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.opentok.android.Publisher;
import com.opentok.android.Subscriber;

import java.text.DecimalFormat;
import java.util.Locale;

import app.com.vaipo.appState.AppState;
import app.com.vaipo.appState.Utils.Utils;
import app.com.vaipo.format.JsonFormatter;
import app.com.vaipo.messages.RegistrationMsg;
import app.com.vaipo.openTok.ITalkUICallbacks;
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
    private static final String STATE = "state";
    private static final String SESSIONID = "sessionId";
    private static final String TOKEN = "token";


    private Firebase myFirebaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Firebase.setAndroidContext(this);
        setContentView(R.layout.activity_main);

        appState = (AppState)getApplication();
        formatter.initialize();

        Locale locale = this.getResources().getConfiguration().locale;
        //String code = locale.getCountry();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
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
        number.setText(tm.getLine1Number());

        text1 = (EditText) findViewById(R.id.editText);
        text2 = (EditText) findViewById(R.id.editText2);
        text3 = (EditText) findViewById(R.id.editText3);

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
                    editor.putString("intl_number", intlCode + number + "");
                    editor.putString("number", number + "");
                    editor.commit();
                }
                RegistrationMsg msg = new RegistrationMsg(appState.getID(), number);
                rest.call(RestAPI.REGISTER, formatter.get(msg), new RestAPI.onPostCallBackDone() {
                    @Override
                    public void onResult(Integer result) {
                        Log.d(TAG, "Hurrah");
                        appState.setNumber(number);

                        setUpFirebaseListner();
                    }
                });

            }
        });
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

        return super.onOptionsItemSelected(item);
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

    private void setUpFirebaseListner() {
        myFirebaseRef = new Firebase("https://vaipo.firebaseio.com/" + LINK + "/" + appState.getID());
        myFirebaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "There are " + dataSnapshot.getChildrenCount() + " values @ " + myFirebaseRef);
                String newSessionId = "-1", newToken = "-1";

                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    if (postSnapshot.getKey().equalsIgnoreCase(STATE)) {
                        Utils.endVaipoCall(MainActivity.this);
                    } else if (postSnapshot.getKey().equalsIgnoreCase(SESSIONID)) {
                        newSessionId = (String) postSnapshot.getValue();
                        if (newSessionId == null || newSessionId.equalsIgnoreCase("-1")) {
                            //ignore
                            newSessionId = "-1";
                        }
                        Log.d(TAG, "New SessionId Val " + newSessionId);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("sessionId", newSessionId);
                        editor.commit();
                    } else if (postSnapshot.getKey().equalsIgnoreCase(TOKEN)) {
                        newToken = (String) postSnapshot.getValue();
                        if (newToken == null || newToken.equalsIgnoreCase("-1")) {
                            //ignore
                            newToken = "-1";
                        }
                        Log.d(TAG, "New Token Val " + newToken);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("token", newToken);
                        editor.commit();
                    } else {
                        continue;
                    }

                }

                if (!newSessionId.equalsIgnoreCase("-1")) {
                    /*MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent i = new Intent(MainActivity.this, VaipoView.class);
                            i.putExtra("sessionId", newSessionId);

                            MainActivity.this.startActivity(i);
                        }
                    });*/

                    Intent i = new Intent(MainActivity.this, VaipoView.class);
                    i.putExtra("sessionId", newSessionId);
                    i.putExtra("token", newToken);
                    MainActivity.this.startActivityForResult(i, 1001);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }
}
