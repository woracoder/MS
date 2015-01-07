/**
 * 
 */
package edu.buffalo.cse.ir.wikiindexer.parsers;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;

import edu.buffalo.cse.ir.wikiindexer.ApplicationConstants;
import edu.buffalo.cse.ir.wikiindexer.wikipedia.WikipediaDocument;
import edu.buffalo.cse.ir.wikiindexer.wikipedia.WikipediaParser;
import edu.buffalo.cse.ir.wikiindexer.wikipedia.XmlParser;

/**
 * @author nikhillo
 *
 */
public class Parser {
	
	/* */
	private final Properties props;
	
	/**
	 * 
	 * @param idxConfig
	 * @param parser
	 */
	public Parser(Properties idxProps) {
		props = idxProps;
	}
	
	/**
	 * 
	 * @param filename
	 * @param docs
	 */
	public void parse(String filename, Collection<WikipediaDocument> docs) {

		if(null != filename && !(filename = filename.trim()).equalsIgnoreCase(ApplicationConstants.BLANK_STRING)) {
			try {
				Long start = System.currentTimeMillis();
				List<WikipediaDocument> wikiObjList = new ArrayList<WikipediaDocument>();
				SAXParserFactory factory = SAXParserFactory.newInstance();
				SAXParser saxParser = factory.newSAXParser();
				XmlParser xmlParserObj = new XmlParser(wikiObjList);
				File file = new File(filename);
	  	        InputStream inputStream= new FileInputStream(file);
	  	        Reader reader = new InputStreamReader(inputStream, ApplicationConstants.UTF8_FORMAT);
	  	        InputSource is = new InputSource(reader);
	  	        is.setEncoding(ApplicationConstants.UTF8_FORMAT);
				saxParser.parse(is, xmlParserObj);
				System.out.println("Time taken for XML parsing = "+(System.currentTimeMillis()-start));
				start = System.currentTimeMillis();
				for (WikipediaDocument doc : wikiObjList) {
					WikipediaParser.parseWikiMarkupText(doc);
					add(doc, docs);
				}
				System.out.println("Time taken for Wiki parsing = "+(System.currentTimeMillis()-start));
			} catch (Exception e) {
	             e.printStackTrace();
			}
		}
	}

	/**
	 * Method to add the given document to the collection.
	 * PLEASE USE THIS METHOD TO POPULATE THE COLLECTION AS YOU PARSE DOCUMENTS
	 * For better performance, add the document to the collection only after
	 * you have completely populated it, i.e., parsing is complete for that document.
	 * @param doc: The WikipediaDocument to be added
	 * @param documents: The collection of WikipediaDocuments to be added to
	 */
	private synchronized void add(WikipediaDocument doc, Collection<WikipediaDocument> documents) {
		documents.add(doc);
	}
}
