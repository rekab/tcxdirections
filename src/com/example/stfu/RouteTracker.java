package com.example.stfu;

import java.util.List;

import com.example.stfu.model.RoutePoint;

public class RouteTracker {
	private List<RoutePoint> routePoints;
	private int currentDestinationIndex;

	public RouteTracker(List<RoutePoint> routePoints) {
		this.routePoints = routePoints;
		setCurrentDestinationIndex(0);
	}
	
	public RoutePoint getCurrentDestination() {
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
