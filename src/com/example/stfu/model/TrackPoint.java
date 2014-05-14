package com.example.stfu.model;

import android.location.Location;

public class TrackPoint extends Location {

	private Double distance;
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

	public Double getDistance() {
		return distance;
	}

	public void setDistance(Double distance) {
		this.distance = distance;
	}

}
