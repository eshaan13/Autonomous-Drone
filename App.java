package uk.ac.ed.inf.heatmap;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.*;

/**
 * The Class App.
 */
public class App
{
	
	// Class Variables
	
	
	/** This private variable gets the values read from the predictions file.*/
	final static int predictions[][] = new int[10][10];// 2D array for predictions
	
	/** GeoJSON points of the Drone Confinement Zone. */
	final private  static Point Forrest_Hill = Point.fromLngLat(-3.192473, 55.946233);
	final private static Point KFC = Point.fromLngLat(-3.184319, 55.946233);
	final private static Point Buccleuch_St_bus_stop = Point.fromLngLat(-3.184319, 55.942617);
	final private static Point Meadows_Top = Point.fromLngLat(-3.192473, 55.942617);
	
	/** dd/mm/yyyy when the drone has to reads the sensors */
	private static String day;
	private static String month;
	private static String year;
	
	/** Starting position of the drone */
	private static Point startingPosition;
	
	/** GeoJSON features of No Fly Zone Buildings */
	private static List<Feature> noFlyZoneBuildings = new ArrayList<Feature>();
	
	/** GeoJSON polygons of No Fly Zone Buildings */
	private static Polygon AT;
	private static Polygon INF;
	private static Polygon DHT;
	private static Polygon LIB;
	
	/** Air Quality Data read from the sensors */
	private static List<AirQualityData> aqdList = new ArrayList<AirQualityData>();

	/** List of sensors to be read */
	private static List<Point> sensors = new ArrayList<Point>();
	
	
	// Methods
	
	/**
	 * Constructor to initialise the date and the starting position of the drone
	 * @param Date and starting position of the drone
	 */
	public App(String day, String mnth, String yr, Point startPos) {
		this.day = day;
		this.month = mnth;
		this.year = yr;
		this.startingPosition = startPos;
	}
	
	/**
	 * Method to return the RGB value for the reading
	 * @param reading reading of the sensor
	 * @return RGB string for that sensor
	 */
	protected String rgbValue(int reading) {
		if(reading >= 0 && reading < 32) // Green
			return "#00ff00";
		else if(reading >= 32 && reading < 64) // Medium Green
			return "#40ff00";
		else if(reading >= 64 && reading < 96) // Light Green
			return "#80ff00";
		else if(reading >= 96 && reading < 128) // Lime Green
			return "#c0ff00";
		else if(reading >= 128 && reading < 160) // Gold
			return "#ffc000";
		else if(reading >= 160 && reading < 192) // Orange
			return "#ff8000";
		else if(reading >= 192 && reading < 224) // Red / Orange
			return "#ff4000";
		else if(reading >= 224 && reading < 256) // Red
			return "#ff0000";
		return null;
	}
	
	/**
	 * Method to find the distance between 2 given points using the Pythagorean
	 * distance = SQRT((x2-x1)^2+(y2-y1)^2))
	 * @param p1 First point
	 * @param p2 Second point
	 * @return Distance between the 2 points 
	 */
	protected static double distance(Point p1, Point p2) {
		return Math.sqrt(Math.pow(p2.latitude() - p1.latitude(), 2) + Math.pow(p2.longitude() - p1.longitude(), 2));
	}
	
	/**
	 * Method to set up a connection with the HTTP Server.
	 * @param urlString Address of the file to be read
	 * @return response received from the server
	 * @throws IOException
	 * @throws InterruptedException
	 */
	protected static Object setConnection(String urlString) throws IOException, InterruptedException {
		final HttpClient client = HttpClient.newHttpClient();
		var request = HttpRequest.newBuilder().uri(URI.create(urlString)).build();
		var response = client.send(request, BodyHandlers.ofString());
		return response.body();
	}
	
	/**
	 * Method to read the No FlyZone file and initialise the global variable of buildings.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	protected static void noFlyZoneBuildings() throws IOException, InterruptedException {

		// Address of geoJson file of No Fly Zone Buildings
		String urlString = "http://localhost:80/buildings/no-fly-zones.geojson";
		var response = setConnection(urlString);
		FeatureCollection fc = FeatureCollection.fromJson(response.toString());
		var noFlyZoneBuildings = fc.features();
		AT = (Polygon) noFlyZoneBuildings.get(0).geometry();
		DHT = (Polygon) noFlyZoneBuildings.get(1).geometry();
		LIB = (Polygon) noFlyZoneBuildings.get(2).geometry();
		INF = (Polygon) noFlyZoneBuildings.get(3).geometry();
	}
	
	
	/**
	 * Method to read the JSON file for Air Quality Data
	 * @throws IOException
	 * @throws InterruptedException
	 */
	protected static void airQualityData() throws IOException, InterruptedException {
		String urlString = "http://localhost:80/maps/" + year + "/" + month + "/" + day + "/air-quality-data.json";
		var response = setConnection(urlString);
		var listType = new TypeToken<ArrayList<AirQualityData>>() {}.getType();
		aqdList = new Gson().fromJson(response.toString(), listType);
	}
	
	/**
	 * Method to read the JSON file for What Three Word
	 * @param words location of the sensor in what three word format
	 * @return an instance of what three word
	 * @throws IOException
	 * @throws InterruptedException
	 */
	protected static WhatThreeWord whatThreeWord(String loc) throws IOException, InterruptedException {
		String words[] = loc.split("\\.", 3);
		String urlString = "http://localhost:80/words/" + words[0] + "/" + words[1] + "/" + words[2] + "/details.json";
		var response = setConnection(urlString);
		var wtw = new Gson().fromJson(response.toString(), WhatThreeWord.class);
		return wtw;
	}
	
