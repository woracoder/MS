package edu.buffalo.cse.ir.wikiindexer.indexer;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;
import java.util.TreeMap;

import edu.buffalo.cse.ir.wikiindexer.ApplicationConstants;

/**
 * @author nikhillo
 * This class is used to write an index to the disk
 * 
 */
public class IndexWriter implements Writeable {
	
	private static final long serialVersionUID = -6030246605778549970L;
	private INDEXFIELD keyField;
	private INDEXFIELD valueField;
	private boolean isForward;
	protected TreeMap<Integer, TreeMap<Integer, Integer>> linkIndex;
	protected TreeMap<String, TreeMap<Integer, Integer>> termAuthorCategoryIndex;
	transient private TreeMap<Integer, TreeMap<String, Integer>> intStrIntIndex;
	transient private TreeMap<String, TreeMap<String, Integer>> strStrIntIndex;
	transient Properties props;
	
	transient ObjectOutputStream oos;

	protected int partitionNumber;
	
	/**
	 * Constructor that assumes the underlying index is inverted
	 * Every index (inverted or forward), has a key field and the value field
	 * The key field is the field on which the postings are aggregated
	 * The value field is the field whose postings we are accumulating
	 * For term index for example:
	 * 	Key: Term (or term id) - referenced by TERM INDEXFIELD
	 * 	Value: Document (or document id) - referenced by LINK INDEXFIELD
	 * @param props: The Properties file
	 * @param keyField: The index field that is the key for this index
	 * @param valueField: The index field that is the value for this index
	 */
	//Called by threaded index runner for (TERM - LINK) props, term, link i.e. doc
	public IndexWriter(Properties props, INDEXFIELD keyField, INDEXFIELD valueField) {
		this.keyField = keyField;
		this.props = props;
		this.valueField = valueField;
		this.isForward = false;
		linkIndex = new TreeMap<Integer, TreeMap<Integer, Integer>>();
		termAuthorCategoryIndex = new TreeMap<String, TreeMap<Integer, Integer>>();
		intStrIntIndex = new TreeMap<Integer, TreeMap<String, Integer>>();
		strStrIntIndex = new TreeMap<String, TreeMap<String, Integer>>();
	}
	
	/**
	 * Overloaded constructor that allows specifying the index type as
	 * inverted or forward
	 * Every index (inverted or forward), has a key field and the value field
	 * The key field is the field on which the postings are aggregated
	 * The value field is the field whose postings we are accumulating
	 * For term index for example:
	 * 	Key: Term (or term id) - referenced by TERM INDEXFIELD
	 * 	Value: Document (or document id) - referenced by LINK INDEXFIELD
	 * @param props: The Properties file
	 * @param keyField: The index field that is the key for this index
	 * @param valueField: The index field that is the value for this index
	 * @param isForward: true if the index is a forward index, false if inverted
	 */
	//Call by single index runner for (AUTHOR-LINK, CATEGORY-LINK, LINK-LINK) props, (author/category/link i.e. doc), link i.e. doc, true only for link
	public IndexWriter(Properties props, INDEXFIELD keyField, INDEXFIELD valueField, boolean isForward) {
		this.keyField = keyField;
		this.props = props;
		this.valueField = valueField;
		this.isForward = isForward;
		linkIndex = new TreeMap<Integer, TreeMap<Integer, Integer>>();
		termAuthorCategoryIndex = new TreeMap<String, TreeMap<Integer, Integer>>();
		intStrIntIndex = new TreeMap<Integer, TreeMap<String, Integer>>();
		strStrIntIndex = new TreeMap<String, TreeMap<String, Integer>>();
	}
	
	/**
	 * Method to make the writer self aware of the current partition it is handling
	 * Applicable only for distributed indexes.
	 * @param pnum: The partition number
	 */
	public void setPartitionNumber(int pnum) {
		partitionNumber = pnum;
	}
	
