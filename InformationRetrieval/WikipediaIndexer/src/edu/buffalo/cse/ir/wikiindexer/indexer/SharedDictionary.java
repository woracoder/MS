/**
 * 
 */
package edu.buffalo.cse.ir.wikiindexer.indexer;

import java.util.Properties;

import edu.buffalo.cse.ir.wikiindexer.ApplicationConstants;

/**
 * @author nikhillo
 * This class represents a subclass of a Dictionary class that is
 * shared by multiple threads. All methods in this class are
 * synchronized for the same reason.
 */
public class SharedDictionary extends Dictionary {
	
	private static final long serialVersionUID = -2092883848725062132L;
	static Integer id = 0;
	
	/**
	 * Public default constructor
	 * @param props: The properties file
	 * @param field: The field being indexed by this dictionary
	 */
	public SharedDictionary(Properties props, INDEXFIELD field) {
		super(props, field);
	}
	
	/**
	 * Method to lookup and possibly add a mapping for the given value
	 * in the dictionary. The class should first try and find the given
	 * value within its dictionary. If found, it should return its
	 * id (Or hash value). If not found, it should create an entry and
	 * return the newly created id.
	 * @param value: The value to be looked up
	 * @return The id as explained above.
	 */
	public synchronized int lookup(String value) {

		int val = 0;
		if (null != value && !value.trim().equalsIgnoreCase(ApplicationConstants.BLANK_STRING)) {
			value = value.replaceAll("\\s", "_");
			if (value.length() > 0) {
				// Capitalizing the First Alphabet
				value = value.substring(0, 1).toUpperCase() + value.substring(1);
			}
			if (dictMap.containsKey(value)) {
				val = dictMap.get(value).intValue();
			} else {
				dictMap.put(value, ++id);
				val = id;
			}
		}
		return val;
	}

}
