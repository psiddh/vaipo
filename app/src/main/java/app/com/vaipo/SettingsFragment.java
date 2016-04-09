

package app.com.vaipo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import app.com.vaipo.Utils.Utils;

public class SettingsFragment extends PreferenceFragment {
    private String mRegNumber = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        updateRegStatus(getPreferenceManager().findPreference("checkbox_preference"));

        SharedPreferences.OnSharedPreferenceChangeListener spChanged = new
                SharedPreferences.OnSharedPreferenceChangeListener() {
                    @Override
                    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                                          String key) {
                        if (key.equals("registered")) {
                            updateRegStatus(getPreferenceScreen().findPreference("checkbox_preference"));
                        }
                    }
                };

        if (!Utils.isSimReady(getActivity())) {
            CheckBoxPreference checkBoxPreference = (CheckBoxPreference) getPreferenceManager().findPreference("enable_incall");
            checkBoxPreference.setSummary("SIM State is either Bad / Unknown (or) \n SIM is Not Present. Disabling this feature");
            checkBoxPreference.setEnabled(false);
        } else {
            CheckBoxPreference checkBoxPreference = (CheckBoxPreference) getPreferenceManager().findPreference("enable_incall");
            checkBoxPreference.setSummary("During regular PSTN calls. \n (Note that audio is still through PSTN calls only)");
            checkBoxPreference.setEnabled(true);
            checkBoxPreference.setChecked(true);
        }


    }

    private void updateRegStatus(Preference p) {
        mRegNumber = (String) Utils.getPref(getActivity(), "number");
        String statusText = "";
        Integer type = (Integer) Utils.getPref(getActivity(), "reg_type");
        String typeText = (type == Utils.REGISTER_TYPE_NUMBER) ? "Phone Number" : "Email ID";
        if (mRegNumber != null && !mRegNumber.isEmpty()) {
            statusText = /*typeText + ", " + */ mRegNumber + " is registered";
        } else {
            statusText = "Unknown / UnRegistered " + typeText;
        }
        //Utils.putPref(getActivity(), "edittext_preference", statusText);
        updatePrefSummary(p, statusText);

    }

    private void updatePrefSummary(Preference p, Object val) {
        if (p instanceof ListPreference) {
            ListPreference listPref = (ListPreference) p;
            //p.setSummary(listPref.getEntry());
        }
        if (p instanceof EditTextPreference) {
            EditTextPreference editTextPref = (EditTextPreference) p;
            /*if (editTextPref.getKey().equals("edittext_preference")) {
                editTextPref.setSummary((String) val);
            }*/

        }

        if (p instanceof LongSummaryCheckboxPreference) {
            CheckBoxPreference checkBoxPreference = (CheckBoxPreference) p;
            if (checkBoxPreference.getKey().equals("checkbox_preference")) {
                checkBoxPreference.setTitle((String) val);

                if (mRegNumber != null && !mRegNumber.isEmpty()) {
                    checkBoxPreference.setChecked(true);
                    checkBoxPreference.setEnabled(false);
                } else {
                    checkBoxPreference.setChecked(false);
                    checkBoxPreference.setEnabled(false);
                }
            }
        }
    }
}
