package com.example.mapbook;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.AdapterView.OnItemClickListener;

/**
 * @class MainActivity
 * @author Kho, Purswani, Ramos, Tiongson
 *
 */
public class MainActivity extends Activity {
	
	//default coordinates
	private double latitude = 0.0;
	private double longitude = 0.0;
	
	
	private ViewFlipper viewFlipper;
	private ImageButton listButton, mapButton, createButton, searchButton;
	
	private TextView latText;
	private TextView longText;
	
	//database connectivity
	private SimpleCursorAdapter dataAdapter;
	private LocationDBAdapter dbHelper;
	
	private Context context = this;
	
	private EditText editLocName, editAddress, editNotes;
	
	private GoogleMap map;
	
	private LocationManager locationManager;
	
	//final Strings, used for determining whether methods are working with paths or locations.
	private final int VIEW_ONLY = 0;
	private final int VIEW_PATH = 1;
	
	private final int ADD_MARKER = 0;
	private final int NO_MARKER = 1;
	
	public static final int SAVE_LOC = 0;
	public static final int SAVE_PATH = 1;
	public static final String LOCAL = "savedLocally";
	
	private ObjectMapper om;
	
	private ArrayList<LatLng> pathCoordinates = new ArrayList<LatLng>();
	
	private String user;
	private String city;
	private String country;
	
	private String osmUser;
	private String osmPass;
	
	private String objectId;
	
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//open LocationDBAdapter
		dbHelper = new LocationDBAdapter(this);
		dbHelper.open();
		
		//get user info from login screen
		Intent data = getIntent();
		
		//sets user string to firstname and lastname, to be placed in textview profileName
		if(data.hasExtra(Login.FIRSTNAME)){
			user = data.getStringExtra(Login.FIRSTNAME) + " " + data.getStringExtra(Login.LASTNAME);
		}
		else{
			user = "Offline Mode";
		}
		
		//sets city string to be placed in textview profileCity
		if(data.hasExtra(Login.CITY)){
			city = data.getStringExtra(Login.CITY);
		}
		else{
			city = " ";
		}
		
		//sets country string to be placed in textview profileCountry
		if(data.hasExtra(Login.COUNTRY)){
			country = data.getStringExtra(Login.COUNTRY);
		}
		else{
			country = " ";
		}
		
		//sets osm credentials
		if(data.hasExtra(Login.OSMUSER) && data.hasExtra(Login.OSMPASS)){
			osmUser = data.getStringExtra(Login.OSMUSER);
			osmPass = data.getStringExtra(Login.OSMPASS);
		}
		else{
			osmUser = "";
			osmPass = "";
		}
		//obtain coordinates/locations from parse if logged in
		if(data.hasExtra(Login.ID)){
			//dbHelper.deleteAllInfo();
			
			//get objectId from intent from login screen
			objectId = data.getStringExtra(Login.ID);
			//query coordinates, include location, from parse which have the same idUser
			ParseQuery<ParseObject> queryPoints = ParseQuery
					.getQuery(LocationDBAdapter.TABLE_PATH_LIST);
			queryPoints.whereEqualTo("iduser", objectId);
			queryPoints.include("location");
			Log.d("Parse", "querry with locid and objid");
			queryPoints.findInBackground(new FindCallback<ParseObject>() {

				@Override
				public void done(List<ParseObject> paths, ParseException e) {
					if (e == null) {
						Log.d("Parse", "no path error");
						for (ParseObject path : paths) {
							//get coordinates info
							String pathObjId = path.getObjectId();
							Cursor checkDuplicatePath = dbHelper.fetchPathByObjId(pathObjId);
							if (checkDuplicatePath.getCount() == 0) {
								int locId = path.getInt("idlocation");
								double thisLat = path.getDouble("latitude");
								double thisLong = path.getDouble("longitude");
								String time = path.getString("time");
								ParseObject location = path
										.getParseObject("location");
								String locObjId = location.getObjectId();
								//now that we have the needed information, 
								//we can create a row for the coordinates in SQLite
								long pathId = dbHelper.createPath(locId,
										thisLat, thisLong, time, objectId,
										locObjId, pathObjId);
								Log.d("path", "pathcreated");
								//get the rest of the location's information
								String locName = location.getString("locname");
								String address = location.getString("address");
								String notes = location.getString("notes");
								String slocId = Long.toString(locId);
								Cursor checkDuplicateLocation = dbHelper
										.fetchLocByObjId(
												LocationDBAdapter.TABLE_LOC,
												locObjId);
								Log.d("loc",
										"cursor count "
												+ Integer
														.toString(checkDuplicateLocation
																.getCount()));
								//if there are no locations with the same objectId, 
								//we create a new row in SQLite
								if (checkDuplicateLocation.getCount() == 0) {
									//create new Location row, get the _id primary key
									long newLocId = dbHelper.createLoc(locName,
											address, "", objectId, notes,
											locObjId);
									Log.d("loc",
											"loc created at "
													+ Long.toString(newLocId));
									slocId = Long.toString(newLocId);
									//edit this path's locationId to the newly created
									//location's _id primary key
									dbHelper.editPathLocId(slocId,
											Long.toString(pathId));
									path.put("idlocation", newLocId);
									path.saveInBackground();
								}
								//else, we don't need to create a new location.
								else {
									Log.d("loc",
											"found a dupicate, no need to create a new location");
								}
							}
							else{
								Log.d("PathObjId", "path's objectId already exists in this SQLite database, no need to save.");
							}
							
							
						}
					} else {
						Log.d("Parse", e.getMessage());
					}

				}
			});
		}
		else{
			objectId = LOCAL;
		}
		
