package com.hiswill.babybrezza_1505.wheel;
import com.hiswill.babybrezza_1505.R.color;

import android.graphics.Color;

/**
 * ��ӭ��ҷ���
 * http://www.topithome.com
 * רҵ�ļ�������,��ӭ��ҵķ���!
 * 
 * �����Ŀ��������ܼ�����ַ
 * http://www.topithome.com/androidwheelviewself/
 * 
 * ljw ��ӵ� �� WheelView ���� �õ����������ݣ�����ժ¼��
 * д����������ļ���
 * @author Administrator
 *
 */
public class WheelViewConfig {
	
	/**
	 * �ж� �����е��� �ַ����������ε����֣�
	 * true�����ַ�������Ϊ true ʱ�������� ���ֵ ��Сֵ ��Ч�ģ������õ�
	 * int�������� ���Լ��㣬�� �������ֵ����Сֵ���ͼ�������Զ����㡣С��100����200�����50��
	 * �� ������ ����ʾ 100,150,200��
	 */
	/*public static boolean showStrOrInt=false;*/
	
	/**
	 * �����е���ʾ�������� �ַ��� ��ʱ��ֱ�Ӹ���� list ��ֵ ���ɡ�
	 */
	//public static List<String> showStrListData;
	
	
	
	
	/** The default min value������ ������ ���ֵ���ʽ�Ļ�������ֵ */
	public static final int DEFAULT_MAX_VALUE = 10;

	/** The default max value������ ������ ���ֵ���ʽ�Ļ�����С��ֵ  */
	public static final int DEFAULT_MIN_VALUE = 1;
	
	/** Minimum delta for scrolling;��������֮��� ��� ��С */
	public static int MIN_DELTA_FOR_SCROLLING = 1;
	
	
	
	
	/** Minimum delta for scrolling���� ѡ�� ��Ŀ���������ɫ��ǰ��λ ����͸���ȣ��� 6λ����ɫ�� 16���Ʊ�ʾ��
	 *  ���� alpha��00��ʾ��ȫ͸����ff��ʾ��ȫ��͸���� */
	public static final int VALUE_TEXT_COLOR = color.BLACK70;//Color.BLACK;//Color.BLUE;
	
	/** Items text color ;�� δѡ�е��������ɫ*/
	public static final int ITEMS_TEXT_COLOR = 0xF07b7b7b;//Color.BLACK;
}
