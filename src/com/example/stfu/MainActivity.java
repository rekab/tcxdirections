package com.example.stfu;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.github.barcodeeye.migrated.Intents;
import com.github.barcodeeye.scan.CaptureActivity;

import android.os.AsyncTask;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;

public class MainActivity extends Activity {


    private boolean shouldFinishOnMenuClose;
    private DownloadManager dm;
    private long downloadRequestId;
	private boolean isAttached = false;
    private static final String TAG = "Menu";
    private static final int SCAN_BARCODE = 0;
    private static final int PICK_FILE = 1;

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
      // is this needed?
      if (isAttached) {
        openOptionsMenu();
      }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
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
            startActivityForResult(intent, SCAN_BARCODE);
            return true;
        } else if (id == R.id.action_browse) {
        	shouldFinishOnMenuClose = false;
        	//shouldFinishOnMenuClose = true;
        	Log.i(TAG, "starting file browser activity");
            // Start a card-based file browser
        	Intent intent = new Intent(this, FileBrowserActivity.class);
        	//intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // wat?
        	startActivityForResult(intent, PICK_FILE);
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
        if (requestCode == SCAN_BARCODE) {
            if (resultCode == RESULT_OK) {
                String uriString = data.getStringExtra(CaptureActivity.BARCODE_RESULT);
                Log.i(TAG, "should fetch " + uriString);
                ConnectivityManager connMgr = (ConnectivityManager)
                        getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    // TODO: display download started
                    new DownloadGpxTask().execute(uriString);
                } else {
                    Log.e(TAG, "no connection");
                    // TODO: error display
                }
                /*dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                Request request = new Request(Uri.parse(uriString));
                downloadRequestId = dm.enqueue(request);*/
            }
            // TODO: display error
        } else if (requestCode == PICK_FILE) {
            if (resultCode == RESULT_OK) {
                String filePath = data.getStringExtra(FileBrowserActivity.FILE_RESULT);
                Log.i(TAG, "will open " + filePath);
                displayGpx(filePath);
            }
        }
        // We're done here.
        finish();
    }

    private void displayGpx(String filePath) {
        Log.i(TAG, "Launching intent for " + filePath);
		Intent intent = new Intent(this, StfuLiveCardService.class);
		intent.setAction(StfuLiveCardService.DISPLAY_GPX);
		intent.putExtra(StfuLiveCardService.FILE_PATH, filePath);
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

    public class DownloadGpxTask extends android.os.AsyncTask<String, Void, String> {

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
                displayGpx(filePath);
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
}
;
