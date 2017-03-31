package com.hiswill.babybrezza_1505;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hiswill.babybrezza_1505.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.util.Log;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Toast;
 

public class SettiingGuideActivity extends Activity 
{
	private String TAG = "SettiingGuideActivity";
	private SharedPreferences mSettings;

	private SlowScrollView mScrollView;
	private ImageView scroll_iv1,scroll_iv2,scroll_iv3,scroll_iv4;
	private boolean iniIV;
	
	private TextView tv_back_set;
	private ImageView iv_back;
	//private ImageView iv_setting_index;
	
	private int mSpeedMode,mTempMode;
	private int PageNumber;
	private int pgHeight;
	private int scrollStep;
	private int scrollPosit;
	private boolean bScrollDown;
	Handler handler = new Handler();

	float x1 = 0;
	float x2 = 0;
	float y1 = 0;
	float y2 = 0;

	//private BluetoothLeService	sgBleService; 
	//private String DevName,DevAddress;
	//private boolean LinkOk;
	//private boolean bQueryVersion 	= false;
	//private int     QueryVersionCnt	= 0;
	
	private static final int NONE = 0;  
    private static final int MOVE = 1;  
    private static final int ZOOM = 2;  
    private static final int ROTATION = 1;  
      
    private Matrix matrix = new Matrix();  
    private Matrix savedMatrix = new Matrix();  
    private PointF start = new PointF();  
    private PointF mid = new PointF();  
    private float oldDistance;
	int pointerId = 0;   

	private hw1505BleComm mHw1505BleComm;
	
 	@Override
	protected void onCreate(Bundle savedInstanceState) 
 	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Log.i(TAG,"onCreate");
		setContentView(R.layout.setting_guide_page);
		
		Intent i=new Intent();

