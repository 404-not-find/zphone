package com.zycoo.android.zphone.ui.me;

import java.util.List;

/**
 * Created by tqcenglish on 14-12-17.
 */
public class ListViewItemSpinnerWithText extends ListViewItem {
    public int getSpinnerResId() {
        return spinnerResId;
    }


    public int selection;
    public int spinnerResId;
    public int nameResId;
    public boolean dividerVisible;

    public ListViewItemSpinnerWithText(int nameResId, int spinnerResId, boolean dividerVisible, int selection) {
        type = ListViewItem.TYPE_SPINNER_WITH_TEXT;
        visible = true;
        this.spinnerResId = spinnerResId;
        this.nameResId = nameResId;
        this.dividerVisible = dividerVisible;
        this.selection = selection;
    }

    public int getNameResId() {
        return nameResId;
    }

    public boolean isDividerVisible() {
        return dividerVisible;
    }

    public int getSelection() {
        return selection;
    }

    public void setSelection(int selection) {
        this.selection = selection;
    }
    public void setDividerVisible(boolean dividerVisible) {
        this.dividerVisible = dividerVisible;
    }
}
