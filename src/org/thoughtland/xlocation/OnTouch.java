package org.thoughtland.xlocation;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
//import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
//import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;
//import android.widget.RelativeLayout.LayoutParams;

public class OnTouch extends IStatusCallback.Stub {

	static public final int CALLBACK_REGISTER = 0x1;
	static public final int CALLBACK_UNREGISTER = 0x2;

	static Application application = null;
	static Activity activity = null;
	static SensorManager sensors = null;
	static WindowManager wm = null;
	static NotificationManager nm = null;
	static View enabledView = null;
	static View disabledView = null;
	static View placementView = null;
	static String receiverName = null;

	static PendingIntent pendingToggleViews = null, 
			pendingMoveButton = null, 
			pendingToggleCompass = null;
	static ImageView compassView = null, thumbView = null;
	static TextView textView = null;
	static GestureDetector gesture = null;
	static ScaleGestureDetector scale = null;
	static Handler textHandler = null;

	static String appName = "Unknown App";
	static Location location = new Location(LocationManager.GPS_PROVIDER);

	static int addCount = 0;
	static float compassWidth;
	static float compassHeight;
	static float compassViewWidth;
	static float compassViewHeight;
	static final int sensorDelay = SensorManager.SENSOR_DELAY_NORMAL;
	
	static float ontouchX = 0;
	static float ontouchY = 0;
	static float ontouchWidth = 0;
	static float ontouchHeight = 0;

	static final float METER_SCALE = 0.00001f;
	static double moveScale = 1;
	static double altitude = 0;
	static boolean scaleMode = true;

	static int current_icon_position = 1;
	static int compassMsgCount = 0;
	static float lastCompass = 0;
	static double screenWidth = 0;
	static double screenHeight = 0;
	static double screenMax = 0;
	
	static boolean appSetup = false;
	static boolean viewSetup = false;
	static boolean touchEnabled = false;
	static boolean compassEnabled = false;
	static boolean appInForeground = false;
	static boolean lifecycleRegistered = false;

	static final WindowManager.LayoutParams paramse = new WindowManager.LayoutParams();
	static final WindowManager.LayoutParams paramsd = new WindowManager.LayoutParams();
	static final String self = OnTouch.class.getPackage().getName();

	static final ViewGroup.LayoutParams layoutd = 
			new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
	static final ViewGroup.LayoutParams layoute = 
			new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

	static final LightingColorFilter redColorFilter = new LightingColorFilter(0xf04040, 0);
	static final LightingColorFilter greenColorFilter = new LightingColorFilter(0x40f040, 0);
	static final LightingColorFilter blueColorFilter = new LightingColorFilter(0x4040f0, 0);

	static final int notificationId = 0xdefaced;

	static final IntentFilter intentFilter = new IntentFilter();

	static final int[] icon_positions = { 0,
		Gravity.TOP|Gravity.LEFT,
		Gravity.TOP|Gravity.RIGHT, 
		Gravity.BOTTOM|Gravity.RIGHT, 
		Gravity.BOTTOM|Gravity.LEFT };

	public OnTouch(){}

