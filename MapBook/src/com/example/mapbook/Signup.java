package com.example.mapbook;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * @class Signup
 * @author Kho, Purswani, Ramos, Tiongson
 *
 */
public class Signup extends Activity {
	
	private Button save;
	private Button cancel;
	
	String TAG_SUCCESS = "success";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signup);
		
		save = (Button) findViewById(R.id.saveUser);
        save.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) 
			{
				save();
			}
		});
        
        cancel = (Button) findViewById(R.id.cancelUser);
        cancel.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) 
			{
				finish();			
			}
		});
		
	}
	
	/**
	 * @method save sends inputed information to Parse
	 */
	public void save(){
		
		//get user information
		EditText username = (EditText) findViewById(R.id.editUsername);
		EditText password = (EditText) findViewById(R.id.editPassword);
		EditText osmUser = (EditText) findViewById(R.id.editOSMUser);
		EditText osmPass = (EditText) findViewById(R.id.editOSMPass);
		EditText firstName = (EditText) findViewById(R.id.editFirstName);
		EditText lastName = (EditText) findViewById(R.id.editLastName);
		EditText country = (EditText) findViewById(R.id.editCountry);
		EditText city = (EditText) findViewById(R.id.editCity);
		
		String sUsername = username.getText().toString();
		String sPassword = password.getText().toString();
		String sOSMUser = osmUser.getText().toString();
		String sOSMPass = osmPass.getText().toString();
		String sFirstName = firstName.getText().toString();
		String sLastName = lastName.getText().toString();
		String sCountry = country.getText().toString();
		String sCity = city.getText().toString();
		
		//put them in a new ParseUser
		ParseUser user = new ParseUser();
        user.setUsername(sUsername);
        user.setPassword(sPassword);
        user.put("osmuser", sOSMUser);
        user.put("osmpass", sOSMPass);
        user.put("firstName", sFirstName);
        user.put("lastName", sLastName);
        user.put("country", sCountry);
        user.put("city", sCity);
        user.signUpInBackground(new SignUpCallback() {
			
			@Override
			public void done(ParseException e) 
			{
				if (e==null)
				{
					//success
					Toast.makeText(getApplicationContext(), "Registration successful.  You can now log in", Toast.LENGTH_LONG).show();
			    	finish();
				}						
				else
				{
					//failed
					e.printStackTrace();
			    	Toast.makeText(getApplicationContext(), "Unable to register: "+e.getMessage(), Toast.LENGTH_LONG).show();
				}
				doneLoading();
			}
		});	
        
	}
	
	public void loading(){
		View progress = findViewById(R.id.progressBarRegister);
		
		save.setEnabled(false);
		cancel.setEnabled(false);
		progress.setVisibility(View.VISIBLE);
	}
	
	public void doneLoading(){
		View progress = findViewById(R.id.progressBarRegister);
		
		save.setEnabled(true);
		cancel.setEnabled(true);
		progress.setVisibility(View.INVISIBLE);
	}
	
}