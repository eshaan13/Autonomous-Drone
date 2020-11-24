package uk.ac.ed.inf.heatmap;

public class WhatThreeWord {
	
	private String country;
	private Square square;
	private String nearestPlace;
	private Coordinates coordinates;
	private String words;
	private String language;
	private String map;
	
	
	public Coordinates getCoordinates() {
		return coordinates;
	}
	
	public static class Coordinates {
		private double lng;
		private double lat;
		
		public double getLongitude() {
			return lng;
		}
		public double getLatitude() {
			return lat;
		}
	}

	public static class Square {
		private Coordinates southwest;
		private Coordinates northeast;
	}
}
