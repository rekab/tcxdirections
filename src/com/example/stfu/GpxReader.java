package com.example.stfu;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.example.stfu.model.CoursePoint;

import android.util.Log;


public class GpxReader {
	public static ArrayList<CoursePoint> getRoutePoints(File source) {
		ArrayList<CoursePoint> points = new ArrayList<CoursePoint>();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
			return null;
		}
		FileInputStream input;
		try {
			input = new FileInputStream(source);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
        Document dom;
		try {
			dom = builder.parse(input);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
        Element root = dom.getDocumentElement();
        NodeList items = root.getElementsByTagName("rte");
        
        for (int i = 0; i < items.getLength(); i++) {
        	Node item = items.item(i);
            NodeList children = item.getChildNodes();
            for (int j = 0 ; j < children.getLength(); j++) {
            	Node child = children.item(j);
            	if (child.getNodeName().equals("rtept")) {
            		CoursePoint point = getRoutePointFromNode(child);
            		points.add(point);
            	}
            }
        }
        return points;
	}

	private static CoursePoint getRoutePointFromNode(Node item) {
		NodeList children = item.getChildNodes();

		String name = null, desc = null;
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			String childName = child.getNodeName();
			if (childName.equals("name")) {
				name = child.getFirstChild().getNodeValue();
			} else if (childName.equals("cmt")) {
				desc = child.getFirstChild().getNodeValue();
			}
		}

		if (name == null) {
			name = "(undef)";
		}
		if (desc == null) {
			desc = "";
		}
		NamedNodeMap attrs = item.getAttributes();
		CoursePoint point = new CoursePoint("test", name, desc);
		point.setLatitude(Double.parseDouble(attrs.getNamedItem("lat").getTextContent()));
		point.setLongitude(Double.parseDouble(attrs.getNamedItem("lon").getTextContent()));
		return point;
	}

	public static ArrayList<CoursePoint> getTrackPoints(File source) throws FileNotFoundException {
		FileInputStream input;
		input = new FileInputStream(source);
		return getTrackPoints(input);
	}
		
	public static ArrayList<CoursePoint> getTrackPoints(InputStream input) {
		ArrayList<CoursePoint> points = new ArrayList<CoursePoint>();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
			return null;
		}
		Document dom;
		try {
			dom = builder.parse(input);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
        Element root = dom.getDocumentElement();
        NodeList items = root.getElementsByTagName("trkseg");
        
        for (int i = 0; i < items.getLength(); i++) {
        	Node item = items.item(i);
            NodeList children = item.getChildNodes();
            for (int j = 0 ; j < children.getLength(); j++) {
            	Node child = children.item(j);
            	if (child.getNodeName().equals("trkpt")) {
            		CoursePoint point = getRoutePointFromNode(child);
            		points.add(point);
            	}
            }
        }
        return points;
	}
}
