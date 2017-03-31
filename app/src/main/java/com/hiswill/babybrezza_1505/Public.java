package com.hiswill.babybrezza_1505;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

public class Public {
	 
    public static void ShowAlert(String title, String msg, Context context) {
	    new AlertDialog.Builder(context)
	    .setIcon(android.R.drawable.ic_dialog_alert)
	    .setTitle(title)
	    .setMessage(msg)
	    .setCancelable(false)
	    .setNegativeButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        })
	    .show();
    }
    
    public static void ShowInfo(String title, String msg, Context context) {
	    new AlertDialog.Builder(context)
	    .setIcon(android.R.drawable.ic_dialog_info)
	    .setTitle(title)
	    .setMessage(msg)
	    .setCancelable(false)
	    .setNegativeButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        })
	    .show();
    }
    
    public static boolean is_hex_char(String str) {
    	for (int i=0; i<str.length(); i++) {
    		char c = str.charAt(i);
    		
    		if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F'))) {
    			return false;
    		}
    	}
    	return true;
    }
    

	public static String getAppVersionName(Context context) {
		String versionName = "";
		try {
			// ---get the package info---
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
			versionName = pi.versionName;
			if (versionName == null || versionName.length() <= 0) {
				return "1.0";
			}
		} catch (Exception e) {
			Log.e("VersionInfo", "Exception", e);
		}
		return versionName;
	}
	
}
