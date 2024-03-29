package com.github.rekab.tcxdirections;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.text.format.Time;
import android.util.Log;
import android.view.WindowManager;
import android.widget.RemoteViews;

import com.github.rekab.tcxdirections.R;
import com.github.rekab.tcxdirections.model.CoursePoint;
import com.github.rekab.tcxdirections.model.TcxReader;
import com.github.rekab.tcxdirections.model.TcxRoute;
import com.google.android.glass.media.Sounds;
import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.LiveCard.PublishMode;

public class StfuLiveCardService extends Service {

    private static final String LIVE_CARD_TAG = "something";
	private LiveCard liveCard;
	private RemoteViews liveCardView;
    private final Handler handler = new Handler();
	private UpdateLiveCardRunnable updateRunnable = new UpdateLiveCardRunnable();

	private static final String TAG = "StfuLiveCardService";
	public static final long DELAY_MILLIS = 2000;
	protected static final String FILE_PATH = "file_path";
	public static final String DISPLAY_ROUTE_FILE_ACTION = "display_file";
	private static final String ROUTE_INDEX = "route_index";
	private static final String PICK_DESTINATION_CARD_ACTION = "pick_card";
	private static final String PROXIMITY_ALERT_ACTION = "proximity_alert";
	private static final String STOP_NAV_ACTION = "stop_nav";
	private static final float APPROACHING_DEST_ALERT_RADIUS_METERS = 100;
	private static final float ARRIVED_AT_DEST_ALERT_RADIUS_METERS = 40;
	private LocationManager locationManager;
	private LocationListener locationListener;
	private Location latestLocation;
	private Time t;
	private long lastLocationUpdate = 0;
	private int currentDestinationRouteIndex;
	//private ArrayList<CoursePoint> route = null;
	private PendingIntent proximityAlert = null;
	protected static final boolean FAKE_LOCATION_UPDATES = false; // For testing
	// Test route: http://ridewithgps.com/routes/4783905
	// Test track: http://ridewithgps.com/trips/2701861
	private static final String TEST_FILE_LOCATION = Filesystem.getStorageDirectory() + "/cts-8-gpx-track.gpx";
	// Test route: http://ridewithgps.com/routes/4051817
	// Test track: http://ridewithgps.com/trips/2550337
	//private static final String TEST_FILE_LOCATION = Filesystem.getStorageDirectory() + "/2550337.gpx";
	private static final long SCREEN_TIMEOUT_MS = 15 * 1000;
	private PowerManager pm;
	private PowerManager.WakeLock wl;
	private AudioManager audio;
	private TcxRoute tcxRoute;

	@Override
	public IBinder onBind(Intent intent) {
	  /*
	   *  If you need to set up interprocess communication
	   * (activity to a service, for instance), return a binder object
	   * so that the client can receive and modify data in this service.
	   *
	   * A typical use is to give a menu activity access to a binder object
	   * if it is trying to change a setting that is managed by the live card
	   * service. The menu activity in this sample does not require any
	   * of these capabilities, so this just returns null.
	   */
		return null;
	}
	
    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(
				PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
				PowerManager.ACQUIRE_CAUSES_WAKEUP |
				PowerManager.ON_AFTER_RELEASE,
				TAG);
		audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		t = new Time();

