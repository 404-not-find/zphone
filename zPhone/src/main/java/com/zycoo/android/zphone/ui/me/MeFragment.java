
package com.zycoo.android.zphone.ui.me;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.zycoo.android.zphone.ui.settings.HelpActivity;
import com.zycoo.android.zphone.Engine;
import com.zycoo.android.zphone.ui.IdentitySettingsActivity;
import com.zycoo.android.zphone.R;
import com.zycoo.android.zphone.task.UpdateOnlineStatus;
import com.zycoo.android.zphone.ZphoneApplication;
import com.zycoo.android.zphone.ui.settings.AboutActivity;
import com.zycoo.android.zphone.ui.settings.ScreenSettingsActivity;
import com.zycoo.android.zphone.widget.SuperAwesomeCardFragment;

import org.doubango.ngn.events.NgnRegistrationEventTypes;
import org.doubango.ngn.services.INgnConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MeFragment extends SuperAwesomeCardFragment implements AdapterView.OnItemClickListener,
        UpdateOnlineStatus {
    private BroadcastReceiver mBroadcastReceiver;
    private ListViewItem[] items;
    private MeListAdapter adapter;
    private ListView listView;
    private Logger mLogger = LoggerFactory.getLogger(MeFragment.class);
    private INgnConfigurationService configurationService = getEngine().getConfigurationService();

    public static MeFragment newInstance(int position) {
        MeFragment f = new MeFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(false);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView;
        initData();
        rootView = inflater.inflate(R.layout.fragment_me, null);
        listView = (ListView) rootView.findViewById(R.id.me_lv);
        adapter = new MeListAdapter(getActivity(), items);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        listView.setDivider(null);
        listView.setDividerHeight(0);
        return rootView;
    }

    public void initData() {
        items = new ListViewItem[11];
        items[0] = new ListViewItemGrey(20);
        items[1] = new MeListViewItemAccount();
        items[2] = new ListViewItemGrey(40);
        items[3] = new ListViewItemAvatarWithText(R.string.setting, R.drawable.ic_settings_white, true);
        items[4] = new ListViewItemAvatarWithText(R.string.account, R.drawable.ic_perm_identity_white, false);
        items[5] = new ListViewItemGrey(40);
        items[6] = new ListViewItemAvatarWithText(R.string.help, R.drawable.ic_help_white, true);
        items[7] = new ListViewItemAvatarWithText(R.string.about, R.drawable.ic_info_white, false);
        items[8] = new ListViewItemGrey(40);
        items[9] = new MeListViewItemExit();
        items[10] = new ListViewItemGrey(60);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            //Settings
            case 3:
                startActivity(new Intent(getActivity(), ScreenSettingsActivity.class));
                break;
            //Account
            case 4:
                Intent intent = new Intent(getActivity(), IdentitySettingsActivity.class);

                startActivityForResult(intent,
                        IdentitySettingsActivity.REQUEST_CODE_ACCOUNT_PREFERENCES);
                break;
            //Help
            case 6:
                startActivity(new Intent(getActivity(), HelpActivity.class));
                break;
            //about and update
            case 7:
                startActivity(new Intent(getActivity(), AboutActivity.class));
                break;
            //exit
            case 9:
                final AlertDialog dialog = com.zycoo.android.zphone.widget.CustomDialog.create(
                        getActivity(), R.drawable.ic_exit_to_app_grey600,
                        null, getResources().getString(R.string.sure_exit), getResources()
                                .getString(R.string.yes),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                for (Activity activity : ZphoneApplication.getActivityLists()) {
                                    if (!activity.isFinishing()) {

                                        activity.finish();
                                    }
                                }
                                Engine.getInstance().stop();
                                Intent i = new Intent(Intent.ACTION_MAIN);
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                i.addCategory(Intent.CATEGORY_HOME);
                                startActivity(i);
                                getActivity().finish();
                                //SystemClock.sleep(5000);
                                //android.os.Process.killProcess(android.os.Process.myPid());
                            }

                        }, getResources().getString(R.string.no),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                dialog.show();
                break;
            default:
                break;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IdentitySettingsActivity.REQUEST_CODE_ACCOUNT_PREFERENCES
                && resultCode == Activity.RESULT_OK) {
            adapter.notifyDataSetChanged();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        if (mBroadcastReceiver != null) {
            getActivity().unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
        super.onDestroy();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }

    @Override
    public void statusChange(NgnRegistrationEventTypes type) {
        MeListViewItemAccount account = (MeListViewItemAccount) (items[1]);
        if (null != account.getStatusChangeListener()) {
            account.getStatusChangeListener().statusChange(type);
        } else {
            mLogger.error("getStatusChangeListener is null");
        }
    }
}
