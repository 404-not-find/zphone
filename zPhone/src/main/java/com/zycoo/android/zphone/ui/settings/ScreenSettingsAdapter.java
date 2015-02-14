package com.zycoo.android.zphone.ui.settings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import com.zycoo.android.zphone.R;
import com.zycoo.android.zphone.ui.me.ListViewItem;
import com.zycoo.android.zphone.ui.me.ListViewItemAvatarWithText;
import com.zycoo.android.zphone.ui.me.ListViewItemAvatarWithTextViewHolder;
import com.zycoo.android.zphone.ui.me.ListViewItemSpinnerWithText;
import com.zycoo.android.zphone.ui.me.ListViewItemSpinnerWithTextViewHolder;
import com.zycoo.android.zphone.ui.me.ListViewItemSwitchIconWithText;
import com.zycoo.android.zphone.ui.me.ListViewItemSwitchIconWithTextViewHolder;
import com.zycoo.android.zphone.ui.me.ListViewItemTextOnly;
import com.zycoo.android.zphone.ui.me.ListViewItemTextOnlyViewHolder;
import com.zycoo.android.zphone.ui.me.ListViewItemGrey;
import com.zycoo.android.zphone.ui.me.ListViewItemGreyViewHolder;
import com.zycoo.android.zphone.utils.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tqcenglish on 14-12-18.
 */
public class ScreenSettingsAdapter extends BaseAdapter {
    private Logger mLogger = LoggerFactory.getLogger(ScreenSettingsActivity.class);
    private Context context;
    private ListViewItem[] objects;

    public interface SettingsListener {
        public void onCheckedChanged(boolean isChecked);
    }

    public ScreenSettingsAdapter(Context context, ListViewItem[] objects) {
        this.context = context;
        this.objects = objects;
    }

    @Override
    public int getCount() {
        return objects.length;
    }

