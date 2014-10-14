package org.thoughtland.xlocation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.text.TextUtils;
import android.util.Log;

public class XILocationManager extends XHook {
	private String mMethod;
	private String mClassName;

	private static final String cClassName[] = { "android.location.LocationManager" };

	private XILocationManager(String method, String restrictionName, String className) {
		super(restrictionName, method, className+"."+method);
		//Util.log(this, Log.WARN, "creating hook for " + className + "." + method);
		mMethod = method;
		mClassName = className;
	}

	public String getClassName() {
		return mClassName;
	}

	// @formatter:off
	private enum Methods {
		getAllProviders, getProviders, updateProviders, 
		requestLocationUpdates, requestLocationUpdatesPI, removeUpdates, removeUpdatesPI, 
		addGpsStatusListener, removeGpsStatusListener, sendExtraCommand, 
		addProximityAlert, removeProximityAlert, 
		getProviderInfo, isProviderEnabled, 
		getLastKnownLocation, getFromLocation, getFromLocationName, 
		addTestProvider, removeTestProvider, 
		setTestProviderLocation, clearTestProviderLocation, 
		setTestProviderEnabled, clearTestProviderEnabled, 
		setTestProviderStatus, clearTestProviderStatus
	};
	// @formatter:on

	static private List<String> ignoreMethods = Arrays.asList(new String[]{
			"equals", "hashCode", "toString"
	});

	static private String[] contextMethods = new String[]{
		"bindService", "getSystemService", "registerReceiver", "unbindService", "unregisterReceiver", "getPackageManager"
	};

	public static List<XHook> getInstances(Class<?> clazz) {
		List<XHook> listHook = new ArrayList<XHook>();
		if (clazz != null)
			if (clazz.getName().equals( "android.app.ContextImpl")) {
				for (String methodName : contextMethods)
					listHook.add(new XILocationManager(methodName, null, clazz.getName()));
			} else
				for (Method method : clazz.getDeclaredMethods())
					if (!ignoreMethods.contains(method.getName()))
						listHook.add(new XILocationManager(method.getName(), null, clazz.getName()));
		return listHook;
	}

