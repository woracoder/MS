/**
 * 
 */
package edu.buffalo.cse.ir.wikiindexer.wikipedia;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.buffalo.cse.ir.wikiindexer.ApplicationConstants;

/**
 * @author nikhillo This class implements Wikipedia markup processing. Wikipedia
 *         markup details are presented here:
 *         http://en.wikipedia.org/wiki/Help:Wiki_markup It is expected that all
 *         methods marked "todo" will be implemented by students. All methods
 *         are static as the class is not expected to maintain any state.
 */
public class WikipediaParser {

	public static void parseWikiMarkupText(WikipediaDocument doc) {

		doc.setTextFromXml(parseTextFormatting(doc.getTextFromXml()));		
		doc.setTextFromXml(parseListItem(doc.getTextFromXml()));
		parseCategories(doc);
		parseForLinks(doc);
		parseSections(doc);
		doc.setTextFromXml(null);
		parseSectionTags(doc);
		parseSectionTemplates(doc);
		
	}

	private static void parseSectionTags(WikipediaDocument doc) {
		for (WikipediaDocument.Section s : doc.getSections()) {
			s.setText(parseTagFormatting(s.getText()));
		}
	}

	private static void parseSectionTemplates(WikipediaDocument doc) {
		for (WikipediaDocument.Section s : doc.getSections()) {
			s.setText(parseTemplates(s.getText()));
		}
	}

	

	// Check to remove category or not
	public static void parseCategories(WikipediaDocument doc) {
		String text = null;
		if (null != doc.getTextFromXml() && !(text = doc.getTextFromXml()).equalsIgnoreCase(ApplicationConstants.BLANK_STRING)) {
			Pattern p = Pattern.compile("\\[\\[[Cc][Aa][Tt][Ee][Gg][Oo][Rr][Yy][:](.*?)\\]\\]");
			Matcher m = p.matcher(text);
			while (m.find()) {
				doc.addCategory(m.group(1).replaceAll("[\\|]+", ApplicationConstants.BLANK_STRING).trim());
			}
			doc.setTextFromXml(text.replaceAll("\\[\\[[Cc][Aa][Tt][Ee][Gg][Oo][Rr][Yy][:](.*?)\\]\\]", "$1"));
		}
	}

	public static void parseSections(WikipediaDocument doc) {
		
		String text = null;
		if(null != doc.getTextFromXml() && !(text = doc.getTextFromXml().trim()).equalsIgnoreCase(ApplicationConstants.BLANK_STRING)) {
			if(!text.contains("==")) {
				doc.addSection("Default", text);
			} else {
				Pattern p = Pattern.compile("(={2,6})(.*?)\\1([\\s\\S]*?)(?=(={2,6}|\\z))");
				Matcher m = p.matcher(text);
				if (m.find()) {
					if (m.start() != 0) {
						doc.addSection("Default", text.substring(0, m.start()));
					}
					doc.addSection(m.group(2).trim(), m.group(3));
				}
				while (m.find()) {
					doc.addSection(m.group(2).trim(), m.group(3));
				}
			}
			
		}
	}

	/**
	 * Method to parse section titles or headings. Refer:
	 * http://en.wikipedia.org/wiki/Help:Wiki_markup#Sections
	 * 
	 * @param titleStr
	 *            : The string to be parsed
	 * @return The parsed string with the markup removed
	 */
	public static String parseSectionTitle(String titleStr) {
		return (null != titleStr) 
				? titleStr.replaceAll("(={2,6})(.*?)\\1", "$2").trim() 
				: null;
	}

