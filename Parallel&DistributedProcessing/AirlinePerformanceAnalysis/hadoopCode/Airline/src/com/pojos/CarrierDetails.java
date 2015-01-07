package com.pojos;

public class CarrierDetails {
	
	public CarrierDetails(String code, String name) {
		this.setCarrierCode(code);
		this.setCarrierName(name);
	}
	
	private String carrierCode;
	private String carrierName;
	
	public String getCarrierCode() {
		return carrierCode;
	}
	public void setCarrierCode(String carrierCode) {
		this.carrierCode = carrierCode;
	}
	
	public String getCarrierName() {
		return carrierName;
	}
	public void setCarrierName(String carrierName) {
		this.carrierName = carrierName;
	}
	
}
