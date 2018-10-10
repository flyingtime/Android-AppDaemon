package com.coolerfall.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import android.util.Log;

import com.coolerfall.daemon.Daemon;

import java.util.List;

public class DaemonService extends Service {
	@Override
	public void onCreate() {
		super.onCreate();
		Log.i("DemoLog","TestService -> onCreate, Thread ID: " + Thread.currentThread().getId());
		Daemon.run(this, DaemonService.class, Daemon.INTERVAL_ONE_MINUTE * 2);
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.i("DemoLog","TestService -> onBind, Thread ID: " + Thread.currentThread().getId());
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		/* do something here */
		Log.i("DemoLog","TestService -> onStartCommand, Thread ID: " + Thread.currentThread().getId());

		if (getAppSatus(this,"com.coolerfall.daemon.sample") == 3) {
			Log.i("DemoLog","TestService -> com.coolerfall.daemon.sample will start");
			doStartApplicationWithPackageName("com.coolerfall.daemon.sample");
		} else {
			Log.i("DemoLog","TestService -> com.coolerfall.daemon.sample exsit");
		}

		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * 返回app运行状态
	 * 1:程序在前台运行
	 * 2:程序在后台运行
	 * 3:程序未启动
	 * 注意：需要配置权限<uses-permission android:name="android.permission.GET_TASKS" />
	 */
	public int getAppSatus(Context context, String pageName) {

		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(20);

		//判断程序是否在栈顶
		if (list.get(0).topActivity.getPackageName().equals(pageName)) {
			return 1;
		} else {
			//判断程序是否在栈里
			for (ActivityManager.RunningTaskInfo info : list) {
				if (info.topActivity.getPackageName().equals(pageName)) {
					return 2;
				}
			}
			return 3;//栈里找不到，返回3
		}
	}

	private void doStartApplicationWithPackageName(String packagename) {

		// 通过包名获取此APP详细信息，包括Activities、services、versioncode、name等等
		PackageInfo packageinfo = null;
		try {
			packageinfo = getPackageManager().getPackageInfo(packagename, 0);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		if (packageinfo == null) {
			return;
		}

		// 创建一个类别为CATEGORY_LAUNCHER的该包名的Intent
		Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
		resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		resolveIntent.setPackage(packageinfo.packageName);

		// 通过getPackageManager()的queryIntentActivities方法遍历
		List<ResolveInfo> resolveinfoList = getPackageManager()
				.queryIntentActivities(resolveIntent, 0);

		ResolveInfo resolveinfo = resolveinfoList.iterator().next();
		if (resolveinfo != null) {
			// packagename = 参数packname
			String packageName = resolveinfo.activityInfo.packageName;
			// 这个就是我们要找的该APP的LAUNCHER的Activity[组织形式：packagename.mainActivityname]
			String className = resolveinfo.activityInfo.name;
			// LAUNCHER Intent
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);

			// 设置ComponentName参数1:packagename参数2:MainActivity路径
			ComponentName cn = new ComponentName(packageName, className);

			intent.setComponent(cn);
			startActivity(intent);
		}
	}


	@Override
	public void onDestroy() {
		Log.i("DemoLog", "TestService -> onDestroy, Thread ID: " + Thread.currentThread().getId());
		super.onDestroy();
	}

}
