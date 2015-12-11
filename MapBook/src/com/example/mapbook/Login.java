package com.example.mapbook;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * @class Login
 * @author Kho, Purswani, Ramos, Tiongson
 *
 */
public class Login extends Activity {
	
	private Button login;
	private Button loginOffline;
	private Button register;
	//private TextView logo;
	
	//final strings for use on MainActivity class as well
	public static final String USERNAME = "USERNAME";
	public static final String COUNTRY = "COUNTRY";
	public static final String CITY = "CITY";
	public static final String FIRSTNAME = "FIRSTAME";
	public static final String LASTNAME = "LASTNAME";
	public static final String OSMUSER = "OSMUSER";
	public static final String OSMPASS = "OSMPASS";
	public static final String ID = "ID";
	
	Context context = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		//Pacifico is crashing the app. fix me
		//logo = (TextView) findViewById(R.id.loginlogo);
		//Typeface pacifico = Typeface.createFromAsset(getAssets(), "Pacifico.TTF");
		//logo.setTypeface(pacifico);
		
		login = (Button) findViewById(R.id.login);
		login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	//check if there is an internet connection. Otherwise, don't use login() method.
            	ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
				if(activeNetworkInfo != null && activeNetworkInfo.isConnected()){
					login();
				}
				else{
					Toast toast = Toast.makeText(context, "Please connect to the internet!", Toast.LENGTH_SHORT);
					toast.show();
				}
            }
        });
		
		loginOffline = (Button) findViewById(R.id.loginOffline);
		loginOffline.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	//use app with no account. No internet needed.
            	useAppWithNoAccount();
            }
        });
		
		register = (Button) findViewById(R.id.register);
		register.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	//checks if there is an internet connection. Otherwise, don't use register() method.
            	ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
				if(activeNetworkInfo != null && activeNetworkInfo.isConnected()){
					register();
				}
				else{
					Toast toast = Toast.makeText(context, "Please connect to the internet!", Toast.LENGTH_SHORT);
					toast.show();
				}
            }
        });
	}
	
	/**
	 * @method register go to register screen
	 */
	public void register(){
		Intent intent = new Intent(this, Signup.class);
		startActivity(intent);
		
	}
	
	/**
	 * @method useAppWithNoAccount use the app without logging in. No Parse connectivity
	 */
	public void useAppWithNoAccount(){
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}
	
	/**
	 * @method login Logs into the app using Parse
	 */
	public void login(){
		//get the credentials
		EditText user = (EditText) findViewById(R.id.editLoginUser);
		EditText pass = (EditText) findViewById(R.id.editLoginPass);
		
		String sUser = user.getText().toString();
		String sPass = pass.getText().toString();	
		
		//disable the buttons
		loading();
		
		ParseUser.logInInBackground(sUser, 
				sPass, 
				new LogInCallback(){
			public void done(ParseUser user, ParseException e) {
			    if (user != null) 
			    {
			      //send the user's information if successful log in
			    	Intent intent = new Intent(getApplicationContext(), MainActivity.class);
			    	String city = user.getString("city");
			    	String country = user.getString("country");
			    	String osmuser = user.getString("osmuser");
			    	String osmpass = user.getString("osmpass");
			    	String firstName = user.getString("firstName");
			    	String lastName = user.getString("lastName");
			    	String username = user.getString("username");
			    	String objectId = user.getObjectId();
			    	Log.d("login", osmuser);
			    	Log.d("login", osmpass);
			    	intent.putExtra(CITY, city);
			    	intent.putExtra(COUNTRY, country);
			    	intent.putExtra(OSMUSER, osmuser);
			    	intent.putExtra(OSMPASS, osmpass);
			    	intent.putExtra(FIRSTNAME, firstName);
			    	intent.putExtra(LASTNAME, lastName);
			    	intent.putExtra(USERNAME, username);
			    	intent.putExtra(ID, objectId);
					startActivity(intent);
					
					finish();
			    	
			    } 
			    else 
			    {
			      // toast the parse exception if logging in failed.
			    	e.printStackTrace();
			    	Toast.makeText(getApplicationContext(), "Unable to sign in: "+e.getMessage(), Toast.LENGTH_LONG).show();

			    }
			    
			    
			    // reenable the buttons
			    doneLoading();
			    
			  }
			});	
		
		
		
		
	}
	
	/**
	 * @method loading disables the buttons
	 */
	public void loading(){
		login.setEnabled(false);
		loginOffline.setEnabled(false);
		register.setEnabled(false);
	}
	
	/**
	 * @method doneLoading reenables buttons
	 */
	public void doneLoading(){
		login.setEnabled(true);
		loginOffline.setEnabled(true);
		register.setEnabled(true);
	}
	
}
