package org.thoughtland.xlocation;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

public class OnTouchProxy {
	final Map<Object,Integer> listeners = Collections.synchronizedMap(new HashMap<Object,Integer>());
	Context context = null;

	public OnTouchProxy() {
		context = Util.getActivityContext();
		if (context == null) {
			context = Util.getActivity();			
		}
	}
	
	final ILocationCallback locationCallback = new ILocationCallback.Stub() {
		public void onLocationChanged(Location location) {
			Util.log(null, Log.WARN, "Broadcasting location " + location);
			Set<Object> badKeys = new HashSet<Object>();
			for (Object o : listeners.keySet()) {
				if (o != null) {
					if (o instanceof PendingIntent) {
						Intent intent = new Intent();
						intent.putExtra("location", location);
						intent.putExtra("com.google.android.location.LOCATION", location);
						try {
							((PendingIntent)o).send(context,0, intent);
						} catch (CanceledException e) {
							e.printStackTrace();
							badKeys.add(o);
						}
					} else {
						Class<?> clazz = o.getClass();
						try {
							Util.getMethod(clazz, "onLocationChanged", null).invoke(o, location);
						} catch (Exception e) {
							e.printStackTrace();
							badKeys.add(o);
						}
					}
				}
			}
			for (Object o : badKeys)
				unregister(o);
		}
		
		@Override
		public void onProviderDisabled(String provider) throws RemoteException {
			Util.log(null, Log.WARN, "Broadcasting onProviderDisabled " + provider);
			for (Object o : listeners.keySet()) {
				try {
					Class<?> clazz = o.getClass();
					Util.getMethod(clazz, "onProviderDisabled", null).invoke(o, provider);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public void onProviderEnabled(String provider) throws RemoteException {
			Util.log(null, Log.WARN, "Broadcasting onProviderEnabled " + provider);
			for (Object o : listeners.keySet()) {
				try {
					Class<?> clazz = o.getClass();
					Util.getMethod(clazz, "onProviderEnabled", null).invoke(o, provider);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) throws RemoteException {
			Util.log(null, Log.WARN, "Broadcasting onStatusChanged " + provider);
			for (Object o : listeners.keySet()) {
				try {
					Class<?> clazz = o.getClass();
					Util.getMethod(clazz, "onStatusChanged", null).invoke(o, provider, status, extras);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	};


	void register(Object listener) {
		Util.log(null,Log.WARN,"Registering listener " + listener);

		synchronized(listeners) {
			int instances = listeners.containsKey(listener) ? listeners.get(listener) : 0;
			listeners.put(listener, instances+1);
			//if (listeners.size()==1 && instances==0) {
				try {
					Util.log(null,Log.WARN,"Calling setStatus " + OnTouch.CALLBACK_REGISTER + " for uid " + Binder.getCallingUid() + "!");
					LocationProxyService.getClient().registerOnTouchLocation(locationCallback);
					LocationProxyService.getClient().sendOnTouchStatus(OnTouch.CALLBACK_REGISTER);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			//} else
				//Util.log(null, Log.WARN, "Listener already registered, not setting up views");
		}
	}

	void unregister(Object listener) {
		Util.log(null,Log.WARN,"UnRegistering listener " + listener);
		synchronized(listeners) {
			if (listeners.containsKey(listener)) {
				int instances = listeners.get(listener);
				if (instances==1)
					listeners.remove(listener);
				else
					listeners.put(listener, instances-1);

				if (listeners.isEmpty()) {
					try {
						Util.log(null,Log.WARN,"Calling setStatus " + OnTouch.CALLBACK_UNREGISTER + " for uid " + Binder.getCallingUid() + "!");
						LocationProxyService.getClient().sendOnTouchStatus(OnTouch.CALLBACK_UNREGISTER);
						LocationProxyService.getClient().removeOnTouchLocation();
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

}