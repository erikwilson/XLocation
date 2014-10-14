package org.thoughtland.xlocation;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

@SuppressLint("UseSparseArrays")
public 
class LocationProxyService extends ILocationProxyService.Stub {
	private static ILocationProxyService mClient = null;
	private static LocationProxyService mLocationProxyService = null;
	private static final int cCurrentVersion = 0;
	private static final String cServiceName = "xlocationProxy";
	private LocationProxyService() {}

	public static void register() {
		try {
			mLocationProxyService = new LocationProxyService();
			// @formatter:off
			// public static void addService(String name, IBinder service)
			// public static void addService(String name, IBinder service, boolean allowIsolated)
			// @formatter:on
			Class<?> cServiceManager = Class.forName("android.os.ServiceManager");
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				Method mAddService = cServiceManager.getDeclaredMethod("addService", String.class, IBinder.class,
						boolean.class);
				mAddService.invoke(null, cServiceName, mLocationProxyService, true);
			} else {
				Method mAddService = cServiceManager.getDeclaredMethod("addService", String.class, IBinder.class);
				mAddService.invoke(null, cServiceName, mLocationProxyService);
			}
			// This will and should open the database
			Util.log(null, Log.WARN, "Service registered name=" + cServiceName + " version=" + cCurrentVersion);		
		} catch (Throwable ex) {
			Util.bug(null, ex);
		}
	}
	
	public static ILocationProxyService getClient() {
		// Runs client side
		if (mClient == null)
			try {
				// public static IBinder getService(String name)
				Class<?> cServiceManager = Class.forName("android.os.ServiceManager");
				Method mGetService = cServiceManager.getDeclaredMethod("getService", String.class);
				mClient = ILocationProxyService.Stub.asInterface((IBinder) mGetService.invoke(null, cServiceName));
			} catch (Throwable ex) {
				Util.bug(null, ex);
			}

		return mClient;
	}

	private Set<IStatusCallback> statusUpdates = Collections.synchronizedSet(new HashSet<IStatusCallback>());

	@Override
	public void removeUpdates(IStatusCallback cb) throws RemoteException {
		statusUpdates.remove(cb);
	}

	@Override
	public void registerUpdates(IStatusCallback cb) throws RemoteException {
		statusUpdates.add(cb);
	}

	@Override
	public void sendUpdateStatus(int status) throws RemoteException {
		Set<IStatusCallback> errors = new HashSet<IStatusCallback>();
		synchronized(statusUpdates) {
			for (IStatusCallback cb : statusUpdates ) {
				try {
					cb.setStatus(status);
				} catch (Exception e) {
					errors.add(cb);
				}
			}
		}
		for (IStatusCallback e : errors) {
			Util.log(null, Log.WARN, "removing errored callback "+e);
			removeUpdates(e);
		}
	}

	private final Map<PendingIntent,PendingIntent> pendingProxy = 
			Collections.synchronizedMap(new HashMap<PendingIntent,PendingIntent>());
	private final Map<PendingIntent,BroadcastReceiver> receiverProxy = 
			Collections.synchronizedMap(new HashMap<PendingIntent,BroadcastReceiver>());
	private final Map<PendingIntent,Context> contextProxy = 
			Collections.synchronizedMap(new HashMap<PendingIntent,Context>());

	@Override
	public void unregisterPendingIntent(PendingIntent originalIntent) throws RemoteException {
		synchronized (receiverProxy) {
			if (receiverProxy.containsKey(originalIntent)) {
				try {
					PendingIntent proxy = pendingProxy.get(originalIntent);
					BroadcastReceiver receiver = receiverProxy.get(originalIntent);
					Context context = contextProxy.get(originalIntent);
					Util.log(null, Log.WARN, "Removing pendingIntent " + originalIntent);
					receiverProxy.remove(originalIntent);
					pendingProxy.remove(originalIntent);
					contextProxy.remove(originalIntent);
					Long token = Binder.clearCallingIdentity();
					try { proxy.cancel(); } 
					catch (Exception e) { Util.log(null,Log.WARN,"Could not cancel proxy!"); }
					finally { Binder.restoreCallingIdentity(token); }
					context.unregisterReceiver(receiver);
				} catch (Exception e) {
					Util.log(null, Log.WARN, "Could not unregister proxy intent");
				}
			}
		}
	}

	@Override
	public PendingIntent proxyPendingIntent(final PendingIntent originalIntent, 
			final boolean single, final boolean restricted) throws RemoteException {
		final Context activity = Util.getActivityContext();

		if (activity==null) {
			Util.log(null, Log.ERROR, "Unable to find Activity Context");
			return null;
		}
		unregisterPendingIntent(originalIntent);

		final int id = (int) (Math.random() * Integer.MAX_VALUE);
		final String name = "proxyPendingIntent:" + originalIntent.hashCode();// + "." + id;

		PendingIntent result = null;
		Intent intent = new Intent(name);
		intent.setFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
		final int uid = Binder.getCallingUid();
		final long token = Binder.clearCallingIdentity();
		try {
			result = PendingIntent.getBroadcast(Util.getSystemContext(), id, intent, (single ? PendingIntent.FLAG_ONE_SHOT : 0) );
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Binder.restoreCallingIdentity(token);
		}

		if (result != null) {
			final BroadcastReceiver receiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					boolean done = single;
					if (intent != null) {
						Binder.restoreCallingIdentity(token);
						Util.log(null, Log.WARN, "onReceive for " + name + 
								" with extras [ " + TextUtils.join(", ", intent.getExtras().keySet()) + " ]");
						if (restricted)
							for (String extra : intent.getExtras().keySet()) {
								Object obj = intent.getParcelableExtra(extra);
								if (obj instanceof Location) {
									Util.log(null, Log.INFO, "  defacing " + extra);
									intent.putExtra(extra, PrivacyManager.getDefacedLocation(uid,(Location)obj));
								}
							}
						try { 
							originalIntent.send(activity, this.getResultCode(), intent); 
						} catch (Exception e) { 
							Util.log(null,Log.WARN, "Could not send intent!");
							done=true; 
						}
					} else
						Util.log(null,Log.ERROR,"Received null intent in BroadcastReceiver");
					if (done) {
						try { unregisterPendingIntent(originalIntent); } 
						catch (RemoteException e) { e.printStackTrace(); }
					}
				}
			};
			try {
				IntentFilter filter = new IntentFilter();
				filter.addAction(name);
				activity.registerReceiver(receiver, filter);
				pendingProxy.put(originalIntent, result);
				receiverProxy.put(originalIntent, receiver);
				contextProxy.put(originalIntent, activity);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	private static final Map<Integer,IStatusCallback> onTouchStatusCallbacks = 
			Collections.synchronizedMap(new HashMap<Integer,IStatusCallback>());

	@Override
	public void removeOnTouch() throws RemoteException {
		Util.log(null,Log.WARN,"Removing onTouch status for uid " + Binder.getCallingUid() + "!");
		onTouchStatusCallbacks.remove(Binder.getCallingUid());
	}

	@Override
	public void registerOnTouch(IStatusCallback cb) throws RemoteException {
		Util.log(null,Log.WARN,"Registering onTouch status for uid " + Binder.getCallingUid() + "!");
		onTouchStatusCallbacks.put(Binder.getCallingUid(), cb);
	}

	@Override
	public void sendOnTouchStatus(int status) throws RemoteException {
		Util.log(null,Log.WARN,"Sending onTouch status for uid " + Binder.getCallingUid() + "!");
		try {
			onTouchStatusCallbacks.get(Binder.getCallingUid()).setStatus(status);
		} catch (Exception e) {
			e.printStackTrace();
			//removeOnTouch();
		}
	}

	private static final Map<Integer,ILocationCallback> onTouchLocationCallbacks = 
			Collections.synchronizedMap(new HashMap<Integer,ILocationCallback>());

	@Override
	public void removeOnTouchLocation() throws RemoteException {
		Util.log(null,Log.WARN,"Removing onTouch location for uid " + Binder.getCallingUid() + "!");
		onTouchLocationCallbacks.remove(Binder.getCallingUid());
	}

	@Override
	public void registerOnTouchLocation(ILocationCallback cb) throws RemoteException {
		Util.log(null,Log.WARN,"Registering onTouch location for uid " + Binder.getCallingUid() + "!");
		onTouchLocationCallbacks.put(Binder.getCallingUid(), cb);
	}

	@Override
	public void sendOnTouchLocation(Location location) throws RemoteException {
		Util.log(null,Log.WARN,"Send onTouch location for uid " + Binder.getCallingUid() + "!");
		try {
			onTouchLocationCallbacks.get(Binder.getCallingUid()).onLocationChanged(location);
		} catch (Exception e) {
			e.printStackTrace();
			//removeOnTouchLocation();
			//sendOnTouchStatus(OnTouch.CALLBACK_UNREGISTER);
		}
	}

	@Override
	public void onProviderDisabled(String provider) throws RemoteException {
		try {
			onTouchLocationCallbacks.get(Binder.getCallingUid()).onProviderDisabled(provider);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onProviderEnabled(String provider) throws RemoteException {
		try {
			onTouchLocationCallbacks.get(Binder.getCallingUid()).onProviderEnabled(provider);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) throws RemoteException {
		try {
			onTouchLocationCallbacks.get(Binder.getCallingUid()).onStatusChanged(provider, status, extras);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static final Map<Integer,IGpsStatusListener> onGpsStatusCallbacks = 
			Collections.synchronizedMap(new HashMap<Integer,IGpsStatusListener>());

	@Override
	public void removeGpsStatusListener() throws RemoteException {
		Util.log(null,Log.WARN,"Removing GpsStatusListener for uid " + Binder.getCallingUid() + "!");
		onGpsStatusCallbacks.remove(Binder.getCallingUid());
	}

	@Override
	public void registerGpsStatusListener(IGpsStatusListener gpscb) throws RemoteException {
		Util.log(null,Log.WARN,"Registering GpsStatusListener for uid " + Binder.getCallingUid() + "!");
		onGpsStatusCallbacks.put(Binder.getCallingUid(), gpscb);
	}

	@Override
	public void onGpsStatusChanged(int event) throws RemoteException {
		try {
			onGpsStatusCallbacks.get(Binder.getCallingUid()).onGpsStatusChanged(event);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
