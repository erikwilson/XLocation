package org.thoughtland.xlocation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Binder;
import android.os.Build;
import android.os.Process;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.saurik.substrate.MS;

import dalvik.system.DexFile;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XC_MethodHook;
import static de.robv.android.xposed.XposedHelpers.findClass;

// TODO: fix link error when using Cydia Substrate
public class XLocation implements IXposedHookLoadPackage, IXposedHookZygoteInit {
	private static boolean mCydia = false;
	private static String mSecret = null;
	private static List<String> mListHookError = new ArrayList<String>();
	private static List<CRestriction> mListDisabled = new ArrayList<CRestriction>();

	// http://developer.android.com/reference/android/Manifest.permission.html

	static {
		if (mListDisabled.size() == 0) {
			File disabled = new File("/data/system/xlocation/disabled");
			if (disabled.exists() && disabled.canRead())
				try {
					Log.w("XLocation", "Reading " + disabled.getAbsolutePath());
					FileInputStream fis = new FileInputStream(disabled);
					InputStreamReader ir = new InputStreamReader(fis);
					BufferedReader br = new BufferedReader(ir);
					String line;
					while ((line = br.readLine()) != null) {
						String[] name = line.split("/");
						if (name.length > 0) {
							String methodName = (name.length > 1 ? name[1] : null);
							CRestriction restriction = new CRestriction(0, name[0], methodName, null);
							Log.w("XLocation", "Disabling " + restriction);
							mListDisabled.add(restriction);
						}
					}
					br.close();
					ir.close();
					fis.close();
				} catch (Throwable ex) {
					Log.w("XLocation", ex.toString());
				}
		}
	}

	// Xposed
	public void initZygote(StartupParam startupParam) throws Throwable {
		// Check for LBE security master
		if (Util.hasLBE()) {
			Util.log(null, Log.ERROR, "LBE installed");
			return;
		}

		init(startupParam.modulePath);
	}

	public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
		// Check for LBE security master
		if (Util.hasLBE())
			return;

