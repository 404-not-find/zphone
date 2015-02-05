package com.zycoo.android.zphone.ui.settings;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.zycoo.android.zphone.Engine;
import com.zycoo.android.zphone.R;
import com.zycoo.android.zphone.ui.me.ListViewItem;
import com.zycoo.android.zphone.ui.me.ListViewItemSpinnerWithText;
import com.zycoo.android.zphone.ui.me.ListViewItemSwitchIconWithText;
import com.zycoo.android.zphone.ui.me.ListViewItemSwitchIconWithTextViewHolder;
import com.zycoo.android.zphone.ui.me.ListViewItemGrey;

import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScreenNetworkActivity
        extends BaseScreen implements AdapterView.OnItemClickListener {

    private ListViewItem[] items;
    private ListView mListView;
    private BaseAdapter adapter;
    private final INgnConfigurationService mConfigurationService = Engine.getInstance().getConfigurationService();
    private Logger mLogger = LoggerFactory.getLogger(ScreenNetworkActivity.class);
    public final static String[] sSpinnerTransportItems = new String[]
            {
                    NgnConfigurationEntry.DEFAULT_NETWORK_TRANSPORT.toUpperCase(),
                    "TCP"
                    ,
                    "TLS"/* , "SCTP"*/
            };

    private final static String[] sSpinnerProxydiscoveryItems =
            new String[]
                    {
                            NgnConfigurationEntry.DEFAULT_NETWORK_PCSCF_DISCOVERY,
                            NgnConfigurationEntry.PCSCF_DISCOVERY_DNS_SRV
            /*
             * , "DHCPv4/v6",
             * "Both"
             */};

    public void initData() {
        items = new ListViewItem[7];
        items[0] = new ListViewItemGrey(20);
        items[1] = new ListViewItemSwitchIconWithText(R.string.enable_wifi, true, mConfigurationService.getBoolean(
                NgnConfigurationEntry.NETWORK_USE_WIFI,
                NgnConfigurationEntry.DEFAULT_NETWORK_USE_WIFI));
        items[2] = new ListViewItemSwitchIconWithText(R.string.enable_mobile_network, false, mConfigurationService.getBoolean(
                NgnConfigurationEntry.NETWORK_USE_3G,
                NgnConfigurationEntry.DEFAULT_NETWORK_USE_3G));
        items[3] = new ListViewItemGrey(40);

        items[4] = new ListViewItemSwitchIconWithText(R.string.enable_ipv6, false, mConfigurationService.getString(
                NgnConfigurationEntry.NETWORK_IP_VERSION,
                NgnConfigurationEntry.DEFAULT_NETWORK_IP_VERSION).equalsIgnoreCase(
                "ipv6"));

        items[5] = new ListViewItemGrey(40);
        items[6] = new ListViewItemSpinnerWithText(R.string.transport, R.layout.list_item_setting_spinner, false, super.getSpinnerIndex(mConfigurationService
                .getString(NgnConfigurationEntry.NETWORK_TRANSPORT,
                        sSpinnerTransportItems[0]), sSpinnerTransportItems));


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_listview);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mListView = (ListView) findViewById(android.R.id.list);
        initData();
        adapter = new ScreenSettingsAdapter(this, items);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(this);
    }

    protected void onPause() {
        if (super.mComputeConfiguration) {
            for (int i = 0; i < 7; i++) {
                switch (i) {
                    case 1:
                        mConfigurationService.putBoolean(NgnConfigurationEntry.NETWORK_USE_WIFI, ((ListViewItemSwitchIconWithText) items[i]).isChecked());
                        break;
                    case 2:
                        mConfigurationService.putBoolean(NgnConfigurationEntry.NETWORK_USE_3G, ((ListViewItemSwitchIconWithText) items[i]).isChecked());
                        break;
                    case 4:
                        mConfigurationService.putString(NgnConfigurationEntry.NETWORK_IP_VERSION, ((ListViewItemSwitchIconWithText) items[i]).isChecked() ? "ipv6" : "ipv4");
                        break;

                    case 6:
                        mConfigurationService.putString(NgnConfigurationEntry.NETWORK_TRANSPORT,
                                sSpinnerTransportItems[((ListViewItemSpinnerWithText) items[i]).getSelection()]);
                        break;
                }
            }
            // Compute
            if (!mConfigurationService.commit()) {
                mLogger.debug("Failed to commit() configuration");
            }
            super.mComputeConfiguration = false;
        }
        super.onPause();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ListViewItemSwitchIconWithTextViewHolder switchIconWithTextViewHolder = (ListViewItemSwitchIconWithTextViewHolder) view.getTag(ListViewItemSwitchIconWithTextViewHolder.switch_icon_with_text_id);
        if (null != switchIconWithTextViewHolder) {
            switchIconWithTextViewHolder.getSwitchButton().toggle();
            switchIconWithTextViewHolder.setChecked(!switchIconWithTextViewHolder.getSwitchButton().isChecked());
        }
    }
}
