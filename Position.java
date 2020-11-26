package uk.ac.ed.inf.heatmap;

import java.util.List;
import com.mapbox.geojson.Point;

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
	protected double distance(Position pos) {
		return Math.sqrt(Math.pow(lat - pos.getLat(), 2) + Math.pow(lng - pos.getLng(), 2));
	}
	
	/**
	 * Method to find the closest sensor to the current position of the drone
	 * @param pos current position of the drone
	 * @return number of closest sensor
	 */
	protected int findClosestSensor(List<Sensor> sensors, List<Integer> exploredList) {
		
		double min_dist = Double.MAX_VALUE;
		int min_pos = 0;
		for(int i = 0; i < sensors.size(); ++i) {
			if(!exploredList.contains(i)) {
				Sensor sensor = sensors.get(i);
				double distance = distance(sensor.getPos());
				if(distance < min_dist) {
					min_dist = distance;
					min_pos = i;
				}
			}
		}
		return min_pos;
	}

	/**
     * Check if the given point is inside the boundary by using the ray intersection algorithm.
     * @param test The point to check
     * @return true if the point is inside the boundary, false otherwise
     */
    protected boolean containPoint(List<Point> points) {
    	
    	int i,j;
    	boolean result = false;
    	for (i = 0, j = points.size() - 1; i < points.size(); j = i++) {
    		Point pt1 = points.get(i);
    		Point pt2 = points.get(j);
        if ((pt1.latitude() > lat) != (pt2.latitude() > lat) && (lng < (pt2.longitude() - pt1.longitude()) * (lat - pt1.latitude()) / (pt2.latitude() - pt1.latitude()) + pt1.longitude())) {
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
	protected boolean insideDroneConfinementZone(List <Point> droneConfinementZone) {
		boolean result = containPoint(droneConfinementZone);
		return result;
	}
	
	protected double findDirection(Position pos) {
    	double direction = Math.toDegrees(Math.atan2((pos.getLat() - lat), (pos.getLng() - lng)));
		if(direction < 0)
			direction += 360;
		direction = 10 * (Math.round(direction / 10.0));
		return direction;
	}
    
}
