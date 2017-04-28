package com.hiswill.babybrezza_1505;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.Button;

import com.hiswill.babybrezza_1505.wheel.NumericWheelAdapter;
import com.hiswill.babybrezza_1505.wheel.WheelView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by xn058827 on 2017/3/28.
 */

public class TestActivity extends Activity {
    private String[] ItemCapacity;
    private int capItems;
    private NumericWheelAdapter numericWheelAdapterCapacity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        switchLanguage();
        setContentView(R.layout.warmeroperater_page);
        ItemCapacity=new String[]
                {
                        getString(R.string.defrost)
                };
        initWheelForShowCapacity();
    }

    private void initWheelForShowCapacity()
    {
        List<String> showStrListData = new ArrayList<String>();
        //showStrListData.removeAll(showStrListData);

        for(int i=0; i<ItemCapacity.length; i++)
        {
            showStrListData.add(ItemCapacity[i]);
        }

        capItems = ItemCapacity.length;

        WheelView wheel = getWheel(R.id.wv_capacity);
        numericWheelAdapterCapacity = new NumericWheelAdapter(1, 10000, true, showStrListData);
        numericWheelAdapterCapacity.showStrOrInt=true;
        wheel.setAdapter(numericWheelAdapterCapacity);
        wheel.setCyclic(true);
        wheel.setInterpolator(new AnticipateOvershootInterpolator());
        //wheel.setAlpha((float) 0.5);

        //wheel.setCurrentItem(0);
        wheel.setCurrentItem(showStrListData.size()-1);
        wheel.setVisibility(View.VISIBLE);
        wheel.setEnabled(true);
    }

    private WheelView getWheel(int id)
    {
        return (WheelView) findViewById(id);
    }

    private void switchLanguage() {
        Resources resources = getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.locale = Locale.ENGLISH;
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
//        SharedPreferences.Editor editor = getSharedPreferences(Constants.NAME_SP, MODE_PRIVATE).edit();
//        editor.putInt(Constants.KEY_SP_LANGUAGE, language);
//        editor.commit();
    }
}
