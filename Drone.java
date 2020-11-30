package uk.ac.ed.inf.aqmaps;

import java.util.ArrayList;
import java.util.List;

import com.mapbox.geojson.*;
import com.mapbox.turf.TurfJoins;

public class Drone {
	
	private Position startingPosition;
	private List<Sensor> sensors = new ArrayList<Sensor>();
	private List<Polygon> noFlyZoneBuildings = new ArrayList<Polygon>();
	private List<Point> droneConfinementZone = new ArrayList<Point>();
	private List<Integer> exploredSensors = new ArrayList<Integer>();
	private List<Position> moves = new ArrayList<Position>();
	private int movesCount = 0;
	
	
	public Drone(Position startingPosition, List<Sensor> sensors, List<Polygon> noFlyZoneBuildings,
			List<Point> droneConfinementZone) {
		super();
		this.startingPosition = startingPosition;
		this.sensors = sensors;
		this.noFlyZoneBuildings = noFlyZoneBuildings;
		this.droneConfinementZone = droneConfinementZone;
	}
	
	public Position getStartingPosition() {
		return startingPosition;
	}

	public List<Sensor> getSensors() {
		return sensors;
	}

	public List<Polygon> getNoFlyZoneBuildings() {
		return noFlyZoneBuildings;
	}

	public List<Point> getDroneConfinementZone() {
		return droneConfinementZone;
	}

	public List<Integer> getExploredSensors() {
		return exploredSensors;
	}

	public List<Position> getMoves() {
		return moves;
	}

	public int getMovesCount() {
		return movesCount;
	}

	List<Integer> traverseSensors() {
		var moveWhileReading = new ArrayList<Integer>();
		Position currentPos = startingPosition;
		moves.add(startingPosition);
		while(exploredSensors.size() < 33 && movesCount < 150) {
			int closestSensorNum = 0;
			Sensor closestSensor = null;
			Position nextPos = null;
			do {
				closestSensorNum =  currentPos.findClosestSensor(sensors, exploredSensors);
				closestSensor = sensors.get(closestSensorNum);
				nextPos = makeMove(currentPos, closestSensorNum, null);
				currentPos = nextPos;
				moves.add(currentPos);
				++ movesCount;
			} while(!closestSensor.inRange(currentPos) && movesCount < 150);
			if(closestSensor.inRange(currentPos)) {
				readSensor(closestSensorNum);
				moveWhileReading.add(movesCount); /* Storing the count of the move 
													when a sensor was read (used to generate output files) */
			}
		}
		return moveWhileReading;
	}
	
	void homeComing() {
		Position currentPos = moves.get(moves.size()-1);
		Position target = startingPosition;
		while(!(currentPos.distance(target) < 0.0003) && movesCount < 150) {
			Position nextPos = makeMove(currentPos, 0, target);
			currentPos = nextPos;
			moves.add(currentPos);
			++ movesCount;
		}
	}
	
	private Position makeMove(Position currentPos, int closestSensorNum, Position target) {
		Position destination;
		if(target == null)
			destination = sensors.get(closestSensorNum).getPos();
		else destination = target;
		double direction = currentPos.findDirection(destination);
		if(checkDroneCrossNFZ(currentPos, direction))
			direction = posInsideNFZ(currentPos, direction, closestSensorNum);
		double radianAngle = Math.toRadians(direction);
		Position nextPos = new Position(currentPos.getLng() + 0.0003 * Math.cos(radianAngle), 
				currentPos.getLat() + 0.0003 * Math.sin(radianAngle));
		return nextPos;
	}
	
