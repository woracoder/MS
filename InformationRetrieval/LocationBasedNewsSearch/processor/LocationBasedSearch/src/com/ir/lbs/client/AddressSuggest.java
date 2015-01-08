package com.ir.lbs.client;

import java.util.ArrayList;
import java.util.Collection;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.maps.gwt.client.Geocoder;
import com.google.maps.gwt.client.GeocoderRequest;
import com.google.maps.gwt.client.GeocoderResult;
import com.google.maps.gwt.client.GeocoderStatus;

public class AddressSuggest extends SuggestOracle {
	
	public Geocoder geoCoder = null;
	
	public AddressSuggest() {
		geoCoder = Geocoder.create();
	}

	@Override
	public void requestSuggestions(final Request request, final Callback callback) {
		// TODO Auto-generated method stub
		String partialQuery = request.getQuery();
		
		if (partialQuery.length() >= 1) {
			GeocoderRequest geoCodeRequest = GeocoderRequest.create();
			geoCodeRequest.setAddress(partialQuery);
			geoCoder.geocode(geoCodeRequest, new Geocoder.Callback() {

				@Override
				public void handle(JsArray<GeocoderResult> results, GeocoderStatus status) {
					if (status == GeocoderStatus.OK) {
						Collection<Suggestion> locationResults = new ArrayList<Suggestion>();
						for (int index = 0; index < results.length(); index++) {
							System.out.println("Location : " + results.get(index).getFormattedAddress());
							AddressSuggestion addressSuggestion = new AddressSuggestion(results.get(index).getFormattedAddress());
							locationResults.add(addressSuggestion);
						}
						
						Response response = new Response(locationResults);
						callback.onSuggestionsReady(request, response);
					}
					
				}
				
			});
		}
	}

}
