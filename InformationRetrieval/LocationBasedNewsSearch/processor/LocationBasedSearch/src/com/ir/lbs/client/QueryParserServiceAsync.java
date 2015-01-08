package com.ir.lbs.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface QueryParserServiceAsync {
	void parseQuery(String keywordQuery, String locationQuery,
			String distanceRange, String startOffset, String filter, AsyncCallback<List<List<String>>> asyncCallback);

	void getKeywordSuggestions(String partialQuery,
			AsyncCallback<String> asyncCallback);

	void moreLikeThis(String docID,
			AsyncCallback<List<List<String>>> asnycCallback);
}
