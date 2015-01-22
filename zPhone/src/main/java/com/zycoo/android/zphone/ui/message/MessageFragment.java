package com.zycoo.android.zphone.ui.message;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.diegocarloslima.fgelv.bak.FloatingGroupExpandableListView;
import com.diegocarloslima.fgelv.bak.WrapperExpandableListAdapter;
import com.github.kevinsawicki.http.HttpRequest;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.tqcenglish.vlcdemo.AudioPlayActivity;
import com.zycoo.android.zphone.DatabaseHelper;
import com.zycoo.android.zphone.Engine;
import com.zycoo.android.zphone.ui.MainActivity;
import com.zycoo.android.zphone.NativeService;
import com.zycoo.android.zphone.R;
import com.zycoo.android.zphone.ZphoneApplication;
import com.zycoo.android.zphone.ui.dialpad.ScreenAV;
import com.zycoo.android.zphone.ui.message.MessagerAdapter.VoiceMailBean;
import com.zycoo.android.zphone.utils.Utils;
import com.zycoo.android.zphone.widget.SuperAwesomeCardFragment;
import com.zycoo.android.zphonelib.PullToRefreshExpandableListView;

import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageFragment extends SuperAwesomeCardFragment implements OnChildClickListener,
        OnCreateContextMenuListener {
    public static final int HANDLE_WHAT = 99;
    private static final int UNIQUE_FRAGMENT_GROUP_ID = 0;
    public static final String UPDATE_MESSAGE_FRAGMENT = MessageFragment.class.getName()
            + ".UPDATE_MESSAGE_FRAGMENT";
    private Logger mLogger = LoggerFactory.getLogger(MessageFragment.class);
    private PullToRefreshExpandableListView mPullToRefreshExpandableListView;
    private MessagerAdapter messagerAdapter;
    private WrapperExpandableListAdapter wrapperAdapter;
    private MainActivity mMainActivity;
    private RelativeLayout loadRl;
    private NativeService mNativeService;

    public static MessageFragment newInstance(int position) {
        MessageFragment f = new MessageFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        mMainActivity = (MainActivity) activity;
        super.onAttach(activity);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mNativeService = mMainActivity.getNativeService();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView;
        rootView = inflater.inflate(R.layout.fragment_message, null);
        loadRl = (RelativeLayout) rootView.findViewById(R.id.loading_rl);
        mPullToRefreshExpandableListView = (PullToRefreshExpandableListView) rootView
                .findViewById(R.id.id_messager_lv);
        mPullToRefreshExpandableListView
                .setOnRefreshListener(new OnRefreshListener<FloatingGroupExpandableListView>() {
                    @Override
                    public void onRefresh(
                            PullToRefreshBase<FloatingGroupExpandableListView> refreshView) {
                        // Do work to refresh the list here.
                        new PullRefreshTask().execute();
                    }
                });

        FloatingGroupExpandableListView fglv = (FloatingGroupExpandableListView) mPullToRefreshExpandableListView
                .getRefreshableView();
        fglv.setOnChildClickListener(this);
        fglv.setOnCreateContextMenuListener(this);
        fglv.setOnChildClickListener(this);
        final View header = inflater.inflate(R.layout.fragment_message_list_header, fglv, false);
        mPullToRefreshExpandableListView.getRefreshableView().addHeaderView(header);

       /* final View footer = inflater.inflate(R.layout.fragment_message_list_footer, fglv, false);
        footer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                final Intent intent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://www.zycoo.com/"));
                startActivity(intent);
            }
        });
        fglv.addFooterView(footer);*/

        // Even though the child divider has already been set on the layout
        // file, we have to set it again here
        // This prevents a bug where the background turns to the color of the
        // child divider when the list is expanded
        //fglv.setChildDivider(new ColorDrawable(Color.BLUE)); 
        messagerAdapter = new MessagerAdapter(getActivity());
        new GetDataFromDBTask().execute();
        wrapperAdapter = new WrapperExpandableListAdapter(messagerAdapter);
        fglv.setAdapter(wrapperAdapter);
        for (int i = 0; i < wrapperAdapter.getGroupCount(); i++) {
            fglv.expandGroup(i);
        }
        fglv.setOnScrollFloatingGroupListener(new FloatingGroupExpandableListView.OnScrollFloatingGroupListener() {
            @Override
            public void onScrollFloatingGroupListener(View floatingGroupView, int scrollY) {
                float interpolation = -scrollY / (float) floatingGroupView.getHeight();
                // Changing from RGB(162,201,85) to RGB(255,255,255)
                final int greenToWhiteRed = (int) (162 + 93 * interpolation);
                final int greenToWhiteGreen = (int) (201 + 54 * interpolation);
                final int greenToWhiteBlue = (int) (85 + 170 * interpolation);
                final int greenToWhiteColor = Color.argb(255, greenToWhiteRed, greenToWhiteGreen,
                        greenToWhiteBlue);
                // Changing from RGB(255,255,255) to RGB(0,0,0)
                final int whiteToBlackRed = (int) (255 - 255 * interpolation);
                final int whiteToBlackGreen = (int) (255 - 255 * interpolation);
                final int whiteToBlackBlue = (int) (255 - 255 * interpolation);
                final int whiteToBlackColor = Color.argb(255, whiteToBlackRed, whiteToBlackGreen,
                        whiteToBlackBlue);
                final ImageView image = (ImageView) floatingGroupView
                        .findViewById(R.id.sample_activity_list_group_item_image);
                image.setBackgroundColor(greenToWhiteColor);
                final Drawable imageDrawable = image.getDrawable().mutate();
                imageDrawable.setColorFilter(whiteToBlackColor, PorterDuff.Mode.SRC_ATOP);
                final View background = floatingGroupView
                        .findViewById(R.id.sample_activity_list_group_item_background);
                background.setBackgroundColor(greenToWhiteColor);
                final TextView text = (TextView) floatingGroupView
                        .findViewById(R.id.sample_activity_list_group_item_text);
                text.setTextColor(whiteToBlackColor);
                final ImageView expanded = (ImageView) floatingGroupView
                        .findViewById(R.id.sample_activity_list_group_expanded_image);
                final Drawable expandedDrawable = expanded.getDrawable().mutate();
                expandedDrawable.setColorFilter(whiteToBlackColor, PorterDuff.Mode.SRC_ATOP);
                //final TextView  badge = (TextView) floatingGroupView.findViewById(R.id.menu_badge_text);
            }
        });
        // return super.onCreateView(inflater, container, savedInstanceState);]
        return rootView;
    }


    public MessagerAdapter getMessagerAdapter() {
        return messagerAdapter;
    }

    public WrapperExpandableListAdapter getWrapperAdapter() {
        return wrapperAdapter;
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                int childPosition, long id) {
        switch (groupPosition) {
            case 0:
                new VoiceMailMenuTask().execute(childPosition, 1);
                break;
            case 1:
                new RecordMenuTask().execute(childPosition, 1);
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * @param childPosition
     * @return file_name, wd, inbox_old, extension
     */
    public String[] getVoiceMailFileNameAndWd(int childPosition) {
        //voiceMail
        VoiceMailBean voiceMailBean = messagerAdapter.getVoiceMails().get(childPosition);
        int wd = 0;
        String file_name = null;
        String inbox_or_old = null;
        //query db
        SQLiteDatabase rSQLiteDatabase = new DatabaseHelper(getActivity()
                .getApplicationContext(),
                "SoftPhone.db", null, 1)
                .getReadableDatabase();
        Cursor cursor = rSQLiteDatabase
                .rawQuery(
                        "select WD, FILE_NAME, TYPE from INFOS where CALLERID=? AND ORIGTIME=? order by origtime desc",
                        new String[]{
                                voiceMailBean.getCallerID(),
                                voiceMailBean.getDate()
                        });
        if (cursor.moveToFirst()) {
            wd = cursor.getInt(0);
            file_name = cursor.getString(1);
            inbox_or_old = cursor.getString(2);
        }
        cursor.close();
        String[] result = new String[4];
        result[0] = file_name;
        result[1] = Integer.toString(wd);
        result[2] = inbox_or_old;
        result[3] = voiceMailBean.getExtension();
        return result;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {

        ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
        int type = ExpandableListView
                .getPackedPositionType(info.packedPosition);
        int group = ExpandableListView
                .getPackedPositionGroup(info.packedPosition);
        /*int child = ExpandableListView
                .getPackedPositionChild(info.packedPosition);*/
        menu.clear();
        menu.setHeaderTitle(getResources().getString(R.string.choice));
        if (1 == type) {
            switch (group) {
                case 0:
                    //添加菜单项  
                    menu.add(UNIQUE_FRAGMENT_GROUP_ID, 1, 0,
                            getResources().getString(R.string.voicemail_play));
                    menu.add(UNIQUE_FRAGMENT_GROUP_ID, 2, 0,
                            getResources().getString(R.string.voicemail_call));
                    menu.add(UNIQUE_FRAGMENT_GROUP_ID, 3, 0,
                            getResources().getString(R.string.voicemail_make_as_new));
                    menu.add(UNIQUE_FRAGMENT_GROUP_ID, 4, 0,
                            getResources().getString(R.string.voicemail_make_as_read));
                    menu.add(UNIQUE_FRAGMENT_GROUP_ID, 5, 0,
                            getResources().getString(R.string.voicemail_delete));
                    break;
                case 1:
                    //添加菜单项  
                    menu.add(UNIQUE_FRAGMENT_GROUP_ID, 1, 0,
                            getResources().getString(R.string.voicemail_play));
                    //TODO 删除需要管理员权限
                    //menu.add(UNIQUE_FRAGMENT_GROUP_ID, 2, 0,
                    //      getResources().getString(R.string.voicemail_delete));
                    break;
                default:
                    break;
            }
        }

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getGroupId() != UNIQUE_FRAGMENT_GROUP_ID) {
            return false;
        }
        ContextMenuInfo menuInfo = item.getMenuInfo();
        ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
        int type = ExpandableListView
                .getPackedPositionType(info.packedPosition);
        int group = ExpandableListView
                .getPackedPositionGroup(info.packedPosition);
        int child = ExpandableListView
                .getPackedPositionChild(info.packedPosition);
        switch (group) {
            case 0:
                new VoiceMailMenuTask().execute(child, item.getItemId());
                break;
            case 1:
                new RecordMenuTask().execute(child, item.getItemId());
                break;
            default:
                break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if (Utils.isPro(getActivity())) {
            if (null != mNativeService && mNativeService.isConnected()) {
                getSherlockActivity().getSupportMenuInflater().inflate(R.menu.activity_mqtt, menu);
            } else {
                getSherlockActivity().getSupportMenuInflater().inflate(
                        R.menu.activity_mqtt_disconnected, menu);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
        switch (item.getItemId()) {
            case R.id.disconnect:
                mNativeService.disconnect();
                break;
            case R.id.connect:
                mNativeService.connectAction();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setLoadRlVisible(boolean visible) {
        if (visible) {
            loadRl.setVisibility(View.VISIBLE);
        } else {
            loadRl.setVisibility(View.INVISIBLE);
        }
    }

    private class PullRefreshTask extends GetDataFromDBTask {
        boolean pullResult = true;

        @Override
        protected void onPostExecute(Void result) {
            if (!pullResult) {
                Toast.makeText(ZphoneApplication.getContext(), R.string.connect_server_failure, Toast.LENGTH_SHORT).show();
            }
            mPullToRefreshExpandableListView.onRefreshComplete();
            super.onPostExecute(result);
        }

        @Override
        protected Void doInBackground(Void... params) {

            /*if (Utils.isPro(getActivity())) {
                if (!mNativeService.isConnected()) {
                    mNativeService.connectAction();
                }
                mNativeService.subscribe();
            }*/
            try {
                new UpdateMessage().Get();
            } catch (RuntimeException e) {
                pullResult = false;
                mLogger.error("pullrefresh failure " + e.getMessage());
            }
            //new UpdateMessage().Get();

            return super.doInBackground(params);
        }
    }

    private class GetDataFromDBTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPostExecute(Void result) {
            messagerAdapter.notifyDataSetInvalidated();
            //setLoadRlVisible(false);
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            //setLoadRlVisible(true);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            messagerAdapter.getVoiceMailsFromDB();
            messagerAdapter.getMonitorsFromDB();
            return null;
        }
    }

    private class RecordMenuTask extends AsyncTask<Integer, Void, Boolean> {
        @Override
        protected void onPostExecute(Boolean aBoolean) {
            messagerAdapter.notifyDataSetInvalidated();
            super.onPostExecute(aBoolean);
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            //monitor
            MonitorBean monitorBean = messagerAdapter.getMonitors().get(params[0]);
            switch (params[1]) {
                //play
                case 1:
                   /* Intent it = new Intent(Intent.ACTION_VIEW);*/
                    Intent it = new Intent(getActivity(), AudioPlayActivity.class);
                    ///var/spool/asterisk/monitor/recording/804
                    String downloadUrl = String
                            .format("http://%s:4242/download_monitor_file?type=%s&extension=%s&file_name=%s",
                                    ZphoneApplication.getHost(),
                                    monitorBean.getType(),
                                    ZphoneApplication.getUserName(),
                                    monitorBean.getFile_name()
                            );
                    /*it.setDataAndType(
                            Uri.parse(downloadUrl), "audio/MP3");*/
                    it.putExtra("path", downloadUrl);
                    startActivity(it);
                    break;
                //remove_from_call_log
                case 2:
                    SQLiteDatabase wSQLiteDatabase = new DatabaseHelper(getActivity()
                            .getApplicationContext(),
                            "SoftPhone.db", null, 1)
                            .getWritableDatabase();
                    wSQLiteDatabase.execSQL("delete  from MONITORS where FILE_NAME=\'"
                            + monitorBean.getFile_name() + "\'");
                    wSQLiteDatabase.close();
                    final String deleteUrl = String
                            .format("http://%s:4242/delete_monitor_file?type=%s&extension=%s&file_name=%s",
                                    ZphoneApplication.getHost(),
                                    monitorBean.getType(),
                                    ZphoneApplication.getUserName(),
                                    monitorBean.getFile_name()
                            );
                    messagerAdapter.getMonitors().remove(params[0].intValue());
                    HttpRequest request = HttpRequest.get(deleteUrl);
                    try {
                        request.ok();
                    } catch (RuntimeException e) {
                        mLogger.error("error " + e.getMessage());
                    }
                    break;
            }
            return null;
        }
    }

    private class VoiceMailMenuTask extends AsyncTask<Integer, Void, Boolean> {
        @Override
        protected void onPostExecute(Boolean result) {
            messagerAdapter.notifyDataSetInvalidated();
            if (!result) {
                //Toast.makeText(getActivity(), R.string.operation_failure, Toast.LENGTH_SHORT).show();
            } else {
                //Toast.makeText(getActivity(), R.string.operation_successful, Toast.LENGTH_SHORT).show();
            }
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            boolean result = true;
            final String server = Engine.getInstance().getConfigurationService()
                    .getString(NgnConfigurationEntry.NETWORK_PCSCF_HOST, "");
            String[] strs = getVoiceMailFileNameAndWd(params[0]);
            if (2 == params.length) {
                String wd = strs[1];
                String file_name = strs[0];
                String requestUrl = null;
                switch (params[1]) {
                    case 1:
                        //Intent it = new Intent(Intent.ACTION_VIEW);
                        Intent it = new Intent(getActivity(), AudioPlayActivity.class);
                        String wav_file_name = file_name.substring(0, file_name.length() - 3)
                                + "wav";
                        /*it.setDataAndType(
                                Uri.parse("http://" + server + ":4242/download_voicemail?wd="
                                        + wd + "&file_name=" + wav_file_name), "audio/MP3");*/
                        String download_url = String.format("http://%s:4242/download_voicemail?wd=%s&file_name=%s", server, wd, wav_file_name);
                        it.putExtra("path", download_url);
                        startActivity(it);
                        break;
                    case 2:
                        //voiceMail
                        final String number = strs[3];
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
                    //mark as new
                    case 3:
                        if ("Old".equals(strs[2])) {
                            requestUrl = "http://" + server + ":4242/move_voicemail_file?wd="
                                    + wd + "&file_name="
                                    + file_name.substring(0, file_name.length() - 4);

                        }
                        break;
                    //mark as old
                    case 4:
                        if ("INBOX".equals(strs[2])) {
                            requestUrl = "http://" + server + ":4242/move_voicemail_file?wd="
                                    + wd + "&file_name="
                                    + file_name.substring(0, file_name.length() - 4);
                        }
                        break;

                    case 5:
                        messagerAdapter.getVoiceMails().remove(params[0].intValue());
                        requestUrl = "http://" + server + ":4242/delete_voicemail_file?wd="
                                + wd + "&file_name="
                                + file_name.substring(0, file_name.length() - 4);

                        break;
                    default:
                        break;
                }
                if (null != requestUrl) {
                    HttpRequest request = HttpRequest.get(requestUrl);
                    try {
                        request.ok();
                    } catch (RuntimeException e) {
                        mLogger.error("error " + e.getMessage());
                        result = false;
                    }

                    /*if (request.ok()){
                        mLogger.debug("remove_from_call_log voicemail successful");

                        switch (params[1]) {
                            case 5:
                                messagerAdapter.getVoiceMails().remove(params[0]);
                                messagerAdapter.notifyDataSetInvalidated();
                                break;
                            default:
                                break;
                        }
                        result = false;
                    } else {
                        mLogger.debug("remove_from_call_log voicemail failure  code : " + request.code());
                    }*/
                }
            }
            return result;
        }
    }
}
