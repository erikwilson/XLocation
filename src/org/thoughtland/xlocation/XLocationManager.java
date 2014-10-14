package org.thoughtland.xlocation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.location.Location;
import android.os.Binder;
import android.os.RemoteException;
import android.util.Log;
import android.location.GpsSatellite;
import android.location.GpsStatus;

public class XLocationManager extends XHook {
	private Methods mMethod;
	private String mClassName;
	private static final String cClassName = "android.location.LocationManager";
	private static final LocationProxy mProxy = new LocationProxy("removeUpdates");
	private static final LocationProxy mProxySrv = new LocationProxy("removeUpdates", new int[]{1,2,3});

	private XLocationManager(Methods method, String restrictionName, String className) {
		super(restrictionName, method.name().replace("Srv_", ""), method.name());
		mMethod = method;
		mClassName = className;
	}

	public String getClassName() {
		return mClassName;
	}

	// @formatter:off

	// public void addGeofence(LocationRequest request, Geofence fence, PendingIntent intent)
	// public boolean addGpsStatusListener(GpsStatus.Listener listener)
	// public boolean addNmeaListener(GpsStatus.NmeaListener listener)
	// public void addProximityAlert(double latitude, double longitude, float radius, long expiration, PendingIntent intent)
	// public List<String> getAllProviders()
	// public String getBestProvider(Criteria criteria, boolean enabledOnly)
	// public GpsStatus getGpsStatus(GpsStatus status)
	// public Location getLastKnownLocation(String provider)
	// public List<String> getProviders(boolean enabledOnly)
	// public List<String> getProviders(Criteria criteria, boolean enabledOnly)
	// public boolean isProviderEnabled(String provider)
	// public void removeUpdates(LocationListener listener)
	// public void removeUpdates(PendingIntent intent)
	// public void requestLocationUpdates(String provider, long minTime, float minDistance, LocationListener listener)
	// public void requestLocationUpdates(String provider, long minTime, float minDistance, LocationListener listener, Looper looper)
	// public void requestLocationUpdates(long minTime, float minDistance, Criteria criteria, LocationListener listener, Looper looper)
	// public void requestLocationUpdates(String provider, long minTime, float minDistance, PendingIntent intent)
	// public void requestLocationUpdates(long minTime, float minDistance, Criteria criteria, PendingIntent intent)
	// public void requestSingleUpdate(String provider, LocationListener listener, Looper looper)
	// public void requestSingleUpdate(Criteria criteria, LocationListener listener, Looper looper)
	// public void requestSingleUpdate(String provider, PendingIntent intent)
	// public void requestSingleUpdate(Criteria criteria, PendingIntent intent)
	// public boolean sendExtraCommand(String provider, String command, Bundle extras)
	// frameworks/base/location/java/android/location/LocationManager.java
	// http://developer.android.com/reference/android/location/LocationManager.html

	// public void requestLocationUpdates(LocationRequest request, ILocationListener listener, android.app.PendingIntent intent, java.lang.String packageName)
	// public void removeUpdates(ILocationListener listener, android.app.PendingIntent intent, java.lang.String packageName)
	// public void requestGeofence(LocationRequest request, Geofence geofence, android.app.PendingIntent intent, java.lang.String packageName)
	// public void removeGeofence(Geofence fence, android.app.PendingIntent intent, java.lang.String packageName)
	// public Location getLastLocation(LocationRequest request, java.lang.String packageName)
	// public boolean addGpsStatusListener(IGpsStatusListener listener, java.lang.String packageName)
	// public void removeGpsStatusListener(IGpsStatusListener listener)
	// public java.util.List<java.lang.String> getAllProviders()
	// public java.util.List<java.lang.String> getProviders(Criteria criteria, boolean enabledOnly)
	// public java.lang.String getBestProvider(Criteria criteria, boolean enabledOnly)
	// public boolean isProviderEnabled(java.lang.String provider)
	// public boolean sendExtraCommand(java.lang.String provider, java.lang.String command, android.os.Bundle extras)
	// http://grepcode.com/file/repository.grepcode.com/java/ext/com.google.android/android/4.4.4_r1/com/android/server/LocationManagerService.java

	// @formatter:on

