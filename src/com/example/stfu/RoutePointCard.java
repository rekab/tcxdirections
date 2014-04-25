package com.example.stfu;

import com.google.android.glass.app.Card;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;

// It'd be great to just extend Card, but this is destined for a RemoteView
public class RoutePointCard {
	
	private static final String TAG = "RoutePointCard";
	private RoutePoint routePoint;
	private Object layoutInflater;
	private String packageName;
	public RoutePointCard(String packageName, RoutePoint routePoint) {
		this.packageName = packageName;
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
    public RemoteViews getView() {
    	RemoteViews view = new RemoteViews(packageName, R.layout.route_point_card);
    	view.setTextViewText(R.id.route_point_description, routePoint.getDescription());
		if (routePoint.getName() != null) {
			if (routePoint.getName().equalsIgnoreCase("right")) {
				view.setImageViewResource(R.id.route_point_icon, R.drawable.right_arrow);
			} else if (routePoint.getName().equalsIgnoreCase("left")) {
				view.setImageViewResource(R.id.route_point_icon, R.drawable.left_arrow);
			} else if (routePoint.getName().equalsIgnoreCase("straight")) {
				view.setImageViewResource(R.id.route_point_icon, R.drawable.straight_arrow);
			} else if (routePoint.getName().equalsIgnoreCase("water")) {
				view.setImageViewResource(R.id.route_point_icon, R.drawable.water);
			} else {
				Log.e(TAG, "unknown name:" + routePoint.getName());
			}
		}
		return view;
	}

}
