package com.zycoo.android.zphone.ui.me;

import android.view.View;
import android.widget.TextView;

import com.zycoo.android.zphone.R;

/**
 * Created by tqcenglish on 14-12-18.
 */
public class ListViewItemWhiteViewHolder {
    public final static int white_divider_id = R.layout.list_item_white_divider;
    private TextView divider;

    public ListViewItemWhiteViewHolder(View v) {
        divider = (TextView) v.findViewById(R.id.id_item_white_divider_tv);
        v.setTag(white_divider_id, this);
    }

    public void setWhiteDividerHeight(int heightPx) {
        divider.setHeight(heightPx);
    }
}
