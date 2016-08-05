package org.thoughtland.xlocation;

import org.thoughtland.xlocation.IStatusCallback;
import org.thoughtland.xlocation.ILocationCallback;
import org.thoughtland.xlocation.IGpsStatusListener;

interface ILocationProxyService {
	void removeUpdates(in IStatusCallback cb);
	void registerUpdates(in IStatusCallback cb);
	void sendUpdateStatus(int status);
	
	PendingIntent proxyPendingIntent(in PendingIntent originalIntent, boolean single, boolean restricted);
	void unregisterPendingIntent(in PendingIntent originalIntent);
	
	void removeOnTouch();
	void registerOnTouch(in IStatusCallback cb);
	void sendOnTouchStatus(int status);

	void removeOnTouchLocation();
	void registerOnTouchLocation(in ILocationCallback cb);
	void sendOnTouchLocation(in Location location);
	void onProviderDisabled(in String provider);
	void onProviderEnabled(in String provider);
	void onStatusChanged(in String provider, int status, in Bundle extras);
	
	void removeGpsStatusListener();
	void registerGpsStatusListener(in IGpsStatusListener gpscb);
	void onGpsStatusChanged(int event);
}