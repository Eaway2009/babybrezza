package com.hiswill.babybrezza_1505;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ScrollView;


public class SlowScrollView extends ScrollView 
{
    private float spdFactor;
	
    public SlowScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        spdFactor = 1;
    }

    public SlowScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        spdFactor = 1;
    }

    public SlowScrollView(Context context) {
        super(context);
        spdFactor = 1;
    }

    /**
     * 滑动事件
     */
    @Override
    public void fling(int velocityY) 
    {
        Log.i("velocityY","velocityY="+velocityY);
        Log.i("velocityY","velocityY*spdFactor=" + velocityY*spdFactor);
        super.fling((int)(velocityY/4));//这里设置滑动的速度
    }
    
    public void setSpeedFactor(float factor)
    {
    	spdFactor = factor;
        Log.i("velocityY","factor="+factor);
    }
    
    @Override  
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {  
        super.onScrollChanged(x, y, oldx, oldy);  
        
        //Log.i("scroll",",x="+x+",y="+y+",oldx="+oldx+",oldy="+oldy);
        //if (scrollViewListener != null) {  
        //    scrollViewListener.onScrollChanged(this, x, y, oldx, oldy);  
        //}  
    }  
}



