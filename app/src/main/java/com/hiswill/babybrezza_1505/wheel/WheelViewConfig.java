package com.hiswill.babybrezza_1505.wheel;
import com.hiswill.babybrezza_1505.R.color;

import android.graphics.Color;

/**
 * 欢迎大家访问
 * http://www.topithome.com
 * 专业的技术博客,欢迎大家的访问!
 * 
 * 这个项目的详情介绍见下网址
 * http://www.topithome.com/androidwheelviewself/
 * 
 * ljw 添加的 对 WheelView 其中 用到的配置数据，进行摘录。
 * 写在这个配置文件中
 * @author Administrator
 *
 */
public class WheelViewConfig {
	
	/**
	 * 判断 滚轮中的是 字符串还是整形的数字；
	 * true——字符串；若为 true 时，给出的 最大值 最小值 无效的；不会用到
	 * int——数字 可以计算，如 给出最大值，最小值，和间隔。可自动计算。小：100；大：200；间隔50；
	 * 则 滚轮中 则显示 100,150,200；
	 */
	/*public static boolean showStrOrInt=false;*/
	
	/**
	 * 滚轮中的显示的内容是 字符串 的时候，直接给这个 list 赋值 即可。
	 */
	//public static List<String> showStrListData;
	
	
	
	
	/** The default min value；滚轮 中若是 数字的形式的话，最大的值 */
	public static final int DEFAULT_MAX_VALUE = 10;

	/** The default max value；滚轮 中若是 数字的形式的话，最小的值  */
	public static final int DEFAULT_MIN_VALUE = 1;
	
	/** Minimum delta for scrolling;滚动数字之间的 间隔 大小 */
	public static int MIN_DELTA_FOR_SCROLLING = 1;
	
	
	
	
	/** Minimum delta for scrolling；即 选中 项目的字体的颜色；前两位 代表透明度，后 6位是颜色的 16进制表示；
	 *  对于 alpha，00表示完全透明，ff表示完全不透明。 */
	public static final int VALUE_TEXT_COLOR = color.BLACK70;//Color.BLACK;//Color.BLUE;
	
	/** Items text color ;即 未选中的字体的颜色*/
	public static final int ITEMS_TEXT_COLOR = 0xF07b7b7b;//Color.BLACK;
}
