package com.example.stfu.model;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class CoursePoint extends Location implements Parcelable {

	private String desc;
	private String name;
	private String provider;

    public static final Parcelable.Creator<CoursePoint> CREATOR
    	= new Parcelable.Creator<CoursePoint>() {
		public CoursePoint createFromParcel(Parcel in) {
			Log.i(TAG, "createFromParcel()");
		    return new CoursePoint(in);
		}
		
		public CoursePoint[] newArray(int size) {
		    return new CoursePoint[size];
		}
	};
	private static final String TAG = "RoutePoint";

	public CoursePoint(String provider, String name, String desc) {
		super(provider);
		this.provider = provider;
		setName(name);
		setDescription(desc);
	}
	
	private CoursePoint(Parcel in) {
		super(in.readString());
		Log.i(TAG, "constructing from parcel");
		setName(in.readString());
		setDescription(in.readString());
		setLatitude(in.readDouble());
		setLongitude(in.readDouble());
	}
	
	public CoursePoint(String provider, String name, String desc, double lat, double lng) {
		super(provider);
		this.provider = provider;
		setName(name);
		setDescription(desc);
		setLatitude(lat);
		setLongitude(lng);
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
	
	@Override
	public String toString() {
		return super.toString() + " desc=" + getDescription();
	}

}
