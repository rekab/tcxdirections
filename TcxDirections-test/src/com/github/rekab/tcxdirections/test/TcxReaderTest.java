package com.github.rekab.tcxdirections.test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;

import com.github.rekab.tcxdirections.model.CoursePoint;
import com.github.rekab.tcxdirections.model.TcxReader;
import com.github.rekab.tcxdirections.model.TcxRoute;
import com.github.rekab.tcxdirections.model.TrackPoint;

import android.test.suitebuilder.annotation.MediumTest;
import junit.framework.TestCase;

public class TcxReaderTest extends TestCase {
	private final static String cts5tcx = "assets/cts-5-course.tcx";
	private final static String cts5mile64ErrorTrack = "assets/cts-5-track-mile-64-off.gpx";
	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	@MediumTest
	public void testLoadTcx() {
		InputStream input = this.getClass().getClassLoader().getResourceAsStream(cts5tcx);
		if (input == null) {
			fail("Couldn't load " + cts5tcx);
		}
		TcxRoute route = TcxReader.getTcxRoute(input);
		assertNotNull(route);
		assertNotNull(route.getCoursePoints());
		assertNotNull(route.getTrackPoints());
		assertTrue(route.getCoursePoints().size() > 0);
		assertTrue(route.getTrackPoints().size() > 0);

		// Verify all track points have a destination, and that all course points
		// are accounted for.
		HashSet<CoursePoint> coursePoints = new HashSet<CoursePoint>(route.getCoursePoints());
		// Remove the start.
		coursePoints.remove(route.getCoursePoints().get(0));
		for (TrackPoint tp : route.getTrackPoints()) {
			assertNotNull(String.format("null destination at %f, %f",
					tp.getLatitude(),  tp.getLongitude()),
					tp.getDestination());
			if (coursePoints.contains(tp.getDestination())) {
				coursePoints.remove(tp.getDestination());
			}
		}
		// Verify we've seen all course points.
		if (!coursePoints.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			for (CoursePoint cp : coursePoints) {
				sb.append(cp.toString() + ", ");
			}
			fail("Course points contain: " + sb.toString());
		}
	}

	@MediumTest
	public void testZipTrackPointsToCoursePoints() {
		ArrayList<CoursePoint> coursePoints = new ArrayList<CoursePoint>();
		coursePoints.add(new CoursePoint(null, null, "origin", null, 0.0, 0.0));
		coursePoints.add(new CoursePoint(null, null, "topleft", null, 1.0, 0.0));
		coursePoints.add(new CoursePoint(null, null, "topright", null, 1.0, 1.0));
		coursePoints.add(new CoursePoint(null, null, "bottomleft", null, 0.0, 1.0));

		ArrayList<TrackPoint> trackPoints = new ArrayList<TrackPoint>();
		// Points 0-2 should be zipped to the "topleft"
		trackPoints.add(new TrackPoint(null, 0.0, 0.0)); // == origin
		trackPoints.add(new TrackPoint(null, 0.5, 0.0));
		trackPoints.add(new TrackPoint(null, 0.6, 1.0));

		// Points 3-6 should be zipped to the "topright"
		trackPoints.add(new TrackPoint(null, 1.0, 0.0)); // == topleft
		trackPoints.add(new TrackPoint(null, 1.1, 0.2));
		trackPoints.add(new TrackPoint(null, 1.0, -0.3));
		trackPoints.add(new TrackPoint(null, 1.0, 0.7));
		
		// Points 7-11 should be zipped to the "bottomleft" (the end)
		trackPoints.add(new TrackPoint(null, 1.0, 1.0)); // == topright
		trackPoints.add(new TrackPoint(null, 1.0, 1.1));
		trackPoints.add(new TrackPoint(null, 1.2, 0.5));
		trackPoints.add(new TrackPoint(null, 1.5, 0.6));
		trackPoints.add(new TrackPoint(null, 1.0, 0.0));

		TcxReader.zipTrackPointsToCoursePoints(trackPoints, coursePoints);
		int tpIndex = 0;
		int cpIndex = 1;
		CoursePoint cp = coursePoints.get(cpIndex);
		for (; tpIndex < 3; tpIndex++) {
			assertEquals(cp, trackPoints.get(tpIndex).getDestination());
		}
		cpIndex++;
		cp = coursePoints.get(cpIndex);
		for (; tpIndex < 7; tpIndex++) {
			assertEquals(cp, trackPoints.get(tpIndex).getDestination());
		}
		cpIndex++;
		assertEquals(cpIndex, coursePoints.size() - 1);
		cp = coursePoints.get(cpIndex);
		for (; tpIndex < trackPoints.size(); tpIndex++) {
			assertEquals(cp, trackPoints.get(tpIndex).getDestination());
		}
	}
}
