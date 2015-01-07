package edu.buffalo.cse.ir.wikiindexer.wikipedia;

import java.text.ParseException;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import edu.buffalo.cse.ir.wikiindexer.ApplicationConstants;

public class XmlParser extends DefaultHandler {

	public XmlParser(List<WikipediaDocument> wikiObjList) {
		this.wikiObjList = wikiObjList;
	}

	boolean page = false;
	boolean title = false;
	boolean id = false;
	boolean revision = false;
	boolean timestamp = false;
	boolean username = false;
	boolean ip = false;
	boolean text = false;

	int idFromXml;
	String ttl, timestampFromXml, authorFromXml;
	StringBuilder textFromXml = new StringBuilder();
	StringBuilder titleFromXml = new StringBuilder();
	StringBuilder idFromXmlSb = new StringBuilder();
	StringBuilder timestampFromXmlSb = new StringBuilder();
	StringBuilder authorFromXmlSb = new StringBuilder();
	List<WikipediaDocument> wikiObjList;

	/**
	 * XML processing starts here. Blank URI & localName as namespaces are not
	 * being processed. Qualified name is the name of the tags encountered.
	 * Attribute object populated if element has any attributes.
	 */
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {

		if (!page && qName.equalsIgnoreCase(ApplicationConstants.PAGE_TAG)) {
			page = true;
		} else if (qName.equalsIgnoreCase(ApplicationConstants.TITLE_TAG)) {
			title = true;
		} else if (!revision && qName.equalsIgnoreCase(ApplicationConstants.ID_TAG)) {
			id = true;
		} else if (qName.equalsIgnoreCase(ApplicationConstants.REVISION_TAG)) {
			revision = true;
		} else if (qName.equalsIgnoreCase(ApplicationConstants.TIMESTAMP_TAG)) {
			timestamp = true;
		} else if (qName.equalsIgnoreCase(ApplicationConstants.IP_TAG)) {
			ip = true;
		} else if (qName.equalsIgnoreCase(ApplicationConstants.USERNAME_TAG)) {
			username = true;
		} else if (qName.equalsIgnoreCase(ApplicationConstants.TEXT_TAG)) {
			text = true;
		}
	}

	/**
	 * ch is array of characters found inside element
	 * 
	 */
	public void characters(char ch[], int start, int length)
			throws SAXException {

		if (page) {
			if (title) {
				titleFromXml = titleFromXml.append(new String(ch, start, length));
			} else if (!revision && id) {
				idFromXmlSb = idFromXmlSb.append(new String(ch, start, length));
			} else if (timestamp) {
				timestampFromXmlSb = timestampFromXmlSb.append(new String(ch, start, length));
			} else if (ip) {
				authorFromXmlSb = authorFromXmlSb.append(new String(ch, start, length));
			} else if (username) {
				authorFromXmlSb = authorFromXmlSb.append(new String(ch, start, length));
			} else if (text) {
				textFromXml = textFromXml.append(new String(ch, start, length));
			}
		}
	}

	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (text && qName.equalsIgnoreCase(ApplicationConstants.TEXT_TAG)) {
			text = false;
		} else if (username) {
			authorFromXml = authorFromXmlSb.toString();
			authorFromXmlSb.setLength(0);
			username = false;
		} else if (ip) {
			authorFromXml = authorFromXmlSb.toString();
			authorFromXmlSb.setLength(0);
			ip = false;
		} else if (timestamp) {
			timestampFromXml = timestampFromXmlSb.toString();
			timestampFromXmlSb.setLength(0);
			timestamp = false;
		} else if (!revision && id) {
			idFromXml = Integer.parseInt(idFromXmlSb.toString());
			idFromXmlSb.setLength(0);
			id = false;
		} else if (title) {
			ttl = titleFromXml.toString();
			titleFromXml.setLength(0);
			title = false;
		} else if (page && qName.equalsIgnoreCase(ApplicationConstants.PAGE_TAG)) {
			WikipediaDocument obj = null;
			try {
				obj = new WikipediaDocument(idFromXml, timestampFromXml, authorFromXml, ttl);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			obj.setTextFromXml(textFromXml.toString());
			wikiObjList.add(obj);
			textFromXml.setLength(0);
			page = false;
			revision = false;
		}
	}

}
