package com.example.stfu;

import java.util.List;

import com.example.stfu.model.CoursePoint;

public class RouteTracker {
	private List<CoursePoint> routePoints;
	private int currentDestinationIndex;

	public RouteTracker(List<CoursePoint> routePoints) {
		this.routePoints = routePoints;
		setCurrentDestinationIndex(0);
	}
	
	public CoursePoint getCurrentDestination() {
		return routePoints.get(getCurrentDestinationIndex());
	}

	public int getSize() {
		return routePoints.size();
	}

	public int getCurrentDestinationIndex() {
		return currentDestinationIndex;
	}

	public void setCurrentDestinationIndex(int currentDestinationIndex) {
		this.currentDestinationIndex = currentDestinationIndex;
	}
}
