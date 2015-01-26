package com.zycoo.android.zphone.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zycoo.android.zphone.R;

/**
 * Created by tqcenglish on 15-1-7.
 */
public class DialDialpadButton extends RelativeLayout {

    private TextView tv1;
    private TextView tv2;

    public DialDialpadButton(Context context) {
        super(context);
    }

    public DialDialpadButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = context.obtainStyledAttributes(attrs,
                R.styleable.Options, 0, 0);
        String text1 = array.getString(R.styleable.Options_text1);
        String text2 = array.getString(R.styleable.Options_text2);
        float textSize1 = array.getFloat(R.styleable.Options_textSize1, 48);
        float textSize2 = array.getFloat(R.styleable.Options_textSize2, 14);
        boolean text2Visible = array.getBoolean(R.styleable.Options_text2visible, true);
        array.recycle();
        View rootView = LayoutInflater.from(context).inflate(R.layout.dial_dialpad_button, this, true);
        tv1 = (TextView) findViewById(android.R.id.text1);
        tv2 = (TextView) findViewById(android.R.id.text2);
        tv1.setText(text1);
        tv2.setText(text2);
        tv1.setTextSize(textSize1);
        tv2.setTextSize(textSize2);
        if (!text2Visible) {
            tv2.setVisibility(View.INVISIBLE);
        }
    }

    public void setTextViewText1(String text) {
        tv1.setText(text);

    }

    public void setTextViewText2(String text) {
        tv2.setText(text);
    }
}
