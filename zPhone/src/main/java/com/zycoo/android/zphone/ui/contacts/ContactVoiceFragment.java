package com.zycoo.android.zphone.ui.contacts;

import android.os.Bundle;

import com.zycoo.android.zphone.widget.SuperAwesomeCardFragment;

/**
 * Created by tqcenglish on 15-1-29.
 */
public class ContactVoiceFragment extends SuperAwesomeCardFragment{

    public static ContactVoiceFragment newInstance(int position) {
        ContactVoiceFragment f = new ContactVoiceFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);
        return f;
    }
}
