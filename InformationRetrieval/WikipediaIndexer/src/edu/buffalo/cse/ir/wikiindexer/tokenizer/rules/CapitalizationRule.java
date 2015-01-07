package edu.buffalo.cse.ir.wikiindexer.tokenizer.rules;

import java.util.ArrayList;

import edu.buffalo.cse.ir.wikiindexer.ApplicationConstants;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenStream;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenizerException;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.rules.TokenizerRule.RULENAMES;

//Aannotation for Capitalization Rule
@RuleClass(className = RULENAMES.STEMMER)
public class CapitalizationRule implements TokenizerRule {

	public void apply(TokenStream stream) throws TokenizerException {

		if (null != stream) {
			if (null != stream.getAllTokens() && !stream.getAllTokens().isEmpty() && stream.getAllTokens().size() > 0) {
				int upperCaseCount = 0;
				// int lowerCaseCount=0;
				boolean currentTokenEndOfSentenceFlag = false;
				boolean previousTokenEndOfSentenceFlag = true;
				boolean currentCamelCase = false;
				boolean previousCamelCase = false;
				// boolean upperCaseFlag=false;
				// boolean consecutiveUpperCaseFlag = true;
				// boolean firstAlphabetUpperCaseFlag=false;
				ArrayList<String> tokenList = null;
				String token = ApplicationConstants.BLANK_STRING;
				int i = 0;
				int j = 0;

				/*
				 * 1. Navigate through the ArrayList of tokens one by one 2. For
				 * any of these strings Check whether end of sentenence is . or
				 * ! or ?. If yes put endofSentence flag =true else false
				 * 3.check if all letters are in upper case. If yes let them be
				 * and if not proceed to step 3 3. check if first letter of
				 * string is upper case and endofsentenceflag = true
				 */

				tokenList = (ArrayList<String>) stream.getAllTokens();
				for (i = 0; i < tokenList.size(); i++) {
					if(null != tokenList.get(i) && !(token = tokenList.get(i).trim()).equalsIgnoreCase(ApplicationConstants.BLANK_STRING)) {
						if (Character.isUpperCase(token.charAt(0))) {
							// firstAlphabetUpperCaseFlag=true;
							upperCaseCount = 1;
						} /*else { // firstAlphabetUpperCaseFlag=false;
						 * lowerCaseCount = 1; }*/
	
						for (j = 1; j < token.length(); j++) {
							if (Character.isUpperCase(token.charAt(j))) {
								/*if(upperCaseFlag){ consecutiveUpperCaseFlag=true;
								 * }-----Removing Toggle Case*/
								upperCaseCount++;
								// upperCaseFlag = true;
							} /*else { lowerCaseCount++; upperCaseFlag = false; } */
						}
	
						if (token.charAt((token.length() - 1)) == '.' || token.charAt((token.length() - 1)) == '?' || token.charAt((token.length() - 1)) == '!') {
							currentTokenEndOfSentenceFlag = true;
						}
	
						if (upperCaseCount == token.length()) {
							continue;
						} else if (upperCaseCount == token.length() - 1 && currentTokenEndOfSentenceFlag) {
							continue;
							// && !consecutiveUpperCaseFlag){-----Removing  toggleCase
						} else if (upperCaseCount >= 1 && !previousTokenEndOfSentenceFlag) {
							currentCamelCase = true;
							if (previousCamelCase) {
								token = tokenList.get(i - 1) + ApplicationConstants.SPACE_STRING + tokenList.get(i);
								tokenList.set(i - 1, token);
								tokenList.remove(i);
								i--;
								currentCamelCase = false;
							}
							previousCamelCase = currentCamelCase;
							currentCamelCase = false;
						} else {
							token = token.toLowerCase();
							tokenList.set(i, token);
						}
	
						previousTokenEndOfSentenceFlag = currentTokenEndOfSentenceFlag;
						currentTokenEndOfSentenceFlag = false;
						previousCamelCase = currentCamelCase;
						currentCamelCase = false;
						upperCaseCount = 0;
						// lowerCaseCount = 0;
						// firstAlphabetUpperCaseFlag=false;
						// upperCaseFlag = false;
						// consecutiveUpperCaseFlag=false;
					}
				}
				stream.syncMap();
			}
		}
	}
}
