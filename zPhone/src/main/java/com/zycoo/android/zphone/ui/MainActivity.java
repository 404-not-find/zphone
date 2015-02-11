package com.zycoo.android.zphone.ui;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.diegocarloslima.fgelv.bak.WrapperExpandableListAdapter;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.hp.views.PagerSlidingTabStrip;
import com.hp.views.PagerSlidingTabStrip.TitleIconTabProvider;
import com.zycoo.android.zphone.BuildConfig;
import com.zycoo.android.zphone.NativeService;
import com.zycoo.android.zphone.NativeService.NativeServiceBinder;
import com.zycoo.android.zphone.R;
import com.zycoo.android.zphone.ZphoneApplication;
import com.zycoo.android.zphone.ui.contacts.ContactDetailActivity;
import com.zycoo.android.zphone.ui.contacts.ContactsContainerFragment;
import com.zycoo.android.zphone.ui.contacts.ContactsListFragment;
import com.zycoo.android.zphone.ui.dialpad.DialerFragment;
import com.zycoo.android.zphone.ui.message.MessageFragment;
import com.zycoo.android.zphone.ui.message.MessageAdapter;
import com.zycoo.android.zphone.utils.Theme;
import com.zycoo.android.zphone.utils.Utils;
import com.zycoo.android.zphone.ZycooConfigurationEntry;
import com.zycoo.android.zphone.ui.me.MeFragment;
import com.zycoo.android.zphone.widget.SuperAwesomeCardFragment;

import org.doubango.ngn.events.NgnEventArgs;
import org.doubango.ngn.events.NgnRegistrationEventArgs;
import org.doubango.ngn.sip.NgnSubscriptionSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

