package processor.preprocessor;

import info.bliki.wiki.model.WikiModel;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;

public class XmlParser extends DefaultHandler {
	
	Map<String, Map<String, Integer>> idLocMap;
	Map<String, List<Integer>> locGeonameIdMap;
	Map<Integer, List<LocationHierarchy>> geonameIdHierarchyMap;
	
	@SuppressWarnings("unchecked")
	protected XmlParser() throws ClassNotFoundException, IOException {
		this.classifier = 
			CRFClassifier.getClassifierNoExceptions(
				"D:\\Softwares\\Softwares\\Programming\\IDE\\eclipse-jee-kepler-SR1-win32-x86_64"
				+ "\\workspace\\LocationBasedSearchIndexer\\resources\\english.all.3class.distsim.crf.ser.gz");
		
		ObjectInputStream oisIdLocMap = new ObjectInputStream(new FileInputStream(new File("D:\\Softwares\\Softwares\\Programming\\IDE" 
				+ "\\eclipse-jee-kepler-SR1-win32-x86_64\\workspace\\LocationBasedSearchIndexer\\idLocMap.ser")));
		idLocMap = new HashMap<String, Map<String,Integer>>();
		idLocMap = (Map<String, Map<String, Integer>>)oisIdLocMap.readObject();
		oisIdLocMap.close();
		
		ObjectInputStream oisLocGeonameId = new ObjectInputStream(new FileInputStream(new File("D:\\Softwares\\Softwares\\Programming\\IDE" 
				+ "\\eclipse-jee-kepler-SR1-win32-x86_64\\workspace\\LocationBasedSearchIndexer\\locGeonameId.ser")));
		locGeonameIdMap = new HashMap<String, List<Integer>>();
		locGeonameIdMap = (Map<String, List<Integer>>)oisLocGeonameId.readObject();
		oisLocGeonameId.close();
		
		ObjectInputStream oisgeonameIdHierarchy = new ObjectInputStream(new FileInputStream(new File("D:\\Softwares\\Softwares\\Programming\\IDE" 
				+ "\\eclipse-jee-kepler-SR1-win32-x86_64\\workspace\\LocationBasedSearchIndexer\\geonameIdHierarchy.ser")));
		geonameIdHierarchyMap = new HashMap<Integer, List<LocationHierarchy>>();
		geonameIdHierarchyMap = (Map<Integer, List<LocationHierarchy>>)oisgeonameIdHierarchy.readObject();
		oisgeonameIdHierarchy.close();
	}
	
	//private final static String GEONAMES_USERNAME = "vzanpure";
	
	boolean page = false;
	boolean title = false;
	boolean id = false;
	boolean revision = false;
	boolean timestamp = false;
	boolean text = false;
	boolean isLocArticle = false;
	
	Set<String> locSet = new HashSet<String>();
	AbstractSequenceClassifier<CoreLabel> classifier;
	
	//Pattern p = Pattern.compile("\\{\\{date\\|([\\s|\\S]*?)\\}\\}");
	
	int idFromXml, noOfArticles = 0, noOfLocArticles = 0, writeCount = 1;
	String titleFromXml, timestampFromXml, textFromXml, textToCheckForLocation;
	StringBuilder textFromXmlSb = new StringBuilder();
	StringBuilder titleFromXmlSb = new StringBuilder();
	StringBuilder idFromXmlSb = new StringBuilder();
	StringBuilder timestampFromXmlSb = new StringBuilder();
	StringBuilder textToCheckForLocationSb = new StringBuilder();

	/**
	 * XML processing starts here. Blank URI & localName as namespaces are not
	 * being processed. Qualified name is the name of the tags encountered.
	 * Attribute object populated if element has any attributes.
	 */
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

