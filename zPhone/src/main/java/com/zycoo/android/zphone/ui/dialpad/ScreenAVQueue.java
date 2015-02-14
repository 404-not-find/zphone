/* Copyright (C) 2010-2011, Mamadou Diop.
*  Copyright (C) 2011, Doubango Telecom.
*
* Contact: Mamadou Diop <diopmamadou(at)doubango(dot)org>
*	
* This file is part of imsdroid Project (http://code.google.com/p/imsdroid)
*
* imsdroid is free software: you can redistribute it and/or modify it under the terms of 
* the GNU General Public License as published by the Free Software Foundation, either version 3 
* of the License, or (at your option) any later version.
*	
* imsdroid is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
* without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
* See the GNU General Public License for more details.
*	
* You should have received a copy of the GNU General Public License along 
* with this program; if not, write to the Free Software Foundation, Inc., 
* 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/

package com.zycoo.android.zphone.ui.dialpad;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.zycoo.android.zphone.R;
import com.zycoo.android.zphone.ui.settings.BaseScreen;
import com.zycoo.android.zphone.ZycooConfigurationEntry;

import org.doubango.ngn.sip.NgnAVSession;
import org.doubango.ngn.utils.NgnObservableHashMap;
import org.doubango.ngn.utils.NgnStringUtils;

import java.util.Map;
import java.util.Observable;
import java.util.Observer;

public class ScreenAVQueue extends BaseScreen {
    private static final String TAG = ScreenAVQueue.class.getCanonicalName();

    private final static int MENU_OPEN_CALL = 0;
    private final static int MENU_HANGUP_CALL = 1;
    private final static int MENU_HANGUP_ALLCALLS = 2;

    private ListView mListView;
    private ScreenAVQueueAdapter mAdapter;

    public ScreenAVQueue() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_av_queue);

        mListView = (ListView) findViewById(R.id.screen_av_queue_listView);
        registerForContextMenu(mListView);
        mAdapter = new ScreenAVQueueAdapter(this);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final NgnAVSession session = (NgnAVSession) mAdapter.getItem(position);
                if (session != null) {
                    resumeAVSession(session);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_HANGUP_ALLCALLS, 0, "Hang Up all calls").setIcon(
                R.drawable.ic_call_end_grey600);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case ScreenAVQueue.MENU_HANGUP_ALLCALLS:
                final NgnObservableHashMap<Long, NgnAVSession> sessions = NgnAVSession
                        .getSessions();
                NgnAVSession session;
                for (Map.Entry<Long, NgnAVSession> entry : sessions.entrySet()) {
                    session = entry.getValue();
                    if (session.isActive()) {
                        session.hangUpCall();
                    }
                }
                break;
        }
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        menu.add(0, MENU_OPEN_CALL, Menu.NONE, "Open");
        menu.add(0, MENU_HANGUP_CALL, Menu.NONE, "Hang Up");
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        final NgnAVSession session;
        final int location = ((AdapterContextMenuInfo) item.getMenuInfo()).position;
        if ((session = (NgnAVSession) mAdapter.getItem(location)) == null) {
            return super.onContextItemSelected(item);
        }

        switch (item.getItemId()) {
            case MENU_OPEN_CALL:
                resumeAVSession(session);
                return true;
            case ScreenAVQueue.MENU_HANGUP_CALL:
                session.hangUpCall();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    /*
    @Override
    public boolean back(){
    	boolean ret =  mScreenService.back();
    	if(ret){
    		mScreenService.destroy(getId());
    	}
    	return ret;
    }*/

    private void resumeAVSession(NgnAVSession session) {
        // Hold the active call
        final NgnAVSession activeSession = NgnAVSession.getFirstActiveCallAndNot(session.getId());
        if (activeSession != null) {
            activeSession.holdCall();
        }
        // Resume the selected call and display it to the screen
        Intent intent = new Intent(this, ScreenAV.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //Bundle bundle = new Bundle();
        intent.putExtra(ZycooConfigurationEntry.SESSION_ID, session.getId());
        this.startActivity(intent);
        if (session.isLocalHeld()) {
            session.resumeCall();
        }
    }

    //
    // ScreenAVQueueAdapter
    //
    private class ScreenAVQueueAdapter extends BaseAdapter implements Observer {
        private NgnObservableHashMap<Long, NgnAVSession> mAVSessions;
        private final LayoutInflater mInflater;
        private final Handler mHandler;

        ScreenAVQueueAdapter(Context context) {
            mHandler = new Handler();
            mInflater = LayoutInflater.from(context);
            mAVSessions = NgnAVSession.getSessions();
            mAVSessions.addObserver(this);
        }

        @Override
        public int getCount() {
            return mAVSessions.size();
        }

        @Override
        public Object getItem(int position) {
            return mAVSessions.getAt(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public void update(Observable observable, Object data) {
            mAVSessions = NgnAVSession.getSessions();
            if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
                notifyDataSetChanged();
            } else {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            NgnAVSession session;

            if (view == null) {
                view = mInflater.inflate(R.layout.screen_av_queue_item, null);
            }
            session = (NgnAVSession) getItem(position);

            if (session != null) {
                final ImageView imageView = (ImageView) view
                        .findViewById(R.id.screen_av_queue_item_imageView);
                final TextView tvRemoteParty = (TextView) view
                        .findViewById(R.id.screen_av_queue_item_textView_remote);
                final TextView tvInfo = (TextView) view
                        .findViewById(R.id.screen_av_queue_item_textView_info);

                if (session.isLocalHeld() || session.isRemoteHeld()) {
                    imageView.setImageResource(R.drawable.ic_phone_paused_black);
                    tvInfo.setText(getResources().getString(R.string.string_call_hold));
                } else {
                    imageView.setImageResource(R.drawable.ic_phone_in_talk_grey600);
                    imageView.setColorFilter(Color.rgb(3, 169, 237));
                    switch (session.getState()) {
                        case INCOMING:
                            tvInfo.setText(getResources().getString(R.string.string_in_coming));
                            break;
                        case INPROGRESS:
                            tvInfo.setText(getResources().getString(R.string.string_in_progress));
                            break;
                        case INCALL:
                        default:
                            tvInfo.setText(getResources().getString(R.string.string_incall));
                            break;
                        case TERMINATED:
                            tvInfo.setText(getResources().getString(R.string.string_call_terminated));
                            break;
                    }
                }

                final String remoteParty = session.getRemotePartyDisplayName();
                if (remoteParty != null) {
                    tvRemoteParty.setText(remoteParty);
                } else {
                    tvRemoteParty.setText(NgnStringUtils.nullValue());
                }
            }

            return view;
        }
    }
}
