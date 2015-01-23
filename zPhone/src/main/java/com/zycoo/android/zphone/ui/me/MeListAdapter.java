package com.zycoo.android.zphone.ui.me;

import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.kyleduo.switchbutton.SwitchButton;
import com.zycoo.android.zphone.Engine;
import com.zycoo.android.zphone.R;
import com.zycoo.android.zphone.RegisterTask;
import com.zycoo.android.zphone.ZphoneApplication;
import com.zycoo.android.zphone.utils.Utils;

import org.doubango.ngn.events.NgnRegistrationEventTypes;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.services.INgnSipService;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.os.Handler;


/**
 * Created by tqcenglish on 14-12-17.
 */
public class MeListAdapter extends BaseAdapter {
    private Logger logger = LoggerFactory.getLogger(MeListAdapter.class);
    private INgnConfigurationService configurationService = Engine.getInstance().getConfigurationService();
    private INgnSipService sipService = Engine.getInstance().getSipService();
    private Context context;
    private ListViewItem[] objects;
    MeListViewItemAccountViewHolder accountViewHolder = null;

    public interface OnStatusChangeListener {
        public void statusChange(NgnRegistrationEventTypes type);
    }

    @Override
    public int getViewTypeCount() {
        return ListViewItem.getTypeCount();
    }

    @Override
    public int getItemViewType(int position) {
        return objects[position].getType();
    }

    public MeListAdapter(Context context, ListViewItem[] objects) {
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
    public View getView(int position, View convertView, ViewGroup parent) {

        ListViewItemAvatarWithTextViewHolder itemViewHolder = null;

        ListViewItem listViewItem = objects[position];
        int listViewItemType = getItemViewType(position);
        switch (listViewItemType) {
            case ListViewItem.TYPE_ACCOUNT:
                if (convertView == null || null == convertView.getTag(R.layout.list_item_me_account_info)) {
                    convertView = LayoutInflater.from(context).inflate(R.layout.list_item_me_account_info, null);
                    accountViewHolder = new MeListViewItemAccountViewHolder(convertView);
                } else {
                    accountViewHolder = (MeListViewItemAccountViewHolder) convertView.getTag(R.layout.list_item_me_account_info);
                }

                String name = configurationService.getString(NgnConfigurationEntry.IDENTITY_DISPLAY_NAME, "");
                String account = configurationService.getString(NgnConfigurationEntry.IDENTITY_IMPI, "");
                if (0 != name.length()) {
                    accountViewHolder.getName().setText(name);
                }
                if (0 != account.length()) {
                    accountViewHolder.getExtension().setText(account);
                }

                if (sipService.isRegistered()) {
                    //accountViewHolder.getAvatar().setImageResource(R.drawable.ic_status_dot_green);

                    accountViewHolder.getExtension().setTextColor(context.getResources().getColor(
                            R.color.light_blue));
                    accountViewHolder.getStatus_tv().setText(R.string.registration_ok);
                    accountViewHolder.getStatus_tv().setTextColor(context.getResources().getColor(
                            R.color.light_blue));
                    accountViewHolder.getStatus_bt().setChecked(true);
                    accountViewHolder.getAvatar().setColorFilter(null);
                } else {
                    accountViewHolder.getExtension().setTextColor(context.getResources().getColor(
                            R.color.red_700));
                    accountViewHolder.getStatus_tv().setText(R.string.registration_nok);
                    accountViewHolder.getStatus_tv().setTextColor(context.getResources().getColor(
                            R.color.red_700));
                    accountViewHolder.getStatus_bt().setChecked(false);
                    setBlackAndWhite(accountViewHolder.getAvatar());
                }
                accountViewHolder.getStatus_bt().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (
                                !((SwitchButton) v).isChecked()) {
                            new RegisterTask(true, (SwitchButton) v).execute();
                        } else {
                            new RegisterTask(false, (SwitchButton) v).execute();
                        }
                    }
                });

