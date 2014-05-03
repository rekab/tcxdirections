package com.example.stfu;

import com.example.stfu.model.RoutePoint;
import com.google.android.glass.app.Card;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

// It'd be great to just extend Card, but this is destined for a RemoteView
public class RoutePointCard {
	
	private static final String TAG = "RoutePointCard";
	private RoutePoint routePoint;
	public RoutePointCard(RoutePoint routePoint) {
		this.routePoint = routePoint;
	}
	/*
		setText(routePoint.getDescription());
		if (routePoint.getName() != null) {
			if (routePoint.getName().equalsIgnoreCase("right")) {
				addImage(R.drawable.right_arrow);
			} else if (routePoint.getName().equalsIgnoreCase("left")) {
				addImage(R.drawable.left_arrow);
			} else if (routePoint.getName().equalsIgnoreCase("straight")) {
				addImage(R.drawable.straight_arrow);
			} else if (routePoint.getName().equalsIgnoreCase("water")) {
				addImage(R.drawable.water);
			} else {
				Log.e(TAG, "unknown name:" + routePoint.getName());
			}
		} */
    public RemoteViews getRemoteViews(String packageName) {
    	RemoteViews view = new RemoteViews(packageName, R.layout.route_point_card);
    	view.setTextViewText(R.id.route_point_description, routePoint.getDescription());
		if (routePoint.getName() != null) {
	    	Integer id = getImageIdForRoutePoint();
	    	if (id != null) {
	    		view.setImageViewResource(R.id.route_point_icon, id);
	    	}
		}
		return view;
	}
    
    public View getView(Context ctx, View convertView, ViewGroup parent) {
    	if (convertView == null) {
    		LayoutInflater inflater = LayoutInflater.from(ctx);
    		convertView = inflater.inflate(R.layout.route_point_card, parent);
    	}
    	((TextView) convertView.findViewById(R.id.route_point_description)).setText(routePoint.getDescription());
    	Integer id = getImageIdForRoutePoint();
    	if (id != null) {
    		((ImageView) convertView.findViewById(R.id.route_point_icon)).setImageResource(id);
    	}
    	return convertView;
    }

	private Integer getImageIdForRoutePoint() {
		if (routePoint.getName() == null) {
			return null;
		}
		if (routePoint.getName().equalsIgnoreCase("right")) {
			return R.drawable.right_arrow;
		} else if (routePoint.getName().equalsIgnoreCase("left")) {
			return R.drawable.left_arrow;
		} else if (routePoint.getName().equalsIgnoreCase("straight")) {
			return R.drawable.straight_arrow;
		} else if (routePoint.getName().equalsIgnoreCase("water")) {
			return R.drawable.water;
		}
		// TODO:  "Generic", "Summit", "Valley", "Water", "Food", "Danger", 
		// "Left", "Right", "Straight", "First Aid", "4th Category",
		// "3rd Category", "2nd Category", "1st Category", "Hors Category",
		// "Sprint"
		Log.e(TAG, "unknown name:" + routePoint.getName());
		return null;
	}

}
