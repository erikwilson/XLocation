package org.thoughtland.xlocation;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.nfc.NfcAdapter;
import android.provider.MediaStore;
import android.provider.Telephony;
import android.telephony.TelephonyManager;
import android.util.Log;

@SuppressLint("InlinedApi")
public class Meta {
	private static boolean mAnnotated = false;
	private static List<Hook> mListHook = new ArrayList<Hook>();

	public final static String cTypeAccount = "Account";
	public final static String cTypeAccountHash = "AccountHash";
	public final static String cTypeApplication = "Application";
	public final static String cTypeContact = "Contact";
	public final static String cTypeTemplate = "Template";

	public final static String cTypeAddress = "Address";
	public final static String cTypeAction = "Action";
	public final static String cTypeCommand = "Command";
	public final static String cTypeFilename = "Filename";
	public final static String cTypeIPAddress = "IPAddress";
	public final static String cTypeLibrary = "Library";
	public final static String cTypeMethod = "Method";
	public final static String cTypePermission = "Permission";
	public final static String cTypeProc = "Proc";
	public final static String cTypeTransaction = "Transaction";
	public final static String cTypeUrl = "Url";

	public static boolean isWhitelist(String type) {
		return (cTypeAddress.equals(type) || cTypeAction.equals(type) || cTypeCommand.equals(type)
				|| cTypeFilename.equals(type) || cTypeIPAddress.equals(type) || cTypeLibrary.equals(type)
				|| cTypeMethod.equals(type) || cTypePermission.equals(type) || cTypeProc.equals(type)
				|| cTypeTransaction.equals(type) || cTypeUrl.equals(type));
	}

