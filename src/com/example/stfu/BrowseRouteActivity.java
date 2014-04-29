package com.example.stfu;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;

public class BrowseRouteActivity extends Activity {
	private static final String ROUTE_EXTRA = "route";
	private static final String TAG = "BaseRouteActivity";
	ArrayList<RoutePoint> route = null;
	@Override
	public void onStart() {
		Parcelable[] parcelableRoute = getIntent().getParcelableArrayExtra(ROUTE_EXTRA);
		if (parcelableRoute == null) {
			Log.e(TAG, "failed getting route from intent");
		} else {
			route = new ArrayList<RoutePoint>(parcelableRoute.length);
			for (int i = 0; i < parcelableRoute.length; i++) {
				route.set(i, (RoutePoint) parcelableRoute[i]);
			}
		}
	}

	public static Intent newIntent(Context ctx, ArrayList<RoutePoint> route) {
		Intent intent = new Intent(ctx, BrowseRouteActivity.class);
		intent.putParcelableArrayListExtra(ROUTE_EXTRA, route);
		return intent;
	}
}
