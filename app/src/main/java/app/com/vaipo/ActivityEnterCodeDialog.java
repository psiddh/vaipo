package app.com.vaipo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.dd.processbutton.iml.ActionProcessButton;

import app.com.vaipo.Utils.Utils;
import app.com.vaipo.appState.AppState;
import app.com.vaipo.format.JsonFormatter;
import app.com.vaipo.messages.AuthenticateMsg;
import app.com.vaipo.rest.RestAPI;


public class ActivityEnterCodeDialog extends Activity {

    private static String TAG = "ActivityEnterCodeDialog";
    private Button submit;
    private Button dismiss;
    private EditText enterCode;
    private ActionProcessButton resend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout linearLayout = (LinearLayout) View.inflate(this, R.layout.enter_code, null);
        setContentView(linearLayout);

        this.setFinishOnTouchOutside(false);

        final RestAPI rest = new RestAPI();

        enterCode = (EditText) linearLayout.findViewById(R.id.edittext_code);
        submit = (Button) linearLayout.findViewById(R.id.btn_submit);
        dismiss = (Button) linearLayout.findViewById(R.id.btn_dismiss);
        resend = (ActionProcessButton) linearLayout.findViewById(R.id.btn_resend);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rest.call(RestAPI.AUTHENTICATE, constructAuthenticateMsg(), new RestAPI.onPostCallBackDone() {
                    @Override
                    public void onResult(Integer result) {
                        Log.d(TAG, "Hurrah " + result);
                        Utils.sendRegistartionStatus(getApplicationContext(), (result == Utils.REST_RESPONSE_OK));
                        finish();
                    }
                });
            }
        });

        dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.sendRegistartionStatus(getApplicationContext(), false);
                finish();
            }
        });

        resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rest.call(RestAPI.RESEND, constructAuthenticateMsg(), new RestAPI.onPostCallBackDone() {
                    @Override
                    public void onResult(Integer result) {
                        Log.d(TAG, "Hurrah " + result);
                        //Utils.sendRegistartionStatus(getApplicationContext(), (result == Utils.REST_RESPONSE_OK));
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    public void onRestoreInstanceState(Bundle inState) {
        super.onRestoreInstanceState(inState);



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return false;
    }

    private String constructAuthenticateMsg() {
        AuthenticateMsg msg = null;

        JsonFormatter formatter = new JsonFormatter();
        formatter.initialize();

        final AppState appState = (AppState)getApplication();

        Intent intent = getIntent();
        String number = intent.getStringExtra("number");

        String code = enterCode.getText().toString();

        msg = new AuthenticateMsg(appState.getID(), number, code);
        return formatter.get(msg);
    }

}
