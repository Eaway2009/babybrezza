package com.hiswill.babybrezza_1505;

import java.lang.reflect.Method;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Scroller;

public class VerticalLinearLayout extends ViewGroup
{
	private String TAG = "VerticalLinearLayout";
	
	/**
	 * 屏幕的高度
	 */
	private int mScreenHeight;
	/**
	 * 手指按下时的getScrollY
	 */
	private int mScrollStart;
	/**
	 * 手指抬起时的getScrollY
	 */
	private int mScrollEnd;
	/**
	 * 记录移动时的Y
	 */
	private int mLastY;
	/**
	 * 滚动的辅助类
	 */
	private Scroller mScroller;
	/**
	 * 是否正在滚动
	 */
	private boolean isScrolling;
	/**
	 * 加速度检测
	 */
	private VelocityTracker mVelocityTracker;
	/**
	 * 记录当前页
	 */
	private int currentPage = 0;
	

	private int mScreenHeight_t;
	private int lay_l,lay_t,lay_r,lay_b;

	private OnPageChangeListener mOnPageChangeListener;

	public VerticalLinearLayout(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		/**
		 * 获得屏幕的高度, 不包括虚拟功能键高度
		 */
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		DisplayMetrics outMetrics = new DisplayMetrics();
		
		display.getMetrics(outMetrics);
		mScreenHeight = outMetrics.heightPixels;
		mScreenHeight_t = mScreenHeight;
		
		int dpi = 0;
		@SuppressWarnings("rawtypes")
		Class c;
        try {
            c = Class.forName("android.view.Display");
            @SuppressWarnings("unchecked")
            Method method = c.getMethod("getRealMetrics",DisplayMetrics.class);
            method.invoke(display, outMetrics);
            dpi=outMetrics.heightPixels;
        }catch(Exception e){
            e.printStackTrace();
        } 
		
		Log.i(TAG,"context = "+context);
		Log.i(TAG,"mScreenHeight = "+mScreenHeight);
		Log.i(TAG,"dpi = "+dpi);
		
		// 初始化
		mScroller = new Scroller(context);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		Log.i(TAG,"onMeasure 1");
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int count = getChildCount();
		for (int i = 0; i < count; ++i)
		{
			Log.i(TAG,"onMeasure 2:"+i);
			View childView = getChildAt(i);
			measureChild(childView, widthMeasureSpec, heightMeasureSpec);
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b)
	{
		Log.i(TAG, ",l="+l + ",t="+t + ",r="+r + ",b="+b);
		lay_l = l;
		lay_t = t;
		lay_r = r;
		lay_b = b;
		if (changed)
		{
			int childCount = getChildCount();
			// 设置主布局的高度
			MarginLayoutParams lp = (MarginLayoutParams) getLayoutParams();
			lp.height = mScreenHeight * childCount;
			//lp.width = 480;
			setLayoutParams(lp);

			for (int i = 0; i < childCount; i++)
			{
				View child = getChildAt(i);
				if (child.getVisibility() != View.GONE)
				{
					child.layout(l, i * mScreenHeight, r, (i + 1) * mScreenHeight);// 调用每个自布局的layout
				}
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		// 如果当前正在滚动，调用父类的onTouchEvent
		if (isScrolling)
			return super.onTouchEvent(event);

		int action = event.getAction();
		int y = (int) event.getY();

		obtainVelocity(event);
		switch (action)
		{
		case MotionEvent.ACTION_DOWN:

			mScrollStart = getScrollY();
			mLastY = y;
			break;
		case MotionEvent.ACTION_MOVE:

			if (!mScroller.isFinished())
			{
				mScroller.abortAnimation();
			}

			int dy = mLastY - y;
			// 边界值检查
			int scrollY = getScrollY();
			// 已经到达顶端，下拉多少，就往上滚动多少
			if (dy < 0 && scrollY + dy < 0)
			{
				dy = -scrollY;
			}
			// 已经到达底部，上拉多少，就往下滚动多少
			if (dy > 0 && scrollY + dy > getHeight() - mScreenHeight)
			{
				dy = getHeight() - mScreenHeight - scrollY;
			}

			scrollBy(0, dy);
			mLastY = y;
			break;
		case MotionEvent.ACTION_UP:

			mScrollEnd = getScrollY();

			int dScrollY = mScrollEnd - mScrollStart;

			if (wantScrollToNext())// 往上滑动
			{
				if (shouldScrollToNext())
				{
					mScroller.startScroll(0, getScrollY(), 0, mScreenHeight - dScrollY);

				} else
				{
					mScroller.startScroll(0, getScrollY(), 0, -dScrollY);
				}

			}

			if (wantScrollToPre())// 往下滑动
			{
				if (shouldScrollToPre())
				{
					mScroller.startScroll(0, getScrollY(), 0, -mScreenHeight - dScrollY);

				} else
				{
					mScroller.startScroll(0, getScrollY(), 0, -dScrollY);
				}
			}
			isScrolling = true;
			postInvalidate();
			recycleVelocity();
			break;
		}

		return true;
	}

	/**
	 * 根据滚动距离判断是否能够滚动到下一页
	 * 
	 * @return
	 */
	private boolean shouldScrollToNext()
	{
		return mScrollEnd - mScrollStart > mScreenHeight / 2 || Math.abs(getVelocity()) > 600;
	}

	/**
	 * 根据用户滑动，判断用户的意图是否是滚动到下一页
	 * 
	 * @return
	 */
	private boolean wantScrollToNext()
	{
		return mScrollEnd > mScrollStart;
	}

	/**
	 * 根据滚动距离判断是否能够滚动到上一页
	 * 
	 * @return
	 */
	private boolean shouldScrollToPre()
	{
		return -mScrollEnd + mScrollStart > mScreenHeight / 2 || Math.abs(getVelocity()) > 600;
	}

	/**
	 * 根据用户滑动，判断用户的意图是否是滚动到上一页
	 * 
	 * @return
	 */
	private boolean wantScrollToPre()
	{
		return mScrollEnd < mScrollStart;
	}

	@Override
	public void computeScroll()
	{
		super.computeScroll();
		if (mScroller.computeScrollOffset())
		{
			scrollTo(0, mScroller.getCurrY());
			postInvalidate();
		} else
		{

			int position = getScrollY() / mScreenHeight;

			Log.e("xxx", position + "," + currentPage);
			if (position != currentPage)
			{
				if (mOnPageChangeListener != null)
				{
					currentPage = position;
					mOnPageChangeListener.onPageChange(currentPage);
				}
			}

			isScrolling = false;
		}

	}

	/**
	 * 获取y方向的加速度
	 * 
	 * @return
	 */
	private int getVelocity()
	{
		mVelocityTracker.computeCurrentVelocity(1000);
		return (int) mVelocityTracker.getYVelocity();
	}

	/**
	 * 释放资源
	 */
	private void recycleVelocity()
	{
		if (mVelocityTracker != null)
		{
			mVelocityTracker.recycle();
			mVelocityTracker = null;
		}
	}

	/**
	 * 初始化加速度检测器
	 * 
	 * @param event
	 */
	private void obtainVelocity(MotionEvent event)
	{
		if (mVelocityTracker == null)
		{
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(event);
	}

	/**
	 * 设置回调接口
	 * 
	 * @param onPageChangeListener
	 */
	public void setOnPageChangeListener(OnPageChangeListener onPageChangeListener)
	{
		mOnPageChangeListener = onPageChangeListener;
	}

	/**
	 * 回调接口
	 * 
	 * @author zhy
	 * 
	 */
	public interface OnPageChangeListener
	{
		void onPageChange(int currentPage);
	}
	
	public void setLayoutHeight(int h)
	{
		//mScreenHeight = h;
		mScreenHeight = mScreenHeight_t-h;
		onLayout(true,lay_l,lay_t,lay_r,lay_b);
		postInvalidate();
	}
	public int getLayoutHeight()
	{
		return mScreenHeight;
	}
}
