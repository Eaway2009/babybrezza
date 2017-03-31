 package com.hiswill.babybrezza_1505;
 
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hiswill.babybrezza_1505.utils.DataUtils;
import com.hiswill.babybrezza_1505.wheel.NumericWheelAdapter;
import com.hiswill.babybrezza_1505.wheel.OnWheelChangedListener;
import com.hiswill.babybrezza_1505.wheel.OnWheelScrollListener;
import com.hiswill.babybrezza_1505.wheel.WheelView;

public class WarmerOperateActivity extends Activity
{
	private String TAG = "WarmerOperateActivity";
	static  Activity  Activity_Opr=null;
	
	LinearLayout layout_error;
	LinearLayout layout_0000;
	
	private TextView tv_counttime,tv_showstatus,tv_info;//,tv_speed,tv_temp,tv_setting, tv_back;
	private TextView tv_seting_guide;
	private ImageView iv_logo;
	
	private AutofitTextView btn_start;
	private ImageView iv_sound,iv_cap_up,iv_cap_down,iv_tmp_up,iv_tmp_down,iv_spd_up,iv_spd_down;
	
	NumericWheelAdapter numericWheelAdapterCapacity;//����������
	NumericWheelAdapter numericWheelAdapterSpeed;	//�ٶ�������
	NumericWheelAdapter numericWheelAdapterTemp;	//�¶�������
	
	//List<String> showStrListData;
	
	//private int hour,minute,second;
	//private int iniHour,iniMinute,iniSecond;
	private int AppHour,AppMinute,AppSecond;
	private int ShowHour,ShowMinute,ShowSecond;
	private int Cnt1s=0, TimerInterval = 1000;

	private int capacityBak,speedModelBak,workingModelBak;
	private int capacity,	speedModel,	workingModel,	SoundOn;
	private int capItems,spdItems,tmpItems;
	private boolean forceToQuick;
	private boolean defrostSetting;
	
	public static final int SPEED_QUICK = 1,SPEED_STEADY = 2;
	public static final int TEMP_ROOM = 1, TEMP_COLD = 2;
	private Handler mHandler;
	private boolean handlerRunning = false;
	
	private String	mDeviceName	= null;
	private String	mDeviceAddress	= null;
	private long exitTime;

	//APP's
	static final int STATE_SETTING		= 0;
	static final int STATE_COUNTING		= 1;
	static final int STATE_COUNDDOWNOK	= 2;	
	private int WorkState=STATE_SETTING;
	
	private hw1505BleComm mHw1505BleComm;
	
	//Device',see strStatusTab
	static final byte STATE_UNKNOWN		= 0;		// unknown status,
	static final byte STATE_POWEROFF	= 1;		// Warmer is power off,
	static final byte STATE_POWERON		= 2;		// Warmer is power on,
	static final byte STATE_WORKING		= 3;		// Warmer is working,
	static final byte STATE_1ST_OPEN	= 4;		// The first warmer sensor was open circuit fault,
	static final byte STATE_1ST_SHORT	= 5;		// The first warmer sensor was short circuit fault,
	static final byte STATE_2ND_OPEN	= 6;		// The second warmer sensor was open circuit fault,
	static final byte STATE_2RD_SHORT	= 7;		// The third warmer sensor was short circuit fault
	static final byte STATE_NO_WATER	= 8;		// no water
	static final byte STATE_TBD_ERROR	= 9;
	private byte DeviceStatus			= STATE_UNKNOWN;
	private byte SettingStatus			= STATE_UNKNOWN;
	

	private Handler handlerToInit;				//v0.5.3
	private boolean bChkState;					//v0.5.3
	
	//��������࣬�ж�APP�Ƿ���ǰ̨����
	Test test;
	DataUtils dataUtils;
	
	private SharedPreferences mSettings;
	private SharedPreferences.Editor mSettingEditor;
	private int ClickCnt, firClick, secClick;

	/**
	 * �Ƿ�ǰҳ��
	 */
	private boolean ifOnOperatePage()
	{
        ActivityManager activityManager=(ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);  
        String runningActivity=activityManager.getRunningTasks(1).get(0).topActivity.getClassName();
		Log.i(TAG, "runningActivity="+runningActivity);

		//if(test.isRunningForground(WarmerOperateActivity.this)==true)
		return (runningActivity.equals("com.hiswill.babybrezza_1505.WarmerOperateActivity"));
	}
	
	/**
	 * ��ʼ�����
	 */
	private void initView()
	{
		Resources resources = getResources();

		initWheelForShowCapacity();
		initWheelForShowSpeed();
		initWheelForShowTemp();

		layout_error = (LinearLayout)findViewById(R.id.layout_error);
		layout_0000 = (LinearLayout)findViewById(R.id.layout_0000);
		
		tv_counttime	= (TextView) findViewById(R.id.tv_counttime);
		tv_showstatus	= (TextView) findViewById(R.id.tv_showstatus);
		tv_info			= (TextView) findViewById(R.id.tv_info);
		tv_info.setText("");
		
		//tv_speed = (TextView) findViewById(R.id.tv_speed);
		//tv_temp = (TextView) findViewById(R.id.tv_temp);
		//tv_setting = (TextView) findViewById(R.id.tv_setting);
		
		//Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/MyriadPro-Regular.otf");
		//tv_counttime.setTypeface(tf);
		//tv_setting.setTypeface(tf);
		//tv_speed.setTypeface(tf);
		//tv_temp.setTypeface(tf);
		//tv_showstatus.setTypeface(tf);
		
		btn_start=(AutofitTextView) findViewById(R.id.btn_start);
		btn_start.setTypeface(PairingPageActivity.typeFace);
		btn_start.setOnClickListener(listener);
		
		iv_sound = (ImageView) findViewById(R.id.iv_sound);
		iv_sound.setOnClickListener(listener);
		
		iv_cap_up = (ImageView) findViewById(R.id.iv_cap_up);
		iv_cap_up.setOnClickListener(listener);
		iv_cap_down = (ImageView) findViewById(R.id.iv_cap_down);
		iv_cap_down.setOnClickListener(listener);
		
		iv_tmp_up = (ImageView) findViewById(R.id.iv_tmp_up);
		iv_tmp_up.setOnClickListener(listener);
		iv_tmp_down = (ImageView) findViewById(R.id.iv_tmp_down);
		iv_tmp_down.setOnClickListener(listener);
		
		iv_spd_up = (ImageView) findViewById(R.id.iv_spd_up);
		iv_spd_up.setOnClickListener(listener);
		iv_spd_down = (ImageView) findViewById(R.id.iv_spd_down);
		iv_spd_down.setOnClickListener(listener);
		
		tv_seting_guide = (TextView) findViewById(R.id.tv_seting_guide);
		tv_seting_guide.setOnClickListener(listener);

		//tv_back = (TextView) findViewById(R.id.tv_back);
		//tv_back.setOnClickListener(listener);

		iv_logo = (ImageView)findViewById(R.id.iv_logo);
		iv_logo.setOnClickListener(listener);
		
		test = new Test();
		dataUtils = new DataUtils();
	}

    // Wheel scrolled listener
    private OnWheelScrollListener scrolledListener = new OnWheelScrollListener() 
    {
        public void onScrollingStarted(WheelView wheel) 
        {
            //Log.i(TAG,"onScrollingStarted");
        }
        public void onScrollingFinished(WheelView wheel) 
        {
            //Log.i(TAG,"onScrollingFinished");
        	if(wheel == getWheel(R.id.wv_capacity))
        	{
        		Log.i(TAG,"capacity is changed");
        		forceToQuick = true;
        	}
        	else if(wheel == getWheel(R.id.wv_speed))
        	{
        		Log.i(TAG,"speed is changed");
        	}

            updateStatus();
        }
    };
    
