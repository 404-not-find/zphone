package com.zycoo.android.zphone.ui.contacts;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.tqcenglish.vlcdemo.AudioPlayActivity;
import com.zycoo.android.zphone.R;
import com.zycoo.android.zphone.ui.message.MessageFragment;
import com.zycoo.android.zphone.utils.Utils;
import com.zycoo.android.zphone.widget.SuperAwesomeCardFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tqcenglish on 15-1-29.
 */
public class ContactVoiceFragment extends SuperAwesomeCardFragment implements AdapterView.OnItemClickListener {

    private String mContactDisplayName;
    private List<String> mNumbers;
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
        if (null != mNumbers) {
            for (String number : mNumbers) {
                new CheckVoiceTask().execute(MessageFragment.VOICE_MAIL_PATH + number, MessageFragment.RECORD_PATH + number);
            }
        }
        super.onResume();
       /* if (null != mNumber) {
            new CheckVoiceTask().execute(MessageFragment.VOICE_MAIL_PATH + mNumber, MessageFragment.RECORD_PATH + mNumber);
        }*/
    }

    public void setContact(List<String> numbers) {
        mNumbers = numbers;
        for (String number : mNumbers) {
            new CheckVoiceTask().execute(MessageFragment.VOICE_MAIL_PATH + number, MessageFragment.RECORD_PATH + number);
        }

        /*
        mContactDisplayName = displayName;
        mNumber = getPhoneNumber(mContactDisplayName, getSherlockActivity());
        if (null != mNumber) {
            new CheckVoiceTask().execute(MessageFragment.VOICE_MAIL_PATH + mNumber, MessageFragment.RECORD_PATH + mNumber);
        }*/
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

    class CheckVoiceTask extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {

            mVoiceItems.clear();
            String voice_mail = params[0];
            File voice_mail_file = new File(voice_mail);
            File[] voice_mails = voice_mail_file.listFiles();
            if (null != voice_mails) {
                for (File voice : voice_mails) {
                    VoiceItem item = new VoiceItem();
                    item.path = voice.getAbsolutePath();
                    item.name = voice.getName();
                    String[] strs = voice.getName().split("\\.");
                    item.date = strs[0];
                    item.type = VOICE_TYPE.VOICE_MAIL;
                    mVoiceItems.add(item);
                }
            }


            String record = params[1];
            String recording_record = record + "/recording/";
            File file = new File(recording_record);
            File[] records = file.listFiles();
            if (null != records) {
                for (File voice : records) {
                    VoiceItem item = new VoiceItem();
                    item.path = voice.getAbsolutePath();
                    //eg 20150108-115214-804-803-1420689134.274-71.wav
                    item.name = voice.getName();
               /* Pattern p = Pattern.compile("-([0-9]{10}).");
                Matcher m = p.matcher(voice.getName());
                item.date =  m.group();*/
                    item.date = item.name.split("-")[4].split("\\.")[0];
                    item.type = VOICE_TYPE.RECORD;
                    mVoiceItems.add(item);
                }
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

            switch (mVoiceItems.get(position).type) {
                case VOICE_MAIL:
                    voiceItemViewHolder.typeTv.setText(R.string.voice_mail);
                    voiceItemViewHolder.mTypeIv.setImageResource(R.drawable.ic_email_white);
                    Utils.setImageViewFilter(voiceItemViewHolder.mTypeIv, R.color.cyan_500);
                    break;
                case RECORD:
                    voiceItemViewHolder.typeTv.setText(getResources().getString(R.string.record));
                    voiceItemViewHolder.mTypeIv.setImageResource(R.drawable.ic_voicemail_white);
                    Utils.setImageViewFilter(voiceItemViewHolder.mTypeIv, R.color.teal_500);
                    break;
            }
            voiceItemViewHolder.dateTv.setReferenceTime(Long.parseLong(mVoiceItems.get(position).date) * 1000);
            voiceItemViewHolder.nameTv.setText(mVoiceItems.get(position).name);
            voiceItemViewHolder.mDeleteIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //delete file
                    new DeleteFileTask().execute(mVoiceItems.get(position).path, String.valueOf(position));
                }
            });
            return convertView;

        }

        class DeleteFileTask extends AsyncTask<String, Void, Boolean> {
            String path;
            String position;
            @Override
            protected void onPostExecute(Boolean result) {
                if (result) {
                    mVoiceItems.remove(Integer.parseInt(position));
                    notifyDataSetChanged();
                    String toastStr = getString(R.string.delete_voice_file_successful, path);
                    Toast.makeText(getActivity(), toastStr, Toast.LENGTH_SHORT).show();
                } else {
                    String toastStr = getString(R.string.delete_voice_file_failure, path);
                    Toast.makeText(getActivity(), toastStr, Toast.LENGTH_SHORT).show();
                }
                super.onPostExecute(result);
            }

            @Override
            protected Boolean doInBackground(String... params) {
                if (2 != params.length) {
                    return false;
                }
                path = params[0];
                position = params[1];
                return new File(params[0]).delete();
            }
        }
    }

    enum VOICE_TYPE {
        VOICE_MAIL,
        RECORD
    }

    class VoiceItem {

        String path;
        VOICE_TYPE type;
        String date;
        String name;
    }

    class VoiceItemViewHolder {
        RelativeTimeTextView dateTv;
        TextView typeTv;
        TextView nameTv;
        ImageView mDeleteIv;
        ImageView mTypeIv;

        public VoiceItemViewHolder(View view) {
            view.setTag(this);
            dateTv = (RelativeTimeTextView) view.findViewById(R.id.time_date);
            nameTv = (TextView) view.findViewById(R.id.item_name);
            typeTv = (TextView) view.findViewById(R.id.voice_type);
            mDeleteIv = (ImageView) view.findViewById(R.id.voice_delete);
            mTypeIv = (ImageView) view.findViewById(android.R.id.icon);
        }
    }
}
