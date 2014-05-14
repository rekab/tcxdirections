package com.example.stfu.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;
import android.util.Xml;

public class TcxReader {
	private static final String TAG = "TcxReader";

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
			} finally {
	            input.close();
	        }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private static TcxRoute readTcxDoc(XmlPullParser parser) throws XmlPullParserException, IOException {
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

	private static TcxRoute readCourse(XmlPullParser parser) throws XmlPullParserException, IOException {
		ArrayList<TrackPoint> trackPoints = null;
		ArrayList<CoursePoint> coursePoints = null;
		parser.require(XmlPullParser.START_TAG, null, "Course");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			if (parser.getName().equals("Track")) {
				trackPoints = readTrack(parser);
			}
		}
		// "Zip" the track points to the course points: for each course point, find
		// all the track points that lead to that course point. A track point
		// is assumed to be leading away from a course point once the distance
		// is small and bearing to the course point changes by a significant amount.

		return new TcxRoute(trackPoints, coursePoints);
	}

	private static ArrayList<TrackPoint> readTrack(XmlPullParser parser) throws XmlPullParserException, IOException  {
		// TODO Auto-generated method stub
		return null;
	}
	
}
