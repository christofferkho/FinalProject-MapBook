package com.example.mapbook;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @class EditLocation
 * @author Kho, Purswani, Ramos, Tiongson
 *
 */
public class EditLocation extends Activity {
	
	private ProgressDialog loadingGPX;
	
	private LocationDBAdapter dbHelper;
	
	private EditText editLocation;
	private EditText editAddress;
	private EditText editNotes;
	
	private Button cancel;
	private Button save;
	private Button send;
	
	private TextView gpsText;
	
	private String gpsList = "";
	
	private Context context = this;
	
	private ObjectMapper om;
	
	//location's information
	private String id;
	private String locName;
	private String locAdd;
	private String locNotes;
	private String locObjId;
	
	private File gpxFile;
	
	//osm credentials
	String osmUser = "";
	String osmPass = "";
	
	//user's country and object id
	String country = "";
	String objectId = "";
	
	//multi-part form data format strings
	private static final String BOUNDARY = "----------------------------d10f7aa230e8";
    private static final String LINE_END = "\r\n";
    private static final int BUFFER_SIZE = 65535;
    private static final String BASE64_ENC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
	
	SaveGPX createGPX;
	
	Cursor pathCursor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_location);
		
		dbHelper = new LocationDBAdapter(this);
		dbHelper.open();
		
		editLocation = (EditText) findViewById(R.id.editNewLocationName);
		editAddress = (EditText) findViewById(R.id.editNewAddress);
		editNotes = (EditText) findViewById(R.id.editNewNotes);
		
		gpsText = (TextView) findViewById(R.id.locationCoordinates);
		
		//get the location/path information from the intent
		Intent i = getIntent();
		id = i.getStringExtra("id");
		locName = i.getStringExtra("locName");
		locAdd = i.getStringExtra("locAdd");
		locNotes = i.getStringExtra("locNotes");
		locObjId = i.getStringExtra("locObjId");
		country = i.getStringExtra("country");
		osmUser = i.getStringExtra("osmUser");
		osmPass = i.getStringExtra("osmPass");
		objectId = i.getStringExtra("objectId");
		
		editLocation.setText(locName);
		editAddress.setText(locAdd);
		editNotes.setText(locNotes);
		
		om = new ObjectMapper();
		
		createGPX = new SaveGPX();
		
		try {
			//get the coordinates
			pathCursor = dbHelper.fetchPathByLocId(id);
			pathCursor.moveToFirst();
			do {
				double thisLat = pathCursor.getDouble(pathCursor.getColumnIndexOrThrow(LocationDBAdapter.KEY_LAT));
				double thisLong = pathCursor.getDouble(pathCursor.getColumnIndexOrThrow(LocationDBAdapter.KEY_LONG));
				gpsList += "Latitude: " + thisLat + "\n" + "Longitude: " + thisLong + "\n....\n";
			} while (pathCursor.moveToNext());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		gpsText.setText(gpsList);
		
		save = (Button) findViewById(R.id.saveEditLocation);
        save.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) 
			{
				save();
			}
		});
        
        cancel = (Button) findViewById(R.id.cancelEditLocation);
        cancel.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) 
			{
				finish();			
			}
		});
        
        send = (Button) findViewById(R.id.sendToOSM);
        send.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) 
			{
				//check if the user is connected to the internet.
				//no need to try to send if there is no internet connection.
				ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
				if(activeNetworkInfo != null && activeNetworkInfo.isConnected()){
					if(objectId.equals(MainActivity.LOCAL)){
						Toast toast = Toast.makeText(context, "Cannot send. \nPlease Log into an account!", Toast.LENGTH_SHORT);
						toast.show();
					}
					else{
						if(osmUser.equals("") || osmPass.equals("")){
							Toast toast = Toast.makeText(context, "Cannot send. \nThis account has no OSM credentials!", Toast.LENGTH_SHORT);
							toast.show();
						}
						else{
							send();
						}
						
					}
				}
				else{
					Toast toast = Toast.makeText(context, "Cannot send. \nPlease connect to the internet!", Toast.LENGTH_SHORT);
					toast.show();
				}
			}
		});
	}
	
	/**
	 * @method save saves all changed information in a location/path
	 */
	public void save(){
		//get the strings
		final String newName = editLocation.getText().toString();
		final String newAdd = editAddress.getText().toString();
		final String newNotes = editNotes.getText().toString();
		if(newName.equals(locName) && newAdd.equals(locAdd) && newNotes.equals(locNotes)){
			Toast toast = Toast.makeText(this, "Nothing was changed!", Toast.LENGTH_SHORT);
			toast.show();
		}
		else{
			int i = dbHelper.editLocation(newName, newAdd, newNotes, id);
			String success = "Edit successful. Total rows edited: " + i;
			Toast toast = Toast.makeText(this, success, Toast.LENGTH_SHORT);
			toast.show();
			if(!objectId.equals("")){
				//query the location being edited
				Log.d("editing in parse", objectId);
				ParseQuery<ParseObject> queryPoints = ParseQuery.getQuery(LocationDBAdapter.TABLE_LOC);
				Log.d("editing in parse", "querying");
				queryPoints.getInBackground(locObjId, new GetCallback<ParseObject>() {

					@Override
					public void done(ParseObject loc, ParseException e) {
						// TODO Auto-generated method stub
						if(e == null){
							//save the changes in parse
							loc.put("locationId", id);
							loc.put(LocationDBAdapter.KEY_LOC_NAME, newName);
							loc.put(LocationDBAdapter.KEY_LOC_ADD, newAdd);
							loc.put(LocationDBAdapter.KEY_LOC_NOTES, newNotes);
							loc.saveInBackground();
							Log.d("editing in parse", "saved");
						}
						else{
							Log.d("Parse EditLocation", e.getMessage());
						}
					}
					
				
					});
			}
			finish();
		}
	}
	
	/**
	 * @method send sends information to OSM.
	 */
	public void send(){
		File sdCard = Environment.getExternalStorageDirectory();
		File directory = new File (sdCard.getAbsolutePath() + "/ATestGPX/");
		directory.mkdirs();
		String fileName = locName + "_" + System.currentTimeMillis()+".gpx";
		// unique filename based on the time
		File outputFile = new File(directory, fileName);
		String gpxXML = "";
		try {
			//this is the .gpx format being made.
			FileWriter outputFileWriter = new FileWriter(
			        outputFile);
			BufferedWriter buffWrite = new BufferedWriter(
			        outputFileWriter);
			String part1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			        + "\n"
			        + "<gpx version=\"1.1\" creator=\"OSMTrack\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.topografix.com/GPX/1/1\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">"
			        + "\n" 
			        + "\t<trk>" 
			        + "\n"
					+ "\t\t<trkseg>";
			buffWrite.write(part1);
			pathCursor.moveToFirst();
			String part2 = "";
			do {
				double thisLat = pathCursor.getDouble(pathCursor.getColumnIndexOrThrow(LocationDBAdapter.KEY_LAT));
				double thisLong = pathCursor.getDouble(pathCursor.getColumnIndexOrThrow(LocationDBAdapter.KEY_LONG));
				String thisTime = pathCursor.getString(pathCursor.getColumnIndexOrThrow(LocationDBAdapter.KEY_TIME));
				String sThisLat = String.format("%.7f", thisLat);
				String sThisLong = String.format("%.7f", thisLong);
				String tempPart = "\n"
			    		+ "\t\t\t<trkpt lat=\""
			    		+ sThisLat
			    		+ "\" lon=\""
			    		+ sThisLong
			    		+ "\">"
			    		+ "\n"
			    		+ "\t\t\t\t<ele>0.0</ele>"
			    		+ "\n"
			    		+ "\t\t\t\t<time>"
			    		+ thisTime
			    		+ "</time>"
			    		+ "\n"
			    		+ "\t\t\t</trkpt>";
			    buffWrite.write(tempPart);
			    part2 += tempPart;
			} while(pathCursor.moveToNext());
			String part3 = "\n"
		    		+ "\t\t</trkseg>"
		    		+ "\n"
		    		+ "\t</trk>"
		    		+ "\n"
		    		+ "</gpx>";
			buffWrite.write(part3);
			buffWrite.close();
			gpxXML = part1 + part2 + part3;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.d("GPXSaved", "gpx file was saved");
		gpxFile = new File(directory, fileName);
		Log.d("GPXFound", "gpx file was found");
		
		createGPX.execute();
		
	}
	
	/**
	 * @method encodeBase64 readies credentials for authorization
	 * @param s
	 * @return
	 */
	public static String encodeBase64(String s) {
	      StringBuilder out = new StringBuilder();
	      for (int i = 0; i < (s.length()+2)/3; ++i) {
	        int l = Math.min(3, s.length()-i*3);
	        String buf = s.substring(i*3, i*3+l);
	        out.append(BASE64_ENC.charAt(buf.charAt(0)>>2));
	        out.append(BASE64_ENC.charAt((buf.charAt(0) & 0x03) << 4 | (l==1?0:(buf.charAt(1) & 0xf0) >> 4)));
	        out.append(l>1 ? BASE64_ENC.charAt((buf.charAt(1) & 0x0f) << 2 | (l==2 ? 0 : (buf.charAt(2) & 0xc0) >> 6)) : '=');
	        out.append(l>2 ? BASE64_ENC.charAt(buf.charAt(2) & 0x3f) : '=');
	      }
	      return out.toString();
	}
	
	/**
	 * @method formDataFile writes .gpx file into outputstream in multipart-form data format
     * @param out
     * @param string
     * @param gpxFile
     * @throws IOException 
     */
    private void formDataFile(DataOutputStream out, String name, File gpxFile) throws IOException {
        out.writeBytes("--" + BOUNDARY + LINE_END);
        out.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + gpxFile.getName() + "\"" + LINE_END);
        out.writeBytes("Content-Type: application/octet-stream" + LINE_END);
        out.writeBytes(LINE_END);
        
        byte[] buffer = new byte[BUFFER_SIZE];
        int read;
        int sumread = 0;
        InputStream in = new BufferedInputStream(new FileInputStream(gpxFile));
        System.err.println("Transferring data to server");
        while((read = in.read(buffer)) >= 0) {
            out.write(buffer, 0, read);
            out.flush();
            sumread += read;
        }
        in.close();        
        out.writeBytes(LINE_END);
    }

    /**
     * @method formDataParams writes information to outputstream in multipart-form data format.
     * @param string
     * @param urlDesc
     * @throws IOException 
     */
    public void formDataParams(DataOutputStream out, String name, String value) throws IOException {
        out.writeBytes("--" + BOUNDARY + LINE_END);
        out.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"" + LINE_END);
        out.writeBytes(LINE_END);
        out.writeBytes(value + LINE_END);
    }
	
    /**
     * @class SaveGPX sends .gpx file to OSM
     * @author Kho, Purswani, Ramos, Tiongson
     *
     */
	class SaveGPX extends AsyncTask<String, String, Integer>{
		
		@Override
		protected void onPreExecute(){
			super.onPreExecute();
			
			loadingGPX = new ProgressDialog(EditLocation.this);
			loadingGPX.setMessage("Sending...");
			loadingGPX.setIndeterminate(false);
			loadingGPX.setCancelable(true);
			loadingGPX.show();
		}

		@Override
		protected Integer doInBackground(String... arg0) {
			try {
				URL url = new URL("http://www.openstreetmap.org/api/0.6/gpx/create");
				HttpURLConnection con = (HttpURLConnection) url.openConnection();
	            con.setRequestMethod("POST");
	            con.setConnectTimeout(15000);
	            con.setDoOutput(true);
	            Log.d("Http1", "http instantiated");
	            String credentials = osmUser + ":" + osmPass;
	            //there is something wrong with using String's built in encodeBase64 method
	            //that doesnt allow this file to be sent to OSM.
	            String encodedCredentials = encodeBase64(credentials);
	            System.out.println(credentials + "\n" + encodedCredentials);
	            con.addRequestProperty("Authorization", "Basic "+ encodedCredentials);
	            con.addRequestProperty("Content-Type", "multipart/form-data; boundary="+BOUNDARY);
	            con.addRequestProperty("Connection", "close");
	            con.addRequestProperty("Expect", "");
	            Log.d("HttpPost", "headers set");
	            
	            con.connect();
	            Log.d("HttpPost", "con nect");
	            
	            DataOutputStream out  = new DataOutputStream(new BufferedOutputStream(con.getOutputStream()));
	            Log.d("HttpPost", "outputstream");

	            formDataFile(out, "file", gpxFile);
	            Log.d("HttpPost", "gpx file");
	            formDataParams(out, "description", locName);
	            formDataParams(out, "tags", country);
	            formDataParams(out, "public", "1");
	            formDataParams(out, "visibility", "identifiable");
	            Log.d("HttpPost", "entity set");
	            
	            out.writeBytes("--" + BOUNDARY + "--" + LINE_END);
	            out.flush();
	            Log.d("HttpPost", "flush");
	            
	            int retCode = con.getResponseCode();
	            String sRetCode = Integer.toString(retCode);
	            Log.d("ret", sRetCode);
	            String retMsg = con.getResponseMessage();
	            Log.d("ret", retMsg);
	            
	            out.close();
	            con.disconnect();
	            
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


			// TODO Auto-generated method stub
			return null;
		}
		
		protected void onPostExecute(Integer i){
			loadingGPX.dismiss();
			
		}
		
	}
}
