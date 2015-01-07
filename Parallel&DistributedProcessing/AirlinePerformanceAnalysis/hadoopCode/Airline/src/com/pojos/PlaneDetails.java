package com.pojos;

import java.util.Date;

public class PlaneDetails {

	public PlaneDetails(String tailNo, String typ, String manufactur,
			Date isueDt, String mod, String stat, String aircraftTyp,
			String engineTye, String yr) {
		this.setTailNum(tailNo);
		this.setType(typ);
		this.setManufacturer(manufactur);
		this.setIssueDate(isueDt);
		this.setModel(mod);
		this.setStatus(stat);
		this.setAircraftType(aircraftTyp);
		this.setEngineType(engineTye);
		this.setYear(yr);
	}

	private String tailNum;
	private String type;
	private String manufacturer;
	private Date issueDate;
	private String model;
	private String status;
	private String aircraftType;
	private String engineType;
	private String year;

	public String getTailNum() {
		return tailNum;
	}

	public void setTailNum(String tailNum) {
		this.tailNum = tailNum;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}

	public Date getIssueDate() {
		return issueDate;
	}

	public void setIssueDate(Date issueDate) {
		this.issueDate = issueDate;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getAircraftType() {
		return aircraftType;
	}

	public void setAircraftType(String aircraftType) {
		this.aircraftType = aircraftType;
	}

	public String getEngineType() {
		return engineType;
	}

	public void setEngineType(String engineType) {
		this.engineType = engineType;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

}
