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
import android.os.Handler;
import android.os.IBinder;
import android.widget.RemoteViews;
import android.util.Log;

public class StfuLiveCardService extends Service {

    private static final String LIVE_CARD_TAG = "something";
	private LiveCard liveCard;
	private RemoteViews liveCardView;
	private Random mRandom;
    private final Handler mHandler = new Handler();
	private UpdateLiveCardRunnable mUpdateLiveCardRunnable = new UpdateLiveCardRunnable();

	private static final String TAG = "StfuLiveCardService";
	public static final long DELAY_MILLIS = 3000;
	protected static final String FILE_PATH = "file_path";
	public static final String DISPLAY_GPX = "display_gpx";
	private static final String ROUTE_INDEX = "route_index";
	private static final String PICK_DESTINATION_CARD = "pick_card";
	private ArrayList<RoutePoint> route = null;

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
        mRandom = new Random();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (liveCard == null) {
        	Log.d(TAG, "starting live card");
            liveCard = new LiveCard(this, LIVE_CARD_TAG);
            // Inflate a layout into a remote view
            liveCardView = new RemoteViews(getPackageName(),
                R.layout.live_card_layout);
    		liveCardView.setTextViewText(R.id.text, "Tap for options");
    		liveCard.setViews(liveCardView);
            setMenuPendingIntent();
            
            liveCard.publish(PublishMode.REVEAL);
            //mHandler.post(mUpdateLiveCardRunnable);
        } else if (intent.getAction().equals(DISPLAY_GPX)) {
        	if (intent.hasExtra(FILE_PATH)) {
        		File gpxFile = new File(intent.getStringExtra(FILE_PATH));
        		route = GpxReader.getRoutePoints(gpxFile);
        		Log.i(TAG, "loaded route " + route);
        		setMenuPendingIntent();
        		int routeIndex = intent.getIntExtra(ROUTE_INDEX, 0);
        		// TODO: here we should setup a RouteTracker
        		setDestination(routeIndex);
        	} else {
        		Log.e(TAG, "Got a DISPLAY_GPX action with no file?");
        	}
        } else if (intent.getAction().equals(PICK_DESTINATION_CARD)) {
    		int routeIndex = intent.getIntExtra(ROUTE_INDEX, 0);
    		setDestination(routeIndex);
        } else {
        	Log.e(TAG, "Unknown action for intent: " + intent.getAction());
        }
        Log.d(TAG, "returning from onStartCommand()");
        return START_STICKY;
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
		liveCardView = new RoutePointCard(route.get(routeIndex)).getRemoteViews(
				getPackageName());
		liveCard.setViews(liveCardView);
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

        private boolean mIsStopped = false;


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
            	int val = mRandom.nextInt(3);
            	if (val == 0) {
                  liveCardView.setTextViewText(R.id.text, "Leslie is a loser.");
            	} else if (val == 1) {
                  liveCardView.setTextViewText(R.id.text, "Nobody likes Leslie.");
            	} else {
                  liveCardView.setTextViewText(R.id.text, "Leslie stinks.");
            	}

                // Always call setViews() to update the live card's RemoteViews.
                liveCard.setViews(liveCardView);

                // Queue another score update in 30 seconds.
                mHandler.postDelayed(mUpdateLiveCardRunnable, DELAY_MILLIS);
            }
        }

        public boolean isStopped() {
            return mIsStopped;
        }

        public void setStop(boolean isStopped) {
            this.mIsStopped = isStopped;
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
		intent.setAction(DISPLAY_GPX);
		intent.putExtra(FILE_PATH, filePath);
		intent.putExtra(ROUTE_INDEX, index);
		return intent;
	}

	public static Intent newDisplayDestinationIntent(Context ctx,
			int picked) {
		Intent intent = new Intent(ctx, StfuLiveCardService.class);
		intent.setAction(PICK_DESTINATION_CARD);
		intent.putExtra(ROUTE_INDEX, picked);
		return intent;
	}
}
