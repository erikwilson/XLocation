package org.thoughtland.xlocation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;

import android.app.Application;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.location.Location;
import android.os.Binder;
import android.os.IInterface;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

public class LocationProxy implements LocationUpdater.Remove, LocationUpdater.Request {
	
	private final Map<String,Integer> mWhitelist = new HashMap<String,Integer>();
	private final Map<Object, Object> mMapProxy = Collections.synchronizedMap(new WeakHashMap<Object, Object>());
	private final Map<Object,LocationRequest> mLocationListeners =
			Collections.synchronizedMap(new WeakHashMap<Object,LocationRequest>());
	private final Map<Object, Entry<Context,BroadcastReceiver>> mMapReceiverContext = 
			Collections.synchronizedMap(new WeakHashMap<Object, Entry<Context,BroadcastReceiver>>());
	private String mRemoveUpdates = "removeLocationUpdates";
	private int[] mRemoveArgs = new int[]{-1};
	private boolean initialized = false;

	static public final int CALLBACK_REMOVE_UPDATES = 0x1;
	static public final int CALLBACK_REQUEST_UPDATES = 0x2;

	private final Map<Object,PrivacyManager.DenyMethods> mDenyMethod = new HashMap<Object,PrivacyManager.DenyMethods>();
	
	private final Map<Integer,OnTouchProxy> mOnTouchProxy = new HashMap<Integer,OnTouchProxy>();

	private final IStatusCallback statusCallback = new IStatusCallback.Stub() {
		@Override
		public void setStatus(int status) throws RemoteException {
			if ((status & CALLBACK_REMOVE_UPDATES)!=0)
				removeUpdates();
			if ((status & CALLBACK_REQUEST_UPDATES)!=0)
				requestUpdates();	
		}
	};

	LocationProxy() {}

	LocationProxy(String removeUpdates) {
		mRemoveUpdates = removeUpdates;
	}

	LocationProxy(int[] removeArgs) {
		mRemoveArgs = removeArgs;
	}

	LocationProxy(String removeUpdates, int[] removeArgs) {
		mRemoveUpdates = removeUpdates;
		mRemoveArgs = removeArgs;
	}

	private void removeUpdates() {
		Util.log(null, Log.WARN, "RemoveUpdates for " + this + " has " + mLocationListeners.size() + " listeners");
		Set<Object> failedUpdates = new HashSet<Object>();
		synchronized(mLocationListeners) {
			for (Object key : mLocationListeners.keySet()) {
				LocationRequest lr = mLocationListeners.get(key);
				if (!lr.remove.removeUpdates(lr)) {
					failedUpdates.add(key);
				}
			}
		}
		for (Object key : failedUpdates) {
			Util.log(null, Log.WARN, "Removing locationrequest for " + key + " due to error in removeUpdate");
			mLocationListeners.remove(key);
		}
	}

	private void requestUpdates() {
		Util.log(null, Log.WARN, "requestUpdates for " + this + " has " + mLocationListeners.size() + " listeners");
		Set<Object> failedUpdates = new HashSet<Object>();
		synchronized(mLocationListeners) {
			for (Object key : mLocationListeners.keySet()) {
				LocationRequest lr = mLocationListeners.get(key);
				if (!lr.request.requestUpdates(lr)) {
					failedUpdates.add(key);
				}
			}
		}
		for (Object key : failedUpdates) {
			Util.log(null, Log.WARN, "Removing locationrequest for " + key + " due to error in requestUpdate");
			mLocationListeners.remove(key);
		}
	}

	private void setup() {
		try { LocationProxyService.getClient().registerUpdates(statusCallback); }
		catch (Exception e) { e.printStackTrace(); }
	}

	private void register(Object hook, XParam param, int arg) {
		//if (Util.getApplication().getPackageName().equals("android")) return;
		if (!initialized) {
			setup();
			initialized = true;
		}
		Object key = param.args[arg];
		if (!mLocationListeners.containsKey(getIBinder(key)))
			mLocationListeners.put(getIBinder(key),new LocationRequest(hook,param,key,this));
	}

	private Object getIBinder(final Object key) {
		return (key instanceof IInterface ? ((IInterface) key).asBinder() : key);
	}

	private Object proxyPendingIntent(final PendingIntent originalIntent, final boolean single, final boolean restricted) {
		try {
			return LocationProxyService.getClient().proxyPendingIntent(originalIntent, single, restricted);
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		}
	}

