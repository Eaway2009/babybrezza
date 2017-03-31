package com.hiswill.babybrezza_1505;

import com.hiswill.babybrezza_1505.R;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import java.util.Locale;

public class ConnectedActivity extends Activity
{
	private String TAG = "ConnectedActivity";
	private hw1505BleComm mHw1505BleComm;
	//public final static String ACTION_DEVICE_CONTROL = PairingPageActivity.ACTION_DEVICE_CONTROL;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Log.i(TAG,"onCreate");
	}

	@Override
	protected void onStart() 
	{
		super.onStart();
		Log.i(TAG,"onStart");
		
		setContentView(R.layout.showstate_page);
		AutofitTextView autofitTextView = (AutofitTextView) findViewById(R.id.bottle_warmer_connected_text);
		Resources resources = getResources();
		Configuration configuration = resources.getConfiguration();
		if(configuration.locale == Locale.ENGLISH){
			autofitTextView.setMaxLines(2);
		}else{
			autofitTextView.setMaxLines(3);
		}

		Intent intent = getIntent();
		String parentActivity = intent.getStringExtra("parent_Activity");
		if(parentActivity.equals("WarmerOperateActivity"))
		{
			Log.i(TAG,"! Jump from WarmerOperateActivity");
		}
		else
		{
			Log.i(TAG,"! Jump from PairingPageActivity");
		}
		
		new Handler().postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				Log.i(TAG,"from ConnectedActivity to WarmerOperateActivity");

				Intent intent = new Intent(ConnectedActivity.this,WarmerOperateActivity.class);
				startActivity(intent);
				finish();
			}
		}, 1000);
	}

	@Override
	protected void onResume() 
	{
		super.onResume();

        if(mHw1505BleComm == null)
        {
    		Log.i(TAG,"new hw1505BleComm");
        	mHw1505BleComm = new hw1505BleComm(this,new hw1505BleUiCallbacks.Null()
        	{
	    		@Override
	    		public void uiActivityFinish()
	    		{
	        		runOnUiThread(new Runnable()
	        		{
						@Override
						public void run() {
							Log.i(TAG, "self-finish");
							finish();
						}
	        		});
	    		}
	        });
        }
        hw1505BleComm.setCurrentActivity(this);
	}

	@Override
	protected void onPause() 
	{
		super.onPause();
	}
	
	@Override
	protected void onStop() 
	{
		super.onStop();
	}

	@Override
	protected void onDestroy() 
	{
		super.onDestroy();
		mHw1505BleComm.ActivtiyDestroyed();
	}
}

