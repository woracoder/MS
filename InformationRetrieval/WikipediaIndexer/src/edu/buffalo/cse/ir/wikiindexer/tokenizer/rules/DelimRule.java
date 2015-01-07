package edu.buffalo.cse.ir.wikiindexer.tokenizer.rules;

import edu.buffalo.cse.ir.wikiindexer.ApplicationConstants;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenStream;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenizerException;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.rules.TokenizerRule.RULENAMES;

//Aannotation for Capitalization Rule
@RuleClass(className = RULENAMES.DELIM)
public class DelimRule implements TokenizerRule {
	
	String[] delimiters = null;
	
	public DelimRule(String[] delims) {
		this.delimiters = delims;
	}

	public void apply(TokenStream stream) throws TokenizerException {
		
		if(null != stream) {
			String text = null;
			if(null != stream.getTokenStreamSb() && !(text = stream.getTokenStreamSb().toString().trim()).equalsIgnoreCase(ApplicationConstants.BLANK_STRING)) {
				stream.setPreTokenStage(true);
				for(String s : delimiters) {
					text = text.replaceAll(s, ApplicationConstants.SPACE_STRING);
				}
				stream.setTokenStreamSb(new StringBuilder(text.trim()));
			}
		}
	}
	
}

