package com.zycoo.android.zphone.ui.me;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.kyleduo.switchbutton.SwitchButton;
import com.zycoo.android.zphone.R;
import com.zycoo.android.zphone.ui.settings.ScreenSettingsAdapter;

/**
 * Created by tqcenglish on 14-12-18.
 */
public class ListViewItemSwitchIconWithTextViewHolder implements View.OnClickListener {

    public final static int switch_icon_with_text_id = R.layout.list_item_switch_icon_with_text;
    private ScreenSettingsAdapter.SettingsListener mSettingsListener;
    private SwitchButton switchButton;
    private TextView name;
    private TextView divider;

    public ListViewItemSwitchIconWithTextViewHolder(View v) {
        name = (TextView) v.findViewById(R.id.id_item_name_tv);
        switchButton = (SwitchButton) v.findViewById(R.id.id_item_switch_bt);
        switchButton.setOnClickListener(this);
        divider = (TextView) v.findViewById(R.id.id_item_divider_tv);
        v.setTag(switch_icon_with_text_id, this);

    }

    public TextView getName() {
        return name;
    }

    public SwitchButton getSwitchButton() {
        return switchButton;
    }

    public void setName(TextView name) {
        this.name = name;
    }

    public void setDividerVisible() {
        divider.setVisibility(View.VISIBLE);
    }

    public void setDividerInVisible() {
        divider.setVisibility(View.INVISIBLE);
    }

    public void setSettingsLiListener(ScreenSettingsAdapter.SettingsListener listener) {
        mSettingsListener = listener;

    }

    public void setChecked(boolean isChecked) {
        mSettingsListener.onCheckedChanged(isChecked);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.id_item_switch_bt:
                mSettingsListener.onCheckedChanged(!switchButton.isChecked());
                break;
        }
    }
}
