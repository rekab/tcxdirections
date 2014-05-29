package com.github.rekab.tcxdirections.test;

import java.io.InputStream;
import java.util.ArrayList;

import com.github.rekab.tcxdirections.GpxReader;
import com.github.rekab.tcxdirections.model.CoursePoint;
import com.github.rekab.tcxdirections.model.TcxReader;
import com.github.rekab.tcxdirections.model.TcxRoute;

import junit.framework.TestCase;

public class TcxRouteTest extends TestCase {
	private final String testRoute1 = "assets/test_route_1.tcx";
	private final String testOfRoute1 = "assets/test_of_route_1.gpx";

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	private TcxRoute loadRoute(String fileName) {
		InputStream input = getResourceAsStream(fileName);
		if (input == null) {
			fail("Couldn't load " + fileName);
		}
		TcxRoute route = TcxReader.getTcxRoute(input);
		assertNotNull(route);
		assertNotNull(route.getCoursePoints());
		assertNotNull(route.getTrackPoints());
		assertTrue(route.getCoursePoints().size() > 0);
		assertTrue(route.getTrackPoints().size() > 0);
		return route;
	}
	
	private InputStream getResourceAsStream(String fileName) {
		return this.getClass().getClassLoader().getResourceAsStream(fileName);
	}
	
	public void testRoute1() throws Exception {
		TcxRoute route = loadRoute(testRoute1);
		ArrayList<CoursePoint> coursePoints = GpxReader.getTrackPoints(
				getResourceAsStream(testOfRoute1));
		assertNotNull(coursePoints);
		assertEquals(coursePoints.size(), 12);

		// Points 0-3: on track, should be linked to the first turn
		int trackIndex = 0;
		CoursePoint cp = null;
		for (;trackIndex < 4; trackIndex++) {
			cp = coursePoints.get(trackIndex);
			assertEquals(1, route.getNextCoursePointIndex(cp, 0));
			assertTrue(route.isOnCourse(cp, 1));
		}

		// Points 4-5: on track, should be linked to the second turn
		cp = coursePoints.get(trackIndex++);
		assertEquals(2, route.getNextCoursePointIndex(cp, 1));
		assertTrue(route.isOnCourse(cp, 2));
		cp = coursePoints.get(trackIndex++);
		assertEquals(2, route.getNextCoursePointIndex(cp, 1));
		assertTrue(route.isOnCourse(cp, 2));

		// Point 6: off by a block
		cp = coursePoints.get(trackIndex++);
		assertEquals(2, route.getNextCoursePointIndex(cp, 1));
		assertTrue(route.isOnCourse(cp, 2));

		// Point 7: off by a block
		cp = coursePoints.get(trackIndex++);
		assertEquals(3, route.getNextCoursePointIndex(cp, 2));
		assertTrue(route.isOnCourse(cp, 2));
		
		// Point 8: off track
		cp = coursePoints.get(trackIndex++);
		assertEquals(3, route.getNextCoursePointIndex(cp, 2));
		assertEquals(3, route.getNextCoursePointIndex(cp, 3));
		assertFalse(route.isOnCourse(cp, 2));
		assertFalse(route.isOnCourse(cp, 3));

		// Point 9: off track
		cp = coursePoints.get(trackIndex++);
		assertEquals(3, route.getNextCoursePointIndex(cp, 2));
		assertEquals(3, route.getNextCoursePointIndex(cp, 3));
		assertFalse(route.isOnCourse(cp, 2));
		assertFalse(route.isOnCourse(cp, 3));

		// Point 10: back on track
		cp = coursePoints.get(trackIndex++);
		assertEquals(3, route.getNextCoursePointIndex(cp, 2));
		assertEquals(3, route.getNextCoursePointIndex(cp, 3));
		assertTrue(route.isOnCourse(cp, 3));

		// Point 11: back on track
		cp = coursePoints.get(trackIndex++);
		assertEquals(3, route.getNextCoursePointIndex(cp, 2));
		assertEquals(3, route.getNextCoursePointIndex(cp, 3));
		assertTrue(route.isOnCourse(cp, 3));
	}

}
