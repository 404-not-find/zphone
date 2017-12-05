
package com.zycoo.android.zphone.ui.contacts;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.zycoo.android.zphone.R;
import com.zycoo.android.zphone.ZphoneApplication;
import com.zycoo.android.zphone.ui.contacts.ContactsListFragment.ContactsQuery;
import com.zycoo.android.zphone.utils.Utils;
import com.zycoo.android.zphone.widget.SuperAwesomeCardFragment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.hoang8f.android.segmented.SegmentedGroup;

public class ContactsContainerFragment extends SuperAwesomeCardFragment implements
        android.view.View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private final Logger mLogger = LoggerFactory.getLogger(ContactsContainerFragment.class
            .getSimpleName());
    private static final String CURRENT_FRAGMENT = "CURRENT_CONTACTS_FRAGMENT";
    public static final int UNIQUE_MENU_GROUP_ID = 0;
    public static final int UNIQUE_MENU_SEARCH_ID = 0;
    public static final int UNIQUE_MENU_RELOAD_ID = 1;
    private boolean mIsSearchResultView = false;
    private int mCurrentlyShowingFragment = 0;
    private String mSearchTerm; // Stores the current search query term
    //android-segmented-control replace LineLayout
    /*private TextView mLocalTextView;
    private TextView mRemoteTextView;*/
    private SegmentedGroup mContacts_local_remote_segmented;
    ContactsListFragment contactsListFragment = ContactsListFragment.newInstance(0);
    ContactListFragment contactListFragment = ContactListFragment.newInstance(1);
    private boolean mIsVisibleToUser;

    public static ContactsContainerFragment newInstance(int position) {
        ContactsContainerFragment f = new ContactsContainerFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);
        return f;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        mIsVisibleToUser = isVisibleToUser;

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        if(savedInstanceState == null)
        {
            mCurrentlyShowingFragment=0;
        }
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts_container, null);
        setHasOptionsMenu(true);
        /*
        mLocalTextView = (TextView) view.findViewById(R.id.contacts_local_tv);
        mRemoteTextView = (TextView) view.findViewById(R.id.contacts_pbx_tv);
        mLocalTextView.setOnClickListener(this);
        mRemoteTextView.setOnClickListener(this);*/
        mContacts_local_remote_segmented = (SegmentedGroup) view.findViewById(R.id.contacts_local_remote_segmented);
        mContacts_local_remote_segmented.setOnCheckedChangeListener(this);
        //mContacts_local_remote_segmented.clearCheck();
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (null != savedInstanceState) {
            mCurrentlyShowingFragment = savedInstanceState.getInt(CURRENT_FRAGMENT);
        }
        switchLocalRemoteFragment();
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(CURRENT_FRAGMENT, mCurrentlyShowingFragment);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        SearchView sv = new SearchView(getActivity().getActionBar().getThemedContext());
        switch (mCurrentlyShowingFragment) {
            case 0:
                sv.setQueryHint(ZphoneApplication.getAppResources().getString(
                        R.string.contacts_search_local));
                break;
            case 1:
                sv.setQueryHint(ZphoneApplication.getAppResources().getString(
                        R.string.contacts_search_remote));
                sv.setVisibility(View.INVISIBLE);
                menu.add(UNIQUE_MENU_GROUP_ID, UNIQUE_MENU_RELOAD_ID, 1,
                        ZphoneApplication.getAppResources().getString(R.string.contacts_pbx_reload))
                        .setIcon(R.drawable.ic_replay_white)
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            default:
                break;
        }

        menu.add(UNIQUE_MENU_GROUP_ID, UNIQUE_MENU_SEARCH_ID, 0,
                ZphoneApplication.getAppResources().getString(R.string.contacts_search_local))
                .setActionView(sv)
                .setIcon(R.drawable.ic_search_white).setShowAsAction(
                MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        MenuItem searchItem = menu.getItem(0);
        if (mCurrentlyShowingFragment == 1) {
            searchItem.setVisible(false);
        }

        if (mIsSearchResultView) {
            searchItem.setVisible(false);
        }
        // Retrieves the system search manager service
        final SearchManager searchManager =
                (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);

        // Retrieves the SearchView from the search menu item
        final SearchView searchView = (SearchView) searchItem.getActionView();

        // Assign searchable info to SearchView
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getActivity().getComponentName()));

        // Set listeners for SearchView
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String queryText) {
                // Nothing needs to happen when the user submits the search string
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Called when the action bar search text has changed.  Updates
                // the search filter, and restarts the loader to do a new query
                // using the new search string.
                String newFilter = !TextUtils.isEmpty(newText) ? newText : null;
                // Don't do anything if the filter is empty
                if (mSearchTerm == null && newFilter == null) {
                    return true;
                }

                // Don't do anything if the new filter is the same as the current filter
                if (mSearchTerm != null && mSearchTerm.equals(newFilter)) {
                    return true;
                }

                // Updates current filter to new filter
                mSearchTerm = newFilter;

                switch (mCurrentlyShowingFragment) {
                    case 0:
                        // Restarts the loader. This triggers onCreateLoader(), which builds the
                        // necessary content Uri from mSearchTerm.
                        ContactsListFragment contactsListFragment = (ContactsListFragment) getChildFragmentManager()
                                .findFragmentByTag(
                                        ContactsListFragment.class.getCanonicalName());
                        contactsListFragment.setSearchTerm(mSearchTerm);
                        contactsListFragment.getLoaderManager().restartLoader(
                                ContactsQuery.QUERY_ID, null, contactsListFragment);
                        break;
                    case 1:
                        break;
                    default:
                        break;
                }

                return true;
            }
        });

        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                // Nothing to do when the action item is expanded
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {

                switch (mCurrentlyShowingFragment) {
                    case 0:
                        // When the user collapses the SearchView the current search string is
                        // cleared and the loader restarted.
                        if (!TextUtils.isEmpty(mSearchTerm)) {
                            ((ContactsListFragment) getChildFragmentManager()
                                    .findFragmentByTag(
                                            ContactsListFragment.class.getCanonicalName()))
                                    .onSelectionCleared();
                        }
                        mSearchTerm = null;
                        getLoaderManager().restartLoader(
                                ContactsQuery.QUERY_ID, null,
                                (ContactsListFragment) getChildFragmentManager()
                                        .findFragmentByTag(
                                                ContactsListFragment.class.getCanonicalName()));
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        if (mSearchTerm != null) {
            // If search term is already set here then this fragment is
            // being restored from a saved state and the search menu item
            // needs to be expanded and populated again.

            // Stores the search term (as it will be wiped out by
            // onQueryTextChange() when the menu item is expanded).
            final String savedSearchTerm = mSearchTerm;

            // Expands the search menu item
            if (Utils.hasICS()) {
                searchItem.expandActionView();
            }
            // Sets the SearchView to the previous search string
            searchView.setQuery(savedSearchTerm, false);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (mCurrentlyShowingFragment) {
            case 0:
                ContactsListFragment contactsListFragment = (ContactsListFragment) getChildFragmentManager()
                        .findFragmentByTag(
                                ContactsListFragment.class.getCanonicalName());
                if (null != contactsListFragment) {
                    contactsListFragment.onOptionsItemSelected(item);
                }
                break;
            case 1:
                ContactListFragment contactListFragment = (ContactListFragment) getChildFragmentManager()
                        .findFragmentByTag(
                                ContactListFragment.class.getCanonicalName());
                if (null != contactListFragment) {
                    contactListFragment.onOptionsItemSelected(item);
                }
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        FragmentTransaction fragmentTransaction = this.getChildFragmentManager()
                .beginTransaction();
        switch (v.getId()) {
            /*case R.id.contacts_local_tv:
                mLocalTextView.setClickable(false);
                mRemoteTextView.setClickable(true);
                mLocalTextView.setTextColor(getResources().getColor(R.color.white));
                mRemoteTextView.setTextColor(getResources().getColor(R.color.grey_500));
                mRemoteTextView.setBackgroundColor(getResources().getColor(R.color.grey_700));
                mCurrentlyShowingFragment = 0;
                ContactsListFragment contactsListFragment = ContactsListFragment.newInstance(0);
                fragmentTransaction.replace(R.id.contacts_container, contactsListFragment,
                        ContactsListFragment.class.getCanonicalName());
                fragmentTransaction.commit();
                break;
            case R.id.contacts_pbx_tv:
                mRemoteTextView.setTextColor(getResources().getColor(R.color.white));
                mLocalTextView.setClickable(true);
                mRemoteTextView.setClickable(false);
                mLocalTextView.setTextColor(getResources().getColor(R.color.grey_500));
                mLocalTextView.setBackgroundColor(getResources().getColor(R.color.grey_700));
                mCurrentlyShowingFragment = 1;
                ContactListFragment contactListFragment = ContactListFragment.newInstance(1);
                fragmentTransaction.replace(R.id.contacts_container, contactListFragment,
                        ContactListFragment.class.getCanonicalName());
                fragmentTransaction.commit();
                break;*/
            default:
                break;
        }

    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.contacts_local_rb:
                mCurrentlyShowingFragment = 0;
                break;
            case R.id.contacts_remote_rb:
                mCurrentlyShowingFragment = 1;
                break;
            default:
                break;
        }
        switchLocalRemoteFragment();
    }

    private void switchLocalRemoteFragment() {
        FragmentTransaction fragmentTransaction = this.getChildFragmentManager()
                .beginTransaction();
        switch (mCurrentlyShowingFragment) {
            case 0:
                if (contactsListFragment.isAdded()) {
                    fragmentTransaction.hide(contactListFragment).show(contactsListFragment);
                } else {
                    fragmentTransaction.hide(contactListFragment).add(R.id.contacts_container, contactsListFragment,
                            ContactsListFragment.class.getCanonicalName());
                }
                break;
            case 1:
                if (contactListFragment.isAdded()) {
                    fragmentTransaction.hide(contactsListFragment).show(contactListFragment);
                } else {
                    fragmentTransaction.hide(contactsListFragment).add(R.id.contacts_container, contactListFragment,
                            ContactListFragment.class.getCanonicalName()).show(contactListFragment);
                }
                break;
            default:
                break;
        }
        fragmentTransaction.commit();
    }
}