public class MainActivity extends SherlockFragmentActivity implements
        ContactsListFragment.OnContactsInteractionListener {
    private static final int DEFAULT_DISPLAY_ITEM = 2;
    private Logger mLogger = LoggerFactory.getLogger(MainActivity.class);
    private BroadcastReceiver mBroadcastReceiver;
    private NativeService mNativeService;
    private IBinder mNativeServiceBinder;
    private ServiceConnection mServiceConnection;
    private int mCurrentColor;
    private PagerSlidingTabStrip mPagerSlidingTabs;
    private ViewPager mViewPagers;
    private NgnSubscriptionSession subSession;
    // True if this activity instance is a search result view (used on pre-HC devices that load
    // search results in a separate instance of the activity rather than loading results in-line
    // as the query is typed.
    // android 14以前版本将不同显示
    private boolean isSearchResultView = false;

    private static class MainHandler extends Handler {
        private final WeakReference<MainActivity> mMainActivity;

        public MainHandler(MainActivity activity) {
            mMainActivity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainActivity activity = mMainActivity.get();
            if (null != activity) {
                switch (msg.what) {
                    case MessageFragment.HANDLE_WHAT:
                        MessageFragment messageFragment = (MessageFragment) activity.getSupportFragmentManager()
                                .findFragmentByTag(
                                        "android:switcher:" + R.id.pager + ":0");
                        if (null != messageFragment) {
                            MessageAdapter messageAdapter = messageFragment.getMessageAdapter();
                            WrapperExpandableListAdapter wrapperAdapter = messageFragment
                                    .getWrapperAdapter();
                            if (null != messageAdapter) {
                                messageAdapter.getVoiceMailsFromDB();
                                messageAdapter.getMonitorsFromDB();
                                messageAdapter.notifyDataSetChanged();
                                wrapperAdapter.notifyDataSetChanged();
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private final MainHandler handler = new MainHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Google analytics
        // Get tracker.
        Tracker t = ((ZphoneApplication) getApplication()).getTracker(
                ZphoneApplication.TrackerName.APP_TRACKER);
        // Set screen name.
        t.setScreenName(MainActivity.class.getCanonicalName());
        // Send a screen view.
        t.send(new HitBuilders.ScreenViewBuilder().build());

        //Debug
        if (BuildConfig.DEBUG) {
            Utils.enableStrictMode();
        }

        if (null != savedInstanceState) {
            mCurrentColor = savedInstanceState.getInt(ZycooConfigurationEntry.CURRENT_COLOR);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if this activity instance has been triggered as a result of a search query. This
        // will only happen on pre-HC OS versions as from HC onward search is carried out using
        // an ActionBar SearchView which carries out the search in-line without loading a new
        // Activity.
        if (Intent.ACTION_SEARCH.equals(getIntent().getAction())) {
            // Fetch query from intent and notify the fragment that it should display search
            // results instead of all contacts.
            String searchQuery = getIntent().getStringExtra(SearchManager.QUERY);
            ContactsListFragment mContactsListFragment = (ContactsListFragment)
                    getSupportFragmentManager().findFragmentById(R.id.contact_list);

            // This flag notes that the Activity is doing a search, and so the result will be
            // search results rather than all contacts. This prevents the Activity and Fragment
            // from trying to a search on search results.
            isSearchResultView = true;
            mContactsListFragment.setSearchQuery(searchQuery);

            // Set special title for search results
            String title = getString(R.string.contacts_list_search_results_title, searchQuery);
            setTitle(title);
        }

        mPagerSlidingTabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        mViewPagers = (ViewPager) findViewById(R.id.pager);
        mViewPagers.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
        mViewPagers.setCurrentItem(DEFAULT_DISPLAY_ITEM);
        setViewPagerScrollSpeed();

        // fade animation
        mViewPagers.setPageTransformer(false, new ViewPager.PageTransformer() {
            private float MIN_SCALE = 0.5f;
            private float MIN_ALPHA = 0.4f;

            @Override
            public final void transformPage(final View view, final float position) {
                if (position < -1) { // [-Infinity,-1)
                    // This page is way off-screen to the left.
                    Utils.CustomSetAlpha(view);
                } else if (position <= 1) { // [-1,1]
                    float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                    Utils.CustomSetAlpha(view, MIN_ALPHA + (scaleFactor - MIN_SCALE)
                            / (1 - MIN_SCALE) * (1 - MIN_ALPHA));
                } else { // (1,+Infinity]
                    // This page is way off-screen to the right.
                    Utils.CustomSetAlpha(view);
                }
            }
        });

        //TODO setPageMargin
        //issue https://github.com/astuetz/PagerSlidingTabStrip/issues/27
        /*final int pageMargin = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                        .getDisplayMetrics());
        mViewPagers.setPageMargin(pageMargin);*/


        mPagerSlidingTabs.setViewPager(mViewPagers);
        mCurrentColor = ZphoneApplication.getConfigurationService().getInt(
                ZycooConfigurationEntry.CURRENT_COLOR,
                getResources().getColor(R.color.light_blue));
        changeColor(mCurrentColor);
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Registration Events
                if (NgnRegistrationEventArgs.ACTION_REGISTRATION_EVENT.equals(intent.getAction())) {
                    NgnRegistrationEventArgs args = intent
                            .getParcelableExtra(NgnEventArgs.EXTRA_EMBEDDED);
                    if (args == null) {
                        mLogger.error("Invalid event args");
                        return;
                    }
                    //注册提示
                    Toast.makeText(ZphoneApplication.getContext(), args.getPhrase(),
                            Toast.LENGTH_SHORT).show();
                    MeFragment meFragment = (MeFragment) getSupportFragmentManager()
                            .findFragmentByTag(
                                    "android:switcher:" + R.id.pager + ":3");
                    DialerFragment dialerFragment =
                            (DialerFragment) getSupportFragmentManager().findFragmentByTag(
                                    "android:switcher:" + R.id.pager + ":2");
                    if (null != dialerFragment && dialerFragment.isVisible()) {
                        dialerFragment.statusChange(args.getEventType());
                    }
                    if (null != meFragment && meFragment.isVisible()) {
                        meFragment.statusChange(args.getEventType());
                    }
                }
            }
        };
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NgnRegistrationEventArgs.ACTION_REGISTRATION_EVENT);
        intentFilter.addAction(MessageFragment.UPDATE_MESSAGE_FRAGMENT);
        registerReceiver(mBroadcastReceiver, intentFilter);

        Intent intent = new Intent(ZphoneApplication.getContext(), NativeService.class);
        bindService(intent, mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mNativeServiceBinder = service;
                if (service instanceof NativeServiceBinder) {
                    mNativeService = ((NativeServiceBinder) service).getService();
                    mNativeService.setHandler(handler);
                } else {
                    mNativeServiceBinder = null;
                    mLogger.error("bindService failure ");
                }
            }
        }, BIND_AUTO_CREATE); //BIND_ABOVE_CLIENT

    }

    public void changeColor(int newColor) {
        mPagerSlidingTabs.setIndicatorColor(newColor);
        new Theme(this, handler).changeActionBarColor(newColor);
        mCurrentColor = newColor;
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //cancel onSaveInstanceState, because some time fragment display than one time
        //super.onSaveInstanceState(outState);
        outState.putInt(ZycooConfigurationEntry.CURRENT_COLOR, mCurrentColor);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mCurrentColor = savedInstanceState.getInt(ZycooConfigurationEntry.CURRENT_COLOR);
        changeColor(mCurrentColor);
    }

    public class MyPagerAdapter extends FragmentPagerAdapter implements TitleIconTabProvider {
        private final String[] TITLES = {
                getResources().getString(R.string.message),
                getResources().getString(R.string.contacts),
                getResources().getString(R.string.dialer),
                getResources().getString(R.string.me)
        };
        private final int[] ICONS = {
                R.drawable.ic_message_grey600,
                R.drawable.ic_contacts_grey600,
                R.drawable.ic_dialer_sip_grey600,
                R.drawable.ic_account_box_grey600
        };

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getPageIconResId(int position) {
            return ICONS[position];
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
                    return MessageFragment.newInstance(position);
                case 1:
                    return ContactsContainerFragment.newInstance(position);
                case 2:
                    return DialerFragment.newInstance(position);
                case 3:
                    return MeFragment.newInstance(position);
                default:
                    return SuperAwesomeCardFragment.newInstance(position);
            }
        }
    }

    /**
     * This interface callback lets the main contacts list fragment notify this
     * activity that a contact has been selected.
     *
     * @param contactUri The contact Uri to the selected contact.
     */
    @Override
    public void onContactSelected(Uri contactUri) {
        //start a new ContactDetailActivity with
        // the contact Uri
        Intent intent = new Intent(this, ContactDetailActivity.class);
        intent.setData(contactUri);
        startActivity(intent);

    }

    /**
     * This interface callback lets the main contacts list fragment notify this
     * activity that a contact is no longer selected.
     */
    @Override
    public void onSelectionCleared() {

    }

    @Override
    public boolean onSearchRequested() {
        // Don't allow another search if this activity instance is already showing
        // search results. Only used pre-HC.
        return !isSearchResultView && super.onSearchRequested();
    }

    @Override
    protected void onDestroy() {
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
        if (null != mServiceConnection) {
            unbindService(mServiceConnection);
            mServiceConnection = null;
        }
        super.onDestroy();
    }

    public Handler getHandler() {
        return handler;
    }

    public NativeService getNativeService() {
        return mNativeService;
    }

    /**
     * edit number on before call
     *
     * @param number need call phone number
     */
    @Override
    public void ChangeNumberCall(String number) {
        mViewPagers.setCurrentItem(2);
        DialerFragment fragment = (DialerFragment) getSupportFragmentManager()
                .findFragmentByTag(
                        "android:switcher:" + R.id.pager + ":2");
        fragment.setCallText(number);
    }

    /**
     * close all display, but don't close app
     */
    @Override
    public void onBackPressed() {
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addCategory(Intent.CATEGORY_HOME);
        startActivity(i);
    }

    /**
     * custom scroll duration
     */
    private void setViewPagerScrollSpeed() {
        try {
            Field mScroller = null;
            mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            ViewPagerScroller scroller = new ViewPagerScroller(mViewPagers.getContext());
            mScroller.set(mViewPagers, scroller);
        } catch (Exception e) {
            mLogger.error("setViewPagerScrollSpeed error " + e.getStackTrace());
        }
    }
}
