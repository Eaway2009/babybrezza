package com.hiswill.babybrezza_1505;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;

public class Test
{
	public boolean isRunningForground(Context context)
	{
		String packageName = getPackageName(context);
		String topActivityClassName = getTopActivityName(context);
		System.out.println("packageName=" + packageName+ ",topActivityClassName=" + topActivityClassName);
		if (packageName != null && topActivityClassName != null
				&& topActivityClassName.startsWith(packageName))
		{
			System.out.println("---> isRunningForeGround");
			return true;
		}
		else
		{
			System.out.println("---> isRunningBackGround");
			return false;
		}
	}

	public String getTopActivityName(Context context)
	{
		String topActivityClassName = null;
		ActivityManager activityManager = (ActivityManager) (context
				.getSystemService(android.content.Context.ACTIVITY_SERVICE));
		List<RunningTaskInfo> runningTaskInfos = activityManager
				.getRunningTasks(1);
		if (runningTaskInfos != null)
		{
			ComponentName f = runningTaskInfos.get(0).topActivity;
			topActivityClassName = f.getClassName();
		}
		return topActivityClassName;
	}

	public String getPackageName(Context context)
	{
		String packageName = context.getPackageName();
		return packageName;
	}
}
