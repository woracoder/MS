package com.ir.lbs.client;

import java.util.List;

import org.apache.solr.client.solrj.response.SpellCheckResponse.Suggestion;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client-side stub for the RPC service.
 */
@RemoteServiceRelativePath("greet")
public interface QueryParserService extends RemoteService {
	List<List<String>> parseQuery(String keywordQuery, String locationQuery,
			String distanceRange, String startOffset, String filter);

	String getKeywordSuggestions(String partialQuery);

	List<List<String>> moreLikeThis(String docID);
}
