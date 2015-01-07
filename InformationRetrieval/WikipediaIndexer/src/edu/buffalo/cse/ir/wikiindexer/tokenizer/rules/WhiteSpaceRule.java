package edu.buffalo.cse.ir.wikiindexer.tokenizer.rules;

import edu.buffalo.cse.ir.wikiindexer.ApplicationConstants;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenStream;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenizerException;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.rules.TokenizerRule.RULENAMES;

//Aannotation for Capitalization Rule
@RuleClass(className = RULENAMES.WHITESPACE)
public class WhiteSpaceRule implements TokenizerRule {

	public void apply(TokenStream stream) throws TokenizerException {
		if(null != stream) {
			String text=null;
			if(null != stream.getTokenStreamSb() && !(text = stream.getTokenStreamSb().toString().trim()).equalsIgnoreCase(ApplicationConstants.BLANK_STRING)) {
				String whiteSpaceRegex = "[\\s]+";
				String[] tokens = null;
				stream.getTokenList().clear();
				if (text.length() >= 1) {
					tokens = text.split(whiteSpaceRegex);
					stream.getTokenList().clear();
					stream.getTokenMap().clear();
					stream.getTokenStreamSb().setLength(0);
					stream.append(tokens);
				}
			}
			stream.setPreTokenStage(false);
		}
	}

	/*
	 * public void apply(TokenStream stream) throws TokenizerException {
	 * 
	 * int startTokenIndex =0; int endTokenIndex =0; StringBuilder
	 * text=stream.getText(); String token=null;
	 * 
	 * if (stream!=null && text.length()>=1){
	 * 
	 * while(startTokenIndex!=text.length()){
	 * 
	 * //Removing multiple white spaces and pointing startTokenIndex to first
	 * alphabet of token while (text.charAt(startTokenIndex)==' ' ){
	 * startTokenIndex++; if (startTokenIndex==text.length()){ return ; } }
	 * 
	 * 
	 * //finding the end of token by searching for white space
	 * endTokenIndex=text.indexOf(" ", startTokenIndex);
	 * 
	 * //if it is the last token and there is no space after it if
	 * (endTokenIndex==-1){ endTokenIndex=text.length(); }
	 * 
	 * if (startTokenIndex==endTokenIndex){ return ; }
	 * 
	 * token=text.substring(startTokenIndex, endTokenIndex);
	 * 
	 * startTokenIndex=endTokenIndex; stream.addTokenList(token);
	 * 
	 * } }
	 * 
	 * }
	 */

}
