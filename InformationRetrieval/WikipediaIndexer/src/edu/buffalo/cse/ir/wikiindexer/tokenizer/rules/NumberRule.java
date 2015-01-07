package edu.buffalo.cse.ir.wikiindexer.tokenizer.rules;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.buffalo.cse.ir.wikiindexer.ApplicationConstants;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenStream;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenizerException;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.rules.TokenizerRule.RULENAMES;

//Aannotation for NUMBERS Rule
@RuleClass(className = RULENAMES.NUMBERS)
public class NumberRule implements TokenizerRule {

	public void apply(TokenStream stream) throws TokenizerException {
		if(null != stream) {
			String text=null;
			if(null != stream.getTokenStreamSb() && !(text = stream.getTokenStreamSb().toString().trim()).equalsIgnoreCase(ApplicationConstants.BLANK_STRING)) {
				/* works on a string */
				String generalNumberRegex = "[0-9]+[,.]*[0-9]*";
				String dateRegex = "\\b[0-9]{8}\\b";
				Pattern p1 = Pattern.compile(generalNumberRegex);
				Matcher m1 = p1.matcher(text);
				while (m1.find()) {
					Pattern p2 = Pattern.compile(dateRegex);
					Matcher m2 = p2.matcher(m1.group());
					
					if(m1.group().length()<8 || m1.group().length()>8) {
						text = text.replaceFirst(m1.group(), ApplicationConstants.BLANK_STRING);
					} else {
						if (!m2.find()) {
							text = text.replaceFirst(m1.group(), ApplicationConstants.BLANK_STRING);
						}
					}
					
					/*if (!m2.find()) {
						text = text.replaceFirst(m1.group(), ApplicationConstants.BLANK_STRING);
					}*/
				}
				text = text.replaceAll("[\\s]+", ApplicationConstants.SPACE_STRING);
				stream.setTokenStreamSb(new StringBuilder(text.trim()));
				stream.setPreTokenStage(true);
			}
		}
	}

}
