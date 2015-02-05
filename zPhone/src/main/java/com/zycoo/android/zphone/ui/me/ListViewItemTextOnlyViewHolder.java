package com.zycoo.android.zphone.ui.me;

import android.view.View;
import android.widget.TextView;

import com.zycoo.android.zphone.R;

/**
 * Created by tqcenglish on 14-12-18.
 */
public class ListViewItemTextOnlyViewHolder {
    public final static int text_only_id = R.layout.list_item_text_only;
    private TextView name;
    private View divider;

    public ListViewItemTextOnlyViewHolder(View v) {
        name = (TextView) v.findViewById(R.id.id_item_name_tv);
        divider = v.findViewById(R.id.id_item_divider_lv);
        v.setTag(text_only_id, this);
    }

    public TextView getName() {
        return name;
    }

    public void setName(TextView name) {
        this.name = name;
    }

    public void setDividerVisible() {
        divider.setVisibility(View.VISIBLE);
    }

    public void setDividerInVisible() {
        divider.setVisibility(View.INVISIBLE);
    }
}
