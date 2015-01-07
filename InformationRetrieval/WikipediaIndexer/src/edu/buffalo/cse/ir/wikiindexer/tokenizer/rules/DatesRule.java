package edu.buffalo.cse.ir.wikiindexer.tokenizer.rules;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.buffalo.cse.ir.wikiindexer.ApplicationConstants;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenStream;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenizerException;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.rules.TokenizerRule.RULENAMES;

@RuleClass(className = RULENAMES.DATES)
public class DatesRule implements TokenizerRule {
	
	static Map<String, Integer> monthMap = new HashMap<String, Integer>();
	static {
		monthMap.put("January", 1);
		monthMap.put("February", 2);
		monthMap.put("March", 3);
		monthMap.put("April", 4);
		monthMap.put("May", 5);
		monthMap.put("June", 6);
		monthMap.put("July", 7);
		monthMap.put("August", 8);
		monthMap.put("September", 9);
		monthMap.put("October", 10);
		monthMap.put("November", 11);
		monthMap.put("December", 12);
	}
	
	@Override
	public void apply(TokenStream stream) throws TokenizerException {
		
		if(null != stream) {
			String text=null;
			if(null != stream.getTokenStreamSb() && !(text = stream.getTokenStreamSb().toString().trim()).equalsIgnoreCase(ApplicationConstants.BLANK_STRING)) {
				stream.setPreTokenStage(true);
				String date = null;
				String months = "(January|February|March|April|May|June|July|August|September|October|November|December)";
				String weekdays = "(Sunday|Monday|Tuesday|Wednesday|Thursday|Friday|Saturday)";
				
				//00:58:53 UTC on Sunday, 26 December 2004 to 20041226 00:58:53
				SimpleDateFormat sdf5 = new SimpleDateFormat("HH:mm:ss dd MMM yyyy");
				SimpleDateFormat esdf5 = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
				Pattern p = Pattern.compile("(\\d{1,2}:\\d{1,2}:\\d{1,2})\\s[\\w\\s]*on\\s" + weekdays + ",\\s*(\\d{1,2})\\s" + months + "\\s*(\\d{4})");
				Matcher m = p.matcher(text);
				while(m.find()) {
					try {
						date = esdf5.format(sdf5.parse(m.group(1) + ApplicationConstants.SPACE_STRING + m.group(3) + ApplicationConstants.SPACE_STRING + m.group(4)
								+ ApplicationConstants.SPACE_STRING + m.group(5)));
					} catch (ParseException e) {
						e.printStackTrace();
					}
					text = text.replace(m.group(), Matcher.quoteReplacement(date));
				}
				
				SimpleDateFormat sdf0 = new SimpleDateFormat("dd MMM yyyy");
				SimpleDateFormat esdf = new SimpleDateFormat("yyyyMMdd");
				
				//1 January 1978 to 19780101
				p = Pattern.compile("([\\d]{1,2})\\s+" + months + "\\s+([\\d]{4})");
				m = p.matcher(text);
				while(m.find()) {
					try {
						date = esdf.format(sdf0.parse(m.group()));
					} catch (ParseException e) {
						e.printStackTrace();
					}
					text = text.replace(m.group(), Matcher.quoteReplacement(date));
				}
				
				//December 7, 1941 to 19411207
				SimpleDateFormat sdf1 = new SimpleDateFormat("MMM dd, yyyy");
				p = Pattern.compile(months + "\\s*(\\d{1,2})\\s*([,])\\s*(\\d{4})");
				m = p.matcher(text);
				while(m.find()) {
					try {
						date = esdf.format(sdf1.parse(m.group(1) + ApplicationConstants.SPACE_STRING + m.group(2)+m.group(3) + ApplicationConstants.SPACE_STRING + m.group(4)));
					} catch (ParseException e) {
						e.printStackTrace();
					}
					text = text.replace(m.group(), Matcher.quoteReplacement(date));
				}
				
				//84 BC to -00840101 & 847 AD to 08470101
				SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy GG");
				SimpleDateFormat esdf2 = new SimpleDateFormat("yyyyMMdd");
				p = Pattern.compile("(\\d{1,4})\\s*(BC|AD)");
				m = p.matcher(text);
				while(m.find()) {
					try {
						if(m.group().contains("BC")) {
							date = "-" + esdf2.format(sdf2.parse(m.group(1) + ApplicationConstants.SPACE_STRING + m.group(2)));
						} else if(m.group().contains("AD")) {
							date = esdf2.format(sdf2.parse(m.group(1) + ApplicationConstants.SPACE_STRING + m.group(2)));
						}
					} catch (ParseException e) {
						e.printStackTrace();
					}
					text = text.replace(m.group(), Matcher.quoteReplacement(date));
				}
				
				//10:15 am to 10:15:00
				SimpleDateFormat sdf3 = new SimpleDateFormat("hh:mm aa");
				SimpleDateFormat esdf3 = new SimpleDateFormat("hh:mm:ss");
				p = Pattern.compile("(\\d{1,2}:\\d{1,2})\\s*(am|AM)");
				m = p.matcher(text);
				while(m.find()) {
					try {
						date = esdf3.format(sdf3.parse(m.group(1) + ApplicationConstants.SPACE_STRING + m.group(2)));
					} catch (ParseException e) {
						e.printStackTrace();
					}
					text = text.replace(m.group(), Matcher.quoteReplacement(date));
				}
				
				//5:15PM to 17:15:00
				SimpleDateFormat sdf4 = new SimpleDateFormat("hh:mm aa");
				SimpleDateFormat esdf4 = new SimpleDateFormat("HH:mm:ss");
				p = Pattern.compile("(\\d{1,2}:\\d{1,2})\\s*(pm|PM)");
				m = p.matcher(text);
				while(m.find()) {
					try {
						date = esdf4.format(sdf4.parse(m.group(1) + ApplicationConstants.SPACE_STRING + m.group(2)));
					} catch (ParseException e) {
						e.printStackTrace();
					}
					text = text.replace(m.group(), Matcher.quoteReplacement(date));
				}
				
				//April 11 to 19000411
				SimpleDateFormat sdf6 = new SimpleDateFormat("MMM dd");
				SimpleDateFormat esdf6 = new SimpleDateFormat("1900MMdd");
				p = Pattern.compile(months + "\\s+\\d{1,2}");
				m = p.matcher(text);
				while(m.find()) {
					try {
						date = esdf6.format(sdf6.parse(m.group()));
					} catch (ParseException e) {
						e.printStackTrace();
					}
					text = text.replace(m.group(), Matcher.quoteReplacement(date));
				}
				
				//2011�12 to 20110101�20120101
				SimpleDateFormat sdf7 = new SimpleDateFormat("yyyy");
				SimpleDateFormat sdf8 = new SimpleDateFormat("yy");
				p = Pattern.compile("(\\d{4})([\\W&&\\S])(\\d{2})");
				m = p.matcher(text);
				while(m.find()) {
					try {
						date = esdf.format(sdf7.parse(m.group(1)));
						date = date + m.group(2) + esdf.format(sdf8.parse(m.group(3)));
					} catch (ParseException e) {
						e.printStackTrace();
					}
					text = text.replace(m.group(), Matcher.quoteReplacement(date));
				}
				
				//Do at last
				//1948 to 19480101
				//Replace any four digits by appending it with 0101
				text = text.replaceAll("\\s\\b(\\d{4})\\b\\s", ApplicationConstants.SPACE_STRING + "$1" + "0101" + ApplicationConstants.SPACE_STRING);

				stream.setTokenStreamSb(new StringBuilder(text.trim()));
			}
		}
	}
	
}
