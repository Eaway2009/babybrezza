package com.hiswill.babybrezza_1505;

import com.hiswill.babybrezza_1505.utils.DataUtils;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class hw1505BleComm {
	private String TAG;
	private Activity mParent = null;

	public static Activity CurrentActivity;
	public static boolean brandSamsung;
	public static boolean samsungG7105;

	// public static String mDeviceName;
	// public static String mDeviceAddress;
	public static BluetoothDevice mDevice;
	public static BluetoothLeService mBleService;

	public static boolean bAutoConnection;
	public static boolean bReconnectEn;
	public static boolean bQueryVersion;
	public static boolean bCommuicatedOk = false;
	public static byte BleVersion = (byte) 0xFF, BleYear, BleMonth, BleDay;
	Handler handler = new Handler();
	Handler connHandler = new Handler();

	private static long ConnectedSysTime;
	private static int unstableCnt;
	private static int Cnt_OK;
	private static int Cnt_Conn;

	/* callback object through which we are returning results to the caller */
	private hw1505BleUiCallbacks mUiCallback = null;

	/* define NULL object for UI callbacks */
	private static final hw1505BleUiCallbacks NULL_CALLBACK = new hw1505BleUiCallbacks.Null();

	/* creates BleWrapper object, set its parent activity and callback object */
	public hw1505BleComm(Activity parent, hw1505BleUiCallbacks callback) {
		this.mParent = parent;
		hw1505BleComm.CurrentActivity = parent;

		mUiCallback = callback;
		if (mUiCallback == null)
			mUiCallback = NULL_CALLBACK;

		TAG = mParent.getLocalClassName();
		// Log.i("getLocalClassName",mParent.getLocalClassName());
		// Log.i("getPackageName",mParent.getPackageName());
		// Log.i("getPackageCodePath",mParent.getPackageCodePath());
		Log.i("hw1505BleComm", "Parent:" + TAG);

		mParent.registerReceiver(mHw1505Receiver, Hw1505IntentFilter());
		boolean res = mParent.bindService(new Intent(mParent, BluetoothLeService.class), bleServiceConn, Context.BIND_AUTO_CREATE);
		Log.e(TAG, "bindService:" + (res == true ? "OK" : "Failed"));

		brandSamsung = android.os.Build.BRAND.equalsIgnoreCase("samsung");
		samsungG7105 = android.os.Build.MODEL.equalsIgnoreCase("SM-G7105");
	}

	public void ActivtiyDestroyed() {
		Log.i(TAG, "ActivtiyDestroyed()");
		mParent.unregisterReceiver(mHw1505Receiver);
		mParent.unbindService(bleServiceConn);
	}

	public static void setDevice(BluetoothDevice dev) {
		mDevice = dev;
	}

	public static BluetoothDevice getDevice() {
		return mDevice;
	}

	public static String getConnectedName() {
		return mDevice.getName();
	}

	public static String getConnectedAddress() {
		return mDevice.getAddress();
	}

	public static void setCurrentActivity(Activity act) {
		CurrentActivity = act;
	}

	public static void resetCounter() {
		Cnt_OK = 0;
		Cnt_Conn = 0;
	}

	public static void SetAutoConn(boolean en) {
		bAutoConnection = en;
	}

	private ServiceConnection bleServiceConn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.i(TAG, "onServiceDisconnected");
			mBleService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// ����һ������
			Log.i(TAG, "onServiceConnected:");
			mBleService = ((BluetoothLeService.bleBinder) service).getService();
		}
	};

	Runnable mRunnableQuery = new Runnable() {
		@Override
		public void run() {
			QueryVersion();
			if (bQueryVersion == true) {
				handler.postDelayed(this, 5000);
			}
		}
	};

	public void startQueryVersion() {
		bQueryVersion = true;
		handler.postDelayed(mRunnableQuery, 500);
	}

	public void QueryVersion() {
		Log.i(TAG, ":��ѯ�汾");

		byte[] data;
		DataUtils dataUtils;
		dataUtils = new DataUtils();

		data = dataUtils.reayTXData((byte) 'H', (byte) 'W', (byte) 'V', (byte) 'E', (byte) 'R', (byte) '?', (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x00);
		mBleService.uiWriteData(data);
	}

	public void parentConnect(String DeviceName, String DeviceAddress) {
		Log.i(TAG, "parentConnect");
		mBleService.uiConnect(mParent, mDevice.getName(), mDevice.getAddress());
	}

	/*
	 * start re-connection step by step, step interval is 1s
	 */
	int step = 0;
	Runnable proReconnection = new Runnable() {
		public void run() {
			switch (step) {
			case 0:
				mBleService.getBTAdapter().startLeScan(scanCallback);
				break;

			case 1:
				mBleService.getBTAdapter().stopLeScan(scanCallback);
				break;

			case 4:
				mBleService.uiConnect(mParent, mDevice.getName(), mDevice.getAddress());
				break;
			}

			step++;
			if (step < 5) {
				connHandler.postDelayed(this, 1000);
			}
		}
	};

	private void startToReconnect(long delay) {
		connHandler.removeCallbacks(proReconnection);

		if (bAutoConnection == false)
			return;

		if (samsungG7105 == true)
		// if(brandSamsung == true)
		{
			// start scan, stop scan, connect
			// Log.e(TAG,"samsung:start to reconnect");
			step = 0;
		} else {
			// connect directly after a few seconds
			// Log.e(TAG,"not samsung:start to reconnect");
			step = 2;
		}
		connHandler.postDelayed(proReconnection, delay);
	}

	public final BroadcastReceiver mHw1505Receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			Log.i(TAG, "onReceive, Current activity: " + CurrentActivity.getLocalClassName());

			// ========== if connected
			if (BluetoothLeService.DEVSTATE_CONNECTED.equals(action)) {
				String DeviceAddress = intent.getStringExtra("deviceAddress");
				Cnt_Conn++;
				Log.i(TAG, Cnt_Conn + ":������:" + DeviceAddress);

				bReconnectEn = false;
				mUiCallback.uiDeviceConnected();
				ConnectedSysTime = System.currentTimeMillis();
			}
			// ========== if disconnected
			else if (BluetoothLeService.DEVSTATE_DISCONNECTED.equals(action)) {
				int status = intent.getIntExtra("status", -1);
				Log.i(TAG, "�Ͽ���, status=" + status);

				handler.removeCallbacks(mRunnableQuery);
				connHandler.removeCallbacks(proReconnection);

				bQueryVersion = false;
				bCommuicatedOk = false;
				mUiCallback.uiDeviceDisconnected();

				if (CurrentActivity.equals(mParent) == false)
					return;

				mBleService.uiClose();
				BluetoothLeService.ClrDeviceInfo();

				// check error status
				// status is 8 normally for n9005
				boolean bRestartBT = false;
				long delay = 0;
				if ((status == 133) || (status == 129)) {
					Log.e(TAG, "133 or 129, close BT");
					bRestartBT = true;
					mUiCallback.uiBluetoothErr133();
				} else {
					// check if connection is unstable
					long interval = System.currentTimeMillis() - ConnectedSysTime;
					if (interval < 600) {
						// maybe the signal is unstable
						delay = 3000;
					} else if (interval < 3000) {
						// the connection is unstable
						unstableCnt++;
						if (unstableCnt >= 3) {
							Log.e(TAG, "unstable, close BT");
							unstableCnt = 0;
							bRestartBT = true;
							ConnectedSysTime = System.currentTimeMillis();
						}
					}
				}

				if (bRestartBT == true) {
					bReconnectEn = true;
					mBleService.turnOffSystemBluetooth(mParent);
				} else {
					startToReconnect(delay);
				}
			}
			// ========== if discovered service
			else if (BluetoothLeService.DEVSTATE_SERVICES_DISCOVERED.equals(action)) {
				Log.i(TAG, "DEVSTATE_SERVICES_DISCOVERED");

				mUiCallback.uiServiceDiscovered();
				if (CurrentActivity.equals(mParent) == false)
					return;

				startQueryVersion();
			}
			// ========== if connect to a new device (unused now)
			else if (BluetoothLeService.ACTION_NEW_CONNECTION.equals(action)) {
				mUiCallback.uiNewConnection();
			}
			// ========== if connect to a new device (unused now)
			else if (BluetoothLeService.ACTION_CLOSE_ACTIVITY.equals(action)) {
				Log.i(TAG, "ACTION_CLOSE_ACTIVITY");
				mUiCallback.uiActivityFinish();
			}
			// ========== if write data ok
			else if (BluetoothLeService.DEVSTATE_WRITEDATA_OK.equals(action)) {
				Log.i(TAG, "DEVSTATE_WRITEDATA_OK");
				mUiCallback.uiSuccessfulWrite();
			}
			// ========== if write data failed
			else if (BluetoothLeService.DEVSTATE_WRITEDATA_FAIL.equals(action)) {
				Log.i(TAG, "DEVSTATE_WRITEDATA_FAIL");
				mUiCallback.uiFailedWrite();
			}
			// ========== if blue-tooth is turned off
			else if (BluetoothLeService.SYSSTATE_BLE_OFF.equals(action)) {
				Log.i(TAG, "ϵͳ�����ر�");
				connHandler.removeCallbacks(proReconnection);
				mUiCallback.uiSystemBluetoothOff();
				if (CurrentActivity.equals(mParent) == false)
					return;

				if (bReconnectEn == true) {
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							mBleService.turnOnSystemBluetooth();
						}
					}, 2000);
				}
			}
			// ========== if blue-tooth is turned on
			else if (BluetoothLeService.SYSSTATE_BLE_ON.equals(action)) {
				Log.i(TAG, "ϵͳ������");
				mUiCallback.uiSystemBluetoothOn();
				if (CurrentActivity.equals(mParent) == false)
					return;

				if (bReconnectEn == true) {
					startToReconnect(1000);
				}
			}
			// ========== if received data
			else if (BluetoothLeService.DEVSTATE_RECEIVE_DATA.equals(action)) {
				Log.i(TAG, "get data from device");
				byte rCommand = (byte) intent.getIntExtra("command", 0);
				byte rHour = (byte) intent.getIntExtra("hour", 0);
				byte rMinute = (byte) intent.getIntExtra("minute", 0);
				byte rSecond = (byte) intent.getIntExtra("second", 0);
				byte rCapacity = (byte) intent.getIntExtra("capacity", 0);
				byte rSpeedModel = (byte) intent.getIntExtra("speedModel", 0);
				byte rWorkingModel = (byte) intent.getIntExtra("workingModel", 0);
				byte rStatus = (byte) intent.getIntExtra("status", 0);

				if (bQueryVersion == true) {
					// 0xA5,0xFF,'V','E','R', version, yy,mm,dd
					// (byte)command, (byte)hour, (byte)minute, (byte)second,
					// (byte)capacity, (byte)speedModel, (byte)workingModel,
					// (byte)temp, (byte)version, (byte)save, (byte)save_t,
					// (byte)status);
					if ((rCommand == 'V') && (rHour == 'E') && (rMinute == 'R')) {
						Cnt_OK++;
						Log.i(TAG, "===== " + Cnt_OK + ":���ӳɹ����õ��汾��");
						bQueryVersion = false;
						bCommuicatedOk = true;

						BleVersion = (byte) rSecond;
						BleYear = rCapacity;
						BleMonth = rSpeedModel;
						BleDay = rWorkingModel;

						handler.removeCallbacks(mRunnableQuery);
						connHandler.removeCallbacks(proReconnection);

						// Version, Year, Month, Day
						mUiCallback.uiCommunicated();
						unstableCnt = 0;
						return;
					}
				}

				mUiCallback.uiGetData(rCommand, rHour, rMinute, rSecond, rCapacity, rSpeedModel, rWorkingModel, rStatus);
			}
		}

	};

	private static IntentFilter Hw1505IntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.DEVSTATE_CONNECTED);
		intentFilter.addAction(BluetoothLeService.DEVSTATE_DISCONNECTED);
		intentFilter.addAction(BluetoothLeService.DEVSTATE_SERVICES_DISCOVERED);
		intentFilter.addAction(BluetoothLeService.DEVSTATE_RECEIVE_DATA);
		intentFilter.addAction(BluetoothLeService.ACTION_CLOSE_ACTIVITY);

		intentFilter.addAction(BluetoothLeService.SYSSTATE_BLE_ON);
		intentFilter.addAction(BluetoothLeService.SYSSTATE_BLE_OFF);

		intentFilter.addAction(BluetoothLeService.DEVSTATE_WRITEDATA_OK);
		intentFilter.addAction(BluetoothLeService.DEVSTATE_WRITEDATA_FAIL);

		// intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
		// intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
		// intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);

		// intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		// intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);

		return intentFilter;
	}

	private BluetoothAdapter.LeScanCallback scanCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
			// TODO Auto-generated method stub
		}
	};

	public void turnOnSystemBluetooth() {
		mBleService.turnOnSystemBluetooth();
	}

	public boolean ChkSysBluetoothState() {

		return mBleService.ChkSysBluetoothState();
	}
}