    // Wheel changed listener
    private OnWheelChangedListener changedListener = new OnWheelChangedListener() 
    {
        public void onChanged(WheelView wheel, int oldValue, int newValue) 
        {
        	//Log.i(TAG,"OnWheelChangedListener");
        }
    };

    /**
     * 
     * @param id
     * @param tv
     * ���ؿؼ�
     */
    private void hideWheel(int id)
    {
    	/*
    	WheelView wheel = getWheel(id);
    	TextView TV;
    	ImageView keyUp,keyDn;
    	if(id == R.id.wv_capacity)
    	{
    		keyUp = (ImageView) findViewById(R.id.iv_cap_up);
    		keyDn = (ImageView) findViewById(R.id.iv_cap_down);
    		TV    = tv_setting;
    	}
    	else if(id == R.id.wv_speed)
    	{
    		keyUp = (ImageView) findViewById(R.id.iv_spd_up);
    		keyDn = (ImageView) findViewById(R.id.iv_spd_down);
    		TV    = tv_speed;
    	}
    	else// if(id == R.id.wv_temp)
    	{
    		keyUp = (ImageView) findViewById(R.id.iv_tmp_up);
    		keyDn = (ImageView) findViewById(R.id.iv_tmp_down);
    		TV    = tv_temp;
    	}
    	
    	keyUp.setVisibility(View.INVISIBLE);
    	keyDn.setVisibility(View.INVISIBLE);
    	wheel.setVisibility(View.INVISIBLE);
    	TV.setVisibility(View.INVISIBLE);
    	//*/
    }
    
    /**
     *
     * @param id
     * @param tvSpeed
     * @param tvTemp
     *  ��ʾ�ؼ�
     */
    private void showWheel(int id)
    {
    	/*
    	WheelView wheel = getWheel(id);

    	TextView TV;
    	ImageView keyUp,keyDn;
    	if(id == R.id.wv_capacity)
    	{
    		keyUp = (ImageView) findViewById(R.id.iv_cap_up);
    		keyDn = (ImageView) findViewById(R.id.iv_cap_down);
    		TV    = tv_setting;
    	}
    	else if(id == R.id.wv_speed)
    	{
    		keyUp = (ImageView) findViewById(R.id.iv_spd_up);
    		keyDn = (ImageView) findViewById(R.id.iv_spd_down);
    		TV    = tv_speed;
    	}
    	else// if(id == R.id.wv_temp)
    	{
    		keyUp = (ImageView) findViewById(R.id.iv_tmp_up);
    		keyDn = (ImageView) findViewById(R.id.iv_tmp_down);
    		TV    = tv_temp;
    	}
    	
    	keyUp.setVisibility(View.VISIBLE);
    	keyDn.setVisibility(View.VISIBLE);
    	wheel.setVisibility(View.VISIBLE);
    	TV.setVisibility(View.VISIBLE);
    	//*/
    }
    
    /**
     * ��ʼ����ʾ����
     */
    private void UpdateSetting()
    {
    	String strTmp;
    	Log.i(TAG,"UpdateSetting");
    	
    	capacity = getWheel(R.id.wv_capacity).getCurrentItem()+1;
		
    	modifySpdAndTempWheel();
    	/*
    	strTmp = numericWheelAdapterCapacity.getItem(getWheel(R.id.wv_capacity).getCurrentItem());
    	if(strTmp.equals("defrost"))
    	{
    		getWheel(R.id.wv_speed).setCurrentItem(0);
    		getWheel(R.id.wv_temp).setCurrentItem(1);
    	}
    	//*/
    	
    	strTmp = numericWheelAdapterSpeed.getItem(getWheel(R.id.wv_speed).getCurrentItem());
    	//Log.i(TAG,"speed string="+strTmp);
    	if(strTmp.equals(getString(R.string.steady)))	 	speedModel = SPEED_STEADY;
    	else				    		speedModel = SPEED_QUICK;
    	
    	strTmp = numericWheelAdapterTemp.getItem(getWheel(R.id.wv_temp).getCurrentItem());
    	//Log.i(TAG,"temp string="+strTmp);
    	if(strTmp.equals(getString(R.string.room)))    	workingModel = TEMP_ROOM;
    	else    						workingModel = TEMP_COLD;
    	
		if(speedModel==SPEED_STEADY)
		{
			hideWheel(R.id.wv_capacity);
			hideWheel(R.id.wv_temp);
		   	//removeWheelListener(R.id.wv_capacity);
		   	//removeWheelListener(R.id.wv_temp);
		}
		else
		{
			showWheel(R.id.wv_capacity);
			showWheel(R.id.wv_temp);
		   	//addWheelListener(R.id.wv_capacity);
		   	//addWheelListener(R.id.wv_temp);
		}
		
		int bakCap,bakTmp,bakSpd;
		bakCap 	= mSettings.getInt("set_capacity",-1);
		bakTmp 	= mSettings.getInt("set_workingModel",-1);
		bakSpd 	= mSettings.getInt("set_speedModel",-1);
		if((bakCap != capacity) || (bakTmp != workingModel) || (bakSpd != speedModel))
		{
			storeSettings();
		}
    }

    /**
     * Updates entered PIN status;
     * ������ʾ����
     */
    private void updateStatus()
    {
    	Log.i(TAG, "updateStatus��");
    	UpdateSetting();
		initWorkingTime();

    	if(hw1505BleComm.bCommuicatedOk == false) return;
		if((SettingStatus == STATE_POWEROFF) || (SettingStatus == STATE_UNKNOWN))
			SettingStatus = STATE_POWERON;
    	Log.i(TAG, "SettingStatus = STATE_POWERON");

        if((capacityBak != capacity) || (speedModelBak != speedModel) || (workingModelBak != workingModel))
        {
        	capacityBak		= capacity;
        	speedModelBak	= speedModel;
        	workingModelBak = workingModel;
        	sendData((byte)0x03, (byte)0x00,(byte)0x01, (byte)0x00, (byte)0x00, (byte)SettingStatus);
        }
    }
    
    
    /**
     * ��������
     */
    private void sendData(byte command,byte temp,byte version,byte save,byte save_t,byte status)
    {
    	byte[] data;
		Log.i(TAG, "��������  ����="+command+"  ����="+capacity+"  �ٶ�="+speedModel+"  �¶�="+workingModel+"  ״̬="+status);
    	
    	//data = dataUtils.reayTXData((byte)command, (byte)hour, (byte)minute, (byte)second, (byte)capacity, (byte)speedModel, (byte)workingModel, (byte)temp, (byte)version, (byte)save, (byte)save_t, (byte)status);
    	data = dataUtils.reayTXData((byte)command, (byte)0, (byte)0, (byte)0, (byte)capacity, (byte)speedModel, (byte)workingModel, (byte)temp, (byte)version, (byte)save, (byte)save_t, (byte)status);
    	hw1505BleComm.mBleService.uiWriteData(data);
    }
    
	Runnable mRunnable = new Runnable()
	{
		@Override
		public void run()
		{
			handlerRunning = true;
			
			if((1000/TimerInterval)*TimerInterval != 1000)
			{
				Log.e(TAG,"TimerInterval setting error!");
				TimerInterval = 1000;
			}
			
			Cnt1s++;
			
			if(Cnt1s >= (1000/TimerInterval))
			{
				Cnt1s = 0;
				if((DeviceStatus == STATE_WORKING) && (WorkState == STATE_COUNTING)) 
				{
					/*
					if(speedModel == SPEED_STEADY)
					{
						CountTimeUp();
						Log.i(TAG, "APP ����ʱ:   "+AppHour+":"+AppMinute+":"+AppSecond);
					}
					else*/
					{
						CountTimeDown();
						//Log.i(TAG, "APP ����ʱ:   "+AppHour+":"+AppMinute+":"+AppSecond);
					}
					
					showTime(AppHour, AppMinute, AppSecond);
					ChkWorkingState(AppHour, AppMinute, AppSecond);
				}
			}
			mHandler.postDelayed(this, TimerInterval);
		}
	};
	
