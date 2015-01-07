/**
 * 
 */
package edu.buffalo.cse.ir.wikiindexer.wikipedia;

import java.util.HashMap;
import java.util.Map;

import edu.buffalo.cse.ir.wikiindexer.indexer.INDEXFIELD;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenStream;

/**
 * A simple map based token view of the transformed document
 * @author nikhillo
 *
 */
public class IndexableDocument {
	
	private Map<INDEXFIELD, TokenStream> indexMap;
	
	private String uid;
	
	/**
	 * Default constructor
	 */
	public IndexableDocument(String uid) {
		this.uid = uid;
		this.indexMap = new HashMap<INDEXFIELD, TokenStream>();
	}
	
	/**
	 * MEthod to add a field and stream to the map
	 * If the field already exists in the map, the streams should be merged
	 * @param field: The field to be added
	 * @param stream: The stream to be added.
	 */
	public void addField(INDEXFIELD field, TokenStream stream) {
		
		if(indexMap.containsKey(field)) {
			indexMap.get(field).merge(stream);
		} else {
			indexMap.put(field, stream);
		}
	}
	
	/**
	 * Method to return the stream for a given field
	 * @param key: The field for which the stream is requested
	 * @return The underlying stream if the key exists, null otherwise
	 */
	public TokenStream getStream(INDEXFIELD key) {
		
		TokenStream ts = null;
		if(null != key) {
			if(indexMap.containsKey(key)) {
				ts = indexMap.get(key);
			}
		}
		return ts;
	}
	
	/**
	 * Method to return a unique identifier for the given document.
	 * It is left to the student to identify what this must be
	 * But also look at how it is referenced in the indexing process
	 * @return A unique identifier for the given document
	 */
	public String getDocumentIdentifier() {
		return uid;
	}
	
}
