package uk.ac.ed.inf.aqmaps;

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
	
	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public Square getSquare() {
		return square;
	}

	public void setSquare(Square square) {
		this.square = square;
	}

	public String getNearestPlace() {
		return nearestPlace;
	}

	public void setNearestPlace(String nearestPlace) {
		this.nearestPlace = nearestPlace;
	}

	public String getWords() {
		return words;
	}

	public void setWords(String words) {
		this.words = words;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getMap() {
		return map;
	}

	public void setMap(String map) {
		this.map = map;
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
		public Coordinates getSouthwest() {
			return southwest;
		}
		public void setSouthwest(Coordinates southwest) {
			this.southwest = southwest;
		}
		public Coordinates getNortheast() {
			return northeast;
		}
		public void setNortheast(Coordinates northeast) {
			this.northeast = northeast;
		}
	}
}
