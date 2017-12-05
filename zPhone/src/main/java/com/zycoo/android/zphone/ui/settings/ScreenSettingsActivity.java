package com.zycoo.android.zphone.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.zycoo.android.zphone.R;
import com.zycoo.android.zphone.ui.me.ListViewItem;
import com.zycoo.android.zphone.ui.me.ListViewItemAvatarWithText;
import com.zycoo.android.zphone.ui.me.ListViewItemTextOnly;
import com.zycoo.android.zphone.ui.me.ListViewItemGrey;
import com.zycoo.android.zphone.utils.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScreenSettingsActivity extends BaseScreen implements OnItemClickListener {
    private static final int PRO_ITEMS_SIZE = 8;
    private static final int FREE_ITEMS_SIZE = 6;
    private int mItemsSize;
    private ListViewItem[] items;
    private ListView mListView;
    private BaseAdapter adapter;
    private Logger mLogger = LoggerFactory.getLogger(ScreenSettingsActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        //geActionBar().setDisplayHomeAsUpEnabled(true);
        mListView = (ListView) findViewById(R.id.setting_listView);
        isFreeVersion();
        items = new ListViewItem[mItemsSize];
        initData();
        adapter = new ScreenSettingsAdapter(this, items);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(this);
    }

    public void isFreeVersion() {
        if (Utils.isPro(this)) {
            mItemsSize = PRO_ITEMS_SIZE;
        }
        else
        {
            mItemsSize = FREE_ITEMS_SIZE;
        }
    }

    public void initData() {
        items[0] = new ListViewItemGrey(20);
        items[1] = new ListViewItemAvatarWithText(R.string.general, R.drawable.ic_settings_input_composite_white, true);
        items[2] = new ListViewItemAvatarWithText(R.string.network, R.drawable.ic_settings_input_antenna_white, true);
        items[3] = new ListViewItemAvatarWithText(R.string.security, R.drawable.ic_lock_white, false);
        items[4] = new ListViewItemGrey(40);
        if (Utils.isPro(this)) {
            items[5] = new ListViewItemTextOnly(R.string.codecs, true);
            items[6] = new ListViewItemTextOnly(R.string.qos_qoe, true);
            items[7] = new ListViewItemTextOnly(R.string.natt, false);
        }
        else
        {
            items[5] = new ListViewItemTextOnly(R.string.codecs, false);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Intent intent = null;
        switch (position) {
            case 1:
                intent = new Intent(this, ScreenGeneralActivity.class);
                break;
            case 2:
                intent = new Intent(this, ScreenNetworkActivity.class);
                break;
            case 3:
                intent = new Intent(this, ScreenSecurityActivity.class);
                break;
            case 5:
                intent = new Intent(this, ScreenCodecsActivity.class);
                break;
            case 6:
                intent = new Intent(this, ScreenQoS.class);
                break;
            case 7:
                intent = new Intent(this, ScreenNattActivity.class);
                break;

        }
        if (null != intent) {
            startActivity(intent);
        }
    }


    @Override
    public void onBackPressed() {
        finish();
        return;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            int SUCCESS_RESULT = 1;
            setResult(SUCCESS_RESULT, new Intent());
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