		//set textviews at the top of the viewflipper
		TextView profileName = (TextView) findViewById(R.id.profile_name);
		TextView profileCountry = (TextView) findViewById(R.id.profile_country);
		TextView profileCity = (TextView) findViewById(R.id.profile_city);
		
		profileName.setText(user);
		profileCountry.setText(country);
		profileCity.setText(city);
		
		
		
		
		
		viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper1);
		listButton = (ImageButton) findViewById(R.id.locationListViewButton);
		mapButton = (ImageButton) findViewById(R.id.mapButton);
		createButton = (ImageButton) findViewById(R.id.createButton);
		searchButton = (ImageButton) findViewById(R.id.searchOpen);
		
		//this is needed to get GPS coordinates
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
      //these are the initial coordinates, if they are available
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(location != null){
        	latitude = location.getAltitude();
        	longitude = location.getLongitude();
		}
		
		
		listButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	listLocations();
            }
        });
		
		mapButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	viewMap(VIEW_ONLY, "", "", "");
            }
        });
		
		createButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	create();
            }
        });
		
		searchButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
				if(activeNetworkInfo != null && activeNetworkInfo.isConnected()){
					Intent intent = new Intent(context, Search.class);
	            	startActivity(intent);
				}
				else{
					Toast toast = Toast.makeText(context, "Cannot search items. \nPlease connect to the internet!", Toast.LENGTH_SHORT);
					toast.show();
				}
            }
        });
		
		//check for info not associated with any account
		Cursor locallySavedInfoChecker = dbHelper.fetchLocByUser(LocationDBAdapter.TABLE_LOC, LOCAL);
		Log.d("objid", objectId);
		Log.d("num of rows", Integer.toString(locallySavedInfoChecker.getCount()));
		if ((!objectId.equals(LOCAL)) && !(locallySavedInfoChecker.getCount() == 0)) {
			//display alert dialog
			showAlertDialog();
		}

	}
	
	//ALERT DIALOG STARTS HERE
	public void showAlertDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("There are locations in your phone that are not saved to this account. Would you like to save" +
				" these into your account?")
		       .setCancelable(false)
		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) 
		           {
		        	   //find locations/paths that don't have objectIds.
		        	   Cursor cursorLocs = dbHelper.fetchLocByUser(LocationDBAdapter.TABLE_LOC, LOCAL);
		        	   cursorLocs.moveToFirst();
		        	   do{
		        		   //get their information
		        		    final int locId = cursorLocs.getInt(cursorLocs.getColumnIndexOrThrow(LocationDBAdapter.KEY_ROWID));
		        		    String sLocId = Integer.toString(locId);
		   					String locName = cursorLocs.getString(cursorLocs.getColumnIndexOrThrow(LocationDBAdapter.KEY_LOC_NAME));
		   					String locAdd = cursorLocs.getString(cursorLocs.getColumnIndexOrThrow(LocationDBAdapter.KEY_LOC_ADD));
		   					String locNotes = cursorLocs.getString(cursorLocs.getColumnIndexOrThrow(LocationDBAdapter.KEY_LOC_NOTES));
		   					final ParseObject loc = new ParseObject(LocationDBAdapter.TABLE_LOC);
		   					//put their info in a parse object
		   					loc.put("locationId", locId);
		   					loc.put(LocationDBAdapter.KEY_LOC_NAME, locName);
		   					loc.put(LocationDBAdapter.KEY_LOC_ADD, locAdd);
		   					loc.put(LocationDBAdapter.KEY_USER, objectId);
		   					loc.put(LocationDBAdapter.KEY_LOC_NOTES, locNotes);
		   					Cursor cursorCoords = dbHelper.fetchPathByLocId(sLocId);
		   					cursorCoords.moveToFirst();
		   					do{
		   						final long coordId = cursorCoords.getLong(cursorCoords.getColumnIndexOrThrow(LocationDBAdapter.KEY_ROWID));
		   						double thisLat = cursorCoords.getDouble(cursorCoords.getColumnIndexOrThrow(LocationDBAdapter.KEY_LAT));
		   						double thisLong = cursorCoords.getDouble(cursorCoords.getColumnIndexOrThrow(LocationDBAdapter.KEY_LONG));
		   						String time = cursorCoords.getString(cursorCoords.getColumnIndexOrThrow(LocationDBAdapter.KEY_TIME));
		   						final ParseObject path = new ParseObject(LocationDBAdapter.TABLE_PATH_LIST);
		   						//put the coordinates in another parse object
		   						path.put(LocationDBAdapter.KEY_LOCATION_ID, locId);
		   						path.put(LocationDBAdapter.KEY_LAT, thisLat);
		   						path.put(LocationDBAdapter.KEY_LONG, thisLong);
		   						path.put(LocationDBAdapter.KEY_TIME, time);
		   						path.put(LocationDBAdapter.KEY_USER, objectId);
		   						path.put("location", loc);
		   						
		   						path.saveInBackground(new SaveCallback() {
		   							
		   							@Override
		   							public void done(ParseException e) {
		   								// TODO Auto-generated method stub
		   								if(e == null){
		   									// Saved successfully.
		   				                    Log.d("Saving", "User update saved!");
		   				                    String locObjId = loc.getObjectId();
		   				                    String sPathId = Long.toString(coordId);
		   				                    //update the objectId's in the SQLite database
		   				                    dbHelper.editLocObjId(locObjId, Long.toString(locId));
		   				                    dbHelper.editPathObjId(path.getObjectId(), sPathId);
		   				                    dbHelper.editPathLocObjId(locObjId, sPathId);
		   								}
		   								else{
		   									// The save failed.
		   				                    Log.d("Saving", "error: " + e);
		   								}
		   								
		   							}
		   						});
		   					}while(cursorCoords.moveToNext());
		        	   }while(cursorLocs.moveToNext());
		        	   Toast.makeText(getApplicationContext(), "Information saved to account", Toast.LENGTH_SHORT).show();
		           }
		       })
		       .setNegativeButton("No", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) 
		           {
				        Toast.makeText(getApplicationContext(), "Locations will not be saved.", Toast.LENGTH_SHORT).show();
		           }
		       });
		AlertDialog alert = builder.show();
		alert.show();
	}
	
	
	//ALERT DIALOG ENDS HERE
	
	//LISTVIEW VIEWFLIPPER METHODS START HERE
	
	/**
	 * @method listLocations instatiates the listview
	 */
	public void listLocations(){
		viewFlipper.setDisplayedChild(0);
		
		Cursor cursorAllInfo = dbHelper.fetchAllLoc();
 		
     	//listview which will list the location information.
     	ListView listView = (ListView) findViewById(R.id.locationListView);
     	
     	//Cursor adapter to fill in a row in the list. Flag is set to default
     	dataAdapter = new SimpleCursorAdapter(this, R.layout.rowlayout, cursorAllInfo, 
     			new String[] { LocationDBAdapter.KEY_LOC_NAME, LocationDBAdapter.KEY_LOC_ADD}, // columns to be bound  
     			new int[] { R.id.loctext, R.id.addtext}, 0// views id which the data will be bound to 
    			); 
     	listView.setAdapter(dataAdapter);
     	//Clicking a row will open the EditLocation activity
     	listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> listView, View view,
					int position, long id) {
						// Get the cursor, positioned to the corresponding row in the result set
				Cursor cursor = (Cursor) listView.getItemAtPosition(position);

						// Get the location information from this row in the database.
				int locId = cursor.getInt(cursor.getColumnIndexOrThrow(LocationDBAdapter.KEY_ROWID));
				String sLocId = Integer.toString(locId);
				String locName = cursor.getString(cursor.getColumnIndexOrThrow(LocationDBAdapter.KEY_LOC_NAME));
				String locAdd = cursor.getString(cursor.getColumnIndexOrThrow(LocationDBAdapter.KEY_LOC_ADD));
				String locNotes = cursor.getString(cursor.getColumnIndexOrThrow(LocationDBAdapter.KEY_LOC_NOTES));
				String locObjId = cursor.getString(cursor.getColumnIndexOrThrow(LocationDBAdapter.KEY_OBJID));
				Intent editLocation = new Intent(context, EditLocation.class);
				editLocation.putExtra("id", sLocId);
				editLocation.putExtra("locName", locName);
				editLocation.putExtra("locAdd", locAdd);
				editLocation.putExtra("locNotes", locNotes);
				editLocation.putExtra("locObjId", locObjId);
				editLocation.putExtra("osmUser", osmUser);
				editLocation.putExtra("osmPass", osmPass);
				editLocation.putExtra("objectId", objectId);
				editLocation.putExtra("country", country);
				startActivity(editLocation);
			}
		});
	}
	
	//LISTVIEW VIEWFLIPPER METHODS END HERE
	
	//CREATE VIEWFLIPPER METHODS START HERE
	
	/**
	 * @method create allows for the creation of paths or locations
	 */
	public void create(){
		viewFlipper.setDisplayedChild(1);
		
		editLocName = (EditText) findViewById(R.id.editName);
        editAddress = (EditText) findViewById(R.id.editAddress);
        editNotes = (EditText) findViewById(R.id.editNotes);
        
        latText = (TextView) findViewById(R.id.latitude);
        longText = (TextView) findViewById(R.id.longitude);
        
        //listener class allows GPS coordinates to change once the device has changed location in the physical world
        LocationListener listener = new LocationListener(){
        	/**
    		 * method onLocationChanged will update the latitude and longitude textviews
    		 * @param location
    		 */
        	@Override
    		public void onLocationChanged(Location location) {
    			// TODO Auto-generated method stub
    			if(location != null){
    				latitude = location.getLatitude();
    				longitude = location.getLongitude();
    				
    				String sLat = Double.toString(latitude);
    				String sLong = Double.toString(longitude);
    				
    				latText.setText(sLat);
    				longText.setText(sLong);
    			}
    			
    		}

    		/**
    		 * 
    		 * @param provider
    		 */
        	@Override
    		public void onProviderDisabled(String provider) {
    			// TODO Auto-generated method stub
    			
    		}

    		/**
    		 * 
    		 * @param provider
    		 */
        	@Override
    		public void onProviderEnabled(String provider) {
    			// TODO Auto-generated method stub
    			
    		}

    		/**
    		 * 
    		 * @param provider
    		 * @param status
    		 * @param extras
    		 */
        	@Override
    		public void onStatusChanged(String provider, int status,
    				Bundle extras) {
    			// TODO Auto-generated method stub
    			
    		}
        };
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
        
        
        Button save = (Button) findViewById(R.id.save);
        save.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) 
			{
				String inputLocation = editLocName.getText().toString();
        		String inputAddress = editAddress.getText().toString();
        		String inputNotes = editNotes.getText().toString();
        		//go to save method.
        		boolean successful = save(inputLocation, inputAddress, inputNotes, SAVE_LOC);
				
				if(successful)
				{
					Toast successToast = Toast.makeText(context,"Successfully saved!", Toast.LENGTH_SHORT);
					successToast.show();
					
				}
				else
				{
					Toast errorToast = Toast.makeText(context,"ALL FIELDS NEEDED", Toast.LENGTH_SHORT);
					errorToast.show(); 
				}
			}
		});
        
        Button newPath = (Button) findViewById(R.id.newPath);
        newPath.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) 
			{
				String inputLocation = editLocName.getText().toString();
        		String inputAddress = editAddress.getText().toString();
        		String inputNotes = editNotes.getText().toString();
        		//go to viewMap method, but this will not display all the info you have saved.
        		//instead, you will plot the points of the path you are currently tracking.
				viewMap(VIEW_PATH, inputLocation, inputAddress, inputNotes);
			}
		});
	}
	
	/**
	 * @method save saves information to SQLite and Parse, if logged in.
	 * @param location
	 * @param address
	 * @param notes
	 * @param request
	 * @return true if information was successfully saved, false if not.
	 */
	public boolean save(String location, String address, String notes, int request){
		if (location.equals("") || notes.equals("") || address.equals("") )
		{
			return false;
		}
		else if (request == SAVE_LOC)
		{
			final long locId = dbHelper.createLoc(location, address, "", objectId, notes, "");
			Calendar calendar = Calendar.getInstance();
			Date current = calendar.getTime();
			DateFormat date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			
			String tempTime = date.format(current);
			double thisLat = latitude;
			double thisLong = longitude;
			final long pathId = dbHelper.createPath(locId, thisLat, thisLong, tempTime, objectId, "", "");
			if(!(objectId.equals(LOCAL))){
				final ParseObject loc = new ParseObject(LocationDBAdapter.TABLE_LOC);
				loc.put("locationId", locId);
				loc.put(LocationDBAdapter.KEY_LOC_NAME, location);
				loc.put(LocationDBAdapter.KEY_LOC_ADD, address);
				loc.put(LocationDBAdapter.KEY_USER, objectId);
				loc.put(LocationDBAdapter.KEY_LOC_NOTES, notes);
				
				final ParseObject path = new ParseObject(LocationDBAdapter.TABLE_PATH_LIST);
				path.put(LocationDBAdapter.KEY_LOCATION_ID, locId);
				path.put(LocationDBAdapter.KEY_LAT, thisLat);
				path.put(LocationDBAdapter.KEY_LONG, thisLong);
				path.put(LocationDBAdapter.KEY_TIME, tempTime);
				path.put(LocationDBAdapter.KEY_USER, objectId);
				path.put("location", loc);
				
				path.saveInBackground(new SaveCallback() {
					
					@Override
					public void done(ParseException e) {
						// TODO Auto-generated method stub
						if(e == null){
							// Saved successfully.
		                    Log.d("Saving", "User update saved!");
		                    String locObjId = loc.getObjectId();
		                    String sPathId = Long.toString(pathId);
		                    dbHelper.editLocObjId(locObjId, Long.toString(locId));
		                    dbHelper.editPathObjId(path.getObjectId(), sPathId);
		                    dbHelper.editPathLocObjId(locObjId, sPathId);
						}
						else{
							// The save failed.
		                    Log.d("Saving", "error: " + e);
						}
						
					}
				});
			}
			return true;
		}
		else if (request == SAVE_PATH){
			final long locId = dbHelper.createLoc(location, address, "", objectId, notes, "");
			final ParseObject loc= new ParseObject(LocationDBAdapter.TABLE_LOC);;
			if (!(objectId.equals(LOCAL))) 
				loc.put("locationId", locId);{
				loc.put(LocationDBAdapter.KEY_LOC_NAME, location);
				loc.put(LocationDBAdapter.KEY_LOC_ADD, address);
				loc.put(LocationDBAdapter.KEY_USER, objectId);
				loc.put(LocationDBAdapter.KEY_LOC_NOTES, notes);
			}
			//boolean locSaved = false;
			for(LatLng x: pathCoordinates){
				Calendar calendar = Calendar.getInstance();
				Date current = calendar.getTime();
				DateFormat date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
				
				String tempTime = date.format(current);
				double thisLat = x.latitude;
				double thisLong = x.longitude;
				final long pathId = dbHelper.createPath(locId, thisLat, thisLong, tempTime, objectId, "", "");
				if(!(objectId.equals(LOCAL))){
					final ParseObject path = new ParseObject(LocationDBAdapter.TABLE_PATH_LIST);
					path.put(LocationDBAdapter.KEY_LOCATION_ID, locId);
					path.put(LocationDBAdapter.KEY_LAT, thisLat);
					path.put(LocationDBAdapter.KEY_LONG, thisLong);
					path.put(LocationDBAdapter.KEY_TIME, tempTime);
					path.put(LocationDBAdapter.KEY_USER, objectId);
					path.put("location", loc);
					
					path.saveInBackground(new SaveCallback() {
						
						@Override
						public void done(ParseException e) {
							// TODO Auto-generated method stub
							if(e == null){
								// Saved successfully.
			                    Log.d("Saving", "User update saved!");
			                    String locObjId = loc.getObjectId();
			                    String sPathId = Long.toString(pathId);
			                    dbHelper.editLocObjId(locObjId, Long.toString(locId));
			                    dbHelper.editPathObjId(path.getObjectId(), sPathId);
			                    dbHelper.editPathLocObjId(locObjId, sPathId);
							}
							else{
								// The save failed.
			                    Log.d("Saving", "error: " + e);
							}
							
						}
					});
				
				}
			}
			
			return true;
		}
		else{
			return false;
		}
	}
	
	//CREATE VIEWFLIPPER METHODS END HERE
	
	//MAP VIEWFLIPPER METHODS START HERE
	
	/**
	 * @method viewMap allows user to see all saved information, or view the current path being tracked
	 * @param request
	 * @param locName
	 * @param address
	 * @param notes
	 */
	public void viewMap(int request, final String locName, final String address, final String notes){
		viewFlipper.setDisplayedChild(2);
		map = ((MapFragment) getFragmentManager()
				.findFragmentById(R.id.map)).getMap();
		map.setMyLocationEnabled(true);
		map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		LatLng current = new LatLng(latitude, longitude);
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 15));
		map.clear();
		if (request == VIEW_ONLY) {
			Cursor cursorAllInfo = dbHelper.fetchAllLoc();
			try {
				int rowIdIndex = cursorAllInfo
						.getColumnIndex(LocationDBAdapter.KEY_ROWID);
				int locNameIndex = cursorAllInfo
						.getColumnIndex(LocationDBAdapter.KEY_LOC_NAME);
				cursorAllInfo.moveToFirst();
				do{
					int locationPrimaryKey = cursorAllInfo.getInt(rowIdIndex);
					String sLocationPrimaryKey = Integer
							.toString(locationPrimaryKey);
					String locationName = cursorAllInfo.getString(locNameIndex);
					Cursor cursorPathInfo = dbHelper
							.fetchPathByLocId(sLocationPrimaryKey);
					int pathLatIndex = cursorPathInfo
							.getColumnIndex(LocationDBAdapter.KEY_LAT);
					int pathLongIndex = cursorPathInfo
							.getColumnIndex(LocationDBAdapter.KEY_LONG);
					cursorPathInfo.moveToFirst();
					if(cursorPathInfo.moveToNext()){
						cursorPathInfo.moveToFirst();
						do{
							double thisLatitude = cursorPathInfo
									.getDouble(pathLatIndex);
							double thisLongitude = cursorPathInfo
									.getDouble(pathLongIndex);
							LatLng temp = new LatLng(thisLatitude, thisLongitude);
							addMarker(temp, ADD_MARKER, SAVE_PATH);
						}while (cursorPathInfo.moveToNext());
					}
					else{
						cursorPathInfo.moveToFirst();
						double thisLatitude = cursorPathInfo
								.getDouble(pathLatIndex);
						double thisLongitude = cursorPathInfo
								.getDouble(pathLongIndex);
						LatLng temp = new LatLng(thisLatitude, thisLongitude);
						addMarker(temp, ADD_MARKER, SAVE_LOC);
					}
				} while (cursorAllInfo.moveToNext());
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if (request == VIEW_PATH){
			final LocationListener listener = new LocationListener(){
	        	/**
	    		 * method onLocationChanged will update the latitude and longitude textviews
	    		 * @param location
	    		 */
	        	@Override
	    		public void onLocationChanged(Location location) {
	    			// TODO Auto-generated method stub
	    			if(location != null){
	    				latitude = location.getLatitude();
	    				longitude = location.getLongitude();
	    				
	    				LatLng temp = new LatLng(latitude, longitude);
	    				addMarker(temp, ADD_MARKER, SAVE_PATH);
	    			}
	    			
	    		}

	    		/**
	    		 * 
	    		 * @param provider
	    		 */
	        	@Override
	    		public void onProviderDisabled(String provider) {
	    			// TODO Auto-generated method stub
	    			
	    		}

	    		/**
	    		 * 
	    		 * @param provider
	    		 */
	        	@Override
	    		public void onProviderEnabled(String provider) {
	    			// TODO Auto-generated method stub
	    			
	    		}

	    		/**
	    		 * 
	    		 * @param provider
	    		 * @param status
	    		 * @param extras
	    		 */
	        	@Override
	    		public void onStatusChanged(String provider, int status,
	    				Bundle extras) {
	    			// TODO Auto-generated method stub
	    			
	    		}
	        };
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 5, listener);
			
			final Button saveNewPath = (Button) findViewById(R.id.saveNewPath);
			saveNewPath.setVisibility(Button.VISIBLE);
			saveNewPath.setOnClickListener(new OnClickListener() {
				
				public void onClick(View v) 
				{
	        		boolean successful = save(locName, address, notes, SAVE_PATH);
					if(successful)
					{
						Toast successToast = Toast.makeText(context,"Successfully saved!", Toast.LENGTH_SHORT);
						successToast.show();
						pathCoordinates.clear();
						saveNewPath.setVisibility(Button.INVISIBLE);
						locationManager.removeUpdates(listener);
						
					}
					else
					{
						Toast errorToast = Toast.makeText(context,"Error. Unable to Save paths", Toast.LENGTH_SHORT);
						errorToast.show(); 
						pathCoordinates.clear();
						saveNewPath.setVisibility(Button.INVISIBLE);
						locationManager.removeUpdates(listener);
					}
				}
			});
		}
	}
	
	/**
	 * @method addMarker marks coordinates as paths or locations
	 * @param pos
	 * @param request
	 * @param type
	 */
	private void addMarker(final LatLng pos, int request, final int type)
	{
		if (request == ADD_MARKER) {
			// force this action on the UI thread, to update changes
			runOnUiThread(new Runnable() {
				public void run() {
					// Use is diff icon to indicate if the data is sent or not
					MarkerOptions markerOptions = new MarkerOptions()
							.position(pos);

					

					// based on local profile
					if(type == SAVE_LOC){
						markerOptions.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.red_pin));
						markerOptions.title("Location");
					}
					else{
						markerOptions.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.path));
						markerOptions.title("Path");
					}
					

					Marker marker = map.addMarker(markerOptions);
					
					pathCoordinates.add(pos);
				}
			});
		}
		else{}

	}
	
	//MAP VIEWFLIPPER METHODS END HERE
}

