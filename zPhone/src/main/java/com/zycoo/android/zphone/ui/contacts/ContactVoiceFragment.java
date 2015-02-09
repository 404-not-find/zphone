package com.zycoo.android.zphone.ui.contacts;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.tqcenglish.vlcdemo.AudioPlayActivity;
import com.zycoo.android.zphone.R;
import com.zycoo.android.zphone.ZphoneApplication;
import com.zycoo.android.zphone.widget.SuperAwesomeCardFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tqcenglish on 15-1-29.
 */
public class ContactVoiceFragment extends SuperAwesomeCardFragment implements AdapterView.OnItemClickListener {

    private final String basePath = Environment.getExternalStorageDirectory().getPath() + "/zycoo/voice_mail/" + ZphoneApplication.getHost() + "/";
    private String mContactDisplayName;
    private String mNumber;
    private ListView mListView;
    private List<VoiceItem> mVoiceItems;
    private BaseAdapter mAdapter;

    public static ContactVoiceFragment newInstance(int position) {
        ContactVoiceFragment f = new ContactVoiceFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_contact_detail_voice, container, false);
        mListView = (ListView) view.findViewById(android.R.id.list);
        mVoiceItems = new ArrayList<>();
        mAdapter = new VoiceAdapter();
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        return view;

    }

    @Override
    public void onResume() {
        super.onResume();
        if (null != mNumber) {
            new checkVoiceMailTask().execute(basePath + mNumber);
        }
    }

    public void setContact(String displayName) {
        mContactDisplayName = displayName;
        mNumber = getPhoneNumber(mContactDisplayName, getSherlockActivity());
        if (null != mNumber) {
            new checkVoiceMailTask().execute(basePath + mNumber);
        }
    }

    public String getPhoneNumber(String name, Context context) {
        String ret = null;
        String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " like'%" + name + "%'";
        String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
        Cursor c = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection, selection, null, null);
        if (c.moveToFirst()) {
            ret = c.getString(0);
        }
        c.close();
        if (ret == null)
            ret = "Unsaved";
        return ret;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent it = new Intent(getActivity(), AudioPlayActivity.class);
        it.putExtra("path", mVoiceItems.get(position).path);
        startActivity(it);
    }

    class checkVoiceMailTask extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            String path = params[0];
            mVoiceItems.clear();
            mAdapter.notifyDataSetChanged();
            File file = new File(path);
            File[] voices = file.listFiles();
            for (File voice : voices) {
                VoiceItem item = new VoiceItem();
                item.path = voice.getAbsolutePath();
                item.name = voice.getName();
                String[] strs = voice.getName().split("\\.");
                item.date = strs[0];
                item.type = getResources().getString(R.string.voice_mail);
                mVoiceItems.add(item);
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            mAdapter.notifyDataSetChanged();
            super.onPostExecute(aBoolean);
        }
    }

    class VoiceAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mVoiceItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mVoiceItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            VoiceItemViewHolder voiceItemViewHolder;
            if (null != convertView) {
                voiceItemViewHolder = (VoiceItemViewHolder) convertView.getTag();
            } else {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_contact_detail_voice_item, parent, false);
                voiceItemViewHolder = new VoiceItemViewHolder(convertView);
            }
            voiceItemViewHolder.typeTv.setText(mVoiceItems.get(position).type);
            voiceItemViewHolder.dateTv.setReferenceTime(Long.parseLong(mVoiceItems.get(position).date) * 1000);
            voiceItemViewHolder.nameTv.setText(mVoiceItems.get(position).name);
            voiceItemViewHolder.mDeleteIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //delete file
                    new File(mVoiceItems.get(position).path).delete();
                    mVoiceItems.remove(position);
                    notifyDataSetChanged();
                }
            });
            return convertView;
        }
    }

    class VoiceItem {
        String path;
        String type;
        String date;
        String name;
    }

    class VoiceItemViewHolder {
        RelativeTimeTextView dateTv;
        TextView typeTv;
        TextView nameTv;
        ImageView mDeleteIv;

        public VoiceItemViewHolder(View view) {
            view.setTag(this);
            dateTv = (RelativeTimeTextView) view.findViewById(R.id.time_date);
            nameTv = (TextView) view.findViewById(R.id.item_name);
            typeTv = (TextView) view.findViewById(R.id.voice_type);
            mDeleteIv = (ImageView) view.findViewById(R.id.voice_delete);
        }
    }
}
