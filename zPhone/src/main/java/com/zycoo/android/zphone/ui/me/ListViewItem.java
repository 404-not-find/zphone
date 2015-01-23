package com.zycoo.android.zphone.ui.me;

/**
 * Created by tqcenglish on 14-12-17.
 */
public abstract class ListViewItem {
    public static final int TYPE_AVATAR_WITH_TEXT = 0;
    public static final int TYPE_TEXT_ONLY = 1;
    public static final int TYPE_ACCOUNT = 2;
    public static final int TYPE_WHITE = 3;
    public static final int TYPE_EXIT = 4;
    public static final int TYPE_SWITCH_ICON_WITH_TEXT = 5;
    public static final int TYPE_SPINNER_WITH_TEXT = 6;
    public static final int TYPE_EDIT = 7;
    protected int type;
    protected boolean visible;

    public static int getTypeCount() {
        return 8;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

}
