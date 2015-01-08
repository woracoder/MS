package com.ir.lbs.shared;

public class LocationCoordinates {
	
	protected LocationCoordinates(String ln, String lt, Double lat, Double lon) {
		this.locName = ln;
		this.locType = lt;
		this.latitude = lat;
		this.longitude = lon;
	}
	
	private String locName = null;
	private String locType = null;
	private Double latitude = 0.0;
	private Double longitude = 0.0;
	
	public String getLocName() {
		return locName;
	}
	public void setLocName(String locName) {
		this.locName = locName;
	}
	public String getLocType() {
		return locType;
	}
	public void setLocType(String locType) {
		this.locType = locType;
	}
	public Double getLatitude() {
		return latitude;
	}
	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}
	public Double getLongitude() {
		return longitude;
	}
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}
	
}
