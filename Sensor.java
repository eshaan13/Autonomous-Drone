package uk.ac.ed.inf.aqmaps;

public class Sensor {
	
	private String location;
	private double battery;
	private String reading;
	private Position pos;
	private int sensorNumber;
	private String rgbValue;
	private String markerSymbol;
	private boolean visited;
	
	
	public String getLocation() {
		return location;
	}
	public double getBattery() {
		return battery;
	}
	public String getReading() {
		return reading;
	}
	public Position getPos() {
		return pos;
	}
	public void setPos(Position pos) {
		this.pos = pos;
	}
	public int getSensorNumber() {
		return sensorNumber; 
	}
	public void setSensorNumber(int sensorNumber) {
		this.sensorNumber = sensorNumber; 
	}
	public String getRgbValue() {
		return rgbValue;
	}
	public void setRgbValue(String rgbValue) {
		this.rgbValue = rgbValue;
	}
	public String getMarkerSymbol() {
		return markerSymbol;
	}
	public void setMarkerSymbol(String markerSymbol) {
		this.markerSymbol = markerSymbol;
	}
	public boolean isVisited() {
		return visited;
	}
	public void setVisited(boolean visited) {
		this.visited = visited;
	}
	/**
	 * Method to check if the drone is in range of the sensor or not
	 * @param p position of the drone
	 * @param sensor sensor to be read
	 * @return true if the drone is in range of the sensor, otherwise false
	 */
	boolean inRange(Position position) {
		if(position.distance(pos) < 0.0002)
			return true;
		else return false;
	}
}
