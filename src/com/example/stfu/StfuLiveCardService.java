package com.example.stfu;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.LiveCard.PublishMode;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.widget.RemoteViews;
import android.util.Log;

public class StfuLiveCardService extends Service {

    private static final String LIVE_CARD_TAG = "something";
	private LiveCard liveCard;
	private RemoteViews liveCardView;
    private final Handler handler = new Handler();
	private UpdateLiveCardRunnable mUpdateLiveCardRunnable = new UpdateLiveCardRunnable();

	private static final String TAG = "StfuLiveCardService";
	public static final long DELAY_MILLIS = 2000;
	protected static final String FILE_PATH = "file_path";
	public static final String DISPLAY_GPX_ACTION = "display_gpx";
	private static final String ROUTE_INDEX = "route_index";
	private static final String PICK_DESTINATION_CARD_ACTION = "pick_card";
	private static final float PROXIMITY_ALERT_RADIUS_METERS = 50;
	private static final int PROXIMITY_ALERT_REQUEST_CODE = 1;
	private static final String PROXIMITY_ALERT_ACTION = "proximity_alert";
	private ArrayList<RoutePoint> route = null;
	private LocationManager locationManager;
	private LocationListener locationListener;
	private Location latestLocation;
	private int currentDestinationRouteIndex;
	private PendingIntent proximityAlertIntent = null;

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
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (liveCard == null) {
        	Log.i(TAG, "starting live card");
            liveCard = new LiveCard(this, LIVE_CARD_TAG);
            // Inflate a layout into a remote view
            liveCardView = new RemoteViews(getPackageName(),
                R.layout.live_card_layout);
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        	liveCardView.setTextViewText(R.id.text, "Tap for options");
            setMenuPendingIntent();
    		liveCard.setViews(liveCardView);
            liveCard.publish(PublishMode.REVEAL);
        } else if (intent.getAction().equals(DISPLAY_GPX_ACTION)) {
        	if (intent.hasExtra(FILE_PATH)) {
        		File gpxFile = new File(intent.getStringExtra(FILE_PATH));
        		route = GpxReader.getRoutePoints(gpxFile);
        		Log.i(TAG, "loaded route " + route);
        		setMenuPendingIntent();

        		// TODO: here we should setup a RouteTracker
        		setDestination(intent.getIntExtra(ROUTE_INDEX, 0));
        		addLocationListener();
        	} else {
        		Log.e(TAG, "Got a DISPLAY_GPX action with no file?");
        	}
        } else if (intent.getAction().equals(PICK_DESTINATION_CARD_ACTION)) {
    		int routeIndex = intent.getIntExtra(ROUTE_INDEX, 0);
    		setDestination(routeIndex);
        } else if (intent.getAction().equals(PROXIMITY_ALERT_ACTION)) {
        	Log.i(TAG, "Proximity alert");
        	// TODO: turn on the screen, play a sound
        }else {
        	Log.e(TAG, "Unknown action for intent: " + intent.getAction());
        }
        Log.i(TAG, "returning from onStartCommand()");
        return START_STICKY;
    }

	/**
	 * 
	 */
	private void addLocationListener() {
		locationListener = new LocationListener() {
			
			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProviderEnabled(String provider) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProviderDisabled(String provider) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onLocationChanged(Location location) {
				latestLocation = location;
				Log.i(TAG, "got new location: " + location);
			}
		};
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		List<String> providers = locationManager.getProviders(
		        criteria, true /* enabledOnly */);
		for (String provider : providers) {
			// TODO: how do we stop requesting updates?
		    locationManager.requestLocationUpdates(provider, 0, 0, locationListener);
		}
	}

	/**
	 * Display a point on the route.
	 * @param routeIndex
	 */
	private void setDestination(int routeIndex) {
    	if (route == null) {
    		Log.e(TAG, "wtf can't pick a destination without a route!");
    		return;
    	}
    	currentDestinationRouteIndex = routeIndex;
    	updateProximityAlert();
		liveCardView = new RoutePointCard(route.get(routeIndex)).getRemoteViews(
				getPackageName());
		liveCard.setViews(liveCardView);
	}

	private void updateProximityAlert() {
		RoutePoint destination = route.get(currentDestinationRouteIndex);
		if (proximityAlertIntent == null) {
			proximityAlertIntent = PendingIntent.getService(this, PROXIMITY_ALERT_REQUEST_CODE,
					new Intent(PROXIMITY_ALERT_ACTION), 0);
		}
		locationManager.addProximityAlert(
				destination.getLatitude(),
				destination.getLongitude(), PROXIMITY_ALERT_RADIUS_METERS, -1, proximityAlertIntent);
	}

	/**
     * Set up the live card's action with a pending intent
     * to show a menu when tapped.
	 */
	private void setMenuPendingIntent() {
		liveCard.setAction(PendingIntent.getActivity(
		        this, 0, 
		        MainActivity.newIntent(this, route), // pass the route
		        PendingIntent.FLAG_CANCEL_CURRENT /* clear the existing pending intent */));
	}

    /**
     * Runnable that updates live card contents
     */
    private class UpdateLiveCardRunnable implements Runnable{

        private boolean isStopped = false;


        /*
         * Updates the card with a fake score every 30 seconds as a demonstration.
         * You also probably want to display something useful in your live card.
         *
         * If you are executing a long running task to get data to update a
         * live card(e.g, making a web call), do this in another thread or
         * AsyncTask.
         */
        public void run(){
            if(!isStopped()){
            	// Get our current location
            	
            	// Determine if we're near the destination
            	
            	// Determine if we're at the destination
            	
            	// Determine if we're past the destination

                // Queue another update
                handler.postDelayed(mUpdateLiveCardRunnable, DELAY_MILLIS);
            }
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
            mUpdateLiveCardRunnable.setStop(true);
            liveCard.unpublish();
            liveCard = null;
        }
        super.onDestroy();
    }

	public static Intent newDisplayRouteIntent(Context ctx,
			String filePath, int index) {
		Intent intent = new Intent(ctx, StfuLiveCardService.class);
		intent.setAction(DISPLAY_GPX_ACTION);
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
}
