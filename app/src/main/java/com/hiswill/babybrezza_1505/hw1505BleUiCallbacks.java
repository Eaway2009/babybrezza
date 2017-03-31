package com.hiswill.babybrezza_1505;


public interface hw1505BleUiCallbacks {

	public void uiSystemBluetoothOn();
	public void uiSystemBluetoothOff();
	public void uiDeviceConnected();
	
	public void uiDeviceDisconnected();
	public void uiServiceDiscovered();
	public void uiNewConnection();
	public void uiActivityFinish();	
	public void uiCommunicated();

	public void uiSuccessfulWrite();
	public void uiFailedWrite();
	public void uiBluetoothErr133();
	public void uiGetData(byte rCommand, byte rHour, byte rMinute, byte rSecond, byte rCapacity, byte rSpeedModel, byte rWorkingModel, byte rStatus);
	
	//implement
	public static class Null implements hw1505BleUiCallbacks 
	{
		@Override
		public void uiSystemBluetoothOn(){}
		@Override
		public void uiSystemBluetoothOff(){}
		@Override
		public void uiDeviceConnected() {}
		@Override
		public void uiDeviceDisconnected() {}
		@Override
		public void uiServiceDiscovered() {}
		@Override
		public void uiNewConnection(){}
		@Override
		public void uiActivityFinish(){}
		@Override	
		public void uiCommunicated(){}
		@Override
		public void uiSuccessfulWrite() {}
		@Override
		public void uiFailedWrite() {}
		@Override
		public void uiBluetoothErr133(){}
		
		@Override
		public void uiGetData(byte rCommand, byte rHour, byte rMinute, byte rSecond, byte rCapacity, byte rSpeedModel, byte rWorkingModel, byte rStatus)	{}
	}

}