	/**
	 * Method to add a given key - value mapping to the index
	 * @param keyId: The id for the key field, pre-converted
	 * @param valueId: The id for the value field, pre-converted
	 * @param numOccurances: Number of times the value field is referenced
	 *  by the key field. Ignore if a forward index
	 * @throws IndexerException: If any exception occurs while indexing
	 *
	 */
	//Called from link case & lookup both, of single index runner
	public void addToIndex(int keyId, int valueId, int numOccurances) throws IndexerException {

		TreeMap<Integer, Integer> postingList;
		if (linkIndex.containsKey(keyId)) {
			postingList = linkIndex.get(keyId);
			postingList.put(valueId, numOccurances);
		} else {
			postingList = new TreeMap<Integer, Integer>();
			postingList.put(valueId, numOccurances);
			linkIndex.put(keyId, postingList);
		}
	}
		
	
	/**
	 * Method to add a given key - value mapping to the index
	 * @param keyId: The id for the key field, pre-converted
	 * @param value: The value for the value field
	 * @param numOccurances: Number of times the value field is referenced
	 *  by the key field. Ignore if a forward index
	 * @throws IndexerException: If any exception occurs while indexing
	 */
	public void addToIndex(int keyId, String value, int numOccurances) throws IndexerException {

		TreeMap<String, Integer> postingList;
		if (intStrIntIndex.containsKey(keyId)) {
			postingList = intStrIntIndex.get(keyId);
			postingList.put(value, numOccurances);
		} else {
			postingList = new TreeMap<String, Integer>();
			postingList.put(value, numOccurances);
			intStrIntIndex.put(keyId, postingList);
		}
	}
	
	/**
	 * Method to add a given key - value mapping to the index
	 * @param key: The key for the key field
	 * @param valueId: The id for the value field, pre-converted
	 * @param numOccurances: Number of times the value field is referenced by the key field. Ignore if a forward index
	 * @throws IndexerException: If any exception occurs while indexing
	 */
	//Being invoked for term index, author index, category index
	public void addToIndex(String key, int valueId, int numOccurances) throws IndexerException {
		if(null != key && !key.trim().equalsIgnoreCase(ApplicationConstants.BLANK_STRING)) {
			TreeMap<Integer, Integer> postingList;
			if (termAuthorCategoryIndex.containsKey(key)) {
				postingList = termAuthorCategoryIndex.get(key);
				postingList.put(valueId, numOccurances);
			} else {
				postingList = new TreeMap<Integer, Integer>();
				postingList.put(valueId, numOccurances);
				termAuthorCategoryIndex.put(key, postingList);
			}
		}
	}
	
	/**
	 * Method to add a given key - value mapping to the index
	 * @param key: The key for the key field
	 * @param value: The value for the value field
	 * @param numOccurances: Number of times the value field is referenced
	 *  by the key field. Ignore if a forward index
	 * @throws IndexerException: If any exception occurs while indexing
	 */
	public void addToIndex(String key, String value, int numOccurances) throws IndexerException {
		
		TreeMap<String,Integer> postingList;			
		if(strStrIntIndex.containsKey(key)){
			postingList=strStrIntIndex.get(key);
			postingList.put(value, numOccurances);
		} else {
			postingList = new TreeMap<String,Integer>();
			postingList.put(value, numOccurances);
			strStrIntIndex.put(key, postingList);
		}
	}

	/* (non-Javadoc)
	 * @see edu.buffalo.cse.ir.wikiindexer.indexer.Writeable#writeToDisk()
	 */
	public void writeToDisk() throws IndexerException {

		String fileName = null;
		try {
			if (keyField == INDEXFIELD.AUTHOR) {
				fileName = props.getProperty("output.dir") + "authorIndex.ser";
			} else if (keyField == INDEXFIELD.CATEGORY) {
				fileName = props.getProperty("output.dir") + "categoryIndex.ser";
			} else if (keyField == INDEXFIELD.LINK) {
				fileName = props.getProperty("output.dir") + "linkIndex.ser";
			} else if(keyField == INDEXFIELD.TERM) {
				fileName = props.getProperty("output.dir") + "termIndex_" + partitionNumber + ".ser";
			}
			
			oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)));
			oos.writeObject(this);
			oos.flush();
		} catch (Exception e) {

		}
	}

	/* (non-Javadoc)
	 * @see edu.buffalo.cse.ir.wikiindexer.indexer.Writeable#cleanUp()
	 */
	public void cleanUp() {
		try {
			if(null != oos) {
				oos.close();
			}
			linkIndex = null;
			termAuthorCategoryIndex = null;
			intStrIntIndex = null;
			strStrIntIndex = null;
		} catch (Exception e) {

		}
	}

}
