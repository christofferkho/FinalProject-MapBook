package com.example.mapbook;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
/**
 * @class SearchItem
 * @author Kho, Purswani, Ramos, Tiongson
 *
 */
public class SearchItem extends Activity {
	
	private TextView locNameView;
	private TextView addressView;
	private TextView notesView;
	private TextView coordsView;
	private String gpsList = "";
	private String locObjId = "";
	
	private ArrayList<LatLng> pathCoordinates = new ArrayList<LatLng>();
	
	private GoogleMap map;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_item);
		
		locNameView = (TextView) findViewById(R.id.searchItemLocName);
		addressView = (TextView) findViewById(R.id.searchItemAddress);
		notesView = (TextView) findViewById(R.id.searchItemNotes);
		coordsView = (TextView) findViewById(R.id.locationCoordinatesSearchItem);
		
		//instantiate the map
		map = ((MapFragment) getFragmentManager()
				.findFragmentById(R.id.mapSearch)).getMap();
		map.setMyLocationEnabled(true);
		map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		map.clear();
		
		//get objectId from intent
		Intent data = getIntent();
		locObjId = data.getStringExtra("locObjId");
		//query the objectId and get its contents
		ParseQuery<ParseObject> queryLoc = ParseQuery.getQuery(LocationDBAdapter.TABLE_LOC);
		queryLoc.whereEqualTo("objectId", locObjId);
		Log.d("Query", locObjId);
		queryLoc.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> locs, ParseException e) {
				// TODO Auto-generated method stub
				if(e == null){
					for(ParseObject loc: locs){
						ParseQuery<ParseObject> queryPoints = ParseQuery
								.getQuery(LocationDBAdapter.TABLE_PATH_LIST);
						queryPoints.whereEqualTo("location", loc);
						Log.d("Query", "location object");
						queryPoints.findInBackground(new FindCallback<ParseObject>() {

							@Override
							public void done(List<ParseObject> paths, ParseException e) {
								if (e == null) {
									for (ParseObject path : paths) {
										//get coordinates info
										double thisLat = path.getDouble("latitude");
										double thisLong = path.getDouble("longitude");
										LatLng current = new LatLng(thisLat, thisLong);
										pathCoordinates.add(current);
										gpsList += "Latitude: " + thisLat + "\n" + "Longitude: " + thisLong + "\n....\n";
										coordsView.setText(gpsList);
										Log.d("Path", "coords get");
									}
									initMap();
										
								}
								else {
									Log.d("Parse", e.getMessage());
								}

							}
						});
						//set the views
						locNameView.setText(loc.getString("locname"));
						addressView.setText(loc.getString("address"));
						notesView.setText(loc.getString("notes"));
					}
				}
				else {
					Log.d("Parse", e.getMessage());
				}
			}});
		
		
		
	}
	
	/**
	 * @method initMap plots points in the map, depending on whether the information is from a path 
	 * or location.
	 */
	public void initMap(){
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(pathCoordinates.get(0), 15));
		if(pathCoordinates.size() == 1){
			addMarker(pathCoordinates.get(0), MainActivity.SAVE_LOC);
		}
		else{
			for(LatLng x: pathCoordinates){
				addMarker(x, MainActivity.SAVE_PATH);
			}
		}
	}
	
	/**
	 * @method addMarker marks the map
	 * @param pos
	 * @param type
	 */
	private void addMarker(final LatLng pos, final int type)
	{
		// force this action on the UI thread, to update changes
		runOnUiThread(new Runnable() {
			public void run() {
				// Use is diff icon to indicate if the data is sent or not
				MarkerOptions markerOptions = new MarkerOptions()
						.position(pos);
				// based on local profile
				if(type == MainActivity.SAVE_LOC){
					markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.red_pin));					
				}
				else{
					markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.path));
				}
				Marker marker = map.addMarker(markerOptions);
				}
			});

	}
}