	private boolean checkDroneCrossNFZ(Position currentPos, double direction) {
		double radianAngle = Math.toRadians(direction);
		Position nextPos = null;
		for(int i = 1; i <= 1000; ++i) {
			nextPos = new Position(currentPos.getLng() + (0.0000003 * i) * Math.cos(radianAngle), 
					currentPos.getLat() + (0.0000003 * i) * Math.sin(radianAngle));
			if(insideNoFlyZone(nextPos) || !nextPos.insideDroneConfinementZone(droneConfinementZone))
				return true;
		}
		return false;
	}

//	private double posInsideNFZ(Position currentPos, double direction) {
//		double directionNew = direction;
//		int c = 0;
//		do {
//			if(c > 5) {
//				directionNew = direction;
//				do {
//					directionNew = directionNew - 10;
//					directionNew = directionNew % 360;
//				} while(checkDroneCrossNFZ(currentPos, directionNew));
//			}
//			else {
//				++ c;
//				directionNew = directionNew + 10;
//				directionNew = directionNew % 360;
//			}
//		} while(checkDroneCrossNFZ(currentPos, directionNew));
//			
//		return directionNew;
//	}
	
	private double goRight(Position currentPos, double direction) {
		double directionNew = direction;
		do {
			directionNew = directionNew + 10;
			directionNew = directionNew % 360;
		} while(checkDroneCrossNFZ(currentPos, directionNew));
		return directionNew;
	}
	
	private double goLeft(Position currentPos, double direction) {
		double directionNew = direction;
		do {
			directionNew = directionNew - 10;
			directionNew = directionNew % 360;
		} while(checkDroneCrossNFZ(currentPos, directionNew));
		return directionNew;
	}
	
	private double posInsideNFZ(Position currentPos, double direction, int closestSensorNum) {
		if(goRightOrLeft(currentPos, closestSensorNum, movesCount, "+"))
			return goRight(currentPos, direction);
		else if(goRightOrLeft(currentPos, closestSensorNum, movesCount, "-"))
			return goLeft(currentPos, direction);
		return closestSensorNum;
	}
	
	private Boolean goRightOrLeft(Position currentPos, int closestSensorNum, int movesCountTemp, String sign) {
		double directionNew = currentPos.findDirection((sensors.get(closestSensorNum).getPos()));
		if(sensors.get(closestSensorNum).inRange(currentPos))
			return true;
		else if(movesCountTemp > 150)
			return false;
		
		while(checkDroneCrossNFZ(currentPos, directionNew)) {
			
			if(sign.equals("+"))
				directionNew = directionNew + 10;
			else if(sign.equals("-")) 
				directionNew = directionNew - 10;
			directionNew = directionNew % 360;
		}
		double radianAngle = Math.toRadians(directionNew);
		++ movesCountTemp;
		Position nextPos = new Position(currentPos.getLng() + 0.0003 * Math.cos(radianAngle), 
				currentPos.getLat() + 0.0003 * Math.sin(radianAngle));
		return goRightOrLeft(nextPos, closestSensorNum, movesCountTemp, sign);
	}
	
	/**
	 * Method to check if the next position of the drone is inside any of the No Fly Zone Buildings
	 * @param position of the drone
	 * @return true if it is inside no fly zone, false otherwise
	 */
	private Boolean insideNoFlyZone(Position pos) {
		Point p = Point.fromLngLat(pos.getLng(), pos.getLat());

//		boolean result_AT =  pos.containPoint(noFlyZoneBuildings.get(0).coordinates().get(0));
//		boolean result_DHT =  pos.containPoint(noFlyZoneBuildings.get(1).coordinates().get(0));
//		boolean result_LIB =  pos.containPoint(noFlyZoneBuildings.get(2).coordinates().get(0));	
//		boolean result_INF =  pos.containPoint(noFlyZoneBuildings.get(3).coordinates().get(0));
		
		boolean result_AT =  TurfJoins.inside(p, noFlyZoneBuildings.get(0));
		boolean result_DHT =  TurfJoins.inside(p, noFlyZoneBuildings.get(1));
		boolean result_LIB =  TurfJoins.inside(p, noFlyZoneBuildings.get(2));	
		boolean result_INF =  TurfJoins.inside(p, noFlyZoneBuildings.get(3));
		
		return result_AT || result_INF || result_DHT || result_LIB;
	}

	/**
	 * Method to read the sensor when the drone is in its range.
	 * @param closestSensorNum Number of the closest sensor to read
	 */
	private void readSensor(int closestSensorNum) {
		sensors.get(closestSensorNum).setVisited(true);
		exploredSensors.add(closestSensorNum);
	}
}