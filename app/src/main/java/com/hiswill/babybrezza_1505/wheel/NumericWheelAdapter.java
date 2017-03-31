/*
 *  Copyright 2010 Yuri Kanivets
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.hiswill.babybrezza_1505.wheel;
import java.util.List;



/**
 * Numeric Wheel adapter.
 */
public class NumericWheelAdapter implements WheelAdapter {

	public List<String> showStrListData;
	/**
	 * �ж� �����е��� �ַ����������ε����֣�
	 * true�����ַ�������Ϊ true ʱ�������� ���ֵ ��Сֵ ��Ч�ģ������õ�
	 * int�������� ���Լ��㣬�� �������ֵ����Сֵ���ͼ�������Զ����㡣С��100����200�����50��
	 * �� ������ ����ʾ 100,150,200��
	 */
	public boolean showStrOrInt;
	
	/** The default min value */
	public static final int DEFAULT_MAX_VALUE = WheelViewConfig.DEFAULT_MAX_VALUE;

	/** The default max value */
	private static final int DEFAULT_MIN_VALUE =  WheelViewConfig.DEFAULT_MIN_VALUE;
	
	// Values
	private int minValue;
	private int maxValue;
	
	// format
	private String format;
	
	/**
	 * Default constructor
	 */
	public NumericWheelAdapter() {
		this(DEFAULT_MIN_VALUE, DEFAULT_MAX_VALUE);
	}

	/**
	 * Constructor
	 * @param minValue the wheel min value
	 * @param maxValue the wheel max value
	 */
	public NumericWheelAdapter(int minValue, int maxValue) {
		this(minValue, maxValue, null);
	}
	
	/**
	 * ljw ��ӵķ���
	 * Constructor
	 * @param minValue the wheel min value
	 * @param maxValue the wheel max value
	 * @param showStrOrInt true ���� ��ʾ�����ַ���
	 */
	public NumericWheelAdapter(int minValue, int maxValue,boolean showStrOrInt,List<String> showListSource) {
		this(minValue, maxValue, null);
		this.showStrListData=showListSource;
		this.showStrOrInt=showStrOrInt;
	}


	/**
	 * Constructor
	 * @param minValue the wheel min value
	 * @param maxValue the wheel max value
	 * @param format the format string
	 */
	public NumericWheelAdapter(int minValue, int maxValue, String format) {
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.format = format;
	}

	@Override
	public String getItem(int index) {
		 
		// ԭ�� ��������ʾ�����������ֵ�ʱ��ֻ��Ҫ ��Щ����
		/*
		if (index >= 0 && index < getItemsCount()) {
			//int value = minValue + (getItemsCount()-index-1)*WheelViewConfig.MIN_DELTA_FOR_SCROLLING;
			int tempData=getItemsCount()-((getItemsCount()-index-1))-1;
			int value = minValue +tempData*WheelViewConfig.MIN_DELTA_FOR_SCROLLING;
			return format != null ? String.format(format, value) : Integer.toString(value);
		}
		*/
		
		//���� �Լ������� ��������ʾ�������� �ַ�����ʱ���������������޸�Ϊ���£�
		if(showStrOrInt==true){//������ʾ�� ��������ʾ������ �� ���ַ�����
			
			return showStrListData.get(index);
		}else{//������ʾ�� ��������ʾ������ �� ��������ʽ���������Զ����м���
			if (index >= 0 && index < getItemsCount()) {
				//int value = minValue + (getItemsCount()-index-1)*WheelViewConfig.MIN_DELTA_FOR_SCROLLING;
				int tempData=getItemsCount()-((getItemsCount()-index-1))-1;
				int value = minValue +tempData*WheelViewConfig.MIN_DELTA_FOR_SCROLLING;
				return format != null ? String.format(format, value) : Integer.toString(value);
			}
		}
		
		return null;
	}

	@Override
	public int getItemsCount() {
		//���� �Լ������� ��������ʾ�������� �ַ�����ʱ���������������޸�Ϊ���£�
		if(showStrOrInt==true){//������ʾ�� ��������ʾ������ �� ���ַ�����
			
			return showStrListData.size();
		}else{//������ʾ�� ��������ʾ������ �� ��������ʽ���������Զ����м���
			int result= 0;
			result=(maxValue - minValue)/WheelViewConfig.MIN_DELTA_FOR_SCROLLING + 1;
			return result;
		}
	}
	
	@Override
	public int getMaximumLength() {
		int max = Math.max(Math.abs(maxValue), Math.abs(minValue));
		int maxLen = Integer.toString(max).length();
		if (minValue < 0) {
			maxLen++;
		}
		//return maxLen;
		return maxLen;
	}
}
