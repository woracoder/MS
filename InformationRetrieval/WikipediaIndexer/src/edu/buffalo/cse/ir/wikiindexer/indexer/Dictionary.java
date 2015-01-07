/**
 * 
 */
package edu.buffalo.cse.ir.wikiindexer.indexer;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.buffalo.cse.ir.wikiindexer.ApplicationConstants;

/**
 * @author nikhillo
 * An abstract class that represents a dictionary object for a given index
 */
public abstract class Dictionary implements Writeable {
	
	
	private static final long serialVersionUID = -6306566668107243285L;
	protected Map<String, Integer> dictMap;
	private INDEXFIELD field;
	transient private ObjectOutputStream out = null;
	transient Properties props;
	
	public Dictionary (Properties props, INDEXFIELD field) {
		this.dictMap = new LinkedHashMap<String, Integer>();
		this.field = field;
		this.props = props;
	}
	
	/* (non-Javadoc)
	 * @see edu.buffalo.cse.ir.wikiindexer.indexer.Writeable#writeToDisk()
	 */
	public void writeToDisk() throws IndexerException {
		
		try {
			System.out.println("Dict size = "+dictMap.size());
			out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(props.getProperty("output.dir") + "docDict.ser")));
			out.writeObject(this);
			out.flush();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see edu.buffalo.cse.ir.wikiindexer.indexer.Writeable#cleanUp()
	 */
	public void cleanUp() {
		try {
			if(null != out) {
				out.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Method to check if the given value exists in the dictionary or not
	 * Unlike the subclassed lookup methods, it only checks if the value exists
	 * and does not change the underlying data structure
	 * @param value: The value to be looked up
	 * @return true if found, false otherwise
	 */
	public boolean exists(String value) {
		if(null != value && !value.trim().equalsIgnoreCase(ApplicationConstants.BLANK_STRING)) {
			if(dictMap.containsKey(value)) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}
	
	/**
	 * MEthod to lookup a given string from the dictionary.
	 * The query string can be an exact match or have wild cards (* and ?)
	 * Must be implemented ONLY AS A BONUS
	 * @param queryStr: The query string to be searched
	 * @return A collection of ordered strings enumerating all matches if found
	 * null if no match is found
	 */
	public Collection<String> query(String queryStr) {

		//Check if queryStr is not null or empty
		if (null != queryStr && !(queryStr = queryStr.trim()).equalsIgnoreCase(ApplicationConstants.BLANK_STRING)) {
			//Collection to be returned
			List<String> strings = new ArrayList<String>();
			//Check if wildcard entry
			if (queryStr.contains("*")) {
				// Check if only * in which case we have to return entire key set of map
				if (queryStr.length() == 1) {
					return new ArrayList<String>(dictMap.keySet());
				} else if (queryStr.charAt(0) == '*') {
					//If wild card at start of query string 
					Pattern p = Pattern.compile("\\b[\\s\\S]*?" + "\\Q" + queryStr.split("\\*")[1] + "\\E" + "\\b");
					Matcher m;
					for (String s : dictMap.keySet()) {
						m = p.matcher(s);
						if (m.find()) {
							strings.add(s);
						}
					}
				} else if (queryStr.charAt(queryStr.length() - 1) == '*') {
					//If wild card at end of query string
					Pattern p = Pattern.compile("\\b" + "\\Q" + queryStr.split("\\*")[0] + "\\E" + "[\\s\\S]*?\\b");
					Matcher m;
					for (String s : dictMap.keySet()) {
						m = p.matcher(s);
						if (m.find()) {
							strings.add(s);
						}
					}
				} else {
					//If wild card between query string
					String[] sa = queryStr.split("\\*");
					Pattern p = Pattern.compile("\\b" + "\\Q" + sa[0] + "\\E" + "[\\s\\S]*?" + "\\Q" + sa[1] + "\\E" + "\\b");
					Matcher m;
					for (String s : dictMap.keySet()) {
						m = p.matcher(s);
						if (m.find()) {
							strings.add(s);
						}
					}
				}
			} else if(queryStr.contains("?")) {
				// Check if only ? in which case we have to return top key of map
				if (queryStr.length() == 1) {
					ArrayList<String> retList = new ArrayList<String>();
					retList.add(new ArrayList<String>(dictMap.keySet()).get(0));
					return retList;
				} else if (queryStr.charAt(0) == '?') {
					//If wild card at start of query string 
					Pattern p = Pattern.compile("\\b[\\S]" + "\\Q" + queryStr.split("\\?")[1] + "\\E" + "\\b");
					Matcher m;
					for (String s : dictMap.keySet()) {
						m = p.matcher(s);
						if (m.find()) {
							strings.add(s);
						}
					}
				} else if (queryStr.charAt(queryStr.length() - 1) == '?') {
					//If wild card at end of query string
					Pattern p = Pattern.compile("\\b" + "\\Q" + queryStr.split("\\?")[0] + "\\E" + "[\\S]\\b");
					Matcher m;
					for (String s : dictMap.keySet()) {
						m = p.matcher(s);
						if (m.find()) {
							strings.add(s);
						}
					}
				} else {
					//If wild card between query string
					String[] sa = queryStr.split("\\?");
					Pattern p = Pattern.compile("\\b" + "\\Q" + sa[0] + "\\E" + "[\\S]" + "\\Q" + sa[1] + "\\E" + "\\b");
					Matcher m;
					for (String s : dictMap.keySet()) {
						m = p.matcher(s);
						if (m.find()) {
							strings.add(s);
						}
					}
				}
			} else if (dictMap.containsKey(queryStr)) {
				strings.add(queryStr);
			}
			return (strings.size()>0) ? strings: null;
		} else {
			return null;
		}
	}
	
	/**
	 * Method to get the total number of terms in the dictionary
	 * @return The size of the dictionary
	 */
	public int getTotalTerms() {
		int size=0;
		if(null != dictMap && !dictMap.isEmpty() && (size = dictMap.size())>0) {
			return size;
		} else {
			return size;
		}
	}
}
