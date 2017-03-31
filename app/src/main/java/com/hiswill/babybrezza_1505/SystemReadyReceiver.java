package com.hiswill.babybrezza_1505;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SystemReadyReceiver extends BroadcastReceiver
{
	String TAG = "SystemReadyReceiver";
	@Override
	public void onReceive(Context context, Intent intent)
	{
		// TODO Auto-generated method stub
		try
		{
			context.startService(new Intent(context, BluetoothLeService.class));
		} catch (Exception e)
		{
			Log.i(TAG, "BluetoothLeService can't start");
		}
	}

}