                ((MeListViewItemAccount) listViewItem).setStatusChangeListener(new OnStatusChangeListener() {
                    @Override
                    public void statusChange(NgnRegistrationEventTypes type) {
                        accountViewHolder.getExtension().setText(ZphoneApplication.getUserName());
                        accountViewHolder.getExtension().setText(ZphoneApplication.getUserName());
                        switch (type) {
                            case REGISTRATION_OK:

                                accountViewHolder.getExtension().setTextColor(context.getResources().getColor(
                                        R.color.light_blue));
                                accountViewHolder.getStatus_tv().setText(R.string.registration_ok);
                                accountViewHolder.getStatus_tv().setTextColor(context.getResources().getColor(
                                        R.color.light_blue));
                                accountViewHolder.getStatus_bt().setChecked(true);
                                accountViewHolder.getAvatar().setColorFilter(null);
                                break;
                            case REGISTRATION_NOK:

                                accountViewHolder.getExtension().setTextColor(context.getResources().getColor(
                                        R.color.red_700));
                                accountViewHolder.getStatus_tv().setText(R.string.registration_nok);
                                accountViewHolder.getStatus_tv().setTextColor(context.getResources().getColor(
                                        R.color.red_700));
                                accountViewHolder.getStatus_bt().setChecked(false);
                                setBlackAndWhite(accountViewHolder.getAvatar());
                                break;
                            case REGISTRATION_INPROGRESS:
                                accountViewHolder.getStatus_tv().setText(R.string.registration_inprogress);
                                accountViewHolder.getStatus_tv().setTextColor(context.getResources().getColor(
                                        R.color.red_700));
                                break;
                            case UNREGISTRATION_INPROGRESS:
                                accountViewHolder.getStatus_tv().setText(R.string.unregistration_inprogress);
                                accountViewHolder.getStatus_tv().setTextColor(context.getResources().getColor(
                                        R.color.red_700));
                                break;
                            case UNREGISTRATION_OK:
                                accountViewHolder.getExtension().setTextColor(context.getResources().getColor(
                                        R.color.red_700));
                                accountViewHolder.getStatus_tv().setText(R.string.unregistration_ok);
                                accountViewHolder.getStatus_tv().setTextColor(context.getResources().getColor(
                                        R.color.red_700));
                                accountViewHolder.getStatus_bt().setChecked(false);
                                setBlackAndWhite(accountViewHolder.getAvatar());

                                break;
                            case UNREGISTRATION_NOK:
                                accountViewHolder.getStatus_tv().setText(R.string.unregistration_nok);
                                accountViewHolder.getStatus_tv().setTextColor(context.getResources().getColor(
                                        R.color.red_700));
                                break;
                            default:
                                break;
                        }
                    }
                });

                break;
            case ListViewItem.TYPE_AVATAR_WITH_TEXT:
                if (convertView == null || null == convertView.getTag(ListViewItemAvatarWithTextViewHolder.avatar_with_text_id)) {
                    convertView = LayoutInflater.from(context).inflate(ListViewItemAvatarWithTextViewHolder.avatar_with_text_id, null);
                    itemViewHolder = new ListViewItemAvatarWithTextViewHolder(convertView);
                } else {
                    itemViewHolder = (ListViewItemAvatarWithTextViewHolder) convertView.getTag(ListViewItemAvatarWithTextViewHolder.avatar_with_text_id);
                }
                ListViewItemAvatarWithText optionItem = ((ListViewItemAvatarWithText) objects[position]);
                itemViewHolder.getAvatar().setImageResource(optionItem.getAvatarResId());
                itemViewHolder.getName().setText(optionItem.getNameResId());
                if (
                        optionItem.isDividerVisible()
                        ) {
                    itemViewHolder.setDividerVisible();
                } else {
                    itemViewHolder.setDividerInVisible();
                }
                break;
            case ListViewItem.TYPE_WHITE:
                convertView = LayoutInflater.from(context).inflate(R.layout.list_item_white_divider, null);
                TextView whiteTv = (TextView) convertView.findViewById(R.id.id_item_white_divider_tv);
                int height_dp = ((ListViewItemWhite) objects[position]).getItemHeight();
                whiteTv.setHeight((int) Utils.convertDpToPixel(height_dp, context));
                break;
            case ListViewItem.TYPE_EXIT:
                convertView = LayoutInflater.from(context).inflate(R.layout.list_item_me_exit, null);
                break;
        }
        return convertView;
    }

    private void setBlackAndWhite(ImageView iv) {
        float brightness = (float) (180 - 255);
        float[] colorMatrix = {
                0.33f, 0.33f, 0.33f, 0, brightness, //red
                0.33f, 0.33f, 0.33f, 0, brightness, //green
                0.33f, 0.33f, 0.33f, 0, brightness, //blue
                0, 0, 0, 1, 0    //alpha
        };
        ColorFilter colorFilter = new ColorMatrixColorFilter(colorMatrix);
        iv.setColorFilter(colorFilter);
    }
}
