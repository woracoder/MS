/**
 * 
 */
package edu.buffalo.cse.ir.wikiindexer.indexer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import edu.buffalo.cse.ir.wikiindexer.ApplicationConstants;

/**
 * @author nikhillo
 * This class is used to introspect a given index
 * The expectation is the class should be able to read the index
 * and all associated dictionaries.
 */
public class IndexReader implements Serializable {
	
	private static final long serialVersionUID = -8535633550874410151L;
	INDEXFIELD field;
	String dictionaryFilename;
	ObjectInputStream ois;
	TreeMap<Integer, TreeMap<Integer, Integer>> linkIndex;
	TreeMap<String, TreeMap<Integer, Integer>> termAuthorCategoryIndex;
	Map<Integer, TreeMap<String, TreeMap<Integer, Integer>>> allTermIndexesMap;
	Map<String, Integer> docDict;
	
	/**
	 * Constructor to create an instance 
	 * @param props: The properties file
	 * @param field: The index field whose index is to be read
	 */
	public IndexReader(Properties props, INDEXFIELD field) {

		this.field = field;
		try {
			//Read the document dictionary
			ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(props.getProperty("output.dir") + "docDict.ser")));
			docDict = ((Dictionary)ois.readObject()).dictMap;
			
			//Read the indexes based on the fields
			if(field == INDEXFIELD.AUTHOR) {
				ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(props.getProperty("output.dir") + "authorIndex.ser")));
				termAuthorCategoryIndex = ((IndexWriter)ois.readObject()).termAuthorCategoryIndex;
			} else if(field == INDEXFIELD.CATEGORY) {
				ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(props.getProperty("output.dir") + "categoryIndex.ser")));
				termAuthorCategoryIndex = ((IndexWriter)ois.readObject()).termAuthorCategoryIndex;
			} else if(field == INDEXFIELD.LINK) {
				ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(props.getProperty("output.dir") + "linkIndex.ser")));
				linkIndex = ((IndexWriter)ois.readObject()).linkIndex;
			} else if(field == INDEXFIELD.TERM) {
				allTermIndexesMap = new HashMap<Integer, TreeMap<String,TreeMap<Integer,Integer>>>();
				File outputDir = new File(props.getProperty("output.dir"));
				IndexWriter iw;
				for(File f : outputDir.listFiles()) {
					if(f.isFile()) {
						if(f.getName().contains("termIndex")) {
							ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(props.getProperty("output.dir") + f.getName())));
							iw = (IndexWriter)ois.readObject();
							allTermIndexesMap.put(iw.partitionNumber, iw.termAuthorCategoryIndex);
						}
					}
				}
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} finally {
			try {
				if(null != ois) {
					ois.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Method to get the total number of terms in the key dictionary
	 * @return The total number of terms as above
	 */
	public int getTotalKeyTerms() {
		if (field == INDEXFIELD.LINK) {
			return linkIndex.size();
		} else if (field == INDEXFIELD.CATEGORY || field == INDEXFIELD.AUTHOR) {
			return termAuthorCategoryIndex.size();
		} else if (field == INDEXFIELD.TERM) {
			int size=0;
			for(Map<String, TreeMap<Integer, Integer>> e : allTermIndexesMap.values()) {
				if(null != e && !e.isEmpty() && e.size()>0) {
					size = size + e.size();
				}
			}
			return size;
		} else {
			return 0;
		}
	}
	
	/**
	 * Method to get the total number of terms in the value dictionary
	 * @return The total number of terms as above
	 */
	public int getTotalValueTerms() {
		int size=0;
		if(null != docDict && !docDict.isEmpty() && (size=docDict.size())>0) {
			return size;
		} else {
			return size;
		}
	}
	
	/**
	 * Method to retrieve the postings list for a given dictionary term
	 * @param key: The dictionary term to be queried
	 * @return The postings list with the value term as the key and the
	 * number of occurrences as value. An ordering is not expected on the map
	 */
	public Map<String, Integer> getPostings(String key) {
	
		//Check if key is not null & blank
		if(null != key && !key.trim().equalsIgnoreCase(ApplicationConstants.BLANK_STRING)) {
			//Initialize the posting map to be returned
			Map<String, Integer> postingMap = new HashMap<String, Integer>();
			//Put the keys of the dictionary in a list. As the document dictionary is a linked hash map the insertion order is maintained.
			//As the values of the dictionary are in increasing order of 1 the list of keys can directly be queried based on index to get the String key. 
			List<String> keys = new ArrayList<String>(docDict.keySet());
			//For category & author index
			if(field == INDEXFIELD.CATEGORY || field == INDEXFIELD.AUTHOR) {
				if(termAuthorCategoryIndex.containsKey(key)) {
					for(Map.Entry<Integer, Integer> e : termAuthorCategoryIndex.get(key).entrySet()) {
						postingMap.put(keys.get(e.getKey()), e.getValue());
					}
				}
			} else if(field == INDEXFIELD.TERM) {
				int partNo = Partitioner.getPartitionNumber(key);
				if(allTermIndexesMap.containsKey(partNo)) {
					if(allTermIndexesMap.get(partNo).containsKey(key)) {
						for(Map.Entry<Integer, Integer> e : allTermIndexesMap.get(partNo).get(key).entrySet()) {
							postingMap.put(keys.get(e.getKey()), e.getValue());
						}
					}
				}
			} else if(field == INDEXFIELD.LINK) {
				if(docDict.containsKey(key)) {
					Integer linkIndexKey = docDict.get(key);
					if(linkIndex.containsKey(linkIndexKey)) {
						for(Map.Entry<Integer, Integer> e : linkIndex.get(linkIndexKey).entrySet()) {
							postingMap.put(keys.get(e.getKey()), e.getValue());
						}
					}
				}
			}
			return (postingMap.size()>0) ? postingMap : null;
		} else {
			return null;
		}
	}
	
	/**
	 * Method to get the top k key terms from the given index
	 * The top here refers to the largest size of postings.
	 * @param k: The number of postings list requested
	 * @return An ordered collection of dictionary terms that satisfy the requirement
	 * If k is more than the total size of the index, return the full index and don't 
	 * pad the collection. Return null in case of an error or invalid inputs
	 */
	public Collection<String> getTopK(int k) {
		
		if(k>0) {
			if(field == INDEXFIELD.CATEGORY || field == INDEXFIELD.AUTHOR) {
				return getTopKAuthCatgry(k);
			} else if(field == INDEXFIELD.TERM) {
				return getTopKTerms(k);
			} else if(field == INDEXFIELD.LINK) {
				return getTopKLinks(k);
			}
			return null;
		} else {
			return null;
		}
	}

	private Collection<String> getTopKLinks(int k) {
		if(null != linkIndex && !linkIndex.isEmpty() && linkIndex.size()>0) {
			Map<String, Integer> freqMap = new HashMap<String, Integer>();
			List<String> keys = new ArrayList<String>(docDict.keySet());
			for(Map.Entry<Integer, TreeMap<Integer, Integer>> e : linkIndex.entrySet()) {
				freqMap.put(keys.get(e.getKey()), e.getValue().size());
			}
			Map<String, Integer> sortedFreqMap = sortMap(freqMap);
			if(k>sortedFreqMap.size()) {
				return sortedFreqMap.keySet();
			} else {
				Set<String> topKSet = new LinkedHashSet<String>();
				for(Map.Entry<String, Integer> e : sortedFreqMap.entrySet()) {
					if(k>0) {
						k--;
						topKSet.add(e.getKey());
					} else {
						break;
					}
				}
				return topKSet;
			}
		} else {
			return null;
		}
	}

	private Collection<String> getTopKTerms(int k) {
		if(null != allTermIndexesMap && !allTermIndexesMap.isEmpty() && allTermIndexesMap.size()>0) {
			for(Map.Entry<Integer, TreeMap<String, TreeMap<Integer, Integer>>> e : allTermIndexesMap.entrySet()) {
				if(null != e.getValue() && !e.getValue().isEmpty() && e.getValue().size()>0) {
					Map<String, Integer> freqMap = new HashMap<String, Integer>();
					for(Map.Entry<String, TreeMap<Integer, Integer>> es : e.getValue().entrySet()) {
						freqMap.put(es.getKey(), es.getValue().size());
					}
					Map<String, Integer> sortedFreqMap = sortMap(freqMap);
					if(k>sortedFreqMap.size()) {
						return sortedFreqMap.keySet();
					} else {
						Set<String> topKSet = new LinkedHashSet<String>();
						for(Map.Entry<String, Integer> eso : sortedFreqMap.entrySet()) {
							if(k>0) {
								k--;
								topKSet.add(eso.getKey());
							} else {
								break;
							}
						}
						return topKSet;
					}
				} else {
					continue;
				}
			}
			return null;
		} else {
			return null;
		}
	}

	private Collection<String> getTopKAuthCatgry(int k) {
		//Check if the index is not null or empty or of 0 size
		if(null != termAuthorCategoryIndex && !termAuthorCategoryIndex.isEmpty() && termAuthorCategoryIndex.size()>0) {
			Map<String, Integer> freqMap = new HashMap<String, Integer>();
			//Create a freq map of author/category of author & posting list size
			for(Map.Entry<String, TreeMap<Integer, Integer>> e : termAuthorCategoryIndex.entrySet()) {
				freqMap.put(e.getKey(), e.getValue().size());
			}
			Map<String, Integer> sortedFreqMap = sortMap(freqMap);
			if(k>sortedFreqMap.size()) {
				return sortedFreqMap.keySet();
			} else {
				Set<String> topKSet = new LinkedHashSet<String>();
				for(Map.Entry<String, Integer> e : sortedFreqMap.entrySet()) {
					if(k>0) {
						k--;
						topKSet.add(e.getKey());
					} else {
						break;
					}
				}
				return topKSet;
			}
		} else {
			return null;
		}
	}
	
	private Map<String, Integer> sortMap(Map<String, Integer> freqMap) {
		
		if(null != freqMap && !freqMap.isEmpty() && freqMap.size()>0) {
			List<Entry<String, Integer>> list = new LinkedList<Entry<String, Integer>>(freqMap.entrySet());
			Collections.sort(list, new Comparator<Entry<String, Integer>>() {
				public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
					return o2.getValue().compareTo(o1.getValue());
			    }
			});
			Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
	        for (Entry<String, Integer> entry : list) {
	            sortedMap.put(entry.getKey(), entry.getValue());
	        }
	        return sortedMap;
		} else {
			return null;
		}
        
    }
	
	private Map<Integer, Integer> sortIntMap(Map<Integer, Integer> freqMap) {
		
		if(null != freqMap && !freqMap.isEmpty() && freqMap.size()>0) {
			List<Entry<Integer, Integer>> list = new LinkedList<Entry<Integer, Integer>>(freqMap.entrySet());
			Collections.sort(list, new Comparator<Entry<Integer, Integer>>() {
				public int compare(Entry<Integer, Integer> o1, Entry<Integer, Integer> o2) {
					return o2.getValue().compareTo(o1.getValue());
			    }
			});
			Map<Integer, Integer> sortedMap = new LinkedHashMap<Integer, Integer>();
	        for (Entry<Integer, Integer> entry : list) {
	            sortedMap.put(entry.getKey(), entry.getValue());
	        }
	        return sortedMap;
		} else {
			return null;
		}
        
    }
	
	/**
	 * Method to execute a boolean AND query on the index
	 * @param terms The terms to be queried on
	 * @return An ordered map containing the results of the query
	 * The key is the value field of the dictionary and the value
	 * is the sum of occurrences across the different postings.
	 * The value with the highest cumulative count should be the
	 * first entry in the map.
	 */
	public Map<String, Integer> query(String... terms) {
		
		//Check if the array is not null & has elements
		if(null != terms && terms.length>0) {
			
			//The list of the doc dict keys
			List<String> keys = new ArrayList<String>(docDict.keySet());
			//The final Map to be returned
			Map<Integer, Integer> finalMap = new HashMap<Integer, Integer>();
			Map<String, Integer> retMap = new HashMap<String, Integer>();
			//Map of term & posting list
			TreeMap<String, TreeMap<Integer, Integer>> indexMap = new TreeMap<String, TreeMap<Integer,Integer>>();
			//Create a hashset for the intersection
			Set<Integer> intersectedIds=null;
			
			//Check if field is author or category
			if(field == INDEXFIELD.CATEGORY || field == INDEXFIELD.AUTHOR) {
				//Iterate over the terms
				for(String s : terms) {
					if(null != s && !s.trim().equalsIgnoreCase(ApplicationConstants.BLANK_STRING)) {
						if(null != termAuthorCategoryIndex && !termAuthorCategoryIndex.isEmpty() && termAuthorCategoryIndex.size()>0) {
							if(termAuthorCategoryIndex.containsKey(s)) {
								indexMap.put(s, termAuthorCategoryIndex.get(s));
							}
						}
					}
				}
				//Now iterate over the created index map & get the intersection of the ids which are the keys of the value element of the index map
				for(TreeMap<Integer, Integer> e : indexMap.values()) {
					if(null == intersectedIds) {
						intersectedIds = new HashSet<Integer>(e.keySet());
					} else {
						intersectedIds.retainAll(e.keySet());
					}
				}
				
				for(Integer i :intersectedIds) {
					for(TreeMap<Integer, Integer> e : indexMap.values()) {
						if(e.containsKey(i)) {
							if(finalMap.containsKey(i)) {
								finalMap.put(i, finalMap.get(i) + e.get(i));
							} else {
								finalMap.put(i, e.get(i));
							}
						}
					}
				}
				finalMap = sortIntMap(finalMap);
				for(Map.Entry<Integer, Integer> e :finalMap.entrySet()) {
					//retMap.put(keys.get(e.getKey()), );
				}
				
			} else if(field == INDEXFIELD.TERM) {
				
			} else if(field == INDEXFIELD.LINK) {
				
			}
			return null;
		} else {
			return null;
		}
	}
	
}