		handleLoadPackage(lpparam.packageName, lpparam.classLoader, mSecret);
	}

	// Cydia
	public static void initialize() {
		mCydia = true;
		init(null);

		// Self
		MS.hookClassLoad(Util.class.getName(), new MS.ClassLoadHook() {
			@Override
			public void classLoaded(Class<?> clazz) {
				hookAll(XUtilHook.getInstances(), clazz.getClassLoader(), mSecret);
			}
		});

		// TODO: Cydia: Build.SERIAL
		// TODO: Cydia: android.provider.Settings.Secure
		// TODO: Cydia: Phone instances

		// User activity
		MS.hookClassLoad("com.google.android.gms.location.ActivityRecognitionClient", new MS.ClassLoadHook() {
			@Override
			public void classLoaded(Class<?> clazz) {
				hookAll(XActivityRecognitionClient.getInstances(), clazz.getClassLoader(), mSecret);
			}
		});

		// GoogleApiClient.Builder
		MS.hookClassLoad("com.google.android.gms.common.api.GoogleApiClient", new MS.ClassLoadHook() {
			@Override
			public void classLoaded(Class<?> clazz) {
				hookAll(XGoogleApiClient.getInstances(), clazz.getClassLoader(), mSecret);
			}
		});

		// Location client
		MS.hookClassLoad("com.google.android.gms.location.LocationClient", new MS.ClassLoadHook() {
			@Override
			public void classLoaded(Class<?> clazz) {
				hookAll(XLocationClient.getInstances(), clazz.getClassLoader(), mSecret);
			}
		});

		// Google Map V1
		MS.hookClassLoad("com.google.android.maps.GeoPoint", new MS.ClassLoadHook() {
			@Override
			public void classLoaded(Class<?> clazz) {
				hookAll(XGoogleMapV1.getInstances(), clazz.getClassLoader(), mSecret);
			}
		});

		// Google Map V2
		MS.hookClassLoad("com.google.android.gms.maps.GoogleMap", new MS.ClassLoadHook() {
			@Override
			public void classLoaded(Class<?> clazz) {
				hookAll(XGoogleMapV2.getInstances(), clazz.getClassLoader(), mSecret);
			}
		});
	}

	// Common
	private static void init(String path) {
		Util.log(null, Log.WARN, "Init path=" + path);

		// Generate secret
		mSecret = Long.toHexString(new Random().nextLong());

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
			try {
				Class<?> libcore = Class.forName("libcore.io.Libcore");
				Field fOs = libcore.getDeclaredField("os");
				fOs.setAccessible(true);
				Object os = fOs.get(null);
				Method setenv = os.getClass().getMethod("setenv", String.class, String.class, boolean.class);
				setenv.setAccessible(true);
				boolean aosp = new File("/data/system/xlocation/aosp").exists();
				setenv.invoke(os, "XLocation.AOSP", Boolean.toString(aosp), false);
				Util.log(null, Log.WARN, "AOSP mode forced=" + aosp);
			} catch (Throwable ex) {
				Util.bug(null, ex);
			}

		// System server
		try {
			// frameworks/base/services/java/com/android/server/SystemServer.java
			Class<?> cSystemServer = Class.forName("com.android.server.SystemServer");
			Method mMain = cSystemServer.getDeclaredMethod("main", String[].class);
			if (mCydia)
				MS.hookMethod(cSystemServer, mMain, new MS.MethodAlteration<Object, Void>() {
					@Override
					public Void invoked(Object thiz, Object... args) throws Throwable {
						PrivacyService.register(mListHookError, mSecret);
						LocationProxyService.register();
						return invoke(thiz, args);
					}
				});
			else
				XposedBridge.hookMethod(mMain, new XC_MethodHook() {
					@Override
					protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
						PrivacyService.register(mListHookError, mSecret);
						LocationProxyService.register();
					}
				});
		} catch (Throwable ex) {
			Util.bug(null, ex);
		}

		// Activity manager service
		hookAll(XActivityManagerService.getInstances(), null, mSecret);

		// Application
		hookAll(XApplication.getInstances(), null, mSecret);

		// Binder device
		hookAll(XBinder.getInstances(), null, mSecret);

		// Context wrapper
		hookAll(XContextImpl.getInstances(), null, mSecret);

		// Location manager
		hookAll(XLocationManager.getInstances(null), null, mSecret);

		// Settings secure
		if (!mCydia)
			hookAll(XSettingsSecure.getInstances(), null, mSecret);

		// Telephone service
		hookAll(XTelephonyManager.getInstances(null), null, mSecret);

		// Wi-Fi service
		hookAll(XWifiManager.getInstances(null), null, mSecret);

		// Strict Mode
		hookAll(XStrictMode.getInstances(), null, mSecret);

		Util.log(null,Log.WARN,"Done init " + Util.getApplication() + ":" + Util.getActivity() + ":" + Util.getActivityContext());
	}

	static Set<String> probedGms = new HashSet<String>();

	public static void probe(Class<?> clazz) {		
		if (clazz==null) {
			Util.log(null, Log.WARN, "no info for null class");
			return;
		}
		Field[] fields = clazz.getDeclaredFields();
		Constructor<?>[] constructors = clazz.getDeclaredConstructors();
		Method[] methods = clazz.getDeclaredMethods();
		Class<?>[] classes = clazz.getDeclaredClasses();

		try {
			Util.log(null, Log.WARN, "*** Info about "+ (Modifier.isStatic(clazz.getModifiers())?"static ":"") +
					(Modifier.isInterface(clazz.getModifiers())?"interface ":"") + clazz.getName());
			Util.log(null, Log.WARN, "  Fields : ");
			for (Field field : fields)
				Util.log(null, Log.WARN, "    " + (field.isAccessible()?"  private ":"  public ") + 
						(Modifier.isStatic(field.getModifiers())?"static ":"") +
						(Modifier.isInterface(field.getModifiers())?"interface ":"") + 
						field.getType().getName() + " " + field.getName());
			Util.log(null, Log.WARN, "  Constructors : ");
			for (Constructor<?> constructor : constructors)
				Util.log(null, Log.WARN, "    " + (constructor.isAccessible()?"  private ":"  public ") + 
						(Modifier.isStatic(constructor.getModifiers())?"static ":"") +
						(Modifier.isInterface(constructor.getModifiers())?"interface ":"") + 
						constructor.getName() + "(" + TextUtils.join(", ",constructor.getParameterTypes())+" )");
			Util.log(null, Log.WARN, "  Methods : ");
			for (Method method : methods)
				Util.log(null, Log.WARN, "    " + (method.isAccessible()?"  private ":"  public ") + 
						(Modifier.isStatic(method.getModifiers())?"static ":"") +
						(Modifier.isInterface(method.getModifiers())?"interface ":"") + 
						method.getName() + "(" + TextUtils.join(", ",method.getParameterTypes())+" )");
			Util.log(null, Log.WARN, "  Classes : ");
			for (Class<?> clazzz : classes)
				Util.log(null, Log.WARN, "    " + (Modifier.isPublic(clazzz.getModifiers())?"  private ":"  public ") + 
						(Modifier.isStatic(clazzz.getModifiers())?"static ":"") +
						(Modifier.isInterface(clazzz.getModifiers())?"interface ":"") + 
						clazzz.getName() + "<" + TextUtils.join(", ",clazzz.getTypeParameters())+" >");
			probedGms.add(clazz.getName());
		} catch (Throwable e) {
			e.printStackTrace();			
		}
		/*
		for (Class<?> clazzz : classes)
			if (!probedGms.contains(clazzz.getName()))
				probe(clazzz);
				*/
	}

	private static void handleLoadPackage(String packageName, ClassLoader classLoader, String secret) {
		Util.log(null, Log.WARN, "Load package=" + packageName + " uid=" + Process.myUid());

		// Skip hooking self
		String self = XLocation.class.getPackage().getName();
		if (packageName.equals(self)) {
			hookAll(XUtilHook.getInstances(), classLoader, secret);
			return;
		}

		if ("com.nianticproject.ingress".equals(packageName)) {
			try {
				Context context = Util.getSystemContext();
				if (context==null)
					context = Util.getApplication();
				PackageManager pm = context.getPackageManager();
				ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
				String sourceApk = ai.publicSourceDir;
				DexFile df = new DexFile(sourceApk);
				Util.log(null, Log.WARN, "Found " + sourceApk + " for package " + packageName);
				for (Enumeration<String> iter = df.entries(); iter.hasMoreElements();) {
					String s = iter.nextElement();
					if (s.startsWith(packageName)) {
						Util.log(null, Log.WARN, "  package has class "+s);
						probe(Class.forName(s, false, classLoader));
					}
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		
		/*
		//if (packageName.equals("com.google.android.gms"))
		for (String classs : new String[] {
				"com.google.android.gms.common.internal.IGmsCallbacks",
				"com.google.android.gms.common.internal.IGmsServiceBroker",
				"com.google.android.gms.location.internal.IGeofencerCallbacks",
				"com.google.android.gms.location.internal.IGoogleLocationManagerService",
				"com.google.android.gms.location.reporting.internal.IReportingService",
				"com.google.android.gms.maps.internal.CreatorImpl",
				"com.google.android.gms.maps.internal.IOnLocationChangeListener",
				"com.google.android.gms.maps.internal.IOnMyLocationChangeListener"
		}) {
			try {
				probe(Class.forName(classs, false, classLoader));
			} catch (Throwable ignored) {
				//Util.log(null, Log.WARN, "Unable to load " + classs);
			}
		}
		 */

		for (String classs : new String[] {
				/*
				"android.location.ILocationListener",
				"android.location.ILocationListener$Stub",
				"android.location.ILocationListener$Stub$Proxy",
				"android.location.ILocationManager",
				"android.location.ILocationManager$Stub",
				"android.location.ILocationManager$Stub$Proxy",
				"com.android.server.LocationManagerService",
				"com.android.server.LocationManagerService$LocationWorkerHandler",
				"com.android.server.LocationManagerService$Receiver",
				"com.android.server.location.LocationProviderProxy",
				 */
				//"android.location.Location",
				//"android.os.ServiceManager",
				"android.app.ContextImpl",
				//"android.app.PendingIntent",
				"android.location.ILocationListener$Stub",
				"android.location.ILocationManager$Stub",
				/*
				"com.google.android.gms.maps.internal.CreatorImpl",
				"com.google.android.gms.maps.internal.IOnLocationChangeListener",
				"com.google.android.gms.maps.internal.IOnMyLocationChangeListener",
				"com.google.android.gms.location.internal.IGoogleLocationManagerService",
				"com.google.android.gms.location.ILocationListener",
				"com.google.android.gms.common.internal.IGmsCallbacks",
				"com.google.android.apps.gmm.map.location.rawlocationevents.AndroidLocationEvent"
				 */
		}) {
			try {

				//Class<?> clazz = Class.forName(classs, false, classLoader);
				//hookAll(XILocationManager.getInstances(clazz), Util.getApplication().getClassLoader(), secret);
				//Util.log(null, Log.WARN, "Hooked class " + classs + "!!!");
				/*
				if (classs.equals("com.google.android.gms.location.ILocationListener"))
					probe(clazz);
				 */
			} catch (Throwable ignored) {
			}
		}


		// Activity recognition
		try {
			Class.forName("com.google.android.gms.location.ActivityRecognitionClient", false, classLoader);
			hookAll(XActivityRecognitionClient.getInstances(), classLoader, secret);
		} catch (Throwable ignored) {
		}

		// GoogleApiClient.Builder
		try {
			Class.forName("com.google.android.gms.common.api.GoogleApiClient$Builder", false, classLoader);
			hookAll(XGoogleApiClient.getInstances(), classLoader, secret);
		} catch (Throwable ignored) {
		}

		// Google Map V1
		try {
			Class.forName("com.google.android.maps.GeoPoint", false, classLoader);
			hookAll(XGoogleMapV1.getInstances(), classLoader, secret);
		} catch (Throwable ignored) {
		}

		// Google Map V2
		try {
			Class.forName("com.google.android.gms.maps.GoogleMap", false, classLoader);
			hookAll(XGoogleMapV2.getInstances(), classLoader, secret);
		} catch (Throwable ignored) {
		}

		// Location client
		try {
			Class.forName("com.google.android.gms.location.LocationClient", false, classLoader);
			hookAll(XLocationClient.getInstances(), classLoader, secret);
		} catch (Throwable ignored) {
		}

		// Location Manager
		try {
			Class.forName("com.google.android.gms.location.LocationManager", false, classLoader);
			hookAll(XLocationManager.getInstances(null), classLoader, secret);
		} catch (Throwable ignored) {
		}

		// Context wrapper
		try {
			Class.forName("android.app.ContextImpl", false, classLoader);
			hookAll(XContextImpl.getInstances(), classLoader, mSecret);
		} catch (Throwable ignored) {
		}

		// Binder device
		try {
			Class.forName("android.os.Binder", false, classLoader);
			hookAll(XBinder.getInstances(), classLoader, mSecret);
		} catch (Throwable ignored) {
		}

		// Phone interface manager
		if ("com.android.phone".equals(packageName))
			hookAll(XTelephonyManager.getPhoneInstances(), classLoader, secret);

		try {
			//if (Util.getApplication() != null && Util.getActivity() != null) {
			Util.log(null,Log.WARN,"Registering a new OnTouch thing for uid " + Binder.getCallingUid());
			LocationProxyService.getClient().registerOnTouch(new OnTouch());
			//}
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	

	}

	public static void handleGetSystemService(String name, String className, String secret) {
		Util.log(null, Log.WARN, "handleGetSystemService("+ name+", "+ className+", "+ secret+") from uid " + Binder.getCallingUid());
		if (PrivacyManager.getTransient(className, null) == null) {
			PrivacyManager.setTransient(className, Boolean.toString(true));

			if (name.equals(Context.LOCATION_SERVICE))
				hookAll(XLocationManager.getInstances(className), null, secret);
			else if (name.equals(Context.TELEPHONY_SERVICE))
				hookAll(XTelephonyManager.getInstances(className), null, secret);
			else if (name.equals(Context.WIFI_SERVICE))
				hookAll(XWifiManager.getInstances(className), null, secret);
		}
	}

	public static void hookAll(List<XHook> listHook, ClassLoader classLoader, String secret) {
		for (XHook hook : listHook)
			if (hook.getRestrictionName() == null)
				hook(hook, classLoader, secret);
			else {
				CRestriction crestriction = new CRestriction(0, hook.getRestrictionName(), null, null);
				CRestriction mrestriction = new CRestriction(0, hook.getRestrictionName(), hook.getMethodName(), null);
				if (mListDisabled.contains(crestriction) || mListDisabled.contains(mrestriction))
					Util.log(hook, Log.WARN, "Skipping " + hook);
				else
					hook(hook, classLoader, secret);
			}
	}

	private static void hook(final XHook hook, ClassLoader classLoader, String secret) {
		// Get meta data
		Hook md = PrivacyManager.getHook(hook.getRestrictionName(), hook.getSpecifier());

		if (md == null) {
			String message = "Not found hook=" + hook;
			mListHookError.add(message);
			//Util.log(hook, Log.ERROR, message);
		} else 
			if (!md.isAvailable())
				return;

		// Provide secret
		if (secret == null)
			Util.log(hook, Log.ERROR, "Secret missing hook=" + hook);
		hook.setSecret(secret);

		try {
			// Find class
			Class<?> hookClass = null;
			try {
				if (mCydia)
					hookClass = Class.forName(hook.getClassName(), false, classLoader);
				else
					hookClass = findClass(hook.getClassName(), classLoader);
			} catch (Throwable ex) {
				String message = "Class not found hook=" + hook;
				mListHookError.add(message);
				Util.log(hook, Log.WARN, message);
				if (md == null || !md.isOptional()) {
					Util.logStack(hook, Log.ERROR);
				}
			}

			// Get members
			List<Member> listMember = new ArrayList<Member>();
			// TODO: enable/disable superclass traversal
			Class<?> clazz = hookClass;
			while (clazz != null && !"android.content.ContentProvider".equals(clazz.getName()))
				try {
					if (hook.getMethodName() == null) {
						for (Constructor<?> constructor : clazz.getDeclaredConstructors())
							if (!Modifier.isAbstract(constructor.getModifiers())
									&& Modifier.isPublic(constructor.getModifiers()) ? hook.isVisible() : !hook
											.isVisible())
								listMember.add(constructor);
						break;
					} else {
						for (Method method : clazz.getDeclaredMethods())
							if (method.getName().equals(hook.getMethodName())
									&& !Modifier.isAbstract(method.getModifiers())
									&& (Modifier.isPublic(method.getModifiers()) ? hook.isVisible() : !hook.isVisible()))
								listMember.add(method);
					}
					clazz = clazz.getSuperclass();
				} catch (Throwable ex) {
					if (ex.getClass().equals(ClassNotFoundException.class))
						break;
					else
						throw ex;
				}

			// Hook members
			for (Member member : listMember)
				try {
					if (mCydia) {
						XMethodAlteration alteration = new XMethodAlteration(hook, member);
						if (member instanceof Method)
							MS.hookMethod(member.getDeclaringClass(), (Method) member, alteration);
						else
							MS.hookMethod(member.getDeclaringClass(), (Constructor<?>) member, alteration);
					} else
						XposedBridge.hookMethod(member, new XMethodHook(hook));
				} catch (NoSuchFieldError ex) {
					Util.log(hook, Log.WARN, ex.toString());
				} catch (Throwable ex) {
					mListHookError.add(ex.toString());
					Util.bug(hook, ex);
				}

			// Check if members found
			/*
			if (listMember.isEmpty() && !hook.getClassName().startsWith("com.google.android.gms")) {
				String message = "Method not found hook=" + hook;
				if (md == null || !md.isOptional())
					mListHookError.add(message);
				Util.log(hook, md != null && md.isOptional() ? Log.WARN : Log.ERROR, message);
			}*/
		} catch (Throwable ex) {
			mListHookError.add(ex.toString());
			Util.bug(hook, ex);
		}
	}

	// Helper classes

	private static class XMethodHook extends XC_MethodHook {
		private XHook mHook;

		public XMethodHook(XHook hook) {
			mHook = hook;
		}

		@Override
		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
			try {
				// Do not restrict Zygote
				if (Process.myUid() <= 0)
					return;

				// Pre processing
				XParam xparam = XParam.fromXposed(param);

				long start = System.currentTimeMillis();

				// Execute hook
				mHook.before(xparam);

				long ms = System.currentTimeMillis() - start;
				if (ms > PrivacyManager.cWarnHookDelayMs)
					Util.log(mHook, Log.WARN, String.format("%s %d ms", param.method.getName(), ms));

				// Post processing
				if (xparam.hasResult())
					param.setResult(xparam.getResult());
				if (xparam.hasThrowable())
					param.setThrowable(xparam.getThrowable());
				param.setObjectExtra("xextra", xparam.getExtras());
			} catch (Throwable ex) {
				Util.bug(null, ex);
			}
		}

		@Override
		protected void afterHookedMethod(MethodHookParam param) throws Throwable {
			if (!param.hasThrowable())
				try {
					// Do not restrict Zygote
					if (Process.myUid() <= 0)
						return;

					// Pre processing
					XParam xparam = XParam.fromXposed(param);
					xparam.setExtras(param.getObjectExtra("xextra"));

					long start = System.currentTimeMillis();

					// Execute hook
					mHook.after(xparam);

					long ms = System.currentTimeMillis() - start;
					if (ms > PrivacyManager.cWarnHookDelayMs)
						Util.log(mHook, Log.WARN, String.format("%s %d ms", param.method.getName(), ms));

					// Post processing
					if (xparam.hasResult())
						param.setResult(xparam.getResult());
					if (xparam.hasThrowable())
						param.setThrowable(xparam.getThrowable());
				} catch (Throwable ex) {
					Util.bug(null, ex);
				}
		}
	};

	private static class XMethodAlteration extends MS.MethodAlteration<Object, Object> {
		private XHook mHook;
		private Member mMember;

		public XMethodAlteration(XHook hook, Member member) {
			mHook = hook;
			mMember = member;
		}

		@Override
		public Object invoked(Object thiz, Object... args) throws Throwable {
			if (Process.myUid() <= 0)
				return invoke(thiz, args);

			XParam xparam = XParam.fromCydia(mMember, thiz, args);
			mHook.before(xparam);

			if (!xparam.hasResult() || xparam.hasThrowable()) {
				try {
					Object result = invoke(thiz, args);
					xparam.setResult(result);
				} catch (Throwable ex) {
					xparam.setThrowable(ex);
				}

				mHook.after(xparam);
			}

			if (xparam.hasThrowable())
				throw xparam.getThrowable();
			return xparam.getResult();
		}
	}
}
