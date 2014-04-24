package com.example.stfu;

import android.location.Location;

public class RoutePoint extends Location {

	private String desc;
	private String name;

	public RoutePoint(String provider, String name, String desc) {
		super(provider);
		setName(name);
		setDescription(desc);
	}

	public String getDescription() {
		return desc;
	}

	public void setDescription(String desc) {
		this.desc = desc;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
