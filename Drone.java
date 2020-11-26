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
				nextPos = makeMove(currentPos, closestSensorPos, null);
				if(insideNoFlyZone(nextPos)) {
	//				TODO : Algo to find a new path ignoring the no fly zones
					System.out.println("Drone inside No Fly Zone");
				}
				if(!nextPos.insideDroneConfinementZone(droneConfinementZone)) {
	//				TODO : Change the direction towards inside the DCZ
					System.out.println("Drone outside Drone Confinement Zone");
				}
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
			
			Position nextPos = makeMove(currentPos, 0, target);
			if(insideNoFlyZone(nextPos)) {
	//			TODO : Algo to find a new path ignoring the no fly zones
				System.out.println("Drone inside No Fly Zone");
			}
			if(!nextPos.insideDroneConfinementZone(droneConfinementZone)) {
	//			TODO : Change the direction towards inside the DCZ
				System.out.println("Drone outside Drone Confinement Zone");
			}
			currentPos = nextPos;
			moves.add(currentPos);
			++ movesCount;
		}
	}

	protected void readSensor(int closestSensorPos) {
		exploredSensors.add(closestSensorPos);
	}
	
	protected Position makeMove(Position currentPos, int closestSensorPos, Position target) {
		
		Position destination;
		if(target == null)
			destination = sensors.get(closestSensorPos).getPos();
		else destination = target;
			
		double direction = currentPos.findDirection(destination);
		double radianAngle = Math.toRadians(direction);
		
		// Next position in the direction of the closest sensor, distance of 0.0003 degrees
		Position nextPos = new Position(currentPos.getLng() + 0.0003 * Math.cos(radianAngle), 
				currentPos.getLat() + 0.0003 * Math.sin(radianAngle));
		return nextPos;
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
}