		mSettings = getSharedPreferences(Constants.NAME_SP,Context.MODE_PRIVATE);
	}

	//
 	@Override
	protected void onStart() 
 	{
		// TODO Auto-generated method stub
		super.onStart();
		Log.i(TAG,"onStart");
		
		//Intent intent = getIntent();
		//mSpeedMode 		= intent.getIntExtra("SpeedMode", 1);
		//mTempMode  		= intent.getIntExtra("TempMode", 1);

		mTempMode 	= mSettings.getInt("set_workingModel",1);
		mSpeedMode 	= mSettings.getInt("set_speedModel",1);
		Log.i(TAG,"SpeedMode="+mSpeedMode+",TempMode="+mTempMode);
		
		initView();
		iv_back = (ImageView) findViewById(R.id.iv_back);
		iv_back.setOnClickListener(listener);

		tv_back_set = (TextView) findViewById(R.id.tv_back_set);
		tv_back_set.setOnClickListener(listener);		

		scroll_iv1 = (ImageView)findViewById(R.id.iv_setting_guide1);
		scroll_iv2 = (ImageView)findViewById(R.id.iv_setting_guide2);
		scroll_iv3 = (ImageView)findViewById(R.id.iv_setting_guide3);
		scroll_iv4 = (ImageView)findViewById(R.id.iv_setting_guide4);

		iniIV = false;
		PageNumber = 0;	
		//PageNumber = (mTempMode-1)*2 + (mSpeedMode-1);
		//ShowCurrentPage();

		mScrollView=(SlowScrollView) findViewById(R.id.scrollView_showMessages);
		mScrollView.setSpeedFactor(5.0f);
		
		//if(false)
		{
		mScrollView.setOnTouchListener(new View.OnTouchListener() 
		{
			private float startX, startY, offsetX, offsetY;

			@Override
			public boolean onTouch(View v, MotionEvent event) 
			{ 
				//Log.i(TAG,"onTouch");
				//int pointerCount = event.getPointerCount(); 
				int action = (event.getAction() & MotionEvent.ACTION_MASK) % 5;//统一单点和多点
				
				boolean ret = false;
				
				switch (action) 
				{
					case MotionEvent.ACTION_DOWN:
	                	//Log.i(TAG,"ACTION_DOWN,"+pointerCount+" points");
	                    pointerId = (event.getAction()&MotionEvent.ACTION_POINTER_INDEX_MASK)>>> MotionEvent.ACTION_POINTER_INDEX_SHIFT; 
						//Log.i(TAG,"pointerId,"+pointerId);
						
						if(pointerId == 0)
						{
							startX = event.getX();
							startY = event.getY();
						}
						break;
						
					case MotionEvent.ACTION_UP:
	                	//Log.i(TAG,"ACTION_UP,"+pointerCount+" points");  
	                	
						if(pointerId > 0) break;
						
						offsetX = event.getX() - startX;
						offsetY = event.getY() - startY;

						if (Math.abs(offsetX) > Math.abs(offsetY)) 
						{
							//if (offsetX < -50)		Log.i(TAG,"===== left");
							//else if (offsetX > 50)	Log.i(TAG,"===== right");
						}
						else
						{
							if (offsetY < -100)
							{
								//Log.i(TAG,"===== up");
								if(PageNumber < 3)
								{
									PageNumber++;
									bScrollDown = true;
								}
							}
							else if (offsetY > 100)
							{
								//Log.i(TAG,"===== down");
								if(PageNumber > 0)
								{
									PageNumber--;
									bScrollDown = false;
								}
							}
							ShowCurrentPage();
							ret = true;
						}
						break;

	                case MotionEvent.ACTION_POINTER_UP:
	                	//Log.i(TAG,"ACTION_POINTER_UP,"+pointerCount+" points");  
	                	break;

	                case MotionEvent.ACTION_POINTER_DOWN:  
	                	//Log.i(TAG,"ACTION_POINTER_DOWN,"+pointerCount+" points");
	                	break;
						
					case MotionEvent.ACTION_MOVE:
	                	//Log.i(TAG,"ACTION_MOVE,"+pointerCount+" points");
	                    break;
				}
			
				//iv_setting_index.setImageMatrix(matrix);
				return ret;
			}
		});
 		}
	}

 	private float getDistance(float x0, float x1, float y0, float y1)
 	{
 		float Distance;
 		Distance = (float)Math.sqrt((x0-x1)*(x0-x1)+(y0-y1)*(y0-y1));  
 		
 		return Distance;
 	}
 	
	private void initView() 
	{
	}
	

	private OnClickListener listener = new OnClickListener()
	{

		@Override
		public void onClick(View v) 
		{
			// TODO Auto-generated method stub
			switch (v.getId())
			{
			case R.id.iv_back:
			case R.id.tv_back_set:
				Log.i(TAG,"finish()");
				//if(parentActivity.equals("BottleIsReadyActivity"))
				finish();
				break;
			}
		}
	};
	
	private Runnable runnableScroll = new Runnable() 
	{
	    @Override  
	    public void run() 
	    {
	    	//Jump to the position
	    	//mScrollView.smoothScrollTo(0, PageNumber*pgHeight);
	    	
	    	//* scroll by timer
	    	//mScrollView.scrollTo(0, PageNumber*pgHeight);	    	
	    	int scrollTo;
	    	
	    	scrollTo = PageNumber*pgHeight;
	    	scrollPosit += scrollStep;
			
	    	if(bScrollDown)
	    	{
	    		if(scrollPosit >= scrollTo)	scrollPosit = scrollTo;
	    	}
	    	else
	    	{
	    		if(scrollPosit <= scrollTo)	scrollPosit = scrollTo;
	    	}
	    	mScrollView.scrollTo(0,scrollPosit);

			//Log.i(TAG,"scrollTo= "+scrollTo+ ", scrollPosit= "+scrollPosit+ ", scrollStep = "+scrollStep);
	    	
	    	if(scrollPosit != scrollTo)
	    	{
	    		int diff = Math.abs(scrollTo-scrollPosit);
	    		if(diff > 50) diff = diff>>1;
	    		scrollStep = diff;

		    	//scrollStep = (scrollStep*3)>>3;			//?/8 
				//scrollStep = (int)Math.max(50,Math.abs(scrollStep));

	    		if(bScrollDown == false) scrollStep = -scrollStep;
	    		handler.postDelayed(this, 20);
	    	}
	    	//*/
	    }
	    
	};
	

	private void ScrollToCurrentPage()
	{
		//
		scrollPosit= mScrollView.getScrollY();
		scrollStep = (int)Math.max(10,Math.abs((PageNumber*pgHeight-mScrollView.getScrollY())/2));
		if(bScrollDown == false) scrollStep = -scrollStep;
		
		handler.postDelayed(runnableScroll, 200);
		
	}
	
	private void ShowCurrentPage()
	{
		//handler.postDelayed(runnableScroll, 200);
		
		pgHeight = scroll_iv1.getHeight();
		ScrollToCurrentPage();

		/*
    	//if(iniIV == true)
    	//{
    	//	iniIV = false;
		//	scroll_iv1.setImageResource(R.drawable.quick_room);
		//	scroll_iv2.setImageResource(R.drawable.quick_cold);
		//	scroll_iv3.setImageResource(R.drawable.steady_room);
		//	scroll_iv4.setImageResource(R.drawable.steady_cold);
    	//}
		
		Log.i(TAG,"image.getMeasuredHeight:"+scroll_iv1.getMeasuredHeight());
		Log.i(TAG,"image.getHeight:"+scroll_iv1.getHeight());
		Log.i(TAG,"image.getWidth:"+scroll_iv1.getWidth());

		LinearLayout linearLayout_sv = (LinearLayout)findViewById(R.id.layoutScrollView);
		Log.i(TAG,"linearLayout getMeasuredHeight:"+linearLayout_sv.getMeasuredHeight());
		Log.i(TAG,"linearLayout getMeasuredWidth:"+linearLayout_sv.getMeasuredWidth());
		//*/
		
		
		/*
		Log.i(TAG,"getBaseline:"+mScrollView.getBaseline());
		Log.i(TAG,"getChildCount:"+mScrollView.getChildCount());
		Log.i(TAG,"getHeight:"+mScrollView.getHeight());
		Log.i(TAG,"getTotalVerticalScrollRange:"+mScrollView.computeVerticalScrollRange());
		
		//mScrollView.focusSearch(View.FOCUS_DOWN);
		//*/
		
		
		/*
		switch(PageNumber) 
		{
		case 0:
			iv_setting_index.setImageResource(R.drawable.quick_room);
			break;
		case 1:
			iv_setting_index.setImageResource(R.drawable.steady_room);
			break;
		case 2:
			iv_setting_index.setImageResource(R.drawable.quick_cold);
			break;
		case 3:
			iv_setting_index.setImageResource(R.drawable.steady_cold);
			break;
		}
		*/
	}

	/*
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		//继承了Activity的onTouchEvent方法，直接监听点击事件
		Log.i(TAG,"onTouchEvent");
		if(event.getAction() == MotionEvent.ACTION_DOWN) 
		{
			//当手指按下的时候
			x1 = event.getX();
			y1 = event.getY();
		 }
		if(event.getAction() == MotionEvent.ACTION_UP) 
		{
			//当手指离开的时候
			x2 = event.getX();
			y2 = event.getY();
			if(y1 - y2 > 50) 
			{
				//Toast.makeText(this, "向上滑", Toast.LENGTH_SHORT).show();
				Log.i(TAG,"向上滑");
	       		 if(PageNumber > 0)
	       		 {
	       			 PageNumber--;
	       			 ShowCurrentPage();
	       		 }
			} 
			else if(y2 - y1 > 50)
			{
				//Toast.makeText(this, "向下滑", Toast.LENGTH_SHORT).show();
				Log.i(TAG,"向下滑");
	       		 if(PageNumber < 3)
	       		 {
	       			 PageNumber++;
	       			 ShowCurrentPage();
	       		 }
			} 
			else if(x1 - x2 > 50)
			{
				//Toast.makeText(this, "向左滑", Toast.LENGTH_SHORT).show();
				Log.i(TAG,"向左滑");
			} else if(x2 - x1 > 50) 
			{
				//Toast.makeText(this, "向右滑", Toast.LENGTH_SHORT).show();
				Log.i(TAG,"向右滑");
			}
		}
		return super.onTouchEvent(event);
	}
	*/
	
	@Override
	protected void onResume()
	{
		super.onResume();

        if(mHw1505BleComm == null)
        {
    		Log.i(TAG,"new hw1505BleComm");
        	mHw1505BleComm = new hw1505BleComm(this,new hw1505BleUiCallbacks.Null()
        	{
	        	@Override
	        	public void uiCommunicated()
	        	{
	            	runOnUiThread(new Runnable()
	            	{
	        			@Override
	        			public void run() {
	        			}
	            	});
	        	}
	        	@Override
	        	public void uiDeviceDisconnected() 
	        	{
	        		runOnUiThread(new Runnable()
	        		{
						@Override
						public void run() {
						}
	        		});
	        	}
	        	
	        	@Override
	        	public void uiBluetoothErr133() 
	        	{
	        		runOnUiThread(new Runnable()
	        		{
						@Override
						public void run() {
						}
	        		});
	        	}
	    		@Override
	    		public void uiActivityFinish()
	    		{
	        		runOnUiThread(new Runnable()
	        		{
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
	}
	
	@Override
	protected void onDestroy()
	{
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.i(TAG, "onDestroy");
		mHw1505BleComm.ActivtiyDestroyed();
	}
}