	private Object proxyLocationListener (final Object oListener, final String sClass) {
		try {
			ClassLoader cl = Util.getApplication().getClassLoader();
			Class<?> ll = Class.forName(sClass, false, cl);
			final int mUid = Binder.getCallingUid();
			InvocationHandler ih = new InvocationHandler(){
				public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
					try {
						if ("equals".equals(method.getName())) return proxy == args[0];
						else if("hashCode".equals(method.getName())) return System.identityHashCode(proxy);
						Util.log(null, Log.WARN, "proxyLocationListener invoking " + method.getName());
						if ("onLocationChanged".equals(method.getName())) {
							args[0] = PrivacyManager.getDefacedLocation(mUid, (Location)args[0]);
							Util.log(null,Log.WARN,"Set defaced location "+args[0]);
						}
						return method.invoke(oListener, args);
					} catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				}
			};
			return Proxy.newProxyInstance(cl, new Class<?>[] { ll }, ih);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void setupProxy(XParam param, int arg, String interfase, boolean pendingResult, boolean single, boolean restricted) {
		Object key = param.args[arg];
		Object proxy = mMapProxy.get(getIBinder(key));
		if (proxy == null) {
			if (key instanceof PendingIntent)
				proxy = proxyPendingIntent((PendingIntent)key, single, restricted);
			else if (key != null && param.thisObject != null)
				proxy = proxyLocationListener(key, interfase);
			if (proxy != null) {
				Util.log(null, Log.INFO, "Created new proxy object " + proxy);
				if (!single)
					mMapProxy.put(getIBinder(key), proxy);
			}
		}

		if (proxy != null) // Use proxy
			param.args[arg] = proxy;
		else {
			Util.log(null, Log.ERROR, "Problem creating proxy!");
			Object result = null;
			if (pendingResult) {
				try { result = XGoogleApiClient.getPendingResult(param.thisObject.getClass().getClassLoader()); }
				catch (Throwable e) { e.printStackTrace(); }
			}
			param.setResult(result);
		}
	}

	void proxy(XHook hook, XParam param, int arg, String interfase) throws Throwable {
		proxy( hook, false, param, null, arg, arg, interfase, false);
	}

	void proxy(XHook hook, XParam param, String extra, int arg, String interfase) throws Throwable {
		proxy( hook, false, param, extra, arg, arg, interfase, false);
	}

	void proxy(XHook hook, boolean single, XParam param, String extra, int arg, String interfase) throws Throwable {
		proxy( hook, single, param, extra, arg, arg, interfase, false);
	}

	void proxy(XHook hook, XParam param, int arg1, int arg2, String interfase) throws Throwable {
		proxy( hook, false, param, null, arg1, arg2, interfase, false);
	}

	void proxy(XHook hook, XParam param, int arg, String interfase, boolean pendingResult) throws Throwable {
		proxy( hook, false, param, null, arg, arg, interfase, pendingResult);
	}

	void proxy(XHook hook, boolean single, XParam param, String extra,
			int arg1, int arg2, String interfase, boolean pendingResult) throws Throwable {
		if (param.args.length > arg1 && param.args.length > arg2) {
			boolean pending = param.args[arg1] instanceof PendingIntent;
			int arg = (pending ? arg1 : arg2);
			Object key = param.args[arg];
			Util.log(null, Log.INFO, "proxy of " + key);
			if (single || !removeWhitelist(param.method.getName(),key)) {
				boolean restricted = 
						(extra == null && hook.isRestricted(param)) || 
						(extra != null && hook.isRestrictedExtra(param, extra));
				PrivacyManager.DenyMethods deny = hook.getDenyMethod();

				if (mDenyMethod.containsKey(key) && !mDenyMethod.get(key).equals(deny))
					unproxy(param,arg1,arg2);

				mDenyMethod.put(key, deny);

				int uid = Binder.getCallingUid();
				
				Util.log(hook,Log.WARN,"Using proxy method " + deny.name() + " for uid " + uid);
				if (uid == 1000 && !deny.name().equals("proxy")) {
					deny = PrivacyManager.DenyMethods.proxy;
					Util.log(hook,Log.WARN,"Changing proxy method to " + deny.name() + " for uid " + uid);
				}
				
				
				switch (deny) {
				case proxy:
					if(restricted || pending)
						setupProxy(param, arg, interfase, pendingResult, single, restricted);
					if (!single && !pending)
						register(hook, param, arg);
					break;
				case touch:
					OnTouchProxy onTouch = mOnTouchProxy.get(Binder.getCallingUid());
					if (onTouch == null) {
						onTouch = new OnTouchProxy();
						mOnTouchProxy.put(Binder.getCallingUid(), onTouch);
					}
					onTouch.register(key);
				case abort:
					Object result = null;
					if (pendingResult) {
						try { result = XGoogleApiClient.getPendingResult(param.thisObject.getClass().getClassLoader()); }
						catch (Throwable e) { e.printStackTrace(); }						
					}
					param.setResult(result);
					break;
				default:
					Util.log(null, Log.ERROR, "Unknown Deny Method in Proxy!");
				}
			}
		}
	}

	void unproxy(XParam param, int arg) {
		unproxy(param, arg, arg);
	}

	void unproxy(XParam param, int arg1, int arg2) {
		if (param.args.length > arg1 && param.args.length > arg2) {
			boolean pending = param.args[arg1] instanceof PendingIntent;
			int arg = (pending ? arg1 : arg2);
			if (param.args[arg] != null) {
				Object key = getIBinder(param.args[arg]);
				Util.log(null, Log.INFO, "unproxy of " + key + " to " + param.thisObject);
				if (removeWhitelist(param.method.getName(),key))
					return;

				PrivacyManager.DenyMethods deny = mDenyMethod.remove(key);
				if (deny == null) deny = PrivacyManager.DenyMethods.proxy;
				
				switch(deny) {
				case proxy:
					if (mMapProxy.containsKey(key)) {
						Object proxy = mMapProxy.remove(key);
						param.args[arg] = proxy;					
						if (pending) {
							try { LocationProxyService.getClient().unregisterPendingIntent((PendingIntent) key); }
							catch (RemoteException e) { e.printStackTrace(); }
						} else
							mLocationListeners.remove(proxy);

						if (mMapReceiverContext.containsKey(proxy)) {
							Entry<Context,BroadcastReceiver> entry = mMapReceiverContext.remove(proxy);
							entry.getKey().unregisterReceiver(entry.getValue());
						}					
					} else if (!pending)
						mLocationListeners.remove(key);
					if (mLocationListeners.isEmpty()) {
						try {
							LocationProxyService.getClient().removeUpdates(statusCallback);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					break;
				case touch:
					OnTouchProxy onTouch = mOnTouchProxy.get(Binder.getCallingUid());
					if (onTouch != null)
						onTouch.unregister(key);
					else
						Util.log(null, Log.ERROR, "Asked to unproxy unregistered OnTouch");
				case abort:
					param.setResult(null);
					break;
				default:
					Util.log(null, Log.ERROR, "Unknown Deny Method in UnProxy!");					
				}
			}
		}
	}

	private String getWLName(String name, Object o) {
		return name+"/"+getIBinder(o).hashCode();
	}

	private void addWhitelist(String name, Object o) {
		String key = getWLName(name,o);
		synchronized(mWhitelist) {
			mWhitelist.put(key, (mWhitelist.containsKey(key) ? mWhitelist.get(key) : 0) + 1);
		}
	}

	private boolean removeWhitelist(String name, Object o) {
		String key = getWLName(name,o);
		synchronized(mWhitelist) {
			if (mWhitelist.containsKey(key)) {
				if (mWhitelist.get(key)<=1)
					mWhitelist.remove(key);
				else
					mWhitelist.put(key,mWhitelist.get(key)-1);
				return true;
			} else {
				return false;
			}
		}
	}

	boolean execMethod(LocationRequest request) {
		return execMethod(request,request.param.method.getName(),request.param.args);
	}

	boolean execMethod(LocationRequest request, String methodName, Object[] args) {
		Binder.restoreCallingIdentity(request.token);
		Util.log(request.hook, Log.WARN, "Calling from uid " + Binder.getCallingUid() + " method " +
				request.param.thisObject.getClass().getSimpleName() + "." + methodName + "( " + TextUtils.join(", ",args)+" )");
		try {
			addWhitelist(methodName, request.listener);
			Util.getMethod(request.param.thisObject.getClass(),methodName,args).invoke(request.param.thisObject,args);
		} catch (Exception e) { 
			//e.printStackTrace(); 
			Util.log(request.hook, Log.WARN, "Caught error from last exec! " + e.getCause().getMessage());
			removeWhitelist(methodName, request.listener);
			return false;
		}
		return true;
	}

	@Override
	public boolean removeUpdates(LocationRequest request) {
		Object[] args = new Object[mRemoveArgs.length];
		for (int i=0; i<args.length; i++)
			args[i] = ( mRemoveArgs[i]<0 ? request.listener : 
				( mRemoveArgs[i]>=request.param.args.length ? null : request.param.args[mRemoveArgs[i]] ) );
		return execMethod(request,mRemoveUpdates,args);
	}

	@Override
	public boolean requestUpdates(LocationRequest request) {
		return execMethod(request);
	}

	static public class LocationRequest {

		public XHook hook;
		public LocationProxy proxy;
		public LocationUpdater.Remove remove;
		public LocationUpdater.Request request;
		public XParam param;
		public Object listener;
		public Application app;
		public int uid;
		public long token;

		LocationRequest(Object hook, XParam param, Object listener, LocationProxy parent) {
			this.hook = (XHook)hook;
			this.proxy = parent;
			this.remove = (LocationUpdater.Remove)(hook instanceof LocationUpdater.Remove ? hook : parent);
			this.request = (LocationUpdater.Request)(hook instanceof LocationUpdater.Request ? hook : parent);
			this.param = param;
			this.listener = listener;
			this.app = Util.getApplication();
			this.uid = Binder.getCallingUid();
			this.token = Binder.clearCallingIdentity();
			Binder.restoreCallingIdentity(token);
		}
	};

}

class LocationUpdater {
	static public interface Remove {
		boolean removeUpdates(LocationProxy.LocationRequest request);
	};
	static public interface Request {
		boolean requestUpdates(LocationProxy.LocationRequest request);
	};
};
