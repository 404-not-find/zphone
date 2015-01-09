package com.zycoo.android.zphone.ui.me;

/**
 * Created by tqcenglish on 14-12-17.
 */
public class MeListViewItemAccount extends ListViewItem {
    public MeListViewItemAccount() {
        type = ListViewItem.TYPE_ACCOUNT;
    }

    private MeListAdapter.OnStatusChangeListener listener;

    public void setStatusChangeListener(MeListAdapter.OnStatusChangeListener listener) {
        this.listener = listener;
    }
    public MeListAdapter.OnStatusChangeListener getStatusChangeListener()
    {
        return listener;
    }
}
