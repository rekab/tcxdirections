package com.example.stfu.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import android.annotation.SuppressLint;
import android.util.Log;
import android.util.Pair;
import android.util.Xml;

public class TcxReader {
	private static final String TAG = "TcxReader";
	// 2014-05-08T04:21:41Z
	private static final String TCX_DATE_FORMAT = "yyy-mm-dd'T'HH:mm:ss'Z'";

	public static TcxRoute getTcxRoute(File source) {
		FileInputStream input;
		try {
			input = new FileInputStream(source);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
		return getTcxRoute(input);
	}

	public static TcxRoute getTcxRoute(InputStream input) {
		try {
	        try {
	            XmlPullParser parser = Xml.newPullParser();
				parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
	            parser.setInput(input, null);
	            return readTcxDoc(parser);
	        } catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
	        } catch (ParseException e) {
	        	e.printStackTrace();
			} finally {
	            input.close();
	        }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private static TcxRoute readTcxDoc(XmlPullParser parser) throws XmlPullParserException, IOException, ParseException {
		int eventType;
		do {
			eventType = parser.next();
			if (eventType == XmlPullParser.START_TAG && parser.getName().equals("Course")) {
				// Get the first course
				Log.i(TAG, "reading course");
				return readCourse(parser);
			}
		} while (eventType != XmlPullParser.END_DOCUMENT);
		Log.e(TAG, "Couldn't find <Course> tag.");
		return null;
	}

	private static TcxRoute readCourse(XmlPullParser parser) throws XmlPullParserException, IOException, ParseException {
		ArrayList<TrackPoint> trackPoints = null;
		ArrayList<CoursePoint> coursePoints = new ArrayList<CoursePoint>();
		parser.require(XmlPullParser.START_TAG, null, "Course");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			if (parser.getName().equals("Track")) {
				trackPoints = readTrack(parser);
			} else if (parser.getName().equals("CoursePoint")) {
				coursePoints.add(readCoursePoint(parser));
			} else {
				skip(parser);
			}
		}
		// "Zip" the track points to the course points: for each course point, find
		// all the track points that lead to that course point. A track point
		// is assumed to be leading away from a course point once the distance
		// is small and bearing to the course point changes by a significant amount.

		return new TcxRoute(trackPoints, coursePoints);
	}

	private static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
	    if (parser.getEventType() != XmlPullParser.START_TAG) {
	        throw new IllegalStateException();
	    }
	    int depth = 1;
	    while (depth != 0) {
	        switch (parser.next()) {
	        case XmlPullParser.END_TAG:
	            depth--;
	            break;
	        case XmlPullParser.START_TAG:
	            depth++;
	            break;
	        }
	    }		
	}

	private static ArrayList<TrackPoint> readTrack(XmlPullParser parser) throws XmlPullParserException, IOException, ParseException {
		ArrayList<TrackPoint> trackPoints = new ArrayList<TrackPoint>();
		parser.require(XmlPullParser.START_TAG, null, "Track");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			if (parser.getName().equals("Trackpoint")) {
				trackPoints.add(readTrackPoint(parser));
			} else {
				skip(parser);
			}
		}
		return trackPoints;
	}

	private static TrackPoint readTrackPoint(XmlPullParser parser) throws XmlPullParserException, IOException, ParseException {
		parser.require(XmlPullParser.START_TAG, null, "Trackpoint");
		TrackPoint trackPoint = new TrackPoint(TAG, null);

		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			if (parser.getName().equals("Position")) {
				Pair<Double, Double> latLong = readPosition(parser);
				trackPoint.setLatitude(latLong.first);
				trackPoint.setLongitude(latLong.second);
			} else if (parser.getName().equals("AltitudeMeters")) {
				trackPoint.setAltitude(readTextToDouble(parser));
				parser.require(XmlPullParser.END_TAG, null, "AltitudeMeters");
			} else if (parser.getName().equals("DistanceMeters")) {
				trackPoint.setDistance(readTextToDouble(parser));
				parser.require(XmlPullParser.END_TAG, null, "DistanceMeters");
			} else if (parser.getName().equals("Time")) {
				Long t = readTime(parser);
				trackPoint.setTime(t);
				parser.require(XmlPullParser.END_TAG, null, "Time");
			} else {
				skip(parser);
			}
		}
		return trackPoint;
	}

	private static Pair<Double, Double> readPosition(XmlPullParser parser) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, null, "Position");
		Double lat = null;
		Double lng = null;
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			if (parser.getName().equals("LatitudeDegrees")) {
				lat = readTextToDouble(parser);
				parser.require(XmlPullParser.END_TAG, null, "LatitudeDegrees");
			} else if (parser.getName().equals("LongitudeDegrees")) {
				lng = readTextToDouble(parser);
				parser.require(XmlPullParser.END_TAG, null, "LongitudeDegrees");
			}
		}
		parser.require(XmlPullParser.END_TAG, null, "Position");
		return new Pair<Double, Double>(lat, lng);
	}

	@SuppressLint("SimpleDateFormat")  // we don't care about date locales, we're using longs
	private static Long readTime(XmlPullParser parser) throws XmlPullParserException, IOException, ParseException {
		Long ret = null;
		if (parser.next() == XmlPullParser.TEXT) {
			SimpleDateFormat sf = new SimpleDateFormat(TCX_DATE_FORMAT);
			ret = sf.parse(parser.getText()).getTime();
			parser.nextTag();
		} else {
			Log.e(TAG, "unable to find text for time");
		}
		return ret;
	}

	private static Double readTextToDouble(XmlPullParser parser) throws NumberFormatException, XmlPullParserException, IOException {
		Double ret = null;
		if (parser.next() == XmlPullParser.TEXT) {
			ret = Double.parseDouble(parser.getText());
			parser.nextTag();
		}
		return ret;
	}

	private static CoursePoint readCoursePoint(XmlPullParser parser) throws XmlPullParserException, IOException {
		// TODO Auto-generated method stub
		return null;
	}
}
