package org.thoughtland.xlocation;

import java.util.ArrayList;
import java.util.List;

import android.os.Binder;
import android.os.StrictMode;
import android.util.Log;

public class XStrictMode extends XHook {
	private Methods mMethod;

	private XStrictMode(Methods method, String restrictionName) {
		super(restrictionName, method.name(), null);
		mMethod = method;
	}

	public String getClassName() {
		return "android.os.StrictMode";
	}

	// boolean enableMyLocation()
	// void disableMyLocation()
	// https://developers.google.com/maps/documentation/android/v1/reference/index

	private enum Methods {
		setThreadPolicy, setVmPolicy
	};

	public static List<XHook> getInstances() {
		List<XHook> listHook = new ArrayList<XHook>();
		listHook.add(new XStrictMode(Methods.setThreadPolicy, null));
		listHook.add(new XStrictMode(Methods.setVmPolicy, null));
		return listHook;
	}

	@Override
	protected void before(XParam param) throws Throwable {
		//Util.log(this, Log.WARN, "hooked before " + getClassName() + "." + mMethod.toString() + " for uid " + Binder.getCallingUid());
		switch(mMethod) {
		case setThreadPolicy:
			param.args[0] = StrictMode.ThreadPolicy.LAX;
			break;
		case setVmPolicy:
			param.args[0] = StrictMode.VmPolicy.LAX;
			break;
		default:
			break;
		
		}
		//param.setResult(null);
	}

	@Override
	protected void after(XParam param) throws Throwable {
		// Do nothing
	}
}
