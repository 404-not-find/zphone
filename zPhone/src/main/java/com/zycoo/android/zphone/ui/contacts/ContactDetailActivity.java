package com.zycoo.android.zphone.ui.contacts;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.hp.views.PagerSlidingTabStrip;
import com.zycoo.android.zphone.BuildConfig;
import com.zycoo.android.zphone.R;
import com.zycoo.android.zphone.ZphoneApplication;
import com.zycoo.android.zphone.ui.dialpad.DialerFragment;
import com.zycoo.android.zphone.ui.me.MeFragment;
import com.zycoo.android.zphone.ui.message.MessageFragment;
import com.zycoo.android.zphone.utils.ImageLoader;
import com.zycoo.android.zphone.utils.Theme;
import com.zycoo.android.zphone.utils.Utils;
import com.zycoo.android.zphone.utils.ZycooConfigurationEntry;
import com.zycoo.android.zphone.widget.SuperAwesomeCardFragment;

import java.io.FileNotFoundException;
import java.io.IOException;

public class ContactDetailActivity extends SherlockFragmentActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG_DETAIL = ContactDetailFragment.class.getCanonicalName();
    private static final String TAG_HISTORY = ContactHistoryFragment.class.getCanonicalName();
    private static final String TAG_VOICE = ContactVoiceFragment.class.getCanonicalName();
    private Uri mContactUri; // Stores the contact Uri for this fragment instance
    private ImageLoader mImageLoader; // Handles loading the contact image in a background thread
    private Handler mHandler = new Handler();
    private PagerSlidingTabStrip tabs;
    private ViewPager pager;
    private ContactsDetailAdapter adapter;
    private ImageView mImageView;
    private MenuItem mEditContactMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.DEBUG) {
            // Enable strict mode checks when in debug modes
            Utils.enableStrictMode();
        }
        // This activity expects to receive an intent that contains the uri of a contact
        if (getIntent() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Fetch the data Uri from the intent provided to this activity
            final Uri uri = getIntent().getData();
            /*// Checks to see if fragment has already been added, otherwise adds a new
            // ContactDetailFragment with the Uri provided in the intent
            if (getSupportFragmentManager().findFragmentByTag(TAG) == null) {
                final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                // Adds a newly created ContactDetailFragment that is instantiated with the
                // data Uri
                ft.add(android.R.id.content, ContactDetailFragment.newInstance(uri), TAG);
                ft.commit();
            }*/
            setContentView(R.layout.activity_contact_detail);
            tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
            pager = (ViewPager) findViewById(R.id.pager);
            adapter = new ContactsDetailAdapter(getSupportFragmentManager());
            pager.setAdapter(adapter);
            pager.setCurrentItem(1);
            tabs.setViewPager(pager);
            // Let this activity contribute menu items
            //setHasOptionsMenu(true);

        /*
         * The ImageLoader takes care of loading and resizing images asynchronously into the
         * ImageView. More thorough sample code demonstrating background image loading as well as
         * details on how it works can be found in the following Android Training class:
         * http://developer.android.com/training/displaying-bitmaps/
         */
            mImageLoader = new ImageLoader(this, getLargestScreenDimension()) {
                @Override
                protected Bitmap processBitmap(Object data) {
                    // This gets called in a background thread and passed the data from
                    // ImageLoader.loadImage().
                    return loadContactPhoto((Uri) data, getImageSize());

                }
            };

            // Set a placeholder loading image for the image loader
            mImageLoader.setLoadingImage(R.drawable.ic_contact_picture_180_holo_light);

            // Tell the image loader to set the image directly when it's finished loading
            // rather than fading in
            mImageLoader.setImageFadeIn(false);

        } else {
            // No intent provided, nothing to do so finish()
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Tapping on top left ActionBar icon navigates "up" to hierarchical parent screen.
                // The parent is defined in the AndroidManifest entry for this activity via the
                // parentActivityName attribute (and via meta-data tag for OS versions before API
                // Level 16). See the "Tasks and Back Stack" guide for more information:
                // http://developer.android.com/guide/components/tasks-and-back-stack.html
                //NavUtils.navigateUpFromSameTask(this);
                finish();
                return true;
        }
        // Otherwise, pass the item to the super implementation for handling, as described in the
        // documentation.
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            // Two main queries to load the required information
            case ContactDetailQuery.QUERY_ID:
                // This query loads main contact details, see
                // ContactDetailQuery for more information.
                return new CursorLoader(this, mContactUri,
                        ContactDetailQuery.PROJECTION,
                        null, null, null);
            case ContactAddressQuery.QUERY_ID:
                // This query loads contact address details, see
                // ContactAddressQuery for more information.
                final Uri uri = Uri.withAppendedPath(mContactUri, ContactsContract.Contacts.Data.CONTENT_DIRECTORY);
                return new CursorLoader(this, uri,
                        ContactAddressQuery.PROJECTION,
                        ContactAddressQuery.SELECTION,
                        null, null);

            case ContactCallLogQuery.QUERY_ID:
                String str = args.getString("contactName");
                return new CursorLoader(this, Uri.parse("content://call_log/calls"),
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
        if (mContactUri == null) {
            return;
        }

        switch (loader.getId()) {
            case ContactDetailQuery.QUERY_ID:
                // Moves to the first row in the Cursor
                if (data.moveToFirst()) {
                    // For the contact details query, fetches the contact display name.
                    // ContactDetailQuery.DISPLAY_NAME maps to the appropriate display
                    // name field based on OS version.
                    final String contactName = data.getString(ContactDetailQuery.DISPLAY_NAME);
                    getActionBar().setTitle(contactName);
                    Bundle bundle = new Bundle();
                    bundle.putString("contactName", contactName);
                    getSupportLoaderManager().restartLoader(ContactCallLogQuery.QUERY_ID, bundle, this);
                }
                break;
            case ContactAddressQuery.QUERY_ID:
                // This query loads the contact address details. More than
                // one contact address is possible, so move each one to a
                // LinearLayout in a Scrollview so multiple addresses can
                // be scrolled by the user.

                // Each LinearLayout has the same LayoutParams so this can
                // be created once and used for each address.
                final LinearLayout.LayoutParams layoutParams =
                        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);

                // Clears out the details layout first in case the details
                // layout has addresses from a previous data load still
                // added as children.
                //TODO
                //mDetailsLayout.removeAllViews();

                // Loops through all the rows in the Cursor
                if (data.moveToFirst()) {
                    do {
                        //TODO
                        /*// Builds the address layout
                        final LinearLayout layout = buildAddressLayout(
                                data.getInt(ContactAddressQuery.TYPE),
                                data.getString(ContactAddressQuery.LABEL),
                                data.getString(ContactAddressQuery.ADDRESS));
                        // Adds the new address layout to the details layout
                        mDetailsLayout.addView(layout, layoutParams);*/


                    } while (data.moveToNext());
                } else {
                    // If nothing found, adds an empty address layout
                    Log.e("nothing", " adds an empty address layout");
                    //mDetailsLayout.addView(buildEmptyAddressLayout(), layoutParams);
                }
                break;
            case ContactCallLogQuery.QUERY_ID:
                // Each LinearLayout has the same LayoutParams so this can
                // be created once and used for each address.
                final LinearLayout.LayoutParams callLogLayoutParams =
                        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);
                //TODO 移动到HISTORY
               /* mCallLogLayout.removeAllViews();
                if (data.moveToFirst()) {
                    int i = 0;
                    do {
                        i++;
                        //Builds the address layout
                        final LinearLayout layout = buildCallLogLayout(
                                data.getInt(data.getColumnIndex(CallLog.Calls.TYPE)),
                                data.getString(data.getColumnIndex(CallLog.Calls.NUMBER)),
                                data.getInt(data.getColumnIndex(CallLog.Calls.DURATION)),
                                data.getLong(data.getColumnIndex(CallLog.Calls.DATE)));
                        if (i % 2 == 0) {
                            layout.setBackgroundColor(ZphoneApplication.color_grey_200);
                        } else {
                            layout.setBackgroundColor(ZphoneApplication.color_grey_100);
                        }

                        mCallLogLayout.addView(layout, callLogLayoutParams);

                    } while (data.moveToNext());
                } else {
                    Log.d("nothing", " no call log in the item");
                }*/
                break;

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Nothing to do here. The Cursor does not need to be released as it was never directly
        // bound to anything (like an adapter).
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //TODO 编辑联系人
        /*
        // Inflates the options menu for this fragment
        inflater.inflate(R.menu.contact_detail_menu, menu);

        // Gets a handle to the "find" menu item
        mEditContactMenuItem = menu.findItem(R.id.menu_edit_contact);

        // If contactUri is null the edit menu item should be hidden, otherwise
        // it is visible.
        mEditContactMenuItem.setVisible(mContactUri != null);
        super.onCreateOptionsMenu(menu, inflater);*/
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Sets the contact that this Fragment displays, or clears the display if
     * the contact argument is null. This will re-initialize all the views and
     * start the queries to the system contacts provider to populate the contact
     * information.
     *
     * @param contactLookupUri The contact lookup Uri to load and display in
     *                         this fragment. Passing null is valid and the fragment will
     *                         display a message that no contact is currently selected
     *                         instead.
     */
    public void setContact(Uri contactLookupUri) {

        // In version 3.0 and later, stores the provided contact lookup Uri in a class field. This
        // Uri is then used at various points in this class to map to the provided contact.
        if (Utils.hasHoneycomb()) {
            mContactUri = contactLookupUri;
        } else {
            // For versions earlier than Android 3.0, stores a contact Uri that's constructed from
            // contactLookupUri. Later on, the resulting Uri is combined with
            // Contacts.Data.CONTENT_DIRECTORY to map to the provided contact. It's done
            // differently for these earlier versions because Contacts.Data.CONTENT_DIRECTORY works
            // differently for Android versions before 3.0.
            mContactUri = ContactsContract.Contacts.lookupContact(getContentResolver(),
                    contactLookupUri);
        }

        // If the Uri contains data, load the contact's image and load contact details.
        if (contactLookupUri != null) {
            // Asynchronously loads the contact image
            mImageLoader.loadImage(mContactUri, mImageView);
            // Shows the contact photo ImageView and hides the empty view
            mImageView.setVisibility(View.VISIBLE);

            //mEmptyView.setVisibility(View.GONE);

            // Shows the edit contact action/menu item
            if (mEditContactMenuItem != null) {
                mEditContactMenuItem.setVisible(true);
            }

            // Starts two queries to to retrieve contact information from the Contacts Provider.
            // restartLoader() is used instead of initLoader() as this method may be called
            // multiple times.
            getSupportLoaderManager().restartLoader(ContactDetailQuery.QUERY_ID, null, this);
            getSupportLoaderManager().restartLoader(ContactAddressQuery.QUERY_ID, null, this);

        } else {
            // If contactLookupUri is null, then the method was called when no contact was selected
            // in the contacts list. This should only happen in a two-pane layout when the user
            // hasn't yet selected a contact. Don't display an image for the contact, and don't
            // account for the view's space in the layout. Turn on the TextView that appears when
            // the layout is empty, and set the contact name to the empty string. Turn off any menu
            // items that are visible.
            mImageView.setVisibility(View.GONE);
            // TODO
           /* mEmptyView.setVisibility(View.VISIBLE);
            mDetailsLayout.removeAllViews();*/

            getSupportActionBar().setTitle("");
            if (mEditContactMenuItem != null) {
                mEditContactMenuItem.setVisible(false);
            }
        }
    }


    /**
     * Decodes and returns the contact's thumbnail image.
     *
     * @param contactUri The Uri of the contact containing the image.
     * @param imageSize  The desired target width and height of the output image
     *                   in pixels.
     * @return If a thumbnail image exists for the contact, a Bitmap image,
     * otherwise null.
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private Bitmap loadContactPhoto(Uri contactUri, int imageSize) {

        // Ensures the Fragment is still added to an activity. As this method is called in a
        // background thread, there's the possibility the Fragment is no longer attached and
        // added to an activity. If so, no need to spend resources loading the contact photo.
       /* if (!isAdded() || getActivity() == null) {
            return null;
        }*/

        // Instantiates a ContentResolver for retrieving the Uri of the image
        final ContentResolver contentResolver = getContentResolver();

        // Instantiates an AssetFileDescriptor. Given a content Uri pointing to an image file, the
        // ContentResolver can return an AssetFileDescriptor for the file.
        AssetFileDescriptor afd = null;

        if (Utils.hasICS()) {
            // On platforms running Android 4.0 (API version 14) and later, a high resolution image
            // is available from Photo.DISPLAY_PHOTO.
            try {
                // Constructs the content Uri for the image
                Uri displayImageUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.DISPLAY_PHOTO);

                // Retrieves an AssetFileDescriptor from the Contacts Provider, using the
                // constructed Uri
                afd = contentResolver.openAssetFileDescriptor(displayImageUri, "r");
                // If the file exists
                if (afd != null) {
                    // Reads and decodes the file to a Bitmap and scales it to the desired size
                    return ImageLoader.decodeSampledBitmapFromDescriptor(
                            afd.getFileDescriptor(), imageSize, imageSize);
                }
            } catch (FileNotFoundException e) {
                // Catches file not found exceptions
                if (BuildConfig.DEBUG) {
                    // Log debug message, this is not an error message as this exception is thrown
                    // when a contact is legitimately missing a contact photo (which will be quite
                    // frequently in a long contacts list).
                    Log.d("TAG", "Contact photo not found for contact " + contactUri.toString()
                            + ": " + e.toString());
                }
            } finally {
                // Once the decode is complete, this closes the file. You must do this each time
                // you access an AssetFileDescriptor; otherwise, every image load you do will open
                // a new descriptor.
                if (afd != null) {
                    try {
                        afd.close();
                    } catch (IOException e) {
                        // Closing a file descriptor might cause an IOException if the file is
                        // already closed. Nothing extra is needed to handle this.
                    }
                }
            }
        }

        // If the platform version is less than Android 4.0 (API Level 14), use the only available
        // image URI, which points to a normal-sized image.
        try {
            // Constructs the image Uri from the contact Uri and the directory twig from the
            // Contacts.Photo table
            Uri imageUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);

            // Retrieves an AssetFileDescriptor from the Contacts Provider, using the constructed
            // Uri
            afd = getContentResolver().openAssetFileDescriptor(imageUri, "r");

            // If the file exists
            if (afd != null) {
                // Reads the image from the file, decodes it, and scales it to the available screen
                // area
                return ImageLoader.decodeSampledBitmapFromDescriptor(
                        afd.getFileDescriptor(), imageSize, imageSize);
            }
        } catch (FileNotFoundException e) {
            // Catches file not found exceptions
            if (BuildConfig.DEBUG) {
                // Log debug message, this is not an error message as this exception is thrown
                // when a contact is legitimately missing a contact photo (which will be quite
                // frequently in a long contacts list).
                Log.d("TAG", "Contact photo not found for contact " + contactUri.toString()
                        + ": " + e.toString());
            }
        } finally {
            // Once the decode is complete, this closes the file. You must do this each time you
            // access an AssetFileDescriptor; otherwise, every image load you do will open a new
            // descriptor.
            if (afd != null) {
                try {
                    afd.close();
                } catch (IOException e) {
                    // Closing a file descriptor might cause an IOException if the file is
                    // already closed. Ignore this.
                }
            }
        }

        // If none of the case selectors match, returns null.
        return null;
    }


    public class ContactsDetailAdapter extends FragmentPagerAdapter {

        private final String[] TITLES = {
                getResources().getString(R.string.contacts_history),
                getResources().getString(R.string.contacts_detail),
                getResources().getString(R.string.contacts_voice),
        };

        public ContactsDetailAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return ContactHistoryFragment.newInstance(position);
                case 1:
                    return ContactDetailFragment.newInstance(position);
                case 2:
                    return ContactVoiceFragment.newInstance(position);
                default:
                    return SuperAwesomeCardFragment.newInstance(position);
            }
        }
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

    /**
     * This interface defines constants used by address retrieval queries.
     */
    public interface ContactAddressQuery {
        // A unique query ID to distinguish queries being run by the
        // LoaderManager.
        final static int QUERY_ID = 2;

        // The query projection (columns to fetch from the provider)
        final static String[] PROJECTION = {
                ContactsContract.CommonDataKinds.StructuredPostal._ID,
                ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS,
                ContactsContract.CommonDataKinds.StructuredPostal.TYPE,
                ContactsContract.CommonDataKinds.StructuredPostal.LABEL,
        };

        // The query selection criteria. In this case matching against the
        // StructuredPostal content mime type.
        final static String SELECTION =
                ContactsContract.Data.MIMETYPE + "='" + ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE + "'";

        // The query column numbers which map to each value in the projection
        final static int ID = 0;
        final static int ADDRESS = 1;
        final static int TYPE = 2;
        final static int LABEL = 3;
    }

    public interface SipContactDetailQuery {
        final static int QUERY_ID = 3;
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
    /**
     * Fetches the width or height of the screen in pixels, whichever is larger.
     * This is used to set a maximum size limit on the contact photo that is
     * retrieved from the Contacts Provider. This limit prevents the app from
     * trying to decode and load an image that is much larger than the available
     * screen area.
     *
     * @return The largest screen dimension in pixels.
     */
    private int getLargestScreenDimension() {
        // Gets a DisplayMetrics object, which is used to retrieve the display's pixel height and
        // width
        final DisplayMetrics displayMetrics = new DisplayMetrics();

        // Retrieves a displayMetrics object for the device's default display
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final int height = displayMetrics.heightPixels;
        final int width = displayMetrics.widthPixels;

        // Returns the larger of the two values
        return height > width ? height : width;
    }
}
