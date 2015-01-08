package processor.preprocessor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.geonames.Style;
import org.geonames.Toponym;
import org.geonames.WebService;
import org.xml.sax.InputSource;

public class WikinewsXmlPreprocessor {

	static List<String> loginNames = new ArrayList<String>();
	static {
		loginNames.add("vzanpure");
		loginNames.add("vzanpure0");
		loginNames.add("vzanpure1");
		loginNames.add("vzanpure2");
		loginNames.add("vzanpure3");
		loginNames.add("vzanpure4");
		loginNames.add("vzanpure5");
		loginNames.add("vzanpure6");
		loginNames.add("vzanpure7");
		loginNames.add("vzanpure8");
		loginNames.add("vzanpure9");
		loginNames.add("vzanpure10");
		loginNames.add("vzanpure11");
		loginNames.add("vzanpure12");
		loginNames.add("vzanpure13");
		loginNames.add("vzanpure14");
		loginNames.add("vzanpure15");
		loginNames.add("vzanpure16");
		loginNames.add("vzanpure17");
		loginNames.add("vzanpure18");
		loginNames.add("vzanpure19");
		loginNames.add("vzanpure20");
		loginNames.add("vzanpure21");
		loginNames.add("vzanpure22");
		loginNames.add("vzanpure23");
	}
	
	public static void main(String[] args) throws Exception {

		Long startTime = System.currentTimeMillis();

		//readXlAndObjs();
		
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();
		XmlParser xmlParserObj = new XmlParser();
		/*File file = new File("D:\\Softwares\\Softwares\\Programming\\IDE\\eclipse-jee-kepler-SR1-win32-x86_64\\workspace\\"
			+ "LocationBasedSearchIndexer\\resources\\text.xml");*/
		File file = new File("D:\\Softwares\\Softwares\\Programming\\IDE\\eclipse-jee-kepler-SR1-win32-x86_64\\workspace\\"
				+ "LocationBasedSearchIndexer\\resources\\timeRefinedCdataWikinewsDump.xml");
		InputStream inputStream = new FileInputStream(file);
		Reader reader = new InputStreamReader(inputStream, ApplicationConstants.UTF8_FORMAT);
		InputSource is = new InputSource(reader);
		is.setEncoding(ApplicationConstants.UTF8_FORMAT);
		saxParser.parse(is, xmlParserObj);
		System.out.println("Total time required in seconds = " + (System.currentTimeMillis()-startTime)/1000);
		System.exit(0);
	}

