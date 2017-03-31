
package com.hiswill.babybrezza_1505;
import java.io.IOException;

import com.hiswill.babybrezza_1505.utils.DataUtils;
import com.hiswill.babybrezza_1505.R;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
//import android.content.Intent;
//import android.os.Handler;
import android.widget.TextView;

public class BottleIsReadyActivity extends Activity
{
	private String TAG = "BottleIsReadyActivity";
	private SharedPreferences mSettings;
	private SharedPreferences.Editor mSettingEditor;
	
	private int SoundOn;
	
	private Button key_ok;
	private TextView tv_0000, tv_linkstatus, tv_settingguide;
	private ImageView iv_sound;
	
	private Handler hTmr;
	private int Tick05s;
	private hw1505BleComm mHw1505BleComm;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bottle_is_ready);

		//close the others activities
		Log.i(TAG,"close the others activities");
		Intent intent = new Intent(BluetoothLeService.ACTION_CLOSE_ACTIVITY);
		sendBroadcast(intent);
		
		Log.i(TAG,"BottleIsReadyActivity");
		key_ok = (Button) findViewById(R.id.btn_OK);
		key_ok.setTypeface(PairingPageActivity.typeFace);
		key_ok.setOnClickListener(keylistener);
		
		iv_sound  = (ImageView)findViewById(R.id.iv_sound);
		iv_sound.setOnClickListener(keylistener);
		
		tv_settingguide  = (TextView)findViewById(R.id.tv_settingguide);
		tv_settingguide.setTypeface(PairingPageActivity.typeFace);
		tv_settingguide.setOnClickListener(keylistener);
		
		
		tv_0000  = (TextView)findViewById(R.id.tv_0000);
		tv_0000.setTypeface(PairingPageActivity.typeFace);
		
		tv_linkstatus = (TextView)findViewById(R.id.tv_linkstatus);
		tv_linkstatus.setTypeface(PairingPageActivity.typeFace);

		mSettings = getSharedPreferences("hw1505",Context.MODE_PRIVATE);
		mSettingEditor = mSettings.edit();
		SoundOn   = mSettings.getInt("set_sound",1);
		if(SoundOn == 1) 	iv_sound.setImageResource(R.drawable.icon_soundon);
		else				iv_sound.setImageResource(R.drawable.icon_soundoff);		
		
		ShowLinkStatus();

		Tick05s = 0;
		hTmr = new Handler();
		hTmr.postDelayed(new Runnable()
		{
			public void run()
			{
				Tick05s++;
				if(Tick05s < 10)
				{
					if((Tick05s & 1) != 0)
					{
						tv_0000.setVisibility(View.INVISIBLE);
					}
					else
					{
						tv_0000.setVisibility(View.VISIBLE);
					}
					hTmr.postDelayed(this, 500);
				}
				else
				{
					tv_0000.setVisibility(View.VISIBLE);
				}
			}
		}
		, 500);
	}

	
	private OnClickListener keylistener = new OnClickListener()
	{
		@Override
		public void onClick(View v) 
		{
			// TODO Auto-generated method stub
			int key = v.getId();
			
			if(key == R.id.btn_OK)
			{
				ReturnToPairingPage();
			}
			else if(key == R.id.tv_settingguide)
			{
				Intent intent = new Intent(BottleIsReadyActivity.this,SettiingGuideActivity.class);
				intent.putExtra("Parent Activity", "BottleIsReadyActivity");
				startActivity(intent);
				//finish();
			}
			else if(key == R.id.iv_sound)
			{
				if(SoundOn == 0) 	
				{
					SoundOn = 1;
					iv_sound.setImageResource(R.drawable.icon_soundon);
				}
				else				
				{
					SoundOn = 0;
					iv_sound.setImageResource(R.drawable.icon_soundoff);
				}
				//Indicator();

				mSettingEditor.putInt("set_sound", SoundOn);
				mSettingEditor.commit();
			}
		}
	};
	

	public void Indicator()
	{
		if(SoundOn == 0)
		{
    		Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    		vibrator.vibrate(new long[]{100,200}, -1);
    		return;
		}
		
		AudioManager mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		
		//������� & ��ǰ����
		//int maxVolume =mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		int currentVolume =mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		
		//(2016-4-18)
		//����������������Ч (audioStreamType�����õ������)
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
		
		MediaPlayer mp = new MediaPlayer();
		mp=MediaPlayer.create(BottleIsReadyActivity.this, R.raw.ding);
		try {
			mp.prepare();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mp.start();
	}
	
	private void ShowLinkStatus()
	{
		if(hw1505BleComm.bCommuicatedOk == true)	
			tv_linkstatus.setText(R.string.connected);
		else				
			tv_linkstatus.setText(R.string.disconnect);
		
		tv_linkstatus.setVisibility(View.VISIBLE);
	}

	//
	private void ReturnToPairingPage()
	{
		Log.i(TAG,"Close & back to PairingPageActivity");

		hw1505BleComm.SetAutoConn(false);
		hw1505BleComm.mBleService.uiClose();
		try
		{
			Thread.sleep(500);
		}catch(InterruptedException e)
		{
			e.printStackTrace();
		}

		//*
		Intent intent = new Intent(this,PairingPageActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
		finish();
		//*/
		
		/*
		Intent intent = new Intent(BottleIsReadyActivity.this,PairingPageActivity.class);
		startActivity(intent);
		finish();
		//*/
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if(keyCode == KeyEvent.KEYCODE_BACK)
		{
			ReturnToPairingPage();
		}
		return super.onKeyDown(keyCode, event);
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
	        	public void uiCommunicated()
	        	{
	            	runOnUiThread(new Runnable()
	            	{
	        			@Override
	        			public void run() {
							ShowLinkStatus();
	        			}
	            	});
	        	}
	        	@Override
	        	public void uiDeviceDisconnected() 
	        	{
	        		runOnUiThread(new Runnable()
	        		{
						@Override
						public void run() {
							ShowLinkStatus();
						}
	        		});
	        	}
	        	
	        	@Override
	        	public void uiBluetoothErr133() 
	        	{
	        		runOnUiThread(new Runnable()
	        		{
						@Override
						public void run() {
							//tv_linkstatus.setText("Error:Pls turn off system bluetooth then turn it on");
						}
	        		});
	        	}
	        	/*
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
	    		}*/
	        });
        }
        hw1505BleComm.setCurrentActivity(this);
	}
	
	@Override
	protected void onDestroy()
	{
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.i(TAG, "onDestroy");		
		mHw1505BleComm.ActivtiyDestroyed();
	}
}
