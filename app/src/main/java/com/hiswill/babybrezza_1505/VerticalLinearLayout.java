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
	 * ��Ļ�ĸ߶�
	 */
	private int mScreenHeight;
	/**
	 * ��ָ����ʱ��getScrollY
	 */
	private int mScrollStart;
	/**
	 * ��ָ̧��ʱ��getScrollY
	 */
	private int mScrollEnd;
	/**
	 * ��¼�ƶ�ʱ��Y
	 */
	private int mLastY;
	/**
	 * �����ĸ�����
	 */
	private Scroller mScroller;
	/**
	 * �Ƿ����ڹ���
	 */
	private boolean isScrolling;
	/**
	 * ���ٶȼ��
	 */
	private VelocityTracker mVelocityTracker;
	/**
	 * ��¼��ǰҳ
	 */
	private int currentPage = 0;
	

	private int mScreenHeight_t;
	private int lay_l,lay_t,lay_r,lay_b;

	private OnPageChangeListener mOnPageChangeListener;

	public VerticalLinearLayout(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		/**
		 * �����Ļ�ĸ߶�, ���������⹦�ܼ��߶�
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
		
		// ��ʼ��
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
			// ���������ֵĸ߶�
			MarginLayoutParams lp = (MarginLayoutParams) getLayoutParams();
			lp.height = mScreenHeight * childCount;
			//lp.width = 480;
			setLayoutParams(lp);

			for (int i = 0; i < childCount; i++)
			{
				View child = getChildAt(i);
				if (child.getVisibility() != View.GONE)
				{
					child.layout(l, i * mScreenHeight, r, (i + 1) * mScreenHeight);// ����ÿ���Բ��ֵ�layout
				}
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		// �����ǰ���ڹ��������ø����onTouchEvent
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
			// �߽�ֵ���
			int scrollY = getScrollY();
			// �Ѿ����ﶥ�ˣ��������٣������Ϲ�������
			if (dy < 0 && scrollY + dy < 0)
			{
				dy = -scrollY;
			}
			// �Ѿ�����ײ����������٣������¹�������
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

			if (wantScrollToNext())// ���ϻ���
			{
				if (shouldScrollToNext())
				{
					mScroller.startScroll(0, getScrollY(), 0, mScreenHeight - dScrollY);

				} else
				{
					mScroller.startScroll(0, getScrollY(), 0, -dScrollY);
				}

			}

			if (wantScrollToPre())// ���»���
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
	 * ���ݹ��������ж��Ƿ��ܹ���������һҳ
	 * 
	 * @return
	 */
	private boolean shouldScrollToNext()
	{
		return mScrollEnd - mScrollStart > mScreenHeight / 2 || Math.abs(getVelocity()) > 600;
	}

	/**
	 * �����û��������ж��û�����ͼ�Ƿ��ǹ�������һҳ
	 * 
	 * @return
	 */
	private boolean wantScrollToNext()
	{
		return mScrollEnd > mScrollStart;
	}

	/**
	 * ���ݹ��������ж��Ƿ��ܹ���������һҳ
	 * 
	 * @return
	 */
	private boolean shouldScrollToPre()
	{
		return -mScrollEnd + mScrollStart > mScreenHeight / 2 || Math.abs(getVelocity()) > 600;
	}

	/**
	 * �����û��������ж��û�����ͼ�Ƿ��ǹ�������һҳ
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
	 * ��ȡy����ļ��ٶ�
	 * 
	 * @return
	 */
	private int getVelocity()
	{
		mVelocityTracker.computeCurrentVelocity(1000);
		return (int) mVelocityTracker.getYVelocity();
	}

	/**
	 * �ͷ���Դ
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
	 * ��ʼ�����ٶȼ����
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
	 * ���ûص��ӿ�
	 * 
	 * @param onPageChangeListener
	 */
	public void setOnPageChangeListener(OnPageChangeListener onPageChangeListener)
	{
		mOnPageChangeListener = onPageChangeListener;
	}

	/**
	 * �ص��ӿ�
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
