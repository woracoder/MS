package edu.buffalo.cse.ir.wikiindexer.tokenizer.rules;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.buffalo.cse.ir.wikiindexer.ApplicationConstants;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenStream;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenizerException;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.rules.TokenizerRule.RULENAMES;

//Aannotation for Accents Rule
@RuleClass(className = RULENAMES.HYPHEN)
public class HyphenRule implements TokenizerRule {
	/* White Space has to be before this */
	public void apply(TokenStream stream) throws TokenizerException {
		if(null != stream) {
			if(null != stream.getAllTokens() &&!stream.getAllTokens().isEmpty() && stream.getAllTokens().size()>0) {
				String hyphenRegex = "^[\\s]*[a-zA-Z0-9]*[\\s]*[-]+[\\s]*[a-zA-Z0-9]*$";
				String alphaRegex = "^[\\sa-zA-Z]*[-]+[a-zA-Z\\s]*$";
				String rightAlphaNumeric = "^[\\s]*[-]+[\\sa-zA-Z0-9]+$";
				String leftAlphaNumeric = "^[\\sa-zA-Z0-9]+[-]+[\\s]*$";
				String token = null;
				ArrayList<String> tokenList = null;
				Pattern p1;
				Pattern p2;
				Pattern p3;
				Pattern p4;
				Matcher m1;
				Matcher m2;
				Matcher m3;
				Matcher m4;
				
				tokenList = (ArrayList<String>) stream.getAllTokens();
				for (int i = 0; i < tokenList.size(); i++) {
					token = tokenList.get(i);
					// Searching all hyphens which fall under our purview
					p1 = Pattern.compile(hyphenRegex); 
					m1 = p1.matcher(token);
					if (m1.find()) {
						// Searching all hyphens with alphabets on either side
						p2 = Pattern.compile(alphaRegex); 
						m2 = p2.matcher(token);
						if (m2.find()) {
							token = token.replaceAll("[-]+", ApplicationConstants.SPACE_STRING);
						} else {
							// Searching all hyphens with alphabets on either side
							p3 = Pattern.compile(rightAlphaNumeric);
							m3 = p3.matcher(token);
							// Searching all hyphens with alphabets on either side
							p4 = Pattern.compile(leftAlphaNumeric);  
							m4 = p4.matcher(token);
							if (m3.find() || m4.find()) {
								token = token.replaceAll("[-]+", ApplicationConstants.SPACE_STRING);
							}
						}
						token = token.replaceAll("[\\s]+", ApplicationConstants.SPACE_STRING).trim();
					} else {
						// removing all hyphens which do not fall under our purview
						token = token.replaceAll("[-]+", ApplicationConstants.BLANK_STRING); 
					}
					if (token.trim().equalsIgnoreCase(ApplicationConstants.BLANK_STRING)) {
						tokenList.remove(i);
					} else {
						tokenList.set(i, token);
					}
				}
				stream.syncMap();
			}
		}
	}
}

/*
 * p3=Pattern.compile(leftWhiteSpaceRegex); //removing hyphens
 * with white space padding on left hand side
 * m3=p3.matcher(token);
 * p4=Pattern.compile(rightWhiteSpaceRegex); //removing all
 * hyphens which donot fall under our purview
 * m4=p4.matcher(token);
 * p5=Pattern.compile(BeginWhiteSpaceRegex); //removing all
 * hyphens which donot fall under our purview
 * m5=p5.matcher(token);
 * p6=Pattern.compile(rightWhiteSpaceRegex); //removing all
 * hyphens which donot fall under our purview
 * m6=p5.matcher(token); if(m3.find() || m4.find() || m5.find()
 * || m6.find()){ token=token.replaceAll("[\\s]*[-]+[\\s]*",
 * ""); }
 * 
 * 
 * 
 * if(m4.find() ){ token=token.replaceAll("[\\s]*[-]+[\\s]",
 * ""); }
 */
