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
/*
 * 拨号盘下方按钮的定制
 * 
 */

package com.zycoo.android.zphone.ui.dialpad;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.LinearLayout;

import com.zycoo.android.zphone.R;
import com.zycoo.android.zphone.ZphoneApplication;
import com.zycoo.android.zphone.utils.Utils;

public class DialerCallBar extends LinearLayout implements OnClickListener, OnLongClickListener {

    public interface OnDialActionListener {
        void placeTextDigit(View view);

        /**
         * The make call button has been pressed
         */
        void placeCall();

        /**
         * The video button has been pressed
         */
        void placeVideoCall();

        /**
         * The remove_from_call_log button has been pressed
         */
        void deleteChar();

        /**
         * The remove_from_call_log button has been long pressed
         */
        void deleteAll();
    }

    private OnDialActionListener actionListener;

    public DialerCallBar(Context context) {
        this(context, null, 0);
    }

    public DialerCallBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DialerCallBar(Context context, AttributeSet attrs, int style) {
        super(context, attrs);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.fragment_dialpad_additional_buttons, this, true);
        findViewById(R.id.dialVideoButton).setOnClickListener(this);
        findViewById(R.id.dialButton).setOnClickListener(this);
        findViewById(R.id.deleteButton).setOnClickListener(this);
        findViewById(R.id.deleteButton).setOnLongClickListener(this);
        this.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 32));

        if (!Utils.isPro(ZphoneApplication.getContext())) {
            findViewById(R.id.dialVideoButton).setVisibility(View.GONE);
            findViewById(R.id.divider2).setVisibility(View.GONE);
        }

        if (getOrientation() == LinearLayout.VERTICAL) {
            LayoutParams lp;
            for (int i = 0; i < getChildCount(); i++) {
                lp = (LayoutParams) getChildAt(i).getLayoutParams();
                /*
                int w = lp.width;
                lp.width = lp.height;
                lp.height = w;
                */
                lp.height = 32;
                lp.gravity = Gravity.CENTER_HORIZONTAL;
                // Added for clarity but not necessary
                getChildAt(i).setLayoutParams(lp);

            }
        }
    }

    /**
     * Set a listener for this widget actions
     *
     * @param l the listener called back when some user action is done on this
     *          widget
     */
    public void setOnDialActionListener(OnDialActionListener l) {
        actionListener = l;
    }

    /**
     * Set the action buttons enabled or not
     */
    public void setEnabled(boolean enabled) {
        findViewById(R.id.dialButton).setEnabled(enabled);
        if (Utils.isPro(ZphoneApplication.getContext())) {
            findViewById(R.id.dialVideoButton).setEnabled(enabled);
        } else {
            findViewById(R.id.dialVideoButton).setVisibility(View.GONE);
        }
        findViewById(R.id.deleteButton).setEnabled(enabled);
    }

    /**
     * Set the video capabilities 控制视频按钮选项
     *
     * @param enabled whether the client is able to make video calls
     */
    public void setVideoEnabled(boolean enabled) {
        findViewById(R.id.dialVideoButton).setVisibility(enabled ? View.VISIBLE : View.INVISIBLE);
    }

    /**
     *
     */
    @Override
    public void onClick(View v) {
        if (actionListener != null) {
            int viewId = v.getId();
            if (viewId == R.id.dialVideoButton) {
                actionListener.placeVideoCall();
            } else if (viewId == R.id.dialButton) {
                actionListener.placeCall();
            } else if (viewId == R.id.deleteButton) {
                actionListener.deleteChar();
            }
        }
    }

    /**
     * 长按监听
     */
    @Override
    public boolean onLongClick(View v) {
        if (actionListener != null) {
            int viewId = v.getId();
            if (viewId == R.id.deleteButton) {
                actionListener.deleteAll();
                v.setPressed(false);
                return true;
            }
        }
        return false;
    }

}
