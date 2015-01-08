package com.ir.lbs.client;

import com.google.gwt.user.client.ui.SuggestOracle;


public class AddressSuggestion implements SuggestOracle.Suggestion{
	
	private String addressSuggestion;
	
	public AddressSuggestion(String suggestion) {
		addressSuggestion = suggestion;
	}

	@Override
	public String getDisplayString() {
		// TODO Auto-generated method stub
		return this.addressSuggestion;
	}

	@Override
	public String getReplacementString() {
		// TODO Auto-generated method stub
		return this.addressSuggestion;
	}
	
	
}
