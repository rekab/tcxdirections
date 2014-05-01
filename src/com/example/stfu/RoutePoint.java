package com.example.stfu;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class RoutePoint extends Location implements Parcelable {

	private String desc;
	private String name;
	private String provider;

    public static final Parcelable.Creator<RoutePoint> CREATOR
    	= new Parcelable.Creator<RoutePoint>() {
		public RoutePoint createFromParcel(Parcel in) {
			Log.i(TAG, "createFromParcel()");
		    return new RoutePoint(in);
		}
		
		public RoutePoint[] newArray(int size) {
		    return new RoutePoint[size];
		}
	};
	private static final String TAG = "RoutePoint";

	public RoutePoint(String provider, String name, String desc) {
		super(provider);
		this.provider = provider;
		setName(name);
		setDescription(desc);
	}
	
	private RoutePoint(Parcel in) {
		super(in.readString());
		Log.i(TAG, "constructing from parcel");
		setName(in.readString());
		setDescription(in.readString());
		setLatitude(in.readDouble());
		setLongitude(in.readDouble());
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
	
	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		parcel.writeString(provider);
		parcel.writeString(name);
		parcel.writeString(desc);
		parcel.writeDouble(getLatitude());
		parcel.writeDouble(getLongitude());
	}

}