	//����ʱ
	private void CountTimeUp()
	{
		if((AppHour == 99) && (AppMinute == 59) && (AppSecond == 59)) return;
		
		AppSecond++; 
		if(AppSecond>59)
		{
			AppSecond = 0;
			AppMinute++;
			
			if(AppMinute>59)
			{
				AppMinute = 0;
				
				AppHour++;
				if(AppHour > 99) AppHour = 99;
			}
		}
	}
	
	//����ʱ
	private void CountTimeDown()
	{
		if((AppHour | AppMinute | AppSecond)==0 ) return;
		
		AppSecond--; 
		if(AppSecond<0)
		{
			AppSecond = 59;
			
			AppMinute--;
			if(AppMinute<0)
			{
				AppMinute = 59;
				
				AppHour--;
				if(AppHour < 0) AppHour = 0;
			}
		}
	}
	

	Runnable SendInitData = new Runnable()
	{
		@Override
		public void run() 
		{
			if(WorkState == STATE_SETTING)
			{
				Log.w(TAG, "delay to initial");
				updateStatus();
				
			}
		}
	};
	
	/**
	 * Activity ��ת
	 */	//
	private void ReturnToPairingPage()
	{
		Log.i(TAG,"Close & back to PairingPageActivity");
		hw1505BleComm.mBleService.uiClose();
		try
		{
			Thread.sleep(500);
		}catch(InterruptedException e)
		{
			e.printStackTrace();
		}

		Log.i(TAG,"intent to PairingPageActivity");
		Intent intent = new Intent(WarmerOperateActivity.this,PairingPageActivity.class);
		/*
		mSettingEditor.putBoolean("WarmerOperateActivity", false);
		mSettingEditor.commit();
		*/
		startActivity(intent);
		finish();
	}
	
