package com.zycoo.android.zphone.ui.me;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kyleduo.switchbutton.SwitchButton;
import com.zycoo.android.zphone.R;

/**
 * Created by tqcenglish on 14-12-17.
 */
public class MeListViewItemAccountViewHolder {
    private ImageView avatar;
    private TextView name;
    private TextView extension;
    private TextView status_tv;
    private SwitchButton status_bt;


    public MeListViewItemAccountViewHolder(View v)
    {
        avatar = (ImageView) v.findViewById(R.id.id_me_photo_iv);
        name = (TextView) v.findViewById(R.id.nick_name_tv);
        extension = (TextView) v.findViewById(R.id.account_tv);
        status_tv = (TextView) v.findViewById(R.id.register_status_tv);
        status_bt = (SwitchButton) v.findViewById(R.id.register_status_bt);
        v.setTag(R.layout.list_item_me_account_info, this);
    }
    public TextView getExtension() {
        return extension;
    }

    public void setExtension(TextView extension) {
        this.extension = extension;
    }

    public TextView getStatus_tv() {
        return status_tv;
    }

    public void setStatus_tv(TextView status_tv) {
        this.status_tv = status_tv;
    }

    public SwitchButton getStatus_bt() {
        return status_bt;
    }

    public void setStatus_bt(SwitchButton status_bt) {
        this.status_bt = status_bt;
    }

    public ImageView getAvatar() {
        return avatar;
    }

    public void setAvatar(ImageView avatar) {
        this.avatar = avatar;
    }

    public TextView getName() {
        return name;
    }

    public void setName(TextView name) {
        this.name = name;
    }
}
