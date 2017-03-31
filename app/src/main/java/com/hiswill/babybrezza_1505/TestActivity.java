package com.hiswill.babybrezza_1505;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.Locale;

/**
 * Created by xn058827 on 2017/3/28.
 */

public class TestActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        switchLanguage();
        setContentView(R.layout.showstate_page);
        AutofitTextView autofitTextView = (AutofitTextView) findViewById(R.id.bottle_warmer_connected_text);
        Resources resources = getResources();
        Configuration configuration = resources.getConfiguration();
        if(configuration.locale == Locale.ENGLISH){
            autofitTextView.setMaxLines(2);
        }else{
            autofitTextView.setMaxLines(3);
        }
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
