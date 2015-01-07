package edu.buffalo.cse.ir.wikiindexer.tokenizer.rules;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.buffalo.cse.ir.wikiindexer.ApplicationConstants;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenStream;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenizerException;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.rules.TokenizerRule.RULENAMES;

@RuleClass(className = RULENAMES.PUNCTUATION)
public class PunctuationRule implements TokenizerRule {

	public PunctuationRule() {
		
	}
	
	@Override
	public void apply(TokenStream stream) throws TokenizerException {
		if(null != stream) {
			if(null != stream.getAllTokens() && !stream.getAllTokens().isEmpty() && stream.getAllTokens().size()>0) {
				Pattern p = Pattern.compile("[.!?]+(\\s|\\z)");
				Matcher m;
				ArrayList<String> tokens = (ArrayList<String>)stream.getAllTokens();
				for(int i=0; i<tokens.size(); i++) {
					m = p.matcher(tokens.get(i));
					while(m.find()) {
						tokens.set(i, tokens.get(i).replace(m.group(), ApplicationConstants.SPACE_STRING));
					}
					tokens.set(i, tokens.get(i).trim());
				}
				stream.syncMap();
			}
		}
	}
}
