package com.zycoo.android.zphone.ui.settings;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.zycoo.android.zphone.Engine;
import com.zycoo.android.zphone.R;
import com.zycoo.android.zphone.ZphoneApplication;
import com.zycoo.android.zphone.ui.me.ListViewItem;
import com.zycoo.android.zphone.ui.me.ListViewItemSpinnerWithText;
import com.zycoo.android.zphone.ui.me.ListViewItemSwitchIconWithText;
import com.zycoo.android.zphone.ui.me.ListViewItemSwitchIconWithTextViewHolder;
import com.zycoo.android.zphone.ui.me.ListViewItemWhite;
import com.zycoo.android.zphone.utils.Utils;

import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.tinyWRAP.MediaSessionMgr;
import org.doubango.tinyWRAP.tmedia_profile_t;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by tqcenglish on 14-12-18.
 */
public class ScreenGeneralActivity extends BaseScreen implements AdapterView.OnItemClickListener {
    private ListViewItem[] items;
    private ListView mListView;
    private BaseAdapter adapter;
    private final INgnConfigurationService mConfigurationService = Engine.getInstance().getConfigurationService();
    private Logger mLogger = LoggerFactory.getLogger(ScreenGeneralActivity.class);
    public final static AudioPlayBackLevel[] sAudioPlaybackLevels = new AudioPlayBackLevel[]{
            new AudioPlayBackLevel(0.25f, R.string.low),
            new AudioPlayBackLevel(0.50f, R.string.medium),
            new AudioPlayBackLevel(0.75f, R.string.high),
            new AudioPlayBackLevel(1.0f, R.string.maximum),
    };
    public final static Profile[] sProfiles = new Profile[]
            {
                    new Profile(tmedia_profile_t.tmedia_profile_default, R.string.default_defined),
                    new Profile(tmedia_profile_t.tmedia_profile_rtcweb, R.string.rtc_web)
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listview);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mListView = (ListView) findViewById(android.R.id.list);
        items = new ListViewItem[12];
        new InitDataTask().execute();

