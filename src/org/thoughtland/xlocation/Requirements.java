package org.thoughtland.xlocation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InterfaceAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.GpsStatus;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

public class Requirements {
	private static String[] cIncompatible = new String[] { "com.lbe.security" };

	@SuppressWarnings("unchecked")
	public static void check(final ActivityBase context) {
		// Check Android version
		if (Build.VERSION.SDK_INT != Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
				&& Build.VERSION.SDK_INT != Build.VERSION_CODES.JELLY_BEAN
				&& Build.VERSION.SDK_INT != Build.VERSION_CODES.JELLY_BEAN_MR1
				&& Build.VERSION.SDK_INT != Build.VERSION_CODES.JELLY_BEAN_MR2
				&& Build.VERSION.SDK_INT != Build.VERSION_CODES.KITKAT) {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
			alertDialogBuilder.setTitle(R.string.app_name);
			alertDialogBuilder.setMessage(R.string.app_wrongandroid);
			alertDialogBuilder.setIcon(context.getThemed(R.attr.icon_launcher));
			alertDialogBuilder.setPositiveButton(context.getString(android.R.string.ok),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent androidIntent = new Intent(Intent.ACTION_VIEW);
							androidIntent.setData(Uri.parse("https://github.com/M66B/XLocation#installation"));
							context.startActivity(androidIntent);
						}
					});
			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();
		}

		// Check if XLocation is enabled
		if (Util.isXposedEnabled()) {
			// Check privacy client
			try {
				if (PrivacyService.checkClient()) {
					List<String> listError = (List<String>) PrivacyService.getClient().check();
					if (listError.size() > 0)
						sendSupportInfo(TextUtils.join("\r\n", listError), context);
				}
			} catch (Throwable ex) {
				sendSupportInfo(ex.toString(), context);
			}
		} else {
			// @formatter:off
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
			alertDialogBuilder.setTitle(R.string.app_name);
			alertDialogBuilder.setMessage(R.string.app_notenabled);
			alertDialogBuilder.setIcon(context.getThemed(R.attr.icon_launcher));
			alertDialogBuilder.setPositiveButton(context.getString(android.R.string.ok),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent xInstallerIntent = new Intent("de.robv.android.xposed.installer.OPEN_SECTION")
								.setPackage("de.robv.android.xposed.installer")
								.putExtra("section", "modules")
								.putExtra("module", context.getPackageName())
								.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							context.startActivity(xInstallerIntent);
						}
					});
			// @formatter:on
			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();
		}

		// Check pro enabler
		Version version = Util.getProEnablerVersion(context);
		if (version != null && !Util.isValidProEnablerVersion(version)) {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
			alertDialogBuilder.setTitle(R.string.app_name);
			alertDialogBuilder.setMessage(R.string.app_wrongenabler);
			alertDialogBuilder.setIcon(context.getThemed(R.attr.icon_launcher));
			alertDialogBuilder.setPositiveButton(context.getString(android.R.string.ok),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent storeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="
									+ context.getPackageName() + ".pro"));
							context.startActivity(storeIntent);
						}
					});
			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();
		}

		// Check incompatible apps
		checkCompatibility(context);

		// Check activity thread
		try {
			Class<?> clazz = Class.forName("android.app.ActivityThread", false, null);
			try {
				clazz.getDeclaredMethod("unscheduleGcIdler");
			} catch (NoSuchMethodException ex) {
				reportClass(clazz, context);
			}
		} catch (ClassNotFoundException ex) {
			sendSupportInfo(ex.toString(), context);
		}

		// Check activity thread receiver data
		try {
			Class<?> clazz = Class.forName("android.app.ActivityThread$ReceiverData", false, null);
			if (!checkField(clazz, "intent"))
				reportClass(clazz, context);
		} catch (ClassNotFoundException ex) {
			try {
				reportClass(Class.forName("android.app.ActivityThread", false, null), context);
			} catch (ClassNotFoundException exex) {
				sendSupportInfo(exex.toString(), context);
			}
		}

		// Check file utils
		try {
			Class<?> clazz = Class.forName("android.os.FileUtils", false, null);
			try {
				clazz.getDeclaredMethod("setPermissions", String.class, int.class, int.class, int.class);
			} catch (NoSuchMethodException ex) {
				reportClass(clazz, context);
			}
		} catch (ClassNotFoundException ex) {
			sendSupportInfo(ex.toString(), context);
		}

		// Check interface address
		if (!checkField(InterfaceAddress.class, "address") || !checkField(InterfaceAddress.class, "broadcastAddress")
				|| (PrivacyService.getClient() != null && PrivacyManager.getDefacedProp(0, "InetAddress") == null))
			reportClass(InterfaceAddress.class, context);

		// Check package manager service
		try {
			Class<?> clazz = Class.forName("com.android.server.pm.PackageManagerService", false, null);
			try {
				try {
					clazz.getDeclaredMethod("getPackageUid", String.class, int.class);
				} catch (NoSuchMethodException ignored) {
					clazz.getDeclaredMethod("getPackageUid", String.class);
				}
			} catch (NoSuchMethodException ex) {
				reportClass(clazz, context);
			}
		} catch (ClassNotFoundException ex) {
			sendSupportInfo(ex.toString(), context);
		}

		// Check GPS status
		if (!checkField(GpsStatus.class, "mSatellites"))
			reportClass(GpsStatus.class, context);

		// Check service manager
		try {
			Class<?> clazz = Class.forName("android.os.ServiceManager", false, null);
			try {
				// @formatter:off
				// public static void addService(String name, IBinder service)
				// public static void addService(String name, IBinder service, boolean allowIsolated)
				// public static String[] listServices()
				// public static IBinder checkService(String name)
				// @formatter:on

				Method listServices = clazz.getDeclaredMethod("listServices");
				Method getService = clazz.getDeclaredMethod("getService", String.class);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
					clazz.getDeclaredMethod("addService", String.class, IBinder.class, boolean.class);
				else
					clazz.getDeclaredMethod("addService", String.class, IBinder.class);

				// Get services
				Map<String, String> mapService = new HashMap<String, String>();
				String[] services = (String[]) listServices.invoke(null);
				if (services != null)
					for (String service : services)
						if (service != null) {
							IBinder binder = (IBinder) getService.invoke(null, service);
							String descriptor = (binder == null ? null : binder.getInterfaceDescriptor());
							mapService.put(service, descriptor);
						}

				if (mapService.size() > 0) {
					// Check services
					int i = 0;
					List<String> listMissing = new ArrayList<String>();
					for (String name : XBinder.cServiceName) {
						String descriptor = XBinder.cServiceDescriptor.get(i++);
						if (descriptor != null && !XBinder.cServiceOptional.contains(name)) {
							// Check name
							boolean checkDescriptor = false;

							if (mapService.containsKey(name))
								checkDescriptor = true;
							else
								listMissing.add(name);

							// Check descriptor
							if (checkDescriptor) {
								String d = mapService.get(name);
								if (d != null && !d.equals(descriptor))
									listMissing.add(descriptor);
							}
						}
					}

					// Check result
					if (listMissing.size() > 0) {
						List<String> listService = new ArrayList<String>();
						for (String service : mapService.keySet())
							listService.add(String.format("%s: %s", service, mapService.get(service)));
						sendSupportInfo("Missing:\r\n" + TextUtils.join("\r\n", listMissing) + "\r\n\r\nAvailable:\r\n"
								+ TextUtils.join("\r\n", listService), context);
					}
				}
			} catch (NoSuchMethodException ex) {
				reportClass(clazz, context);
			} catch (Throwable ex) {
				Util.bug(null, ex);
			}
		} catch (ClassNotFoundException ex) {
			sendSupportInfo(ex.toString(), context);
		}

		// Check context services
		checkService(context, Context.LOCATION_SERVICE,
				new String[] { "android.location.LocationManager", "android.location.ZTEPrivacyLocationManager",
						"android.privacy.surrogate.PrivacyLocationManager" /* PDroid */});
		checkService(context, Context.WIFI_SERVICE, new String[] { "android.net.wifi.WifiManager",
				"com.amazon.net.AmazonWifiManager", "com.amazon.android.service.AmazonWifiManager",
				"android.privacy.surrogate.PrivacyWifiManager" /* PDroid */});
	}

	public static void checkService(ActivityBase context, String name, String[] className) {
		Object service = context.getSystemService(name);
		if (service == null)
			sendSupportInfo("Service missing name=" + name, context);
		else if (!Arrays.asList(className).contains(service.getClass().getName()))
			reportClass(service.getClass(), context);
	}

	public static void checkCompatibility(ActivityBase context) {
		for (String packageName : cIncompatible)
			try {
				ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(packageName, 0);
				if (appInfo.enabled) {
					String name = context.getPackageManager().getApplicationLabel(appInfo).toString();

					AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
					alertDialogBuilder.setTitle(R.string.app_name);
					alertDialogBuilder.setMessage(String.format(context.getString(R.string.app_incompatible), name));
					alertDialogBuilder.setIcon(context.getThemed(R.attr.icon_launcher));
					alertDialogBuilder.setPositiveButton(context.getString(android.R.string.ok),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
								}
							});
					AlertDialog alertDialog = alertDialogBuilder.create();
					alertDialog.show();
				}
			} catch (NameNotFoundException ex) {
			}
	}

	private static boolean checkField(Class<?> clazz, String fieldName) {
		try {
			clazz.getDeclaredField(fieldName);
			return true;
		} catch (NoSuchFieldException ex) {
			return false;
		}
	}

	private static void reportClass(Class<?> clazz, ActivityBase context) {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("Incompatible %s", clazz.getName()));
		sb.append("\r\n");
		sb.append("\r\n");
		for (Constructor<?> constructor : clazz.getConstructors()) {
			sb.append(constructor.toString());
			sb.append("\r\n");
		}
		sb.append("\r\n");
		for (Method method : clazz.getDeclaredMethods()) {
			sb.append(method.toString());
			sb.append("\r\n");
		}
		sb.append("\r\n");
		for (Field field : clazz.getDeclaredFields()) {
			sb.append(field.toString());
			sb.append("\r\n");
		}
		sb.append("\r\n");
		sendSupportInfo(sb.toString(), context);
	}

	public static void sendSupportInfo(final String text, final ActivityBase context) {
		Util.log(null, Log.WARN, text);

		if (Util.hasValidFingerPrint(context) || Util.isDebuggable(context)) {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
			alertDialogBuilder.setTitle(R.string.app_name);
			alertDialogBuilder.setMessage(R.string.msg_support_info);
			alertDialogBuilder.setIcon(context.getThemed(R.attr.icon_launcher));
			alertDialogBuilder.setPositiveButton(context.getString(android.R.string.ok),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int _which) {
							String ourVersion = Util.getSelfVersionName(context);
							StringBuilder sb = new StringBuilder(text);
							sb.insert(0, "\r\n");
							sb.insert(0, String.format("Override: %s\r\n", System.getenv("XLocation.AOSP")));
							sb.insert(0, String.format("Id: %s\r\n", Build.ID));
							sb.insert(0, String.format("Display: %s\r\n", Build.DISPLAY));
							sb.insert(0, String.format("Host: %s\r\n", Build.HOST));
							sb.insert(0, String.format("Device: %s\r\n", Build.DEVICE));
							sb.insert(0, String.format("Product: %s\r\n", Build.PRODUCT));
							sb.insert(0, String.format("Model: %s\r\n", Build.MODEL));
							sb.insert(0, String.format("Manufacturer: %s\r\n", Build.MANUFACTURER));
							sb.insert(0, String.format("Brand: %s\r\n", Build.BRAND));
							sb.insert(0, "\r\n");
							sb.insert(0, String.format("Android: %s (SDK %d)\r\n", Build.VERSION.RELEASE,
									Build.VERSION.SDK_INT));
							sb.insert(0, String.format("XLocation: %s\r\n", ourVersion));

							Intent sendEmail = new Intent(Intent.ACTION_SEND);
							sendEmail.setType("message/rfc822");
							sendEmail.putExtra(Intent.EXTRA_EMAIL, new String[] { "marcel+xlocation@faircode.eu" });
							sendEmail.putExtra(Intent.EXTRA_SUBJECT, "XLocation " + ourVersion + " debug info");
							sendEmail.putExtra(Intent.EXTRA_TEXT, sb.toString());
							try {
								context.startActivity(sendEmail);
							} catch (Throwable ex) {
								Util.bug(null, ex);
							}
						}
					});
			alertDialogBuilder.setNegativeButton(context.getString(android.R.string.cancel),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});
			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();
		}
	}
}
