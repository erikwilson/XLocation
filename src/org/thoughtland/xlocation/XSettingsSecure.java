package org.thoughtland.xlocation;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.util.Log;
import android.os.RemoteException;
import android.provider.Settings.Secure;

public class XSettingsSecure extends XHook {
	private Methods mMethod;

	private XSettingsSecure(Methods method, String restrictionName) {
		super(restrictionName, method.name(), null);
		mMethod = method;
	}

	public String getClassName() {
		return "android.provider.Settings.Secure";
	}

	// @formatter:off

	// public synchronized static String getString(ContentResolver resolver, String name)
	// frameworks/base/core/java/android/provider/Settings.java
	// frameworks/base/core/java/android/content/ContentResolver.java
	// http://developer.android.com/reference/android/provider/Settings.Secure.html

	// @formatter:on

	private enum Methods {
		putFloat, putInt, putLong, putString, setLocationProviderEnabled
	};

	public static List<XHook> getInstances() {
		List<XHook> listHook = new ArrayList<XHook>();
		listHook.add(new XSettingsSecure(Methods.putFloat, null));
		listHook.add(new XSettingsSecure(Methods.putInt, null));
		listHook.add(new XSettingsSecure(Methods.putLong, null));
		listHook.add(new XSettingsSecure(Methods.putString, null));
		listHook.add(new XSettingsSecure(Methods.setLocationProviderEnabled, null));
		return listHook;
	}
/*
	private void sendBroadcast(String action) {
		try {
			Intent intent = new Intent();
			intent.setAction(action);
			intent.setFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
			Util.getSystemContext().sendBroadcast(intent);
			Util.log(null, Log.WARN, "sent " + action + " broadcast");
		} catch (Exception e) { e.printStackTrace(); }
	}
*/
	private void sendStatus(int status) {
		try {
			LocationProxyService.getClient().sendUpdateStatus(status);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	private boolean resetUpdates(XParam param) {
		if (PrivacyManager.getSettingBool(0, PrivacyManager.cSettingResetListeners, true))
			switch(mMethod) {
			case putFloat:
			case putInt:
			case putLong:
			case putString:
				if (param.args.length > 1 && param.args[1].equals(Secure.LOCATION_MODE))
					return true;
				break;
			case setLocationProviderEnabled:
				return true;
			}
		return false;
	}
	
	@Override
	protected void before(XParam param) throws Throwable {
		Util.log(null, Log.WARN, mMethod.toString() + " setting " + param.args[1] + " to " + param.args[2]);
		if (resetUpdates(param)) {
			sendStatus(LocationProxy.CALLBACK_REMOVE_UPDATES);
	    	//sendBroadcast(LocationProxy.REMOVE_UPDATES);
	    	Thread.sleep(125);
		}
	}

	@Override
	protected void after(XParam param) throws Throwable {
		if (resetUpdates(param))
			sendStatus(LocationProxy.CALLBACK_REQUEST_UPDATES);
	    	//sendBroadcast(LocationProxy.REQUEST_UPDATES);
	}

}
