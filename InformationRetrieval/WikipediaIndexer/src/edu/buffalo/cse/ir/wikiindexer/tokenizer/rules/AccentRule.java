package edu.buffalo.cse.ir.wikiindexer.tokenizer.rules;

import java.text.Normalizer;

import edu.buffalo.cse.ir.wikiindexer.ApplicationConstants;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenStream;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenizerException;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.rules.TokenizerRule.RULENAMES;

//Aannotation for Accents Rule
@RuleClass(className = RULENAMES.ACCENTS)
public class AccentRule implements TokenizerRule {

	public void apply(TokenStream stream) throws TokenizerException {

		if (null != stream) {
			String nfdNormalizedString=null;
			if(null != stream.getTokenStreamSb() && !(nfdNormalizedString = stream.getTokenStreamSb().toString().trim()).equalsIgnoreCase(ApplicationConstants.BLANK_STRING)) {
				stream.setPreTokenStage(true);
				nfdNormalizedString = Normalizer.normalize(nfdNormalizedString, Normalizer.Form.NFD);
				nfdNormalizedString = nfdNormalizedString.replaceAll("\\p{InCombiningDiacriticalMarks}+", ApplicationConstants.BLANK_STRING);
				/*
				 * Pattern pattern =
				 * Pattern.compile("\\p{InCombiningDiacriticalMarks}+"); Matcher m =
				 * pattern.matcher(nfdNormalizedString);
				 */
				//stream.setTokenStreamSb(new StringBuilder(nfdNormalizedString.trim()));
				//Clear & reset map & list
				stream.getTokenStreamSb().setLength(0);
				stream.getTokenList().clear();
				stream.getTokenMap().clear();
				stream.append(nfdNormalizedString.trim());
				//stream.getTokenList().add(nfdNormalizedString);
				//stream.getTokenMap().put(nfdNormalizedString, 1);
			}
		}
	}
}
