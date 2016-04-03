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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import app.com.vaipo.Utils.ProgressGenerator;
import app.com.vaipo.Utils.Utils;
import app.com.vaipo.appState.AppState;
import app.com.vaipo.format.JsonFormatter;
import app.com.vaipo.messages.RegistrationMsg;
import app.com.vaipo.openTok.Talk;
import app.com.vaipo.rest.RestAPI;

import com.dd.processbutton.iml.ActionProcessButton;
import com.firebase.client.Firebase;

public class VerifyPhoneFragment extends BaseFlagFragment implements ProgressGenerator.OnCompleteListener {

    private final String TAG = "VerifyPhoneFragment";
    private final String LINK = "link";

    private ActionProcessButton btnRegister;
    private ProgressGenerator progressGenerator;

    private SharedPreferences sharedPreferences;
    private AppState appState;

    private RestAPI rest = new RestAPI();
    private JsonFormatter formatter = new JsonFormatter();

    private String number = "";


    private RegistrationVerificationListener mCallback;
    public static boolean DEBUG_FAKE_UI = false;

    // Our handler for received Intents. This will be called whenever an Intent
    // with an action named "end-vaipo-call" is broadcasted.
    public BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Utils.REGISTRATION_STATUS)) {
                boolean status = intent.getBooleanExtra(Utils.REGISTRATION_STATUS, false);
                if (status) {
                    onComplete();
                } else {
                    onError();
                }
            }
        }
    };

    public VerifyPhoneFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_flags, container, false);
        initUI(rootView);

        progressGenerator = new ProgressGenerator(this);
        btnRegister = (ActionProcessButton) rootView.findViewById(R.id.btn_register);
        btnRegister.setMode(ActionProcessButton.Mode.ENDLESS);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressGenerator.start(btnRegister);
                btnRegister.setEnabled(false);

                sendRegisterMessage();
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
        initCodes(getActivity());

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter(Utils.REGISTRATION_STATUS));
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (RegistrationVerificationListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement LogoutUser");
        }
    }


    @Override
    public void onDestroyView() {
        mCallback = null;
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void send() {
        hideKeyboard(mPhoneEdit);
        mPhoneEdit.setError(null);
        String phone = validate();
        if (phone == null) {
            mPhoneEdit.requestFocus();
            mPhoneEdit.setError(getString(R.string.label_error_incorrect_phone));
            return;
        }
        Toast.makeText(getActivity(), "send to " + phone, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onComplete() {
        if (getActivity() != null)
            Toast.makeText(getActivity(), "Registration Completed", Toast.LENGTH_LONG).show();
        btnRegister.setProgress(100);

        /*mStatusEditText.setTextColor(Color.rgb(89, 113, 173));
        mStatusEditText.setText("Hurray! Your Number : " + number + "  is successfully registered! You are good to use Vaipo service");

        //mSpinner.setVisibility(View.GONE);
        mPhoneEdit.setVisibility(View.GONE);
        btnRegister.setVisibility(View.GONE);*/


        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("number", number + "");
        editor.putBoolean("registered", true);
        editor.commit();

        //if (getActivity() != null)
        //    getActivity().getFragmentManager().beginTransaction().remove(this).commitAllowingStateLoss();

        if (mCallback != null) {
            mCallback.onRegistrationDone();
        }

    }

    @Override
    public void onDismiss() {
        btnRegister.setProgress(50);
        progressGenerator.dismiss(btnRegister);
        mPhoneEdit.requestFocus();
        mPhoneEdit.setError("Registration process Dismissed!");

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("number", number + "");
        editor.putBoolean("registered", false);
        editor.commit();

        if (mCallback != null) {
            mCallback.onRegistrationFailed();
        }
    }

    @Override
    public void onError() {
        //btnRegister.setProgress(50);
        progressGenerator.dismiss(btnRegister);
        mPhoneEdit.requestFocus();
        mPhoneEdit.setError("Registration Error!");

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("number", number + "");
        editor.putBoolean("registered", false);
        editor.commit();


        if (mCallback != null) {
            mCallback.onRegistrationFailed();
        }
    }

    private void sendRegisterMessage() {
        String original = validate();
        number = original.replaceAll("\\s+|-","");
        RegistrationMsg msg = new RegistrationMsg(appState.getID(), number);
        rest.call(RestAPI.REGISTER, formatter.get(msg), new RestAPI.onPostCallBackDone() {
            @Override
            public void onResult(Integer result) {
                Log.d(TAG, "Hurrah");

                Intent intent = new Intent(getActivity(), ActivityEnterCodeDialog.class);
                intent.putExtra("number", number);
                startActivity(intent);
            }
        });
    }
}
