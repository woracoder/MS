package processor.preprocessor;

import java.io.Serializable;

public class LocationHierarchy implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected LocationHierarchy(int i, String ln, String lt, Double lat, Double lon) {
		setIndex(i);
		setLocName(ln);
		setLocType(lt);
		setLatitude(lat);
		setLongitude(lon);
	}

	private int index = 0;
	private String locName = null;
	private String locType = null;
	private Double latitude = 0.0;
	private Double longitude = 0.0;
	
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
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
