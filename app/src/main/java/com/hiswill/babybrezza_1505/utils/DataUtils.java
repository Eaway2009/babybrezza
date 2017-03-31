package com.hiswill.babybrezza_1505.utils;

import android.util.Log;

public class DataUtils
{
		
	/**
	 * 准备数据
	 */
	public byte[] reayTXData(byte command,byte hour,byte minute,byte second,byte capacity,byte speedModel, byte workingModel,byte temperature, byte version,byte save,byte save_t,byte status)
	{
		byte[] data=new byte[15];
		
		data[0] = (byte) 0xA5;
		data[1] = (byte) 0xFF;
		data[2] = (byte) (command & 0XFF);
		data[3] = (byte) (hour & 0XFF);
		data[4] = (byte) (minute & 0XFF);
		data[5] = (byte) (second & 0XFF);
		data[6] = (byte) (capacity & 0XFF);
		data[7] = (byte) (speedModel & 0XFF);
		data[8] = (byte) (workingModel & 0XFF);
		data[9] = (byte) (temperature & 0XFF);
		data[10] = (byte) (version & 0XFF);
		data[11] = (byte) (save & 0XFF);
		data[12] = (byte) (save_t & 0XFF);
		data[13] = (byte) (status & 0XFF);
		
		//校验和
		int sum = 0;
		for (int i = 0; i <=13; i++)
		{
			sum+=data[i];
		}
		
		data[14] = (byte) (sum & 0XFF);
		//Log.i("DataUtils", "16进制显示："+encodeHex(sum));
		return data;
	}
	
	/**
	 * int转16进制
	 * @param integer
	 * @return
	 */
	public String encodeHex(int integer) { 
		 StringBuffer buf = new StringBuffer(2); 
		 if (((int) integer & 0xff) < 0x10) 
		 { 
			 buf.append("0"); 
		 } 
		 buf.append(Long.toString((int) integer & 0xff, 16)); 
		 return buf.toString(); 
		 
	}
	
	
}