	// @formatter:off
	private enum Methods {
		addGeofence, addGpsStatusListener, addNmeaListener, addProximityAlert,
		getAllProviders, getBestProvider, getProviders, isProviderEnabled,
		getGpsStatus,
		getLastKnownLocation,
		removeUpdates,
		requestLocationUpdates, requestSingleUpdate,
		sendExtraCommand,

		/*
		Srv_requestGeofence, Srv_removeGeofence,
		Srv_getLastLocation, Srv_getBestProvider,
		
		Srv_getAllProviders, Srv_getProviders, Srv_updateProviders, 
		Srv_requestLocationUpdates, Srv_requestLocationUpdatesPI, Srv_removeUpdates, Srv_removeUpdatesPI, 
		Srv_addGpsStatusListener, Srv_removeGpsStatusListener, Srv_sendExtraCommand, 
		Srv_addProximityAlert, Srv_removeProximityAlert, 
		Srv_getProviderInfo, Srv_isProviderEnabled, 
		Srv_getLastKnownLocation, Srv_getFromLocation, Srv_getFromLocationName, 
		Srv_addTestProvider, Srv_removeTestProvider, 
		Srv_setTestProviderLocation, Srv_clearTestProviderLocation, 
		Srv_setTestProviderEnabled, Srv_clearTestProviderEnabled, 
		Srv_setTestProviderStatus, Srv_clearTestProviderStatus
		
		*/
		Srv_requestLocationUpdates, Srv_removeUpdates,
		Srv_requestGeofence, Srv_removeGeofence,
		Srv_getLastLocation,
		Srv_addGpsStatusListener, Srv_removeGpsStatusListener,
		Srv_getAllProviders, Srv_getProviders, Srv_getBestProvider, Srv_isProviderEnabled,
		Srv_sendExtraCommand
	};
	// @formatter:on

	public static List<XHook> getInstances(String className) {
		List<XHook> listHook = new ArrayList<XHook>();
		if (!cClassName.equals(className)) {
			if (className == null)
				className = cClassName;

			for (Methods loc : Methods.values())
				if (loc == Methods.removeUpdates)
					listHook.add(new XLocationManager(loc, null, className));
				else if (loc.name().startsWith("Srv_remove"))
					listHook.add(new XLocationManager(loc, null, "com.android.server.LocationManagerService"));
				else if (loc.name().startsWith("Srv_"))
					listHook.add(new XLocationManager(loc, PrivacyManager.cLocation,
							"com.android.server.LocationManagerService"));
				else
					listHook.add(new XLocationManager(loc, PrivacyManager.cLocation, className));
		}
		return listHook;
	}

