package com.zycoo.android.zphone.ui.me;

/**
 * Created by tqcenglish on 14-12-17.
 */
public class ListViewItemTextOnly extends ListViewItem {
    public int nameResId;
    public boolean dividerVisible;

    public ListViewItemTextOnly(int nameResId, boolean dividerVisible) {
        type = ListViewItem.TYPE_TEXT_ONLY;
        visible = true;
        this.nameResId = nameResId;
        this.dividerVisible = dividerVisible;
    }

    public int getNameResId() {
        return nameResId;
    }
    public boolean isDividerVisible() {
        return dividerVisible;
    }

    public void setDividerVisible(boolean dividerVisible) {
        this.dividerVisible = dividerVisible;
    }
}