        adapter = new ScreenSettingsAdapter(this, items);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(this);
    }

    public void isFreeVersion() {
        if (!Utils.isPro(this)) {
            ((ListViewItemSwitchIconWithText) items[7]).setVisible(false);
            ((ListViewItemSwitchIconWithText) items[8]).setVisible(false);
            ((ListViewItemSpinnerWithText) items[10]).setVisible(false);
            ((ListViewItemSpinnerWithText) items[11]).setVisible(false);
        }
    }

    private class InitDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPostExecute(Void aVoid) {
            isFreeVersion();
            adapter.notifyDataSetInvalidated();
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... params) {
            initData();
            return null;
        }
    }

    public void initData() {
        items[0] = new ListViewItemWhite(20);
        items[1] = new ListViewItemSwitchIconWithText(R.string.enable_aec, true, mConfigurationService.getBoolean(NgnConfigurationEntry.GENERAL_AEC, NgnConfigurationEntry.DEFAULT_GENERAL_AEC));
        items[2] = new ListViewItemSwitchIconWithText(R.string.voice_activity_detector, true, mConfigurationService.getBoolean(NgnConfigurationEntry.GENERAL_VAD, NgnConfigurationEntry.DEFAULT_GENERAL_VAD));
        items[3] = new ListViewItemSwitchIconWithText(R.string.noise_reduction, true, mConfigurationService.getBoolean(NgnConfigurationEntry.GENERAL_NR, NgnConfigurationEntry.DEFAULT_GENERAL_NR));
        items[4] = new ListViewItemSwitchIconWithText(R.string.launch_when_system_starts, true, mConfigurationService.getBoolean(NgnConfigurationEntry.GENERAL_AUTOSTART, NgnConfigurationEntry.DEFAULT_GENERAL_AUTOSTART));
        items[5] = new ListViewItemSwitchIconWithText(R.string.intercept_outgoing_calls, false, mConfigurationService.getBoolean(NgnConfigurationEntry.GENERAL_INTERCEPT_OUTGOING_CALLS, NgnConfigurationEntry.DEFAULT_GENERAL_INTERCEPT_OUTGOING_CALLS));
        items[6] = new ListViewItemWhite(40);
        items[7] = new ListViewItemSwitchIconWithText(R.string.enable_video_full_screen_mode, true, mConfigurationService.getBoolean(NgnConfigurationEntry.GENERAL_FULL_SCREEN_VIDEO, NgnConfigurationEntry.DEFAULT_GENERAL_FULL_SCREEN_VIDEO));
        items[8] = new ListViewItemSwitchIconWithText(R.string.enable_front_facing_camera, false, mConfigurationService.getBoolean(NgnConfigurationEntry.GENERAL_USE_FFC, NgnConfigurationEntry.DEFAULT_GENERAL_USE_FFC));
        items[9] = new ListViewItemWhite(40);
        items[10] = new ListViewItemSpinnerWithText(R.string.media_profile, R.layout.list_item_setting_spinner, true, Profile.getSpinnerIndex(tmedia_profile_t.valueOf(mConfigurationService.getString(
                NgnConfigurationEntry.MEDIA_PROFILE,
                NgnConfigurationEntry.DEFAULT_MEDIA_PROFILE))));
        items[11] = new ListViewItemSpinnerWithText(R.string.audio_playback_level, R.layout.list_item_setting_spinner, false, AudioPlayBackLevel.getSpinnerIndex(
                mConfigurationService.getFloat(
                        NgnConfigurationEntry.GENERAL_AUDIO_PLAY_LEVEL,
                        NgnConfigurationEntry.DEFAULT_GENERAL_AUDIO_PLAY_LEVEL)));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 7:
            case 8:
                ListViewItemSwitchIconWithTextViewHolder switchIconWithTextViewHolder = (ListViewItemSwitchIconWithTextViewHolder) view.getTag(ListViewItemSwitchIconWithTextViewHolder.switch_icon_with_text_id);
                switchIconWithTextViewHolder.setChecked(!switchIconWithTextViewHolder.getSwitchButton().isChecked());
                switchIconWithTextViewHolder.getSwitchButton().toggle();
                break;
            default:
                mLogger.debug("not use item");
                break;
        }
    }

    @Override
    protected void onPause() {
        if (super.mComputeConfiguration) {
            for (int i = 0; i < 12; i++) {
                switch (i) {
                    case 1:
                        mConfigurationService.putBoolean(NgnConfigurationEntry.GENERAL_AEC, ((ListViewItemSwitchIconWithText) items[i]).isChecked());
                        break;
                    case 2:
                        mConfigurationService.putBoolean(NgnConfigurationEntry.GENERAL_VAD, ((ListViewItemSwitchIconWithText) items[i]).isChecked());
                        break;
                    case 3:
                        mConfigurationService.putBoolean(NgnConfigurationEntry.GENERAL_NR, ((ListViewItemSwitchIconWithText) items[i]).isChecked());
                        break;
                    case 4:
                        mConfigurationService.putBoolean(NgnConfigurationEntry.GENERAL_AUTOSTART, ((ListViewItemSwitchIconWithText) items[i]).isChecked());
                        break;

                    case 5:
                        mConfigurationService.putBoolean(NgnConfigurationEntry.GENERAL_INTERCEPT_OUTGOING_CALLS, ((ListViewItemSwitchIconWithText) items[i]).isChecked());
                        break;

                    case 7:
                        mConfigurationService.putBoolean(NgnConfigurationEntry.GENERAL_FULL_SCREEN_VIDEO, ((ListViewItemSwitchIconWithText) items[i]).isChecked());
                        break;
                    case 8:
                        mConfigurationService.putBoolean(NgnConfigurationEntry.GENERAL_USE_FFC, ((ListViewItemSwitchIconWithText) items[i]).isChecked());
                        break;
                    case 10:
                        // profile should be moved to another screen (e.g. Media)
                        mConfigurationService.putString(NgnConfigurationEntry.MEDIA_PROFILE, sProfiles[((ListViewItemSpinnerWithText) items[i]).getSelection()].mValue.toString());
                        break;
                    case 11:
                        mConfigurationService.putFloat(NgnConfigurationEntry.GENERAL_AUDIO_PLAY_LEVEL, sAudioPlaybackLevels[((ListViewItemSpinnerWithText) items[i]).getSelection()].mValue);
                        break;

                }
            }
            if (!mConfigurationService.commit()) {
                mLogger.error("Failed to commit() configuration");
            } else {
                // codecs, AEC, NoiseSuppression, Echo cancellation, ....
                boolean aec = ((ListViewItemSwitchIconWithText) items[1]).isChecked();
                boolean vad = ((ListViewItemSwitchIconWithText) items[2]).isChecked();
                boolean nr = ((ListViewItemSwitchIconWithText) items[3]).isChecked();
                int echo_tail = mConfigurationService.getInt(NgnConfigurationEntry.GENERAL_ECHO_TAIL, NgnConfigurationEntry.DEFAULT_GENERAL_ECHO_TAIL);
                mLogger.debug("Configure AEC[" + aec + "/" + echo_tail + "] NoiseSuppression[" + nr + "], Voice activity detection[" + vad + "]");
                if (aec) {
                    MediaSessionMgr.defaultsSetEchoSuppEnabled(true);
                    MediaSessionMgr.defaultsSetEchoTail(echo_tail); // 2s  == 100 packets of 20 ms
                } else {
                    MediaSessionMgr.defaultsSetEchoSuppEnabled(false);
                    MediaSessionMgr.defaultsSetEchoTail(0);
                }
                MediaSessionMgr.defaultsSetVadEnabled(vad);
                MediaSessionMgr.defaultsSetNoiseSuppEnabled(nr);
                MediaSessionMgr.defaultsSetProfile(sProfiles[((ListViewItemSpinnerWithText) items[10]).getSelection()].mValue);
            }
            super.mComputeConfiguration = false;
        }
        super.onPause();
    }

    public static class Profile {
        tmedia_profile_t mValue;
        String mDescription;

        Profile(tmedia_profile_t value, int description) {
            mValue = value;
            mDescription = ZphoneApplication.getAppResources().getString(description);
        }

        @Override
        public String toString() {
            return mDescription;
        }

        static int getSpinnerIndex(tmedia_profile_t value) {
            for (int i = 0; i < sProfiles.length; i++) {
                if (sProfiles[i].mValue == value) {
                    return i;
                }
            }
            return 0;
        }
    }

    public static class AudioPlayBackLevel {
        float mValue;
        String mDescription;

        AudioPlayBackLevel(float value, int description) {
            mValue = value;
            mDescription = ZphoneApplication.getAppResources().getString(description);
        }

        @Override
        public String toString() {
            return mDescription;
        }

        static int getSpinnerIndex(float value) {
            for (int i = 0; i < sAudioPlaybackLevels.length; i++) {
                if (sAudioPlaybackLevels[i].mValue == value) {
                    return i;
                }
            }
            return 0;
        }
    }
}