	private OnClickListener listener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			switch(v.getId())
			{
				//v0.4.0
				//case R.id.tv_back:
				//	ReturnToPairingPage();
				//	break;

				case R.id.iv_sound:
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
					break;
					
				case R.id.btn_start:
					String Button_String;
					Button_String = btn_start.getText().toString();
					
					//Shader shader =new LinearGradient(0, 0, 0, 20, Color.BLACK, Color.GRAY, Shader.TileMode.CLAMP);
					//btn_start.getPaint().setShader(shader);

					if(Button_String.equals(getString(R.string.btn_start)))
					{
						Log.i(TAG, "����start");	
						
						if(hw1505BleComm.bCommuicatedOk == false) break;
						
						int settingtime = ShowHour+ShowMinute+ShowSecond;
						
						//if(((speedModel == SPEED_QUICK) && (settingtime >  0 )) || 
						//   ((speedModel == SPEED_STEADY)&& (settingtime == 0)))
						if(settingtime > 0)
						{
							//Log.i(TAG, "����start: ��������" );	
							SettingStatus = STATE_WORKING;
							sendData((byte)0x03, (byte)0x00,(byte)0x01, (byte)0x00, (byte)0x00, (byte)SettingStatus);
							DisableListener();
						}
					}
					else if(Button_String.equals(getString(R.string.cancel)))
					{
						Log.i(TAG, "����cancel");
						//mHandler.removeCallbacks(mRunnable);
						SettingStatus = STATE_POWERON;

						initWorkingTime();
						sendData((byte)0x02, (byte)0x00,(byte)0x01, (byte)0x00, (byte)0x00, (byte)SettingStatus);
						//EnableListener();
						
						//added @2016-6-23
						//send data to initialize device
						new Handler().postDelayed(new Runnable()
						{
							@Override
							public void run() {
								sendData((byte)0x03, (byte)0x00,(byte)0x01, (byte)0x00, (byte)0x00, (byte)SettingStatus);
							}}, 800);
						//send once more
						new Handler().postDelayed(new Runnable()
						{
							@Override
							public void run() {
								sendData((byte)0x03, (byte)0x00,(byte)0x01, (byte)0x00, (byte)0x00, (byte)SettingStatus);
								EnableListener();
							}}, 1200);
					}
					break;
					
				case R.id.iv_cap_up:
					WheelKeyUpDown(R.id.wv_capacity, 1 ,ItemCapacity.length,1);
					updateStatus();
					break;
				case R.id.iv_cap_down:
					WheelKeyUpDown(R.id.wv_capacity, -1,ItemCapacity.length,1);
					updateStatus();
					break;
				case R.id.iv_tmp_up:
					WheelKeyUpDown(R.id.wv_temp, 1,tmpItems,0);
					updateStatus();
					break;
				case R.id.iv_tmp_down:
					WheelKeyUpDown(R.id.wv_temp, -1,tmpItems,0);
					updateStatus();
					break;
				case R.id.iv_spd_up:
					WheelKeyUpDown(R.id.wv_speed, 1,spdItems,0);
					updateStatus();
					break;
				case R.id.iv_spd_down:
					WheelKeyUpDown(R.id.wv_speed, -1,spdItems,0);
					updateStatus();
					break;
				case R.id.tv_seting_guide:
					Intent intent = new Intent(WarmerOperateActivity.this,SettiingGuideActivity.class);
					intent.putExtra("Parent Activity", "WarmerOperateActivity");
					startActivity(intent);
					break;
					
				case R.id.iv_logo:
					ClickCnt++;
					if(ClickCnt == 1) 
					{
						firClick = (int) System.currentTimeMillis();
						break;
					}
					secClick = (int) System.currentTimeMillis();
					Log.e(TAG,"click iv_logo:"+firClick+", "+secClick);
					
					if (secClick - firClick < 300)
					{
						//double click
						Log.e(TAG,"Double click, test BottleIsReadyActivity");
						Intent i = new Intent(WarmerOperateActivity.this,BottleIsReadyActivity.class);
						startActivity(i);
					}
					
					ClickCnt = 0;
				break;
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
		mp=MediaPlayer.create(WarmerOperateActivity.this, R.raw.ding);
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
	
	private void WheelKeyUpDown(int id, int step, int items, int cycle)
	{
		WheelView wheelView = getWheel(id);
		int index = wheelView.getCurrentItem();

		//index = Integer.parseInt(numericWheelAdapterCapacity.getItem(getWheel(id).getCurrentItem()));
		/*
		NumericWheelAdapter wheelptr = null;
		if(id == R.id.wv_capacity)	wheelptr = numericWheelAdapterCapacity;
		if(id == R.id.wv_speed)		wheelptr = numericWheelAdapterSpeed;
		if(id == R.id.wv_temp)		wheelptr = numericWheelAdapterTemp;
		String temp = wheelptr.getItem(index);
		Log.i(TAG, "string :" +temp+" index:"+index);
		//*/
		
		index += step;
		if(cycle>0)
		{
			if(index<0)			index=items-1;
			if(index>=items)	index=0;
		}
		else
		{
			if(index<0)			index=0;
			if(index>=items)	index=items-1;
		}
		Log.i(TAG, "new index: "+index);
		wheelView.setCurrentItem(index);
		
		UpdateSetting();
	}
	
		
	/**
	 * ��ʾʱ�����
	 * @param id
	 */
	String[] ItemCapacity=
	{
		"1", "2", "3", "4", "5", "6", "7", "8", "9", "defrost"
	};
	
	private void initWheelForShowCapacity()
	{
		List<String> showStrListData = new ArrayList<String>();
		//showStrListData.removeAll(showStrListData);
		
		for(int i=0; i<ItemCapacity.length; i++)
		{
			showStrListData.add(ItemCapacity[i]);
		}

		capItems = ItemCapacity.length;
		
		WheelView wheel = getWheel(R.id.wv_capacity);
		numericWheelAdapterCapacity = new NumericWheelAdapter(1, 10000, true, showStrListData);
		numericWheelAdapterCapacity.showStrOrInt=true;
		wheel.setAdapter(numericWheelAdapterCapacity);
		wheel.setCyclic(true);
		wheel.setInterpolator(new AnticipateOvershootInterpolator());
		//wheel.setAlpha((float) 0.5);
		wheel.showContextMenu();

		//wheel.setCurrentItem(0);
		wheel.setCurrentItem(capacity-1);
		wheel.setVisibility(View.INVISIBLE);
		
		wheel.addChangingListener(changedListener);
		wheel.addScrollingListener(scrolledListener);
	}
	
	/**
	 * 
	 * �趨�ٶ�ģʽ��ʾ����
	 */
	private void initWheelForShowSpeed()
	{
		List<String> showStrListData = new ArrayList<String>();
		//showStrListData.removeAll(showStrListData);
		//Log.i(TAG,"initWheelForShowSpeed:"+items);
		
		spdItems = 2;
		showStrListData.add(getString(R.string.steady));
		showStrListData.add(getString(R.string.quick));
		
		WheelView wheel = getWheel(R.id.wv_speed);
		numericWheelAdapterSpeed = new NumericWheelAdapter(1000, 10000, true, showStrListData);
		numericWheelAdapterSpeed.showStrOrInt=true;
		wheel.setAdapter(numericWheelAdapterSpeed);

		wheel.setCurrentItem(0);
		if((speedModel==SPEED_QUICK))			wheel.setCurrentItem(1);
		//else if((speedModel==SPEED_STEADY))		wheel.setCurrentItem(0);
			
		wheel.setCyclic(false);
		wheel.setInterpolator(new AnticipateOvershootInterpolator());
		wheel.setVisibility(View.INVISIBLE);

		wheel.addChangingListener(changedListener);
		wheel.addScrollingListener(scrolledListener);
	}
	
	
	/**
	 * 
	 * �趨�¶�ģʽ��ʾ����
	 */
	private void initWheelForShowTemp()
	{
		List<String> showStrListData = new ArrayList<String>();
		//showStrListData.removeAll(showStrListData);
		//Log.i(TAG,"initWheelForShowTemp:"+items);

		tmpItems = 2;
		showStrListData.add(getString(R.string.room));
		showStrListData.add(getString(R.string.cold));
		
		WheelView wheel = getWheel(R.id.wv_temp);
		numericWheelAdapterTemp = new NumericWheelAdapter(1000, 10000, true, showStrListData);
		numericWheelAdapterTemp.showStrOrInt=true;
		wheel.setAdapter(numericWheelAdapterTemp);
		
		wheel.setCurrentItem(0);
		if((workingModel==(byte)TEMP_COLD))			wheel.setCurrentItem(1);
		//else if((workingModel==(byte)TEMP_ROOM))	wheel.setCurrentItem(0);
		
		wheel.setCyclic(false);
		wheel.setInterpolator(new AnticipateOvershootInterpolator());
		wheel.setVisibility(View.INVISIBLE);
		wheel.addChangingListener(changedListener);
		wheel.addScrollingListener(scrolledListener);
	}
	

	/**
	 * ��������defrost�����߷�defrost�������¶Ⱥ��ٶȹ���
	 * 2016-4-20 ���л���OK
	 */
	private void modifySpdAndTempWheel()
	{
		WheelView wheelCap, wheelTmp, wheelSpd;
    	ImageView keyUp,keyDn;

    	wheelCap = getWheel(R.id.wv_capacity);
    	wheelTmp = getWheel(R.id.wv_temp);
    	wheelSpd = getWheel(R.id.wv_speed);
    	
		//wheelSpd.setVisibility(View.INVISIBLE);
		//wheelTmp.setVisibility(View.INVISIBLE);    	
    	String strTmp = numericWheelAdapterCapacity.getItem(wheelCap.getCurrentItem());
		
		if(strTmp.equals("defrost"))
		{
			//if(setDefrost == false)
			{
				defrostSetting = true;
				
				Log.i(TAG,"����defrost,�����ٶȺ��¶�");
				numericWheelAdapterSpeed.showStrListData.clear();
				numericWheelAdapterSpeed.showStrListData.add(0, getString(R.string.steady));
				numericWheelAdapterSpeed.showStrListData.add(1, " ");
				wheelSpd.setCurrentItem(1);
				wheelSpd.setCurrentItem(0);
				wheelSpd.setEnabled(false);
				//wheelSpd.invalidate();
				//removeWheelListener(R.id.wv_speed);
				//wheelSpd.forceRefresh();/
				//wheelSpd.notifyAll();		//smart phone will stop running
				//wheelSpd.refreshDrawableState();
				//numericWheelAdapterSpeed.notifyAll();
	
				numericWheelAdapterTemp.showStrListData.clear();
				numericWheelAdapterTemp.showStrListData.add(0, " ");
				numericWheelAdapterTemp.showStrListData.add(1, getString(R.string.cold));
				wheelTmp.setCurrentItem(0);
				wheelTmp.setCurrentItem(1);
				wheelTmp.setEnabled(false);
				//removeWheelListener(R.id.wv_temp);
				//wheelTmp.forceRefresh();
				//wheelTmp.invalidate();
				//wheelTmp.notifyAll();
				//wheelTmp.refreshDrawableState();
				//numericWheelAdapterTemp.notifyAll();
				
	    		keyUp = (ImageView) findViewById(R.id.iv_spd_up);
	    		keyDn = (ImageView) findViewById(R.id.iv_spd_down);
	    		keyUp.setVisibility(View.INVISIBLE);
	    		keyDn.setVisibility(View.INVISIBLE);
	    		
	    		keyUp = (ImageView) findViewById(R.id.iv_tmp_up);
	    		keyDn = (ImageView) findViewById(R.id.iv_tmp_down);
	    		keyUp.setVisibility(View.INVISIBLE);
	    		keyDn.setVisibility(View.INVISIBLE);
			}
		}
		else
		{
			if((defrostSetting == true) || (forceToQuick == true))
			{
				numericWheelAdapterSpeed.showStrListData.clear();
				numericWheelAdapterSpeed.showStrListData.add(0, getString(R.string.steady));
				numericWheelAdapterSpeed.showStrListData.add(1, getString(R.string.quick));
				wheelSpd.setCurrentItem(0);
				wheelSpd.setCurrentItem(1);
				wheelSpd.setEnabled(true);
				//addWheelListener(R.id.wv_speed);
			}
			
			if(defrostSetting == true)
			{
				numericWheelAdapterTemp.showStrListData.clear();
				numericWheelAdapterTemp.showStrListData.add(0, getString(R.string.room));
				numericWheelAdapterTemp.showStrListData.add(1, getString(R.string.cold));
				wheelTmp.setCurrentItem(0);
				wheelTmp.setCurrentItem(1);
				wheelTmp.setEnabled(true);
				//addWheelListener(R.id.wv_temp);
			
	    		keyUp = (ImageView) findViewById(R.id.iv_spd_up);
	    		keyDn = (ImageView) findViewById(R.id.iv_spd_down);
	    		keyUp.setVisibility(View.VISIBLE);
	    		keyDn.setVisibility(View.VISIBLE);
	    		
	    		keyUp = (ImageView) findViewById(R.id.iv_tmp_up);
	    		keyDn = (ImageView) findViewById(R.id.iv_tmp_down);
	    		keyUp.setVisibility(View.VISIBLE);
	    		keyDn.setVisibility(View.VISIBLE);
			}

			defrostSetting = false;
			forceToQuick   = false;
		}

		//wheelSpd.setVisibility(View.VISIBLE);
		//wheelTmp.setVisibility(View.VISIBLE);
	}

	
	
	/**
	 * �Ƴ����ּ���
	 * @param id
	 */
	private void removeWheelListener(int id)
	{
		WheelView wheel = getWheel(id);
		//wheel.setEnabled(false);
		wheel.removeChangingListener(changedListener);
		wheel.removeScrollingListener(scrolledListener);
	}
			
	/**
	 * 
	 * @param id
	 */
	private void addWheelListener(int id)
	{
		 WheelView wheel = getWheel(id);
		 //wheel.setEnabled(true);
		 wheel.addChangingListener(changedListener);
		 wheel.addScrollingListener(scrolledListener);
	}

	private void EnableListener()
	{
		//getWheel(R.id.wv_capacity).setEnabled(true);
		//getWheel(R.id.wv_temp).setEnabled(true);
		//getWheel(R.id.wv_speed).setEnabled(true);
		//showWheel(R.id.wv_speed		, 	tv_speed);
		//showWheel(R.id.wv_temp		, 	tv_temp);
		//showWheel(R.id.wv_capacity	, 	tv_setting);
		
		addWheelListener(R.id.wv_capacity);
		addWheelListener(R.id.wv_speed);
		addWheelListener(R.id.wv_temp);
		
		iv_cap_up.setEnabled(true);			//setClickable(true);
		iv_cap_down.setEnabled(true);			//setClickable(true);
		iv_tmp_up.setEnabled(true);			//setClickable(true);
		iv_tmp_down.setEnabled(true);		//setClickable(true);
		iv_spd_up.setEnabled(true);			//setClickable(true);
		iv_spd_down.setEnabled(true);		//setClickable(true);
	}
	private void DisableListener()
	{
		//getWheel(R.id.wv_capacity).setEnabled(false);
		//getWheel(R.id.wv_temp).setEnabled(false);
		//getWheel(R.id.wv_speed).setEnabled(false);
		
		removeWheelListener(R.id.wv_capacity);
		removeWheelListener(R.id.wv_speed);
		removeWheelListener(R.id.wv_temp);

		iv_cap_up.setEnabled(false);			//setClickable(false);
		iv_cap_down.setEnabled(false);		//setClickable(false);
		iv_tmp_up.setEnabled(false);			//setClickable(false);
		iv_tmp_down.setEnabled(false);		//setClickable(false);
		iv_spd_up.setEnabled(false);			//setClickable(false);
		iv_spd_down.setEnabled(false);		//setClickable(false);
	}
	
	private void settingEnable(boolean enable)
	{
		WheelView wheelCap, wheelTmp, wheelSpd;
    	ImageView keyUp,keyDn;

		keyUp = (ImageView) findViewById(R.id.iv_cap_up);
		keyDn = (ImageView) findViewById(R.id.iv_cap_down);
		keyUp.setEnabled(enable);
		keyDn.setEnabled(enable);

    	wheelCap = getWheel(R.id.wv_capacity);
    	wheelTmp = getWheel(R.id.wv_temp);
    	wheelSpd = getWheel(R.id.wv_speed);

    	wheelCap.setEnabled(enable);
    	String strTmp = numericWheelAdapterCapacity.getItem(wheelCap.getCurrentItem());
		if(strTmp.equals("defrost"))
		{
			enable = false;
		}
    	wheelTmp.setEnabled(enable);
		wheelSpd.setEnabled(enable);

		keyUp = (ImageView) findViewById(R.id.iv_spd_up);
		keyDn = (ImageView) findViewById(R.id.iv_spd_down);
		keyUp.setEnabled(enable);
		keyDn.setEnabled(enable);
		
		keyUp = (ImageView) findViewById(R.id.iv_tmp_up);
		keyDn = (ImageView) findViewById(R.id.iv_tmp_down);
		keyUp.setEnabled(enable);
		keyDn.setEnabled(enable);
	}

			
	/**
	 * 
	 * @param id
	 * @return
	 */
	private WheelView getWheel(int id) 
	{
		return (WheelView) findViewById(id);
	}

	/**
	 * 
	 * @param status
	 */
	private void showStatus(int Status)
	{
		tv_showstatus.setVisibility(View.VISIBLE);
		//tv_showstatus.setTextColor(Color.BLUE);
		
		if(mHw1505BleComm != null)
		{
			if(mHw1505BleComm.ChkSysBluetoothState() == false)
			{
				tv_showstatus.setText(R.string.disconnect);
				//tv_info.setText("System Bluetooth is turned off");
				//tv_info.setVisibility(View.VISIBLE);
				return;
			}
		}
		
		tv_info.setVisibility(View.INVISIBLE);
		
		String str_status = getString(R.string.disconnect);
		if(hw1505BleComm.bCommuicatedOk == true)
		{
			tv_info.setText(strStatusTab[Status]);
			//tv_info.setVisibility(View.VISIBLE);
			
			str_status = getString(R.string.connected);
			/*
			if(Status == STATE_UNKNOWN)
			{
				byte VerH,VerL;
				byte BleVersion = hw1505BleComm.BleVersion;
				if(BleVersion!=(byte)0xFF)
				{
					VerH = (byte)((BleVersion&0xF0)>>4);
					VerL = (byte)((BleVersion&0x0F));
					str_status += "(V"+VerH+"."+VerL+")";
				}
				else
				{
					str_status += "(V?.?)";
				}
			}//*/
		}
		
		tv_showstatus.setText(str_status);
	}
	
	private void ShowStartButton(boolean start)
	{
		if(hw1505BleComm.bCommuicatedOk == false)
		{
			btn_start.setVisibility(View.INVISIBLE);
			invalidateOptionsMenu();					//else the button does not disappear on Samsung n9005
			return;
		}

		if(start == true)
		{
			btn_start.setText(R.string.btn_start);
			btn_start.setBackground(getResources().getDrawable(R.drawable.btn_bg_green));
		}
		else
		{
			btn_start.setText(R.string.cancel);
			btn_start.setBackground(getResources().getDrawable(R.drawable.btn_bg_orange));
			
		}
		btn_start.setVisibility(View.VISIBLE);
		invalidateOptionsMenu();
	}

	private void ChkWorkingState(int hour,int minute,int second)
	{
		int time_hms;
		boolean bFinish;
		showStatus(DeviceStatus);

		bFinish  = false;
		time_hms = hour+minute+second;
		
		if(WorkState == STATE_COUNTING)
		{
			//if(speedModel==SPEED_QUICK)
			{
				if(time_hms == 0)
				{
					if((WorkState != STATE_COUNDDOWNOK))// && (ifOnOperatePage() == true))
					{
						bFinish = true;
					}
					WorkState = STATE_COUNDDOWNOK;
				}
			}
			/*
			else//if(speedModel==SPEED_STEADY)
			{
				if((hour==0) && (minute==24) && (second==0))
				{
					if((WorkState != STATE_COUNDDOWNOK))// && (ifOnOperatePage() == true))
					{
						bFinish = true;
					}
				}
			}
			*/
			
			if(bFinish == true)
			{
				Log.i(TAG, "go to BottleIsReadyActivity");
				Intent intent = new Intent(WarmerOperateActivity.this,BottleIsReadyActivity.class);
				startActivity(intent);
				
				addNotofication();
				
				try
				{
					Thread.sleep(1000);
				}
				catch(InterruptedException e)
				{
					e.printStackTrace();
				}
				finish();
				
				WorkState = STATE_COUNDDOWNOK;
			}
		}
		else
		{
			if(time_hms > 0)
			{
				WorkState = STATE_COUNTING;
			}
		}
		
		if(WorkState == STATE_COUNTING)
		{
			DisableListener();
			ShowStartButton(false);
		}
		else if(WorkState == STATE_COUNDDOWNOK)
		{
			EnableListener();
			ShowStartButton(true);
		}
	}
	
	
	/**
	 * ������ʾ��ʱ��
	 * 
	 */
	private void showTime(int hour,int minute,int second)
	{
		String time;
		
		ShowHour 	= hour;
		ShowMinute 	= minute;
		ShowSecond 	= second;
		
		if(ShowHour==(byte)0x00 && ShowMinute==(byte)0x00 && ShowSecond==(byte)0x00)
		{
			time="00:00";
		}
		else
		{
			time = "";

			//if(ShowHour<10)time +="0"+ShowHour;
			//else time += ShowHour;
			//time += ":";
			if(ShowMinute<10)time +="0"+ShowMinute;
			else time += ShowMinute;
			time += ":";
			if(ShowSecond<10)time +="0"+ShowSecond;
			else time += ShowSecond;
		}
		tv_counttime.setText(time);
	}
	
	/**
	 * �㲥�����ߣ���������״̬
	 */
	/*
	01	�ػ�
	02	����������
	03	����������
	04	������1��·����
	05	������1��·����
	06	������2��·����
	07	������3��·����
	*/
	String[] strStatusTab = 
	{
		"",
		" Warmer is power off",
		" Warmer is power on",
		" Warmer is working",
		" The 1st sensor is open!",
		" The 1st sensor is short!",
		" The 2nd sensor is open!",
		" The 2nd sensor is short!",
		" No water!",
		" TBD error:",
	};
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
	    if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
	    {   
			if((System.currentTimeMillis()-exitTime) > 2000){  
	            Toast.makeText(getApplicationContext(), getString(R.string.exit_program), Toast.LENGTH_SHORT).show();                                
	            exitTime = System.currentTimeMillis();   
	        } else 
	        {
	            finish();
	            System.exit(0);
	        }
	        return true;   
	    }
	    return super.onKeyDown(keyCode, event);

		//ReturnToPairingPage();
		
		/* 
		//�������ɲ�����������
		Intent intent = new Intent(this,WarmerOperateActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		intent.addCategory(Intent.CATEGORY_HOME);
		startActivity(intent);
		//*/

		/* YZ's old code
		//���ﷵ�������棬���ǵ��app icon�����½���ɨ��
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addCategory(Intent.CATEGORY_HOME);
		startActivity(intent);
		//*/
	}



