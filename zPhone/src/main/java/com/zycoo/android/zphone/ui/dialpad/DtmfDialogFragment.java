/**
 * Copyright (C) 2010-2012 Regis Montoya (aka r3gis - www.r3gis.fr)
 * This file is part of CSipSimple.
 *
 *  CSipSimple is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  If you own a pjsip commercial license you can also redistribute it
 *  and/or modify it under the terms of the GNU Lesser General Public License
 *  as an android library.
 *
 *  CSipSimple is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with CSipSimple.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.zycoo.android.zphone.ui.dialpad;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.zycoo.android.zphone.R;
import com.zycoo.android.zphone.ui.dialpad.Dialpad.OnDialKeyListener;
import com.zycoo.android.zphone.utils.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DtmfDialogFragment extends SherlockDialogFragment implements OnDialKeyListener {

    private Logger mLogger = LoggerFactory.getLogger(DtmfDialogFragment.class.getCanonicalName());
    private static final String EXTRA_CALL_ID = "call_id";
    private static final String THIS_FILE = "DtmfDialogFragment";
    private TextView dialPadTextView;

    public static DtmfDialogFragment newInstance(int callId) {
        DtmfDialogFragment instance = new DtmfDialogFragment();
        Bundle args = new Bundle();
        args.putInt(EXTRA_CALL_ID, callId);
        instance.setArguments(args);
        return instance;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new AlertDialog.Builder(getActivity())
                .setView(getCustomView(getActivity().getLayoutInflater(), null, savedInstanceState))
                .setCancelable(true)
                .setNeutralButton(R.string.done, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                })
                .create();
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point screenSize = new Point();
        int width;
        int height;
        if (Utils.hasHoneycombMR2()) {
            display.getSize(screenSize);

            width = screenSize.x;
            height = screenSize.y;
        } else {
            height = display.getHeight();
            width = display.getWidth();
        }
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = width >= 1024 ? (int) (width * 0.8) : WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = width >= 1024 ? (int) (height * 0.5) : WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.show();
        dialog.getWindow().setAttributes(lp);
        return dialog;
    }

    public View getCustomView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.in_call_dialpad, container, false);
        Dialpad dialPad = (Dialpad) v.findViewById(R.id.dialPad);
        //dialPad.setForceWidth(true);
        dialPad.setOnDialKeyListener(this);
        dialPadTextView = (TextView) v.findViewById(R.id.digitsText);
        ImageView backspace = (ImageView) v.findViewById(R.id.backspace_ib);
        backspace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = dialPadTextView.getText().toString();
                if (str.length() > 0) {
                    dialPadTextView.setText(str.subSequence(0, str.length() - 1));
                }
            }
        });

        backspace.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                dialPadTextView.setText("");
                return true;
            }
        });
        return v;
    }

    public interface OnDtmfListener {
        void OnDtmf(int callId, int keyCode, int dialTone);
    }

    @Override
    public void onTrigger(int keyCode, int dialTone) {
        if (dialPadTextView != null) {
            // Update text view
            KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
            char nbr = event.getNumber();
            StringBuilder sb = new StringBuilder(dialPadTextView.getText());
            sb.append(nbr);
            dialPadTextView.setText(sb.toString());
        }
        if (getSherlockActivity() instanceof OnDtmfListener) {

            Integer callId = getArguments().getInt(EXTRA_CALL_ID);
            if (callId != null) {
                ((OnDtmfListener) getSherlockActivity()).OnDtmf(callId, keyCode, dialTone);
            } else {
                Log.w(THIS_FILE, "Impossible to find the call associated to this view");
            }
        }

    }

}
