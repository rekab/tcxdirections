package com.example.stfu;

import com.github.barcodeeye.migrated.Intents;
import com.github.barcodeeye.scan.CaptureActivity;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

public class MainActivity extends Activity {
    private boolean shouldFinishOnMenuClose;
    private static final String TAG = "Menu";
	private static final int SCAN_BARCODE = 0;

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        openOptionsMenu();
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
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "Got activity result: " + resultCode + " " + data);
        if (requestCode == SCAN_BARCODE) {
            if (resultCode == RESULT_OK) {
                Log.i(TAG, "should fetch " + data.getStringExtra(CaptureActivity.BARCODE_RESULT));
            }
        }
        // We're done here.
        finish();
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        super.onOptionsMenuClosed(menu);

        if (shouldFinishOnMenuClose) {
            finish();
        }
    }
}
