/**
 * 
 */
package edu.buffalo.cse.ir.wikiindexer.tokenizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.buffalo.cse.ir.wikiindexer.ApplicationConstants;

/**
 * This class represents a stream of tokens as the name suggests.
 * It wraps the token stream and provides utility methods to manipulate it
 * @author nikhillo
 *
 */
public class TokenStream implements Iterator<String> {
	
	private boolean preTokenStage = false;
	
	public boolean isPreTokenStage() {
		return preTokenStage;
	}

	public void setPreTokenStage(boolean isPreTokenStage) {
		this.preTokenStage = isPreTokenStage;
	}
	
	private int position=0;
	
	private StringBuilder tokenStreamSb = new StringBuilder();
	
	public StringBuilder getTokenStreamSb() {
		return tokenStreamSb;
	}

	public void setTokenStreamSb(StringBuilder tokenStreamSb) {
		this.tokenStreamSb = tokenStreamSb;
	}

	private String tokenStream;
	
	public String getTokenStream() {
		return tokenStream;
	}

	public void setTokenStream(String tokenStream) {
		this.tokenStream = tokenStream;
	}

	private List<String> tokenList = new ArrayList<String>();
	
	public List<String> getTokenList() {
		return tokenList;
	}

	public void setTokenList(List<String> tokenList) {
		this.tokenList = tokenList;
	}

	private Map<String, Integer> tokenMap = new HashMap<String, Integer>();

	/**
	 * Default constructor
	 * @param bldr: THe stringbuilder to seed the stream
	 */
	public TokenStream(StringBuilder bldr) {
		if(null != bldr && !bldr.toString().trim().equalsIgnoreCase(ApplicationConstants.BLANK_STRING)) {
			this.tokenStreamSb = bldr;
			this.tokenStream = bldr.toString();
			this.tokenList.add(bldr.toString());
			this.tokenMap.put(bldr.toString(), 1);
		}
	}
	
	/**
	 * Overloaded constructor
	 * @param bldr: THe stringbuilder to seed the stream
	 */
	public TokenStream(String string) {
		if(null != string && !string.trim().equalsIgnoreCase(ApplicationConstants.BLANK_STRING)) {
			this.tokenStream = string;
			this.tokenStreamSb = new StringBuilder(string);
			this.tokenList.add(string);
			this.tokenMap.put(string, 1);
		}
	}
	
	/**
	 * Method to append tokens to the stream
	 * @param tokens: The tokens to be appended
	 */
	public void append(String... tokens) {
		if(null != tokens && tokens.length>0) {
			for(String s : tokens) {
				if(null != s && !(s = s.trim()).equalsIgnoreCase(ApplicationConstants.BLANK_STRING)) {
					tokenStreamSb.append(ApplicationConstants.SPACE_STRING + s);
					tokenList.add(s);
					if(tokenMap.containsKey(s)) {
						tokenMap.put(s, tokenMap.get(s)+1);
					} else {
						tokenMap.put(s, 1);
					}
				}
			}
		}
		/*if(null != tokenStreamSb) {
			tokenStream = tokenStreamSb.toString();
		}*/
	}
	
	/**
	 * Method to retrieve a map of token to count mapping
	 * This map should contain the unique set of tokens as keys
	 * The values should be the number of occurrences of the token in the given stream
	 * @return The map as described above, no restrictions on ordering applicable
	 */
	public Map<String, Integer> getTokenMap() {
		if(null != this.tokenMap && !this.tokenMap.isEmpty() && this.tokenMap.size()>0) {
			return this.tokenMap;
		} else {
			return null;
		}
	}
	
	/**
	 * Method to get the underlying token stream as a collection of tokens
	 * @return A collection containing the ordered tokens as wrapped by this stream
	 * Each token must be a separate element within the collection.
	 * Operations on the returned collection should NOT affect the token stream
	 */
	public Collection<String> getAllTokens() {
		if(preTokenStage) {
			if(null != tokenStreamSb && !tokenStreamSb.toString().trim().equalsIgnoreCase(ApplicationConstants.BLANK_STRING)) {
				return new ArrayList<String>(Arrays.asList(tokenStreamSb.toString().trim()));
			} else {
				return null;
			}
		} else if(null != this.tokenList && !this.tokenList.isEmpty() && this.tokenList.size()>0) {
			return this.tokenList;
		} else {
			return null;
		}
	}
	
	/**
	 * Method to query for the given token within the stream
	 * @param token: The token to be queried
	 * @return: THe number of times it occurs within the stream, 0 if not found
	 */
	public int query(String token) {
		if(null != token && !tokenMap.isEmpty() && tokenMap.size()>0 && tokenMap.containsKey(token)) {
			return tokenMap.get(token);
		} else {
			return 0;
		}
	}
	
