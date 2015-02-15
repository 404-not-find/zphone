package com.zycoo.android.zphone.ui.message;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.zycoo.android.zphone.DatabaseHelper;
import com.zycoo.android.zphone.R;
import com.zycoo.android.zphone.ZphoneApplication;
import com.zycoo.android.zphone.utils.AndroidUtils;
import com.zycoo.android.zphone.utils.Utils;

import org.doubango.ngn.utils.NgnUriUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends BaseExpandableListAdapter {

    public static final String INBOX = "INBOX";
    public static final String OLD = "Old";
    private final Logger mLogger = LoggerFactory.getLogger(MessageAdapter.class);
    private int count_new = 0;
    private final Context mContext;
    private final LayoutInflater mLayoutInflater;
    private final String[] mGroups;
    private final int[] mGroupDrawables = {
            R.drawable.ic_email_white,
            R.drawable.ic_voicemail_white
    };
    private List<List<?>> mAllChilds;
    private List<VoiceMailBean> mVoiceMails;
    private List<MonitorBean> mMonitors;

    public MessageAdapter(Context context) {
        mContext = context;
        mLayoutInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mGroups = new String[]{
                ZphoneApplication.getAppResources().getString(R.string.voice_mail),
                ZphoneApplication.getAppResources().getString(R.string.record),
        };
        mAllChilds = new ArrayList<>();
        mVoiceMails = new ArrayList<VoiceMailBean>();
        mMonitors = new ArrayList<MonitorBean>();
        mAllChilds.add(mVoiceMails);
        mAllChilds.add(mMonitors);
    }

    public void getMonitorsFromDB() {
        SQLiteDatabase rSQLiteDatabase = new DatabaseHelper(mContext.getApplicationContext(),
                "SoftPhone.db", null, 1)
                .getReadableDatabase();
        Cursor cursor = rSQLiteDatabase
                .rawQuery(
                        "select DURATION, TIME, FILE_FORMATE, FROM_EXTENSION, TO_EXTENSION, TYPE, FILE_NAME from MONITORS  order by TIME desc",
                        null);
        mMonitors.clear();
        if (cursor.moveToFirst()) {
            do {
                MonitorBean monitorBean = new MonitorBean();
                monitorBean.setDuration(cursor.getInt(0)); //DURATION
                monitorBean.setTime(cursor.getString(1)); //TIME
                monitorBean.setFile_formate(cursor.getString(2)); // FILE_FORMATE
                monitorBean.setFrom(cursor.getString(3)); // FROM
                monitorBean.setTo(cursor.getString(4)); // TO
                monitorBean.setType(cursor.getString(5)); //TYPE
                monitorBean.setFile_name(cursor.getString(6)); // FILE_NAME
                mMonitors.add(monitorBean);
            } while (cursor.moveToNext());
        }
        cursor.close();
        rSQLiteDatabase.close();
    }

    public void getVoiceMailsFromDB() {
        SQLiteDatabase rSQLiteDatabase = new DatabaseHelper(mContext.getApplicationContext(),
                "SoftPhone.db", null, 1)
                .getReadableDatabase();
        Cursor cursor = rSQLiteDatabase
                .rawQuery(
                        "select callerid, duration, origtime, type from INFOS  order by origtime desc",
                        null);
        mVoiceMails.clear();
        count_new = 0;
        if (cursor.moveToFirst()) {
            do {
                VoiceMailBean voiceMailBean = new VoiceMailBean();
                voiceMailBean.mCallerID = cursor.getString(0);
                voiceMailBean.mDuration = cursor.getString(1);
                voiceMailBean.mDate = cursor.getString(2);
                voiceMailBean.mType = cursor.getString(3);
                //从callid中获取分机号和用户名
                List<String> list = Utils.splitCallid(voiceMailBean.mCallerID);
                if (null != list && list.size() == 2) {
                    voiceMailBean.mName = list.get(0);
                    voiceMailBean.mExtension = list.get(1);
                } else {
                    voiceMailBean.mName = voiceMailBean.mCallerID;
                    voiceMailBean.mExtension = voiceMailBean.mCallerID;
                }
                if (voiceMailBean.getType().equals(INBOX)) {
                    count_new++;
                }
                mVoiceMails.add(voiceMailBean);
            } while (cursor.moveToNext());
        }
        cursor.close();
        rSQLiteDatabase.close();
        mLogger.debug("count_new " + count_new);
    }

    @Override
    public int getGroupCount() {
        return mGroups.length;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mGroups[groupPosition];
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                             ViewGroup parent) {
        GroupViewHolder groupViewHolder;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.fragment_message_list_group_item,
                    parent,
                    false);
            groupViewHolder = new GroupViewHolder(convertView);
        } else {
            groupViewHolder = (GroupViewHolder) convertView.getTag();
        }
        groupViewHolder.photoIv.setImageResource(mGroupDrawables[groupPosition]);
        groupViewHolder.nameTv.setText(mGroups[groupPosition]);
        final int resId = isExpanded ? R.drawable.ic_keyboard_arrow_down_white : R.drawable.ic_keyboard_arrow_right_white;
        groupViewHolder.expandedIv.setImageResource(resId);
        switch (groupPosition) {
            case 0:
                if (count_new > 0) {
                    groupViewHolder.badge_tv.setText(Integer.toString(count_new));
                } else {
                    groupViewHolder.badge_tv.setVisibility(View.INVISIBLE);
                }
                Utils.setImageViewFilter(groupViewHolder.photoIv,R.color.cyan_500);
                break;
            case 1:
                groupViewHolder.badge_tv.setVisibility(View.INVISIBLE);
                Utils.setImageViewFilter(groupViewHolder.photoIv,R.color.teal_500);
                break;

            default:
                break;
        }

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mAllChilds.get(groupPosition).size();
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mAllChilds.get(groupPosition).get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                             View convertView, ViewGroup parent) {
        VoiceMailViewHolder voicemailItemViewHolder;
        MonitorViewHolder monitorViewHolder;
        switch (groupPosition) {
            case 0:
                if (convertView == null || null == convertView
                        .getTag(R.layout.fragment_message_list_child_voicemail_item)) {
                    convertView = mLayoutInflater.inflate(
                            R.layout.fragment_message_list_child_voicemail_item,
                            parent,
                            false);
                    voicemailItemViewHolder = new VoiceMailViewHolder(convertView);
                } else {
                    voicemailItemViewHolder = (VoiceMailViewHolder) convertView
                            .getTag(R.layout.fragment_message_list_child_voicemail_item);
                }
                if (mVoiceMails.size() > childPosition) {
                    VoiceMailBean selectVoiceMailBean = mVoiceMails.get(childPosition);
                    voicemailItemViewHolder.dateTv
                            .setReferenceTime(Long.parseLong(selectVoiceMailBean.mDate) * 1000);
                    voicemailItemViewHolder.durationTv.setText(AndroidUtils
                            .formatIntToTimeStr(selectVoiceMailBean.mDuration));
                    String sipUri = String.format("sip:%s@%s", selectVoiceMailBean.mExtension,
                            ZphoneApplication.getHost());
                    //TODO NULLPoint
                    String newName = NgnUriUtils.getDisplayName(sipUri);
                    if (null != newName) {
                        voicemailItemViewHolder.nameTv.setText(newName);
                    } else {
                        voicemailItemViewHolder.nameTv.setText(selectVoiceMailBean.mName);
                    }
                    if (selectVoiceMailBean.mType.equals(INBOX)) {
                        voicemailItemViewHolder.nameTv.setTextColor(mContext.getResources()
                                .getColor(
                                        com.zycoo.android.zphone.R.color.red_500));
                    } else {
                        voicemailItemViewHolder.nameTv.setTextColor(mContext.getResources()
                                .getColor(
                                        com.zycoo.android.zphone.R.color.dark_grey));
                    }
                    int colorId = Utils.getColorResourceId(mContext, childPosition);
                    TextDrawable textDrawable = ZphoneApplication.getBuilderRect().build(newName.substring(0, 1), colorId);
                    voicemailItemViewHolder.photoIv.setImageDrawable(textDrawable);
                }
                break;
            case 1:
                if (convertView == null || null == (MonitorViewHolder) convertView
                        .getTag(R.layout.fragment_message_list_child_records_item)) {
                    convertView = mLayoutInflater.inflate(
                            R.layout.fragment_message_list_child_records_item,
                            parent,
                            false);
                    monitorViewHolder = new MonitorViewHolder(convertView);
                } else {
                    monitorViewHolder = (MonitorViewHolder) convertView
                            .getTag(R.layout.fragment_message_list_child_records_item);
                }
                if (mMonitors.size() > childPosition) {
                    MonitorBean selectRecordBean = mMonitors.get(childPosition);
                    monitorViewHolder.mDateTv.setReferenceTime(Long
                            .parseLong(selectRecordBean.getTime()) * 1000);
                    monitorViewHolder.mDurationTv.setText(AndroidUtils
                            .formatIntToTimeStr(selectRecordBean
                                    .getDuration()));
                    monitorViewHolder.mTypeTv.setText(selectRecordBean.getType());
                    monitorViewHolder.mNameTv.setText(selectRecordBean.getFrom() + "--"
                            + selectRecordBean.getTo());
                    int colorId = Utils.getColorResourceId(mContext, childPosition);
                    TextDrawable textDrawable = ZphoneApplication.getBuilderCircular().build(selectRecordBean.getFrom().substring(0, 1), colorId);
                    monitorViewHolder.photoIv.setImageDrawable(textDrawable);
                }
                break;
            default:
                break;
        }
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    class GroupViewHolder {
        ImageView photoIv;
        ImageView expandedIv;
        TextView nameTv;
        TextView badge_tv;

        public GroupViewHolder(View v) {
            v.setTag(this);
            photoIv = (ImageView) v
                    .findViewById(R.id.sample_activity_list_group_item_image);

            nameTv = (TextView) v
                    .findViewById(R.id.sample_activity_list_group_item_text);
            expandedIv = (ImageView) v
                    .findViewById(R.id.sample_activity_list_group_expanded_image);
            badge_tv = (TextView) v.findViewById(R.id.menu_badge_text);

        }
    }

    class MonitorViewHolder {
        RelativeLayout mMonitorItemRv;
        ImageView photoIv;
        TextView mNameTv;
        TextView mTypeTv;
        RelativeTimeTextView mDateTv;
        TextView mDurationTv;

        public MonitorViewHolder(View v) {
            v.setTag(R.layout.fragment_message_list_child_records_item, this);
            mNameTv = (TextView) v.findViewById(R.id.monitor_name);
            mTypeTv = (TextView) v.findViewById(R.id.monitor_type);
            mDurationTv = (TextView) v.findViewById(R.id.monitor_duration);
            mDateTv = (RelativeTimeTextView) v.findViewById(R.id.monitor_date);
            mMonitorItemRv = (RelativeLayout) v.findViewById(R.id.monitor_item_rv);
            photoIv = (ImageView) v.findViewById(android.R.id.icon);
        }
    }

    class VoiceMailViewHolder {
        RelativeLayout mVoicemailItemRv;
        ImageView photoIv;
        TextView nameTv;
        RelativeTimeTextView dateTv;
        TextView durationTv;

        public VoiceMailViewHolder(View v) {
            v.setTag(R.layout.fragment_message_list_child_voicemail_item, this);
            dateTv = (RelativeTimeTextView) v.findViewById(R.id.time_date);
            durationTv = (TextView) v.findViewById(R.id.time_duration);
            nameTv = (TextView) v.findViewById(R.id.item_name);
            mVoicemailItemRv = (RelativeLayout) v.findViewById(R.id.voicemail_item_rv);
            photoIv = (ImageView) v.findViewById(android.R.id.icon);
        }
    }

    public class VoiceMailBean {
        String mType;
        String mName;
        String mDate;
        String mDuration;
        String mExtension;
        String mCallerID;

        public String getType() {
            return mType;
        }

        public String getName() {
            return mName;
        }

        public String getCallerID() {
            return mCallerID;
        }

        public String getDate() {
            return mDate;
        }

        public String getExtension() {
            return mExtension;
        }

    }

    public List<VoiceMailBean> getVoiceMails() {
        return mVoiceMails;
    }

    public List<MonitorBean> getMonitors() {
        return mMonitors;
    }
}
