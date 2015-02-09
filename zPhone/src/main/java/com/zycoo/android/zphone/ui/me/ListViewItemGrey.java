package com.zycoo.android.zphone.ui.me;

/**
 * Created by tqcenglish on 14-12-17.
 */
public class ListViewItemGrey extends ListViewItem {
    private int itemHeight;
    public ListViewItemGrey(int height) {
        type = TYPE_GREY;
        visible = true;
        itemHeight = height;
    }
    public int getItemHeight() {
        return itemHeight;
    }
}