    @Override
    public Object getItem(int position) {
        return objects[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return ListViewItem.getTypeCount();
    }

    @Override
    public int getItemViewType(int position) {
        return objects[position].getType();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ListViewItemGreyViewHolder whiteViewHolder = null;
        ListViewItemAvatarWithTextViewHolder avatarWithTextViewHolder = null;
        ListViewItemTextOnlyViewHolder textOnlyViewHolder = null;
        ListViewItemSwitchIconWithTextViewHolder switchIconWithTextViewHolder = null;
        ListViewItemSpinnerWithTextViewHolder spinnerWithTextViewHolder = null;
        ListViewItem listViewItem = objects[position];
        int listViewItemType = getItemViewType(position);
            switch (listViewItemType) {
                case ListViewItem.TYPE_GREY:
                    if (convertView == null || null == (ListViewItemGreyViewHolder) convertView.getTag(ListViewItemGreyViewHolder.grey_divider_id)) {
                        convertView = LayoutInflater.from(context).inflate(ListViewItemGreyViewHolder.grey_divider_id, parent, false);
                        whiteViewHolder = new ListViewItemGreyViewHolder(convertView);
                    } else {
                        whiteViewHolder = (ListViewItemGreyViewHolder) convertView.getTag(ListViewItemGreyViewHolder.grey_divider_id);
                    }
                    int height_dp = ((ListViewItemGrey) listViewItem).getItemHeight();
                    whiteViewHolder.setWhiteDividerHeight((int) Utils.convertDpToPixel(height_dp, context));
                    convertView.setClickable(false);
                    break;
                case ListViewItem.TYPE_AVATAR_WITH_TEXT:
                    ListViewItemAvatarWithText itemAvatarWithText = ((ListViewItemAvatarWithText) listViewItem);
                    if (convertView == null || null == (ListViewItemAvatarWithTextViewHolder) convertView.getTag(ListViewItemAvatarWithTextViewHolder.avatar_with_text_id)) {
                        convertView = LayoutInflater.from(context).inflate(ListViewItemAvatarWithTextViewHolder.avatar_with_text_id, parent, false);
                        avatarWithTextViewHolder = new ListViewItemAvatarWithTextViewHolder(convertView);
                    } else {
                        avatarWithTextViewHolder = (ListViewItemAvatarWithTextViewHolder) convertView.getTag(ListViewItemAvatarWithTextViewHolder.avatar_with_text_id);
                    }

                    avatarWithTextViewHolder.getAvatar().setImageResource(itemAvatarWithText.getAvatarResId());
                    avatarWithTextViewHolder.getName().setText(itemAvatarWithText.getNameResId());
                    if (
                            itemAvatarWithText.isDividerVisible()
                            ) {
                        avatarWithTextViewHolder.setDividerVisible();
                    } else {
                        avatarWithTextViewHolder.setDividerInVisible();
                    }
                    break;
                case ListViewItem.TYPE_TEXT_ONLY:
                    ListViewItemTextOnly itemTextOnly = (ListViewItemTextOnly) objects[position];
                    if (convertView == null || null == (ListViewItemTextOnlyViewHolder) convertView.getTag(ListViewItemTextOnlyViewHolder.text_only_id)) {
                        convertView = LayoutInflater.from(context).inflate(ListViewItemTextOnlyViewHolder.text_only_id, parent, false);
                        textOnlyViewHolder = new ListViewItemTextOnlyViewHolder(convertView);
                    } else {
                        textOnlyViewHolder = (ListViewItemTextOnlyViewHolder) convertView.getTag(ListViewItemTextOnlyViewHolder.text_only_id);
                    }
                    textOnlyViewHolder.getName().setText(itemTextOnly.getNameResId());
                    if (
                            itemTextOnly.isDividerVisible()
                            ) {
                        textOnlyViewHolder.setDividerVisible();
                    } else {
                        textOnlyViewHolder.setDividerInVisible();
                    }

                    break;
                case ListViewItem.TYPE_SPINNER_WITH_TEXT:
                    final ListViewItemSpinnerWithText spinnerWithText = (ListViewItemSpinnerWithText) objects[position];
                    if (convertView == null || null == (ListViewItemSpinnerWithTextViewHolder) convertView.getTag(ListViewItemSpinnerWithTextViewHolder.spinner_with_text_id)) {
                        convertView = LayoutInflater.from(context).inflate(ListViewItemSpinnerWithTextViewHolder.spinner_with_text_id, parent, false);
                        spinnerWithTextViewHolder = new ListViewItemSpinnerWithTextViewHolder(convertView);
                    } else {
                        spinnerWithTextViewHolder = (ListViewItemSpinnerWithTextViewHolder) convertView.getTag(ListViewItemSpinnerWithTextViewHolder.spinner_with_text_id);
                    }
                    spinnerWithTextViewHolder.getName().setText(spinnerWithText.getNameResId());
                    if (
                            spinnerWithText.isDividerVisible()
                            ) {
                        spinnerWithTextViewHolder.setDividerVisible();
                    } else {
                        spinnerWithTextViewHolder.setDividerInVisible();
                    }
                    List<Map<String, Object>> listMap = new ArrayList<>();
                    switch (spinnerWithText.getNameResId()) {
                        case R.string.audio_playback_level:
                            for (ScreenGeneralActivity.AudioPlayBackLevel level : ScreenGeneralActivity.sAudioPlaybackLevels) {
                                Map<String, Object> map = new HashMap<>();
                                map.put("value_name", level.toString());
                                listMap.add(map);
                            }
                            break;
                        case R.string.media_profile:
                            for (ScreenGeneralActivity.Profile profile : ScreenGeneralActivity.sProfiles) {
                                Map<String, Object> map = new HashMap<>();
                                map.put("value_name", profile.toString());
                                listMap.add(map);
                            }
                            break;
                        case R.string.transport:
                            for (String str : ScreenNetworkActivity.sSpinnerTransportItems) {
                                Map<String, Object> map = new HashMap<>();
                                map.put("value_name", str);
                                listMap.add(map);
                            }
                            break;
                        case R.string.srtp_mode:
                            for (ScreenSecurityActivity.ScreenSecuritySRtpMode mode : ScreenSecurityActivity.sSpinnerSRtpModeItems) {
                                Map<String, Object> map = new HashMap<>();
                                map.put("value_name", mode.toString());
                                listMap.add(map);
                            }
                            break;
                        case R.string.srtp_type:
                            for (ScreenSecurityActivity.ScreenSecuritySRtpType type : ScreenSecurityActivity.sSpinnerSRtpTypeItems) {
                                Map<String, Object> map = new HashMap<>();
                                map.put("value_name", type.toString());
                                listMap.add(map);
                            }
                            break;
                    }

                    spinnerWithTextViewHolder.getSpinner().setAdapter(new SpinnerSettingsAdapter(context, listMap, spinnerWithText.getSpinnerResId(), new String[]{
                            "value_name",
                    }, new int[]{
                            R.id.list_item_spinner_tv
                    }));
                    spinnerWithTextViewHolder.getSpinner().setSelection(spinnerWithText.getSelection());
                    // not more than one use
                    // ((BaseScreen) context).addConfigurationListener(spinnerWithTextViewHolder.getSpinner());
                    spinnerWithTextViewHolder.getSpinner().setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            spinnerWithText.setSelection(position);
                            ((BaseScreen) context).setComputeConfiguration();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                    break;
                case ListViewItem.TYPE_SWITCH_ICON_WITH_TEXT:
                    final ListViewItemSwitchIconWithText switchIconWithText = (ListViewItemSwitchIconWithText) objects[position];
                    if (convertView == null || null == (ListViewItemSwitchIconWithTextViewHolder) convertView.getTag(ListViewItemSwitchIconWithTextViewHolder.switch_icon_with_text_id)) {
                        convertView = LayoutInflater.from(context).inflate(ListViewItemSwitchIconWithTextViewHolder.switch_icon_with_text_id, parent, false);
                        switchIconWithTextViewHolder = new ListViewItemSwitchIconWithTextViewHolder(convertView);
                    } else {
                        switchIconWithTextViewHolder = (ListViewItemSwitchIconWithTextViewHolder) convertView.getTag(ListViewItemSwitchIconWithTextViewHolder.switch_icon_with_text_id);
                    }
                    switchIconWithTextViewHolder.getName().setText(switchIconWithText.getNameResId());
                    if (
                            switchIconWithText.isDividerVisible()
                            ) {
                        switchIconWithTextViewHolder.setDividerVisible();
                    } else {
                        switchIconWithTextViewHolder.setDividerInVisible();
                    }
                    switchIconWithTextViewHolder.getSwitchButton().setChecked(switchIconWithText.isChecked);
                    //switchIconWithTextViewHolder.getSwitchButton().setOnClickListener();
                    ((BaseScreen) context).addConfigurationListener(switchIconWithTextViewHolder.getSwitchButton());
                    switchIconWithTextViewHolder.setSettingsLiListener(new SettingsListener() {
                        @Override
                        public void onCheckedChanged(boolean isChecked) {
                            switchIconWithText.setChecked(isChecked);
                        }
                    });
                    break;
                default:
                    mLogger.error("error on get view");
                    break;
            }
        return convertView;
    }
}