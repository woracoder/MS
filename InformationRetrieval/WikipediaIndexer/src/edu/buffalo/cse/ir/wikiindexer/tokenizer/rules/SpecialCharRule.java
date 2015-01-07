package edu.buffalo.cse.ir.wikiindexer.tokenizer.rules;

import edu.buffalo.cse.ir.wikiindexer.ApplicationConstants;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenStream;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenizerException;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.rules.TokenizerRule.RULENAMES;

//Aannotation for Accents Rule
@RuleClass(className = RULENAMES.SPECIALCHARS)
public class SpecialCharRule implements TokenizerRule {

	public void apply(TokenStream stream) throws TokenizerException {
		if(null != stream) {
			String text = null;
			if(null != stream.getTokenStreamSb() && !(text = stream.getTokenStreamSb().toString().trim()).equalsIgnoreCase(ApplicationConstants.BLANK_STRING)) {
				String specialCharRegex = "[^a-zA-Z0-9.?!\\'\\-\\s\\n\\r]+";
				String whiteSpaceRegex = "[\\s]+";
				text = text.replaceAll(specialCharRegex, ApplicationConstants.SPACE_STRING);
				text = text.replaceAll(whiteSpaceRegex, ApplicationConstants.SPACE_STRING);
				stream.setTokenStreamSb(new StringBuilder(text.trim()));
				stream.setPreTokenStage(true);
			}
		}
	}

}
