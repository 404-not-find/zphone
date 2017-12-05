package com.zycoo.android.zphone.ui.settings;

import android.os.Bundle;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.zycoo.android.zphone.Engine;
import com.zycoo.android.zphone.R;
import com.zycoo.android.zphone.ui.me.ListViewItem;
import com.zycoo.android.zphone.ui.me.ListViewItemSpinnerWithText;
import com.zycoo.android.zphone.ui.me.ListViewItemGrey;

import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.tinyWRAP.MediaSessionMgr;
import org.doubango.tinyWRAP.tmedia_srtp_mode_t;
import org.doubango.tinyWRAP.tmedia_srtp_type_t;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScreenSecurityActivity extends BaseScreen {

    private ListViewItem[] items;
    private ListView mListView;
    private BaseAdapter adapter;
    private final INgnConfigurationService mConfigurationService = Engine.getInstance().getConfigurationService();
    private Logger mLogger = LoggerFactory.getLogger(ScreenNetworkActivity.class);

    public final static ScreenSecuritySRtpMode sSpinnerSRtpModeItems[] = new ScreenSecuritySRtpMode[]{
            new ScreenSecuritySRtpMode(tmedia_srtp_mode_t.tmedia_srtp_mode_none, "None"),
            new ScreenSecuritySRtpMode(tmedia_srtp_mode_t.tmedia_srtp_mode_optional, "Optional"),
            new ScreenSecuritySRtpMode(tmedia_srtp_mode_t.tmedia_srtp_mode_mandatory, "Mandatory"),
    };
    public final static ScreenSecuritySRtpType sSpinnerSRtpTypeItems[] = new ScreenSecuritySRtpType[]{
            new ScreenSecuritySRtpType(tmedia_srtp_type_t.tmedia_srtp_type_sdes, "SDES"),
            new ScreenSecuritySRtpType(tmedia_srtp_type_t.tmedia_srtp_type_dtls, "DTLS"),
            new ScreenSecuritySRtpType(tmedia_srtp_type_t.tmedia_srtp_type_sdes_dtls, "BOTH"),
    };


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_listview);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        mListView = (ListView) findViewById(android.R.id.list);
        initData();
        adapter = new ScreenSettingsAdapter(this, items);
        mListView.setAdapter(adapter);
    }

    private void initData() {
        items = new ListViewItem[3];
        items[0] = new ListViewItemGrey(20);
        items[1] = new ListViewItemSpinnerWithText(R.string.srtp_mode, R.layout.list_item_setting_spinner, true,
                ScreenSecuritySRtpMode.getSpinnerIndex(tmedia_srtp_mode_t.valueOf(mConfigurationService.getString(
                        NgnConfigurationEntry.SECURITY_SRTP_MODE,
                        NgnConfigurationEntry.DEFAULT_SECURITY_SRTP_MODE))));
        items[2] = new ListViewItemSpinnerWithText(R.string.srtp_type, R.layout.list_item_setting_spinner, false, ScreenSecuritySRtpType.getSpinnerIndex(tmedia_srtp_type_t.valueOf(mConfigurationService.getString(
                NgnConfigurationEntry.SECURITY_SRTP_TYPE,
                NgnConfigurationEntry.DEFAULT_SECURITY_SRTP_TYPE))));
    }

    protected void onPause() {
        if (super.mComputeConfiguration) {
            for (int i = 0; i < 3; i++) {
                switch (i) {
                    case 1:
                        mConfigurationService.putString(NgnConfigurationEntry.SECURITY_SRTP_MODE,
                                sSpinnerSRtpModeItems[((ListViewItemSpinnerWithText) items[i]).getSelection()].mMode.toString());
                        break;
                    case 2:
                        mConfigurationService.putString(NgnConfigurationEntry.SECURITY_SRTP_TYPE,
                                sSpinnerSRtpTypeItems[((ListViewItemSpinnerWithText) items[i]).getSelection()].mType.toString());
                        break;
                }
            }
            if (!mConfigurationService.commit()) {
                mLogger.debug("Failed to Compute() configuration");
            } else {
                MediaSessionMgr.defaultsSetSRtpMode(sSpinnerSRtpModeItems[((ListViewItemSpinnerWithText) items[1]).getSelection()].mMode);
                MediaSessionMgr.defaultsSetSRtpType(sSpinnerSRtpTypeItems[((ListViewItemSpinnerWithText) items[2]).getSelection()].mType);
            }

            super.mComputeConfiguration = false;
        }
        super.onPause();
    }

    public static class ScreenSecuritySRtpMode {
        private final String mDescription;
        private final tmedia_srtp_mode_t mMode;

        private ScreenSecuritySRtpMode(tmedia_srtp_mode_t mode, String description) {
            mMode = mode;
            mDescription = description;
        }

        @Override
        public String toString() {
            return mDescription;
        }

        @Override
        public boolean equals(Object o) {
            return mMode.equals(((ScreenSecuritySRtpMode) o).mMode);
        }

        static int getSpinnerIndex(tmedia_srtp_mode_t mode) {
            for (int i = 0; i < sSpinnerSRtpModeItems.length; i++) {
                if (mode == sSpinnerSRtpModeItems[i].mMode) {
                    return i;
                }
            }
            return 0;
        }
    }

    public static class ScreenSecuritySRtpType {
        private final String mDescription;
        private final tmedia_srtp_type_t mType;

        private ScreenSecuritySRtpType(tmedia_srtp_type_t type, String description) {
            mType = type;
            mDescription = description;
        }

        @Override
        public String toString() {
            return mDescription;
        }

        @Override
        public boolean equals(Object o) {
            return mType.equals(((ScreenSecuritySRtpType) o).mType);
        }

        static int getSpinnerIndex(tmedia_srtp_type_t type) {
            for (int i = 0; i < sSpinnerSRtpTypeItems.length; i++) {
                if (type == sSpinnerSRtpTypeItems[i].mType) {
                    return i;
                }
            }
            return 0;
        }
    }
}
