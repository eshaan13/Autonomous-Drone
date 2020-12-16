package uk.ac.ed.inf.aqmaps;

import java.util.List;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.turf.TurfJoins;

public class Position {
	
	private double lng;
	private double lat;
	
	public Position(double longitude, double latitude) {
		this.lng = longitude;
		this.lat = latitude;
	}
	
	
	public double getLng() {
		return lng;
	}
	
	public double getLat() {
		return lat;
	}
	
	/**
	 * Method to find the distance between 2 given points using the Pythagorean
	 * distance = SQRT((x2-x1)^2+(y2-y1)^2))
	 * @param p1 First point
	 * @param p2 Second point
	 * @return Distance between the 2 points 
	 */
	double distance(Position pos) {
		return Math.sqrt(Math.pow(lat - pos.getLat(), 2) + Math.pow(lng - pos.getLng(), 2));
	}
	
	/**
	 * Method to find the closest sensor to the current position of the drone
	 * @param pos current position of the drone
	 * @return number of closest sensor
	 */
	int findClosestSensor(List<Sensor> sensors, List<Integer> exploredList) {
		
		double min_dist = Double.MAX_VALUE;
		int min_pos = 0;
		// iterating over all the sensors to get the closest one
		for(int i = 0; i < sensors.size(); ++i) {
			// checking for the sensors that are not already visited
			if(!exploredList.contains(i)) {
				Sensor sensor = sensors.get(i);
				double distance = distance(sensor.getPos()); // distance to the sensor
				if(distance < min_dist) { // checking if the new distance is less than the previous distance
					min_dist = distance;
					min_pos = i;
				}
			}
		}
		return min_pos; // returning the number of the closest sensor
	}
    
    /**
	 * Method to check if the next position of the drone is inside the Drone Confinement Zone
	 * @param pt next position of the drone
	 * @return true if the position is inside the Drone Confinement Zone, false otherwise
	 */
	boolean insideDroneConfinementZone(List <Point> droneConfinementZone) { 
		Point p = Point.fromLngLat(lng, lat);
		// checking if the position is inside the drone confinement zone
		boolean result = TurfJoins.inside(p, Polygon.fromLngLats(List.of(droneConfinementZone)));
		return result;
	}
	
	/**
	 * Method to find direction towards the given position
	 * @param pos position
	 * @return direction of the given position
	 */
	double findDirection(Position pos) {
    	double direction = Math.toDegrees(Math.atan2((pos.getLat() - lat), (pos.getLng() - lng)));
		if(direction < 0) // incase the direction is less than 360 degrees
			direction += 360;
		// rounding of the direction to the nearest multiple of 10
		direction = 10 * (Math.round(direction / 10.0));
		return direction;
	}
   
}