	@Override
	public void setStatus(final int status) throws RemoteException {
		Util.log(null,Log.WARN,"Calling setStatus " + status + " for uid " + Binder.getCallingUid() + "!");
		if (!appSetup) initApp();
		
		if (appSetup) {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (!viewSetup) initView();

					if (viewSetup) {
						if ((status & CALLBACK_REGISTER)!=0) {
							appInForeground = true;
							activity.registerReceiver(receiver, intentFilter);
							if (!lifecycleRegistered) {
								application.registerActivityLifecycleCallbacks(callbacks);
								lifecycleRegistered = true;
							}
							try { Looper.prepare(); }
							catch(Exception e) {}
							new Handler().post((new Runnable(){
								public void run() {
									doNotify();
									setupViews();
								}
							}));
							try {
								LocationProxyService.getClient().sendOnTouchLocation(location);
							} catch (RemoteException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}

						if ((status & CALLBACK_UNREGISTER)!=0) {
							appInForeground = false;
							unNotify();
							activity.unregisterReceiver(receiver);
							lifecycleRegistered = false;
							setupCompass(false);
							application.unregisterActivityLifecycleCallbacks(callbacks);
							try{ wm.removeView(enabledView); } catch (Exception e) {}
							try{ wm.removeView(disabledView); } catch (Exception e) {}
						}
					}
				}
			});
		}
	}


	static public void initApp() {
		application = Util.getApplication();
		activity = Util.getActivity();
		appSetup = (application != null && activity != null);
	}
	
	static public void initView() {

		if (application == null || activity == null) {
			Util.log(null, Log.ERROR, "Skipping OnTouch.initView due to null " + 
					(application == null ? "application" : "") +
					(application == null && activity == null ? " & " : "") +
					(activity == null ? "activity" : ""));
			return;
		}

		try {

			Util.log(null,Log.WARN,"Initializing OnTouch!");

			wm = ((WindowManager)activity.getSystemService(Context.WINDOW_SERVICE));
			nm = (NotificationManager)activity.getSystemService(Context.NOTIFICATION_SERVICE);
			receiverName = activity.toString() + ":touch";

			try { Looper.prepare(); }
			catch (Exception e) {}

			textHandler = new Handler();

			scale = new ScaleGestureDetector(activity, 
					new ScaleGestureDetector.SimpleOnScaleGestureListener() {

				public boolean onScale (ScaleGestureDetector detector) {
					double scaleFactor = detector.getScaleFactor();
					Util.log(null,Log.WARN,"onScale "+scaleFactor);
					if (scaleMode) {
						moveScale *= scaleFactor;
						setTextView("scale = " + String.format("%.3f",moveScale));
					} else {
						altitude += ( scaleFactor > 1 ? scaleFactor : -1/scaleFactor ) * moveScale;
						setTextView("altitude = " + String.format("%.3f",altitude));
						location.setAltitude(altitude);
					}
					return true;					
				}
			});

			pendingToggleViews = generatePendingIntent("toggleViews");
			pendingToggleCompass = generatePendingIntent("toggleCompass");
			pendingMoveButton = generatePendingIntent("moveButton");

			int windowFlags = activity.getWindow().getAttributes().flags & 
					(
							WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON | 
							WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN |
							WindowManager.LayoutParams.FLAG_FULLSCREEN |
							WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
							WindowManager.LayoutParams.FLAG_SECURE |
							WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
							WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION |
							WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS |
							WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
							);

			paramse.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
			paramse.width = WindowManager.LayoutParams.MATCH_PARENT;
			paramse.height = WindowManager.LayoutParams.MATCH_PARENT;
			paramse.format = PixelFormat.TRANSLUCENT;
			paramse.flags = WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | windowFlags;

			paramsd.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
			paramsd.width = WindowManager.LayoutParams.WRAP_CONTENT;
			paramsd.height = WindowManager.LayoutParams.WRAP_CONTENT;
			paramsd.format = PixelFormat.TRANSLUCENT;
			paramsd.flags = 
					WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | 
					WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | windowFlags;
			paramsd.gravity = icon_positions[current_icon_position]; 


			try {
				PackageManager pm = activity.getPackageManager();
				ApplicationInfo appInfo = pm.getApplicationInfo(activity.getApplicationInfo().packageName, 0);
				appName = (String)pm.getApplicationLabel(appInfo);
			} catch (Exception e) {}

			enabledView = LayoutInflater.from(activity.createPackageContext(self, 0)).inflate(R.layout.ontouch, null);
			disabledView = LayoutInflater.from(activity.createPackageContext(self, 0)).inflate(R.layout.ontouch, null);
			placementView = LayoutInflater.from(activity.createPackageContext(self, 0)).inflate(R.layout.ontouch, null);
			
			enabledView.setLayoutParams(layoute);
			//enabledView.setBackgroundColor(0x10ff0000);
			enabledView.setAlpha(.95f);
			//enabledView.setGravity(icon_positions[current_icon_position]);

			disabledView.setLayoutParams(layoutd);
			disabledView.setAlpha(.95f);
			
			placementView.setLayoutParams(layoute);
			//placementView.setBackgroundColor(0x10ff0000);
			placementView.setAlpha(.95f);
			
			placementView.setOnTouchListener(new View.OnTouchListener() {				
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					ontouchX = event.getX();
					ontouchY = event.getY();
					updateButton();
					return false;
				}
			});

			//((RelativeLayout)disabledView.findViewById(R.id.onTouchIconLayout)).setLayoutParams(layoutd);

			compassView = (ImageView)enabledView.findViewById(R.id.onTouchCompass);			
			compassView.setScaleType(ScaleType.MATRIX);
			compassWidth = compassView.getDrawable().getIntrinsicWidth();
			compassHeight = compassView.getDrawable().getIntrinsicHeight();
			compassViewWidth = compassView.getLayoutParams().width - compassView.getPaddingLeft() - compassView.getPaddingRight();
			compassViewHeight = compassView.getLayoutParams().height - compassView.getPaddingTop() - compassView.getPaddingBottom();
			
			ontouchWidth = compassView.getLayoutParams().width;
			ontouchHeight = compassView.getLayoutParams().height;

			paramsd.width = (int) ontouchWidth;
			paramsd.height = (int) ontouchHeight;

			thumbView = (ImageView)enabledView.findViewById(R.id.onTouchThumb);	
			
			textView = (TextView)enabledView.findViewById(R.id.onTouchText);
			//textView = (TextView)enabledView.findViewById(R.id.onTouchText);	

			TextView placementText = (TextView)placementView.findViewById(R.id.onTouchText);
			placementText.setVisibility(View.VISIBLE);
			placementText.setText("Touch screen to place icon");
			placementView.findViewById(R.id.onTouchIconLayout).setVisibility(View.GONE);
			//textView = (TextView);
			//ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(-1, -1);
			
			//placementView.addView(textViewLayout);
			
			gesture = new GestureDetector(activity,gestureListener);
			gesture.setOnDoubleTapListener(gestureListener);

			enabledView.setOnTouchListener(new View.OnTouchListener() {		
				@SuppressLint("ClickableViewAccessibility")
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					//Util.log(null, Log.WARN, "Dealing with motion event " + event);
					gesture.onTouchEvent(event);
					if (event.getPointerCount()==2)
						scale.onTouchEvent(event);
					return false;
				}
			});


			View.OnClickListener toggleClick = 
					new View.OnClickListener() {		
				@Override
				public void onClick(View v) {
					Util.log(null, Log.WARN, "in icon onclick receiver");
					toggleViews();
				}
			};
			enabledView.findViewById(R.id.onTouchIconView).setOnClickListener(toggleClick);
			disabledView.findViewById(R.id.onTouchIconView).setOnClickListener(toggleClick);
			disabledView.findViewById(R.id.onTouchTextLayout).setVisibility(View.GONE);

			sensors = (SensorManager)activity.getSystemService(Context.SENSOR_SERVICE);

			location = PrivacyManager.getDefacedLocation(Binder.getCallingUid(), location);

			if(Build.VERSION.SDK_INT>12){
				Point size = new Point();
				wm.getDefaultDisplay().getSize(size);
				screenWidth = size.x;
				screenHeight = size.y;
			}
			else{
				screenWidth = wm.getDefaultDisplay().getWidth();  // Deprecated
				screenHeight = wm.getDefaultDisplay().getHeight();  // Deprecated
			}
			
			screenMax = (screenHeight>screenWidth ? screenHeight : screenWidth);

			viewSetup = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
	};

	static void removeViews() {
		try{ wm.removeViewImmediate(enabledView); } catch (Exception e) {}
		try{ wm.removeViewImmediate(disabledView); } catch (Exception e) {}		
		try{ wm.removeViewImmediate(placementView); } catch (Exception e) {}
	}
	
	static void setupViews() {
		removeViews();
		try {
			if (touchEnabled)
				wm.addView(enabledView, paramse);
			else
				wm.addView(disabledView, paramsd);
			setupCompass();
			setupThumb(); 
		} catch (Exception e) {
			e.printStackTrace();
			unNotify();
		}
	}

	static void toggleViews() {
		touchEnabled = !touchEnabled;
		try {
			if (touchEnabled) 
				wm.removeView(disabledView);
			else 
				wm.removeView(enabledView);
		} catch (Exception e) { e.printStackTrace(); }
		doNotify();
		setupViews();
	}

	static void doNotify() {
		if (appInForeground) {
			Builder noti = makeNotification();
			nm.notify(notificationId, noti.build());
		} else 
			Util.log(null, Log.WARN, "Called doNotify without appPresent");
	}

	static void unNotify() {
		nm.cancel(notificationId);
		setupCompass(false);
	}

	static void setTextView(String s) {
		try { 
			textView.setText(s); 
			textView.setVisibility(View.VISIBLE);
			textHandler.removeCallbacks(textViewHider);
			textHandler.postDelayed(textViewHider, 3000);
		}
		catch (Exception e) { e.printStackTrace(); }
	}

	static void rotateCompass(double deg, boolean color) {
		if (compassMsgCount++%50==0)
			Util.log(null, Log.WARN, "rotateCompass " + deg);
		lastCompass = (float)deg;
		Matrix matrix=new Matrix();
		matrix.postRotate(lastCompass, compassWidth/2, compassHeight/2);
		matrix.postScale(compassViewWidth/compassWidth, compassViewHeight/compassHeight);
		compassView.setImageMatrix(matrix);
		if (color)
			compassView.setColorFilter(greenColorFilter);
		else
			compassView.clearColorFilter();
		compassView.invalidate();
	}

	static void setupThumb() {
		if (touchEnabled) thumbView.setColorFilter(blueColorFilter);
		else thumbView.clearColorFilter();
	}

	static void setupCompass() {
		setupCompass(true);
	}

	static void setupCompass(boolean doRegister) {
		if (doRegister && compassEnabled && touchEnabled && appInForeground) {
			sensors.registerListener(sensorListener, sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), sensorDelay);
			sensors.registerListener(sensorListener, sensors.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), sensorDelay);
			Util.log(null, Log.WARN, "Registered sensor listeners");
		} else {
			sensors.unregisterListener(sensorListener);
			rotateCompass(0,false);
		}		
	}

	static void toggleCompass() {
		Util.log(null,Log.WARN,"In toggle compass!");
		compassEnabled = !compassEnabled;
		setupCompass();
		doNotify();
	}

	static void moveButton() {
		//current_icon_position = (current_icon_position>=icon_positions.length-1 ? 0 : current_icon_position+1);
		//int gravity = icon_positions[current_icon_position];
		current_icon_position = current_icon_position > 0 ? 0 : 1;
		try {
			//if (gravity == 0) {
			if (current_icon_position == 0) {
				enabledView.findViewById(R.id.onTouchIconLayout).setVisibility(View.GONE);
				disabledView.setVisibility(View.GONE);
				setupViews();
				//wm.updateViewLayout((touchEnabled ? enabledView : disabledView), (touchEnabled ? paramse : paramsd));
			} else {
				//enabledView.findViewById(R.id.onTouchIconLayout).setVisibility(View.VISIBLE);
				//disabledView.setVisibility(View.VISIBLE);
				//RelativeLayout icon = ((RelativeLayout)enabledView.findViewById(R.id.onTouchIconLayout));
				//icon.setX(ontouchX);
				//icon.setY(ontouchY);
				removeViews();
				wm.addView(placementView, paramse);
				//setTextView("Touch to place button");
				//icon.setGravity(gravity);
				//paramsd.gravity = gravity;
			}
		} catch (Exception e) { e.printStackTrace(); }
		doNotify();
	}

	@SuppressWarnings("deprecation")
	static void updateButton() {
		Util.log(null, Log.WARN, "Setting icon position to " + ontouchX + ", " + ontouchY);
		enabledView.findViewById(R.id.onTouchIconLayout).setVisibility(View.VISIBLE);
		disabledView.setVisibility(View.VISIBLE);
		AbsoluteLayout.LayoutParams layout = new AbsoluteLayout.LayoutParams((int)ontouchWidth, (int)ontouchHeight, 
				(int)(ontouchX - ontouchWidth/2), (int)(ontouchY - ontouchHeight/2));
		enabledView.findViewById(R.id.onTouchIconLayout).setLayoutParams(layout);
		paramsd.x = layout.x;
		paramsd.y = layout.y;
		setupViews();
	}
	
	static PendingIntent generatePendingIntent(String id) {
		String action = receiverName + ":" + id;
		intentFilter.addAction(action);
		Intent intentToggle = new Intent(action);
		intentToggle.putExtra(id,true);
		intentToggle.setFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
		return PendingIntent.getBroadcast(activity, 0, intentToggle, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	static Builder makeNotification() {	
		Builder noti = new NotificationCompat.Builder(activity);
		noti.setContentTitle(appName);
		noti.setContentIntent(pendingToggleViews);
		noti.setSmallIcon(touchEnabled ? android.R.drawable.ic_menu_compass : android.R.drawable.ic_menu_mylocation);
		noti.addAction(android.R.color.transparent, 
				(current_icon_position==0 ? "Show" : current_icon_position==icon_positions.length-1 ? "Hide" : "Move") + 
				" Button", pendingMoveButton);
		noti.addAction(android.R.color.transparent, "Compass " + (compassEnabled ? "off" : "on"), pendingToggleCompass);
		noti.setOngoing(true);
		noti.setContentText( "Touch location "+(touchEnabled ? "enabled" : "disabled"));
		return noti;
	}

	static final Runnable textViewHider = new Runnable() {
		@Override
		public void run() {
			try { textView.setVisibility(View.GONE); }
			catch (Exception e) { e.printStackTrace(); }
		}
	};

	static final Application.ActivityLifecycleCallbacks callbacks = new Application.ActivityLifecycleCallbacks() {
		@Override
		public void onActivityStopped(Activity activity) {
			Util.log(null, Log.WARN, "Activity " + appName + " Stopped");
			appInForeground = false;
			//unNotify();
		}

		@Override
		public void onActivityStarted(Activity activity) {
			Util.log(null, Log.WARN, "Activity " + appName + " Sarted");
			appInForeground = true;
			//doNotify();
		}

		@Override
		public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
			Util.log(null, Log.WARN, "Activity " + appName + " Save Instance State");
			//unNotify();
		}

		@Override
		public void onActivityResumed(Activity activity) {
			Util.log(null, Log.WARN, "Activity " + appName + " Resumed");
			(new Handler()).postDelayed(new Runnable() {
				@Override
				public void run() {
					setupViews();
				}				
			},100);
			try {
				LocationProxyService.getClient().onProviderEnabled(LocationManager.GPS_PROVIDER);
				LocationProxyService.getClient().onGpsStatusChanged(GpsStatus.GPS_EVENT_STARTED);
			} catch (RemoteException e) {
				e.printStackTrace();
			}

			appInForeground = true;
			doNotify();
		}

		@Override
		public void onActivityPaused(Activity activity) {
			Util.log(null, Log.WARN, "Activity " + appName + " Paused");
			try {
				LocationProxyService.getClient().onProviderDisabled(LocationManager.GPS_PROVIDER);
				LocationProxyService.getClient().onGpsStatusChanged(GpsStatus.GPS_EVENT_STOPPED);
			} catch (RemoteException e) {
				e.printStackTrace();
			}

			appInForeground = false;
			unNotify();
		}

		@Override
		public void onActivityDestroyed(Activity activity) {
			Util.log(null, Log.WARN, "Activity " + appName + " Destroyed");
			appInForeground = false;
			//unNotify();
		}

		@Override
		public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
			Util.log(null, Log.WARN, "Activity " + appName + " Created");
			appInForeground = true;
			//doNotify();
		}
	};

	static final BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Util.log(null, Log.WARN, "in broadcast receiver with intent " + intent.getAction() + 
					" extras [ " + TextUtils.join(",  ",intent.getExtras().keySet()) + " ]");
			Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
			context.sendBroadcast(it);
			for (final String key : intent.getExtras().keySet()) {
				Util.log(null, Log.WARN, "executing method " + key);
				try { OnTouch.class.getDeclaredMethod(key).invoke(null); }
				catch (Exception e) { e.printStackTrace(); }
			}
		}    
	};

	static final SensorEventListener sensorListener = new SensorEventListener() {
		final float[] R = new float[16];
		final float[] I = new float[16];
		final float[] orientVals = new float[3];
		final float[] gravity = new float[3];
		final float[] geomag = new float[3];

		final float ALPHA = 0.5f;

		void lowPass( float[] input, float[] output ) {
			if ( input != null && output != null )
				for ( int i=0; i<input.length; i++ )
					output[i] = output[i] + ALPHA * (input[i] - output[i]);
		}

		@Override
		public void onSensorChanged(SensorEvent sensorEvent) {
			if (sensorEvent.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE)
				return;
			int eventType = sensorEvent.sensor.getType();
			lowPass( sensorEvent.values, 
					(eventType == Sensor.TYPE_ACCELEROMETER ? gravity : 
						eventType == Sensor.TYPE_MAGNETIC_FIELD ? geomag : null));
			boolean success = SensorManager.getRotationMatrix(R, I, gravity, geomag);
			if (success) {
				SensorManager.getOrientation(R, orientVals);
				rotateCompass(-Math.toDegrees(orientVals[0]),true);
			}
		}

		@Override
		public void onAccuracyChanged(Sensor arg0, int arg1) {}
	};

	static final GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
		int lastDoubleTapAction = 0;

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY){
			Util.log(null,Log.WARN,"onFling(" + e1 + "," + e2 + "," + velocityX + "," + velocityY + ")");
			LocationHandler.moveLocation(e1,e2);
			return true;					
		}

		@Override
		public void onLongPress (MotionEvent e) {
			Util.log(null,Log.WARN,"onLongPress("+e+")");
			scaleMode = !scaleMode;
			setTextView( "zoom mode = " + (scaleMode ? "scale" : "altitude"));
		}

		@Override
		public boolean onScroll (MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			if (e2.getPointerCount()<3) return false;
			Util.log(null,Log.WARN,"onScroll(" + e1 + "," + e2 + "," + distanceX + "," + distanceY + ")");
			LocationHandler.moveLocation(e1, e2);
			return true;
		}

		@Override
		public boolean onDoubleTapEvent (MotionEvent e) {
			Util.log(null,Log.WARN,"onDoubleTap("+e+")");
			int thisAction = e.getAction();
			if (MotionEvent.ACTION_UP == thisAction && MotionEvent.ACTION_DOWN == lastDoubleTapAction) {
				toggleCompass();
				setTextView("compass " + (compassEnabled ? "enabled" : "disabled"));
			}			
			if (MotionEvent.ACTION_UP == thisAction) {
				gesture.setIsLongpressEnabled(true);
			} else if (MotionEvent.ACTION_MOVE == thisAction) {
				LocationHandler.moveLocation(e);
			} else if (MotionEvent.ACTION_DOWN == thisAction) {
				gesture.setIsLongpressEnabled(false);
				LocationHandler.prepareLocation(e);				
			}
			lastDoubleTapAction = thisAction;
			return true;
		}
	};


	static class LocationHandler {

		static float lastX = 0, lastY = 0;
		static long eventTime = 0;

		static void moveLocation(MotionEvent e) {
			long newEventTime = e.getEventTime();
			double X = e.getX();
			double Y = e.getY();
			double dX = X - lastX;
			double dY = lastY - Y;
			double dT = (newEventTime - eventTime)/1.0e3;
			//Util.log(null, Log.WARN, "in MoveLocation with dx " + dX + ", dy " + dY + ", dT " + dT);
			if (compassEnabled) {
				Matrix M = new Matrix();
				M.postRotate(lastCompass);
				float[] v = new float[]{(float)dX,(float)dY};
				M.mapVectors(v);
				dX = v[0]; dY = v[1];
				//Util.log(null, Log.WARN, "in MoveLocation with rotated dx " + dX + ", dy " + dY);
			}

			//double dLat = (dY/screenMax)*METER_SCALE*moveScale;
			//double dLon = (dX/screenMax)*METER_SCALE*moveScale;

			//Util.log(null, Log.WARN, "in MoveLocation with dx " + dX + ", dy " + dY + ", dT " + dT + ", dLat " + dLat + ", dLon " + dLon);
			
			double lat1 = location.getLatitude();
			double lon1 = location.getLongitude();
			//double lat2 = lat1 + dLat;
			//double lon2 = lon1 + dLon;

			double lat1r = Math.toRadians(lat1);
			double lon1r = Math.toRadians(lon1);
			
			double brng = Math.atan2(dX, dY);
			double d = Math.sqrt((dX*dX)+(dY*dY))*(moveScale/screenMax)*25;
			
			double R = 6371000;
			double lat2r = Math.asin(Math.sin(lat1r)*Math.cos(d/R) + 
					Math.cos(lat1r)*Math.sin(d/R)*Math.cos(brng));
			double lon2r = lon1r + Math.atan2(Math.sin(brng)*Math.sin(d/R)*Math.cos(lat1r), 
					Math.cos(d/R)-Math.sin(lat1r)*Math.sin(lat2r));
			
			double lat2 = Math.toDegrees(lat2r);
			double lon2 = Math.toDegrees(lon2r);
			
			double dLat = lat2 - lat1;
			double dLon = lon2 - lon1;
			
			// Wrap coordinates
			double pLat = lat2>0 ? 90 : -90;
			double pLon = lon2>0 ? 180 : -180;
			if (Math.abs(lon2)>180) {
				lon2 = -2*pLon + lon2;
			}
			if (Math.abs(lat2)>90) {
				lon2 = -pLon + lon2;
				lat2 = 2*pLat - lat2;
			}

			Location newLocation = new Location(LocationManager.GPS_PROVIDER);
			newLocation.setLatitude(lat2);
			newLocation.setLongitude(lon2);

			double dist = location.distanceTo(newLocation);
			double speedMs = dist/dT;
			double speedMph = speedMs * 2.23694;
			double speedKmh = speedMs * 3.6;
			if (speedMs!=Double.NaN && speedMs>0)
				newLocation.setSpeed((float)speedMs);

			newLocation.setBearing(location.bearingTo(newLocation));
			newLocation.setAltitude(altitude);
			newLocation.setAccuracy(1);
			newLocation.setTime(System.currentTimeMillis());
			newLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());

			location = newLocation;
			eventTime = e.getEventTime();
			prepareLocation(e);

			Util.log(null, Log.WARN, "in MoveLocation with bearing " + brng + "/" + Math.toDegrees(brng) + " and distance " + d + " dLat " + dLat/METER_SCALE + " dLon " + dLon/METER_SCALE );
			Util.log(null, Log.WARN, "Location now @" + speedMph + "mph "+ location);

			try {
				LocationProxyService.getClient().sendOnTouchLocation(newLocation);
			} catch (RemoteException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			setTextView("bearing: " + (int)location.getBearing() + 
					(speedMph>=0.1 ? " @ " + String.format("%.1f",speedMph) + "mph" : ""));
		}

		static void moveLocation(MotionEvent e1, MotionEvent e2) {
			prepareLocation(e1);
			moveLocation(e2);
		}

		static void prepareLocation(MotionEvent e) {
			lastX = e.getX();
			lastY = e.getY();
		}
	}

};

