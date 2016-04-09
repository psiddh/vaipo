package app.com.vaipo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.dd.processbutton.iml.ActionProcessButton;

import app.com.vaipo.Utils.Utils;
import app.com.vaipo.appState.AppState;
import app.com.vaipo.format.JsonFormatter;
import app.com.vaipo.messages.AuthenticateMsg;
import app.com.vaipo.rest.RestAPI;


public class InCallActivityDialog extends Activity {

    private static String TAG = "InCallActivityDialog";

    public static final int ACTION_NONE = 0;
    public static final int ACTION_END = 1;
    public static final int ACTION_SWAP = 2;
    public static final int ACTION_MUTE = 3;
    public static final int ACTION_SPKR = 4;

    public static final int OPTION_NONE = 1000;
    public static final int OPTION_MUTE_ON = 1001;
    public static final int OPTION_MUTE_OFF = 1002;

    public static final int OPTION_SPKR_ON = 2001;
    public static final int OPTION_SPKR_OFF = 2002;


    private ActionProcessButton swap;
    private ActionProcessButton mute;
    private ActionProcessButton end;
    private ActionProcessButton spkr;



    Drawable muteOff = null;
    Drawable muteOn = null;


    Drawable spkrOff = null;
    Drawable spkrOn = null;

    public static boolean isMuteOff = true;
    public static boolean isSpkrOn = true;

    public BroadcastReceiver mCallListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase(Utils.CALL_STATUS)) {
                boolean end = intent.getBooleanExtra(Utils.CALL_STATUS, Utils.inCall());
                if (end) {
                    finish();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout frameLayout = (FrameLayout) View.inflate(this, R.layout.in_call_button_ui, null);
        setContentView(frameLayout);

        this.setFinishOnTouchOutside(false);


        swap = (ActionProcessButton) frameLayout.findViewById(R.id.end);
        mute = (ActionProcessButton) frameLayout.findViewById(R.id.mute);
        end = (ActionProcessButton) frameLayout.findViewById(R.id.swap);
        spkr = (ActionProcessButton) frameLayout.findViewById(R.id.speaker);


        this.registerReceiver(mCallListener, new IntentFilter(Utils.CALL_STATUS));

        muteOff = getDrawable(getResources(), R.drawable.unmute_pub);
        muteOn = getDrawable(getResources(), R.drawable.mute_pub);
        mute.setText(isMuteOff ? "Mute" : "UnMute");

        spkr.setText(isSpkrOn ? "Spkr On" : "Spkr Off");


        Drawable drawable = isMuteOff ? muteOff : muteOn;
        mute.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);


        swap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.sendInCallAction(getApplicationContext(), Utils.ACTION_INCALL_END, ACTION_NONE);
                //setResult(ACTION_SWAP, OPTION_NONE);
                finish();
            }
        });

        mute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isMuteOff  = !isMuteOff;

                Drawable drawable = isMuteOff ? muteOff : muteOn;
                mute.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);

                mute.setText(isMuteOff ? "Mute" : "UnMute");
                //setResult(ACTION_MUTE, isMuteOff ? OPTION_MUTE_OFF : OPTION_MUTE_ON);
                Utils.sendInCallAction(getApplicationContext(), Utils.ACTION_INCALL_MUTE, isMuteOff ? OPTION_MUTE_OFF : OPTION_MUTE_ON);

                finish();
            }
        });

        end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.sendInCallAction(getApplicationContext(), Utils.ACTION_INCALL_END, OPTION_NONE);
                finish();
            }
        });

        spkr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSpkrOn  = !isSpkrOn;

                spkr.setText(isSpkrOn ? "Spkr On" : "Spkr Off");
                Utils.sendInCallAction(getApplicationContext(), Utils.ACTION_INCALL_SPKR, isSpkrOn ? OPTION_SPKR_ON : OPTION_SPKR_OFF);
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        this.unregisterReceiver(mCallListener);
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

    private Drawable getDrawable(Resources resources, int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return resources.getDrawable(id, this.getTheme());
        } else {
            return resources.getDrawable(id);
        }
    }

    private void setResult(int action, int option) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("action",action);
        returnIntent.putExtra("option",option);

        setResult(Activity.RESULT_OK,returnIntent);
    }


}
