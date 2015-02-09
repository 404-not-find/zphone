package com.zycoo.android.zphone.ui.me;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.zycoo.android.zphone.R;

/**
 * Created by tqcenglish on 14-12-17.
 */
public class ListViewItemAvatarWithTextViewHolder {
    public final static int avatar_with_text_id = R.layout.list_item_avatar_with_text;
    private ImageView avatar;
    private TextView name;
    private View divider;

    public ListViewItemAvatarWithTextViewHolder(View v) {
        name = (TextView) v.findViewById(R.id.id_item_name_tv);
        avatar = (ImageView) v.findViewById(R.id.id_item_av_iv);
        divider = v.findViewById(R.id.id_item_divider_lv);
        v.setTag(avatar_with_text_id, this);
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
    public void setDividerVisible()
    {
        divider.setVisibility(View.VISIBLE);
    }
    public void setDividerInVisible()
    {
        divider.setVisibility(View.INVISIBLE);
    }
}