	/*
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(20==resultCode)
		{
			UpdateSetting();
			Log.i(TAG, "UpdateSetting()");
		}
	}
	*/

	//
	private void restoreSettings()
	{
		
		int default_cap,default_temp,default_spd;
		default_cap = 4;			//ItemCapacity.length;
		default_temp= TEMP_ROOM;	//TEMP_COLD
		default_spd = SPEED_QUICK;	//SPEED_STEADY
		
		/*
		String Address = mSettings.getString("set_DeviceAddr","");	
        Log.i(TAG, "Check if new device"); 
		if(Address.equals(mDeviceAddress))
		{
			Log.i(TAG, "restore preview device's settings"); 
    		capacity	= mSettings.getInt("set_capacity",default_cap);
    		speedModel	= mSettings.getInt("set_speedModel",default_spd);
    		workingModel= mSettings.getInt("set_workingModel",default_temp);
		}
		else
		*/
		{	
			Log.i(TAG, "initialize setting"); 
			capacity	= default_cap;
			speedModel	= default_spd;
			workingModel= default_temp;
		}
		SoundOn = mSettings.getInt("set_sound",1);
		
		Log.i(TAG, "restoreSettings: " + capacity + "," + workingModel + "," + speedModel);
	}
	

	private void storeSettings()
	{
		Log.i(TAG, "storeSettings: " + capacity + "," + workingModel + "," + speedModel);
		mSettingEditor.putInt("set_capacity", capacity);
		mSettingEditor.putInt("set_workingModel", workingModel);
		mSettingEditor.putInt("set_speedModel", speedModel);
		mSettingEditor.putInt("set_sound", SoundOn);
		mSettingEditor.commit();
	}

	
	/** 
     * ��ĳ��activity��á����ס���ϵͳ����ʱ����activity��onSaveInstanceState�ͻᱻִ�У� 
     * ���Ǹ�activity�Ǳ��û��������ٵģ����統�û���BACK����ʱ�� 
     * һ��ԭ�򣺼���ϵͳ��δ������ɡ�ʱ���������activity����onSaveInstanceState�ᱻϵͳ���� 
     * �龰�� 
     * 1. ���û�����HOME��ʱ 
     * 2. ����HOME����ѡ�����������ĳ���ʱ�� 
     * 3. ���µ�Դ�������ر���Ļ��ʾ��ʱ�� 
     * 4. ��activity A������һ���µ�activityʱ�� 
     * 5. ��Ļ�����л�ʱ������������л�������ʱ�� 
     * �����龰�����ú��������ҿ����߿��Ա���һЩ����״̬ 
     */  
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		Log.i(TAG,"onSaveInstanceState()");
		//*
		//outState.putString("save_mDeviceAddress", mDeviceAddress);
		outState.putInt("save_capacity", capacity);
		outState.putInt("save_speedModel", speedModel);
		outState.putInt("save_workingModel", workingModel);
		//*/
	}
	

    /** 
     * onSaveInstanceState������onRestoreInstanceState��������һ�����ǳɶԵı����õģ� 
     * onRestoreInstanceState�����õ�ǰ���ǣ� 
     * activity A��ȷʵ����ϵͳ�����ˣ������������ͣ���������ֿ����Ե�����£� 
     * ��÷������ᱻ���ã����磬��������ʾactivity A��ʱ���û�����HOME���ص������棬 
     * Ȼ���û��������ַ��ص�activity A�����������activity Aһ�㲻����Ϊ�ڴ��ԭ��ϵͳ���٣� 
     * ��activity A��onRestoreInstanceState�������ᱻִ�� 
     */  
    @Override     
    public void onRestoreInstanceState(Bundle savedInstanceState) 
    {     
    	super.onRestoreInstanceState(savedInstanceState);   
        Log.d(TAG, "onRestoreInstanceState()");  
        //*
	  	capacity = savedInstanceState.getInt("save_capacity");
	  	speedModel = savedInstanceState.getInt("save_speedModel");
	  	workingModel = savedInstanceState.getInt("save_workingModel");
	  	//mDeviceAddress = savedInstanceState.getString("save_mDeviceAddress");
	  	//*/
    }
    
	
	private void refreshView()
	{
		//"defrost"&"steady" is displayed right after delay, why
		//new Handler().postDelayed(new Runnable()
		//{    
			//public void run() 
			{
				/*
				WheelView wheel;
				
				//WheelKeyUpDown(R.id.wv_capacity, 1,capItems,1);
				//WheelKeyUpDown(R.id.wv_capacity, -1,capItems,1);
				wheel = getWheel(R.id.wv_capacity);
				wheel.setCurrentItem(capacity&1);
				wheel.setCurrentItem(capacity-1);
				Log.i("capacity",capacity + " " + (capacity&1) + " " + (capacity-1));
				wheel.setVisibility(View.VISIBLE);
				wheel.forceRefresh();
				
				//WheelKeyUpDown(R.id.wv_temp, 1,tmpItems,1);
				//WheelKeyUpDown(R.id.wv_temp, -1,tmpItems,1);
				wheel = getWheel(R.id.wv_temp);
				wheel.setCurrentItem(workingModel & (int)0x1);
				wheel.setCurrentItem(workingModel-1);
				//wheel.forceRefresh();
				wheel.setVisibility(View.VISIBLE);

				//WheelKeyUpDown(R.id.wv_speed, 1,spdItems,1);
				//WheelKeyUpDown(R.id.wv_speed, -1,spdItems,1);
				wheel = getWheel(R.id.wv_speed);
				if(speedModel==SPEED_QUICK)			
				{
					wheel.setCurrentItem(0);
					wheel.setCurrentItem(1);
				}
				else	
				{
					wheel.setCurrentItem(1);
					wheel.setCurrentItem(0);
				}
				wheel.setVisibility(View.VISIBLE);
				wheel.forceRefresh();
				*/

				
				UpdateSetting();
				getWheel(R.id.wv_capacity).setVisibility(View.VISIBLE);
				getWheel(R.id.wv_temp).setVisibility(View.VISIBLE);
				getWheel(R.id.wv_speed).setVisibility(View.VISIBLE);
				EnableListener();
				showStatus(0);
				ShowStartButton(true);
			}
			
		//}, 100);
	}
    @SuppressWarnings("deprecation")
	private void addNotofication()
    {
    	//��������
    	//��ʼ�����������
    	AudioManager mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
    	
    	//������� & ��ǰ����
    	int maxVolume =mAudioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
    	int currentVolume =mAudioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
    	//Log.i(TAG,"maxVolume="+maxVolume+", currentVolume="+currentVolume);
    	
    	//(2016-4-18)
    	//����������������Ч (audioStreamType�����õ������)
    	//��G7105�ϲ�����֪ͨ�������ƣ�currentVolume�����������������û������ֻ��
    	mAudioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, currentVolume, 0);
    	
    	NotificationManager nf = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
    	Notification notification = new Notification();
    	//icon of status bar
    	notification.icon 		= R.drawable.ic_launcher;
    	//content of status bar
    	notification.tickerText = "Bottle is ready!";
    	
        // * notification.contentIntent:һ��PendingIntent���󣬵��û������״̬���ϵ�ͼ��ʱ����Intent�ᱻ���� 
        // * notification.contentView:���ǿ��Բ���״̬����ͼ����Ƿ�һ��view 
        // * notification.deleteIntent ����ǰnotification���Ƴ�ʱִ�е�intent 
        // * notification.vibrate ���ֻ���ʱ������������ 
    	
    	// ���������ʾ  
		//notification.defaults=Notification.DEFAULT_SOUND;
    	notification.defaults = 0;
    	notification.sound = null;
    	if(SoundOn == 1)
    	{
    		notification.sound = Uri.parse("android.resource://" + getPackageName() + "/" +R.raw.noti_sound); 
    	}
    	else
    	{
    		Log.i(TAG,"vibrate");
    		//method 1
    		//Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    		//vibrator.vibrate(new long[]{100,100,300,100,300,100}, -1);
    		
    		//method 2
    		//notification.defaults |= Notification.DEFAULT_VIBRATE; 
    		long[] vibrate = new long[]{100,200,200,200,200,200}; 
    		notification.vibrate = vibrate;
    	}
    	
        // audioStreamType��ֵ������AudioManager�е�ֵ
    	//������� audioStreamType ֮�󣬷���������С
        //notification.audioStreamType= android.media.AudioManager.ADJUST_SAME;//ADJUST_LOWER ADJUST_RAISE ADJUST_SAME
        
        Intent intent = new Intent(WarmerOperateActivity.this,BottleIsReadyActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this,0, intent, PendingIntent.FLAG_ONE_SHOT);
        notification.setLatestEventInfo(this, "BabyBrezza", getString(R.string.bottole_is_ready), pi);
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        
        nf.notify(100, notification);
    }


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{  
		// TODO Auto-generated method stub
		Log.i(TAG,"onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.warmeroperater_page);
		Activity_Opr = this;

		//LLY @2016-4-16
		//boolean res = bindService(new Intent(this,BluetoothLeService.class), bleServiceConn, Context.BIND_AUTO_CREATE);
		//Log.e(TAG, "bindService:"+(res == true?"OK":"Failed"));
		//bReconnectEn = false;
		
		mSettings = getSharedPreferences(Constants.NAME_SP,Context.MODE_PRIVATE);	//PreferenceManager.getDefaultSharedPreferences(this);
		mSettingEditor = mSettings.edit();
		mHandler = new Handler();
		handlerToInit = new Handler();			//v0.5.3

		restoreSettings();
	}

	
	@Override
	protected void onStart()
	{
		super.onStart();
		Log.i(TAG, "onStart");
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		Log.i(TAG, "onResume");

		mDeviceName	= hw1505BleComm.getConnectedName();
		mDeviceAddress	= hw1505BleComm.getConnectedAddress();
		Log.i(TAG,"device:"+mDeviceAddress);
		Log.i(TAG,"Commuicated:"+hw1505BleComm.bCommuicatedOk);

		initView();
		if(SoundOn == 1)	iv_sound.setImageResource(R.drawable.icon_soundon);
		else				iv_sound.setImageResource(R.drawable.icon_soundoff);
		refreshView();

		capacityBak		= -1;
		speedModelBak	= -1;
		workingModelBak	= -1;
		ClickCnt = 0;
		
		if(handlerRunning == false)
			mHandler.postDelayed(mRunnable, TimerInterval);
		//invalidateOptionsMenu();
		
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
	        			public void run() 
	        			{
	        				Log.w(TAG, "WorkState="+WorkState);
	        				bChkState = true;
    						if(WorkState == STATE_COUNTING)
    						{
    							SettingStatus = STATE_WORKING;
    							ShowStartButton(false);
    						}
    						else
    						{
    							//SettingStatus	= STATE_UNKNOWN;
    							SettingStatus = STATE_POWERON;
    							ShowStartButton(true);
    							initWorkingTime();
    						}
    						showStatus(SettingStatus);
    						//showStatus(DeviceStatus);
    						
    						if(WorkState == STATE_SETTING)
    						{
    							capacityBak = -1;
    							handlerToInit.postDelayed(SendInitData, 3000);	//v0.5.3
    							//updateStatus();
    						}
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
							//tv_showstatus.setText("Error:Pls turn off system bluetooth then turn it on");
							//SettingStatus	= STATE_UNKNOWN;
							showStatus(SettingStatus);
							ShowStartButton(true);

							layout_error.setVisibility(View.GONE);
							layout_0000.setVisibility(View.VISIBLE);
						}
	        		});
	        	}

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

	    		@Override
	    		public void uiSystemBluetoothOff()
	    		{
	        		runOnUiThread(new Runnable()
	        		{
						@Override
						public void run() {
							showStatus(SettingStatus);
							//ShowStartButton(true);
						}
	        		});
	    		}
	    		
	    		@Override
	    		public void uiSystemBluetoothOn()
	    		{
	        		runOnUiThread(new Runnable()
	        		{
						@Override
						public void run() {
							showStatus(SettingStatus);
							//ShowStartButton(true);
						}
	        		});
	    		}

	    		@Override
	    		public void uiGetData(final byte rCmd, final byte rH, final byte rM, final byte rS, final byte rCap, final byte rSpd,  final byte rWM, final byte rSts)
		    	{
		    		runOnUiThread(new Runnable()
	        		{
						@Override
						public void run() 
						{
							byte rCommand = rCmd;
							byte rHour = rH;
							byte rMinute = rM;
							byte rSecond = rS;
							byte rCapacity = rCap;
							byte rSpeedModel = rSpd;
							byte rWorkingModel = rWM;
							byte rStatus = rSts;
							
							Log.i(TAG, "uiGetData");
							
							//v0.5.3
							handlerToInit.removeCallbacks(SendInitData);
							
							if((rCommand==(byte)0x05) || (rCommand==(byte)0x01) || (rCommand==(byte)0x02))
							{
								if(rStatus >= STATE_TBD_ERROR)
									rStatus = STATE_TBD_ERROR;
								
								if(rStatus >= STATE_1ST_OPEN)
								{
									layout_error.setVisibility(View.VISIBLE);
									layout_0000.setVisibility(View.GONE);
								}
								else
								{
									layout_error.setVisibility(View.GONE);
									layout_0000.setVisibility(View.VISIBLE);
								}
								
								DeviceStatus = (byte)rStatus;
								SettingStatus = DeviceStatus;
							}
							
							if(rCommand==(byte)0x05)
							{
								//������ʾ��ʱ��
								Log.i(TAG,"Device Command 5:�豸״̬");

								//v0.5.3
								if(rStatus==(byte)STATE_WORKING)
								{
									if(((rHour + rMinute + rSecond) != 0) || (bChkState == true))
									{
										WorkState = STATE_COUNTING;
										Log.w(TAG,"device STATE_WORKING");
									}
								}
								bChkState = false;
								
								AppHour 	= rHour;
								AppMinute   = rMinute;
								AppSecond   = rSecond;
			
								//������ʾ������
								WheelView capacityWheel = getWheel(R.id.wv_capacity);
								if(rCapacity>=1)	capacityWheel.setCurrentItem(rCapacity-1);
			
								//������ʾ����ģʽ
								//tempWheel.setCurrentItem(rWorkingModel-1);
								WheelView tempWheel = getWheel(R.id.wv_temp);
								if((rWorkingModel==(byte)TEMP_ROOM))		tempWheel.setCurrentItem(0);
								else if((rWorkingModel==(byte)TEMP_COLD))	
								{
									if(tmpItems == 2)	tempWheel.setCurrentItem(1);
									else				tempWheel.setCurrentItem(0);
								}
								
								//������ʾ�ٶ�ģʽ
								//speedWheel.setCurrentItem(rSpeedModel-1);
								WheelView speedWheel = getWheel(R.id.wv_speed);
								if((rSpeedModel==(byte)SPEED_STEADY))		speedWheel.setCurrentItem(0);
								else if((rSpeedModel==(byte)SPEED_QUICK))	
								{
									if(spdItems == 2)	speedWheel.setCurrentItem(1);
									else				speedWheel.setCurrentItem(0);
								}
			
								UpdateSetting();
			
								if(rStatus==(byte)STATE_WORKING)
								{
									ChkWorkingState(rHour, rMinute, rSecond);
								}
								else
								{
									Cnt1s = 0;
									showTime(rHour, rMinute, rSecond);
									if(rStatus<=7)	showStatus(DeviceStatus);
									//Public.ShowAlert("Warning","Not connected!", WarmerOperateActivity.this);
								}
								
								//ů�̻�״̬
								if(rStatus==(byte)STATE_POWEROFF)
								{
									//�ػ�
									Log.i(TAG, "Warmer is power off");
									WorkState = STATE_SETTING;
									initWorkingTime();
									
									//AppHour   = 0;
									//AppMinute = 0;
									//AppSecond = 0;
									//showTime(AppHour, AppMinute, AppSecond);
								    //DeviceStatus = STATE_POWEROFF;
									
									ShowStartButton(true);
									EnableListener();
								}
								else if(rStatus==(byte)STATE_POWERON)
								{
									//����
									Log.i(TAG, "Warmer is power on");
			
								    //DeviceStatus = STATE_POWERON;
									WorkState = STATE_SETTING;
									ShowStartButton(true);
									EnableListener();
								}
							}
							else if(rCommand==(byte)0x01)
							{
								Log.i(TAG,"Device Command 1:�豸�ػ�");
								WorkState = STATE_SETTING;
								
								//DeviceStatus = STATE_POWEROFF;
								showStatus(DeviceStatus);
								ShowStartButton(true);
								EnableListener();
							}
							else if(rCommand==(byte)0x02)
							{
								Log.i(TAG,"Device Command 2:�豸���뿪��������");
								//DeviceStatus = STATE_POWERON;
								WorkState = STATE_SETTING;
								showStatus(DeviceStatus);
								ShowStartButton(true);
								EnableListener();
								//iniHour 	= rHour;
								//iniMinute = rMinute;
								//iniSecond = rSecond;
							}
						}
					});
		    	}
	        });
        }
        hw1505BleComm.setCurrentActivity(this);
        

		if(BluetoothLeService.getConnectedState() == false)
		{
			hw1505BleComm.bQueryVersion	= false;
			hw1505BleComm.mBleService.uiConnect(WarmerOperateActivity.this, mDeviceName, mDeviceAddress);
		}
		else
		{
			/*
			if(bCommuicatedOk == false)
			{
				if(WorkState == STATE_SETTING)
				{
		    		//updateStatus();
					//SettingStatus = STATE_POWERON;
		        	//sendData((byte)0x03, (byte)0x00,(byte)0x01, (byte)0x00, (byte)0x00, (byte)SettingStatus);
				}
			}
			else
			//*/
			{
				mHw1505BleComm.startQueryVersion();
			}
		}
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		Log.i(TAG, "onPause");
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		Log.i(TAG, "onStop");
	}
	
	@Override
	protected void onDestroy()
	{
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.i(TAG, "onDestroy");
		mHw1505BleComm.ActivtiyDestroyed();

		/*
		mSettingEditor.putInt("set_capacity", -1);
		mSettingEditor.putInt("set_workingModel", -1);
		mSettingEditor.putInt("set_speedModel", -1);
		mSettingEditor.putBoolean("WarmerOperateActivity", false);
		mSettingEditor.commit();
		*/
	}


	void initWorkingTime()
	{
		int time_tbl[][];
		
		if(capacity == 10)	//10 : means defrost
		{
			AppMinute = Defrost_Time[0][0];
			AppSecond = Defrost_Time[0][1];
		}
		else
		{
			if(speedModel == SPEED_QUICK)
			{
				if(workingModel == TEMP_ROOM)
				{
					//Quick, Room
					time_tbl = QuickRoom_Time;
				}
				else
				{
					//Quick, Cold
					time_tbl = QuickCold_Time;
				}
			}
			else
			{
				if(workingModel == TEMP_ROOM)
				{
					//Steady, Room
					time_tbl = SteadyRoom_Time;
				}
				else
				{
					//Steady, Cold
					time_tbl = SteadyCold_Time;
				}
			}

			AppMinute = time_tbl[capacity-1][0];
			AppSecond = time_tbl[capacity-1][1];
		}

		
		AppHour   = 0;
		showTime(AppHour,AppMinute,AppSecond);
	}
	static final int QuickRoom_Time[][]=
	{
		{1,  55},	//1
		{2,  15},	//2
		{2,  20},	//3
		{2,  35},	//4
		{2,  50},	//5
		{3,  00},	//6
		{3,  10},	//7
		{3,  25},	//8
		{3,  40},	//9
	};

	static final int QuickCold_Time[][]=
	{
		{2,  20},	//1
		{2,  40},	//2
		{3,  00},	//3
		{3,  25},	//4
		{4,  00},	//5
		{4,  00},	//6
		{4,  20},	//7
		{4,  45},	//8
		{4,  50},	//9
	};

	static final int SteadyRoom_Time[][]=
	{
		{3,  45},	//1
		{4,  20},	//2
		{4,  30},	//3
		{5,  30},	//4
		{5,  35},	//5
		{6,  30},	//6
		{6,  50},	//7
		{8,  10},	//8
		{9,  00},	//9
	};
	
	static final int SteadyCold_Time[][]=
	{
		{3,  45},	//1
		{4,  20},	//2
		{4,  55},	//3
		{5,  20},	//4
		{5,  55},	//5
		{6,  30},	//6
		{6,  50},	//7
		{8,  10},	//8
		{9,  00},	//9
	};
	
	static final int Defrost_Time[][]=
	{
		{10,  00},
	};
}
