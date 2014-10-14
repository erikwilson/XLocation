package org.thoughtland.xlocation;

import java.util.ArrayList;
import java.util.List;

import android.location.Location;
import android.os.Binder;
import android.util.Log;

public class XLocationClient extends XHook {
	private Methods mMethod;
	//private static final Map<Object, Object> mMapProxy = new WeakHashMap<Object, Object>();
	private static final LocationProxy mProxy = new LocationProxy();

	private XLocationClient(Methods method, String restrictionName) {
		super(restrictionName, method.name(), String.format("GMS.%s", method.name()));
		mMethod = method;
	}

	public String getClassName() {
		return "com.google.android.gms.location.LocationClient";
	}

	// @formatter:off

	// void addGeofences(List<Geofence> geofences, PendingIntent pendingIntent, LocationClient.OnAddGeofencesResultListener listener)
	// Location getLastLocation()
	// void removeGeofences(List<String> geofenceRequestIds, LocationClient.OnRemoveGeofencesResultListener listener)
	// void removeGeofences(PendingIntent pendingIntent, LocationClient.OnRemoveGeofencesResultListener listener)
	// void removeLocationUpdates(LocationListener listener)
	// void removeLocationUpdates(PendingIntent callbackIntent)
	// void requestLocationUpdates(LocationRequest request, PendingIntent callbackIntent)
	// void requestLocationUpdates(LocationRequest request, LocationListener listener)
	// void requestLocationUpdates(LocationRequest request, LocationListener listener, Looper looper)
	// https://developer.android.com/reference/com/google/android/gms/location/LocationClient.html

	// @formatter:on

	private enum Methods {
		addGeofences, getLastLocation, removeGeofences, removeLocationUpdates, requestLocationUpdates
	};

	public static List<XHook> getInstances() {
		Util.log(null, Log.INFO, "Hooking uid=" + Binder.getCallingUid());

		List<XHook> listHook = new ArrayList<XHook>();
		listHook.add(new XLocationClient(Methods.addGeofences, PrivacyManager.cLocation));
		listHook.add(new XLocationClient(Methods.getLastLocation, PrivacyManager.cLocation));
		listHook.add(new XLocationClient(Methods.removeGeofences, null));
		listHook.add(new XLocationClient(Methods.removeLocationUpdates, null));
		listHook.add(new XLocationClient(Methods.requestLocationUpdates, PrivacyManager.cLocation));
		return listHook;
	}

	@Override
	protected void before(XParam param) throws Throwable {
		Util.log(this, Log.WARN, "hooked before " + getClassName() + "." + mMethod.toString() + " for uid " + Binder.getCallingUid());
		switch (mMethod) {
		case addGeofences:
			if (isRestricted(param))
				param.setResult(null);
			break;

		case removeGeofences:
			if (isRestricted(param, PrivacyManager.cLocation, "GMS.addGeofences"))
				param.setResult(null);
			break;

		case removeLocationUpdates:
			mProxy.unproxy(param,0);
			break;

		case requestLocationUpdates:
			mProxy.proxy(this,param,1,"com.google.android.gms.location.LocationListener");
			break;
			
		default:
		}
	}

	@Override
	protected void after(XParam param) throws Throwable {
		switch (mMethod) {

		case getLastLocation:
			Location location = (Location) param.getResult();
			if (location != null)
				if (isRestricted(param))
					param.setResult(PrivacyManager.getDefacedLocation(Binder.getCallingUid(), location));
			break;

		default:
		}
	}
}
