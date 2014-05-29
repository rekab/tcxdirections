package com.github.rekab.tcxdirections;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.github.barcodeeye.migrated.Intents;
import com.github.barcodeeye.scan.CaptureActivity;
import com.github.rekab.tcxdirections.R;
import com.github.rekab.tcxdirections.model.CoursePoint;

public class MainActivity extends Activity {


    private boolean shouldFinishOnMenuClose;
    private boolean isAttached = false;
    private static final String TAG = "Menu";
    private static final int SCAN_BARCODE_ACTION = 0;
    private static final int PICK_FILE_ACTION = 1;
    private static final int PICK_DESTINATION_ACTION = 2;
	public static final String ROUTE_EXTRA = "route";

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        isAttached = true;
        // TODO: display card selector if a file is already loaded?
        openOptionsMenu();
    }

    @Override
    public void onResume() {
      super.onResume();
      // TODO: is isAttached needed?
      if (isAttached) {
        openOptionsMenu();
      }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        ArrayList<CoursePoint> route = getRoute();
        Log.i(TAG, "opening options menu, route = " + route);
        menu.setGroupVisible(R.id.route_actions_menu_group, route != null);
        return true;
    }

	/**
	 * @return
	 */
	private ArrayList<CoursePoint> getRoute() {
		Intent intentOrigin = getIntent();
		if (intentOrigin.getParcelableArrayListExtra(ROUTE_EXTRA) == null) {
			return null;
		}
        ArrayList<CoursePoint> route = intentOrigin.getParcelableArrayListExtra(ROUTE_EXTRA);
        Log.d(TAG, "intent = " + intentOrigin + " intent.getExtras()="+intentOrigin.getExtras()
        		+ " route=" + route);
		return route;
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_scan) {
            // Keep the activity running until we get an activity result.
            shouldFinishOnMenuClose = false;

            Intent intent = CaptureActivity.newIntent(this);
            intent.putExtra(Intents.Scan.MODE, Intents.Scan.QR_CODE_MODE);
            startActivityForResult(intent, SCAN_BARCODE_ACTION);
            return true;
        } else if (id == R.id.action_browse_files) {
            // Keep the activity running until we get an activity result.
        	shouldFinishOnMenuClose = false;
        	Log.i(TAG, "starting file browser activity");
            // Start a card-based file browser
        	Intent intent = new Intent(this, FileBrowserActivity.class);
        	//intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // wat?
        	startActivityForResult(intent, PICK_FILE_ACTION);
        	return true;
        } else if (id == R.id.action_browse_route) {
            // Keep the activity running until we get an activity result.
        	shouldFinishOnMenuClose = false;
        	Log.i(TAG, "starting route browser activity");
        	// Call and expect a result: pick a different destination
        	// This destination should be a route point index, then it should call
        	// back to the livecard service to change its route index.
        	startActivityForResult(BrowseRouteActivity.newIntent(this, getRoute()), PICK_DESTINATION_ACTION);
        	return true;
        } else if(id == R.id.action_stop_nav) {
        	Log.i(TAG, "told to stop nav");
        	startService(StfuLiveCardService.newStopNavIntent(this));
        	return true;
        }
        
        shouldFinishOnMenuClose = true; // We're not expecting an activity result.
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onDestroy() {
    	Log.i(TAG, "Being destroyed");
    	super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "Got activity requestCode: " + requestCode +
              " result: " + resultCode + " " + data);
        if (requestCode == SCAN_BARCODE_ACTION) {
            if (resultCode == RESULT_OK) {
                String uriString = data.getStringExtra(CaptureActivity.BARCODE_RESULT);
                Log.i(TAG, "should fetch " + uriString);
                ConnectivityManager connMgr = (ConnectivityManager)
                        getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    // TODO: display download started
                    new DownloadFileTask().execute(uriString);
                } else {
                    Log.e(TAG, "no connection");
                    // TODO: error display
                }
            }
            // TODO: display error
        } else if (requestCode == PICK_FILE_ACTION) {
            if (resultCode == RESULT_OK) {
                String filePath = data.getStringExtra(FileBrowserActivity.FILE_RESULT);
                Log.i(TAG, "will open " + filePath);
                displayRouteFile(filePath, 0);
            }
        } else if (requestCode == PICK_DESTINATION_ACTION) {
        	if (resultCode == RESULT_OK) {
        		int picked = data.getIntExtra(BrowseRouteActivity.PICKED_CARD, 0);
        		Log.i(TAG, "picked card #" + picked);
        		Intent intent = StfuLiveCardService.newDisplayDestinationIntent(this, picked);
        		startService(intent);
        		finish();
        	}
        } else {
        	Log.e(TAG, "unknown request code " + requestCode);
        }
        // We're done here.
        finish();
    }

    private void displayRouteFile(String filePath, int index) {
        Log.i(TAG, "Launching intent for " + filePath);
        Intent intent = StfuLiveCardService.newDisplayRouteIntent(this, filePath, index);

		startService(intent);
		finish();
	}

	@Override
    public void onOptionsMenuClosed(Menu menu) {
        super.onOptionsMenuClosed(menu);

        if (shouldFinishOnMenuClose) {
            finish();
        }
    }

    public class DownloadFileTask extends android.os.AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        // TODO: display progress
        //@Override
        //protected void onProgress()

        protected void onPostExecute(String filePath) {
            if (filePath == null) {
                Log.e(TAG, "Download failed");
                // TODO: error display
            } else {
                Log.i(TAG, "Downloaded " + filePath);
                // launch intent for the service to pick up
                displayRouteFile(filePath, 0);
            }
        }

        // Given a URL, establishes an HttpUrlConnection and retrieves
        // the web page content as a InputStream, writes it to a file,
        // and return the path to the file as a string.
        private String downloadUrl(String uriString) throws IOException {
            InputStream is = null;
            try {
                URL url = new URL(uriString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Starts the query
                conn.connect();
                int response = conn.getResponseCode();
                Log.d(TAG, "The response is: " + response);
                if (response == 200) {
                    is = conn.getInputStream();
                    String path = url.getPath();
                    int sep = path.lastIndexOf("/");
                    return saveFile(path.substring(sep + 1), is);
                }

                return null; // TODO: Is this a legal return value?

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }

        private String saveFile(String basename, InputStream inputStream) throws IOException {
            File dir = Filesystem.getStorageDirectory();
            if (!dir.isDirectory()) {
                Log.e(TAG, "Failed to create " + dir.getAbsolutePath());
                return null;
            }
            File file = new File(dir, basename);
            Log.i(TAG, "writing to " + file.getAbsolutePath());

            FileOutputStream outputStream = new FileOutputStream(file);
            try {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            } finally {
                outputStream.close();
            }
            return file.getAbsolutePath();
        }
    }

    /**
     * Create a new intent to display a route.
     * @param ctx
     * @param route
     * @return
     */
	public static Intent newIntent(Context ctx, ArrayList<CoursePoint> route) {
		Intent menuIntent = new Intent(ctx, MainActivity.class);
		menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
		        Intent.FLAG_ACTIVITY_CLEAR_TASK);
		if (route != null) {
			Log.i(TAG, "creating intent to call MainActivity with a route");
			menuIntent.putParcelableArrayListExtra(ROUTE_EXTRA, route);
			Log.d(TAG, "stored: " + menuIntent.getParcelableArrayListExtra(ROUTE_EXTRA));
		} else {
			Log.i(TAG, "NOT creating intent to call MainActivity - there is no route");
		}
		return menuIntent;
	}
}
;
