/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zycoo.android.zphone.ui.contacts;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Contacts.Photo;
import android.provider.ContactsContract.Data;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.zycoo.android.zphone.BuildConfig;
import com.zycoo.android.zphone.Engine;
import com.zycoo.android.zphone.ui.MainActivity;
import com.zycoo.android.zphone.R;
import com.zycoo.android.zphone.ZphoneApplication;
import com.zycoo.android.zphone.ui.dialpad.ScreenAV;
import com.zycoo.android.zphone.utils.AndroidUtils;
import com.zycoo.android.zphone.utils.ImageLoader;
import com.zycoo.android.zphone.utils.Utils;
import com.zycoo.android.zphone.widget.SuperAwesomeCardFragment;

import org.doubango.ngn.media.NgnMediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * This fragment displays details of a specific contact from the contacts
 * provider. It shows the contact's display photo, name and all its mailing
 * addresses. You can also modify this fragment to show other information, such
 * as phone numbers, email addresses and so forth. This fragment appears
 * full-screen in an activity on devices with small screen sizes, and as part of
 * a two-pane layout on devices with larger screens, alongside the
 * {@link ContactsListFragment}. To create an instance of this fragment, use the
 * factory method {@link ContactDetailFragment#newInstance(android.net.Uri)},
 * passing as an argument the contact Uri for the contact you want to display.
 */
public class ContactDetailFragment extends SuperAwesomeCardFragment{

    public final Logger logger;
    public static final String EXTRA_CONTACT_URI =
            "com.example.android.contactslist.ui.EXTRA_CONTACT_URI";
    public static final String EXTRA_TAB_POSITION = "com.zycoo.android.zphone.ui.contacts.EXTRA_TAB_POSITION";

    // Defines a tag for identifying log entries
    private static final String TAG = "ContactDetailFragment";

    // The geo Uri scheme prefix, used with Intent.ACTION_VIEW to form a geographical address
    // intent that will trigger available apps to handle viewing a location (such as Maps)
    private static final String GEO_URI_SCHEME_PREFIX = "geo:0,0?q=";

    // Whether or not this fragment is showing in a two pane layout
    private boolean mIsTwoPaneLayout;



    // Used to store references to key views, layouts and menu items as these need to be updated
    // in multiple methods throughout this class.
    private LinearLayout mDetailsLayout;
    private LinearLayout mCallLogLayout;
    private TextView mEmptyView;


    /**
     * Factory method to generate a new instance of the fragment given a contact
     * Uri. A factory method is preferable to simply using the constructor as it
     * handles creating the bundle and setting the bundle as an argument.
     *
     * @param contactUri The contact Uri to load
     * @return A new instance of {@link ContactDetailFragment}
     */
    public static ContactDetailFragment newInstance(Uri contactUri) {
        // Create new instance of this fragment
        final ContactDetailFragment fragment = new ContactDetailFragment();

        // Create and populate the args bundle
        final Bundle args = new Bundle();
        args.putParcelable(EXTRA_CONTACT_URI, contactUri);

        // Assign the args bundle to the new fragment
        fragment.setArguments(args);

        // Return fragment
        return fragment;
    }

    /**
     * Fragments require an empty constructor.
     */
    public ContactDetailFragment() {
        logger = LoggerFactory.getLogger(ContactDetailFragment.class.getCanonicalName());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflates the main layout to be used by this fragment
        final View detailView =
                inflater.inflate(R.layout.fragment_contact_detail, container, false);

        // Gets handles to view objects in the layout
        mDetailsLayout = (LinearLayout) detailView.findViewById(R.id.contact_details_layout);
        mCallLogLayout = (LinearLayout) detailView.findViewById(R.id.contact_call_log_layout);
        mEmptyView = (TextView) detailView.findViewById(android.R.id.empty);
        return detailView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // If not being created from a previous state
       /* if (savedInstanceState == null) {
            // Sets the argument extra as the currently displayed contact
            setContact(getArguments() != null ?
                    (Uri) getArguments().getParcelable(EXTRA_CONTACT_URI) : null);
        } else {
            // If being recreated from a saved state, sets the contact from the incoming
            // savedInstanceState Bundle
            setContact((Uri) savedInstanceState.getParcelable(EXTRA_CONTACT_URI));
        }*/
    }

   /* *
     * When the Fragment is being saved in order to change activity state, add_to_contacts
     * the currently-selected contact.*/

   /* @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Saves the contact Uri
        outState.putParcelable(EXTRA_CONTACT_URI, mContactUri);
    }
*/

    //TODO 移动到Activity
    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.homeAsUp:
                Intent upIntent = new Intent(getActivity(), MainActivity.class);
                upIntent.putExtra(EXTRA_TAB_POSITION, 1);
                if (NavUtils.shouldUpRecreateTask(getActivity(), upIntent)) {
                    // This activity is not part of the application's task, so
                    // create a new task
                    // with a synthesized back stack.
                    TaskStackBuilder
                            .from(getActivity())
                            .addNextIntent(new Intent(getActivity(), MainActivity.class))
                            .addNextIntent(upIntent).startActivities();
                    getActivity().finish();
                } else {
                    // This activity is part of the application's task, so simply
                    // navigate up to the hierarchical parent activity.
                    NavUtils.navigateUpTo(getActivity(), upIntent);
                }
                return true;

            // When "edit" menu option selected
            case R.id.menu_edit_contact:
                // Standard system edit contact intent
                Intent intent = new Intent(Intent.ACTION_EDIT, mContactUri);

                // Because of an issue in Android 4.0 (API level 14), clicking Done or Back in the
                // People app doesn't return the user to your app; instead, it displays the People
                // app's contact list. A workaround, introduced in Android 4.0.3 (API level 15) is
                // to set a special flag in the extended data for the Intent you send to the People
                // app. The issue is does not appear in versions prior to Android 4.0. You can use
                // the flag with any version of the People app; if the workaround isn't needed,
                // the flag is ignored.
                intent.putExtra("finishActivityOnSaveCompleted", true);

                // Start the edit activity
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }*/




    /**
     * Builds an empty address layout that just shows that no addresses were
     * found for this contact.
     *
     * @return A LinearLayout to add to the contact details layout
     */
    private LinearLayout buildEmptyAddressLayout() {
        return buildAddressLayout(0, null, null);
    }

    private LinearLayout buildCallLogLayout(int callType, final String callNumber,
                                            final int callDuration, final long callDate) {
        // Inflates the address layout
        final LinearLayout callLogLayout =
                (LinearLayout) LayoutInflater.from(getActivity()).inflate(
                        R.layout.fragment_contact_detail_call_log_item, mCallLogLayout, false);
        //get views
        final ImageView callTypeIv = (ImageView) callLogLayout
                .findViewById(R.id.call_type_icons);
        final TextView callNumberTv = (TextView) callLogLayout
                .findViewById(R.id.call_numbe);
        final RelativeTimeTextView callDurationTv = (RelativeTimeTextView) callLogLayout
                .findViewById(R.id.call_history_duration_tv);
        final RelativeTimeTextView callDateTv = (RelativeTimeTextView) callLogLayout
                .findViewById(R.id.call_history_time_tv);
        // If there's no addresses for the contact, shows the empty view and message, and hides the
        // header and button.
        switch (callType) {
            case CallLog.Calls.INCOMING_TYPE:
                callTypeIv.setImageResource(R.drawable.call_incoming_45);
                break;
            case CallLog.Calls.OUTGOING_TYPE:
                callTypeIv.setImageResource(R.drawable.call_outgoing_45);
                break;

            case CallLog.Calls.MISSED_TYPE:
                callTypeIv.setImageResource(R.drawable.call_missed_45);
                break;
            default:
                break;
        }
        callNumberTv.setText(callNumber);
        if (callDuration > 0) {
            callDurationTv.setText(AndroidUtils.formatIntToTimeStr(callDuration));
        } else {
            callDurationTv.setVisibility(View.INVISIBLE);
        }
        callDateTv.setReferenceTime(callDate);
        final ImageButton callIv = (ImageButton) callLogLayout.findViewById(R.id.secondary_action_icon);
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

    /**
     * Builds an address LinearLayout based on address information from the
     * Contacts Provider. Each address for the contact gets its own LinearLayout
     * object; for example, if the contact has three postal addresses, then 3
     * LinearLayouts are generated.
     *
     * @param addressType      From
     *                         {@link android.provider.ContactsContract.CommonDataKinds.StructuredPostal#TYPE}
     * @param addressTypeLabel From
     *                         {@link android.provider.ContactsContract.CommonDataKinds.StructuredPostal#LABEL}
     * @param address          From
     *                         {@link android.provider.ContactsContract.CommonDataKinds.StructuredPostal#FORMATTED_ADDRESS}
     * @return A LinearLayout to add to the contact details layout, populated
     * with the provided address details.
     */
    private LinearLayout buildAddressLayout(int addressType, String addressTypeLabel,
                                            final String address) {

        // Inflates the address layout
        final LinearLayout addressLayout =
                (LinearLayout) LayoutInflater.from(getActivity()).inflate(
                        R.layout.fragment_contact_detail_item, mDetailsLayout, false);

        // Gets handles to the view objects in the layout
        final TextView headerTextView =
                (TextView) addressLayout.findViewById(R.id.contact_detail_header);
        final TextView addressTextView =
                (TextView) addressLayout.findViewById(R.id.contact_detail_item);
        final ImageButton viewAddressButton =
                (ImageButton) addressLayout.findViewById(R.id.button_view_address);

        // If there's no addresses for the contact, shows the empty view and message, and hides the
        // header and button.
        if (addressTypeLabel == null && addressType == 0) {
            headerTextView.setVisibility(View.GONE);
            viewAddressButton.setVisibility(View.GONE);
            addressTextView.setText(R.string.no_address);
        } else {
            // Gets postal address label type
            CharSequence label =
                    StructuredPostal.getTypeLabel(getResources(), addressType, addressTypeLabel);

            // Sets TextView objects in the layout
            headerTextView.setText(label);
            addressTextView.setText(address);

            // Defines an onClickListener object for the address button
            viewAddressButton.setOnClickListener(new View.OnClickListener() {
                // Defines what to do when users click the address button
                @Override
                public void onClick(View view) {

                    final Intent viewIntent =
                            new Intent(Intent.ACTION_VIEW, constructGeoUri(address));

                    // A PackageManager instance is needed to verify that there's a default app
                    // that handles ACTION_VIEW and a geo Uri.
                    final PackageManager packageManager = getActivity().getPackageManager();

                    // Checks for an activity that can handle this intent. Preferred in this
                    // case over Intent.createChooser() as it will still let the user choose
                    // a default (or use a previously set default) for geo Uris.
                    if (packageManager.resolveActivity(
                            viewIntent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
                        startActivity(viewIntent);
                    } else {
                        // If no default is found, displays a message that no activity can handle
                        // the view button.
                        Toast.makeText(getActivity(),
                                R.string.no_intent_found, Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
        return addressLayout;
    }

    /**
     * Constructs a geo scheme Uri from a postal address.
     *
     * @param postalAddress A postal address.
     * @return the geo:// Uri for the postal address.
     */
    private Uri constructGeoUri(String postalAddress) {
        // Concatenates the geo:// prefix to the postal address. The postal address must be
        // converted to Uri format and encoded for special characters.
        return Uri.parse(GEO_URI_SCHEME_PREFIX + Uri.encode(postalAddress));
    }
}