	static Map<Object,Object> googleLocationManagerService = Collections.synchronizedMap(new HashMap<Object,Object>());
	@Override
	protected void before(XParam param) throws Throwable {
		
		Util.log(this, Log.WARN, "!! hooked before " + mClassName + "." + mMethod + " for uid " + Binder.getCallingUid() 
				+ " with args [ " + TextUtils.join(",  ",param.args) + " ]");

		/*
		if (	mClassName.equals("com.google.android.apps.gmm.map.location.rawlocationevents.AndroidLocationEvent") ||
				mClassName.equals("com.google.android.gms.location.ILocationListener") ||
				mClassName.equals("android.app.PendingIntent"))
			Util.log(this, Log.WARN, "!! hooked before " + mClassName + "." + mMethod + " for uid " + Binder.getCallingUid() 
					+ " with args [ " + TextUtils.join(",  ",param.args) + " ]");
*/
		/*
		if (	mClassName.equals("android.location.Location") ) {
			Util.log(this, Log.WARN, "!! Location hooked before " + mClassName + "." + mMethod + " for uid " + Binder.getCallingUid() 
					+ " with args [ " + TextUtils.join(",  ",param.args) + " ]");			
		}
		*/
		/*
		if (mClassName.equals("android.app.ContextImpl") && mMethod.equals("bindService")) {
			try {
				final Intent intent = (Intent)param.args[0];
				if (	intent != null && intent.getAction() != null && intent.getPackage() != null &&
						intent.getAction().contains("com.google.android.location") &&
						intent.getPackage().equals("com.google.android.gms")) {
					final ClassLoader cl = Util.getApplication().getClassLoader();
					Class<?> ll = Class.forName("android.content.ServiceConnection", false, cl);
					final Object caller = param.args[1];
					final int mUid = Binder.getCallingUid();

					final String intentName = intent.getPackage() + " : " + intent.getAction();
					Util.log(null, Log.WARN, "Binding service " + intentName);

					InvocationHandler ih = new InvocationHandler(){
						public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
							try {
								if ("equals".equals(method.getName())) return 
										(proxy == args[0] || caller == args[0] || (caller != null && caller.equals(args[0])));
								else if("hashCode".equals(method.getName())) return System.identityHashCode(proxy);
								if (method.getName().equals("onServiceConnected")) {
									Util.log(null, Log.WARN, "onServiceConnected invoking from " + mUid + " for " + Binder.getCallingUid()
											+ " method "+ getClassName() +"/" + mMethod + ":" + method.getName());

									final Object iBinder = args[1];
									Class<?> lll = Class.forName("android.os.IBinder", false, cl);
									InvocationHandler iih = new InvocationHandler(){
										public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
											try {
												if ("equals".equals(method.getName())) return 
														(proxy == args[0] || iBinder == args[0] || (iBinder != null && iBinder.equals(args[0])));
												else if("hashCode".equals(method.getName())) return System.identityHashCode(proxy);
												Util.log(null, Log.WARN, intentName + " IBinder invoking from " + mUid + " for " + Binder.getCallingUid()
														+ " method "+ method.getName());
												if (method.getName().equals("transact")) {
													try {
														int code = (Integer)args[0];
														Parcel data = (Parcel) args[1];
														Parcel reply = (Parcel) args[2];
														int flags = (Integer) args[3];
														Util.log(null, Log.WARN, "Transacting(" + code + 
																", " + (data != null ? "[" + TextUtils.join(", ",data.readBundle().keySet()) + " ]" : "null") +
																", " + (reply != null ? "[" + TextUtils.join(", ",reply.readBundle().keySet()) + " ]" : "null")+
																", " + flags + ")");
													} catch (Exception e) {
														e.printStackTrace();
													}
												}
												return method.invoke(iBinder, args);
											} catch (Exception e) {
												e.printStackTrace();
											}
											return null;
										}
									};
									args[1] = Proxy.newProxyInstance(cl, new Class<?>[] { lll }, iih);
								}
								return method.invoke(caller, args);
							} catch (Exception e) {
								e.printStackTrace();
							}
							return null;
						}
					};
					Object serviceCon = Proxy.newProxyInstance(cl, new Class<?>[] { ll }, ih);
					googleLocationManagerService.put(param.args[1], serviceCon);
					param.args[1] = serviceCon;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
*/
		/*
		if (mClassName.equals("android.app.ContextImpl") && mMethod.equals("unbindService")) {
			try {
				Object proxyService = googleLocationManagerService.remove(param.args[0]);
				if (proxyService!=null)
					param.args[0] = proxyService;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
*/
		
		if (mClassName.startsWith("android.location.ILocation") && mMethod.equals("asInterface")) {
			try {
				ClassLoader cl = Util.getApplication().getClassLoader();
				final Object iBinder = param.args[0];
				Class<?> ll = Class.forName("android.os.IBinder", false, cl);
				final int mUid = Binder.getCallingUid();
				InvocationHandler ih = new InvocationHandler(){
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						try {
							if ("equals".equals(method.getName())) return 
									(proxy == args[0] || iBinder == args[0] || (iBinder != null && iBinder.equals(args[0])));
							else if("hashCode".equals(method.getName())) return System.identityHashCode(proxy);
							Util.log(null, Log.WARN, "IBinder invoking from " + mUid + " for " + Binder.getCallingUid()
									+ " method "+ method.getName());
							return method.invoke(iBinder, args);
						} catch (Exception e) {
							e.printStackTrace();
						}
						return null;
					}
				};
				param.args[0] = Proxy.newProxyInstance(cl, new Class<?>[] { ll }, ih);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}	
		
	}

	@Override
	protected void after(XParam param) throws Throwable {
		/*
		if (mClassName.startsWith("android.location.ILocation") && mMethod.equals("asInterface")) {
			try {
				ClassLoader cl = Util.getApplication().getClassLoader();
				final Object iLocationManager = param.getResult();
				Class<?> ll = Class.forName(mClassName.replace("$Stub", ""), false, cl);
				final int mUid = Binder.getCallingUid();
				InvocationHandler ih = new InvocationHandler(){
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						try {
							if ("equals".equals(method.getName())) return 
									(proxy == args[0] || iLocationManager == args[0] || (iLocationManager != null && iLocationManager.equals(args[0])));
							else if("hashCode".equals(method.getName())) return System.identityHashCode(proxy);
							Util.log(null, Log.WARN, mClassName.replace("android.location.","").replace("$Stub", "") + 
									" invoking from " + mUid + " for " + Binder.getCallingUid()
									+ " method "+ method.getName());
							return method.invoke(iLocationManager, args);
						} catch (Exception e) {
							e.printStackTrace();
						}
						return null;
					}
				};
				param.setResult(Proxy.newProxyInstance(cl, new Class<?>[] { ll }, ih));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}*/
	}
}