	public static List<Hook> get() {
		// http://developer.android.com/reference/android/Manifest.permission.html
		if (mListHook.size() > 0)
			return mListHook;

		// @formatter:off
		mListHook.add(new Hook("ipc", "Binder", "", 1, "2.1.21", null).notAOSP(19).dangerous().whitelist(cTypeTransaction));

		mListHook.add(new Hook("location", "addGeofence", "ACCESS_COARSE_LOCATION,ACCESS_FINE_LOCATION", 17, null, null).notAOSP(19));
		mListHook.add(new Hook("location", "addGpsStatusListener", "ACCESS_COARSE_LOCATION,ACCESS_FINE_LOCATION", 3, "2.1.17", null).notAOSP(19));
		mListHook.add(new Hook("location", "addNmeaListener", "ACCESS_COARSE_LOCATION,ACCESS_FINE_LOCATION", 5, null, null).notAOSP(19));
		mListHook.add(new Hook("location", "addProximityAlert", "ACCESS_COARSE_LOCATION,ACCESS_FINE_LOCATION", 1, null, null).notAOSP(19));
		mListHook.add(new Hook("location", "getAllProviders", "ACCESS_COARSE_LOCATION,ACCESS_FINE_LOCATION", 1, "2.1.20", null).notAOSP(19).dangerous());
		mListHook.add(new Hook("location", "getBestProvider", "ACCESS_COARSE_LOCATION,ACCESS_FINE_LOCATION", 1, "2.1.20", null).notAOSP(19).dangerous());
		mListHook.add(new Hook("location", "getGpsStatus", "ACCESS_COARSE_LOCATION,ACCESS_FINE_LOCATION", 3, "1.99.29", null).notAOSP(19));
		mListHook.add(new Hook("location", "getLastKnownLocation", "ACCESS_COARSE_LOCATION,ACCESS_FINE_LOCATION", 1, null, null).notAOSP(19));
		mListHook.add(new Hook("location", "getProviders", "ACCESS_COARSE_LOCATION,ACCESS_FINE_LOCATION", 1, "1.99.1", null).notAOSP(19).dangerous());
		mListHook.add(new Hook("location", "isProviderEnabled", "ACCESS_COARSE_LOCATION,ACCESS_FINE_LOCATION", 1, "1.99.1", null).notAOSP(19).dangerous());
		mListHook.add(new Hook("location", "requestLocationUpdates", "ACCESS_COARSE_LOCATION,ACCESS_FINE_LOCATION", 1, null, null).notAOSP(19));
		mListHook.add(new Hook("location", "requestSingleUpdate", "ACCESS_COARSE_LOCATION,ACCESS_FINE_LOCATION", 9, null, null).notAOSP(19));
		mListHook.add(new Hook("location", "sendExtraCommand", "ACCESS_COARSE_LOCATION,ACCESS_FINE_LOCATION", 3, null, null).notAOSP(19));

		mListHook.add(new Hook("location", "Srv_requestGeofence", "ACCESS_COARSE_LOCATION,ACCESS_FINE_LOCATION", 17, "2.99", "addGeofence").AOSP(19));
		mListHook.add(new Hook("location", "Srv_addGpsStatusListener", "ACCESS_COARSE_LOCATION,ACCESS_FINE_LOCATION", 3, "2.99", "addGpsStatusListener").AOSP(19));
		mListHook.add(new Hook("location", "Srv_getAllProviders", "ACCESS_COARSE_LOCATION,ACCESS_FINE_LOCATION", 1, "2.99", "getAllProviders").AOSP(19).dangerous());
		mListHook.add(new Hook("location", "Srv_getBestProvider", "ACCESS_COARSE_LOCATION,ACCESS_FINE_LOCATION", 1, "2.99", "getBestProvider").AOSP(19).dangerous());
		mListHook.add(new Hook("location", "Srv_getProviders", "ACCESS_COARSE_LOCATION,ACCESS_FINE_LOCATION", 1, "2.99", "getProviders").AOSP(19).dangerous());
		mListHook.add(new Hook("location", "Srv_isProviderEnabled", "ACCESS_COARSE_LOCATION,ACCESS_FINE_LOCATION", 1, "2.99", "isProviderEnabled").AOSP(19).dangerous());
		mListHook.add(new Hook("location", "Srv_getLastLocation", "ACCESS_COARSE_LOCATION,ACCESS_FINE_LOCATION", 1, "2.99", "getLastKnownLocation").AOSP(19));
		mListHook.add(new Hook("location", "Srv_requestLocationUpdates", "ACCESS_COARSE_LOCATION,ACCESS_FINE_LOCATION", 1, "2.99", "requestLocationUpdates").AOSP(19));
		mListHook.add(new Hook("location", "Srv_sendExtraCommand", "ACCESS_COARSE_LOCATION,ACCESS_FINE_LOCATION", 3, "2.99", "sendExtraCommand").AOSP(19));

		mListHook.add(new Hook("location", "enableLocationUpdates", "CONTROL_LOCATION_UPDATES", 10, null, null).notAOSP(19));
		mListHook.add(new Hook("location", "getAllCellInfo", "ACCESS_COARSE_UPDATES", 17, null, null).notAOSP(19));
		mListHook.add(new Hook("location", "getCellLocation", "ACCESS_COARSE_LOCATION,ACCESS_FINE_LOCATION", 1, null, null).notAOSP(19));
		mListHook.add(new Hook("location", "getNeighboringCellInfo", "ACCESS_COARSE_UPDATES", 3, null, null).notAOSP(19));

		mListHook.add(new Hook("location", "Srv_enableLocationUpdates", "CONTROL_LOCATION_UPDATES", 10, "2.99", "enableLocationUpdates").AOSP(19));
		mListHook.add(new Hook("location", "Srv_getAllCellInfo", "ACCESS_COARSE_UPDATES", 17, "2.99", "getAllCellInfo").AOSP(19));
		mListHook.add(new Hook("location", "Srv_getCellLocation", "ACCESS_COARSE_LOCATION,ACCESS_FINE_LOCATION", 1, "2.99", "getCellLocation").AOSP(19));
		mListHook.add(new Hook("location", "Srv_getNeighboringCellInfo", "ACCESS_COARSE_UPDATES", 3, "2.99", "getNeighboringCellInfo").AOSP(19));

		mListHook.add(new Hook("location", "WiFi.getScanResults", "ACCESS_WIFI_STATE", 1, "2.2.2", "getScanResults").notAOSP(19).dangerous());
		mListHook.add(new Hook("location", "WiFi.Srv_getScanResults", "ACCESS_WIFI_STATE", 1, "2.99", "WiFi.getScanResults").AOSP(19).dangerous());

		mListHook.add(new Hook("location", "listen", "ACCESS_COARSE_LOCATION", 1, null, null).notAOSP(19));
		mListHook.add(new Hook("location", "Srv_listen", "ACCESS_COARSE_LOCATION", 1, null, null).AOSP(19));

		mListHook.add(new Hook("location", "GMS.addGeofences", "ACCESS_COARSE_LOCATION,ACCESS_FINE_LOCATION", 1, null, null).unsafe().optional());
		mListHook.add(new Hook("location", "GMS.getLastLocation", "ACCESS_COARSE_LOCATION,ACCESS_FINE_LOCATION", 1, null, null).unsafe().optional());
		mListHook.add(new Hook("location", "GMS.requestLocationUpdates", "ACCESS_COARSE_LOCATION,ACCESS_FINE_LOCATION", 1, null, null).unsafe().optional());
		mListHook.add(new Hook("location", "GMS.requestActivityUpdates", "com.google.android.gms.permission.ACTIVITY_RECOGNITION", 1, null, null).unsafe());

		mListHook.add(new Hook("location", "GMS5.getLastLocation", "ACCESS_COARSE_LOCATION,ACCESS_FINE_LOCATION", 1, "2.99.26", null).unsafe().optional());
		mListHook.add(new Hook("location", "GMS5.requestLocationUpdates", "ACCESS_COARSE_LOCATION,ACCESS_FINE_LOCATION", 1, "2.99.26", null).unsafe().optional());
		mListHook.add(new Hook("location", "GMS5.requestActivityUpdates", "com.google.android.gms.permission.ACTIVITY_RECOGNITION", 1, "2.99.26", null).unsafe());

		mListHook.add(new Hook("location", "MapV1.enableMyLocation", "ACCESS_COARSE_LOCATION,ACCESS_FINE_LOCATION", 1, "2.1.25", null).unsafe().optional());

		mListHook.add(new Hook("location", "MapV2.getMyLocation", "ACCESS_COARSE_LOCATION,ACCESS_FINE_LOCATION", 1, "2.1.25", null).unsafe().optional());
		mListHook.add(new Hook("location", "MapV2.getPosition", "ACCESS_COARSE_LOCATION,ACCESS_FINE_LOCATION", 1, "2.1.25", null).unsafe().optional());
		mListHook.add(new Hook("location", "MapV2.setLocationSource", "ACCESS_COARSE_LOCATION,ACCESS_FINE_LOCATION", 1, "2.1.25", null).unsafe().optional());
		mListHook.add(new Hook("location", "MapV2.setOnMapClickListener", "ACCESS_COARSE_LOCATION,ACCESS_FINE_LOCATION", 1, "2.1.25", null).unsafe().optional());
		mListHook.add(new Hook("location", "MapV2.setOnMapLongClickListener", "ACCESS_COARSE_LOCATION,ACCESS_FINE_LOCATION", 1, "2.1.25", null).unsafe().optional());
		mListHook.add(new Hook("location", "MapV2.setOnMyLocationChangeListener", "ACCESS_COARSE_LOCATION,ACCESS_FINE_LOCATION", 1, "2.1.25", null).unsafe().optional());

		// ActivityManager(Service)
		mListHook.add(new Hook(null, "Srv_startActivities", "", 19, null, null).AOSP(19));
		mListHook.add(new Hook(null, "Srv_startActivity", "", 19, null, null).AOSP(19));
		mListHook.add(new Hook(null, "Srv_startActivityAsUser", "", 19, null, null).AOSP(19));
		mListHook.add(new Hook(null, "Srv_startActivityAndWait", "", 19, null, null).AOSP(19));
		mListHook.add(new Hook(null, "Srv_startActivityWithConfig", "", 19, null, null).AOSP(19));

		mListHook.add(new Hook(null, "inputDispatchingTimedOut", "", 17, null, null));
		mListHook.add(new Hook(null, "appNotResponding", "", 15, null, null));
		mListHook.add(new Hook(null, "systemReady", "", 15, null, null));
		mListHook.add(new Hook(null, "finishBooting", "", 15, null, null));
		mListHook.add(new Hook(null, "setLockScreenShown", "", 17, null, null).optional());
		mListHook.add(new Hook(null, "goingToSleep", "", 16, null, null));
		mListHook.add(new Hook(null, "wakingUp", "", 16, null, null));
		mListHook.add(new Hook(null, "shutdown", "", 15, null, null));

		// Application
		mListHook.add(new Hook(null, "onCreate", "", 1, null, null));

		// Binder
		mListHook.add(new Hook(null, "execTransact", "", 1, null, null).notAOSP(19));
		mListHook.add(new Hook(null, "transact", "", 1, null, null).notAOSP(19));

		// ContextImpl
		mListHook.add(new Hook(null, "getPackageManager", "", 1, null, null).notAOSP(19));

		// ContextImpl / Activity
		mListHook.add(new Hook(null, "getSystemService", "", 1, null, null).notAOSP(19));

		// FusedLocationProviderApi // ActivityRecognitionApi
		mListHook.add(new Hook(null, "GMS5.removeLocationUpdates", "", 1, "2.99.26", null).optional());
		mListHook.add(new Hook(null, "GMS5.removeActivityUpdates", "", 1, "2.99.26", null).optional());

		// GoogleApiClient.Builder
		mListHook.add(new Hook(null, "GMS5.addConnectionCallbacks", "", 1, null, null).optional());
		mListHook.add(new Hook(null, "GMS5.onConnected", "", 1, null, null));

		// LocationClient / ActivityRecognitionClient
		mListHook.add(new Hook(null, "GMS.removeActivityUpdates", "", 1, null, null));
		mListHook.add(new Hook(null, "GMS.removeGeofences", "", 1, null, null).optional());
		mListHook.add(new Hook(null, "GMS.removeLocationUpdates", "", 1, null, null).optional());

		// LocationManager/Service
		mListHook.add(new Hook(null, "removeUpdates", "", 3, null, null).notAOSP(19));
		mListHook.add(new Hook(null, "Srv_removeUpdates", "", 19, null, null));
		mListHook.add(new Hook(null, "Srv_removeGeofence", "", 19, null, null));
		mListHook.add(new Hook(null, "Srv_removeGpsStatusListener", "", 19, null, null));
		mListHook.add(new Hook(null, "MapV1.disableMyLocation", "", 1, null, null).optional());

		// TelephonyManager
		mListHook.add(new Hook(null, "disableLocationUpdates", "", 10, null, null).notAOSP(19));
		mListHook.add(new Hook(null, "Srv_disableLocationUpdates", "", 19, null, null));

		// Secure Settings
		mListHook.add(new Hook(null, "putFloat", "", 1, null, null));
		mListHook.add(new Hook(null, "putInt", "", 1, null, null));
		mListHook.add(new Hook(null, "putLong", "", 1, null, null));
		mListHook.add(new Hook(null, "putString", "", 1, null, null));
		mListHook.add(new Hook(null, "setLocationProviderEnabled", "", 1, null, null));

		// UtilHook
		mListHook.add(new Hook(null, "isXposedEnabled", "", 15, null, null));

		// @formatter:on
		return mListHook;
	}

	public static void annotate(Resources resources) {
		if (mAnnotated)
			return;

		String self = Meta.class.getPackage().getName();
		for (Hook hook : get())
			if (hook.getRestrictionName() != null) {
				String name = hook.getRestrictionName() + "_" + hook.getName();
				name = name.replace(".", "_").replace("/", "_").replace("%", "_").replace("-", "_");
				int resId = resources.getIdentifier(name, "string", self);
				if (resId > 0)
					hook.annotate(resources.getString(resId));
				else
					Util.log(null, Log.WARN, "Missing annotation hook=" + hook);
			}

		mAnnotated = true;
	}
}