package org.thoughtland.xlocation;

import java.util.ArrayList;
import java.util.List;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Process;
import android.util.Log;

public class XApplication extends XHook {
	private Methods mMethod;

	private static boolean mReceiverInstalled = false;

	public static String cAction = "Action";
	public static String cActionKillProcess = "Kill";
	public static String cActionFlush = "Flush";

	public static String ACTION_MANAGE_PACKAGE = "org.thoughtland.xlocation.ACTION_MANAGE_PACKAGE";
	public static String PERMISSION_MANAGE_PACKAGES = "org.thoughtland.xlocation.MANAGE_PACKAGES";

	public XApplication(Methods method, String restrictionName, String actionName) {
		super(restrictionName, method.name(), actionName);
		mMethod = method;
	}

	@Override
	public String getClassName() {
		return "android.app.Application";
	}

	// public void onCreate()
	// frameworks/base/core/java/android/app/Application.java
	// http://developer.android.com/reference/android/app/Application.html

	private enum Methods {
		onCreate
	};

	public static List<XHook> getInstances() {
		List<XHook> listHook = new ArrayList<XHook>();
		listHook.add(new XApplication(Methods.onCreate, null, null));
		return listHook;
	}

	@Override
	protected void before(XParam param) throws Throwable {
		// do nothing
	}

	@Override
	protected void after(XParam param) throws Throwable {
		switch (mMethod) {
		case onCreate:
			// Install receiver for package management
			if (PrivacyManager.isApplication(Process.myUid()) && !mReceiverInstalled)
				try {
					Application app = (Application) param.thisObject;
					if (app != null) {
						mReceiverInstalled = true;
						Util.log(this, Log.INFO, "Installing receiver uid=" + Process.myUid());
						app.registerReceiver(new Receiver(app), new IntentFilter(ACTION_MANAGE_PACKAGE),
								PERMISSION_MANAGE_PACKAGES, null);
					}
				} catch (SecurityException ignored) {
				} catch (Throwable ex) {
					Util.bug(this, ex);
				}
			break;
		}
	}

	public static void manage(Context context, int uid, String action) {
		if (uid == 0)
			manage(context, null, action);
		else {
			String[] packageName = context.getPackageManager().getPackagesForUid(uid);
			if (packageName != null && packageName.length > 0)
				manage(context, packageName[0], action);
			else
				Util.log(null, Log.WARN, "No packages uid=" + uid + " action=" + action);
		}
	}

	public static void manage(Context context, String packageName, String action) {
		Util.log(null, Log.INFO, "Manage package=" + packageName + " action=" + action);

		if (packageName == null && XApplication.cActionKillProcess.equals(action)) {
			Util.log(null, Log.WARN, "Kill all");
			return;
		}

		Intent manageIntent = new Intent(XApplication.ACTION_MANAGE_PACKAGE);
		manageIntent.putExtra(XApplication.cAction, action);
		if (packageName != null)
			manageIntent.setPackage(packageName);
		context.sendBroadcast(manageIntent);
	}

	private class Receiver extends BroadcastReceiver {
		public Receiver(Application app) {
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				String action = intent.getExtras().getString(cAction);
				Util.log(null, Log.WARN, "Managing uid=" + Process.myUid() + " action=" + action);

				if (cActionKillProcess.equals(action))
					android.os.Process.killProcess(Process.myPid());
				else if (cActionFlush.equals(action)) {
					PrivacyManager.flush();
				} else
					Util.log(null, Log.WARN, "Unknown management action=" + action);
			} catch (Throwable ex) {
				Util.bug(null, ex);
			}
		}
	}
}
