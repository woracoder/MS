/**
 * 
 */
package edu.buffalo.cse.ir.wikiindexer.wikipedia;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import edu.buffalo.cse.ir.wikiindexer.ApplicationConstants;
import edu.buffalo.cse.ir.wikiindexer.indexer.INDEXFIELD;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenStream;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.Tokenizer;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenizerException;
import edu.buffalo.cse.ir.wikiindexer.wikipedia.WikipediaDocument.Section;

/**
 * A Callable document transformer that converts the given WikipediaDocument object
 * into an IndexableDocument object using the given Tokenizer
 * @author nikhillo
 *
 */
public class DocumentTransformer implements Callable<IndexableDocument> {
	
	private Map<INDEXFIELD, Tokenizer> tokenizerMap;
	private WikipediaDocument doc;
	private TokenStream termTs;
	private TokenStream authorTs;
	private TokenStream categoryTs;
	private TokenStream linkTs;
	
	/**
	 * Default constructor, DO NOT change
	 * @param tknizerMap: A map mapping a fully initialized tokenizer to a given field type
	 * @param doc: The WikipediaDocument to be processed
	 */
	public DocumentTransformer(Map<INDEXFIELD, Tokenizer> tknizerMap, WikipediaDocument doc) {
		this.tokenizerMap = new HashMap<INDEXFIELD, Tokenizer>(tknizerMap);
		this.doc = doc;
	}
	
	/**
	 * Method to trigger the transformation
	 * @throws TokenizerException Inc ase any tokenization error occurs
	 */
	public IndexableDocument call() throws TokenizerException {
		
		StringBuilder termSb = new StringBuilder();
		//StringBuilder catSb = new StringBuilder();
		//StringBuilder linkSb = new StringBuilder();
		//Check if doc is null
		if(null != doc) {
			//Append publish date to term SB
			if(null != doc.getPublishDate() && !doc.getPublishDate().toString().trim().equalsIgnoreCase(ApplicationConstants.BLANK_STRING)) {
				termSb.append(doc.getPublishDate().toString().trim() + ApplicationConstants.SPACE_STRING);
			}
			//Append title to term SB
			if(null != doc.getTitle() && !doc.getTitle().trim().equalsIgnoreCase(ApplicationConstants.BLANK_STRING)) {
				termSb.append(doc.getTitle().trim() + ApplicationConstants.SPACE_STRING);
			}
			//Append section text & title to the term SB
			List<Section> sectionList = doc.getSections();
			String secText = null, secTitle = null;
			if(null != sectionList && !sectionList.isEmpty() && sectionList.size()>0) {
				for(Section s: sectionList) {
					if(null != s) {
						if(null != s.getTitle() && !(secTitle = s.getTitle().trim()).equalsIgnoreCase(ApplicationConstants.BLANK_STRING)) {
							termSb.append(secTitle + ApplicationConstants.SPACE_STRING);
						}
						if(null != s.getText() && !(secText = s.getText().trim()).equalsIgnoreCase(ApplicationConstants.BLANK_STRING)) {
							termSb.append(secText + ApplicationConstants.SPACE_STRING);
						}
					}
				}
			}
			// If the SB has a text & space its length will be > 1
			if (termSb.length() > 1) {
				// Remove the last whitespace i.e. trim
				termSb.setLength(termSb.length() - 1);
				// Initialize the category token stream
				this.termTs = new TokenStream(termSb);
			}
			
			//Create the author token stream
			if(null != doc.getAuthor() && !doc.getAuthor().trim().equalsIgnoreCase(ApplicationConstants.BLANK_STRING)) {
				this.authorTs = new TokenStream(doc.getAuthor().trim());
			}
			
			//Create the category token stream
			//Get the category list
			List<String> categoryList = doc.getCategories();
			String category = null;
			//Check if there are categories in the list
			if(null != categoryList && !categoryList.isEmpty() && categoryList.size()>0) {
				int count = 0;
				for(String cat : categoryList) {
					count++;
					//Check if categories are not null or blank
					if(null != cat && !(category = cat.trim()).equalsIgnoreCase(ApplicationConstants.BLANK_STRING)) {
						this.categoryTs = new TokenStream(category);
						break;
						//catSb.append(category + ApplicationConstants.SPACE_STRING);
					}
				}
				if(count<categoryList.size()) {
					this.categoryTs.append(Arrays.copyOfRange(categoryList.toArray(new String[categoryList.size()]), count, categoryList.size()));
				}
				/*// If the SB has a link & space its length will be > 1
				if (catSb.length() > 1) {
					// Remove the last whitespace i.e. trim
					catSb.setLength(catSb.length() - 1);
					// Initialize the category token stream
					this.categoryTs = new TokenStream(catSb);
				}*/
			}
			
			// Create the link token stream
			// Get the links from the doc
			String[] links = doc.getLinks().toArray(new String[0]);
			String link = null;
			// Check if there are links
			if (null != links && links.length > 0) {
				int count = 0;
				// Iterate over the links array
				for (String s : links) {
					count++;
					// Check if the links are not null & blank
					if (null != s && !(link = s.trim()).equalsIgnoreCase(ApplicationConstants.BLANK_STRING)) {
						// Append link + space to the SB
						this.linkTs = new TokenStream(link);
						break;
						//linkSb.append(link + ApplicationConstants.SPACE_STRING);
					}
				}
				if(count < links.length) {
					this.linkTs.append(Arrays.copyOfRange(links, count, links.length));
				}
				/*// If the SB has a link & space its length will be > 1
				if (linkSb.length() > 1) {
					// Remove the last whitespace i.e. trim
					linkSb.setLength(linkSb.length() - 1);
					// Initialize the link token stream
					this.linkTs = new TokenStream(linkSb);
				}*/
			}
		}
		
		IndexableDocument idoc = new IndexableDocument(doc.getTitle());
			
		for(Map.Entry<INDEXFIELD, Tokenizer> e : tokenizerMap.entrySet()) {
			if(e.getKey().name().equalsIgnoreCase(ApplicationConstants.TERM_FIELD) && null != termTs) {
				e.getValue().tokenize(termTs);
				idoc.addField(e.getKey(), termTs);
			} else if(e.getKey().name().equalsIgnoreCase(ApplicationConstants.AUTHOR_FIELD) && null != authorTs) {
				e.getValue().tokenize(authorTs);
				idoc.addField(e.getKey(), authorTs);
			} else if(e.getKey().name().equalsIgnoreCase(ApplicationConstants.CATEGORY_FIELD) && null != categoryTs) {
				e.getValue().tokenize(categoryTs);
				idoc.addField(e.getKey(), categoryTs);
			} else if(e.getKey().name().equalsIgnoreCase(ApplicationConstants.LINK_FIELD) && null != linkTs) {
				e.getValue().tokenize(linkTs);
				idoc.addField(e.getKey(), linkTs);
			}
		}
		
		return idoc;
	}
	
}
