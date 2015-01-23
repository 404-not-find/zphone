package com.zycoo.android.zphone.ui.me;

/**
 * Created by tqcenglish on 14-12-17.
 */
public class ListViewItemWhite extends ListViewItem {
    private int itemHeight;
    public ListViewItemWhite(int height) {
        type = TYPE_WHITE;
        visible = true;
        itemHeight = height;
    }
    public int getItemHeight() {
        return itemHeight;
    }
}