        if (locationListener == null) {
			locationListener = new LocationListener() {

				@Override
				public void onStatusChanged(String provider, int status, Bundle extras) {
				}

				@Override
				public void onProviderEnabled(String provider) {
				}

				@Override
				public void onProviderDisabled(String provider) {
				}

				@Override
				public void onLocationChanged(Location location) {
					latestLocation = location;
				    t.setToNow();
					long curTime = t.toMillis(true);
					if ((curTime - lastLocationUpdate) < (1 * 1000)) {
						Log.i(TAG, "skipping off course check");
						// TODO: remove this hack
						return;
					}
					lastLocationUpdate = curTime;

					Log.i(TAG, "got new location: " + location);
					if (tcxRoute == null) {
						// TODO: to save power, should only request location updates when a route
						// is loaded.
						Log.i(TAG, "no route loaded");
						return;
					}
					// Check if we're still on the route, if not, try to find
					if (tcxRoute.isOnCourse(location, currentDestinationRouteIndex)) {
						Log.i(TAG, "on course, index=" + currentDestinationRouteIndex + "=" + tcxRoute.getCoursePoints().get(currentDestinationRouteIndex));
					} else {
						Log.w(TAG, "off course, updating destination from index=" + currentDestinationRouteIndex);
						int newDestination = tcxRoute.getNextCoursePointIndex(
					    		location, currentDestinationRouteIndex);
						if (newDestination != currentDestinationRouteIndex) {
							currentDestinationRouteIndex = newDestination;
							Log.i(TAG, "updated index=" + currentDestinationRouteIndex);
							setDestination(currentDestinationRouteIndex);
						}
					}

					// Because Glass doesn't have an emulator, and because proximity
					// alerts can't specify a test provider, we have to fake it here.
					if (FAKE_LOCATION_UPDATES && currentDestinationRouteIndex < tcxRoute.getCoursePoints().size()) {
						Location currentDest = tcxRoute.getCoursePoints().get(currentDestinationRouteIndex);
						if (latestLocation.distanceTo(currentDest) < APPROACHING_DEST_ALERT_RADIUS_METERS) {
							handleProximityAlert();
						}
					}
				}
			};
        }
		if (FAKE_LOCATION_UPDATES) {
			/*locationManager.addTestProvider(TEST_PROVIDER_NAME, false, false, false, false,
					true, true, true, Criteria.POWER_LOW, Criteria.ACCURACY_FINE);
			locationManager.setTestProviderEnabled(TEST_PROVIDER_NAME, true);
		    locationManager.requestLocationUpdates(TEST_PROVIDER_NAME, 0, 0, locationListener);*/
		    File source = new File(TEST_FILE_LOCATION);
		    Log.i(TAG, "Loading " + source.getAbsolutePath());
			try {
				updateRunnable.setTestRoute(GpxReader.getTrackPoints(source));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {

		}
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (liveCard == null) {
        	Log.i(TAG, "starting live card");
            liveCard = new LiveCard(this, LIVE_CARD_TAG);
            // Inflate a layout into a remote view
            liveCardView = new RemoteViews(getPackageName(),
                R.layout.live_card_layout);
        	liveCardView.setTextViewText(R.id.text, "Tap for options");
            setMenuPendingIntent();
    		liveCard.setViews(liveCardView);
            liveCard.publish(PublishMode.REVEAL);
        } else if (intent.getAction().equals(DISPLAY_ROUTE_FILE_ACTION)) {
        	if (intent.hasExtra(FILE_PATH)) {
            	liveCardView.setTextViewText(R.id.text, "Loading route...");
        		liveCard.setViews(liveCardView);

        		File tcxFile = new File(intent.getStringExtra(FILE_PATH));
        		tcxRoute = TcxReader.getTcxRoute(tcxFile);
        		//route = GpxReader.getRoutePoints(gpxFile);
        		//if (route.size() == 0) {
        		if (tcxRoute == null || tcxRoute.getCoursePoints().size() == 0) {
        			Log.e(TAG, "Route is empty!");
        			// TODO: display an error
        		} else {
	        		Log.i(TAG, "loaded route " + tcxRoute);

	        		if (FAKE_LOCATION_UPDATES) {
	        			// Kick off the fake updates
		        		handler.postDelayed(updateRunnable, DELAY_MILLIS);
	        		} else {
	        			Log.i(TAG, "Requesting location updates");
		    			Criteria criteria = new Criteria();
		    			criteria.setAccuracy(Criteria.ACCURACY_FINE);
		    			List<String> providers = locationManager.getProviders(
		    			        criteria, true /* enabledOnly */);
		    			for (String provider : providers) {
		    				// TODO: how do we stop requesting updates?
		    			    locationManager.requestLocationUpdates(provider, 0, 0, locationListener);
		    			}
	        		}
	        		// Update the menu pending intent to reflect that we've got a route.
	        		setMenuPendingIntent();
	
	        		setDestination(intent.getIntExtra(ROUTE_INDEX, 0));
        		}
        		

        	} else {
        		Log.e(TAG, "Got a DISPLAY_ROUTE_FILE_ACTION action with no file?");
        	}
        } else if (intent.getAction().equals(PICK_DESTINATION_CARD_ACTION)) {
    		int routeIndex = intent.getIntExtra(ROUTE_INDEX, 0);
    		setDestination(routeIndex);
        } else if (intent.getAction().equals(PROXIMITY_ALERT_ACTION)) {
        	handleProximityAlert();
        } else if (intent.getAction().equals(STOP_NAV_ACTION)) {
        	Log.i(TAG, "Got intent to stop navigation");
        	updateRunnable.setStop(true);
    		removeProximityAlertForCurrentDestination();
    		locationManager.removeUpdates(locationListener);
        } else {
        	Log.e(TAG, "Unknown action for intent: " + intent.getAction());
        }
        Log.i(TAG, "returning from onStartCommand()");
        return START_STICKY;
    }

	private void handleProximityAlert() {
		Log.i(TAG, "Proximity alert");
		// Turn the screen on.
		wl.acquire(SCREEN_TIMEOUT_MS);  // TODO: estimate time until arrival
		
		// Play a sound
		audio.playSoundEffect(Sounds.SUCCESS);

		// Determine if we're approaching or have arrived at the destination
		CoursePoint dest = tcxRoute.getCoursePoints().get(currentDestinationRouteIndex);
		if (haveArrivedAt(dest)) {
			handleArrivedAtDestination();
		} else {
			setupArrivalProximityAlert();
		}
	}

	private void handleArrivedAtDestination() {
		Log.i(TAG, "have arrived at current destination");
		int nextDest = currentDestinationRouteIndex + 1;
		if (nextDest < tcxRoute.getCoursePoints().size()) {
			Log.i(TAG, "advancing destination");
			setDestination(nextDest);
		} else {
			Log.i(TAG, "Arrived at finish!");
			// TODO: play a sound, shutdown or something
		}

		// Let the screen turn back off.
		wl.release();
	}

	private void setupArrivalProximityAlert() {
		Log.i(TAG, "setting arrival proximity alert");
		removeProximityAlertForCurrentDestination();
    	setProximityAlertForCurrentDestination(ARRIVED_AT_DEST_ALERT_RADIUS_METERS);
	}

	private boolean haveArrivedAt(CoursePoint dest) {
		return latestLocation != null &&
				latestLocation.distanceTo(dest) <= ARRIVED_AT_DEST_ALERT_RADIUS_METERS;
	}

	/**
	 * Display a point on the route.
	 * @param routeIndex
	 */
	private void setDestination(int routeIndex) {
		Log.i(TAG, "Setting destination to index=" + routeIndex);
    	if (tcxRoute == null) {
    		Log.e(TAG, "wtf can't pick a destination without a route!");
    		return;
    	}
    	// Clear the alert for the previous point (we might already be at it)
    	if (currentDestinationRouteIndex != routeIndex && proximityAlert != null) {
    		removeProximityAlertForCurrentDestination();
    	}
    	currentDestinationRouteIndex = routeIndex;
    	setProximityAlertForCurrentDestination(APPROACHING_DEST_ALERT_RADIUS_METERS);

    	liveCardView = new RoutePointCard(
    			tcxRoute.getCoursePoints().get(routeIndex)).getRemoteViews(getPackageName());
		liveCard.setViews(liveCardView);
		wl.acquire(SCREEN_TIMEOUT_MS);
	}

	private void setProximityAlertForCurrentDestination(float radiusMeters) {
		Log.i(TAG, "setting proximity alert for current dest at " + radiusMeters + "m");
		CoursePoint destination = tcxRoute.getCoursePoints().get(currentDestinationRouteIndex);
		proximityAlert = PendingIntent.getService(this, 0, new Intent(PROXIMITY_ALERT_ACTION), 0);
    	locationManager.addProximityAlert(
    			destination.getLatitude(),
    			destination.getLongitude(), radiusMeters, -1, proximityAlert);
	}

	private void removeProximityAlertForCurrentDestination() {
		if (proximityAlert == null) {
			Log.e(TAG, "proximity alert is already null!?");
			return;
		}
		Log.i(TAG, "removing pending intent proximity alert");
		locationManager.removeProximityAlert(proximityAlert);
	}

	/**
     * Set up the live card's action with a pending intent
     * to show a menu when tapped.
	 */
	private void setMenuPendingIntent() {
		liveCard.setAction(PendingIntent.getActivity(
		        this, 0, 
		        MainActivity.newIntent(this, tcxRoute != null ? tcxRoute.getCoursePoints() : null), // pass the route
		        PendingIntent.FLAG_CANCEL_CURRENT /* clear the existing pending intent */));
	}

    /**
     * Runnable that updates live card contents
     */
    private class UpdateLiveCardRunnable implements Runnable {

        private boolean isStopped = false;
        private Queue<CoursePoint> testRoutePoints = null;


        /*
         * Updates the card with a fake score every 30 seconds as a demonstration.
         * You also probably want to display something useful in your live card.
         *
         * If you are executing a long running task to get data to update a
         * live card(e.g, making a web call), do this in another thread or
         * AsyncTask.
         */
        public void run(){
            if (!isStopped()){
            	if (FAKE_LOCATION_UPDATES &&
            			testRoutePoints != null &&
            			testRoutePoints.size() > 0) {
            		Location location = testRoutePoints.remove();
            		Log.i(TAG, "setting test location " + location);
                    //locationManager.setTestProviderLocation(TEST_PROVIDER_NAME, location);
            		locationListener.onLocationChanged(location);
            	}

                // Queue another update
                handler.postDelayed(updateRunnable, DELAY_MILLIS);
            }
        }

        public void setTestRoute(ArrayList<CoursePoint> routePoints) {
        	Log.i(TAG, "adding test route with size=" + routePoints.size());
			testRoutePoints = new LinkedList<CoursePoint>();
			testRoutePoints.addAll(routePoints);
		}

		public boolean isStopped() {
            return isStopped;
        }

        public void setStop(boolean isStopped) {
            this.isStopped = isStopped;
        }
    }
    
    @Override
    public void onDestroy() {
        if (liveCard != null && liveCard.isPublished()) {
        	//Stop the handler from queuing more Runnable jobs
            updateRunnable.setStop(true);
            liveCard.unpublish();
            liveCard = null;
        }
        super.onDestroy();
    }

	public static Intent newDisplayRouteIntent(Context ctx,
			String filePath, int index) {
		Intent intent = new Intent(ctx, StfuLiveCardService.class);
		intent.setAction(DISPLAY_ROUTE_FILE_ACTION);
		intent.putExtra(FILE_PATH, filePath);
		intent.putExtra(ROUTE_INDEX, index);
		return intent;
	}

	/**
	 * Create an intent to display a point within the loaded route.
	 * 
	 * @param ctx
	 * @param index
	 * @return
	 */
	public static Intent newDisplayDestinationIntent(Context ctx,
			int index) {
		Intent intent = new Intent(ctx, StfuLiveCardService.class);
		intent.setAction(PICK_DESTINATION_CARD_ACTION);
		intent.putExtra(ROUTE_INDEX, index);
		return intent;
	}

	/**
	 * Create an intent to stop navigation.
	 * @param ctx
	 * @return
	 */
	public static Intent newStopNavIntent(Context ctx) {
		Intent intent = new Intent(ctx, StfuLiveCardService.class);
		intent.setAction(STOP_NAV_ACTION);
		return intent;
	}
}
