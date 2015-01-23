package com.zycoo.android.zphone.ui.me;

import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import com.kyleduo.switchbutton.SwitchButton;
import com.zycoo.android.zphone.R;

/**
 * Created by tqcenglish on 14-12-18.
 */
public class ListViewItemSpinnerWithTextViewHolder {

    public final static int spinner_with_text_id = R.layout.list_item_spiner_with_text;

    public Spinner getSpinner() {
        return spinner;
    }

    private Spinner spinner;
    private TextView name;
    private TextView divider;

    public ListViewItemSpinnerWithTextViewHolder(View v) {
        name = (TextView) v.findViewById(R.id.id_item_name_tv);
        spinner = (Spinner) v.findViewById(R.id.id_item_spinner);
        divider = (TextView) v.findViewById(R.id.id_item_divider_tv);
        v.setTag(spinner_with_text_id, this);
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
