/**
 * 
 */
package edu.buffalo.cse.ir.wikiindexer.tokenizer;

import java.util.Properties;

import edu.buffalo.cse.ir.wikiindexer.ApplicationConstants;
import edu.buffalo.cse.ir.wikiindexer.indexer.INDEXFIELD;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.rules.AccentRule;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.rules.ApostropheRule;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.rules.CapitalizationRule;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.rules.DatesRule;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.rules.DelimRule;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.rules.EnglishStemmer;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.rules.HyphenRule;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.rules.NumberRule;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.rules.PunctuationRule;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.rules.SpecialCharRule;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.rules.StopWordsRule;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.rules.WhiteSpaceRule;

/**
 * Factory class to instantiate a Tokenizer instance The expectation is that you
 * need to decide which rules to apply for which field Thus, given a field type,
 * initialize the applicable rules and create the tokenizer
 * 
 * @author nikhillo
 * 
 */
public class TokenizerFactory {
	// private instance, we just want one factory
	private static TokenizerFactory factory;

	// properties file, if you want to read soemthing for the tokenizers
	private static Properties props;

	/**
	 * Private constructor, singleton
	 */
	private TokenizerFactory() {
		// TODO: Implement this method
	}

	/**
	 * MEthod to get an instance of the factory class
	 * 
	 * @return The factory instance
	 */
	public static TokenizerFactory getInstance(Properties idxProps) {
		if (factory == null) {
			factory = new TokenizerFactory();
			props = idxProps;
		}

		return factory;
	}

	/**
	 * Method to get a fully initialized tokenizer for a given field type
	 * 
	 * @param field
	 *            : The field for which to instantiate tokenizer
	 * @return The fully initialized tokenizer
	 */
	public Tokenizer getTokenizer(INDEXFIELD field) {
		// TODO: Implement this method
		/*
		 * For example, for field F1 I want to apply rules R1, R3 and R5 For F2,
		 * the rules are R1, R2, R3, R4 and R5 both in order So the pseudo-code
		 * will be like: if (field == F1) return new Tokenizer(new R1(), new
		 * R3(), new R5()) else if (field == F2) return new TOkenizer(new R1(),
		 * new R2(), new R3(), new R4(), new R5()) ... etc
		 */
		Tokenizer t = null;
		try {
			if (field.name().equalsIgnoreCase(ApplicationConstants.TERM_FIELD)) {
				t = new Tokenizer(new AccentRule(), new DatesRule(),
						new NumberRule(), 
						new SpecialCharRule(), new DelimRule(new String[] { ":", ";", "," }),
						//new NumberRule(),
						new WhiteSpaceRule(), new HyphenRule(), new ApostropheRule(), 
						new CapitalizationRule(), new StopWordsRule(), new PunctuationRule(),
						new EnglishStemmer());
			} else if (field.name().equalsIgnoreCase(ApplicationConstants.AUTHOR_FIELD)) {
				t = new Tokenizer(new AccentRule(), new WhiteSpaceRule(), new CapitalizationRule());
			} else if (field.name().equalsIgnoreCase(ApplicationConstants.CATEGORY_FIELD)) {
				t = new Tokenizer(new AccentRule(), new DatesRule(),
						new DelimRule(new String[] { ",", ";", ":" }),
						new WhiteSpaceRule(), new HyphenRule(),
						new ApostropheRule(), new CapitalizationRule(),
						new StopWordsRule(), new PunctuationRule(),
						new EnglishStemmer());
			} else if (field.name().equalsIgnoreCase(ApplicationConstants.LINK_FIELD)) {
				t = new Tokenizer(new AccentRule(), new WhiteSpaceRule());
			}
		} catch (TokenizerException e) {
			e.printStackTrace();
		}
		return t;
	}
}
