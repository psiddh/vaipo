/*
 * Copyright (c) 2014-2015 Amberfog.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.com.vaipo;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.dd.processbutton.iml.ActionProcessButton;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import app.com.vaipo.Utils.ProgressGenerator;
import app.com.vaipo.Utils.Utils;
import app.com.vaipo.appState.AppState;
import app.com.vaipo.config.OpenTokConfig;
import app.com.vaipo.format.JsonFormatter;
import app.com.vaipo.messages.DialMsg;
import app.com.vaipo.messages.RegistrationMsg;
import app.com.vaipo.rest.RestAPI;

public class ContactsFragment extends Fragment implements ProgressGenerator.OnCompleteListener {

    private final String TAG = "ContactsFragment";
    private final String LINK = "link";

    private ActionProcessButton btnContacts;
    private ProgressGenerator progressGenerator;

    private EditText contactEditText;

    private SharedPreferences sharedPreferences;
    private AppState appState;

    private RestAPI rest = new RestAPI();
    private JsonFormatter formatter = new JsonFormatter();

    private String mOutGoingNumber = "";
    private Firebase myFirebaseRef;

    private ContactsListenerAction mCallback = null;

    private enum State { LAUNCH_CONTACTS, DIAL, END }
    private State mState = State.LAUNCH_CONTACTS;

    private boolean isContactNameSelected = false;

    public BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Utils.END_VAIPO_CALL)) {
                if (contactEditText != null)
                    contactEditText.setText("");
            }
        }
    };
    public ContactsFragment() {

    }

    @Override
    public void onCreate(Bundle instance) {
        super.onCreate(instance);
        setHasOptionsMenu(true);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dial_ui, container, false);

        progressGenerator = new ProgressGenerator(this);
        btnContacts = (ActionProcessButton) rootView.findViewById(R.id.dialTo);
        btnContacts.setMode(ActionProcessButton.Mode.ENDLESS);

        contactEditText = (EditText) rootView.findViewById(R.id.contacts_btn);
        isContactNameSelected = false;
        btnContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isState(State.LAUNCH_CONTACTS)) {
                    progressGenerator.start(btnContacts);

                    mCallback.onContactsBtnClicked();
                } else if (isState(State.DIAL)) {
                    String number = sharedPreferences.getString("number", "");
                    //mOutGoingNumber = contactEditText.getText().toString();
                    setupDialMsg(mOutGoingNumber, number);
                    enterState(State.END);
                } else if (isState(State.END)) {
                    setupEndMsg();
                    enterState(State.LAUNCH_CONTACTS);
                }
            }
        });

        contactEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    enterState(State.LAUNCH_CONTACTS);
                    mOutGoingNumber = "";
                } else {
                    if (Utils.inCall()) {
                        enterState(State.END);
                    } else {
                        enterState(State.DIAL);
                    }
                    if (!isContactNameSelected)
                        mOutGoingNumber = s.toString();
                }
            }
        });

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter(Utils.END_VAIPO_CALL));
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        appState = (AppState) getActivity().getApplication();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        formatter.initialize();
        setUpFirebaseListner(getActivity());

        //LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
        //        new IntentFilter(Utils.REGISTRATION_STATUS));
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (ContactsListenerAction) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement LogoutUser");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            //Intent i = new Intent(getActivity(), InCallActivityDialog.class);
            //startActivity(i);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onComplete() {

    }

    @Override
    public void onDismiss() {
    }

    @Override
    public void onError() {
    }

    public void onUserSelectionResult(String phoneNumber, String contactName) {
        if (getActivity() == null)
            return;
        isContactNameSelected = (contactName != null) && !TextUtils.isEmpty(contactName);
        String displayString = (contactName != null && !contactName.isEmpty()) ? contactName : phoneNumber;
        if (displayString == null || displayString.isEmpty()) {
            if (progressGenerator != null)
                progressGenerator.complete(btnContacts);
            mOutGoingNumber = "";
            return;
        }

        mOutGoingNumber = phoneNumber;
        contactEditText.setText(displayString);
        contactEditText.setSelection(displayString.length());
        if (progressGenerator != null)
            progressGenerator.complete(btnContacts);
        enterState(State.DIAL);
    }

    private void enterState(final State state) {
        mState = state;
        btnContacts.setEnabled(true);
        progressGenerator.complete(btnContacts);

        if (mState == State.LAUNCH_CONTACTS) {
            btnContacts.setText("PICK");
            mOutGoingNumber = "";
            isContactNameSelected = false;
        } else if (mState == State.DIAL) {
            btnContacts.setText("DIAL");
        } else if (mState == State.END) {
            btnContacts.setText("END");
            isContactNameSelected = false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                int action = data.getIntExtra("action", InCallActivityDialog.ACTION_NONE);
                int option = data.getIntExtra("option", InCallActivityDialog.OPTION_NONE);

                switch (action) {
                    case InCallActivityDialog.ACTION_END:
                        Utils.endVaipoCall(getActivity());
                        break;
                    case InCallActivityDialog.ACTION_MUTE:
                        break;
                    case InCallActivityDialog.ACTION_SPKR:
                        break;
                    case InCallActivityDialog.ACTION_SWAP:
                        break;
                }

            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }


    private State getState() {
        return mState;
    }

    private boolean isState(State state) {
        return (state == mState);
    }


    private void setupDialMsg(String outgoingNumber, String myNumber) {
        outgoingNumber = Utils.sanitizeRegId(outgoingNumber);
        myNumber = Utils.sanitizeRegId(myNumber);

        DialMsg message = new DialMsg();
        message.setId(appState.getID());
        message.setCaller(myNumber);
        message.setCallee(outgoingNumber);
        message.setState(DialMsg.DIALING);
        message.setPeerautodiscover(true);

        appState.setCallee(outgoingNumber);
        appState.setCaller(myNumber);

        //formatter.destroy();
        formatter.initialize();
        rest.call(RestAPI.CALL, formatter.get(message), null);
    }

    private void setupEndMsg() {
        DialMsg message = new DialMsg();
        message.setId(appState.getID());
        message.setCallee(appState.getCallee());
        message.setCaller(appState.getCaller());
        message.setState(DialMsg.END);
        //formatter.destroy();
        formatter.initialize();
        rest.call(RestAPI.CALL, formatter.get(message), null);

        if (getActivity() == null) {
            Log.d(TAG, "BAD! Really bad for my users!");
            return;
        }
        Intent i = new Intent(getActivity(), BubbleVideoView.class);
        getActivity().stopService(i);
        Utils.endVaipoCall(getActivity());
    }

    private void setUpFirebaseListner(final Context ctx) {
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
                    if (contactEditText != null)
                        contactEditText.setText("");
                    Utils.endVaipoCall(ctx);
                } else {
                    newSessionId = dialMsg.getSessionId();
                    newToken = dialMsg.getToken();
                    newApiKey = dialMsg.getApikey();
                    peerAutoDiscover = dialMsg.getPeerautodiscover();

                    // TBD: Fix this hack!!
                    boolean response = dialMsg.getResponse();
                    if (response)
                        Utils.sendUserResponse(ctx, response);
                }

                if (!newSessionId.equalsIgnoreCase("-1") &&
                        !newToken.equalsIgnoreCase("-1") &&
                        !newApiKey.equalsIgnoreCase("-1")) {

                    Intent i = new Intent(ctx, BubbleVideoView.class);
                    i.putExtra("sessionId", newSessionId);
                    i.putExtra("token", newToken);
                    i.putExtra("apikey", newApiKey);
                    i.putExtra("peerautodiscover", peerAutoDiscover);

                    //if (ctx != null)
                    //    ctx.startService(i);
                    if (newApiKey.equals(OpenTokConfig.API_KEY) &&
                            newSessionId.equals(OpenTokConfig.SESSION_ID) &&
                            newToken.equals(OpenTokConfig.TOKEN)) {
                        return;
                    }
                    OpenTokConfig.API_KEY = newApiKey;
                    OpenTokConfig.SESSION_ID = newSessionId;
                    OpenTokConfig.TOKEN = newToken;

                    Utils.startUI(ctx);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }
}
