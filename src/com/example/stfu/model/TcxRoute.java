package com.example.stfu.model;

import java.util.ArrayList;

import org.eclipse.jdt.annotation.Nullable;

import android.location.Location;

public class TcxRoute {
	private ArrayList<TrackPoint> trackPoints = null;
	private ArrayList<CoursePoint> coursePoints = null;
	
	public TcxRoute(ArrayList<TrackPoint> trackPoints, ArrayList<CoursePoint> coursePoints) {
		this.trackPoints = trackPoints;
		this.coursePoints = coursePoints;
	}
	
	public int getNextCoursePointIndex(Location curLocation, @Nullable CoursePoint prevCoursePoint) {
		// TODO: look at curLocation.getBearing() to make a guess at which course point
		return 0;
	}
	
	public boolean isOnCourse(Location curLocation) {
		return true;
	}

}
