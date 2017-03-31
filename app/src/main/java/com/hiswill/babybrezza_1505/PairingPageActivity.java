package com.hiswill.babybrezza_1505;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
//import android.app.Dialog; 
//import android.content.SharedPreferences;
//import android.preference.PreferenceManager;

public class PairingPageActivity extends Activity {

	public static final int LANGUAGE_ENGLISH = 1;
	public static final int LANGUAGE_FRENCH = 2;

	public static String strtypeFace = "fonts/VAGRounded-Light.otf"; // MyriadPro-Regular.otf
																		// MTCORSVA.TTF
	public static Typeface typeFace;

	private String TAG = "PairingPageActivity";
	private long exitTime = 0;

	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothManager mBluetoothManager;
	private BluetoothDevice mDevice;

	private static final int REQUEST_ENABLE_BT = 1;
	private static final long SCAN_PERIOD = 20000;

	private LeDeviceListAdapter mLeDeviceListAdapter;
	private String mDeviceName;
	private String mDeviceAddress;
	private boolean mScanning;
	private Handler mHandler;

	private TextView mTvLanguageEn, mTvLanguageFr;
	private ListView lv_bleList;
	private Button btn_scan;
	private ProgressBar bar;
	private TextView tv_connectName, tv_connectAddress, tv_arrow;
	private TextView tv_version_name;
	private View myline;

	private SharedPreferences mSettings;
	private SharedPreferences.Editor mSettingEditor;

	private hw1505BleComm mHw1505BleComm;

	private Handler hGotoOperater;

	private ProgressDialog proDialog;
	boolean bTurnOnBT;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences preferences = getSharedPreferences(Constants.NAME_SP, MODE_PRIVATE);
		switchLanguage(preferences.getInt(Constants.KEY_SP_LANGUAGE, LANGUAGE_ENGLISH), false);

		Log.i(TAG, "onCreate()");
		setContentView(R.layout.pairing_page);

		// ��������
		typeFace = Typeface.createFromAsset(getAssets(), strtypeFace);

		// initialize view
		initView();

		// start BLE service
		startService(new Intent(this, BluetoothLeService.class));
		hGotoOperater = new Handler();

		// Clear the saved settings before
		// mSettings = PreferenceManager.getDefaultSharedPreferences(this);
		mSettings = getSharedPreferences("hw1505", Context.MODE_PRIVATE);
		mSettingEditor = mSettings.edit();