	/**
	 * Method to parse list items (ordered, unordered and definition lists).
	 * Refer: http://en.wikipedia.org/wiki/Help:Wiki_markup#Lists
	 * 
	 * @param itemText
	 *            : The string to be parsed
	 * @return The parsed string with markup removed
	 */
	public static String parseListItem(String itemText) {

		if (null != itemText) {
			if(!(itemText = itemText.trim()).equalsIgnoreCase(ApplicationConstants.BLANK_STRING)) {
				// Replace all unordered list with white space
				itemText = itemText.replaceAll("(^|\n|\r)[*]+", ApplicationConstants.BLANK_STRING);
				// Replace all ordered list with white space
				itemText = itemText.replaceAll("(^|\n|\r)[#]+", ApplicationConstants.BLANK_STRING);
				// Replace all definition lists
				Pattern p = Pattern.compile("(^|\n|\r);(.*)(:|(\n{1,}:))(.*)\n{1,}(:(.*)\n{1,})*");
				Matcher m = p.matcher(itemText);
				while (m.find()) {
					itemText = itemText.replace(m.group(), m.group().replaceAll(";|:", ApplicationConstants.BLANK_STRING));
				}
				// Replace all definition lists for satisfying test case
				itemText = itemText.replaceAll("(^|\n|\r):", ApplicationConstants.BLANK_STRING);
				itemText = itemText.replaceAll("\\s+", ApplicationConstants.SPACE_STRING);
				return itemText.trim();
			} else {
				return ApplicationConstants.BLANK_STRING;
			}
		} else {
			return null;
		}
	}

	/**
	 * Method to parse text formatting: bold and italics. Refer:
	 * http://en.wikipedia.org/wiki/Help:Wiki_markup#Text_formatting first point
	 * 
	 * @param text
	 *            : The text to be parsed
	 * @return The parsed text with the markup removed
	 * 
	 *         From the wiki link mentioned above we have to Remove all ''(.*)''
	 *         & '''(.*)''' & '''''(.*)''''' & {{smallcaps|(.*)}} &
	 *         [[help:xyz|(.*)]] Keep only the (.*) stuff & replace all the
	 *         remaining stuff by nothing.
	 */
	public static String parseTextFormatting(String text) {
		if (null != text) {
			if(!(text = text.trim()).equalsIgnoreCase(ApplicationConstants.BLANK_STRING)) {
				text = text.replaceAll("'{5}|'{3}|'{2}|([Ss]mallcaps\\|)|([Hh]elp:(.*)?\\|)", ApplicationConstants.BLANK_STRING);
				text = text.replaceAll("\\s+", ApplicationConstants.SPACE_STRING);
				return text.trim();
			} else {
				return ApplicationConstants.BLANK_STRING;
			}
		} else {
			return null;
		}
	}

	/**
	 * Method to parse *any* HTML style tags like: <xyz ...> </xyz> For most
	 * cases, simply removing the tags should work.
	 * 
	 * @param text
	 *            : The text to be parsed
	 * @return The parsed text with the markup removed.
	 */
	public static String parseTagFormatting(String text) {

		if (null != text) {
			if(!(text= text.trim()).equalsIgnoreCase(ApplicationConstants.BLANK_STRING)) {
				text = text.replaceAll("(<[\\w]+.*?/>)",ApplicationConstants.BLANK_STRING);
				text = text.replaceAll("<([\\w]+).*?>([\\s\\S]*?)</\\1>", "$2");
				text = text.replaceAll("&lt;([\\w]+).*?&gt;([\\s\\S]*?)&lt;/\\1&gt;", "$2");
				text = text.replaceAll("\\s+", ApplicationConstants.SPACE_STRING);
				text = text.trim();
				if (text.matches("(<[\\w]+.*?/>)")
						|| text.matches("(<([\\w]+).*?>([\\s\\S]*?)</\\1>)")
						|| text.matches("(&lt;([\\w]+).*?&gt;([\\s\\S]*?)&lt;/\\1&gt;)")) {
					text = parseTagFormatting(text);
				}
				return text.trim();
			} else {
				return ApplicationConstants.BLANK_STRING;
			}
		} else {
			return null;
		}
	}

	/**
	 * Method to parse wikipedia templates. These are *any* {{xyz}} tags For
	 * most cases, simply removing the tags should work.
	 * 
	 * @param text
	 *            : The text to be parsed
	 * @return The parsed text with the markup removed
	 */
	public static String parseTemplates(String text) {

		if (null != text && !(text = text.trim()).equalsIgnoreCase(ApplicationConstants.BLANK_STRING)) {
			text = text.replaceAll("\\{\\{(.*?)\\}\\}", ApplicationConstants.BLANK_STRING);
			text = text.replaceAll("\\{\\{([\\s|\\S]*?)\\}\\}", ApplicationConstants.BLANK_STRING);
			text = text.replaceAll("\\s+", ApplicationConstants.SPACE_STRING);
			return text.trim();
		} else {
			return null;
		}
	}

