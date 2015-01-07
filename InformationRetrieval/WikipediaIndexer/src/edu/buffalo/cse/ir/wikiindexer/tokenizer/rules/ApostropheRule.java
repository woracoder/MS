package edu.buffalo.cse.ir.wikiindexer.tokenizer.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.buffalo.cse.ir.wikiindexer.ApplicationConstants;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenStream;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenizerException;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.rules.TokenizerRule.RULENAMES;

@RuleClass(className = RULENAMES.APOSTROPHE)
public class ApostropheRule implements TokenizerRule {

	public static Map<String, String> apostropheMap = new HashMap<String, String>();
	static {
		apostropheMap.put("aren't", "are not");
		apostropheMap.put("can't", "cannot");
		apostropheMap.put("couldn't", "could not");
		apostropheMap.put("didn't", "did not");
		apostropheMap.put("doesn't", "does not");
		apostropheMap.put("don't", "do not");
		apostropheMap.put("hadn't", "had not");
		apostropheMap.put("hasn't", "has not");
		apostropheMap.put("haven't", "have not");
		apostropheMap.put("he'd", "he had");// , he would
		apostropheMap.put("he'll", "he will");// , he shall
		apostropheMap.put("he's", "he is");// , he has
		apostropheMap.put("i'd", "i had");// , I would
		apostropheMap.put("i'll", "i will");// , I shall
		apostropheMap.put("i'm", "i am");
		apostropheMap.put("i've", "i have");
		apostropheMap.put("isn't", "is not");
		apostropheMap.put("it's", "it is");// , it has
		apostropheMap.put("let's", "let us");
		apostropheMap.put("mustn't", "must not");
		apostropheMap.put("shan't", "shall not");
		apostropheMap.put("she'd", "she had");// , she would
		apostropheMap.put("she'll", "she will");// , she shall
		apostropheMap.put("she's", "she is");// , she has
		apostropheMap.put("shouldn't", "should not");
		apostropheMap.put("that's", "that is");// , that has
		apostropheMap.put("there's", "there is");// , there has
		apostropheMap.put("they'd", "they had");// , they would
		apostropheMap.put("they'll", "they will");// , they shall
		apostropheMap.put("they're", "they are");
		apostropheMap.put("they've", "they have");
		apostropheMap.put("we'd", "we had");// , we would
		apostropheMap.put("we're", "we are");
		apostropheMap.put("we've", "we have");
		apostropheMap.put("weren't", "were not");
		apostropheMap.put("what'll", "what will");// , what shall
		apostropheMap.put("what're", "what are");
		apostropheMap.put("what's", "what is");// , what has
		apostropheMap.put("what've", "what have");
		apostropheMap.put("where's", "where is");// , where has
		apostropheMap.put("who'd", "who had");// , who would
		apostropheMap.put("who'll", "who will");// , who shall
		apostropheMap.put("who're", "who are");
		apostropheMap.put("who's", "who is");// , who has
		apostropheMap.put("who've", "who have");
		apostropheMap.put("won't", "will not");
		apostropheMap.put("wouldn't", "would not");
		apostropheMap.put("you'd", "you had");// , you would
		apostropheMap.put("you'll", "you will");// , you shall
		apostropheMap.put("you're", "you are");
		apostropheMap.put("you've", "you have");
		apostropheMap.put("should've", "should have");
		apostropheMap.put("they'd", "they would");
		apostropheMap.put("she'll", "she will");
		apostropheMap.put("'em", "them");
	}

	@Override
	public void apply(TokenStream stream) throws TokenizerException {
		
		if(null != stream) {
			if(null != stream.getAllTokens() && !stream.getAllTokens().isEmpty() && stream.getAllTokens().size()>0) {
				//Pattern p = Pattern.compile("([\\w]*)'{1,}([\\w]*)");
				Pattern p = Pattern.compile("([^']*)'{1,}([^']*)");
				Matcher m;
				String text = null;
				ArrayList<String> tokens = (ArrayList<String>) stream.getAllTokens();
				for (int i = 0; i < tokens.size(); i++) {
					m = p.matcher(tokens.get(i));
					if (m.find()) {
						if (apostropheMap.containsKey(m.group().toLowerCase())) {
							text = apostropheMap.get(m.group().toLowerCase());
							if (!apostropheMap.containsKey(m.group())) {
								if (text.length()>1) {
									text = text.substring(0, 1).toUpperCase() + text.substring(1);
								} else {
									text = text.substring(0, 1).toUpperCase();
								}
							}
							tokens.remove(i);
							for(int j=text.split("\\s").length-1; j>=0; j--) {
								tokens.add(i, text.split("\\s")[j]);
							}
						} else {
							text = m.group().replaceAll("'{1,}s", ApplicationConstants.BLANK_STRING);
							text = text.replaceAll("'", ApplicationConstants.BLANK_STRING);
							tokens.set(i, text);
						}
					}
				}
				stream.syncMap();
			}
		}	
	}

}
