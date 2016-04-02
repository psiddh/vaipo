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
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.dd.processbutton.iml.ActionProcessButton;

import app.com.vaipo.Utils.ProgressGenerator;
import app.com.vaipo.Utils.Utils;
import app.com.vaipo.appState.AppState;
import app.com.vaipo.format.JsonFormatter;
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

    private String number = "";

    private ContactsListenerAction mCallback = null;

    private enum State { LAUNCH_CONTACTS, DIAL, END }
    private State mState = State.LAUNCH_CONTACTS;

    public ContactsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dial_ui, container, false);

        progressGenerator = new ProgressGenerator(this);
        btnContacts = (ActionProcessButton) rootView.findViewById(R.id.dialTo);
        btnContacts.setMode(ActionProcessButton.Mode.ENDLESS);

        contactEditText = (EditText) rootView.findViewById(R.id.contacts_btn);

        btnContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isState(State.LAUNCH_CONTACTS)) {
                    progressGenerator.start(btnContacts);

                    mCallback.onContactsBtnClicked();
                } else if (isState(State.DIAL)) {
                    enterState(State.END);


                } else if (isState(State.END)) {
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
                } else {
                    if (Utils.inCall()) {
                        enterState(State.END);
                    } else {
                        enterState(State.DIAL);
                    }
                }
            }
        });
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        appState = (AppState) getActivity().getApplication();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        formatter.initialize();

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
    public void onDestroy() {
        super.onDestroy();
        //LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
        enterState(State.LAUNCH_CONTACTS);
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
        String displayString = (contactName != null && !contactName.isEmpty()) ? contactName : phoneNumber;
        if (displayString == null || displayString.isEmpty()) {
            progressGenerator.complete(btnContacts);
            return;
        }
        contactEditText.setText(displayString);
        contactEditText.setSelection(displayString.length());
        progressGenerator.complete(btnContacts);
        enterState(State.DIAL);
    }

    private void enterState(final State state) {
        mState = state;
        btnContacts.setEnabled(true);

        if (mState == State.LAUNCH_CONTACTS) {
            btnContacts.setText("CONTACTS");
        } else if (mState == State.DIAL) {
            btnContacts.setText("DIAL");
        } else if (mState == State.END) {
            btnContacts.setText("END");
        }
    }

    private State getState() {
        return mState;
    }

    private boolean isState(State state) {
        return (state == mState);
    }
}
