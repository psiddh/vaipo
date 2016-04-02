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
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.codinguser.android.contactpicker.ContactsPickerActivity;
import com.firebase.client.Firebase;

import app.com.vaipo.Utils.Utils;


public class RegisterPhoneNumberActivity extends Activity implements ContactsListenerAction, RegistrationVerificationListener {
    public static final int GET_PHONE_NUMBER = 3007;

    private boolean mIsRegisteredAlready = false;

    private ContactsFragment mContactsFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);
        setContentView(R.layout.activity_flags);
        if (savedInstanceState == null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            mIsRegisteredAlready = prefs.getBoolean("registered", false);

            if (!mIsRegisteredAlready) {
                getFragmentManager().beginTransaction()
                        .add(R.id.container, new VerifyPhoneFragment())
                        .commit();
            } else {
                mContactsFragment = new ContactsFragment();
                getFragmentManager().beginTransaction()
                        .add(R.id.container, mContactsFragment)
                        .commit();
            }
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.flags, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onContactsBtnClicked() {
        Utils.showContact(this, GET_PHONE_NUMBER);
    }

    @Override
    public void onContactsSelectedResult(Object result) {

    }

    @Override
    public void onActionCancel() {

    }

    // Listen for results.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        // See which child activity is calling us back.
        switch (requestCode) {
            case GET_PHONE_NUMBER:
                // This is the standard resultCode that is sent back if the
                // activity crashed or didn't doesn't supply an explicit result.
                if (resultCode == RESULT_CANCELED){
                    Toast.makeText(this, "No phone number found", Toast.LENGTH_SHORT).show();
                    mContactsFragment.onUserSelectionResult("", "");
                }
                else {
                    String phoneNumber = (String) data.getExtras().get(ContactsPickerActivity.KEY_PHONE_NUMBER);
                    String contactName = (String) data.getExtras().get(ContactsPickerActivity.KEY_CONTACT_NAME);

                    mContactsFragment.onUserSelectionResult(phoneNumber, contactName);
                    //Do what you wish to do with phoneNumber e.g.
                    //Toast.makeText(this, "Phone number found: " + phoneNumber , Toast.LENGTH_SHORT).show();
                }
            default:
                break;
        }
    }

    @Override
    public void onRegistrationDone() {
        mIsRegisteredAlready = true;
        mContactsFragment = new ContactsFragment();
        getFragmentManager().beginTransaction()
                .add(R.id.container, mContactsFragment)
                .commit();
    }

    @Override
    public void onRegistrationFailed() {
        mIsRegisteredAlready = false;
    }
}
