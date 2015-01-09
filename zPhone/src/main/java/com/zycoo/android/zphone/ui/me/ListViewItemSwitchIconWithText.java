package com.zycoo.android.zphone.ui.me;

/**
 * Created by tqcenglish on 14-12-17.
 */
public class ListViewItemSwitchIconWithText extends ListViewItem {

    public boolean isChecked;
    public int nameResId;
    public boolean dividerVisible;

    public ListViewItemSwitchIconWithText(int nameResId, boolean dividerVisible, boolean isChecked) {
        type = ListViewItem.TYPE_SWITCH_ICON_WITH_TEXT;
        visible = true;
        this.nameResId = nameResId;
        this.dividerVisible = dividerVisible;
        this.isChecked = isChecked;
    }

    public int getNameResId() {
        return nameResId;
    }

    public boolean isDividerVisible() {
        return dividerVisible;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        this.isChecked = checked;
    }

    public void setDividerVisible(boolean dividerVisible) {
        this.dividerVisible = dividerVisible;
    }
}
