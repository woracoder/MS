package edu.buffalo.cse.ir.wikiindexer.tokenizer.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenStream;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenizerException;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.rules.TokenizerRule.RULENAMES;

//Aannotation for Accents Rule
@RuleClass(className = RULENAMES.STOPWORDS)
public class StopWordsRule implements TokenizerRule {

	static HashSet<String> stopWordList = new HashSet<String>();

	static {
		stopWordList.add("i");
		stopWordList.add("me");
		stopWordList.add("my");
		stopWordList.add("myself");
		stopWordList.add("we");
		stopWordList.add("us");
		stopWordList.add("our");
		stopWordList.add("ours");
		stopWordList.add("ourselves");
		stopWordList.add("you");
		stopWordList.add("your");
		stopWordList.add("yours");
		stopWordList.add("yourself");
		stopWordList.add("yourselves");
		stopWordList.add("he");
		stopWordList.add("him");
		stopWordList.add("his");
		stopWordList.add("himself");
		stopWordList.add("she");
		stopWordList.add("her");
		stopWordList.add("hers");
		stopWordList.add("herself");
		stopWordList.add("it");
		stopWordList.add("its");
		stopWordList.add("itself");
		stopWordList.add("they");
		stopWordList.add("them");
		stopWordList.add("their");
		stopWordList.add("theirs");
		stopWordList.add("themselves");
		stopWordList.add("what");
		stopWordList.add("which");
		stopWordList.add("who");
		stopWordList.add("whom");
		stopWordList.add("this");
		stopWordList.add("that");
		stopWordList.add("these");
		stopWordList.add("those");
		stopWordList.add("am");
		stopWordList.add("is");
		stopWordList.add("are");
		stopWordList.add("was");
		stopWordList.add("were");
		stopWordList.add("be");
		stopWordList.add("been");
		stopWordList.add("being");
		stopWordList.add("have");
		stopWordList.add("has");
		stopWordList.add("had");
		stopWordList.add("having");
		stopWordList.add("do");
		stopWordList.add("does");
		stopWordList.add("did");
		stopWordList.add("doing ");
		stopWordList.add("will");
		stopWordList.add("would");
		stopWordList.add("shall");
		stopWordList.add("should");
		stopWordList.add("can");
		stopWordList.add("could");
		stopWordList.add("may");
		stopWordList.add("might");
		stopWordList.add("must");
		stopWordList.add("ought");
		stopWordList.add("i'm");
		stopWordList.add("you're");
		stopWordList.add("he's");
		stopWordList.add("she's");
		stopWordList.add("it's");
		stopWordList.add("we're");
		stopWordList.add("they're");
		stopWordList.add("i've");
		stopWordList.add("you've");
		stopWordList.add("we've");
		stopWordList.add("they've");
		stopWordList.add("i'd");
		stopWordList.add("you'd");
		stopWordList.add("he'd");
		stopWordList.add("she'd");
		stopWordList.add("we'd");
		stopWordList.add("they'd");
		stopWordList.add("i'll");
		stopWordList.add("you'll");
		stopWordList.add("he'll");
		stopWordList.add("she'll");
		stopWordList.add("we'll");
		stopWordList.add("they'll");
		stopWordList.add("isn't");
		stopWordList.add("aren't");
		stopWordList.add("wasn't");
		stopWordList.add("weren't");
		stopWordList.add("hasn't");
		stopWordList.add("haven't");
		stopWordList.add("hadn't");
		stopWordList.add("doesn't");
		stopWordList.add("don't");
		stopWordList.add("didn't");
		stopWordList.add("won't");
		stopWordList.add("wouldn't");
		stopWordList.add("shan't");
		stopWordList.add("shouldn't");
		stopWordList.add("can't");
		stopWordList.add("cannot");
		stopWordList.add("couldn't");
		stopWordList.add("mustn't");
		stopWordList.add("let's");
		stopWordList.add("that's");
		stopWordList.add("who's");
		stopWordList.add("what's");
		stopWordList.add("here's");
		stopWordList.add("there's");
		stopWordList.add("when's");
		stopWordList.add("where's");
		stopWordList.add("why's");
		stopWordList.add("how's");
		stopWordList.add("a");
		stopWordList.add("an");
		stopWordList.add("the");
		stopWordList.add("oughtn't ");
		stopWordList.add("mightn't");
		stopWordList.add("daren't ");
		stopWordList.add("needn't");
		stopWordList.add("and");
		stopWordList.add("but");
		stopWordList.add("if");
		stopWordList.add("or");
		stopWordList.add("because");
		stopWordList.add("as");
		stopWordList.add("until");
		stopWordList.add("while");
		stopWordList.add("of");
		stopWordList.add("at");
		stopWordList.add("by");
		stopWordList.add("for");
		stopWordList.add("with");
		stopWordList.add("about");
		stopWordList.add("against");
		stopWordList.add("between");
		stopWordList.add("into");
		stopWordList.add("through");
		stopWordList.add("during");
		stopWordList.add("before");
		stopWordList.add("after");
		stopWordList.add("above");
		stopWordList.add("below");
		stopWordList.add("to");
		stopWordList.add("from");
		stopWordList.add("up");
		stopWordList.add("down");
		stopWordList.add("in");
		stopWordList.add("out");
		stopWordList.add("on");
		stopWordList.add("off");
		stopWordList.add("over");
		stopWordList.add("under");
		stopWordList.add("again");
		stopWordList.add("further");
		stopWordList.add("then");
		stopWordList.add("once");
		stopWordList.add("here");
		stopWordList.add("there");
		stopWordList.add("when");
		stopWordList.add("where");
		stopWordList.add("why");
		stopWordList.add("how");
		stopWordList.add("all");
		stopWordList.add("any");
		stopWordList.add("both");
		stopWordList.add("each");
		stopWordList.add("few");
		stopWordList.add("more");
		stopWordList.add("most");
		stopWordList.add("other");
		stopWordList.add("some");
		stopWordList.add("such");
		stopWordList.add("no");
		stopWordList.add("nor");
		stopWordList.add("not");
		stopWordList.add("only");
		stopWordList.add("own");
		stopWordList.add("same");
		stopWordList.add("so");
		stopWordList.add("than");
		stopWordList.add("too");
		stopWordList.add("very");
		stopWordList.add("one");
		stopWordList.add("every");
		stopWordList.add("least");
		stopWordList.add("less");
		stopWordList.add("many");
		stopWordList.add("now");
		stopWordList.add("ever");
		stopWordList.add("never");
		stopWordList.add("say");
		stopWordList.add("says");
		stopWordList.add("said");
		stopWordList.add("also");
		stopWordList.add("get");
		stopWordList.add("go");
		stopWordList.add("goes");
		stopWordList.add("just");
		stopWordList.add("made");
		stopWordList.add("make");
		stopWordList.add("put");
		stopWordList.add("see");
		stopWordList.add("seen");
		stopWordList.add("whether");
		stopWordList.add("like");
		stopWordList.add("well");
		stopWordList.add("back");
		stopWordList.add("even");
		stopWordList.add("still");
		stopWordList.add("way");
		stopWordList.add("take");
		stopWordList.add("since");
		stopWordList.add("another");
		stopWordList.add("however");
		stopWordList.add("two");
		stopWordList.add("three");
		stopWordList.add("four");
		stopWordList.add("five");
		stopWordList.add("first");
		stopWordList.add("second");
		stopWordList.add("new");
		stopWordList.add("old");
		stopWordList.add("high");
		stopWordList.add("long");
		/* Stopword List Ends */
	}

	public void apply(TokenStream stream) throws TokenizerException {
		if(null != stream) {
			if(null != stream.getAllTokens() && !stream.getAllTokens().isEmpty() && stream.getAllTokens().size()>0) {
				ArrayList<String> tokenList = (ArrayList<String>) stream.getAllTokens();
				HashMap<String, Integer> tMap = (HashMap<String, Integer>)stream.getTokenMap();
				String s= null;
				for (int j = tokenList.size()-1; j >= 0; j--) {
				    if(stopWordList.contains((s=tokenList.get(j)))) {
				    	tokenList.remove(j);
				    	//Remove entry from Map
				    	if(tMap.get(s) > 1) {
				    		tMap.put(s, tMap.get(s)-1);
				    	} else {
				    		tMap.remove(s);
				    	}
				    }
				}
			}
		}
	}

}
