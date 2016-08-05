package org.thoughtland.xlocation;

// Declare the interface.
interface ILocationCallback {
	void onLocationChanged(in Location location);
	void onProviderDisabled(in String provider);
	void onProviderEnabled(in String provider);
	void onStatusChanged(in String provider, int status, in Bundle extras);
}