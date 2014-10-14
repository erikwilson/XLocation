package org.thoughtland.xlocation;

import java.util.ArrayList;
import java.util.List;

import android.location.Location;

import android.os.Binder;
import android.util.Log;

public class XFusedLocationApi extends XHook {
	private Methods mMethod;
	private String mClassName;
	private static LocationProxy mProxy = new LocationProxy(new int[]{0,-1});

	private XFusedLocationApi(Methods method, String restrictionName, String className) {
		super(restrictionName, method.name(), "GMS5." + method.name());
		mMethod = method;
		mClassName = className;
	}

	public String getClassName() {
		return mClassName;
	}

	// @formatter:off

	// Location getLastLocation(GoogleApiClient client)
	// abstract PendingResult<Status> removeLocationUpdates(GoogleApiClient client, LocationListener listener)
	// abstract PendingResult<Status> removeLocationUpdates(GoogleApiClient client, PendingIntent callbackIntent)
	// abstract PendingResult<Status> requestLocationUpdates(GoogleApiClient client, LocationRequest request, LocationListener listener, Looper looper)
	// abstract PendingResult<Status> requestLocationUpdates(GoogleApiClient client, LocationRequest request, LocationListener listener)
	// abstract PendingResult<Status> requestLocationUpdates(GoogleApiClient client, LocationRequest request, PendingIntent callbackIntent)
	// https://developer.android.com/reference/com/google/android/gms/location/FusedLocationProviderApi.html
	
	// @formatter:on

	private enum Methods {
		getLastLocation, removeLocationUpdates, requestLocationUpdates
	};

	public static List<XHook> getInstances(Object instance) {
		String className = instance.getClass().getName();
		Util.log(null, Log.INFO, "Hooking class=" + className + " uid=" + Binder.getCallingUid());

		List<XHook> listHook = new ArrayList<XHook>();
		listHook.add(new XFusedLocationApi(Methods.getLastLocation, PrivacyManager.cLocation, className));
		listHook.add(new XFusedLocationApi(Methods.removeLocationUpdates, null, className));
		listHook.add(new XFusedLocationApi(Methods.requestLocationUpdates, PrivacyManager.cLocation, className));
		return listHook;
	}

	@Override
	protected void before(XParam param) throws Throwable {
		Util.log(this, Log.WARN, "hooked before " + mClassName + "." + mMethod.toString() + " for uid " + Binder.getCallingUid());

		switch (mMethod) {
		
		case removeLocationUpdates:
			mProxy.unproxy(param,1);
			//OnTouch.unregister(param.args[1]);
			//param.setResult(null);
			break;

		case requestLocationUpdates:
			mProxy.proxy(this,param,2,"com.google.android.gms.location.LocationListener",true);
			//OnTouch.register(param.args[2]);
			//param.setResult(null);
			break;
			
		default:
		}
	}

	@Override
	protected void after(XParam param) throws Throwable {
		switch (mMethod) {
		
		case getLastLocation:
			Location location = (Location) param.getResult();
			if (location != null && isRestricted(param))
				param.setResult(PrivacyManager.getDefacedLocation(Binder.getCallingUid(), location));
			break;
			
		default:
		}
	}

}