	private static void parseForLinks(WikipediaDocument doc) {

		String link;
		String[] links;
		String textFromXml = null;
		if(null != doc.getTextFromXml() && !(textFromXml = doc.getTextFromXml()).equalsIgnoreCase(ApplicationConstants.BLANK_STRING)) {
			textFromXml = doc.getTextFromXml();
			//Pattern p = Pattern.compile("(={2,6})(.*?)\\1([\\s\\S]*?)(?=(={2,6}|\\z))");
			String linkRegex ="(\\[\\[.*?\\]\\])";
			String externalLinkRegex ="(\\[.*?\\])";
			//String linkRegex ="(\\[{1,2})(.*?)]";
			//Pattern p = Pattern.compile("\\[\\[(.)*?\\]\\][^\\s\\n\\r\\t$']*");
			
			Pattern p = Pattern.compile(linkRegex);
			Matcher m = p.matcher(textFromXml);
			try {
				while (m.find()) {
					link = m.group();
					if (m.start()-1>= 0 && ( m.end() + 1) <textFromXml.length()){
						 if (textFromXml.charAt(m.start() - 1) == '"' && textFromXml.charAt(m.end() + 1) == '"') {
							 continue;
						 }
					}
					links = WikipediaParser.parseLinks(link);
					textFromXml=textFromXml.replaceFirst(linkRegex,Matcher.quoteReplacement(links[0]));
					doc.addLink(links[1]);
				}
				
				p = Pattern.compile(externalLinkRegex);
			    m = p.matcher(textFromXml);
					while (m.find()) {
						link = m.group();
						if (m.start()-1>= 0 && ( m.end() + 1) <textFromXml.length()){
							 if (textFromXml.charAt(m.start() - 1) == '"' && textFromXml.charAt(m.end() + 1) == '"') {
								 continue;
							 }
						}
						links = WikipediaParser.parseLinks(link);
						textFromXml=textFromXml.replaceFirst(linkRegex,Matcher.quoteReplacement(links[0]));
						doc.addLink(links[1]);
					}
					
				
				doc.setTextFromXml(textFromXml);
			}
			catch(Exception e){
				e.printStackTrace();
			}
			
		}
	}
	