	@SuppressWarnings("unchecked")
	private static void readXlAndObjs() throws Exception {
		
		/*ObjectInputStream ois2 = new ObjectInputStream(new FileInputStream(new File("D:\\Softwares\\Softwares\\Programming\\IDE" +
		  	"\\eclipse-jee-kepler-SR1-win32-x86_64\\workspace\\LocationBasedSearchIndexer\\resources\\idLocMap.ser")));
		Map<String, Map<String, Integer>> idLocMap = new HashMap<String, Map<String,Integer>>();
		idLocMap = (Map<String, Map<String, Integer>>)ois2.readObject();
		ois2.close();
		
		Map<String, Map<String, Integer>> tmpIdLocMap = new HashMap<String, Map<String,Integer>>();
		
		for(Map.Entry<String, Map<String, Integer>> e : idLocMap.entrySet()) {
			for(Map.Entry<String, Integer> ie : e.getValue().entrySet()) {
				if(ie.getValue() == 0) {
					idLocMap.get(e.getKey()).put(ie.getKey(), 1);
				}
			}
		}
	
		for(Map.Entry<String, Map<String, Integer>> e : idLocMap.entrySet()) {
			Map<String, Integer> tmpMap = new HashMap<String, Integer>();
			String stm = "";
			for(Map.Entry<String, Integer> ie : e.getValue().entrySet()) {
				if(ie.getKey().contains("|")) {
					stm = ie.getKey().replaceAll("\\|", "").replaceAll("\\s+", " ").trim();
					if(tmpMap.containsKey(stm)) {
						tmpMap.put(stm, tmpMap.get(stm) + ie.getValue());
					} else {
						tmpMap.put(stm, ie.getValue());
					}
				} else {
					if(tmpMap.containsKey(ie.getKey())) {
						tmpMap.put(ie.getKey(), tmpMap.get(ie.getKey()) + ie.getValue());
					} else {
						tmpMap.put(ie.getKey(), ie.getValue());
					}
				}
			}
			tmpIdLocMap.put(e.getKey(), tmpMap);
		}
		
		ObjectOutputStream oo = new ObjectOutputStream(new FileOutputStream("refIdLocMap.ser"));
		oo.writeObject(tmpIdLocMap);
		oo.flush();
		oo.close();*/
		
		/*ObjectInputStream ois1 = new ObjectInputStream(new FileInputStream(new File("D:\\Softwares\\Softwares\\Programming\\IDE" +
		  	"\\eclipse-jee-kepler-SR1-win32-x86_64\\workspace\\LocationBasedSearchIndexer\\locs.ser")));
		Set<String> locSet = new HashSet<String>();
		locSet = (Set<String>)ois1.readObject();
		ois1.close();*/
		
		/*Set<String> refLocSet = new HashSet<String>();
		
		for(String s : locSet) {
			if(s.contains("|")) {
				refLocSet.add(s.replaceAll("\\|", "").replaceAll("\\s+", " ").trim());
			} else {
				refLocSet.add(s);
			}
		}
		
		for(String s: refLocSet) {
			if(s.contains("|")) {
				System.out.println("failed");
				System.exit(0);
			}
		}
		
		System.out.println("Passed");
		
		ObjectOutputStream oos1 = new ObjectOutputStream(new FileOutputStream("locs.ser"));
		oos1.writeObject(refLocSet);
		oos1.flush();
		oos1.close();
		
		System.exit(0);*/
		
		/*BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File("D:\\Softwares\\Softwares\\Programming\\IDE\\
		  	eclipse-jee-kepler-SR1-win32-x86_64\\workspace\\LocationBasedSearchIndexer\\resources\\refinedIdLocSheet.xlsx")));
		XSSFWorkbook wb = new XSSFWorkbook(bis);
		XSSFSheet idLocSheet = wb.getSheetAt(0);
		XSSFSheet locSheet = wb.getSheetAt(1);*/
		
		/*Map<String, GeocodeResponse> locGeocodeMap1 = new HashMap<String, GeocodeResponse>();
		int count = 0;
		for(String s : locSet) {
			if(loadGeocodeMap.containsKey(s)) {
				//do not hit web service as response has already been cached just put the cached response for cumulative serialization
				locGeocodeMap1.put(s, loadGeocodeMap.get(s));
			} else {
				final Geocoder geocoder = new Geocoder();
				GeocoderRequest geocoderRequest = new GeocoderRequestBuilder().setAddress(s).setLanguage("en").getGeocoderRequest();
				GeocodeResponse geocoderResponse = geocoder.geocode(geocoderRequest);
				Thread.sleep(1000);
				if(null != geocoderResponse) {
					if(null != geocoderResponse.getStatus()) {
						if(!geocoderResponse.getStatus().name().equals("OVER_QUERY_LIMIT")) {
							count++;
							locGeocodeMap1.put(s, geocoderResponse);
							if(count %100 == 0) {
								System.out.println("Completed " + count + " requests.");
							}
						} else {
							System.out.println("Limit exceeded " + count);
						}
					}
				}
			}
			//Thread.sleep(5000);
		}
		System.out.println(count);
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File("locGeocode.ser")));
		oos.writeObject(locGeocodeMap1);
		oos.flush();
		oos.close();
		Thread.sleep(5000);
		System.exit(0);*/
		
		/*int partitionNo = 0, count = 0;
		Map<String, GeocodeResponse> splitmap = new HashMap<String, GeocodeResponse>();
		//Code to split locations into groups of 1000
		for(Map.Entry<String, GeocodeResponse> e : locGeocodeMap.entrySet()) {
			count++;
			splitmap.put(e.getKey(), e.getValue());
			if(count%1000 == 0) {
				serializeSplitMap(splitmap, partitionNo);
				partitionNo++;
				splitmap.clear();
			}
		}
		serializeSplitMap(splitmap, partitionNo);
		Thread.sleep(5000);
		System.exit(0);*/
		
		/*Map<String, List<String>> locVsFmtdLocMap = new HashMap<String, List<String>>();
		
		for(Map.Entry<String, GeocodeResponse> gr :locGeocodeMap.entrySet()) {
			List<String> tmpList = new ArrayList<String>();
			for(int i=0; i<gr.getValue().getResults().size(); i++) {
				tmpList.add(gr.getValue().getResults().get(i).getFormattedAddress());
			}
			locVsFmtdLocMap.put(gr.getKey(), tmpList);
		}
		
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File("locAndFormattedLocMap.ser")));
		oos.writeObject(locVsFmtdLocMap);
		oos.flush();
		oos.close();
		
		Thread.sleep(2000);
		System.exit(0);*/
		
		/*ObjectInputStream obs = new ObjectInputStream(new FileInputStream(new File("D:\\Softwares\\Softwares\\Programming\\IDE" +
			  	"\\eclipse-jee-kepler-SR1-win32-x86_64\\workspace\\LocationBasedSearchIndexer\\locGeocode.ser")));
		Map<String, GeocodeResponse> locGeocodeMap = new HashMap<String, GeocodeResponse>();
		locGeocodeMap = (Map<String, GeocodeResponse>)obs.readObject();
		obs.close();*/
		
		ObjectInputStream oism = new ObjectInputStream(new FileInputStream(new File("D:\\Softwares\\Softwares\\Programming\\IDE" +
			  	"\\eclipse-jee-kepler-SR1-win32-x86_64\\workspace\\LocationBasedSearchIndexer\\geonameIdHierarchy.ser")));
		Map<String, List<LocationHierarchy>> loadLocHierarchyMap = new HashMap<String, List<LocationHierarchy>>();
		loadLocHierarchyMap = (Map<String, List<LocationHierarchy>>)oism.readObject();
		oism.close();
		
		ObjectInputStream oisl = new ObjectInputStream(new FileInputStream(new File("D:\\Softwares\\Softwares\\Programming\\IDE" +
			  	"\\eclipse-jee-kepler-SR1-win32-x86_64\\workspace\\LocationBasedSearchIndexer\\addressGeoname.ser")));
		Map<String, List<Integer>> loadLocGeoIdMap = new HashMap<String, List<Integer>>();
		loadLocGeoIdMap = (Map<String, List<Integer>>)oisl.readObject();
		oisl.close();
		
		Map<String, List<Integer>> addressGeonameIdMap = new HashMap<String, List<Integer>>();
		Map<Integer, List<LocationHierarchy>> geonameIdHierarchyMap = new HashMap<Integer, List<LocationHierarchy>>();
		
		int webServiceCalls=0, locListName=0, count=0;
		/*String formattedAddress = "";
		boolean isGeonameFailed = true;
		int noGeonameId=0;*/
		
		for(Map.Entry<String, List<Integer>> eid : loadLocGeoIdMap.entrySet()) {
			count++;
			if(count%100 == 0) {
				System.out.println("Working on request " + count + "out of 9000");
			}
			for(Integer i : eid.getValue()) {
				try {
					WebService.setUserName(loginNames.get(locListName));
					List<Toponym> locationHierarchy = WebService.hierarchy(i, loginNames.get(locListName), Style.MEDIUM);
					webServiceCalls++;
					List<LocationHierarchy> lhList = new ArrayList<LocationHierarchy>();
					for (int index = 0; index < locationHierarchy.size(); index++) {
						Toponym tmp = locationHierarchy.get(index);
						if (index == 0) {
							lhList.add(new LocationHierarchy(index, tmp.getName(), "earth", tmp.getLatitude(), tmp.getLongitude()));
						} else if (index == 1) {
							lhList.add(new LocationHierarchy(index, tmp.getName(), "continent", tmp.getLatitude(), tmp.getLongitude()));
						} else if (index == 2) {
							lhList.add(new LocationHierarchy(index, tmp.getName(), "country", tmp.getLatitude(), tmp.getLongitude()));
						} else if (index == 3) {
							lhList.add(new LocationHierarchy(index, tmp.getName(), "state", tmp.getLatitude(), tmp.getLongitude()));
						} else if (index == 4) {
							lhList.add(new LocationHierarchy(index, tmp.getName(), "county", tmp.getLatitude(), tmp.getLongitude()));
						} else if (index == 5){
							lhList.add(new LocationHierarchy(index, tmp.getName(), "city", tmp.getLatitude(), tmp.getLongitude()));
						}
					}
					geonameIdHierarchyMap.put(i, lhList);
				} catch(Exception ex) {
					writeCumulativelyToFile(addressGeonameIdMap, geonameIdHierarchyMap, locListName);
					ex.printStackTrace();
				}
			}
			if(webServiceCalls > 1950) {
				webServiceCalls = 0;
				writeCumulativelyToFile(addressGeonameIdMap, geonameIdHierarchyMap, locListName);
				locListName++;
			}
		}
		
		/*for(Map.Entry<String, GeocodeResponse> e : locGeocodeMap.entrySet()) {
			if(loadLocGeoIdMap.containsKey(e.getKey())) {
				addressGeonameIdMap.put(e.getKey(), loadLocGeoIdMap.get(e.getKey()));
				continue;
			}
			if(null != e.getValue() && null != e.getValue().getResults() && !e.getValue().getResults().isEmpty() && e.getValue().getResults().size()>0) {
				int size = e.getValue().getResults().size()>3 ? 3: e.getValue().getResults().size();
				for(int i=0; i<size; i++) {
					if(null != e.getValue().getResults().get(i).getFormattedAddress() 
							&& !e.getValue().getResults().get(i).getFormattedAddress().trim().equalsIgnoreCase("")) {
						formattedAddress = e.getValue().getResults().get(i).getFormattedAddress();
					} else {
						continue;
						//formattedAddress = e.getKey();
					}
					
					WebService.setUserName(loginNames.get(locListName));
					ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();
					searchCriteria.setQ(formattedAddress);
					searchCriteria.setStyle(Style.MEDIUM);
					
					try {
						ToponymSearchResult result = WebService.search(searchCriteria);
						webServiceCalls++;
						if (result.getTotalResultsCount() > 0) {
							isGeonameFailed = false;
							List<Integer> tmpGeocodeIdList = new ArrayList<Integer>();
							Toponym locationToponym = result.getToponyms().get(0);
							tmpGeocodeIdList.add(locationToponym.getGeoNameId());
							if(addressGeonameIdMap.containsKey(e.getKey())) {
								tmpGeocodeIdList.addAll(addressGeonameIdMap.get(e.getKey()));
								addressGeonameIdMap.put(e.getKey(), tmpGeocodeIdList);
							} else {
								addressGeonameIdMap.put(e.getKey(), tmpGeocodeIdList);
							}
							List<Toponym> locationHierarchy = WebService.hierarchy(locationToponym.getGeoNameId(), loginNames.get(locListName), Style.MEDIUM);
							webServiceCalls++;
							List<LocationHierarchy> lhList = new ArrayList<LocationHierarchy>();
							for (int index = 0; index < locationHierarchy.size(); index++) {
								Toponym tmp = locationHierarchy.get(index);
								if (index == 0) {
									lhList.add(new LocationHierarchy(index, tmp.getName(), "earth", tmp.getLatitude(), tmp.getLongitude()));
								} else if (index == 1) {
									lhList.add(new LocationHierarchy(index, tmp.getName(), "continent", tmp.getLatitude(), tmp.getLongitude()));
								} else if (index == 2) {
									lhList.add(new LocationHierarchy(index, tmp.getName(), "country", tmp.getLatitude(), tmp.getLongitude()));
								} else if (index == 3) {
									lhList.add(new LocationHierarchy(index, tmp.getName(), "state", tmp.getLatitude(), tmp.getLongitude()));
								} else if (index == 4) {
									lhList.add(new LocationHierarchy(index, tmp.getName(), "county", tmp.getLatitude(), tmp.getLongitude()));
								} else if (index == 5){
									lhList.add(new LocationHierarchy(index, tmp.getName(), "city", tmp.getLatitude(), tmp.getLongitude()));
								}
							}
							geonameIdHierarchyMap.put(locationToponym.getGeoNameId(), lhList);
						} else {
							System.out.println(formattedAddress);
						}
					} catch(Exception ex) {
						writeCumulativelyToFile(addressGeonameIdMap, geonameIdHierarchyMap, locListName);
						ex.printStackTrace();
					}
				}
				if(isGeonameFailed) {
					WebService.setUserName(loginNames.get(locListName));
					ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();
					searchCriteria.setQ(e.getKey());
					searchCriteria.setStyle(Style.MEDIUM);
					try {
						ToponymSearchResult result = WebService.search(searchCriteria);
						webServiceCalls++;
						if (result.getTotalResultsCount() > 0) {
							int siz = result.getTotalResultsCount()>3 ? 3: result.getTotalResultsCount();
							List<Integer> tmpGcidList = new ArrayList<Integer>();
							for(int i=0; i<siz; i++) {
								tmpGcidList.add(result.getToponyms().get(i).getGeoNameId());
							}
							addressGeonameIdMap.put(e.getKey(), tmpGcidList);
						} else {
							noGeonameId++;
						}
					} catch(Exception ex) {
						writeCumulativelyToFile(addressGeonameIdMap, geonameIdHierarchyMap, locListName);
						ex.printStackTrace();
					}
				}
			} else {
				System.out.println("Found blank formatted address for original location so using original location: " + e.getKey());
				WebService.setUserName(loginNames.get(locListName));
				ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();
				searchCriteria.setQ(e.getKey());
				searchCriteria.setStyle(Style.MEDIUM);
				try {
					ToponymSearchResult result = WebService.search(searchCriteria);
					webServiceCalls++;
					if (result.getTotalResultsCount() > 0) {
						int size = result.getTotalResultsCount()>3 ? 3: result.getTotalResultsCount();
						List<Integer> tmpGcidList = new ArrayList<Integer>();
						for(int i=0; i<size; i++) {
							tmpGcidList.add(result.getToponyms().get(i).getGeoNameId());
						}
						addressGeonameIdMap.put(e.getKey(), tmpGcidList);
					} else {
						noGeonameId++;
					}
				} catch(Exception ex) {
					writeCumulativelyToFile(addressGeonameIdMap, geonameIdHierarchyMap, locListName);
					ex.printStackTrace();
				}
			}
			
			if(webServiceCalls > 1950) {
				webServiceCalls = 0;
				writeCumulativelyToFile(addressGeonameIdMap, geonameIdHierarchyMap, locListName);
				locListName++;
			}
		}*/
		
		writeCumulativelyToFile(addressGeonameIdMap, geonameIdHierarchyMap, locListName);
		//System.out.println("No geoname id count = " + noGeonameId + " locations cached = " + addressGeonameIdMap.size());
		Thread.sleep(5000);
		System.exit(0);
	}

	/*private static void serializeSplitMap(
			Map<String, GeocodeResponse> splitmap, int partitionNo) throws FileNotFoundException, IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File("D:\\Softwares\\Softwares\\Programming\\IDE\\
			eclipse-jee-kepler-SR1-win32-x86_64\\workspace\\LocationBasedSearchIndexer\\resources\\locGeocode\\locGeocode_" + partitionNo +".ser")));
		oos.writeObject(splitmap);
		oos.flush();
		oos.close();
	}*/

	private static void writeCumulativelyToFile(
			Map<String, List<Integer>> addressGeonameIdMap,
			Map<Integer, List<LocationHierarchy>> geonameIdHierarchyMap, int locListName) throws FileNotFoundException, IOException {
		
		/*ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File("addressGeoname_" + locListName + ".ser")));
		oos.writeObject(addressGeonameIdMap);
		oos.flush();
		oos.close();*/
		
		ObjectOutputStream oos1 = new ObjectOutputStream(new FileOutputStream(new File("geonameIdHierarchy_" + locListName + ".ser")));
		oos1.writeObject(geonameIdHierarchyMap);
		oos1.flush();
		oos1.close();
		
	}
}
