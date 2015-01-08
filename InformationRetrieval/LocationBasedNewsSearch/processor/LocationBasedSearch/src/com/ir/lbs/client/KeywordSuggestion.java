package com.ir.lbs.client;

import com.google.gwt.user.client.ui.SuggestOracle;


public class KeywordSuggestion implements SuggestOracle.Suggestion{
	
	private String keywordSuggestion;
	
	public KeywordSuggestion(String suggestion) {
		keywordSuggestion = suggestion;
	}

	@Override
	public String getDisplayString() {
		// TODO Auto-generated method stub
		return this.keywordSuggestion;
	}

	@Override
	public String getReplacementString() {
		// TODO Auto-generated method stub
		return this.keywordSuggestion;
	}
	
	
}
