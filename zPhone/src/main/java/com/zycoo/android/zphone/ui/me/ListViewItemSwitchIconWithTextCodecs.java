package com.zycoo.android.zphone.ui.me;

/**
 * Created by tqcenglish on 14-12-17.
 */
public class ListViewItemSwitchIconWithTextCodecs extends ListViewItemSwitchIconWithText {
    public int codecsId;
    public boolean isChecked;
    public int nameResId;
    public boolean dividerVisible;

    public ListViewItemSwitchIconWithTextCodecs(int nameResId, boolean dividerVisible, boolean isChecked, int codecsId) {
        super(nameResId, dividerVisible, isChecked);
        this.codecsId = codecsId;
    }

    public int getCodecsId() {
        return codecsId;
    }

    public void setCodecsId(int codecsId) {
        this.codecsId = codecsId;
    }

    @Override
    public void setChecked(boolean checked) {
        super.setChecked(checked);

    }
}
