package com.zycoo.android.zphone.ui.settings;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

import com.zycoo.android.zphone.R;
import com.zycoo.android.zphone.ZycooConfigurationEntry;

import org.doubango.ngn.services.INgnConfigurationService;

public class ScreenAMI extends BaseScreen
{
	private final static String LOG_TAG = ScreenAMI.class.getCanonicalName();
	private final INgnConfigurationService mConfigurationService;
	private RadioButton mEnableAmiButton, mDisableAmiButton;
	private RelativeLayout mAMIRelativeLayout;
	private EditText mEditTextHost;
	private EditText mEditTextPort;
	private EditText mEditTextUserName;
	private EditText mEditTextSecret;

	public ScreenAMI()
	{
		mConfigurationService = getEngine().getConfigurationService();
	}

	@Override
	protected void onCreate(Bundle arg0)
	{
		super.onCreate(arg0);
		setContentView(R.layout.screen_ami);
		//findViewById
		mAMIRelativeLayout = (RelativeLayout) findViewById(R.id.screen_ami_rl);
		mEnableAmiButton = (RadioButton) findViewById(R.id.screen_ami_radioButton_ami_enable);
		mDisableAmiButton = (RadioButton) findViewById(R.id.screen_ami_radioButton_ami_disable);
		mEditTextHost = (EditText) findViewById(R.id.screen_ami_editText_host);
		mEditTextPort = (EditText) findViewById(R.id.screen_ami_editText_port);
		mEditTextUserName = (EditText) findViewById(R.id.screen_ami_editText_username);
		mEditTextSecret = (EditText) findViewById(R.id.screen_ami_editText_secret);
		//set default value
		boolean enable = mConfigurationService.getBoolean(ZycooConfigurationEntry.NETWORK_AMI_ENABLE, false);
		mEnableAmiButton.setChecked(enable);
		mDisableAmiButton.setChecked(!enable);
		mAMIRelativeLayout.setVisibility(mEnableAmiButton.isChecked() ? View.VISIBLE
				: View.INVISIBLE);
		mEditTextHost.setText(mConfigurationService.getString(
				ZycooConfigurationEntry.NETWORK_AMI_HOST,
				ZycooConfigurationEntry.DEFAULT_NETWORK_AMI_HOST));
		mEditTextPort.setText(mConfigurationService.getString(
				ZycooConfigurationEntry.NETWORK_AMI_PORT,
				ZycooConfigurationEntry.DEFAULT_NETWORK_AMI_PORT));
		mEditTextUserName.setText(mConfigurationService.getString(
				ZycooConfigurationEntry.NETWORK_AMI_USERNAME,
				ZycooConfigurationEntry.DEFAULT_NETWORK_AMI_USERNAME));
		mEditTextSecret.setText(mConfigurationService.getString(
				ZycooConfigurationEntry.NETWORK_AMI_SECRET,
				ZycooConfigurationEntry.DEFAULT_NETWORK_AMI_SECRET));
		//set listener
		super.addConfigurationListener(mEnableAmiButton);
		super.addConfigurationListener(mDisableAmiButton);
		super.addConfigurationListener(mEditTextHost);
		super.addConfigurationListener(mEditTextPort);
		super.addConfigurationListener(mEditTextUserName);
		super.addConfigurationListener(mEditTextSecret);
		mEnableAmiButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				mAMIRelativeLayout.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
				if (isChecked)
				{
					 mConfigurationService.putBoolean(ZycooConfigurationEntry.NETWORK_AMI_ENABLE,
							true);
				}
				else
				{
					mConfigurationService.putBoolean(ZycooConfigurationEntry.NETWORK_AMI_ENABLE,
							false);
				}
				mComputeConfiguration = true;
			}
		});
	}

	@Override
	protected void onPause()
	{
		if (super.mComputeConfiguration)
		{
			mConfigurationService.putString(ZycooConfigurationEntry.NETWORK_AMI_HOST, mEditTextHost
					.getText().toString().trim());
			mConfigurationService.putString(ZycooConfigurationEntry.NETWORK_AMI_PORT, mEditTextPort
					.getText().toString().trim());
			mConfigurationService.putString(ZycooConfigurationEntry.NETWORK_AMI_USERNAME,
					mEditTextUserName.getText().toString().trim());
			mConfigurationService.putString(ZycooConfigurationEntry.NETWORK_AMI_SECRET,
					mEditTextSecret.getText().toString().trim());
			if (!mConfigurationService.commit())
			{
				Log.e(LOG_TAG, "Failed to commit() ami configuration ");
			}
			mComputeConfiguration = false;
		}
		super.onPause();
	}
}