		if (!page && qName.equalsIgnoreCase(ApplicationConstants.PAGE_TAG)) {
			page = true;
		} else if (qName.equalsIgnoreCase(ApplicationConstants.TITLE_TAG)) {
			title = true;
		//} else if (!revision && qName.equalsIgnoreCase(ApplicationConstants.ID_TAG)) {
		} else if (qName.equalsIgnoreCase(ApplicationConstants.ID_TAG)) {
			id = true;
		} /*else if (qName.equalsIgnoreCase(ApplicationConstants.REVISION_TAG)) {
			revision = true;
		}*/ else if (qName.equalsIgnoreCase(ApplicationConstants.TIMESTAMP_TAG)) {
			timestamp = true;
		} else if (qName.equalsIgnoreCase(ApplicationConstants.TEXT_TAG)) {
			text = true;
		}
	}

	/**
	 * ch is array of characters found inside element
	 */
	public void characters(char ch[], int start, int length) throws SAXException {

		if (page) {
			if (title) {
				titleFromXmlSb = titleFromXmlSb.append(new String(ch, start, length));
			//} else if (!revision && id) {
			} else if (id) {
				idFromXmlSb = idFromXmlSb.append(new String(ch, start, length));
			} else if (timestamp) {
				timestampFromXmlSb = timestampFromXmlSb.append(new String(ch, start, length));
			} else if (text) {
				textFromXmlSb = textFromXmlSb.append(new String(ch, start, length));
			}
		}
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		
		if (text && qName.equalsIgnoreCase(ApplicationConstants.TEXT_TAG)) {
			textFromXml = textFromXmlSb.toString();
			text = false;
		} else if (timestamp) {
			timestampFromXml = timestampFromXmlSb.toString();
			timestamp = false;
		/*} else if (!revision && id) {*/
		} else if (id) {
			idFromXml = Integer.parseInt(idFromXmlSb.toString());
			id = false;
		} else if (title) {
			titleFromXml = titleFromXmlSb.toString();
			title = false;
		} else if (page && qName.equalsIgnoreCase(ApplicationConstants.PAGE_TAG)) {
			
			noOfArticles++;
			if(noOfArticles%100 == 0) {
				System.out.println("Currently working on page number: " + noOfArticles + " out of 17000 pages.");
			}
			
			textToCheckForLocationSb.append(titleFromXml).append(" ").append(textFromXml);
			textToCheckForLocation =  textToCheckForLocationSb.toString();
			
			//writeTextToFile(textToCheckForLocation, titleFromXml, Integer.toString(idFromXml), timestampFromXml, textFromXml);
			writeFinalXml(titleFromXml, Integer.toString(idFromXml), timestampFromXml, textFromXml);
			
			/*if(textFromXml.startsWith("{{date|")) {
				Matcher m = p.matcher(textFromXml.substring(0, 40));
				if(m.find()) {
					noOfLocArticles++;
					timestampFromXml = m.group(1);
					StringBuilder data = new StringBuilder();
					data.append("<page>\n<id>" + idFromXml + "</id>\n<title>" + titleFromXml + "</title>\n<timestamp>" + timestampFromXml + 
							"</timestamp>\n<text>" + textFromXml + "</text>\n</page>\n\n");
					
					BufferedWriter bw;
					File f = new File("timeRefinedWikinewsDump.xml");
					try {
						if(!f.exists()) {
							f.createNewFile();	
						} 
						bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f, true), "UTF-8"));
						bw.write(data.toString());
						bw.flush();
						bw.close();
					} catch(Exception ex) {
						ex.printStackTrace();
					}
				}
			}*/
			
			textFromXmlSb.setLength(0);
			timestampFromXmlSb.setLength(0);
			idFromXmlSb.setLength(0);
			titleFromXmlSb.setLength(0);
			textToCheckForLocationSb.setLength(0);
			
			page = false;
			//revision = false;
		}
	}

	private void writeFinalXml(String titleFromXml2, String idFromXml2, String timestampFromXml2, String textFromXml2) {
		
		StringBuilder data = new StringBuilder();
		
		String rawLocs = generateRawLocationTags(idFromXml2);
		String latLon = generateLatLonForLocs(idFromXml2);
		
		String htmlText = WikiModel.toHtml(textFromXml2);
		htmlText = htmlText.replaceAll("\\{\\{[\\s|\\S]*?\\}\\}", "");
		
		data.append("<page>\n<id>" + idFromXml + "</id>\n<timestamp>" + timestampFromXml + "</timestamp>\n"
				+ "<title>" + titleFromXml + "</title>\n<text>" + textFromXml + "</text>\n"
				+ "<htmltext><![CDATA[" + htmlText + "]]></htmltext>\n" + rawLocs + latLon + "</page>\n\n");
		
		BufferedWriter bw;
		File f = new File("finalIndexableDump.xml");
		try {
			if(!f.exists()) {
				f.createNewFile();	
			} 
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f, true), "UTF-8"));
			bw.write(data.toString());
			bw.flush();
			bw.close();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		
	}

	private String generateLatLonForLocs(String idFromXml2) {
		Set<String> latLonSet = new HashSet<String>();
		Set<String> hierSet = new HashSet<String>();
		if(idLocMap.containsKey(idFromXml2)) {
			for(String s: idLocMap.get(idFromXml2).keySet()) {
				if(null != s && !s.trim().equalsIgnoreCase("")) {
					if(locGeonameIdMap.containsKey(s)) {
						for(Integer i : locGeonameIdMap.get(s)) {
							if(geonameIdHierarchyMap.containsKey(i)) {
								if(null != geonameIdHierarchyMap.get(i) && !geonameIdHierarchyMap.get(i).isEmpty() && geonameIdHierarchyMap.get(i).size()>0) {
									String continent = "", country = "", state = "", county = "", city = "";
									for(LocationHierarchy lh : geonameIdHierarchyMap.get(i)) {
										if(lh.getIndex() == 0) {
											latLonSet.add("<earth_location>" + lh.getLatitude() + "," + lh.getLongitude() + "</earth_location>");
											hierSet.add("0\\earth ");
										} else if(lh.getIndex() == 1) {
											latLonSet.add("<continent_location>" + lh.getLatitude() + "," + lh.getLongitude() + "</continent_location>");
											continent = lh.getLocName();
											hierSet.add("1\\earth\\"+lh.getLocName()+" ");
										} else if(lh.getIndex() == 2) {
											latLonSet.add("<country_location>" + lh.getLatitude() + "," + lh.getLongitude() + "</country_location>");
											country = lh.getLocName();
											hierSet.add("2\\earth\\"+continent+"\\"+lh.getLocName()+" ");
										} else if(lh.getIndex() == 3) {
											latLonSet.add("<state_location>" + lh.getLatitude() + "," + lh.getLongitude() + "</state_location>");
											state = lh.getLocName();
											hierSet.add("3\\earth\\"+continent+"\\"+country+"\\"+lh.getLocName()+" ");
										} else if(lh.getIndex() == 4) {
											latLonSet.add("<county_location>" + lh.getLatitude() + "," + lh.getLongitude() + "</county_location>");
											county = lh.getLocName();
											hierSet.add("4\\earth\\"+continent+"\\"+country+"\\"+state+"\\"+lh.getLocName()+" ");
										} else if(lh.getIndex() == 5) {
											latLonSet.add("<city_location>" + lh.getLatitude() + "," + lh.getLongitude() + "</city_location>");
											city = lh.getLocName();
											hierSet.add("5\\earth\\"+continent+"\\"+country+"\\"+state+"\\"+county+"\\"+lh.getLocName()+" ");
										}
									}
								}
							}
						}
					}
				}
			}
		}
		
		StringBuilder data = new StringBuilder("");
		for(String s : latLonSet) {
			if(null != s && !s.trim().equalsIgnoreCase("")) {
				data.append(s+"\n");
			}
		}
		data.append("<location_hierarchy><![CDATA[");
		for(String s : hierSet) {
			if(null != s && !s.trim().equalsIgnoreCase("")) {
				data.append(s+"|@#");
			}
		}
		data.append("]]></location_hierarchy>\n");
		
		return data.toString();
	}

	private String generateRawLocationTags(String idFromXml2) {
		StringBuilder data = new StringBuilder("");
		if(idLocMap.containsKey(idFromXml2)) {
			for(String s : idLocMap.get(idFromXml2).keySet()) {
				if(null != s && !s.trim().equalsIgnoreCase("")) {
					data.append("<location>" + s + "</location>\n");
				}
			}
		}
		return data.toString();
	}

	private void checkIfLocationArticle() {
		try {
			List<List<CoreLabel>> out = classifier.classify(textToCheckForLocation);
		    for (List<CoreLabel> sentence : out) {
		      for (CoreLabel word : sentence) {
		    	  if(word.get(CoreAnnotations.AnswerAnnotation.class).equalsIgnoreCase("LOCATION")) {
		    		  isLocArticle = true;
		    		  break;
		    	  }
		      }
		      if(isLocArticle) {
		    	  noOfLocArticles++;
		    	  writeTextToFile(textToCheckForLocation, titleFromXml, Integer.toString(idFromXml), timestampFromXml, textFromXml);
		    	  isLocArticle = false;
		    	  break;
		      }
		    }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void writeTextToFile(String textToCheckForLocation2, String titleFromXml2,
			String idFromXml2, String timestampFromXml2, String textFromXml2) throws IOException {

		boolean wasPreviousLoc = false;
		StringBuilder prevWord = new StringBuilder();
		Set<String> tempLocs = new HashSet<String>();
		List<List<CoreLabel>> out = classifier.classify(textToCheckForLocation2.toString());
		for (List<CoreLabel> sentence : out) {
			for (CoreLabel word : sentence) {
				if (word.get(CoreAnnotations.AnswerAnnotation.class).equalsIgnoreCase("LOCATION")) {
					if(!word.word().equalsIgnoreCase("|")) {
						if(wasPreviousLoc) {
							if(word.word().equalsIgnoreCase("'s")) {
								tempLocs.add(prevWord.toString() + word.word());
							} else {
								tempLocs.add(prevWord.toString() + " " + word.word());
							}
							tempLocs.remove(prevWord.toString());
						} else {
							tempLocs.add(word.word());
						}
						wasPreviousLoc = true;
						//prevWord.setLength(0);
						if(prevWord.length()>0) {
							if(word.word().equalsIgnoreCase("'s")) {
								prevWord.append(word.word());
							} else {
								prevWord.append(" " + word.word());
							}
						}
						else {
							prevWord.append(word.word());
						}
					} else {
						wasPreviousLoc = false;
						prevWord.setLength(0);
					}
				} else {
					wasPreviousLoc = false;
					prevWord.setLength(0);
				}
			}
		}
		
		Map<String, Integer> locCnt = new HashMap<String, Integer>();
		StringBuilder locDetails = new StringBuilder("");
		StringBuilder csvLocs = new StringBuilder("");
		int matches = 0;
		Set<String> refTmpLocs = new HashSet<String>();
		for(String s : tempLocs) {
			matches = StringUtils.countMatches(textToCheckForLocation2, s);
			if(matches > 0) {
				locCnt.put(s, matches);
				csvLocs.append(s + ", ");
				refTmpLocs.add(s);
			}
			/*String formattedAddress = "";
			final Geocoder geocoder = new Geocoder();
			GeocoderRequest geocoderRequest = new GeocoderRequestBuilder().setAddress(s).setLanguage("en").getGeocoderRequest();
			GeocodeResponse geocoderResponse = geocoder.geocode(geocoderRequest);
			
			if(null != geocoderResponse.getResults() && !geocoderResponse.getResults().isEmpty() && geocoderResponse.getResults().size()>0) {
				formattedAddress = geocoderResponse.getResults().get(0).getFormattedAddress();
			} else {
				formattedAddress = s;
			}
			WebService.setUserName(GEONAMES_USERNAME);
			ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();
			searchCriteria.setQ(formattedAddress);
			searchCriteria.setStyle(Style.MEDIUM);
			searchCriteria.setMaxRows(1);
			
			try {
				ToponymSearchResult result = WebService.search(searchCriteria);
				List<LocationCoordinates> locCo = new ArrayList<LocationCoordinates>();
				if (result.getTotalResultsCount() > 0) {
					Toponym locationToponym = result.getToponyms().get(0);
					List<Toponym> locationHierarchy = WebService.hierarchy(locationToponym.getGeoNameId(), GEONAMES_USERNAME, Style.MEDIUM);
					for (int index = 0; index < locationHierarchy.size(); index++) {
						Toponym tmp = locationHierarchy.get(index);
						if (index == 0) {
							locCo.add(new LocationCoordinates(tmp.getName(), "earth", tmp.getLatitude(), tmp.getLongitude()));
						} else if (index == 1) {
							locCo.add(new LocationCoordinates(tmp.getName(), "continent", tmp.getLatitude(), tmp.getLongitude()));
						} else if (index == 2) {
							locCo.add(new LocationCoordinates(tmp.getName(), "country", tmp.getLatitude(), tmp.getLongitude()));
						} else if (index == 3) {
							locCo.add(new LocationCoordinates(tmp.getName(), "state", tmp.getLatitude(), tmp.getLongitude()));
						} else if (index == 4) {
							locCo.add(new LocationCoordinates(tmp.getName(), "county", tmp.getLatitude(), tmp.getLongitude()));
						} else {
							locCo.add(new LocationCoordinates(tmp.getName(), "city", tmp.getLatitude(), tmp.getLongitude()));
						}
					}
					if(locCo.size()>=1) {
						locDetails.append("<earth_location>" + locCo.get(0).getLatitude() + ", " + locCo.get(0).getLongitude() + "</earth_location>\n");
						if(locCo.size()>=2) {
							locDetails.append("<continent_location>" + locCo.get(1).getLatitude() + ", " + locCo.get(1).getLongitude() + "</continent_location>\n");
							if(locCo.size()>=3) {
								locDetails.append("<country_location>" + locCo.get(2).getLatitude() + ", " + locCo.get(2).getLongitude() + "</country_location>\n");
								if(locCo.size()>=4) {
									locDetails.append("<state_location>" + locCo.get(3).getLatitude() + ", " + locCo.get(3).getLongitude() + "</state_location>\n");
									if(locCo.size()>=5) {
										locDetails.append("<county_location>" + locCo.get(4).getLatitude() + ", " + locCo.get(4).getLongitude() + "</county_location>\n");
										if(locCo.size()>=6) {
											locDetails.append("<city_location>" + locCo.get(5).getLatitude() + ", " + locCo.get(5).getLongitude() + "</city_location>\n");
										}
									}
								}
							}
						}		
					}
				}
			} catch(Exception ex) {
				ex.printStackTrace();
			}*/
		}
		idLocMap.put(idFromXml2, locCnt);
		locSet.addAll(refTmpLocs);
		
		if(csvLocs.length()>2) {
			csvLocs.setLength(csvLocs.length()-2);
		}
		String csvLoc = csvLocs.toString();
		
		String htmlText = WikiModel.toHtml(textFromXml2);
		htmlText = htmlText.replaceAll("\\{\\{[\\s|\\S]*?\\}\\}", "");
		StringBuilder data = new StringBuilder();
		data.append("<page>\n<id>" + idFromXml2 + "</id>\n<title>" + titleFromXml2 + "</title>\n<timestamp>" + timestampFromXml2 + 
				"</timestamp>\n<text>" + textFromXml2 + "</text>\n<locations>" + csvLoc + "</locations>\n<htmltext>" + htmlText + "</htmltext>\n" + 
				//locDetails + 
				"</page>\n\n");
		
		BufferedWriter bw;
		File f = new File("refinedWikinewsDump.xml");
		if(!f.exists()) {
			f.createNewFile();	
		} 
		bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f, true), "UTF-8"));
		bw.write(data.toString());
		bw.flush();
		bw.close();
	}

	public void endDocument() throws SAXException {
		/*System.out.println("Total pages parsed = " + noOfArticles);
		System.out.println("Pages with locations = " + noOfLocArticles);
		try {
			persistToFile(idLocMap, locSet);
			writeToExcel(idLocMap, locSet, writeCount);
		} catch (IOException e) {
			e.printStackTrace();
		}*/
	}
	
	private void persistToFile(Map<String, Map<String, Integer>> idLocMap2,
			Set<String> locSet2) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("idLocMap.ser"));
		oos.writeObject(idLocMap2);
		oos.flush();
		oos.close();
		ObjectOutputStream oos1 = new ObjectOutputStream(new FileOutputStream("locs.ser"));
		oos1.writeObject(locSet2);
		oos1.flush();
		oos1.close();	
	}

	private static void writeToExcel(Map<String, Map<String, Integer>> idLocMap2, Set<String> locSet, int writeCount2) throws IOException {
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("ID-Location");
		XSSFSheet locSheet = workbook.createSheet("Location");
		int rowNum=-1, pageCount=0;
		XSSFRow headRow = sheet.createRow(++rowNum);
		XSSFCell hCell0 = headRow.createCell(0);
		hCell0.setCellValue("Sr. No.");
		XSSFCell hCell1 = headRow.createCell(1);
		hCell1.setCellValue("WikiPageId");
		XSSFCell hCell2 = headRow.createCell(2);
		hCell2.setCellValue("Location");
		XSSFCell hCell3 = headRow.createCell(3);
		hCell3.setCellValue("Frequency of location in Page");
		for(Map.Entry<String, Map<String, Integer>> e : idLocMap2.entrySet())  {
			XSSFRow dataRow = sheet.createRow(++rowNum);
			XSSFCell idCellNo = dataRow.createCell(0);
			idCellNo.setCellValue(++pageCount);
			XSSFCell idCell = dataRow.createCell(1);
			idCell.setCellValue(e.getKey());
			for(Map.Entry<String, Integer> ie : e.getValue().entrySet()) {
				XSSFRow locDataRow = sheet.createRow(++rowNum);
				XSSFCell nullDataCell = locDataRow.createCell(0);
				nullDataCell.setCellValue("");
				XSSFCell nullDataCell1 = locDataRow.createCell(1);
				nullDataCell1.setCellValue("");
				XSSFCell locDataCell = locDataRow.createCell(2);
				locDataCell.setCellValue(ie.getKey());
				XSSFCell locFreqCell = locDataRow.createCell(3);
				locFreqCell.setCellValue(ie.getValue().toString());
			}
		}
		rowNum = -1;
		XSSFRow headRow1 = locSheet.createRow(++rowNum);
		XSSFCell hCell11 = headRow1.createCell(0);
		hCell11.setCellValue("Sr. No. Id");
		XSSFCell hCell21 = headRow1.createCell(1);
		hCell21.setCellValue("Location Names");
		for(String s : locSet) {
			XSSFRow dataRow = locSheet.createRow(++rowNum);
			XSSFCell idCell = dataRow.createCell(0);
			idCell.setCellValue(rowNum);
			XSSFCell locCell = dataRow.createCell(1);
			locCell.setCellValue(s);
		}
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File("idLocSheet.xlsx")));
		workbook.write(bos);
		bos.flush();
		bos.close();
	}
}