	@Override
	protected void before(XParam param) throws Throwable {
		Util.log(this, Log.WARN, "hooked before " + mClassName + "." + mMethod.toString() + " for uid " + Binder.getCallingUid());
		switch (mMethod) {
		case addGeofence:
		case addProximityAlert:
		case Srv_requestGeofence:
			if (isRestricted(param))
				param.setResult(null);
			break;

		case Srv_removeGeofence:
			if (isRestricted(param, PrivacyManager.cLocation, "Srv_requestGeofence"))
				param.setResult(null);
			break;

		case addNmeaListener:
			if (isRestricted(param))
				param.setResult(false);
			break;

		case addGpsStatusListener:
		case Srv_addGpsStatusListener:
			if (isRestricted(param)) {
				param.setResult(false);
				final Object listener = param.args[0];
				try {
					LocationProxyService.getClient().registerGpsStatusListener(
							new IGpsStatusListener.Stub() {							
								@Override
								public void onGpsStatusChanged(int event) throws RemoteException {
									Util.log(null, Log.WARN, "Calling GPS status onChanged");
									try {
										Class<?> clazz = listener.getClass();
										Method m = Util.getMethod(clazz, "onGpsStatusChanged", null);
										if (m!=null) {
											Util.log(null, Log.WARN, " invoking onChanged");
											m.invoke(listener,event);
										} else {
											if (event == GpsStatus.GPS_EVENT_FIRST_FIX) {
												Util.log(null, Log.WARN, " invoking onFirstFix");
												m = Util.getMethod(clazz, "onFirstFix", null);
												if (m!=null) m.invoke(listener,0);
											}
											if (event == GpsStatus.GPS_EVENT_STARTED) {
												Util.log(null, Log.WARN, " invoking onGpsStarted");
												m = Util.getMethod(clazz, "onGpsStarted", null);
												if (m!=null) m.invoke(listener);
											}
											if (event == GpsStatus.GPS_EVENT_STOPPED) {
												Util.log(null, Log.WARN, " invoking onGpsStopped");
												m = Util.getMethod(clazz, "onGpsStopped", null);
												if (m!=null) m.invoke(listener);
											}
										}
										
									} catch (Exception e) {
										e.printStackTrace();
									}

								}
							});
				} catch (Throwable e) {
					e.printStackTrace();
				}				
			}
			break;

		case Srv_removeGpsStatusListener:
			if (isRestricted(param, PrivacyManager.cLocation, "Srv_addGpsStatusListener"))
				param.setResult(null);
			break;

		case removeUpdates:
			mProxy.unproxy(param, 0);
			break;

		case requestLocationUpdates:
			String extra = param.args.length>0 && param.args[0] instanceof String ? (String)param.args[0] : null;
			Util.log(this, Log.WARN, "in requestLocationUpdates with extra " + extra + " and xparam " + param);
			mProxy.proxy(this, param, extra, 3, "android.location.LocationListener");
			break;

		case Srv_removeUpdates:
			mProxySrv.unproxy(param, 1, 0);
			break;

		case Srv_requestLocationUpdates:
			Util.log(this, Log.WARN, "in Srv_requestLocationUpdates and xparam " + param);
			mProxySrv.proxy(this, param, 2, 1, "android.location.ILocationListener");
			break;

		case requestSingleUpdate:
			String provider = param.args.length>0 && param.args[0] instanceof String ? (String)param.args[0] : null;
			mProxy.proxy(this, true, param, provider, 1, "android.location.LocationListener");
			break;

		default:
		}
	}

	@Override
	protected void after(XParam param) throws Throwable {
		switch (mMethod) {

		case isProviderEnabled:
		case Srv_isProviderEnabled:
			if (param.args.length > 0) {
				String provider = (String) param.args[0];
				if (isRestrictedExtra(param, provider))
					param.setResult(false);
			}
			break;

		case getGpsStatus:
			if (param.getResult() instanceof GpsStatus)
				if (isRestricted(param)) {
					GpsStatus status = (GpsStatus) param.getResult();
					// private GpsSatellite mSatellites[]
					try {
						Field mSatellites = status.getClass().getDeclaredField("mSatellites");
						mSatellites.setAccessible(true);
						mSatellites.set(status, new GpsSatellite[0]);
					} catch (Throwable ex) {
						Util.bug(null, ex);
					}
				}
			break;

		case getProviders:
		case getAllProviders:
		case Srv_getAllProviders:
		case Srv_getProviders:
			if (isRestricted(param))
				param.setResult(new ArrayList<String>());
			break;

		case getBestProvider:
		case Srv_getBestProvider:
			if (param.getResult() != null)
				if (isRestricted(param))
					param.setResult(null);
			break;

		case getLastKnownLocation:
			if (param.args.length > 0 && param.getResult() instanceof Location) {
				String provider = (String) param.args[0];
				Location location = (Location) param.getResult();
				if (isRestrictedExtra(param, provider))
					param.setResult(PrivacyManager.getDefacedLocation(Binder.getCallingUid(), location));
			}
			break;

		case Srv_getLastLocation:
			if (param.getResult() instanceof Location) {
				Location location = (Location) param.getResult();
				if (isRestricted(param))
					param.setResult(PrivacyManager.getDefacedLocation(Binder.getCallingUid(), location));
			}
			break;

		case sendExtraCommand:
		case Srv_sendExtraCommand:
			if (param.args.length > 0) {
				String provider = (String) param.args[0];
				if (isRestrictedExtra(param, provider))
					param.setResult(false);
			}
			break;
			
		case Srv_removeUpdates:
		case Srv_requestLocationUpdates:
			if (param.hasThrowable()) {
				Util.log(this, Log.WARN, "** Got throwable from " + mMethod + " and args " + param.args);
			}
		default:
		}
	}
}
