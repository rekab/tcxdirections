package com.example.stfu.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.location.Location;

public class TcxRoute {
	private static final int NEARBY_TRACK_POINT_METERS = 100;
	private ArrayList<TrackPoint> trackPoints = null;
	private ArrayList<CoursePoint> coursePoints = null;
	
	public TcxRoute(ArrayList<TrackPoint> trackPoints, ArrayList<CoursePoint> coursePoints) {
		this.setTrackPoints(trackPoints);
		this.setCoursePoints(coursePoints);
	}
	
	public int getNextCoursePointIndex(Location curLocation) {
		return getNextCoursePointIndex(curLocation, -1);
	}

	
	public int getNextCoursePointIndex(Location curLocation, int prevCoursePointIndex) {
		// Verify we're not at the end already.
		if (prevCoursePointIndex >= (getCoursePoints().size() - 1)) {
			return prevCoursePointIndex;
		}

		int probableNextCoursePointIndex = prevCoursePointIndex + 1;

		// Find the nearest track points within 100m.
		ArrayList<TrackPoint> nearbyTrackPoints = getNearbyTrackPoints(curLocation);
		if (nearbyTrackPoints == null || nearbyTrackPoints.size() == 0) {	
			// If there aren't any we're off course, just assume we're going to the next.
			return probableNextCoursePointIndex;
		}

		// It's possible the tracks could overlap. Try to find tracks leading to the next
		// sequential course point. If none, find the tracks going in the same
		// direction we're going.
		CoursePoint probableNextCoursePoint = getCoursePoints().get(probableNextCoursePointIndex);
		for (TrackPoint tp : nearbyTrackPoints) {
			if (tp.getDestination().equals(probableNextCoursePoint)) {
				return probableNextCoursePointIndex;
			}
		}
		for (TrackPoint tp : nearbyTrackPoints) {
			// If we're headed towards this point (within 30 degrees).
			if (Math.abs(curLocation.bearingTo(tp) - curLocation.getBearing()) < 30) {
				return getCoursePoints().indexOf(tp.getDestination());
			}
		}
		// TODO: should raise an exception instead
		return 0;
	}
	
	public boolean isOnCourse(Location curLocation, int curCoursePointIndex) {
		// Check if we can find a track point within 100m with the same course point.
		ArrayList<TrackPoint> nearbyTrackPoints = getNearbyTrackPoints(curLocation);
		if (nearbyTrackPoints == null || nearbyTrackPoints.size() == 0) {
			return false;
		}
		for (TrackPoint tp : nearbyTrackPoints) {
			if (tp.getDestination().equals(getCoursePoints().get(curCoursePointIndex))) {
				return true;
			}
		}
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
