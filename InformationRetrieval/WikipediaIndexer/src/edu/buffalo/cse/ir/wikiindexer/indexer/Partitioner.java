/**
 * 
 */
package edu.buffalo.cse.ir.wikiindexer.indexer;

import edu.buffalo.cse.ir.wikiindexer.ApplicationConstants;

/**
 * @author nikhillo
 * THis class is responsible for assigning a partition to a given term.
 * The static methods imply that all instances of this class should 
 * behave exactly the same. Given a term, irrespective of what instance
 * is called, the same partition number should be assigned to it.
 */
public class Partitioner {
	
	int partNo = 1;
	
	/**
	 * Method to get the total number of partitions
	 * THis is a pure design choice on how many partitions you need
	 * and also how they are assigned.
	 * @return: Total number of partitions
	 */
	public static int getNumPartitions() {
		return 27;
	}
	
	/**
	 * Method to fetch the partition number for the given term.
	 * The partition numbers should be assigned from 0 to N-1
	 * where N is the total number of partitions.
	 * @param term: The term to be looked up
	 * @return The assigned partition number for the given term
	 */
	public static int getPartitionNumber(String term) {
		
		// Using ASCII Codes a-z-----97-122 A-Z-----65-90
		char firstAlphabet;
		if(null != term && !term.trim().equalsIgnoreCase(ApplicationConstants.BLANK_STRING)) {
			firstAlphabet = term.charAt(0);
			if (firstAlphabet >= 65 && firstAlphabet <= 90) {
				return firstAlphabet % 64;
			}
			if (firstAlphabet >= 97 && firstAlphabet <= 122) {
				return firstAlphabet % 96;
			}
			return 0;
		} else {
			return -1;
		}
	}
}
