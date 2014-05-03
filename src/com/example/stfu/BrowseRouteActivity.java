package com.example.stfu;

import java.util.ArrayList;

import com.example.stfu.model.RoutePoint;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;

public class BrowseRouteActivity extends Activity {
	private static final String ROUTE_EXTRA = "route";
	private static final String TAG = "BaseRouteActivity";
	public static final String PICKED_CARD = "picked_card";
	ArrayList<RoutePointCard> cards;
    private CardScrollView cardScrollView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ArrayList<Parcelable>parcelableRoute = getIntent().getParcelableArrayListExtra(ROUTE_EXTRA);
		if (parcelableRoute == null) {
			Log.e(TAG, "failed getting route from intent");
			return;
		}
		Log.i(TAG, "parcelableRoute.size()="+parcelableRoute.size());
		cards = new ArrayList<RoutePointCard>(parcelableRoute.size());
		for (int i = 0; i < parcelableRoute.size(); i++) {
			cards.add(new RoutePointCard((RoutePoint) parcelableRoute.get(i)));
		}
		cardScrollView = new CardScrollView(this);
		cardScrollView.setAdapter(new RoutePointCardScrollAdapter());
		cardScrollView.setHorizontalScrollBarEnabled(true);
		cardScrollView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
				Log.i(TAG, "picked card #" + pos);
				Intent returnIntent = new Intent();
				returnIntent.putExtra(PICKED_CARD, pos);
				setResult(RESULT_OK, returnIntent);
				Log.i(TAG, "set result, calling finish()");
				finish();
			}
		});
		cardScrollView.activate();
		setContentView(cardScrollView);
	}

	/** Returns an intent for launching this activity. */
	public static Intent newIntent(Context ctx, ArrayList<RoutePoint> route) {
		Intent intent = new Intent(ctx, BrowseRouteActivity.class);
		intent.putParcelableArrayListExtra(ROUTE_EXTRA, route);
		return intent;
	}
	
	private class RoutePointCardScrollAdapter extends CardScrollAdapter {
    	@Override
        public int getPosition(Object item) {
            return cards.indexOf(item);
        }

        @Override
        public int getCount() {
            return cards.size();
        }

        @Override
        public Object getItem(int position) {
            return cards.get(position);
        }

        /**
         * Returns the amount of view types.
         */
        @Override
        public int getViewTypeCount() {
        	return 1; // TODO: other card types
        }

        /**
         * Returns the view type of this card so the system can figure out
         * if it can be recycled.
         */
        @Override
        public int getItemViewType(int position){
            return Adapter.IGNORE_ITEM_VIEW_TYPE;
            // todo: return other types
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return  cards.get(position).getView(BrowseRouteActivity.this, convertView, parent);
        }


	}
}
