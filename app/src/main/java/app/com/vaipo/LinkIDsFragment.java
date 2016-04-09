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

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import com.dd.processbutton.iml.ActionProcessButton;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;

import app.com.vaipo.Utils.ProgressGenerator;
import app.com.vaipo.Utils.Utils;
import app.com.vaipo.appState.AppState;
import app.com.vaipo.config.OpenTokConfig;
import app.com.vaipo.format.JsonFormatter;
import app.com.vaipo.messages.DialMsg;
import app.com.vaipo.rest.RestAPI;

public class LinkIDsFragment extends Fragment implements ProgressGenerator.OnCompleteListener {

    private final String TAG = "LinkIDsFragment";
    private JsonFormatter formatter = new JsonFormatter();

    private EditText email;

    private ActionProcessButton registerId_btn;
    private ProgressGenerator progressGenerator;

    private RegistrationVerificationListener mCallback;

    public static final int REQUEST_CODE_EMAIL = 10001;

    public LinkIDsFragment() {

    }

    @Override
    public void onCreate(Bundle instance) {
        super.onCreate(instance);
        setHasOptionsMenu(true);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.link_ids_ui, container, false);
        email = (EditText) rootView.findViewById(R.id.email_text);
        email.setShowSoftInputOnFocus(true);
        try {
            Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                    new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, false, null, null, null, null);
            startActivityForResult(intent, REQUEST_CODE_EMAIL);
        } catch (ActivityNotFoundException e) {
            // TODO
        }

        progressGenerator = new ProgressGenerator(this);
        registerId_btn = (ActionProcessButton) rootView.findViewById(R.id.register_id);
        registerId_btn.setMode(ActionProcessButton.Mode.ENDLESS);
        registerId_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String regID = email.getText().toString();
                if (regID.isEmpty()) {
                    return;
                }
                progressGenerator.start(registerId_btn);
                formatter.initialize();
                Utils.sendRegisterMessage(getActivity(), regID, Utils.REGISTER_TYPE_EMAILID, new RestAPI(), formatter, new Utils.onRegCallback() {
                    @Override
                    public void onDone(int status, Object result) {
                        if (status == Utils.OK) {
                            registerId_btn.setText("Registered!");
                            if (mCallback != null) {
                                mCallback.onRegistrationDone();
                            }
                        } else {
                            registerId_btn.setText("Not Registered!");
                            if (mCallback != null) {
                                mCallback.onRegistrationFailed();
                            }
                        }
                        progressGenerator.complete(registerId_btn);
                        getActivity().getFragmentManager().popBackStack();
                        ((RegisterPhoneNumberActivity )getActivity()).onPostResume();
                    }
                });
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (RegistrationVerificationListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement LogoutUser");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CODE_EMAIL) {
            if(resultCode == Activity.RESULT_OK){
                String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                Log.d(TAG, "accountName : " + accountName);
                email.setText(accountName);
                email.setSelection(accountName.length());
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                if (mCallback != null) {
                    mCallback.onRegistrationFailed();
                }
            }
        }
    }
}
