package com.example.mapbook;

public class Path {
	int id;
	int locationId;
	double latitude;
	double longitude;
	
	public Path(){
		
	}
	
	public Path(int id, int locationId, double longitude, double latitude){
		this.id = id;
		this.locationId = locationId;
		this.longitude = longitude;
		this.latitude = latitude;
	}
	
	public Path(int locationId, double longitude, double latitude){
		this.locationId = locationId;
		this.longitude = longitude;
		this.latitude = latitude;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	public int getLocationId() {
		return locationId;
	}

	public void setLocationId(int locationId) {
		this.locationId = locationId;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

}
