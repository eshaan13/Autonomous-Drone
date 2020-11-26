package uk.ac.ed.inf.heatmap;

import java.util.ArrayList;
import java.util.List;

import com.mapbox.geojson.*;

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

	protected void traverseSensors() {
		Position currentPos = startingPosition;
		moves.add(startingPosition);
		
		while(exploredSensors.size() < 33 && movesCount < 150) {
			
			int closestSensorPos = 0;
			Sensor closestSensor = null;
			Position nextPos = null;
			do {
				closestSensorPos =  currentPos.findClosestSensor(sensors, exploredSensors);
				closestSensor = sensors.get(closestSensorPos);
				nextPos = makeMove3(currentPos, closestSensorPos, null);
				currentPos = nextPos;
				moves.add(currentPos);
				++ movesCount;
				
			} while(!closestSensor.inRange(currentPos) && movesCount < 150);		
				readSensor(closestSensorPos);
		}
	}
	
	protected void homeComing() {
		
		Position currentPos = moves.get(moves.size()-1);
		Position target = startingPosition;
		
		while(!(currentPos.distance(target) < 0.0003) && movesCount < 150) {
			
			Position nextPos = makeMove3(currentPos, 0, target);
			currentPos = nextPos;
			moves.add(currentPos);
			++ movesCount;
		}
	}
	
	protected Position makeMove3(Position currentPos, int closestSensorPos, Position target) {
		
		Position destination;
		if(target == null)
			destination = sensors.get(closestSensorPos).getPos();
		else destination = target;
			
		double direction = currentPos.findDirection(destination);
		
		if(checkDroneCrossNFZ(currentPos, direction))
			direction = posInsideNFZ(currentPos, direction);
		
		double radianAngle = Math.toRadians(direction);
		Position nextPos = new Position(currentPos.getLng() + 0.0003 * Math.cos(radianAngle), 
				currentPos.getLat() + 0.0003 * Math.sin(radianAngle));
		
		return nextPos;
	}
	
	protected boolean checkDroneCrossNFZ(Position currentPos, double direction) {
		
		double radianAngle = Math.toRadians(direction);
		Position nextPos = null;
		for(int i = 1; i <= 3; ++i) {
			nextPos = new Position(currentPos.getLng() + (0.0001 * i) * Math.cos(radianAngle), 
					currentPos.getLat() + (0.0001 * i) * Math.sin(radianAngle));
			
			if(insideNoFlyZone(nextPos) || !nextPos.insideDroneConfinementZone(droneConfinementZone)) {
				return true;
			}
		}
		
		return false;
	}

	protected double posInsideNFZ(Position currentPos, double direction) {
		
		double directionNew = direction;
		
		do {
			directionNew = directionNew + 10;
		} while(checkDroneCrossNFZ(currentPos, directionNew));
		
		return directionNew;
	}
	
	/**
	 * Method to check if the next position of the drone is inside any of the No Fly Zone Buildings
	 * @param pt next position of the drone
	 * @return true if it is inside no fly zone, false otherwise
	 */
	protected Boolean insideNoFlyZone(Position pos) {
		boolean result_AT =  pos.containPoint(noFlyZoneBuildings.get(0).coordinates().get(0));
		boolean result_DHT =  pos.containPoint(noFlyZoneBuildings.get(1).coordinates().get(0));
		boolean result_LIB =  pos.containPoint(noFlyZoneBuildings.get(2).coordinates().get(0));	
		boolean result_INF =  pos.containPoint(noFlyZoneBuildings.get(3).coordinates().get(0));
		
		return result_AT || result_INF || result_DHT || result_LIB;
	}

	protected void readSensor(int closestSensorPos) {
		sensors.get(closestSensorPos).setVisited(true);
		exploredSensors.add(closestSensorPos);
	}
	
//	protected Position makeMove(Position currentPos, int closestSensorPos, Position target) {
//		
//		Position destination;
//		if(target == null)
//			destination = sensors.get(closestSensorPos).getPos();
//		else destination = target;
//			
//		double direction = currentPos.findDirection(destination);
//		double radianAngle = Math.toRadians(direction);
//		
//		// Next position in the direction of the closest sensor, distance of 0.0003 degrees
//		Position nextPos = new Position(currentPos.getLng() + 0.0003 * Math.cos(radianAngle), 
//				currentPos.getLat() + 0.0003 * Math.sin(radianAngle));
//		return nextPos;
//	}
	
//	protected Position makeMove2(Position currentPos, int closestSensorPos, Position target) {
//		
//		Position destination;
//		if(target == null)
//			destination = sensors.get(closestSensorPos).getPos();
//		else destination = target;
//			
//		double direction = currentPos.findDirection(destination);
//		double radianAngle = Math.toRadians(direction);
//		
//		// Next position in the direction of the closest sensor, distance of 0.0003 degrees
//		Position nextPos = new Position(currentPos.getLng() + 0.0003 * Math.cos(radianAngle), 
//				currentPos.getLat() + 0.0003 * Math.sin(radianAngle));
//		
//		if(insideNoFlyZone(nextPos)) {
//			nextPos = insideNFZ(currentPos, direction);
//		}
//		
//		return nextPos;
//	}
}