	/**
	 * Method to get the list of sensors to be read
	 * @throws IOException
	 * @throws InterruptedException
	 */
	protected static void getSensors() throws IOException, InterruptedException {
		airQualityData(); // Read the air quality file for that day.
		for(AirQualityData aqd: aqdList) {
			String loc = aqd.getLocation();
			var wtw = whatThreeWord(loc);
			Point sensor = Point.fromLngLat(wtw.getCoordinates().getLongitude(), wtw.getCoordinates().getLatitude());
			sensors.add(sensor);
		}
	}
	
	/**
	 * Method to check if the next positon of the drone is inside any of the No Fly Zone Buildings
	 * @param pt next position of the drone
	 * @return true if it is inside no fly zone, false otherwise
	 */
	protected Boolean insideNoFlyZone(Point pt) {
		boolean result_AT =  containPoint(pt, AT.coordinates().get(0));
		boolean result_DHT =  containPoint(pt, DHT.coordinates().get(0));
		boolean result_LIB =  containPoint(pt, LIB.coordinates().get(0));	
		boolean result_INF =  containPoint(pt, INF.coordinates().get(0));
		
		return result_AT || result_INF || result_DHT || result_LIB;
	}
	
	/**
     * Check if the given point is inside the boundary by using the ray intersection algorithm.
     * @param test The point to check
     * @return true if the point is inside the boundary, false otherwise
     */
    protected static boolean containPoint(Point test, List<Point> boundaries) {
    	var points = new ArrayList<Point>();
    	for(int i = 0; i < boundaries.size()-1; ++i)
    		points.add(boundaries.get(i));
    	
    	int i;
    	int j;
    	boolean result = false;
    	for (i = 0, j = points.size() - 1; i < points.size(); j = i++) {
    		Point pt1 = points.get(i);
    		Point pt2 = points.get(j);
        if ((pt1.latitude() > test.latitude()) != (pt2.latitude() > test.latitude()) && (test.longitude() < (pt2.longitude() - pt1.longitude()) * (test.latitude() - pt1.latitude()) / (pt2.latitude() - pt1.latitude()) + pt1.longitude())) {
          result = !result;
         }
    	}
    	return result;
    }

    /**
	 * Method to check if the next position of the drone is inside the Drone Confinement Zone
	 * @param pt next position of the drone
	 * @return true if the position is inside the Drone Confinement Zone, false otherwise
	 */
	protected static Boolean insideDoneConfinementZone(Point pt) {
		var dcz = new ArrayList<Point>();
		dcz.add(Forrest_Hill);
		dcz.add(KFC);
		dcz.add(Buccleuch_St_bus_stop);
		dcz.add(Meadows_Top);
		
		return containPoint(pt, dcz);
	}
	
	
	protected static void readSensors() {
		
	}
	
	
	
	/**
	 * Main method to instantiate the class App.java and call methods accordingly
	 * to read the sensors.
	 * @param args date and the starting position of the drone
	 * @throws IOException
	 * @throws InterruptedException 
	 */
    public static void main( String[] args ) throws IOException, InterruptedException {
    	
    	Point startingPoint = Point.fromLngLat(Double.parseDouble(args[4]), Double.parseDouble(args[3]));
    	App obj = new App(args[0], args[1], args[2], startingPoint);
    	
    	// Checking if the starting position of the drone is outside the Drone Confinement Zone
    	if(!obj.insideDoneConfinementZone(startingPoint)) {
    		System.out.println("Drone starting position outside Drone Confinement Zone.");
    		System.exit(0);
    	}
    	
    	obj.noFlyZoneBuildings();
    	obj.getSensors();
    	
    	
    	
    	
    	
    	
    	
//    	Point test_In_DCZ = Point.fromLngLat(-3.1867003440856934, 55.944345561848124);
//    	Point test_Out_DCZ = Point.fromLngLat(-3.188333138823509, 55.942615746202236);
//    	System.out.println(obj.insideDoneConfinementZone(test_In_DCZ));
    	
//    	Point test_AT = Point.fromLngLat(-3.1867003440856934, 55.944345561848124);
//    	Point test_DHT = Point.fromLngLat(-3.186507225036621, 55.94327009236843);
//    	Point test_LIB = Point.fromLngLat(-3.188985586166382, 55.942771400854795);
//    	Point test_INF = Point.fromLngLat(-3.187258243560791, 55.94510858632841);

//    	System.out.println("Final: " + (obj.insideNoFlyZone(test_INF)));
		
    	
//    	try {
//    	      File myObj = new File("heatmap.geojson");
//    	      myObj.createNewFile();
//    	    } catch (IOException e) {
//    	      System.out.println("An error occurred: " + e);
//    	      e.printStackTrace();
//    	    }
//    	
//    	
//    	try {
//	    		FileWriter fr = new FileWriter("heatmap.geojson");
//	        	fr.write(fc);
//	        	fr.close();
//    	    } catch (IOException e) {
//    	    	System.out.println("An error occurred: " + e);
//	    	      e.printStackTrace();
//    	    }  	
    }
}
