package com.zycoo.android.zphone.ui.contacts;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.zycoo.android.zphone.Engine;
import com.zycoo.android.zphone.R;
import com.zycoo.android.zphone.ZphoneApplication;
import com.zycoo.android.zphone.ui.dialpad.ScreenAV;
import com.zycoo.android.zphone.utils.AndroidUtils;
import com.zycoo.android.zphone.utils.Utils;
import com.zycoo.android.zphone.widget.SuperAwesomeCardFragment;

import org.doubango.ngn.media.NgnMediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by tqcenglish on 15-1-29.
 */
public class ContactHistoryFragment extends SuperAwesomeCardFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public final Logger logger;
    private String mContactDisplayName;
    // Defines a tag for identifying log entries
    private static final String TAG = ContactHistoryFragment.class.getCanonicalName();
    private LinearLayout mCallLogLayout;
    private TextView mEmptyView;

    public static ContactHistoryFragment newInstance(int position) {
        ContactHistoryFragment f = new ContactHistoryFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);
        return f;
    }

    public ContactHistoryFragment() {
        logger = LoggerFactory.getLogger(ContactDetailFragment.class.getCanonicalName());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View callDetailHistoryView =
                inflater.inflate(R.layout.fragment_contact_detail_history, container, false);
        // Gets handles to view objects in the layout
        mCallLogLayout = (LinearLayout) callDetailHistoryView.findViewById(R.id.contact_call_log_layout);
        mEmptyView = (TextView) callDetailHistoryView.findViewById(android.R.id.empty);
        return callDetailHistoryView;
    }

    public RelativeLayout buildCallLogLayout(int callType, final String callNumber,
                                             final int callDuration, final long callDate) {
        // Inflates the address layout
        final RelativeLayout callLogLayout =
                (RelativeLayout) LayoutInflater.from(getActivity()).inflate(
                        R.layout.fragment_dialer_call_log_list_item, mCallLogLayout, false);
        //get views
        final ImageView callTypeIv = (ImageView) callLogLayout
                .findViewById(android.R.id.icon1);
        final TextView callNumberTv = (TextView) callLogLayout
                .findViewById(android.R.id.text1);
        final TextView callDurationTv = (TextView) callLogLayout
                .findViewById(R.id.call_history_duration_tv);
        final RelativeTimeTextView callDateTv = (RelativeTimeTextView) callLogLayout
                .findViewById(R.id.call_history_time_tv);
        // If there's no addresses for the contact, shows the empty view and message, and hides the
        // header and button.
        switch (callType) {
            case CallLog.Calls.INCOMING_TYPE:
                callTypeIv.setImageResource(R.drawable.ic_call_received_white);
                Utils.setImageViewFilter(callTypeIv, R.color.light_blue);
                break;
            case CallLog.Calls.OUTGOING_TYPE:
                callTypeIv.setImageResource(R.drawable.ic_call_made_white);
                Utils.setImageViewFilter(callTypeIv, R.color.blue_700);
                break;

            case CallLog.Calls.MISSED_TYPE:
                callTypeIv.setImageResource(R.drawable.ic_call_missed_white);
                Utils.setImageViewFilter(callTypeIv, R.color.red_700);
                break;
            default:
                break;
        }
        callNumberTv.setText(callNumber);
        if (callDuration > 0) {
            callDurationTv.setText(AndroidUtils.formatIntToTimeStr(callDuration));
        } else {
            callDurationTv.setVisibility(View.GONE);
        }
        callDateTv.setReferenceTime(callDate);
        final ImageView callIv = (ImageView) callLogLayout.findViewById(android.R.id.icon2);
        callIv.setImageResource(R.drawable.ic_call_white);
        Utils.setImageViewFilter(callIv, R.color.blue_700);
        callIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                // voice call
                                if (Engine.getInstance().getSipService().isRegistered()) {
                                    ScreenAV.makeCall(callNumber,
                                            NgnMediaType.Audio,
                                            ZphoneApplication.getContext());
                                }
                            }

                        }

                ).start();
            }
        });
        return callLogLayout;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case ContactCallLogQuery.QUERY_ID:
                String str = args.getString("contactName");
                return new CursorLoader(getActivity(), Uri.parse("content://call_log/calls"),
                        ContactCallLogQuery.PROJECTION,
                        CallLog.Calls.CACHED_NAME + "='" + str + "'", null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        // If this fragment was cleared while the query was running
        // eg. from from a call like setContact(uri) then don't do
        // anything.
        if (mContactDisplayName == null) {
            return;
        }

        switch (loader.getId()) {
            case ContactCallLogQuery.QUERY_ID:
                // Each LinearLayout has the same LayoutParams so this can
                // be created once and used for each address.
                final LinearLayout.LayoutParams callLogLayoutParams =
                        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);
                //TODO 移动到HISTORY
                mCallLogLayout.removeAllViews();
                if (data.moveToFirst()) {
                    int i = 0;
                    do {
                        i++;
                        //Builds the address layout
                        final RelativeLayout layout = buildCallLogLayout(
                                data.getInt(data.getColumnIndex(CallLog.Calls.TYPE)),
                                data.getString(data.getColumnIndex(CallLog.Calls.NUMBER)),
                                data.getInt(data.getColumnIndex(CallLog.Calls.DURATION)),
                                data.getLong(data.getColumnIndex(CallLog.Calls.DATE)));
                        /*if (i % 2 == 0) {
                            layout.setBackgroundColor(ZphoneApplication.color_grey_200);
                        } else {
                            layout.setBackgroundColor(ZphoneApplication.color_grey_100);
                        }*/
                        mCallLogLayout.addView(layout, callLogLayoutParams);
                    } while (data.moveToNext());
                } else {
                    Log.d("nothing", " no call log in the item");
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    private void updateUI() {
        if (mContactDisplayName != null) {
            Bundle bundle = new Bundle();
            bundle.putString("contactName", mContactDisplayName);
            getActivity().getSupportLoaderManager().restartLoader(ContactCallLogQuery.QUERY_ID, bundle, this);
        }
    }

    public void setContact(String displayName) {
        mContactDisplayName = displayName;
        updateUI();
    }


    /**
     * This interface defines constants used by contact retrieval queries.
     */
    public interface ContactDetailQuery {
        // A unique query ID to distinguish queries being run by the
        // LoaderManager.
        final static int QUERY_ID = 1;

        // The query projection (columns to fetch from the provider)
        @SuppressLint("InlinedApi")
        final static String[] PROJECTION = {
                ContactsContract.Contacts._ID,
                Utils.hasHoneycomb() ? ContactsContract.Contacts.DISPLAY_NAME_PRIMARY : ContactsContract.Contacts.DISPLAY_NAME,
        };

        // The query column numbers which map to each value in the projection
        final static int ID = 0;
        final static int DISPLAY_NAME = 1;
    }


    public interface ContactCallLogQuery {
        final static int QUERY_ID = 4;
        final static String[] PROJECTION = {
                CallLog.Calls.NUMBER,
                CallLog.Calls.DURATION,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
        };
    }

    public interface ContactName {
        public void setContact(String diasplyName);
    }
}
