package com.example.stfu.model;

import android.location.Location;

public class TrackPoint extends Location {

	private CoursePoint destination;

	public TrackPoint(String provider, CoursePoint destination) {
		super(provider);
		this.setDestination(destination);
	}

	public CoursePoint getDestination() {
		return destination;
	}

	public void setDestination(CoursePoint destination) {
		this.destination = destination;
	}

}
