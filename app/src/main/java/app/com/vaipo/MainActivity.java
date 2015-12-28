package app.com.vaipo;

import android.app.Activity;
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
import android.widget.ImageButton;

import com.firebase.client.Firebase;

import java.text.DecimalFormat;
import java.util.Locale;

import app.com.vaipo.appState.AppState;
import app.com.vaipo.format.JsonFormatter;
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
}
