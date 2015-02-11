
package com.zycoo.android.zphone.ui.contacts;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.actionbarsherlock.view.MenuItem;
import com.hp.views.PinnedSectionListView.PinnedSectionListAdapter;
import com.zycoo.android.zphone.DatabaseHelper;
import com.zycoo.android.zphone.R;
import com.zycoo.android.zphone.ZphoneApplication;
import com.zycoo.android.zphone.ui.dialpad.ScreenAV;
import com.zycoo.android.zphone.utils.Utils;
import com.zycoo.android.zphone.widget.SuperAwesomeCardFragment;

import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.utils.NgnStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class ContactListFragment extends SuperAwesomeCardFragment implements OnClickListener, AdapterView.OnItemClickListener {
    private Logger mLogger = LoggerFactory.getLogger(ContactListFragment.class);
    private static final String DOWN_DATA_DB = "/download_file?path=/etc/asterisk/sysconf/database/data.db";
    private static final int UNIQUE_FRAGMENT_GROUP_ID = 2;
    private ListView mListView;
    private BaseAdapter mAdapter;
    private LayoutInflater inflater;
    private boolean addPadding = false;
    private TextView footer;
    public RelativeLayout mLoadingRL;

    public static ContactListFragment newInstance(int position) {
        ContactListFragment f = new ContactListFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        SimpleAdapter adapter = (SimpleAdapter) mAdapter;
        if (0 == position || position == mAdapter.getCount() + 1) {
            return;
        }
        int section = ((SeparatedListAdapter) adapter).sectionPosition(position - 1);
        final PBXPhoneBookBean bean = (PBXPhoneBookBean) mListView.getAdapter().getItem(
                position - section);
        if (null != bean) {
            new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            // voice call
                            if (mSipService.isRegistered()) {
                                ScreenAV.makeCall(bean.phoneNumber,
                                        NgnMediaType.Audio,
                                        ZphoneApplication.getContext());
                            }
                        }

                    }

            ).start();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }

    class SimpleAdapter extends SeparatedListAdapter implements PinnedSectionListAdapter,
            Observer {
        private final Context mContext;
        private final Handler mHandler;
        //private final NgnObservableList<NgnContact> mContactsList;
        private List<PBXPhoneBookBean> mPBXContactsList;

        /* private final int[] COLORS = new int[] {
                 R.color.green_light, R.color.orange_light,
                 R.color.light_blue, R.color.light_red
         };*/

        public SimpleAdapter(Context context) {
            super(context);
            mContext = context;
            mHandler = new Handler();
            //mContactsList = Engine.getInstance().getContactService().getObservableContacts();
            //mContactsList.addObserver(this);
            mPBXContactsList = new ArrayList<ContactListFragment.PBXPhoneBookBean>();


        }

        public void updateSections() {
            clearSections();
            synchronized (mPBXContactsList) {
                SQLiteDatabase rSQLiteDatabase = new DatabaseHelper(
                        mContext.getApplicationContext(),
                        "data.db", null, 1)
                        .getReadableDatabase();
                //CREATE TABLE phonebook(NAME varchar(255) NOT NULL,PHONE_NUMBER varchar(64) NOT NULL UNIQUE,SPEED_DIAL_NUMBER varchar(32),BELONGS varchar(16));
                Cursor cursor = rSQLiteDatabase
                        .rawQuery(
                                "select NAME, PHONE_NUMBER, SPEED_DIAL_NUMBER, BELONGS from phonebook order by NAME desc",
                                null);
                mPBXContactsList.clear();
                if (cursor.moveToFirst()) {
                    do {
                        PBXPhoneBookBean pbxBean = new PBXPhoneBookBean();
                        pbxBean.name = cursor.getString(0);
                        pbxBean.phoneNumber = cursor.getString(1);
                        pbxBean.speedDialNumber = cursor.getString(2);
                        pbxBean.belongs = cursor.getString(3);
                        mPBXContactsList.add(pbxBean);
                    } while (cursor.moveToNext());
                }
                cursor.close();
                rSQLiteDatabase.close();

                String lastGroup = "$", displayName;
                ContactsAdapter contactsAdapter = null;

                for (PBXPhoneBookBean contact : mPBXContactsList) {
                    // 获取联系人名
                    displayName = contact.name;
                    // 排除空联系人
                    if (NgnStringUtils.isNullOrEmpty(displayName)) {
                        continue;
                    }
                    // 联系人名第一个字符
                    final String group = displayName.substring(0, 1).toUpperCase();
                    // 当第一个字符不存在时添加section
                    if (!group.equalsIgnoreCase(lastGroup)) {
                        lastGroup = group;
                        contactsAdapter = new ContactsAdapter(mContext, lastGroup);
                        addSection(lastGroup, contactsAdapter);
                    }
                    // 添加联系人到指定section
                    if (contactsAdapter != null) {
                        contactsAdapter.addContact(contact);
                    }
                }
            }
        }

        @Override
        public int getViewTypeCount() {
            // SECITON, ITEM
            return 2;
        }

        ;

        protected void prepareSections(int sectionsNumber) {
        }

        protected void onSectionAdded(Item section, int sectionPosition) {
        }

        @Override
        public Object getItem(int position) {
            return mPBXContactsList.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            if (getItemViewType(position) == Item.SECTION) {
                // view.setOnClickListener(PinnedSectionListActivity.this);
                TextView tv = (TextView) view.findViewById(android.R.id.text1);
                /*tv.setTextColor(Color.DKGRAY);
                tv.setTextSize(20);*/
                //view.setTag("" + position);
                /*view.setBackgroundColor(parent.getResources().getColor(
                        COLORS[sectionPosition(position) % COLORS.length]));*/

            }
            return view;
        }

        @Override
        public boolean isItemViewTypePinned(int viewType) {
            return viewType == Item.SECTION;
        }

        @Override
        public void update(Observable observable, Object data) {

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mLogger.debug("LOG_TAG", "new runable to notifyDataSetChanged");
                    updateSections();
                    notifyDataSetChanged();

                }
            });
        }

        @Override
        protected void finalize() throws Throwable {
            //Engine.getInstance().getContactService().getObservableContacts().deleteObserver(this);
            super.finalize();
        }
    }

    /**
     * ScreenTabContactsAdapter
     */
    static class ContactsAdapter extends BaseAdapter {
        private final LayoutInflater mInflater;

        private final Context mContext;
        private List<PBXPhoneBookBean> mContacts;
        private final String mSectionText;

        private ContactsAdapter(Context context, String sectionText) {
            mContext = context;
            mSectionText = sectionText;
            mInflater = LayoutInflater.from(mContext);
        }

        public String getSectionText() {
            return mSectionText;
        }

        public void addContact(PBXPhoneBookBean contact) {
            if (mContacts == null) {
                mContacts = new ArrayList<PBXPhoneBookBean>();
            }
            mContacts.add(contact);
        }

        @Override
        public int getCount() {
            return mContacts == null ? 0 : mContacts.size();
        }

        @Override
        public Object getItem(int position) {
            if (mContacts != null && mContacts.size() > position) {
                return mContacts.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;

            if (view == null) {
                view = mInflater.inflate(R.layout.fragment_contact_remote_list_item, parent, false);
            }
            final PBXPhoneBookBean contact = (PBXPhoneBookBean) getItem(position);

            if (contact != null) {
                final ImageView ivAvatar = (ImageView) view
                        .findViewById(R.id.screen_tab_contacts_item_imageView_avatar);
                if (ivAvatar != null) {
                    final TextView tvDisplayName = (TextView) view
                            .findViewById(R.id.screen_tab_contacts_item_textView_displayname);
                    tvDisplayName.setText(contact.name);
                    /*
                    final Bitmap avatar = contact.getPhoto();
                    if (avatar == null) {
                        ivAvatar.setImageResource(R.drawable.ic_action_user);
                    }
                    else {
                        ivAvatar.setImageBitmap(NgnGraphicsUtils.getResizedBitmap(avatar,
                                NgnGraphicsUtils.getSizeInPixel(48),
                                NgnGraphicsUtils.getSizeInPixel(48)));
                    }
                    */
                }
            }

            return view;
        }
    }

    class FastScrollAdapter extends SimpleAdapter implements SectionIndexer {
        private Item[] sections;

        public FastScrollAdapter(Context context, int resource, int textViewResourceId) {
            super(context);
        }

        @Override
        protected void prepareSections(int sectionsNumber) {
            sections = new Item[sectionsNumber];
        }

        @Override
        protected void onSectionAdded(Item section, int sectionPosition) {
            sections[sectionPosition] = section;
        }

        @Override
        public Item[] getSections() {
            return sections;
        }

        @Override
        public int getPositionForSection(int section) {
            if (section >= sections.length) {
                section = sections.length - 1;
            }
            return sections[section].listPosition;
        }

        @Override
        public int getSectionForPosition(int position) {
            if (position >= getCount()) {
                position = getCount() - 1;
            }
            return getItemViewType(position);
            // return getItem(position).sectionPosition;
        }
    }

    @Override
    public View onCreateView(LayoutInflater mInflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = mInflater.inflate(R.layout.fragment_contacts_list, null);
        inflater = LayoutInflater.from(getActivity());
        mListView = (ListView) view.findViewById(R.id.contact_list);
        mLoadingRL = (RelativeLayout) view.findViewById(R.id.loading_rl);
        mAdapter = new SimpleAdapter(getActivity());
        addHeader();
        mListView.setAdapter(mAdapter);
        addFooter();
        initializePadding();
        mListView.setOnCreateContextMenuListener(this);
        mListView.setOnItemClickListener(this);
        //http://www.quora.com/Why-does-the-scroll-bar-change-its-length-during-scrolling
        //mListView.setSmoothScrollbarEnabled(false);

        new GetDataFromDBTask().execute(false);
        return view;
    }

    /*
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
     
        if (position == 0 || mListView.getCount() == position + 1)
        {
            //pbx activity
            //Intent intent = new Intent(getActivity(), ContactsPBXActivity.class);
            //mLogger.debug("start pbx activity");
            //startActivity(intent);
            return;

        }
        else
        {
            PBXPhoneBookBean bean = (PBXPhoneBookBean) mListView.getAdapter().getItem(position);
            Resources resources = Engine.getInstance().getMainActivity().getResources();
            //跳转到拨号盘
            new UndoBar.Builder(getActivity())
                    .setMessage(bean.name + resources.getString(R.string.remove_from_call_log))
                    .setListener(new Listener() {
                        @Override
                        public void onUndo(Parcelable token) {
                            Toast.makeText(
                                    getActivity(),
                                    Engine.getInstance().getMainActivity().getResources()
                                            .getString(R.string.cancel_delete), Toast.LENGTH_SHORT)
                                    .show();
                        }

                        @Override
                        public void onHide() {

                        }
                    })
                    .show();
            Toast.makeText(getActivity(), "Item " + position + ": " + bean.name,
                    Toast.LENGTH_SHORT)
                    .show();
        }
        
       

    }
     */

    private void initializePadding() {
        float density = getResources().getDisplayMetrics().density;
        int padding = addPadding ? (int) (16 * density) : 0;
        mListView.setPadding(padding, padding, padding, padding);
    }

    /**
     * 添加Foot或Header
     *
     * @author tqcenglish
     */

    private void addHeader() {
        LinearLayout linearLayout = (LinearLayout) inflater.inflate(
                R.layout.fragment_contact_list_header, mListView, false);
        TextView contact_head_one = (TextView) linearLayout.findViewById(R.id.contact_head_one_tv);
        contact_head_one.setText(ZphoneApplication.getAppResources().getString(
                R.string.contacts_remote));
        mListView.addHeaderView(linearLayout);
    }

    private void addFooter() {
        LinearLayout linearLayout = (LinearLayout) inflater.inflate(
                R.layout.fragment_contact_list_footer,
                mListView,
                false);
        footer = (TextView) linearLayout.findViewById(R.id.contact_footer_one_tv);

        footer.setText(mAdapter.getCount() - ((SeparatedListAdapter) mAdapter).getSectionsCount()
                + " " + ZphoneApplication.getAppResources().getString(R.string.contacts_pbx_footer));
        mListView.addFooterView(linearLayout);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case ContactsContainerFragment.UNIQUE_MENU_RELOAD_ID:
                new GetDataFromDBTask().execute(true);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
        }
    }

    public void downloadDataDB() {
        String dataDbPath = String.format("/data/data/%s/databases/data.db", ZphoneApplication.getContext()
                .getPackageName());
        int bytesRead = 0;
        byte[] buffer = new byte[1024];
        try {
            InputStream in = getEngine().getHttpClientService().getBinary(
                    ZphoneApplication.getHttpSite(DOWN_DATA_DB));
            if (null != in) {
                OutputStream os = new FileOutputStream(new File(dataDbPath));
                while ((bytesRead = in.read(buffer, 0, 1024)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.close();
                in.close();
            } else {
                mLogger.error("get db inputstream is null");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public class PBXPhoneBookBean {
        String name;
        String phoneNumber;
        String speedDialNumber;
        String belongs;
    }

    private class GetDataFromDBTask extends AsyncTask<Boolean, Void, String[]> {
        @Override
        protected void onPostExecute(String[] result) {
            if (null != footer) {
                footer.setText(mAdapter.getCount() - ((SeparatedListAdapter) mAdapter).getSectionsCount()
                        + " " + ZphoneApplication.getAppResources().getString(R.string.contacts_pbx_footer));
            }
            mAdapter.notifyDataSetInvalidated();
            super.onPostExecute(result);
        }

        @Override
        protected String[] doInBackground(Boolean... params) {
            if (getEngine().getConfigurationService().getBoolean("first_load_remote_contacts", true) || params[0]) {
                downloadDataDB();
                getEngine().getConfigurationService().putBoolean("first_load_remote_contacts", false);
                getEngine().getConfigurationService().commit();
            }
            ((SimpleAdapter) mAdapter).updateSections();
            return null;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        int index = info.position;
        if (0 != index) {
            menu.setHeaderTitle(getResources().getString(R.string.call_choice));
            //添加菜单项  
            menu.add(UNIQUE_FRAGMENT_GROUP_ID, 1, 0, getResources().getString(R.string.audio_call));
            if (Utils.isPro(getActivity())) {
                menu.add(UNIQUE_FRAGMENT_GROUP_ID, 2, 0, getResources().getString(R.string.video_call));
            }
        }

        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {

        if (item.getGroupId() != UNIQUE_FRAGMENT_GROUP_ID) {
            return false;
        }
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        int index = info.position;
        SimpleAdapter adapter = (SimpleAdapter) mAdapter;
        int section = ((SeparatedListAdapter) adapter).sectionPosition(index - 1);
        PBXPhoneBookBean bean = (PBXPhoneBookBean) mListView.getAdapter().getItem(
                index - section);
        if (null != bean) {
            final String number = bean.phoneNumber;
            switch (item.getItemId()) {
                case 1:
                    new Thread(
                            new Runnable() {

                                @Override
                                public void run() {
                                    // voice call
                                    if (mSipService.isRegistered()) {
                                        ScreenAV.makeCall(number,
                                                NgnMediaType.Audio,
                                                ZphoneApplication.getContext());
                                    }
                                }

                            }

                    ).start();
                    break;
                case 2:
                    break;
                default:
                    break;
            }

        } else {
            mLogger.error("is't valid PBXPhoneBookBean");
        }

        return true;
    }
}
