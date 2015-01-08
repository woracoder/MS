package com.ir.lbs.client;

import java.util.ArrayList;
import java.util.Collection;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestOracle;

public class KeywordSuggest extends SuggestOracle {
	
	// public Geocoder geoCoder = null;
	
	public KeywordSuggest() {
		// geoCoder = Geocoder.create();
	}

	@Override
	public void requestSuggestions(final Request request, final Callback callback) {
		// TODO Auto-generated method stub
		String partialQuery = request.getQuery();
		
		if (partialQuery.length() >= 1) {
	
			final Collection<KeywordSuggestion> keywordSuggestions = new ArrayList<KeywordSuggestion>();
			
			QueryParserServiceAsync queryParser = GWT.create(QueryParserService.class);
			
			queryParser.getKeywordSuggestions(partialQuery, new AsyncCallback<String>() {

				@Override
				public void onFailure(Throwable caught) {
					// TODO Auto-generated method stub
					caught.printStackTrace();
				}

				
				@Override
				public void onSuccess(String result) {
					
					if (null != result && !result.equalsIgnoreCase("")) {
						keywordSuggestions.add(new KeywordSuggestion(result));
						Response response = new Response(keywordSuggestions);
						callback.onSuggestionsReady(request, response);
					}
					
				}
			});
		}
	}

}