	/**
	 * Method to parse links and URLs. Refer:
	 * http://en.wikipedia.org/wiki/Help:Wiki_markup#Links_and_URLs
	 * 
	 * @param text
	 *            : The text to be parsed
	 * @return An array containing two elements as follows - The 0th element is
	 *         the parsed text as visible to the user on the page The 1st
	 *         element is the link url
	 */
	public static String[] parseLinks(String text) {

		
		if (text == null || text.trim().equalsIgnoreCase(ApplicationConstants.BLANK_STRING))
			return new String[] { ApplicationConstants.BLANK_STRING, ApplicationConstants.BLANK_STRING };
		/*
		 * 1.if there is any text after | then that is how link is going to be
		 * displayed to the user
		 */
		/* when a link starts with # please add the current page */
		StringBuilder linkVisibleToUser = new StringBuilder(text);
		StringBuilder linkActual = new StringBuilder(text);
		String linkActualString;
		String linkVisibleToUserString;
		final String parenthesisRegex = "\\([^)]*\\)";//speed enhancement
		//final String commaRegex = "[,][^]]*?";
		final String noWikiRegex = "[<][nN][oO][wW][iI][kK][iI][\\s]*[/][>]";
		final String categoryLinkRegex = "\\[\\[(:Category)";
		final String categoryRegex = "\\[\\[(Category:)";
		final String fileRegex = "\\[\\[[fF][iI][lL][eE][:]";
		final String languageRegex = "\\[\\[[a-z][a-z][:]";
		final String externalLinksRegex = "\\[(http://)[^]]*\\]";
		//final String doubleBracketRegex = "\\[\\[(.)*?\\]\\]";
		//final String singleBracketRegex = "\\[[^]]*\\]";
		boolean categoryLink = false;
		Pattern p;
		Matcher m;
		//Pattern p1 ;
		//Matcher m1;
		
		//p = Pattern.compile(doubleBracketRegex);
		//m = p.matcher(text);
		//if (m.find()) {
		if (text.contains("[[") && text.contains("]]") && (text.indexOf("[[") < text.indexOf("]]"))) {
			/* removing <nowiki/> tag and text thereafter */
			p = Pattern.compile(noWikiRegex);
			m = p.matcher(linkVisibleToUser);
	
			if (m.find()) {
				//linkVisibleToUser.delete(m.end(), linkVisibleToUser.length());
				linkVisibleToUser.delete(m.start(), m.end());
				text = linkVisibleToUser.toString();
			}
			/* <nowiki/> tag removal end */
	
			/* Checking for Language Links */
			//populateLanguageCode(languageCode);
			p = Pattern.compile(languageRegex);
			m = p.matcher(text);
			if (m.find()) {
				if (languageCode.contains(text.substring(m.start() + 2, m.end() - 1))) {
					linkActual = new StringBuilder(ApplicationConstants.BLANK_STRING);
				}
			}
			/* Parsing Language Links Ends */
			else {
	
					/* parsing category */
					p = Pattern.compile(categoryRegex);
					m = p.matcher(text);
					if (m.find()) {
						linkActual = new StringBuilder(ApplicationConstants.BLANK_STRING);
						linkVisibleToUser = new StringBuilder(linkVisibleToUser.toString().replace(m.group(), ApplicationConstants.BLANK_STRING));
					}/* Parsing Category Ends */
					else {
						/* Parsing for links containing pipe */
						if (text.indexOf('|') != -1) {
							/* parsing category links */
							p = Pattern.compile(categoryLinkRegex);
							m = p.matcher(text);
							if (m.find()) {
								//linkVisibleToUser.delete(0, 1);
								/*linkActual.delete(
								  linkActual.indexOf("[[:Category"),
								  linkActual.indexOf(":Category") + 3);// removing : and [[
								  linkActual.delete(linkActual.indexOf("|"), linkActual.length());*/
								linkActual= new StringBuilder(ApplicationConstants.BLANK_STRING);
								categoryLink = true;
							}
							/* end of parsing category links */
							
							/* Computation for linkActual */
							if (!categoryLink) {
								linkActual = new StringBuilder(text.substring(text.indexOf("[[") + 2, text.indexOf("|")));
								if (text.contains(":") || text.contains("#")) {
									linkActual = new StringBuilder(ApplicationConstants.BLANK_STRING);
								}
							}
							/* End Of Computation for linkActual */
	
							/* computation for linkVisibleToUser */
							if ((linkVisibleToUser.indexOf("]]") - linkVisibleToUser.lastIndexOf("|")) > 1) {
								linkVisibleToUser.delete(linkVisibleToUser.indexOf("[["), linkVisibleToUser.lastIndexOf("|") + 1);
							} else {
								/*Considering the # case */
								if (linkVisibleToUser.indexOf("#")!=-1){
									linkVisibleToUser.delete(linkVisibleToUser.indexOf("|"), linkVisibleToUser.length());
								} else {
								/* removing pipe */
								linkVisibleToUser.delete(linkVisibleToUser.indexOf("|"), linkVisibleToUser.indexOf("|") + 1);
								/*
								 * If Link is to a section, then retain the entire
								 * link and don't omit anything
								 */
								/* parenthesis check */
								p = Pattern.compile(parenthesisRegex);
								m = p.matcher(linkVisibleToUser);
								if (m.find()) {
									linkVisibleToUser.delete(m.start(), m.end());
								}
	
								/* comma check *//*
												 * p=Pattern.compile(commaRegex);
												 * m=p.matcher(linkVisibleToUser);
												 * if (m.find()){
												 * linkVisibleToUser.delete
												 * (m.start(), m.end()); }
												 */
	
								/* comma check */
								if (linkVisibleToUser.toString().contains(",")) {
									linkVisibleToUser.delete(linkVisibleToUser.indexOf(","), linkVisibleToUser.length());
								}
								/* namespace check */
								if (text.contains(":")) {
									if (categoryLink) {
										linkVisibleToUser.delete(0, linkVisibleToUser.indexOf(":Category:") + 10);
									} else {
										linkVisibleToUser.delete(0, linkVisibleToUser.indexOf(":") + 1);
									}
									// linkActual.delete(0,linkActual.indexOf(":") +
									// 1); //But we are not interested in namespaces
									// so removing them
								}
								}
							}
						} else {
							/* Parsing for File */
							p = Pattern.compile(fileRegex);
							m = p.matcher(text);
							if (m.find()) {
								linkVisibleToUser = new StringBuilder(ApplicationConstants.BLANK_STRING);
								linkActual = new StringBuilder(ApplicationConstants.BLANK_STRING);
							}
							/* Parsing For File Ends */
							else {
								/* parsing category links */
								p = Pattern.compile(categoryLinkRegex);
								m = p.matcher(text);
								if (m.find()) {
									linkVisibleToUser = new StringBuilder(text.substring(text.indexOf("Category"), text.indexOf("]]")));
								/*	linkActual = new StringBuilder(text.substring(
											text.indexOf("Category"),
											text.indexOf("]]")));*/
									linkActual = new StringBuilder(ApplicationConstants.BLANK_STRING);
								}
								/* end of parsing category links */
								else {
									/* Parsing the actual link */
	//								if  ((text.indexOf("[[") + 2) < text.indexOf("]]")){
									linkActual = new StringBuilder(text.substring(text.indexOf("[[") + 2,text.indexOf("]]")));
	//								}
	//								else
	//									{
	//									linkActual = new StringBuilder("");
	//									}
									if (text.contains(":") || text.contains("#")) {
										linkActual = new StringBuilder(ApplicationConstants.BLANK_STRING);
									}
								}
							}
						}
					}
				}
		/* removal of start and end tag */
		if (linkVisibleToUser.indexOf("[[") != -1) {
			linkVisibleToUser.delete(linkVisibleToUser.indexOf("[["), linkVisibleToUser.indexOf("[[") + 2);
		}
		if (linkVisibleToUser.indexOf("]]") != -1) {
			linkVisibleToUser.delete(linkVisibleToUser.indexOf("]]"), linkVisibleToUser.indexOf("]]") + 2);
		}
		/* end of removal of start and end tag */
		
		}
		else
		{
			/* Parsing External Links */
			linkActual = new StringBuilder("");
			p = Pattern.compile(externalLinksRegex);
			m = p.matcher(linkVisibleToUser);
			if (m.find()) {		
				/*System.out.println("abhishek " +m.group());
				System.out.println("linkActual " +linkActual);
				System.out.println("linkVisibleToUser " +linkVisibleToUser);
				System.out.println("start = " +m.start());
				System.out.println("end index = " +linkVisibleToUser.indexOf(" "));*/
				if (linkVisibleToUser.indexOf(" ")> m.start() ) {
					linkVisibleToUser.delete(m.start(),linkVisibleToUser.indexOf(" ") );
					linkVisibleToUser.delete(linkVisibleToUser.indexOf("]"), m.end());
				} else {
				linkVisibleToUser=	 new StringBuilder("");
				}
			}
			else 
			{
//			p = Pattern.compile(singleBracketRegex);
//			m = p.matcher(text);
//			if (m.find()) {
				linkVisibleToUser= new StringBuilder("");
//			}
		}
		}

		linkActualString = linkActual.toString();
		linkActualString = linkActualString.trim();
		linkVisibleToUserString = linkVisibleToUser.toString();
		linkVisibleToUserString = linkVisibleToUserString.trim();
		if (linkActualString.length() > 0) {
			// Capitalizing the First Alphabet
//			linkActualString = new Character(Character.toUpperCase(linkActual.charAt(0))).toString() + linkActual.subSequence(1, linkActual.length());
			linkActualString = linkActualString.substring(0, 1).toUpperCase() + linkActualString.substring(1);
		}

		// Replacing all spaces with underscores
		linkActualString = linkActualString.toString().replaceAll(ApplicationConstants.SPACE_STRING, "_");
		return new String[] { linkVisibleToUserString, linkActualString };
	}

