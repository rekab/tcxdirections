package com.example.stfu.model;

import java.util.ArrayList;

import android.os.Parcelable;
import android.util.Log;


public class Route extends ArrayList<CoursePoint> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String TAG = "model.Route";

	public Route(ArrayList<Parcelable> parcelableRoute) {
		super(parcelableRoute.size());
		Log.i(TAG, "parcelableRoute.size()="+parcelableRoute.size());

		for (int i = 0; i < parcelableRoute.size(); i++) {
			add((CoursePoint) parcelableRoute.get(i));
		}
	}
}