		updateConectionState(Color.GRAY);
	}

	private void switchLanguage(int language, boolean needResart) {
		Resources resources = getResources();
		Configuration configuration = resources.getConfiguration();
		if (language == LANGUAGE_ENGLISH) {
			configuration.locale = Locale.ENGLISH;
		} else {
			configuration.locale = Locale.FRANCE;
		}
		resources.updateConfiguration(configuration, resources.getDisplayMetrics());
		if (null != mTvLanguageEn && null != mTvLanguageFr) {
			mTvLanguageEn.setTextAppearance(this, language == LANGUAGE_ENGLISH ? R.style.textview_bold : R.style.textview_normal);
			mTvLanguageFr.setTextAppearance(this, language == LANGUAGE_FRENCH ? R.style.textview_bold : R.style.textview_normal);
		}
		SharedPreferences.Editor editor = getSharedPreferences(Constants.NAME_SP, MODE_PRIVATE).edit();
		editor.putInt(Constants.KEY_SP_LANGUAGE, language);
		editor.commit();

		if (needResart) {
			finish();
			startActivity(new Intent(PairingPageActivity.this, PairingPageActivity.class));
		}
	}

	/**
	 * ��ʼ�����
	 */
	public void initView() {
		mTvLanguageEn = (TextView) findViewById(R.id.tv_language_en);
		mTvLanguageFr = (TextView) findViewById(R.id.tv_language_fr);
		mTvLanguageEn.setOnClickListener(listener);
		mTvLanguageFr.setOnClickListener(listener);

		SharedPreferences preferences = getSharedPreferences(Constants.NAME_SP, MODE_PRIVATE);
		int language = preferences.getInt(Constants.KEY_SP_LANGUAGE, LANGUAGE_ENGLISH);
		mTvLanguageEn.setTextAppearance(this, language == LANGUAGE_ENGLISH ? R.style.textview_bold : R.style.textview_normal);
		mTvLanguageFr.setTextAppearance(this, language == LANGUAGE_FRENCH ? R.style.textview_bold : R.style.textview_normal);

		mLeDeviceListAdapter = new LeDeviceListAdapter();
		lv_bleList = (ListView) findViewById(R.id.lv_bledevice);
		lv_bleList.setOnItemClickListener(mBleDeviceOnItemClickListener);
		lv_bleList.setAdapter(mLeDeviceListAdapter);

		bar = (ProgressBar) findViewById(R.id.bar);

		btn_scan = (Button) findViewById(R.id.btn_scan);
		btn_scan.setTypeface(typeFace);
		btn_scan.setOnClickListener(listener);

		tv_connectName = (TextView) findViewById(R.id.connectname);
		tv_connectAddress = (TextView) findViewById(R.id.connectaddress);
		tv_arrow = (TextView) findViewById(R.id.arrow);

		tv_version_name = (TextView) findViewById(R.id.version_name);
		// tv_version_name.setTextColor(Color.BLUE);
		tv_version_name.setText(Public.getAppVersionName(this));

		myline = findViewById(R.id.myline);
		myline.setOnClickListener(listener);

		mHandler = new Handler();

		// initial
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		ScreenSize.scrWidth = displayMetrics.widthPixels;
		ScreenSize.scrHeight = displayMetrics.heightPixels;

		ScreenSize.SetScreenWidth(displayMetrics.widthPixels);
		ScreenSize.SetScreenHeight(displayMetrics.heightPixels);
	}

	/**
	 * �Ƿ�ǰҳ�� return true = yes, false = no
	 */
	private boolean ifOnPairingPage() {
		// *
		ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		String runningActivity = activityManager.getRunningTasks(1).get(0).topActivity.getClassName();
		Log.i(TAG, "runningActivity=" + runningActivity);

		return runningActivity.equals("com.hiswill.babybrezza_1505.PairingPageActivity");
	}

	/**
	 * ��ʼ��BLE
	 */
	private void initBLE() {
		bTurnOnBT = false;
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, "the device ble_not_supported", Toast.LENGTH_SHORT).show();
			finish();
		}

		// Initializes a Bluetooth adapter. For API level 18 and above, get a
		// reference to BluetoothAdapter through BluetoothManager.
		mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = mBluetoothManager.getAdapter();

		// Checks if Bluetooth is supported on the device.
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "error_bluetooth_not_supported", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

		if (!mBluetoothAdapter.isEnabled()) {
			if (!mBluetoothAdapter.isEnabled()) {
				RequireToTurnOnBluetooth();
			}
		}
	}

	/**
	 * ɨ��BLE�豸
	 */
	private void StartScanDevice() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (mScanning == false) {
					mScanning = true;
					mBluetoothAdapter.startLeScan(mLeScanCallback);

					// showScanButton();
				}
			}
		});
	}

	private void StopScanDevice() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (mScanning == true) {
					mScanning = false;
					mBluetoothAdapter.stopLeScan(mLeScanCallback);
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					/*
					 * bar.setVisibility(View.GONE);
					 * 
					 * btn_scan.setBackground(getResources().getDrawable(R.drawable
					 * .bluetooth_icon)); btn_scan.setVisibility(View.VISIBLE);
					 * invalidateOptionsMenu();
					 */
				}
			}
		});
	}

	private void addStopScanTimer() {
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				StopScanDevice();
				showScanButton();
			}
		}, SCAN_PERIOD);
	}

	/*
	 * private void scanDevice(boolean enable) { if(enable) { StartScanDevice();
	 * addStopScanTimer(); showScanButton();
	 * 
	 * } else { StopScanDevice(); //showScanButton(); } }
	 */

	private void showScanButton() {
		if (mScanning == true) {
			btn_scan.setVisibility(View.GONE);
			bar.setVisibility(View.VISIBLE);
			invalidateOptionsMenu();
		}

		else {
			bar.setVisibility(View.GONE);

			btn_scan.setBackground(getResources().getDrawable(R.drawable.bluetooth_icon));
			btn_scan.setVisibility(View.VISIBLE);
			invalidateOptionsMenu();
		}
	}

	/**
	 * �����ص�
	 */
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
			// TODO Auto-generated method stub
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						if (device.getName().toLowerCase().contains("brezza")) {
							// v0.4.0
							mDevice = device;
							gotoConnectDevice();

							// mLeDeviceListAdapter.addDevice(device);
							// mLeDeviceListAdapter.notifyDataSetChanged();
						}
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}
				}
			});
		}
	};

	/**
	 * ����
	 */
	private OnClickListener listener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.tv_language_en:
				switchLanguage(LANGUAGE_ENGLISH, true);
				break;
			case R.id.tv_language_fr:
				switchLanguage(LANGUAGE_FRENCH, true);
				break;
			case R.id.btn_scan:
				if (mHw1505BleComm.ChkSysBluetoothState() == false) {
					new AlertDialog.Builder(PairingPageActivity.this).setTitle(R.string.warning).setMessage(R.string.tips_turn_on_bluetooth)
							.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									// TODO Auto-generated method stub
								}
							})

							// .setNegativeButton("��" , null)
							.show();
					break; // LLY added @2016-3-26
				}

				mLeDeviceListAdapter.clear();
				StartScanDevice();
				addStopScanTimer();
				showScanButton();

				// scanDevice(true);
				break;
			case R.id.myline:
				break;
			}
		}
	};

	/**
	 * 
	 * ��������������ʾɨ������������б�
	 */
	private class LeDeviceListAdapter extends BaseAdapter {
		private ArrayList<BluetoothDevice> mLeDevices;
		private LayoutInflater mInflater;

		public LeDeviceListAdapter() {
			super();
			mLeDevices = new ArrayList<BluetoothDevice>();
			mInflater = PairingPageActivity.this.getLayoutInflater();
		}

		public void addDevice(BluetoothDevice device) {
			if (!mLeDevices.contains(device)) {
				mLeDevices.add(device);
			}
		}

		public BluetoothDevice getDevice(int position) {
			return mLeDevices.get(position);
		}

		public void clear() {
			mLeDevices.clear();
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mLeDevices.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return mLeDevices.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder viewHolder;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.listitem_device, null);

				viewHolder.deviceName = (TextView) convertView.findViewById(R.id.device_name);
				viewHolder.deviceAddress = (TextView) convertView.findViewById(R.id.device_address);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			BluetoothDevice device = mLeDevices.get(position);
			final String deviceName = device.getName();
			if (deviceName != null && deviceName.length() > 0) {
				viewHolder.deviceName.setText(deviceName);
				viewHolder.deviceAddress.setText(device.getAddress());
			}
			return convertView;
		}

	}

	/**
	 * ɨ��������豸�б����ʱ�����Ӹ��豸
	 */
	private OnItemClickListener mBleDeviceOnItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			mDevice = mLeDeviceListAdapter.getDevice(position);
			if (mDevice == null)
				return;
			gotoConnectDevice();
		}
	};

	private void gotoConnectDevice() {
		String TobeConnectedName, TobeConnectedAddr;

		// scanDevice(false);
		StopScanDevice();
		// showScanButton();

		hw1505BleComm.setDevice(mDevice);

		TobeConnectedName = mDevice.getName();
		TobeConnectedAddr = mDevice.getAddress();
		/*
		 * if(TobeConnectedAddr.equals(mDeviceAddress) == false) { Log.i(TAG,
		 * "!!! Try to connect another device");
		 * 
		 * Intent intent = new Intent(BluetoothLeService.ACTION_NEW_CONNECTION);
		 * intent.putExtra("deviceAddress", TobeConnectedAddr);
		 * sendBroadcast(intent); }
		 */

		mDeviceName = TobeConnectedName;
		mDeviceAddress = TobeConnectedAddr;

		mSettingEditor.putString("set_DeviceName", mDeviceName);
		mSettingEditor.putString("set_DeviceAddr", mDeviceAddress);
		mSettingEditor.commit();

		updateConectionState(Color.GRAY);
		invalidateOptionsMenu();

		// ����
		Log.i(TAG, "����ɨ��������豸:" + mDeviceAddress);
		hw1505BleComm.resetCounter();
		hw1505BleComm.SetAutoConn(true);

		BluetoothLeService.ClrDeviceInfo();
		mHw1505BleComm.parentConnect(mDeviceName, mDeviceAddress);
	}

	/**
	 * ��������״̬
	 */
	private void updateConectionState(final int textColor) {
		/*
		 * runOnUiThread(new Runnable() {
		 * 
		 * @Override public void run() { if(mDeviceAddress.equals("")) {
		 * myline.setVisibility(View.INVISIBLE); //tv_connectName.setText("");
		 * //tv_connectAddress.setText(""); //tv_arrow.setText("");
		 * //tv_connectName.setTextColor(textColor);
		 * //tv_connectAddress.setTextColor(textColor);
		 * //tv_arrow.setTextColor(textColor);
		 * //myline.setBackgroundColor(color.white);//.transparent); } else {
		 * tv_connectName.setTextColor(textColor);
		 * tv_connectAddress.setTextColor(textColor);
		 * tv_arrow.setTextColor(textColor);
		 * 
		 * tv_connectName.setText(mDeviceName);
		 * tv_connectAddress.setText(mDeviceAddress); tv_arrow.setText(">");
		 * myline.setBackgroundColor(color.BLACK70); //backgroundColor
		 * myline.setVisibility(View.VISIBLE); }
		 * 
		 * } }); //
		 */
	}

	/**
	 * ���ӳɹ�֮����ת����������
	 */
	Runnable runnableGotoOperater = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			ConnectedActivity();
		}
	};

	private void gotoOperateActivity() {
		hGotoOperater.removeCallbacks(runnableGotoOperater);
		if (ifOnPairingPage() == true) {
			Log.i(TAG, "goto OperateActivity after 2s");

			showScanButton(); // added: V0.5.6 0627

			hGotoOperater.postDelayed(runnableGotoOperater, 500);
		}
	}

	/*
	 * private void OperateActivity() { Log.i(TAG,
	 * "enter OperateActivity():"+mDeviceAddress);
	 * 
	 * Intent intent = new
	 * Intent(PairingPageActivity.this,WarmerOperateActivity.class);
	 * startActivity(intent); finish(); }
	 */

	private void ConnectedActivity() {
		Log.i(TAG, "enter ConnectedActivity():" + mDeviceAddress);

		Intent intent = new Intent(PairingPageActivity.this, ConnectedActivity.class);
		intent.putExtra("parent_Activity", "PairingPageActivity");
		startActivity(intent);
		finish();
	}

	private void RequireToTurnOnBluetooth() {
		/*
		 * Intent enableBtIntent = new
		 * Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		 * startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT); //
		 */

		// samsung "Bluetooth permission request"
		// "Applcation is requesting \rpermission to turn on Bluetooth.\rAllow?"
		// "NO YES"
		// huawei "Turn on Bluetooth" "An app wants to turn on Bluetooth"
		// "Deny Allow"
		// coolpad "" "An app wants to turn on Bluetooth" "Deny Allow"
		Context mContent = PairingPageActivity.this;
		TextView tv = new TextView(mContent);
		tv.setText(R.string.turn_on_bluetooth);
		tv.setTextSize(20);
		tv.setTypeface(typeFace);
		tv.setPadding(30, 20, 10, 10);
		tv.setTextColor(Color.parseColor("#ffffff"));
		tv.setBackgroundColor(Color.parseColor("#00aaff"));
		/*
		 * AlertDialog.Builder dlgTurnOnBT = new AlertDialog.Builder(mContent);
		 * dlgTurnOnBT.setCustomTitle(tv); //not : setTitle()
		 * dlgTurnOnBT.setMessage("An app wants to turn on Bluetooth");
		 * dlgTurnOnBT.setPositiveButton("Allow", new
		 * DialogInterface.OnClickListener() {
		 * 
		 * @Override public void onClick(DialogInterface dialog, int which) {
		 * Log.i(TAG,"Allow"); mHw1505BleComm.turnOnSystemBluetooth();
		 * 
		 * //while(BluetoothLeService.ChkSysBluetoothState() == false) { try{
		 * Thread.sleep(200); }catch(InterruptedException e) {
		 * e.printStackTrace(); } } } }); dlgTurnOnBT.create();
		 * dlgTurnOnBT.show(); //
		 */

		// *
		Dialog alertDialog = new AlertDialog.Builder(this)
		// .setTitle("Turn on Bluetooth")
				.setCustomTitle(tv).setMessage(R.string.tips_turn_on_bluetooth_2)
				// .setIcon(R.drawable.ic_launcher)
				.setPositiveButton(R.string.allow, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Log.i(TAG, "sys bluetooth on:" + mHw1505BleComm.ChkSysBluetoothState());

						if (mHw1505BleComm.ChkSysBluetoothState())
							return;
						mHw1505BleComm.turnOnSystemBluetooth();

						/*
						 * bTurnOnBT = true; proDialog =
						 * android.app.ProgressDialog
						 * .show(PairingPageActivity.this, "turn on Bluetooth",
						 * "waiting...");
						 * 
						 * try { Thread.sleep(500); } catch
						 * (InterruptedException e) { e.printStackTrace(); }
						 */

						/*
						 * Thread thread = new Thread() { public void run() {
						 * try { sleep(500); } catch (InterruptedException e) {
						 * e.printStackTrace(); }
						 * if(BluetoothLeService.ChkSysBluetoothState() == true)
						 * { Log.i(TAG,"proDialog.dismiss");
						 * proDialog.dismiss(); } } }; thread.start();
						 */
					}
				}).create();
		alertDialog.show();
		// */
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.i(TAG, "onResume");

		// Initializes list view adapter.
		mLeDeviceListAdapter = new LeDeviceListAdapter();
		lv_bleList.setAdapter(mLeDeviceListAdapter);

		if (mHw1505BleComm == null) {
			Log.i(TAG, "new hw1505BleComm");
			mHw1505BleComm = new hw1505BleComm(this, new hw1505BleUiCallbacks.Null() {
				@Override
				public void uiCommunicated() {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							// show connected device in CYAN
							updateConectionState(Color.CYAN);
							invalidateOptionsMenu();
							mLeDeviceListAdapter.clear();

							// jump to connected page
							gotoOperateActivity();
						}
					});
				}

				@Override
				public void uiDeviceDisconnected() {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							updateConectionState(Color.GRAY);
							invalidateOptionsMenu();
						}
					});
				}

				@Override
				public void uiSystemBluetoothOn() {
					Log.i("ddd", "uiSystemBluetoothOn");
					if (bTurnOnBT == true) {
						Log.i(TAG, "proDialog.dismiss");
						bTurnOnBT = false;
						proDialog.dismiss();
					}
				}

				@Override
				public void uiActivityFinish() {
					runOnUiThread(new Runnable() {
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

		// after initialize 'hw1505BleComm', because 'hw1505BleComm' is used in
		// RequireToTurnOnBluetooth
		initBLE();

		// Ensures Bluetooth is enabled on the device. If Bluetooth is not
		// currently enabled,
		// fire an intent to display a dialog asking the user to grant
		// permission to enable it.
		/*
		 * if (!mBluetoothAdapter.isEnabled()) { if
		 * (!mBluetoothAdapter.isEnabled()) { RequireToTurnOnBluetooth(); } } //
		 */

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		// scanDevice(false);
		StopScanDevice();
		showScanButton();
		mLeDeviceListAdapter.clear();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// *
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
			if ((System.currentTimeMillis() - exitTime) > 2000) {
				Toast.makeText(getApplicationContext(), R.string.exit_program, Toast.LENGTH_SHORT).show();
				exitTime = System.currentTimeMillis();
			} else {
				finish();
				System.exit(0);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
		// */

		/*
		 * if(keyCode == KeyEvent.KEYCODE_BACK) { OperateActivity(); } return
		 * super.onKeyDown(keyCode, event); //
		 */
	}

	static class ViewHolder {
		TextView deviceName;
		TextView deviceAddress;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.i(TAG, "onDestroy()");
		mHw1505BleComm.ActivtiyDestroyed();
	}

	// end of PairingPageActivity
}
