package com.example.stfu;

import java.io.File;
import java.util.List;
import java.util.Random;

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.LiveCard.PublishMode;

import android.app.PendingIntent;
import android.app.Service;
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
            setText("Tap for options");
            
            // Set up the live card's action with a pending intent
            // to show a menu when tapped
            Intent menuIntent = new Intent(this, MainActivity.class);
            menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
            liveCard.setAction(PendingIntent.getActivity(
                    this, 0, menuIntent, 0));
            
            liveCard.publish(PublishMode.REVEAL);
            //mHandler.post(mUpdateLiveCardRunnable);
        } else if (intent.getAction().equals(DISPLAY_GPX)) {
        	if (intent.hasExtra(FILE_PATH)) {
        		File gpxFile = new File(intent.getStringExtra(FILE_PATH));
        		List<RoutePoint> route = GpxReader.getRoutePoints(gpxFile);
        		//Log.i(TAG, "loaded route " + route);
        		liveCard.setViews(new RoutePointCard(getPackageName(), route.get(0)).getView());
        		//setText(route.get(0).getDescription());
        	} else {
        		Log.e(TAG, "Got a DISPLAY_GPX action with no file?");
        	}
        }
        Log.d(TAG, "returning from onStartCommand()");
        return START_STICKY;
    }

	private void setText(String text) {
		liveCardView.setTextViewText(R.id.text, text);
		liveCard.setViews(liveCardView);
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
}