	/**
	 * Iterator method: Method to check if the stream has any more tokens
	 * @return true if a token exists to iterate over, false otherwise
	 */
	public boolean hasNext() {
		//Check first if there are tokens in the token stream
		//Also check if the iterator position is within the collection size 
		if(null != tokenList && !tokenList.isEmpty() && tokenList.size()>0 && position>=0 && position<tokenList.size()) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Iterator method: Method to check if the stream has any more tokens
	 * @return true if a token exists to iterate over, false otherwise
	 */
	public boolean hasPrevious() {
		//Check first if there are tokens in the token stream
		//Also check if the iterator position is within the collection size 
		if(null != tokenList && !tokenList.isEmpty() && tokenList.size()>0 && position>0 && position<=tokenList.size()) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Iterator method: Method to get the next token from the stream
	 * Callers must call the set method to modify the token, changing the value
	 * of the token returned by this method must not alter the stream
	 * @return The next token from the stream, null if at the end
	 */
	public String next() {
		//Check if the tokenList is not empty and iterator position is within list size
		if(null != tokenList && !tokenList.isEmpty() && tokenList.size()>0 && position>=0 && position<tokenList.size()) {
			position++;
			return tokenList.get(position-1);
		} else {
			return null;
		}
	}
	
	/**
	 * Iterator method: Method to get the previous token from the stream
	 * Callers must call the set method to modify the token, changing the value
	 * of the token returned by this method must not alter the stream
	 * @return The next token from the stream, null if at the end
	 */
	public String previous() {
		if(null != tokenList && !tokenList.isEmpty() && tokenList.size()>0 && position>0 && position<=tokenList.size()) {
			position--;
			return tokenList.get(position);
		} else {
			return null;
		}
	}
	
	/**
	 * Iterator method: Method to remove the current token from the stream
	 */
	public void remove() {
		//First check if there are tokens in the stream to be removed
		//Also check if the iterator position is within the list size 
		if(null != tokenList && !tokenList.isEmpty() && tokenList.size()>0 && position>=0 && position<tokenList.size()) {
			//Get the token to be removed
			String token = tokenList.get(position);
			//Check if there are more than 1 of these tokens in the map
			//If yes reduce count by 1 else remove that entry from the map
			if(tokenMap.get(token) > 1) {
				tokenMap.put(token, tokenMap.get(token)-1);
			} else {
				tokenMap.remove(token);
			}
			//Remove that token from the list
			tokenList.remove(position);
			//Reconstruct the string builder and string
			//Commenting for now as expecting remove to be invoked after splitting of string into collection based on whitespace
			/*tokenStreamSb.setLength(0);
			if(null != tokenList && !tokenList.isEmpty() && tokenList.size()>0) {
				for(String s : tokenList) {
					tokenStreamSb.append(s + ApplicationConstants.SPACE_STRING);
				}
				tokenStreamSb.setLength((tokenStreamSb.length()-1));
			}
			tokenStream = tokenStreamSb.toString();*/
		}
	}
	
	/**
	 * Method to merge the current token with the previous token, assumes whitespace
	 * separator between tokens when merged. The token iterator should now point
	 * to the newly merged token (i.e. the previous one)
	 * @return true if the merge succeeded, false otherwise
	 */
	public boolean mergeWithPrevious() {
		//Check if the token stream has more than 1 element else merge not possible.
		//And check if position index is atleast 1
		if(tokenList.size()>1 && position>0 && position<tokenList.size()) {
			//Get the token & previous token that needs to be merged
			String token = tokenList.get(position);
			String pToken = tokenList.get(position-1);
			//Reduce the count of that token and previous token in the map
			//If count > 1 reduce count else remove that entry from the map
			if(tokenMap.get(token) > 1) {
				tokenMap.put(token, tokenMap.get(token)-1);
			} else {
				tokenMap.remove(token);
			}
			if(tokenMap.get(pToken) > 1) {
				tokenMap.put(pToken, tokenMap.get(pToken)-1);
			} else {
				tokenMap.remove(pToken);
			}
			//Now merge the token with the previous position
			tokenList.set(position-1, pToken + ApplicationConstants.SPACE_STRING + token);
			//Now check if the token map already has that new token
			//If yes then increase its count else add it to map
			String newToken = tokenList.get(position-1);
			if(tokenMap.containsKey(newToken)) {
				tokenMap.put(newToken, tokenMap.get(newToken)+1);
			} else {
				tokenMap.put(newToken, 1);
			}
			//Remove the element that was to be merged from the arraylist
			tokenList.remove(position);
			//Point the iterator position to the merged element
			position--;
			//No need to remove from String & StringBuilder variables
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Method to merge the current token with the next token, assumes whitespace
	 * separator between tokens when merged. The token iterator should now point
	 * to the newly merged token (i.e. the current one)
	 * @return true if the merge succeeded, false otherwise
	 */
	public boolean mergeWithNext() {
		//Check if token has more than 1 element else merge not possible.
		//And check if position index is at not more than second last position
		if(tokenList.size()>1 && position>=0 && position<tokenList.size()-1) {
			//Get the token and next token that needs to be merged
			String token = tokenList.get(position);
			String nToken = tokenList.get(position+1);
			//Reduce the count of that token and next token in the map
			//If count > 1 reduce count else remove that entry from the map
			if(tokenMap.get(token) > 1) {
				tokenMap.put(token, tokenMap.get(token)-1);
			} else {
				tokenMap.remove(token);
			}
			if(tokenMap.get(nToken) > 1) {
				tokenMap.put(nToken, tokenMap.get(nToken)-1);
			} else {
				tokenMap.remove(nToken);
			}
			//Now merge the token with the next position
			tokenList.set(position+1, token + ApplicationConstants.SPACE_STRING + nToken);
			//Now check if the token map already has that new token
			//If yes then increase its count else add it to map
			String newToken = tokenList.get(position+1);
			if(tokenMap.containsKey(newToken)) {
				tokenMap.put(newToken, tokenMap.get(newToken)+1);
			} else {
				tokenMap.put(newToken, 1);
			}
			//Remove the element that was to be merged from the arraylist
			tokenList.remove(position);
			//No need to set iterator position to the new element as after merge with next the previous element is removed 
			//which automatically shifts the elements to the left at which position iterator is already present.
			//No need to remove from String & StringBuilder variables
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Method to replace the current token with the given tokens
	 * The stream should be manipulated accordingly based upon the number of tokens set
	 * It is expected that remove will be called to delete a token instead of passing
	 * null or an empty string here.
	 * The iterator should point to the last set token, i.e, last token in the passed array.
	 * @param newValue: The array of new values with every new token as a separate element within the array
	 */
	public void set(String... newValue) {
		if(null != newValue && newValue.length>0 && null != tokenList && !tokenList.isEmpty() && tokenList.size()>0 && position>=0 && position<tokenList.size()) {
			//Get the string that needs to be replaced
			String token = tokenList.get(position);
			int count = 0;
			//Iterate over the array that has the new elements
			for(String s : newValue) {
				//Check if the elements are not null & blank
				if(null != s && !s.trim().equalsIgnoreCase(ApplicationConstants.BLANK_STRING)) {
					//Put the new elements in the token map if they are not already there else increase their count by 1
					if(tokenMap.containsKey(s)) {
						tokenMap.put(token, tokenMap.get(token)+1);
					} else {
						tokenMap.put(token, 1);
					}
					//If first set then replace the element else add to the next position
					if(count == 0) {
						//Replace the old element
						tokenList.set(position, s);
						count++;
					} else {
						//Add tokens to the next position
						tokenList.add(++position, s);
					}
				}
			}
			//If the collection has been updated then remove the token, update token map & sync up the string & string builder 
			if(count>0) {
				//Remove or reduce count of that string from the token map 
				if(tokenMap.get(token)>1) {
					tokenMap.put(token, tokenMap.get(token)-1);
				} else {
					tokenMap.remove(token);
				}
				//Sync up the string builder & string
				//Currently commenting for reason as given before in this class
				/*tokenStreamSb.setLength(0);
				for(String s: tokenList) {
					tokenStreamSb.append(s + ApplicationConstants.SPACE_STRING);
				}
				tokenStreamSb.setLength(tokenStreamSb.length()-1);
				tokenStream = tokenStreamSb.toString();*/
			}
			
		}
	}
	
	/**
	 * Iterator method: Method to reset the iterator to the start of the stream
	 * next must be called to get a token
	 */
	public void reset() {
		position = 0;
	}
	
	/**
	 * Iterator method: Method to set the iterator to beyond the last token in the stream
	 * previous must be called to get a token
	 */
	public void seekEnd() {
		if(null != tokenList && !tokenList.isEmpty() && tokenList.size()>0) {
			position = tokenList.size();
		} else {
			position = 0;
		}
	}
	
	/**
	 * Method to merge this stream with another stream
	 * @param other: The stream to be merged
	 */
	public void merge(TokenStream other) {
		//Check if stream is not null
		if(null != other) {
			//Check if other stream has tokens if yes add to list, map, string builder & string
			if(null != other.tokenList && !other.tokenList.isEmpty() && other.tokenList.size()>0) {
				for(String s : other.tokenList) {
					this.tokenList.add(s);
					if(tokenMap.containsKey(s)) {
						tokenMap.put(s, tokenMap.get(s)+1);
					} else {
						tokenMap.put(s, 1);
					}
					//tokenStreamSb.append(ApplicationConstants.SPACE_STRING + s);
				}
				//tokenStream = tokenStreamSb.toString();
			}
		}
	}
	
	public void syncMap() {
		if(null != tokenList && !tokenList.isEmpty() && tokenList.size()>0) {
			tokenMap.clear();
			for(String s: tokenList) {
				if(null != s && !s.trim().equalsIgnoreCase(ApplicationConstants.BLANK_STRING)) {
					if(tokenMap.containsKey(s)) {
						tokenMap.put(s, tokenMap.get(s) + 1);
					} else {
						tokenMap.put(s, 1);
					}
				}
			}
		}
	}

}
