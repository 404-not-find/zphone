package com.zycoo.android.zphone.ui.me;

import android.view.View;
import android.widget.TextView;

import com.zycoo.android.zphone.R;

/**
 * Created by tqcenglish on 14-12-18.
 */
public class ListViewItemGreyViewHolder {
    public final static int grey_divider_id = R.layout.list_item_grey_divider;
    private TextView divider;

    public ListViewItemGreyViewHolder(View v) {
        divider = (TextView) v.findViewById(R.id.id_item_grey_divider_tv);
        v.setTag(grey_divider_id, this);
    }

    public void setWhiteDividerHeight(int heightPx) {
        divider.setHeight(heightPx);
    }
}
