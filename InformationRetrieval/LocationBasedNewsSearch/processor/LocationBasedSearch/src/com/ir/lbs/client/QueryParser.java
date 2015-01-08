package com.ir.lbs.client;

import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import com.ir.lbs.client.geonames.Style;
import com.ir.lbs.client.geonames.Toponym;
import com.ir.lbs.client.geonames.ToponymSearchCriteria;
import com.ir.lbs.client.geonames.ToponymSearchResult;
import com.ir.lbs.client.geonames.WebService;

public class QueryParser {
	
	private Double latitude = 0.0;
	private Double longitude = 0.0;
	private String location = "";
	private String county = "";
	private String state = "";
	private String country = "";
	private String continent = "";
	
	private final static String GEONAMES_USERNAME = "vzanpure";
	private final static String SOLR_BASE_URL = "http://localhost:8983/solr";
	
	
	public void parse(String keyWordQuery, String locationQuery, String distanceRange) {
		
		// Step 1: Call Geonames Webservices and fetch the latitude, longitude and hierarchy
		WebService.setUserName(GEONAMES_USERNAME);
		
		ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();
		searchCriteria.setQ(locationQuery);
		searchCriteria.setStyle(Style.MEDIUM);
		searchCriteria.setMaxRows(1); // We only retrieve the first result and use it...
		
		SolrServer solrServer = null;
		
		try {
			ToponymSearchResult result = WebService.search(searchCriteria);
			
			if (result.getTotalResultsCount() > 0) {
				
				Toponym locationToponym = result.getToponyms().get(0);
				
				setLocation(locationToponym.getName());
				setLatitude(locationToponym.getLatitude());
				setLongitude(locationToponym.getLongitude());
				
				List<Toponym> locationHierarchy = WebService.hierarchy(locationToponym.getGeoNameId(), GEONAMES_USERNAME, Style.MEDIUM);
				
				String tempLocationName = "";
				
				for (int index = 1; index < locationHierarchy.size(); index++) {
					tempLocationName = locationHierarchy.get(index).getName();
					
					if (index == 1) {
						setContinent(tempLocationName);
					} else if (index == 2) {
						setCountry(tempLocationName);
					} else if (index == 3) {
						setState(tempLocationName);
					} else if (index == 4) {
						setCounty(tempLocationName);
					} else {
						setLocation(tempLocationName);
					}
				}
				
				// Step 2: Setup Solr instance....
				solrServer = new HttpSolrServer(SOLR_BASE_URL);
				
				// Step 3: Formulate the query and execute it...
				SolrQuery solrQuery = new SolrQuery();
				
				solrQuery.set("q", keyWordQuery);
				solrQuery.set("pt", getLatitude() + ", " + getLongitude());
				
				if (null != distanceRange && !distanceRange.equals("")) {
					solrQuery.set("d", distanceRange);
				}
				
				solrQuery.set("sort", "geodist() + asc");
				solrQuery.set("fq", "{!geofilt}");
				
				QueryResponse response = solrServer.query(solrQuery);
				
				SolrDocumentList documentList = response.getResults();
				
				for (SolrDocument solrDocument : documentList) {
					System.out.println(solrDocument.getFieldValue("text"));
				}
				
				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (null != solrServer) {
				solrServer.shutdown();
			}
		}
		
	}


	public Double getLatitude() {
		return latitude;
	}


	public void setLatitude(double d) {
		this.latitude = d;
	}


	public Double getLongitude() {
		return longitude;
	}


	public void setLongitude(double d) {
		this.longitude = d;
	}


	public String getLocation() {
		return location;
	}


	public void setLocation(String location) {
		this.location = location;
	}


	public String getCounty() {
		return county;
	}


	public void setCounty(String county) {
		this.county = county;
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


	public String getContinent() {
		return continent;
	}


	public void setContinent(String continent) {
		this.continent = continent;
	}
}
