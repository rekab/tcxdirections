package com.github.rekab.tcxdirections.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.location.Location;
import android.util.Log;

public class TcxRoute {
	private static final int NEARBY_TRACK_POINT_METERS = 100;
	private static final String TAG = "TcxRoute";
	private ArrayList<TrackPoint> trackPoints = null;
	private ArrayList<CoursePoint> coursePoints = null;
	
	public TcxRoute(ArrayList<TrackPoint> trackPoints, ArrayList<CoursePoint> coursePoints) {
		this.setTrackPoints(trackPoints);
		this.setCoursePoints(coursePoints);
	}
	
	public int getNextCoursePointIndex(Location curLocation) {
		return getNextCoursePointIndex(curLocation, -1);
	}

	
	public int getNextCoursePointIndex(Location curLocation, int curCoursePointIndex) {
		// Verify we're not at the end already.
		if (curCoursePointIndex >= (getCoursePoints().size() - 1)) {
			Log.d(TAG, "we're at the end of the route");
			return curCoursePointIndex;
		}

		int probableNextCoursePointIndex = curCoursePointIndex + 1;
		Log.d(TAG, "curLocation=" + curLocation);
		Log.d(TAG, "probableNextCoursePointIndex=" + probableNextCoursePointIndex);

		// Find the nearest track points.
		ArrayList<TrackPoint> nearbyTrackPoints = getNearbyTrackPoints(curLocation);
		if (nearbyTrackPoints == null || nearbyTrackPoints.size() == 0) {
			Log.d(TAG, "no nearby track points, we're off course");
			//// If there aren't any we're off course, just assume we're going to the next.
			//return probableNextCoursePointIndex;
			return curCoursePointIndex;
		}

		// It's possible the tracks could overlap. Try to find tracks leading to the next
		// sequential course point. If none, find the tracks going in the same
		// direction we're going.
		CoursePoint probableNextCoursePoint = getCoursePoints().get(probableNextCoursePointIndex);
		for (TrackPoint tp : nearbyTrackPoints) {
			if (tp.getDestination().equals(probableNextCoursePoint)) {
				Log.d(TAG, "found course point with equal probableNextCoursePoint");
				return probableNextCoursePointIndex;
			} else {
				Log.d(TAG, "tp=" +  tp + " dest=" + tp.getDestination() + " != probable:" +
						probableNextCoursePoint);
			}
		}
		Log.i(TAG, "giving up");
		return curCoursePointIndex;
		/*
		for (TrackPoint tp : nearbyTrackPoints) {
			Log.i(TAG, "attempting to find track points we're headed towards");
			// If we're headed towards this point (within 30 degrees).
			if (Math.abs(curLocation.bearingTo(tp) - curLocation.getBearing()) < 30) {
				int index = getCoursePoints().indexOf(tp.getDestination());
				Log.i(TAG, "found: index=" + index + "loc=" + tp.getDestination());
				return index;
			}
		}
		// TODO: should raise an exception instead
		return 0;*/
	}
	
	public boolean isOnCourse(Location curLocation, int curCoursePointIndex) {
		// Check if we can find a nearby track point with the same course point.
		ArrayList<TrackPoint> nearbyTrackPoints = getNearbyTrackPoints(curLocation);
		if (nearbyTrackPoints == null || nearbyTrackPoints.size() == 0) {
			Log.w(TAG, "Off course: no nearby track points");
			return false;
		}
		for (TrackPoint tp : nearbyTrackPoints) {
			Log.d(TAG, "Checking if curCoursePoint=" + curCoursePointIndex + "agrees with tp=" + tp);
			if (tp.getDestination().equals(getCoursePoints().get(curCoursePointIndex))) {
				Log.d(TAG, "match");
				return true;
			}
		}
		Log.w(TAG, "off course!");
		return false;
	}

	private ArrayList<TrackPoint> getNearbyTrackPoints(final Location curLocation) {
		ArrayList<TrackPoint> filtered = new ArrayList<TrackPoint>();
		for (TrackPoint tp : getTrackPoints()) {
			if (curLocation.distanceTo(tp) < NEARBY_TRACK_POINT_METERS) {
				filtered.add(tp);
			}
		}
		Collections.sort(filtered, new Comparator<TrackPoint>() {
			@Override
			public int compare(TrackPoint lhs, TrackPoint rhs) {
				return (int) (curLocation.distanceTo(lhs) - curLocation.distanceTo(rhs));
			}
		});
		return filtered;
	}

	public ArrayList<TrackPoint> getTrackPoints() {
		return trackPoints;
	}

	public void setTrackPoints(ArrayList<TrackPoint> trackPoints) {
		this.trackPoints = trackPoints;
	}

	public ArrayList<CoursePoint> getCoursePoints() {
		return coursePoints;
	}

	public void setCoursePoints(ArrayList<CoursePoint> coursePoints) {
		this.coursePoints = coursePoints;
	}

}
