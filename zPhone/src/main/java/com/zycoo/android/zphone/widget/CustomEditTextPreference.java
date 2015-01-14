package com.zycoo.android.zphone.widget;

import android.app.Activity;
import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import com.zycoo.android.zphone.R;

import eu.inmite.android.lib.validations.form.FormValidator;
import eu.inmite.android.lib.validations.form.annotations.MinLength;
import eu.inmite.android.lib.validations.form.annotations.NotEmpty;
import eu.inmite.android.lib.validations.form.callback.SimpleErrorPopupCallback;

/**
 * Created by tqcenglish on 1/13/15.
 */
public class CustomEditTextPreference extends EditTextPreference {
    @NotEmpty(messageId = R.string.not_empty)
    private EditText mEditText;
    public CustomEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        FormValidator.validate((Activity)context, new SimpleErrorPopupCallback(context));
    }

    public CustomEditTextPreference(Context context) {
        super(context);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        mEditText = (EditText) view.findViewById(android.R.id.inputExtractEditText);
    }
}
