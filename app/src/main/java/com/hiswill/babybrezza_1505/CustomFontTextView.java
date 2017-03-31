package com.hiswill.babybrezza_1505;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class CustomFontTextView extends TextView {

	public CustomFontTextView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		init(context);
	}

	public CustomFontTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init(context);
	}

	public CustomFontTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context);
		// TODO Auto-generated constructor stub
		init(context);
	}

	private void init(Context context) {
		AssetManager am = context.getAssets();
		//Typeface font = Typeface.createFromAsset(am, "fonts/MyriadPro-Regular.otf");
		Typeface font = Typeface.createFromAsset(am, PairingPageActivity.strtypeFace);
	
		setTypeface(font);
	}

}
