
package com.zycoo.android.zphone.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.zycoo.android.zphone.Engine;
import com.zycoo.android.zphone.R;
import com.zycoo.android.zphone.task.RegisterTask;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class IdentitySettingsActivity extends SherlockPreferenceActivity implements
        OnSharedPreferenceChangeListener {

    /**
     * Determines whether to always show the simplified settings UI, where
     * settings are presented in a single list. When false, settings are shown
     * as a master/detail two-pane view on tablets. When true, a single pane is
     * shown on tablets.
     */
    /**
     * 控制单列或多列显示
     */
    private static final boolean ALWAYS_SIMPLE_PREFS = false;
    private final INgnConfigurationService mConfigurationService;
    private Logger logger = LoggerFactory.getLogger(IdentitySettingsActivity.class
            .getCanonicalName());
    public static int REQUEST_CODE_ACCOUNT_PREFERENCES = 0;
    public static int RESULT_CODE_ACCOUNT_PREFERENCES = 0;

    public IdentitySettingsActivity() {
        mConfigurationService = Engine.getInstance().getConfigurationService();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new RegisterTask(this, false).execute();
        // For OS versions honeycomb and higher use action bar
        getSherlock().getActionBar().setDisplayHomeAsUpEnabled(true);
        getPreferenceManager().setSharedPreferencesName(NgnConfigurationEntry.SHARED_PREF_NAME);
        NgnApplication.getContext().getSharedPreferences(NgnConfigurationEntry.SHARED_PREF_NAME,
                0).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setupSimplePreferencesScreen();
    }

    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     */
    @SuppressWarnings("deprecation")
    private void setupSimplePreferencesScreen() {
        if (!isSimplePreferences(this)) {
            return;
        }

        // In the simplified UI, fragments are not used at all and we instead
        // use the older PreferenceActivity APIs.

        // Add 'account' preferences.
        addPreferencesFromResource(R.xml.pref_identity_account);
        bindPreferenceSummaryToValue(findPreference("identity_acount_display_name"));
        bindPreferenceSummaryToValue(findPreference("identity_acount"));
        bindPreferenceSummaryToValue(findPreference("identity_acount_host_port"));
        bindPreferenceSummaryToValue(findPreference("identity_acount_password"));
        // Add 'ami acount' preference.
        /*
        PreferenceCategory preferenceCategory = new PreferenceCategory(this);
        preferenceCategory = new PreferenceCategory(this);
        preferenceCategory.setTitle(R.string.pref_header_identity_ami_acount);
        getPreferenceScreen().addPreference(preferenceCategory);
        addPreferencesFromResource(R.xml.pref_identity_ami);
        bindPreferenceSummaryToValue(findPreference("identity_ami_acount"));
        bindPreferenceSummaryToValue(findPreference("identity_ami_host_port"));
        bindPreferenceSummaryToValue(findPreference("identity_ami_password"));
        */
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this) && !isSimplePreferences(this);
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Determines whether the simplified settings UI should be shown. This is
     * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
     * doesn't have newer APIs like {@link PreferenceFragment}, or the device
     * doesn't have an extra-large screen. In these cases, a single-pane
     * "simplified" settings UI should be shown.
     */
    private static boolean isSimplePreferences(Context context) {
        return ALWAYS_SIMPLE_PREFS
                || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
                || !isXLargeTablet(context);
    }

    /** {@inheritDoc} */
    /**
     * 大屏幕下双显示
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        if (!isSimplePreferences(this)) {
            loadHeadersFromResource(R.xml.pref_headers, target);
        }
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class AcountPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            bindPreferenceSummaryToValue(findPreference("identity_acount_display_name"));
            bindPreferenceSummaryToValue(findPreference("identity_acount"));
            bindPreferenceSummaryToValue(findPreference("identity_acount_host_port"));
            bindPreferenceSummaryToValue(findPreference("identity_acount_password"));

        }
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class AMIPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_identity_ami);
            bindPreferenceSummaryToValue(findPreference("identity_ami_acount"));
            bindPreferenceSummaryToValue(findPreference("identity_ami_host_port"));
            bindPreferenceSummaryToValue(findPreference("identity_ami_password"));

        }
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        /*
         * sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
         * PreferenceManager
         * .getDefaultSharedPreferences(preference.getContext())
         * .getString(preference.getKey(), ""));
         */

        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, Engine
                .getInstance()
                .getConfigurationService().getString(preference.getKey(), ""));

    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            if (preference.getKey().equals("identity_acount_password")) {
                if (!stringValue.equals("")) {
                    preference.setSummary("******");
                }
            } else {
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case "identity_acount_display_name":
                mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_DISPLAY_NAME,
                        mConfigurationService.getString(key, "John Doe"));

                break;
            case "identity_acount":
                String acount = mConfigurationService.getString(key, "804");

                mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_IMPI, acount);
                break;
            case "identity_acount_host_port":
                String[] host_port = mConfigurationService.getString(key, "127.0.0.1:5060").split(
                        ":");
                String host,
                        port;
                if (host_port.length == 2) {
                    host = host_port[0];
                    port = host_port[1];
                } else {
                    host = host_port[0];
                    port = "5060";
                    logger.debug("set default port 5060");
                }
                logger.debug("host: " + host + "port: " + port);
                mConfigurationService.putString(NgnConfigurationEntry.NETWORK_REALM, host);
                mConfigurationService.putString(NgnConfigurationEntry.NETWORK_PCSCF_HOST, host);
                mConfigurationService.putInt(NgnConfigurationEntry.NETWORK_PCSCF_PORT,
                        Integer.parseInt(port));
                break;
            case "identity_acount_password":
                mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_PASSWORD,
                        mConfigurationService.getString(key, "123456"));
                break;
            default:
                break;
        }
        mConfigurationService.putString(
                NgnConfigurationEntry.IDENTITY_IMPU,
                "sip:"
                        + mConfigurationService.getString(NgnConfigurationEntry.IDENTITY_IMPI, "default")
                        + "@"
                        + mConfigurationService.getString(NgnConfigurationEntry.NETWORK_REALM, "127.0.0.1"));
        boolean result = mConfigurationService.commit();
        logger.debug("add_to_contacts config result " + result);
    }

    @Override
    public void onBackPressed() {
        this.setResult(RESULT_OK, null);
        new RegisterTask(true).execute();
        finish();
        startActivity(new Intent(this, MainActivity.class));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            int SUCCESS_RESULT = 1;
            setResult(SUCCESS_RESULT, new Intent());
            new RegisterTask(true).execute();
            finish(); //return to caller
            startActivity(new Intent(this, MainActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
