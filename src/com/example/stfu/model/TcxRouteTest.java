package com.example.stfu.model;

import java.io.InputStream;

import junit.framework.TestCase;

public class TcxRouteTest extends TestCase {
	private final String testRoute1 = "assets/Test_route__1.tcx";
	private final String testOfRoute1 = "assets/Test_of_route__1.gpx";

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	private TcxRoute loadRoute(String fileName) {
		InputStream input = this.getClass().getClassLoader().getResourceAsStream(fileName);
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

	public void testGetNextCoursePointIndex() {
		fail("Not yet implemented");
	}

	public void testIsOnCourse() {
		fail("Not yet implemented");
	}

}
