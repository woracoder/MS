package com.ir.lbs.client;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.gwt.ajaxloader.client.AjaxLoader;
import com.google.gwt.ajaxloader.client.AjaxLoader.AjaxLoaderOptions;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.HasDirection.Direction;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.HorizontalSplitPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.maps.gwt.client.Animation;
import com.google.maps.gwt.client.GoogleMap;
import com.google.maps.gwt.client.LatLng;
import com.google.maps.gwt.client.MapOptions;
import com.google.maps.gwt.client.MapTypeId;
import com.google.maps.gwt.client.Marker;
import com.google.maps.gwt.client.MarkerOptions;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class LocationBasedSearch implements EntryPoint {
	/**
	 * The message displayed to the user when the server cannot be reached or
	 * returns an error.
	 */
	private static final String SERVER_ERROR = "An error occurred while "
			+ "attempting to contact the server. Please check your network "
			+ "connection and try again.";

	private static final String WIKINEWS_BASE_URL = "http://en.wikinews.org/wiki/";

	/**
	 * Create a remote service proxy to talk to the server-side Greeting service.
	 */
	private final QueryParserServiceAsync queryParser = GWT
			.create(QueryParserService.class);
	
	private final Button prevButton = new Button("Previous");
	private final Button nextButton = new Button("Next");
	private GoogleMap googleMap = null;
	private HorizontalSplitPanel horizontalSplitPanel = new HorizontalSplitPanel();
	private  Integer startOffset = 0;
	private Integer numFound = 0;
	Tree dateFacet = new Tree();
	final VerticalPanel facetPanel = new VerticalPanel();
	
	boolean IsFacetFilter = false;
	String facetFilterType = "";
	String facetFilteString = "";
	
	
	
	SuggestBox keywordSuggestBox= null ; // = new SuggestBox();
	final Button searchButton = new Button("Search");
	SuggestBox locationSuggestBox = null;// new SuggestBox();
	Label distanceLabel = new Label("Distance");
	final TextBox distanceTextBox = new TextBox();
	Label locationLabel = new Label("Location");
	final VerticalPanel resultsPanel = new VerticalPanel();
	
	public void onModuleLoad() {
		
		loadMapsAPI();
		
		
	}

	public void loadMapsAPI() {
		// TODO Auto-generated method stub
		   AjaxLoaderOptions options = AjaxLoaderOptions.newInstance();
		    options.setOtherParms("sensor=false&language=en");
		    Runnable callback = new Runnable() {
		      public void run() {
		        loadGUI();
		      }
		    };
		   AjaxLoader.loadApi("maps", "3", callback, options);
	}

	/**
	 * @wbp.parser.entryPoint
	 */
	public void loadGUI() {
		// TODO Auto-generated method stub
		final Label errorLabel = new Label();
		// Add the nameField and sendButton to the RootPanel
		// Use RootPanel.get() to get the entire body element
		RootPanel rootPanel = RootPanel.get();
		rootPanel.setSize("100%", "100%");
		//rootPanel.get("errorLabelContainer").add(errorLabel);
		AbsolutePanel absolutePanel = new AbsolutePanel();
		rootPanel.add(absolutePanel, 10, 10);
		absolutePanel.setSize("99%", "95%");
		
		HTMLPanel panel = new HTMLPanel("");
		panel.setStyleName("h1");
		absolutePanel.add(panel, 10, 10);
		panel.setSize("499px", "62px");
		
		HTML html = new HTML("<font size = \"16\" family = \"sans-serif\" color=\"grey\">Location Based Search </font>", false);
		html.setStyleName("h1");
		panel.add(html);
		html.setWidth("502px");
		
		///final SuggestBox keywordSuggestBox = new SuggestBox(new KeywordSuggest());
		keywordSuggestBox = new SuggestBox(new KeywordSuggest());
		// keywordSuggestBox.setText("Search For...");
		//keywordSuggestBox.setPopupStyleName("");
		absolutePanel.add(keywordSuggestBox, 10, 102);
		keywordSuggestBox.setSize("173px", "23px");
		
		///final Button searchButton = new Button("Search");
		absolutePanel.add(searchButton, 603, 105);
		searchButton.setSize("59px", "30px");
		
		///final SuggestBox locationSuggestBox = new SuggestBox(new AddressSuggest());
		locationSuggestBox = new SuggestBox(new AddressSuggest());
		absolutePanel.add(locationSuggestBox, 254, 102);
		locationSuggestBox.setSize("173px", "23px");
		
		///Label distanceLabel = new Label("Distance");
		distanceLabel.setDirection(Direction.LTR);
		absolutePanel.add(distanceLabel, 452, 112);
		distanceLabel.setSize("49px", "18px");
		
		///final TextBox distanceTextBox = new TextBox();
		// distanceTextBox.setText("Range...");
		absolutePanel.add(distanceTextBox, 509, 105);
		distanceTextBox.setSize("67px", "18px");
		
	///	Label locationLabel = new Label("Location");
		locationLabel.setDirection(Direction.LTR);
		absolutePanel.add(locationLabel, 199, 112);
		locationLabel.setSize("49px", "18px");
		
		// HorizontalSplitPanel horizontalSplitPanel = new HorizontalSplitPanel();
		horizontalSplitPanel.setSplitPosition("65%");
		absolutePanel.add(horizontalSplitPanel, 10, 156);
		horizontalSplitPanel.setSize("99%", "85%");
			
		
		
		///final VerticalPanel resultsPanel = new VerticalPanel();
		
		
		// resultsPanel.add(new HTML("<font color=\"blue\">Showing results for <u><i>" + keywordSuggestBox.getText() + " within " + distanceTextBox.getText() + " kms of " + locationSuggestBox.getText() + "</u></i></font>"));
		
		
		
		//horizontalSplitPanel.setLeftWidget(resultsPanel);
		HorizontalSplitPanel tree_res = new	HorizontalSplitPanel();
		tree_res.setRightWidget(resultsPanel);
		tree_res.setLeftWidget(facetPanel);
		horizontalSplitPanel.setLeftWidget(tree_res);
		resultsPanel.setSize("90%", "85%");
		initiateMap();
		
		
	    nextButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				// TODO Auto-generated method stub
				if (startOffset + 5 < numFound) {
					prevButton.setEnabled(Boolean.TRUE);
					startOffset = startOffset + 5;
					System.out.println("Start Offset : " + startOffset);
					executeQuery(keywordSuggestBox.getText(), locationSuggestBox.getText(), distanceTextBox.getText().equalsIgnoreCase("") ? "100":distanceTextBox.getText(), startOffset, resultsPanel,"");
				} else  {
					nextButton.setEnabled(Boolean.FALSE);
					prevButton.setEnabled(Boolean.TRUE);
					System.out.println("Start Offset : " + startOffset);
					executeQuery(keywordSuggestBox.getText(), locationSuggestBox.getText(), distanceTextBox.getText().equalsIgnoreCase("") ? "100":distanceTextBox.getText(), startOffset, resultsPanel,"");
				}
				
			}

		});
	    
	    prevButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				// TODO Auto-generated method stub
				
				
				if (startOffset <= 5) {
					prevButton.setEnabled(Boolean.FALSE);
					nextButton.setEnabled(Boolean.TRUE);
					executeQuery(keywordSuggestBox.getText(), locationSuggestBox.getText(), distanceTextBox.getText().equalsIgnoreCase("") ? "100":distanceTextBox.getText(), 0, resultsPanel,"");
				
				} else {
					nextButton.setEnabled(Boolean.TRUE);
					startOffset = startOffset - 5;
					executeQuery(keywordSuggestBox.getText(), locationSuggestBox.getText(), distanceTextBox.getText().equalsIgnoreCase("") ? "100":distanceTextBox.getText(), startOffset, resultsPanel,"");
				
				}
				
			}
	    	
	    });
			
		
		/*// Create the popup dialog box
		final DialogBox dialogBox = new DialogBox();
		dialogBox.setText("Remote Procedure Call");
		dialogBox.setAnimationEnabled(true);
		final Button closeButton = new Button("Close");
		// We can set the id of a widget by accessing its Element
		closeButton.getElement().setId("closeButton");
		final Label textToServerLabel = new Label();
		final HTML serverResponseLabel = new HTML();
		VerticalPanel dialogVPanel = new VerticalPanel();
		dialogVPanel.addStyleName("dialogVPanel");
		dialogVPanel.add(new HTML("<b>Sending name to the server:</b>"));
		dialogVPanel.add(textToServerLabel);
		dialogVPanel.add(new HTML("<br><b>Server replies:</b>"));
		dialogVPanel.add(serverResponseLabel);
		dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
		dialogVPanel.add(closeButton);
		dialogBox.setWidget(dialogVPanel);*/

		/*// Add a handler to close the DialogBox
		closeButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				dialogBox.hide();
				searchButton.setEnabled(true);
				searchButton.setFocus(true);
			}
		});*/
		
		searchButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				// TODO Auto-generated method stub
				IsFacetFilter=false;
				facetFilteString ="";
				
				if (keywordSuggestBox.getText().equals("")) {
					Window.alert("Please enter a keyword in the Keyword Search Field !!!");
				} else if (locationSuggestBox.getText().equals("")) {
					Window.alert("Please enter a location in the Location Search Field !!!");
				} else {
					
					startOffset = 0;
					numFound = 0;
					prevButton.setEnabled(Boolean.FALSE);
					
					executeQuery(keywordSuggestBox.getText(), locationSuggestBox.getText(), distanceTextBox.getText().equalsIgnoreCase("") ? "100":distanceTextBox.getText(), startOffset, resultsPanel,"");
				}
				
			}
			
		});

	}
	
		
	
	private void executeQuery(final String keywordQuery, final String locationQuery, final String distanceRange, Integer startOffset, final VerticalPanel resultsPanel,String filterQuery) {
		
		if(IsFacetFilter)
		{
			// 
			
			//  
			
			
			queryParser.parseQuery(keywordQuery, locationQuery, distanceRange, String.valueOf(startOffset), filterQuery,new AsyncCallback<List<List<String>>>() {

				@Override
				public void onFailure(Throwable caught) {
					// TODO Auto-generated method stub
					caught.printStackTrace();
				}

				
				@Override
				public void onSuccess(List<List<String>> result) {
					// TODO Auto-generated method stub
					String dateFacetString = "" ;
					if (null != result) {
						for (int index = 1; index < result.size(); index ++) {
							if( result.get(index).get(0).equalsIgnoreCase("datefacet") )
							{
								System.out.println("DateFacet data : " +result.get(index).get(2));
								dateFacetString = result.get(index).get(2);
							}
							else
							{
							System.out.println("Document Title : " + result.get(index).get(0));
							}
						}
						paintResultsPanel(resultsPanel, result, keywordQuery, distanceRange, locationQuery);
						
					}
				}
			});

			
			
		}
		else
		{
		
		// TODO Auto-generated method stub
		final int tempstartoffset = startOffset;
		queryParser.parseQuery(keywordQuery, locationQuery, distanceRange, String.valueOf(startOffset), "",new AsyncCallback<List<List<String>>>() {

			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub
				caught.printStackTrace();
			}

			
			@Override
			public void onSuccess(List<List<String>> result) {
				// TODO Auto-generated method stub
				String dateFacetString = "" ;
				if (null != result) {
					for (int index = 1; index < result.size(); index ++) {
						if( result.get(index).get(0).equalsIgnoreCase("datefacet") )
						{
							System.out.println("DateFacet data : " +result.get(index).get(2));
							dateFacetString = result.get(index).get(2);
						}
						else if (!result.get(index).get(0).equalsIgnoreCase("latlnglocations"))
						{
							System.out.println("Document Title : " + result.get(index).get(1));
						}
						// System.out.println("Document Snippet : " + document.get(1));
					}
					paintResultsPanel(resultsPanel, result, keywordQuery, distanceRange, locationQuery);
					
					if(!dateFacetString.equals(""))
					{
					dateFacet = createStaticTree(dateFacetString);
					
					
					dateFacet.addSelectionHandler(new SelectionHandler<TreeItem>() {
						  @Override
						  public void onSelection(SelectionEvent event) {
							 
							  
						    TreeItem item = (TreeItem) event.getSelectedItem();
						    if(item.getChild(0).isVisible())
						    {
						    	//not a leaf
						    }
						    else
						    {
						    // expand the selected item
						    IsFacetFilter = true ;
						    facetFilteString = "timestamp:["+item.getChild(0).getText()+" TO "+item.getChild(0).getText()+"+1MONTH]";
						    LocationBasedSearch.this.startOffset = 0;
						    executeQuery(keywordSuggestBox.getText(), locationSuggestBox.getText(), distanceTextBox.getText().equalsIgnoreCase("") ? "100":distanceTextBox.getText(), tempstartoffset, resultsPanel,facetFilteString);
						    }
						  }
						});
					
					
					facetPanel.clear();
					facetPanel.add(dateFacet);
					}
					
				}
			}
		});
		}
	}
	
	

	private Tree createStaticTree(String dateFacetString) {
	    // Create the tree
//	    String[] composers = {"text1","text2","text3"};
//	    String concertosLabel = constants.cwTreeConcertos();
//	    String quartetsLabel = constants.cwTreeQuartets();
//	    String sonatasLabel = constants.cwTreeSonatas();
//	    String symphoniesLabel = constants.cwTreeSymphonies();
		
		String[] arr =	dateFacetString.split("\\*");
		System.out.println(arr);
		TreeMap<Integer, TreeMap<Date,Integer>> mp = new TreeMap<Integer, TreeMap<Date,Integer>>();
		
		 for(int i = 0 ; i< arr.length ; i =i+2 )
		 {
			String date = arr[i];
			DateTimeFormat fmt = DateTimeFormat.getFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
			Date dt=  fmt.parse(date);
			
			if(mp.containsKey(dt.getYear() + 1900 ))
			{
				int fcount =  Integer.parseInt(arr[i+1]);
				if (fcount > 0)
				{
					mp.get(dt.getYear() + 1900).put(dt,fcount);
				}
			}
			else
			{
				int fcount =  Integer.parseInt(arr[i+1]);
				if (fcount > 0)
				{
					TreeMap<Date,Integer> tmp = new TreeMap<Date, Integer>();
					tmp.put(dt,fcount);
					mp.put(dt.getYear() + 1900, tmp);
				}
			}
			 
		 }
		
		
	    Tree staticTree = new Tree();
	    
	    for(Map.Entry<Integer,TreeMap<Date,Integer>> tmp : mp.entrySet())
	    {
	    	TreeItem item  = new TreeItem(tmp.getKey().toString());
	    	
	    	TreeMap<Date, Integer> countDetails  = tmp.getValue();
	    	DateTimeFormat temp1 = DateTimeFormat.getFormat("MMMM");
	    	DateTimeFormat queryFormat = DateTimeFormat.getFormat("yyyy-MM-dd'T00:00:00Z'");
	    	for( Map.Entry<Date, Integer> tempCount : countDetails.entrySet() )
	    	{
	    		TreeItem monthNode = new TreeItem(temp1.format(tempCount.getKey())+" (" + tempCount.getValue() + ")" );
	    		//item.setText(tempCount.getKey().toString());
	    		//monthNode.addTextItem(queryFormat.format(tempCount.getKey()));
	    		TreeItem text = new TreeItem(queryFormat.format(tempCount.getKey()));
	    		text.setVisible(false);
	    		monthNode.addItem(text);   		
	    		item.addItem(monthNode);
	    	}
	    	
	    	staticTree.addItem(item);   
	    	
		    }
	    

	    // Add some of Beethoven's music
//	    staticTree.addTextItem("text1");
//	    staticTree.addTextItem("text2");
//	    staticTree.addTextItem("text3");
//	    
//	    beethovenItem.addTextItem("");
//	    
////	    addMusicSection(beethovenItem, concertosLabel,
////	        constants.cwTreeBeethovenWorkConcertos());
//	    addMusicSection(
//	        beethovenItem, quartetsLabel, constants.cwTreeBeethovenWorkQuartets());
//	    addMusicSection(
//	        beethovenItem, sonatasLabel, constants.cwTreeBeethovenWorkSonatas());
//	    addMusicSection(beethovenItem, symphoniesLabel,
//	        constants.cwTreeBeethovenWorkSymphonies());
//
//	    // Add some of Brahms's music
//	    TreeItem brahmsItem = staticTree.addTextItem(composers[1]);
//	    addMusicSection(
//	        brahmsItem, concertosLabel, constants.cwTreeBrahmsWorkConcertos());
//	    addMusicSection(
//	        brahmsItem, quartetsLabel, constants.cwTreeBrahmsWorkQuartets());
//	    addMusicSection(
//	        brahmsItem, sonatasLabel, constants.cwTreeBrahmsWorkSonatas());
//	    addMusicSection(
//	        brahmsItem, symphoniesLabel, constants.cwTreeBrahmsWorkSymphonies());
//
//	    // Add some of Mozart's music
//	    TreeItem mozartItem = staticTree.addTextItem(composers[2]);
//	    addMusicSection(
//	        mozartItem, concertosLabel, constants.cwTreeMozartWorkConcertos());
//
//	    // Return the tree
	    return staticTree;
	  }

	

	private void initiateMap() {
		// TODO Auto-generated method stub
		
		AbsolutePanel mapPanel = new AbsolutePanel();
		horizontalSplitPanel.setRightWidget(mapPanel);
		mapPanel.setSize("95%", "90%");
		
		MapOptions options  = MapOptions.create();
		
		
        options.setCenter(LatLng.create(0, 0)); 
        options.setZoom(1);
        options.setMapTypeId(MapTypeId.ROADMAP);
        options.setDraggable(true);
        options.setMapTypeControl(true);
        options.setScaleControl(true);
        options.setScrollwheel(true);

		googleMap = GoogleMap.create(mapPanel.getElement(), options);
	}

	private void paintResultsPanel(VerticalPanel resultsPanel, List<List<String>> results, String keyword, String distance, String location) {
		// TODO Auto-generated method stub
		// Needs Validation
		
		
		/*final CellTable<HTMLPanel> cellTable = new CellTable<HTMLPanel>();
		cellTable.setPageSize(5);
		resultsPanel.add(cellTable);
		cellTable.setSize("100%", "100%");
		
		TextColumn<HTMLPanel> column = new TextColumn<HTMLPanel>(){

			@Override
			public String getValue(HTMLPanel object) {
				// TODO Auto-generated method stub
				return ((HTML)object.getWidget(0)).getText();
			}
		};
		
		cellTable.addColumn(column, "Results");*/
		
		resultsPanel.clear();
		
		if (null != results && results.size() > 3) {
			
			List<String> resultStats = results.get(0);
			numFound = Integer.valueOf(resultStats.get(1));
			
			resultsPanel.add(new HTML("<font color=\"blue\">Fetched " + numFound + " results for <u><i>" + keyword + " within " + distance + " kms of " + location + "</u></i> in " + resultStats.get(0) + " ms</font>"));
			
			// Fetched results are less than 5
			if (numFound <= 5) {
				prevButton.setEnabled(Boolean.FALSE);
				nextButton.setEnabled(Boolean.FALSE);
			} else {
				prevButton.setEnabled(Boolean.TRUE);
				nextButton.setEnabled(Boolean.TRUE);
			}
			
			/*horizontalSplitPanel.getRightWidget().removeFromParent();
			AbsolutePanel mapPanel = new AbsolutePanel();
			horizontalSplitPanel.setRightWidget(mapPanel);
			mapPanel.setSize("95%", "90%");*/
			
			initiateMap();
			
			ScrollPanel scrollableResultsPanel = new ScrollPanel();
			
			VerticalPanel documentsPanel = new VerticalPanel();
			scrollableResultsPanel.add(documentsPanel);
			
			resultsPanel.add(scrollableResultsPanel);
			
			HorizontalPanel footer = new HorizontalPanel();
			footer.add(prevButton);
			footer.add(nextButton);
			
			/*resultsPanel.add(cellTable);*/
			resultsPanel.add(new HTML("<br>"));
			resultsPanel.add(footer);
			
			List<String> latlngLocations;
			
			for (int index = 1; index < results.size(); index++) {
				
				if (results.get(index).get(0).equalsIgnoreCase("latlnglocations")) {
					
					// Draw location clustering based intensity map...
					
					latlngLocations = results.get(index);
					
					for (int counter = 1; counter < latlngLocations.size(); counter++) {
						
						String locPoint = latlngLocations.get(counter);
						
						MarkerOptions markerOpts = MarkerOptions.create();
						Double lat = Double.valueOf(locPoint.split(",")[0].trim());
						Double lng = Double.valueOf(locPoint.split(",")[1].trim());
						LatLng latlng = LatLng.create(lat, lng);
					    markerOpts.setPosition(latlng);
					    //markerOpts.setIcon("D:\\Course Materials\\Information Retrieval\\Project 3\\green_indicator.png");
					    markerOpts.setIcon("http://www.permantech.com/images/green_dot.png");
					    markerOpts.setAnimation(Animation.DROP);
					    
					    
					    final Marker marker = Marker.create(markerOpts);
					    marker.setMap(googleMap);
					    googleMap.setZoom(5.0);
					    googleMap.setCenter(latlng);
					    // marker.setTitle(document.get(0).trim());
					}
					
				} else if (!results.get(index).get(0).equalsIgnoreCase("datefacet")) {
					
					
					final List<String> document = results.get(index);
					Anchor anchor = new Anchor("<br><b>" + document.get(1) + "</b>", Boolean.TRUE);
					anchor.setStyleName("gwt-Anchor");
					anchor.setHref(WIKINEWS_BASE_URL + document.get(1).trim().replaceAll("\\s", "_"));
					// open new window when we click on anchor
					anchor.setTarget("_blank");
					
					documentsPanel.add(anchor);
					HTML snippet = new HTML(document.get(2));
					snippet.setWordWrap(Boolean.TRUE);
					documentsPanel.add(snippet);
					
					Anchor moreLikeThis = new Anchor("More Like This", Boolean.TRUE);
					moreLikeThis.setStyleName("gwt-Anchor");
					documentsPanel.add(moreLikeThis);
					
					// Register onclick handler for this particular moreLikeThis element
					moreLikeThis.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							
							final DialogBox dialogBox = new DialogBox();
							
							dialogBox.setAnimationEnabled(true);
							final Button closeButton = new Button("Close");
							// We can set the id of a widget by accessing its Element
							closeButton.getElement().setId("closeButton");
							
							final VerticalPanel vPanel = new VerticalPanel();
							dialogBox.add(vPanel);
							queryParser.moreLikeThis(document.get(0).trim(), new AsyncCallback<List<List<String>>>() {

								@Override
								public void onFailure(Throwable caught) {
									// TODO Auto-generated method stub
									caught.printStackTrace();
								}

								@Override
								public void onSuccess(List<List<String>> result) {
									// TODO Auto-generated method stub
									
									for (List<String> mltDoc : result) {
										System.out.println("MLT Doc Title : " + mltDoc.get(0));
										// System.out.println("MLT Doc Snippet : " + mltDoc.get(1));
										
									}
									dialogBox.setText("Top " + result.size() + " Similar Documents for \"" + document.get(1).trim() + "\"");
									populateMLTPanel(vPanel, result);
								}

								private void populateMLTPanel(VerticalPanel vPanel,
										List<List<String>> result) {
									
									vPanel.add(closeButton);
									for (List<String> mltDoc : result) {
										Anchor anchor = new Anchor("<br><b>" + mltDoc.get(0).trim() + "</b><br><br>", Boolean.TRUE);
										anchor.setHref(WIKINEWS_BASE_URL + mltDoc.get(0).trim().replaceAll("\\s+", "_"));
										anchor.setStyleName("gwt-Anchor");
										anchor.setTarget("_blank");
										
										vPanel.add(anchor);
									}
									
									dialogBox.show();
								}
								
							});
							
							

							// Add a handler to close the DialogBox
							closeButton.addClickHandler(new ClickHandler() {
								public void onClick(ClickEvent event) {
									dialogBox.hide();
									
									/*searchButton.setEnabled(true);
									searchButton.setFocus(true);*/
								}
							});
							
							
						}
					});
					
					/*MarkerOptions markerOpts = MarkerOptions.create();
					LatLng latlng = LatLng.create(Double.valueOf(document.get(2).split("/")[0]), Double.valueOf(document.get(2).split("/")[1]));
				    markerOpts.setPosition(latlng);
				    
				    final Marker marker = Marker.create(markerOpts);
				    marker.setMap(googleMap);
				    googleMap.setZoom(3.0);
				    googleMap.setCenter(latlng);
				    marker.setTitle(document.get(0).trim());*/
					
				} 
							    
			    
			   /* InfoWindowOptions infoWindowOpts = InfoWindowOptions.create();
			    infoWindowOpts.setContent("<b>" + document.get(0).trim() + "</b>");

			    final InfoWindow infoWindow = InfoWindow.create(infoWindowOpts);
			    infoWindow.open(googleMap);*/
			    
			    //googleMap.setZoom(6.0);

			    /*marker.addClickableChangedListener(new ClickableChangedHandler() {
					@Override
					public void handle() {
						// TODO Auto-generated method stub
						infoWindow.open(googleMap, marker);
					}
				    });*/
			}		
			
			
			
			
			
		} else {
			facetPanel.clear();
			initiateMap();
			resultsPanel.add(new HTML("<font color=\"red\">No results found for <u><i>" + keyword + " within " + distance + " kms of " + location + "</u></i></font>"));
		}
		

		
	}
}