	static HashSet<String> languageCode = new HashSet<String>();
	static {
		languageCode.add("aa");
		languageCode.add("ab");
		languageCode.add("af");
		languageCode.add("am");
		languageCode.add("ar");
		languageCode.add("as");
		languageCode.add("ay");
		languageCode.add("az");
		languageCode.add("ba");
		languageCode.add("be");
		languageCode.add("bg");
		languageCode.add("bh");
		languageCode.add("bi");
		languageCode.add("bn");
		languageCode.add("bo");
		languageCode.add("br");
		languageCode.add("ca");
		languageCode.add("co");
		languageCode.add("cs");
		languageCode.add("cy");
		languageCode.add("da");
		languageCode.add("de");
		languageCode.add("dz");
		languageCode.add("el");
		languageCode.add("en");
		languageCode.add("eo");
		languageCode.add("es");
		languageCode.add("et");
		languageCode.add("eu");
		languageCode.add("fa");
		languageCode.add("fi");
		languageCode.add("fj");
		languageCode.add("fo");
		languageCode.add("fr");
		languageCode.add("fy");
		languageCode.add("ga");
		languageCode.add("gd");
		languageCode.add("gl");
		languageCode.add("gn");
		languageCode.add("gu");
		languageCode.add("ha");
		languageCode.add("hi");
		languageCode.add("he");
		languageCode.add("hr");
		languageCode.add("hu");
		languageCode.add("hy");
		languageCode.add("ia");
		languageCode.add("id");
		languageCode.add("ie");
		languageCode.add("ik");
		languageCode.add("in");
		languageCode.add("is");
		languageCode.add("it");
		languageCode.add("iu");
		languageCode.add("iw");
		languageCode.add("ja");
		languageCode.add("ji");
		languageCode.add("jw");
		languageCode.add("ka");
		languageCode.add("kk");
		languageCode.add("kl");
		languageCode.add("km");
		languageCode.add("kn");
		languageCode.add("ko");
		languageCode.add("ks");
		languageCode.add("ku");
		languageCode.add("ky");
		languageCode.add("la");
		languageCode.add("ln");
		languageCode.add("lo");
		languageCode.add("lt");
		languageCode.add("lv");
		languageCode.add("mg");
		languageCode.add("mi");
		languageCode.add("mk");
		languageCode.add("ml");
		languageCode.add("mn");
		languageCode.add("mo");
		languageCode.add("mr");
		languageCode.add("ms");
		languageCode.add("mt");
		languageCode.add("my");
		languageCode.add("na");
		languageCode.add("ne");
		languageCode.add("nl");
		languageCode.add("no");
		languageCode.add("oc");
		languageCode.add("om");
		languageCode.add("or");
		languageCode.add("pa");
		languageCode.add("pl");
		languageCode.add("ps");
		languageCode.add("pt");
		languageCode.add("qu");
		languageCode.add("rm");
		languageCode.add("rn");
		languageCode.add("ro");
		languageCode.add("ru");
		languageCode.add("rw");
		languageCode.add("sa");
		languageCode.add("sd");
		languageCode.add("sg");
		languageCode.add("sh");
		languageCode.add("si");
		languageCode.add("sk");
		languageCode.add("sl");
		languageCode.add("sm");
		languageCode.add("sn");
		languageCode.add("so");
		languageCode.add("sq");
		languageCode.add("sr");
		languageCode.add("ss");
		languageCode.add("st");
		languageCode.add("su");
		languageCode.add("sv");
		languageCode.add("sw");
		languageCode.add("ta");
		languageCode.add("te");
		languageCode.add("tg");
		languageCode.add("th");
		languageCode.add("ti");
		languageCode.add("tk");
		languageCode.add("tl");
		languageCode.add("tn");
		languageCode.add("to");
		languageCode.add("tr");
		languageCode.add("ts");
		languageCode.add("tt");
		languageCode.add("tw");
		languageCode.add("ug");
		languageCode.add("uk");
		languageCode.add("ur");
		languageCode.add("uz");
		languageCode.add("vi");
		languageCode.add("vo");
		languageCode.add("wo");
		languageCode.add("xh");
		languageCode.add("yi");
		languageCode.add("yo");
		languageCode.add("za");
		languageCode.add("zh");
		languageCode.add("zu");
	}

}
