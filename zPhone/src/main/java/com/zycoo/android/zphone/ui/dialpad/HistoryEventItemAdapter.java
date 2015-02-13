
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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.zycoo.android.zphone.Engine;
import com.zycoo.android.zphone.R;
import com.zycoo.android.zphone.ZphoneApplication;
import com.zycoo.android.zphone.utils.AndroidUtils;
import com.zycoo.android.zphone.utils.Utils;

import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.model.NgnContact;
import org.doubango.ngn.model.NgnHistoryEvent;

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
        //Collections.sort(contactsList, new ContactItemComparator());
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
        return new Filter() {
            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                //FilterResults results = new FilterResults();
                historyItems.clear();
                // perform your search here using the searchConstraint String.
                constraint = constraint.toString().toLowerCase();
                if ("".equals(constraint.toString())) {
                    for (NgnHistoryEvent event : Engine.getInstance().getHistoryService()
                            .getEvents()) {
                        historyItems.add(new HistoryEventItem(event));
                    }
                } else {
                    List<NgnContact> list = Engine.getInstance().getContactService()
                            .getObservableContacts().getList();
                    for (NgnContact item : list) {
                        if (item.getPrimaryNumber()
                                .toLowerCase(Locale.getDefault())
                                .contains(constraint.toString().replace(" ", "").replace("-", ""))) {
                            historyItems.add(new ContactItem(item));
                        }
                    }
                }
                //results.count = historyItems.size();
                //results.values = historyItems;
                //return results;
                return null;
            }
        };
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ContactViewHolder contactViewHolder = null;
        CallLogHolderView callLogHolderView = null;
        if (historyItems.get(position) instanceof ContactItem) {
            final NgnContact contact = ((ContactItem) historyItems.get(position)).getContact();
            if (null == convertView
                    || null == convertView.getTag(R.layout.fragment_dial_contacts_item)) {
                convertView =
                        LayoutInflater.from(context)
                                .inflate(R.layout.fragment_dial_contacts_item, parent, false);
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
                        TextDrawable drawable = ZphoneApplication.getBuilderRect().build(contact.getDisplayName().substring(0, 1), Utils.getColorResourceId(context, position));
                        con.photoBitmapView.setImageDrawable(drawable);
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
        } else if (historyItems.get(position) instanceof HistoryEventItem) {
            NgnHistoryEvent event = ((HistoryEventItem) historyItems.get(position)).getEvent();
            if (null == convertView
                    || null == convertView.getTag(R.layout.fragment_dialer_call_log_list_item)) {
                convertView =
                        LayoutInflater.from(context).inflate(
                                R.layout.fragment_dialer_call_log_list_item, parent, false);

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
                    callLogHolderView.callTypeIconsView.setImageResource(R.drawable.ic_call_made_white);
                    Utils.setImageViewFilter(callLogHolderView.callTypeIconsView, R.color.light_blue);
                    break;
                case Incoming:
                    callLogHolderView.callTypeIconsView.setImageResource(R.drawable.ic_call_received_white);
                    Utils.setImageViewFilter(callLogHolderView.callTypeIconsView, R.color.blue_700);
                    break;
                case Failed:
                case Missed:
                    callLogHolderView.callTypeIconsView.setImageResource(R.drawable.ic_call_missed_white);
                    Utils.setImageViewFilter(callLogHolderView.callTypeIconsView, R.color.red_500);
                    break;
            }
            callLogHolderView.callTimeTextView.setReferenceTime(event.getStartTime());
            callLogHolderView.callDurationTextView.setText(AndroidUtils.formatIntToTimeStr((event
                    .getEndTime()
                    - event.getStartTime()) / 1000));
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
        RelativeLayout mItemRv;

        public ContactViewHolder(View v) {
            v.setTag(this);
            nameTextView = (TextView) v.findViewById(R.id.contact_name_tv);
            numberTextView = (TextView) v
                    .findViewById(R.id.contact_number_tv);
            photoBitmapView = (ImageView) v
                    .findViewById(R.id.conference_item_iv);
            mItemRv = (RelativeLayout) v.findViewById(R.id.dial_contact_item_rv);
        }
    }

    class CallLogHolderView {
        ImageView callImageView;
        TextView nameTextView;
        TextView numberTextView;
        RelativeLayout mItemRv;
        ImageView callTypeIconsView;
        RelativeTimeTextView callTimeTextView;
        TextView callDurationTextView;

        public CallLogHolderView(View v) {
            v.setTag(this);
            callImageView = (ImageView) v
                    .findViewById(android.R.id.icon2);

            nameTextView = (TextView) v.findViewById(android.R.id.text1);
            numberTextView = (TextView) v.findViewById(android.R.id.text2);
            callTypeIconsView = (ImageView) v
                    .findViewById(android.R.id.icon1);
            callTimeTextView = (RelativeTimeTextView) v
                    .findViewById(R.id.call_history_time_tv);
            callDurationTextView = (TextView) v
                    .findViewById(R.id.call_history_duration_tv);
            mItemRv = (RelativeLayout) v.findViewById(R.id.call_history_item_rv);
        }
    }
}
