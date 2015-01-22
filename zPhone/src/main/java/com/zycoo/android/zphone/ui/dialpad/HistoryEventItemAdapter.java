
package com.zycoo.android.zphone.ui.dialpad;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.zycoo.android.zphone.Engine;
import com.zycoo.android.zphone.R;
import com.zycoo.android.zphone.ZphoneApplication;
import com.zycoo.android.zphone.utils.AndroidUtils;

import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.model.NgnContact;
import org.doubango.ngn.model.NgnHistoryEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class HistoryEventItemAdapter
        extends ArrayAdapter<ContactItemInterface> implements Filterable {
    private List<ContactItemInterface> historyItems;
    private Context context;
    private boolean mInSearchMode = false;
    private ContactsSectionIndexer indexer = null;


    public HistoryEventItemAdapter(List<ContactItemInterface> contactsList, Context context,
                                   int resId) {
        super(context, resId);
        this.context = context;
        Collections.sort(contactsList, new ContactItemComparator());
        //indexer = new ContactsSectionIndexer(contactsList);
        historyItems = contactsList;
    }

    @Override
    public int getCount() {
        return historyItems.size();
    }

    @Override
    public ContactItemInterface getItem(int position) {
        return historyItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                historyItems = (ArrayList<ContactItemInterface>) results.values;
                notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                FilterResults results = new FilterResults();
                ArrayList<ContactItemInterface> FilteredArrayNames = new ArrayList<ContactItemInterface>();
                // perform your search here using the searchConstraint String.
                constraint = constraint.toString().toLowerCase();
                if ("".equals(constraint.toString())) {
                    for (NgnHistoryEvent event : Engine.getInstance().getHistoryService()
                            .getEvents()) {
                        FilteredArrayNames.add(new HistoryEventItem(event));
                    }
                } else {
                    List<NgnContact> list = Engine.getInstance().getContactService()
                            .getObservableContacts().getList();
                    for (NgnContact item : list) {
                        if (item.getPrimaryNumber()
                                .toLowerCase(Locale.getDefault())
                                .contains(constraint.toString().replace(" ", "").replace("-", ""))) {
                            FilteredArrayNames.add(new ContactItem(item));
                        }
                    }
                }
                results.count = FilteredArrayNames.size();
                results.values = FilteredArrayNames;
                return results;

            }
        };

        return filter;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ContactViewHolder contactViewHolder = null;
        CallLogHolderView callLogHolderView = null;
        if (historyItems.get(position) instanceof ContactItem) {
            final NgnContact contact = ((ContactItem) historyItems.get(position)).getContact();
            if (null == convertView
                    || null == convertView.getTag(R.layout.fragment_dial_contacts_item)) {
                convertView =
                        LayoutInflater.from(context)
                                .inflate(R.layout.fragment_dial_contacts_item, null);
                contactViewHolder = new ContactViewHolder(convertView);
                convertView.setTag(R.layout.fragment_dial_contacts_item, contactViewHolder);
            } else {
                contactViewHolder =
                        (ContactViewHolder) convertView
                                .getTag(R.layout.fragment_dial_contacts_item);
            }
            contactViewHolder.nameTextView.setText(contact.getDisplayName());
            contactViewHolder.numberTextView.setText(contact.getPrimaryNumber());
            new AsyncTask<ContactViewHolder, Void, Void>() {
                Bitmap bitmap;
                ContactViewHolder con;

                @Override
                protected void onPostExecute(Void aVoid) {
                    if (null != bitmap) {
                        con.photoBitmapView.setImageBitmap(bitmap);
                    } else {
                        con.photoBitmapView.setImageResource(R.drawable.ic_contact_picture_holo_light);
                    }
                    super.onPostExecute(aVoid);
                }

                @Override
                protected Void doInBackground(ContactViewHolder... params) {
                    con = params[0];
                    bitmap = contact.getPhoto();
                    return null;
                }
            }.execute(contactViewHolder);
            if (position % 2 == 0) {
                contactViewHolder.mItemll.setBackgroundColor(ZphoneApplication.color_grey_100);
            } else {
                contactViewHolder.mItemll.setBackgroundColor(ZphoneApplication.color_grey_200);
            }
        } else if (historyItems.get(position) instanceof HistoryEventItem) {
            NgnHistoryEvent event = ((HistoryEventItem) historyItems.get(position)).getEvent();
            if (null == convertView
                    || null == convertView.getTag(R.layout.fragment_dialer_call_log_list_item)) {
                convertView =
                        LayoutInflater.from(context).inflate(
                                R.layout.fragment_dialer_call_log_list_item, null);

                callLogHolderView = new CallLogHolderView(convertView);

                convertView.setTag(R.layout.fragment_dialer_call_log_list_item, callLogHolderView);
            } else {
                callLogHolderView =
                        (CallLogHolderView) convertView
                                .getTag(R.layout.fragment_dialer_call_log_list_item);
            }
            callLogHolderView.nameTextView.setText(event.getDisplayName().replace("%23", "#"));
            callLogHolderView.numberTextView.setText(event.getRemoteParty().replace("%23", "#"));
            final String number = event.getRemoteParty();
            callLogHolderView.callImageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            if (Engine.getInstance().getSipService().isRegistered()) {
                                ScreenAV.makeCall(number,
                                        NgnMediaType.Audio,
                                        ZphoneApplication.getContext());
                            }
                        }
                    }).start();
                }
            });

            switch (event.getStatus()) {
                case Outgoing:
                    callLogHolderView.callTypeIconsView
                            .setImageResource(R.drawable.call_outgoing_45);
                    break;
                case Incoming:
                    callLogHolderView.callTypeIconsView
                            .setImageResource(R.drawable.call_incoming_45);
                    break;
                case Failed:
                case Missed:
                    callLogHolderView.callTypeIconsView.setImageResource(R.drawable.call_missed_45);
                    break;
            }
            callLogHolderView.callTimeTextView.setReferenceTime(event.getStartTime());
            callLogHolderView.callDurationTextView.setText(AndroidUtils.formatIntToTimeStr((event
                    .getEndTime()
                    - event.getStartTime()) / 1000));

            if (position % 2 == 0) {
                callLogHolderView.mItemLv.setBackgroundColor(ZphoneApplication.color_grey_100);
            } else {
                callLogHolderView.mItemLv.setBackgroundColor(ZphoneApplication.color_grey_200);
            }
            //TODO 需要根据呼叫号码查询数据库获取头像
        }
        return convertView;
    }

    public boolean isInSearchMode() {
        return mInSearchMode;
    }

    public void setInsearchMode(boolean isSearchMode) {
        mInSearchMode = isSearchMode;
    }

    public void setIndexer(ContactsSectionIndexer indexer) {
        this.indexer = indexer;
    }

    public ContactsSectionIndexer getIndexer() {
        return this.indexer;
    }

    class ContactViewHolder {
        ImageView photoBitmapView;
        TextView nameTextView;
        TextView numberTextView;
        LinearLayout mItemll;

        public ContactViewHolder(View v) {
            v.setTag(this);
            nameTextView = (TextView) v.findViewById(R.id.contact_name_tv);
            numberTextView = (TextView) v
                    .findViewById(R.id.contact_number_tv);
            photoBitmapView = (ImageView) v
                    .findViewById(R.id.conference_item_iv);
            mItemll = (LinearLayout) v.findViewById(R.id.dial_contact_item_ll);
        }
    }

    class CallLogHolderView {
        ImageView photoImageView;
        ImageButton callImageView;
        TextView nameTextView;
        TextView numberTextView;
        TextView dateTextView;
        //CallTypeIconsView callTypeIconsView;
        LinearLayout mItemLv;
        ImageView callTypeIconsView;
        RelativeTimeTextView callTimeTextView;
        TextView callDurationTextView;

        public CallLogHolderView(View v) {
            v.setTag(this);
            callImageView = (ImageButton) v
                    .findViewById(R.id.secondary_action_icon);
            photoImageView = (ImageView) v
                    .findViewById(R.id.quick_contact_photo);
            nameTextView = (TextView) v.findViewById(R.id.name);
            numberTextView = (TextView) v.findViewById(R.id.number);
            dateTextView = (TextView) v.findViewById(R.id.call_count_and_date);
            callTypeIconsView = (ImageView) v
                    .findViewById(R.id.call_type_icons);
            callTimeTextView = (RelativeTimeTextView) v
                    .findViewById(R.id.call_history_time_tv);
            callDurationTextView = (TextView) v
                    .findViewById(R.id.call_history_duration_tv);
            mItemLv = (LinearLayout) v.findViewById(R.id.call_histroy_itme_lv);
        }
    }
}
