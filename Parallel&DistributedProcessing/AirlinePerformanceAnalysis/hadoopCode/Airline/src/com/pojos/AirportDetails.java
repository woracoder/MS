package com.pojos;

public class AirportDetails {
	
	public AirportDetails(String code, String name, String cityName, String stateName, String countryName, double lat, double longi) {
		this.setIataCode(code);
		this.setAirportName(name);
		this.setCity(cityName);
		this.setState(stateName);
		this.setCountry(countryName);
		this.setLatitude(lat);
		this.setLongitude(longi);
	}

	private String iataCode;
	private String airportName;
	private String city;
	private String state;
	private String country;
	private double latitude;
	private double longitude;
	
	public String getIataCode() {
		return iataCode;
	}
	public void setIataCode(String iataCode) {
		this.iataCode = iataCode;
	}
	
	public String getAirportName() {
		return airportName;
	}
	public void setAirportName(String airportName) {
		this.airportName = airportName;
	}
	
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
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
