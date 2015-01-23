
package com.zycoo.android.zphone.ui.contacts;

import android.app.SearchManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.NavUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.zycoo.android.zphone.ContactsPBXBean;
import com.zycoo.android.zphone.ContactsPBXBean.ContatcsPBXStatus;
import com.zycoo.android.zphone.Engine;
import com.zycoo.android.zphone.R;
import com.zycoo.android.zphone.ZphoneApplication;
import com.zycoo.android.zphone.ui.dialpad.ScreenAV;
import com.zycoo.android.zphone.utils.Theme;
import com.zycoo.android.zphone.utils.Utils;
import com.zycoo.android.zphone.utils.ZycooConfigurationEntry;

import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;
import org.asteriskjava.manager.ManagerConnectionState;
import org.asteriskjava.manager.TimeoutException;
import org.asteriskjava.manager.action.IaxPeerListAction;
import org.asteriskjava.manager.action.SipPeersAction;
import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.services.INgnSipService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class ContactsPBXActivity extends SherlockActivity implements OnClickListener,
        OnItemClickListener {

    private static String LOG_TAG = ContactsPBXActivity.class.getCanonicalName();
    private final Logger mLogger = LoggerFactory.getLogger(LOG_TAG);
    private final Handler mHandler;
    private Context mContext;
    private PullToRefreshListView mPullListView;
    private ListView mListView;
    private TextView mLoadingTv;
    private ProgressBar mLoadingPB;
    private ContactsPBXAdapter mAdapter;
    private int currentColor;
    private Collection<ContactsPBXBean> mSaveContactPBXs;
    private Collection<ContactsPBXBean> mContactsPBXs;
    private Collection<ContactsPBXBean> mSearchContactsPBXs;

    public ContactsPBXActivity()
    {
        mContext = this;
        mHandler = new Handler();
        mSearchContactsPBXs = new ArrayList<ContactsPBXBean>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts_pbx);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        currentColor = ZphoneApplication.getConfigurationService().getInt(
                ZycooConfigurationEntry.THEME_COLOR_KEY,
                getResources().getColor(R.color.light_blue));
        new Theme(this, mHandler).changeActionBarColor(currentColor);
        mPullListView = (PullToRefreshListView) findViewById(R.id.contasts_listView);
        mPullListView.setOnRefreshListener(new OnRefreshListener<ListView>() {

            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                String label = DateUtils.formatDateTime(getApplicationContext(),
                        System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE
                                | DateUtils.FORMAT_ABBREV_ALL);

                // Update the LastUpdatedLabel
                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
                new AsteriskAMIShowPeersTask().execute();

            }
        });
        mListView = mPullListView.getRefreshableView();
        mListView.setOnCreateContextMenuListener(this);
        mLoadingPB = (ProgressBar) findViewById(R.id.loading_pb);
        mLoadingTv = (TextView) findViewById(R.id.loading_tv);
        mAdapter = new ContactsPBXAdapter();
        new AsteriskAMIShowPeersTask().execute();
    }

    @Override
    public void onClick(View v)
    {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        SearchView sv = new SearchView(getSupportActionBar()
                .getThemedContext());
        sv.setQueryHint(getResources().getString(R.string.search_contacts_hint));
        //sv.setOnQueryTextListener(getActivity());
        menu.add(0, 0, 0, getResources().getString(R.string.search_hint)).setActionView(sv)
                .setIcon(R.drawable.ic_search_grey600).setShowAsAction(
                        MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.
                                SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        MenuItem searchItem = menu.getItem(0);
        // Retrieves the system search manager service
        final SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        // Retrieves the SearchView from the search menu item
        final SearchView searchView = (SearchView) searchItem.getActionView();

        // Assign searchable info to SearchView
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        // Set listeners for SearchView
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String queryText) {

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty())
                {
                    mContactsPBXs = mSaveContactPBXs;
                }
                else
                {
                    mSearchContactsPBXs.clear();
                    for (ContactsPBXBean contactsPBXBean : mSaveContactPBXs)
                    {
                        if (contactsPBXBean.getNumber().contains(newText))
                        {
                            mSearchContactsPBXs.add(contactsPBXBean);
                        }
                    }
                    mContactsPBXs = mSearchContactsPBXs;
                }
                mAdapter.notifyDataSetChanged();
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
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
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        // Otherwise, pass the item to the super implementation for handling, as described in the
        // documentation.
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        menu.setHeaderTitle(getResources().getString(R.string.call_choice));
        //添加菜单项  
        menu.add(0, 1, 0, getResources().getString(R.string.audio_call));
        menu.add(0, 2, 0, getResources().getString(R.string.video_call));
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        int index = info.position;
        if (1 == index)
        {
            mLogger.error("index == 1");
            return false;
        }
        ContactsPBXBean mSelectBean = ((ArrayList<ContactsPBXBean>) mContactsPBXs).get(index - 1);
        final String number = mSelectBean.getNumber();
        final ContactsPBXBean.ContactsPBXType type = mSelectBean.getType();
        final INgnSipService mSipService = ZphoneApplication.getSipService();
        if (type != ContactsPBXBean.ContactsPBXType.SIP)
        {
            Toast.makeText(this, getResources().getString(R.string.not_support_iax),
                    Toast.LENGTH_SHORT).show();

        }
        else if (number.equals(ZphoneApplication.getUserName()))
        {
            Toast.makeText(this, getResources().getString(R.string.not_support_call_me),
                    Toast.LENGTH_SHORT).show();
        }
        else
        {
            switch (item.getItemId()) {
                case 1:
                    new Thread(
                            new Runnable() {

                                @Override
                                public void run() {
                                    // voice call
                                    if (mSipService.isRegistered())
                                    {
                                        ScreenAV.makeCall(number,
                                                NgnMediaType.Audio,
                                                ZphoneApplication.getContext());
                                    }
                                }

                            }

                    ).start();

                    break;
                case 2:

                    new Thread(
                            new Runnable() {

                                @Override
                                public void run() {
                                    // voice call
                                    if (mSipService.isRegistered())
                                    {
                                        ScreenAV.makeCall(number,
                                                NgnMediaType.AudioVideo,
                                                ZphoneApplication.getContext());
                                    }
                                }

                            }

                    ).start();

                    break;

                default:
                    break;
            }
        }
        return super.onContextItemSelected(item);
    }

    class ContactsPBXItemViewHolder
    {
        ImageView photo;
        ImageView status;
        TextView type;
        TextView name;
        TextView number;

        public ContactsPBXItemViewHolder(View v)
        {
            v.setTag(this);
            photo = (ImageView) v.findViewById(R.id.pbx_phto_iv);
            status = (ImageView) v.findViewById(R.id.pbx_status_iv);
            type = (TextView) v.findViewById(R.id.pbx_type_tv);
            name = (TextView) v.findViewById(R.id.pbx_extension_name_tv);
            number = (TextView) v.findViewById(R.id.pbx_extension_number_tv);
        }
    }

    class ContactsPBXAdapter extends BaseAdapter
    {

        @Override
        public int getCount() {

            return mContactsPBXs.size();
        }

        @Override
        public Object getItem(int position) {
            return ((ArrayList<ContactsPBXBean>) mContactsPBXs).get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ContactsPBXItemViewHolder contactsPBXItemViewHolder;
            if (null == convertView)
            {
                convertView = View.inflate(mContext, R.layout.fragment_contacts_pbx_item, null);
                contactsPBXItemViewHolder = new ContactsPBXItemViewHolder(convertView);
            }
            else
            {
                contactsPBXItemViewHolder = (ContactsPBXItemViewHolder) convertView.getTag();
            }
            ContactsPBXBean contactsPBXBean = ((ArrayList<ContactsPBXBean>) mContactsPBXs)
                    .get(position);
            if (null == contactsPBXBean.getName() || contactsPBXBean.getName().isEmpty())
            {
                contactsPBXItemViewHolder.name.setText(contactsPBXBean.getNumber());
            }
            else
            {
                contactsPBXItemViewHolder.name.setText(contactsPBXBean.getName());
            }
            contactsPBXItemViewHolder.number.setText(contactsPBXBean.getNumber());
            contactsPBXItemViewHolder.type.setText(contactsPBXBean.getType().getName());
            if (contactsPBXBean.getStatus() == ContatcsPBXStatus.OK)
            {
                contactsPBXItemViewHolder.status.setImageDrawable((getResources()
                        .getDrawable(R.drawable.ic_status_dot_green)));
            }
            else
            {
                contactsPBXItemViewHolder.status.setImageResource(R.drawable.ic_status_dot_red);
            }
            return convertView;
        }
    }

    class AsteriskAMIShowPeersTask extends AsyncTask<Void, Void, Void>
    {
        private PeerEntryEventListener peerEntryEventListener;
        private String mHost, mUsername, mSecret, mPort;
        private ManagerConnection mManagerConnection;
        private INgnConfigurationService mConfigurationService = Engine.getInstance()
                .getConfigurationService();

        public void loadConfig()
        {
            mHost = ZphoneApplication.getHost();
            mPort = mConfigurationService.getString(ZycooConfigurationEntry.NETWORK_AMI_PORT,
                    ZycooConfigurationEntry.DEFAULT_NETWORK_AMI_PORT);
            mUsername = mConfigurationService.getString(
                    ZycooConfigurationEntry.NETWORK_AMI_USERNAME,
                    ZycooConfigurationEntry.DEFAULT_NETWORK_AMI_USERNAME);
            mSecret = mConfigurationService.getString(ZycooConfigurationEntry.NETWORK_AMI_SECRET,
                    ZycooConfigurationEntry.DEFAULT_NETWORK_AMI_SECRET);
        }

        public boolean init()
        {
            loadConfig();
            this.mManagerConnection = new ManagerConnectionFactory(mHost,
                    Integer.parseInt(mPort), mUsername, mSecret).createManagerConnection();
            if (ManagerConnectionState.DISCONNECTED == mManagerConnection.getState()
                    || ManagerConnectionState.INITIAL == mManagerConnection.getState())
            {
                try
                {
                    mManagerConnection.login();
                    return true;
                } catch (IllegalStateException | IOException | AuthenticationFailedException
                        | TimeoutException e)
                {
                    Log.e(LOG_TAG, "ami login failure");
                    e.printStackTrace();
                    return false;
                }
            }
            return false;
        }

        public AsteriskAMIShowPeersTask()
        {
            peerEntryEventListener = new PeerEntryEventListener();
        }

        @Override
        protected Void doInBackground(Void... params)
        {
            // Simulates a background job.
            if (init())
            {
                mManagerConnection.addEventListener(peerEntryEventListener);
                SipPeersAction sipPeersAction = new SipPeersAction();
                IaxPeerListAction iaxPeerListAction = new IaxPeerListAction();
                try
                {
                    mManagerConnection.sendAction(sipPeersAction);
                    while (!peerEntryEventListener.isComplete())
                    {
                        SystemClock.sleep(500);
                    }
                    peerEntryEventListener.setComplete(false);
                    mManagerConnection.sendAction(iaxPeerListAction);
                    while (!peerEntryEventListener.isComplete())
                    {
                        SystemClock.sleep(500);
                    }
                } catch (IllegalArgumentException | IllegalStateException | IOException
                        | TimeoutException e)
                {
                    Log.e(LOG_TAG, "send action SipPeersAction error");
                    e.printStackTrace();
                }
            }
            else
            {
                Log.e(LOG_TAG, "AMI can't create connect");
            }
            return null;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void result)
        {
            mContactsPBXs = peerEntryEventListener.getCollectionResult();
            mSaveContactPBXs = mContactsPBXs;
            if (null == mContactsPBXs)
            {
                mLoadingPB.setVisibility(View.INVISIBLE);
                mLoadingTv.setText("Loading failure");
            }
            else
            {
                mLoadingPB.setVisibility(View.INVISIBLE);
                mLoadingTv.setVisibility(View.INVISIBLE);

                mListView.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();
                mPullListView.onRefreshComplete();
            }
            super.onPostExecute(result);
        }

        @Override
        protected void onProgressUpdate(Void... values)
        {
            super.onProgressUpdate(values);
        }



        @Override
        protected void onCancelled()
        {
            super.onCancelled();
        }
    }
}
