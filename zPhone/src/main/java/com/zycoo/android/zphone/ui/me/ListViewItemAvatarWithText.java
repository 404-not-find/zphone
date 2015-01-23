package com.zycoo.android.zphone.ui.me;

/**
 * Created by tqcenglish on 14-12-17.
 */
public class ListViewItemAvatarWithText extends ListViewItem {
    public int avatarResId;
    public int nameResId;
    public boolean dividerVisible;

    public ListViewItemAvatarWithText(int nameResId, int avatarResId, boolean dividerVisible) {
        type = ListViewItem.TYPE_AVATAR_WITH_TEXT;
        visible = true;
        this.nameResId = nameResId;
        this.avatarResId = avatarResId;
        this.dividerVisible = dividerVisible;
    }

    public int getNameResId() {
        return nameResId;
    }

    public int getAvatarResId() {
        return avatarResId;
    }

    public boolean isDividerVisible() {
        return dividerVisible;
    }

}
