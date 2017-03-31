package com.hiswill.babybrezza_1505;

import java.util.List;
import java.util.UUID;

import com.hiswill.babybrezza_1505.utils.DataUtils;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class BluetoothLeService extends Service
{
    private final static String TAG = "BluetoothLeService";
    
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt    mBluetoothGatt;

    DataUtils dataUtils;
    
    public static final int STATE_DISCONNECTED	= 0;
    public static final int STATE_CONNECTING   	= 1;
    public static final int STATE_CONNECTED    	= 2;
    
    private static String	mBluetoothDeviceName;
    private static String	mBluetoothDeviceAddress;
    private static int		mConnectionState = STATE_DISCONNECTED;
	private static boolean  isSamsung;
    ;    

	public final static String ACTION_CONNECT_DEVICE  		= "com.hiswill.1505.ACTION_CONNECT_DEVICE";
	public final static String ACTION_DISCONNECT_DEVICE 	= "com.hiswill.1505.ACTION_DISCONNECT_DEVICE";
	public final static String ACTION_CLOSE_DEVICE 			= "com.hiswill.1505.ACTION_CLOSE_DEVICE";
	public final static String ACTION_SEND_DATA  			= "com.hiswill.1505.ACTION_SEND_DATA";
	public final static String ACTION_NEW_CONNECTION  		= "com.hiswill.1505.ACTION_NEW_CONNECTION";
	public final static String ACTION_CLOSE_ACTIVITY  		= "com.hiswill.1505.ACTION_CLOSE_ACTIVITY";

    public final static String DEVSTATE_CONNECTED    		= "com.hiswill.1505.DEVSTATE_CONNECTED";
    public final static String DEVSTATE_DISCONNECTED 		= "com.hiswill.1505.DEVSTATE_DISCONNECTED";
    public final static String DEVSTATE_SERVICES_DISCOVERED = "com.hiswill.1505.DEVSTATE_SERVICES_DISCOVERED";
	public final static String DEVSTATE_RECEIVE_DATA 		= "com.hiswill.1505.DEVSTATE_RECEIVE_DATA";
    public final static String DEVSTATE_WRITEDATA_OK		= "com.hiswill.1505.DEVSTATE_WRITEDATA_OK";
    public final static String DEVSTATE_WRITEDATA_FAIL		= "com.hiswill.1505.DEVSTATE_WRITEDATA_FAIL";

    public final static String SYSSTATE_BLE_ON  			= "com.hiswill.1505.ACTION_BLE_ON";
    public final static String SYSSTATE_BLE_OFF  			= "com.hiswill.1505.ACTION_BLE_OFF";
	
	private 	BluetoothGattCharacteristic mNotifyCharacteristic;
	private 	BluetoothGattCharacteristic mWriteCharacteristic = null;
	
	//private SharedPreferences mSettings;
	//private SharedPreferences.Editor mSettingEditor;
    
    public final static UUID UUID_HEART_RATE_MEASUREMENT = UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);
    
    public final static UUID UUID_ISSC_RX = UUID.fromString(SampleGattAttributes.ISSC_CHAR_RX_UUID);
    //public final static UUID UUID_ISSC_RX2 = UUID.fromString(SampleGattAttributes.ISSC_CHAR_RX_UUID2);

    //=====
	@Override
	public IBinder onBind(Intent intent)
	{
		//return null;
		return new bleBinder();
	}
	
    public class bleBinder extends Binder
    {  
        /** 
         * 获取当前Service的实例 
         * @return 
         */  
        public BluetoothLeService getService(){  
            return BluetoothLeService.this;  
        }  
    }
    
    //========================
    // method for calling
    //========================
    public boolean turnOffSystemBluetooth(Activity mContext)
    {
    	Log.e(TAG,mContext.getLocalClassName()+":turn Off BT");
    	if(mBluetoothAdapter.getState() != BluetoothAdapter.STATE_ON) return false;

    	close();
    	mBluetoothAdapter.disable(); 
    	mBluetoothManager = null;

		try{
			Thread.sleep(200);
		}catch(InterruptedException e)
		{
			e.printStackTrace();
		}
    	return true;
    }
    //========================
    public boolean turnOnSystemBluetooth()
    {
    	initialize();
    	if(mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) return false;
    	
    	mBluetoothAdapter.enable();
		try{
			Thread.sleep(200);
		}catch(InterruptedException e)
		{
			e.printStackTrace();
		}
		return true;
    }
    //========================
    public BluetoothAdapter getBTAdapter()
    {
    	return mBluetoothAdapter;
    }
    //========================
    public static boolean getConnectedState()
    {
    	return (mConnectionState == STATE_CONNECTED ? true:false);
    }
    //========================
    public static void ClrDeviceInfo()
    {
    	mBluetoothDeviceAddress = null;
    	mBluetoothDeviceName    = null;
    }
    //========================
    public boolean ChkSysBluetoothState()
	{
		return (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON); 
	}
    //========================
    public static boolean ifBrandSamsung()
    {
    	return isSamsung;
    }
    //========================
	private boolean checkIsSamsung() 
	{
		//android.os.Build.VERSION.RELEASE获取版本号
	    //android.os.Build.MODEL 获取手机型号
		String brand = android.os.Build.BRAND;
		Log.e(TAG, " brand:" + brand+",model:"+android.os.Build.MODEL+",version:"+android.os.Build.VERSION.RELEASE);
		if (brand.toLowerCase().equals("samsung")) {
			return true;
		}
		return false;
	}
    //========================
    public boolean uiWriteData(byte[] data)
    {
    	if(mConnectionState == STATE_CONNECTED)
    	{
    		writeCharacteristic(mWriteCharacteristic, data);
    		return true;
    	}
    	return false;
    }
    //========================
    public void uiConnect(Activity mContext, final String deviceName, final String deviceAddress)
    {
		Log.i(TAG, mContext.getLocalClassName()+" uiConnect:"+deviceAddress);
		if(deviceAddress == null) return;
		
		if(deviceAddress.equals(mBluetoothDeviceAddress))
		{
			if(mConnectionState == STATE_CONNECTED) 
			{
				Log.i(TAG, "已连接:"+mBluetoothDeviceAddress);
				return;
			}
			else if(mConnectionState == STATE_CONNECTING) 
			{
				Log.i(TAG, "正在连接:"+mBluetoothDeviceAddress);
				return;
			}
			if(isSamsung)
			{
				//connect(deviceAddress);
				//return;
			}
		}
			
		mBluetoothDeviceName = deviceName;

		// Connect to BLE device from mHandler
		Handler mHandler = new Handler(mContext.getMainLooper());
		mHandler.post(new Runnable() {
		    @Override
		    public void run() {
		    	connect(deviceAddress);
		    }
		});
	}
    //========================
    public void uiClose()
    {
		Log.i(TAG, "uiClose");
		disconnect();
		close();
		//mConnectionState	= STATE_DISCONNECTED;
		//mBluetoothDeviceAddress = null;
		
		try{
			Thread.sleep(100);
		}catch(InterruptedException e)
		{
			e.printStackTrace();
		}
    }
    

    //========================
    //
    //========================
	@Override
	public void onCreate()
	{
		// TODO Auto-generated method stub
		Log.i(TAG, "onCreate()");
		super.onCreate();
		initialize();
		dataUtils = new DataUtils();

		isSamsung = checkIsSamsung();
	}

	
    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
	private  boolean initialize() 
	{
		if (mBluetoothManager == null) 
		{
			mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			if (mBluetoothManager == null)
			{
				Log.e(TAG, "Unable to initialize BluetoothManager.");
				return false;
			}
		}
	        
		mBluetoothAdapter = mBluetoothManager.getAdapter();
		if (mBluetoothAdapter == null)
		{
			Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
			return false;
	   	}
	    return true;
	}
	 
	 private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback()
	 {
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status,int newState)
		{
			// TODO Auto-generated method stub
			super.onConnectionStateChange(gatt, status, newState);
			String intentAction;
			
			Log.e(TAG,"onConnectionStateChange status:"+status+"& newState:"+newState);			
			if(newState==BluetoothProfile.STATE_DISCONNECTED)			//maybe:status = 133/129/8(n9005), or the others
			{
				mConnectionState	= STATE_DISCONNECTED;
				
				intentAction = DEVSTATE_DISCONNECTED;
				broadcastUpdate(intentAction,status);
			}
			else if(newState==BluetoothProfile.STATE_CONNECTED)
			{
				if(status == BluetoothGatt.GATT_SUCCESS)
				{
					mBluetoothGatt.discoverServices();
					
					mConnectionState = STATE_CONNECTED;
					intentAction = DEVSTATE_CONNECTED;
					broadcastUpdate(intentAction,mBluetoothDeviceAddress);
				}
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status)
		{
			// TODO Auto-generated method stub
			super.onServicesDiscovered(gatt, status);
			if(status==BluetoothGatt.GATT_SUCCESS)
			{
				displayGattServices(getSupportedGattServices());
            	
            	//LLY added
				String intentAction = DEVSTATE_SERVICES_DISCOVERED;
				broadcastUpdate(intentAction);
			}
			else
			{
				Log.e(TAG, "onServicesDiscovered received: " + status);
			}
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,BluetoothGattCharacteristic characteristic, int status)
		{
			// TODO Auto-generated method stub
			super.onCharacteristicRead(gatt, characteristic, status);
			if(status==BluetoothGatt.GATT_SUCCESS)
			{
				byte[] data = characteristic.getValue();
				 
				String ShowRead = "onCharacteristicRead:";
				for (int i = 0; i < data.length; i++)
				{
					ShowRead += dataUtils.encodeHex(data[i])+"  ";
				}
				Log.i(TAG,  ShowRead);
			}
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt,BluetoothGattCharacteristic characteristic, int status)
		{
			// TODO Auto-generated method stub
			super.onCharacteristicWrite(gatt, characteristic, status);

			String intentAction;
			
			if(status==BluetoothGatt.GATT_SUCCESS)
			{
				Log.i(TAG, "写入数据成功");
				intentAction = DEVSTATE_WRITEDATA_OK;
			} 
			else
			{
				Log.i(TAG, "写入数据失败");
				intentAction = DEVSTATE_WRITEDATA_FAIL;
			}
			broadcastUpdate(intentAction,mBluetoothDeviceAddress);
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,BluetoothGattCharacteristic characteristic)
		{
			// TODO Auto-generated method stub
			super.onCharacteristicChanged(gatt, characteristic);
			String GetData = "onCharacteristicChanged:";
			
			byte[] data = characteristic.getValue();
	       	for (int i = 0; i < data.length; i++)
			{
	       		GetData += dataUtils.encodeHex(data[i])+"  ";
			}
	       	Log.i(TAG, GetData);
	       	//Log.i(TAG, "Length:"+data.length);
	            
            if(data.length == 15)
            {
            	byte sum = 0;
            	for(int c=0; c<14; c++)
            	{
            		sum += data[c];
            	}
	            //Log.i(TAG, "校验和:"+ dataUtils.encodeHex(sum)+"  ");		            
	            if(sum == data[14])
	            {
	            	//data is right
		            broadcastUpdate(DEVSTATE_RECEIVE_DATA, data[2],data[3],data[4],data[5],data[6],data[7],data[8],data[9],data[10],data[11],data[12],data[13]);
            	}
            }
		}
		 
		 
	 };
	 
	  /**
	     * Connects to the GATT server hosted on the Bluetooth LE device.
	     *
	     * @param address The device address of the destination device.
	     *
	     * @return Return true if the connection is initiated successfully. The connection result
	     *         is reported asynchronously through the
	     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
	     *         callback.
	     */
	    public  boolean connect(final String address) 
	    {
        	mConnectionState	= STATE_DISCONNECTED;
        	
	        if (mBluetoothAdapter == null || address == null) {
	        	Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
	            return false;
	        }

	        Log.i(TAG,"connect to "+address);
	        mBluetoothDeviceAddress = address;	        
	        /*
	        if (mBluetoothGatt != null && mBluetoothGatt.getDevice().getAddress().equals(address)) 
	        {
	        	if(checkIsSamsung())
	        	{
					try
					{
						Thread.sleep(200);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
		        	boolean res = mBluetoothGatt.connect();
		        	if(res==true) mConnectionState = STATE_CONNECTING;
		        	Log.i(TAG,"启动重连:"+(res==true?"OK":"failed"));
		        	return res;
	        	}
	        }
			*/
			if (mBluetoothGatt != null) 
			{
				mBluetoothGatt.disconnect();
				mBluetoothGatt.close();
				
				try
				{
					Thread.sleep(300);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
	        
	        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
	        if (device == null) 
	        {
	        	Log.w(TAG, "Device not found.  Unable to connect.");
	            return false;
	        }

	        Log.i(TAG, "创建一个新的连接");
	        mConnectionState	= STATE_CONNECTING;
	        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
	        return true;
	    }
	    
	    /**
	     * Disconnects an existing connection or cancel a pending connection. The disconnection result
	     * is reported asynchronously through the
	     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
	     * callback.
	     */
	    public  void disconnect() {
	        if (mBluetoothAdapter == null)	{	Log.w(TAG, "BluetoothAdapter not initialized");	return;	}
	        if (mBluetoothGatt == null)		{	Log.w(TAG, "mBluetoothGatt is null");	return;	}
	        
	        mBluetoothGatt.disconnect();
        	mConnectionState	= STATE_DISCONNECTED;
	    }

	    /**
	     * After using a given BLE device, the app must call this method to ensure resources are
	     * released properly.
	     */
	    public void close() {
	        if (mBluetoothGatt == null) {
	            return;
	        }
	        mBluetoothGatt.close();
	        mBluetoothGatt = null;
        	mConnectionState	= STATE_DISCONNECTED;
	    }
	    
	    /**
	     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
	     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
	     * callback.
	     *
	     * @param characteristic The characteristic to read from.
	     */
	    private void readCharacteristic(BluetoothGattCharacteristic characteristic) {
	        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
	        	Log.w(TAG, "BluetoothAdapter not initialized");
	            return;
	        }
	        mBluetoothGatt.readCharacteristic(characteristic);
	    }
	    
	    private  void writeCharacteristic(BluetoothGattCharacteristic characteristic, byte[] value) {
	    	if (mBluetoothAdapter == null || mBluetoothGatt == null) {
	    		Log.w(TAG, "BluetoothAdapter not initialized");
	    	}
	    	characteristic.setValue(value);
	    	mBluetoothGatt.writeCharacteristic(characteristic);
	    	Log.e(TAG, "GATT write data");
	    }
	    
		/**
		 * 
		 * @param gattServices
		 */
		private void displayGattServices(List<BluetoothGattService> gattServices)
		{
	        if (gattServices == null) return;
	        for (BluetoothGattService gattService : gattServices) {
	            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
	            
	            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
	                final int charaProp = gattCharacteristic.getProperties();
	                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) 
	                {
	                	if (gattCharacteristic.getUuid().toString().compareTo(SampleGattAttributes.ISSC_CHAR_RX_UUID) == 0) 
	                	{
	                		mNotifyCharacteristic = gattCharacteristic;
	                		setCharacteristicNotification(mNotifyCharacteristic, true);
	                	}
	                	/*
	                	if (gattCharacteristic.getUuid().toString().compareTo(SampleGattAttributes.ISSC_CHAR_RX_UUID2) == 0) 
	                	{
	                		mNotifyCharacteristic = gattCharacteristic;
	                		setCharacteristicNotification(mNotifyCharacteristic, true);
	                	}
	                	*/
	                }
	                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0 ||
	                	(charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) 
	                {
	                	//if (gattCharacteristic.getUuid().toString().compareTo(SampleGattAttributes.ISSC_CHAR_TX_UUID) == 0) 
	                	{
	                	mWriteCharacteristic = gattCharacteristic;
	                	}
	                }
	            }
	        }
	    }
		
		   /**
	     * Enables or disables notification on a give characteristic.
	     *
	     * @param characteristic Characteristic to act on.
	     * @param enabled If true, enable notification.  False otherwise.
	     */
	    private void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
	        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
	        	Log.w(TAG, "BluetoothAdapter not initialized");
	            return;
	        }
	        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
	        
	        // This is specific to Heart Rate Measurement.
	        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
	            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
	                                   UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
	            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
	            mBluetoothGatt.writeDescriptor(descriptor);
	        }
	        
	        if (UUID_ISSC_RX.equals(characteristic.getUuid()))// || UUID_ISSC_RX2.equals(characteristic.getUuid())) 
	        {
	        	BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
	        			UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
	        	if (descriptor != null) {
	        		descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
	        		mBluetoothGatt.writeDescriptor(descriptor);
	        	}
	        }
	    }
		
		private  List<BluetoothGattService> getSupportedGattServices() 
		{
			if (mBluetoothGatt == null) return null;
		        
			return mBluetoothGatt.getServices();
		}
	    
	    /**
	     * 广播接受者，来处理前台发过来的命令
	     */
	    private final BroadcastReceiver BluetoothLeReceiver = new BroadcastReceiver()
		{
			@Override
			public void onReceive(Context context, Intent intent)
			{
				// TODO Auto-generated method stub
				final String action = intent.getAction();
				//int cmd = intent.getIntExtra("cmd",-1);
				String deviceAddress = intent.getStringExtra("deviceAddress");	
				
				//==========
				if(ACTION_CONNECT_DEVICE.equals(action))
				{
					Log.i(TAG, "ACTION_CONNECT_DEVICE:"+deviceAddress);
					if(deviceAddress == null) return;
					
					mBluetoothDeviceName = intent.getStringExtra("deviceName");
					if(mBluetoothDeviceAddress == null)
					{
						connect(deviceAddress);
						return;
					}
					
					if(mBluetoothDeviceAddress.equals(deviceAddress))
					{
						if(mConnectionState != STATE_CONNECTING)
						{
							Log.i(TAG, "disconnect,1秒后重连");
							disconnect();
							//close();

							mConnectionState	= STATE_CONNECTING;
							new Handler().postDelayed(new Runnable()
							{    
								public void run() {    
									connect(mBluetoothDeviceAddress);
								}
							}, 1000);   
						}
						else Log.i(TAG, "忽略");		
					}
					else
					{
						Log.i(TAG, "连接其它设备");
						disconnect();
						close();

						mConnectionState	= STATE_DISCONNECTED;
						//mBluetoothDeviceAddress = "";
						
						connect(deviceAddress);
						return;
					}
				}
				//==========
				else if(ACTION_DISCONNECT_DEVICE.equals(action))
				{
					Log.i(TAG, "ACTION_DISCONNECT_DEVICE:"+deviceAddress);
					disconnect();

					mConnectionState	= STATE_DISCONNECTED;
					//mBluetoothDeviceAddress = null;
				}
				//==========
				else if(ACTION_SEND_DATA.equals(action))
				{
					if(mConnectionState==STATE_CONNECTED)
					{
						if(mWriteCharacteristic!=null)
						{
							Log.i(TAG, "mWriteCharacteristic send data ****");
							
							byte[] data = intent.getByteArrayExtra("data");
							writeCharacteristic(mWriteCharacteristic, data);
						}
					}
				}
				//==========
				else if(ACTION_CLOSE_DEVICE.equals(action))
				{
					Log.i(TAG, "ACTION_CLOSE_DEVICE");
					close();

					mConnectionState	= STATE_DISCONNECTED;
					//mBluetoothDeviceAddress = null;
				}
				//==========
				else if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action))
				{
					if(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)==BluetoothAdapter.STATE_OFF)
					{
						Log.i(TAG, "蓝牙关了");
						close();

						mConnectionState	= STATE_DISCONNECTED;
						String intentAction = SYSSTATE_BLE_OFF;
						broadcastUpdate(intentAction);
					}
					else if(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)==BluetoothAdapter.STATE_ON)
					{
						Log.i(TAG, "蓝牙开了");
						Log.i(TAG, "mac:"+mBluetoothDeviceAddress);
						
						//LLY added @2016-3-26
						mConnectionState	= STATE_DISCONNECTED;
						String intentAction = SYSSTATE_BLE_ON;
						broadcastUpdate(intentAction);

						//connect(mBluetoothDeviceAddress);
					}
				}
			}
		}; 
		
		
		private static IntentFilter makeGattUpdateIntentFilter()
		{
			final IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(ACTION_CONNECT_DEVICE);
			intentFilter.addAction(ACTION_DISCONNECT_DEVICE);
			intentFilter.addAction(ACTION_CLOSE_DEVICE);
			
			intentFilter.addAction(ACTION_SEND_DATA);
			intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
			return intentFilter;
		}
	 
	 
	 /**
	  * 
	  * @param action
	  */
	 private void broadcastUpdate(final String action)
	 {
	        final Intent intent = new Intent(action);
	        sendBroadcast(intent);
	 }
		 
	 private void broadcastUpdate(final String action,final String address)
	 {
	        final Intent intent = new Intent(action);
			intent.putExtra("deviceAddress",address);
	        sendBroadcast(intent);
	 }
	 private void broadcastUpdate(final String action,final int status)
	 {
	        final Intent intent = new Intent(action);
			intent.putExtra("status",status);
	        sendBroadcast(intent);
	 }
	 
	 
	 
	 /**
	  * 
	  * @param action
	  * @param hour
	  * @param minute
	  * @param second
	  */
	 private void broadcastUpdate(final String action,int command,int hour,int minute,int second,int capacity,int speedModel,int workingModel,int temp,int version,int save,int save_t,int status)
	 {
		 final Intent intent = new Intent(action);
		 intent.putExtra("command",command);
		 intent.putExtra("hour",hour);
		 intent.putExtra("minute", minute);
		 intent.putExtra("second", second);
		 intent.putExtra("capacity",capacity);
		 intent.putExtra("speedModel",speedModel);
		 intent.putExtra("workingModel",workingModel);
		 intent.putExtra("version",version);
		 intent.putExtra("temp",temp);
		 intent.putExtra("save",save);
		 intent.putExtra("save_t",save_t);
		 intent.putExtra("status",status);
		 sendBroadcast(intent);
	 }
	

    public void QueryVersion(String tag)
    {
    	byte[] data;
    	Log.i(TAG, tag+":查询版本");

    	DataUtils dataUtils;
		dataUtils = new DataUtils();
		
    	data = dataUtils.reayTXData((byte)'H', (byte)'W', (byte)'V', (byte)'E', (byte)'R', (byte)'?', (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00);
    	//uiWriteData(data);
    	if(mConnectionState == STATE_CONNECTED)
    	{
    		writeCharacteristic(mWriteCharacteristic, data);
    	}
    }

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		// TODO Auto-generated method stub
		Log.i(TAG, "onStartCommand()");
		registerReceiver(BluetoothLeReceiver, makeGattUpdateIntentFilter());
		return START_STICKY;
	}
	
	@Override
	public void onDestroy()
	{
		// TODO Auto-generated method stub
		unregisterReceiver(BluetoothLeReceiver);
		super.onDestroy();
	}
	



}
