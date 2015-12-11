package com.example.mapbook;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

/**
 * @class MapApplication
 * @author Kho, Purswani, Ramos, Tiongson
 *
 */
public class MapApplication extends Application 
{
	private String applicationId = "U8UDdnKO3NiBJaBVwpBhZCdCg0gzttlPcsrkfGKS";
	private String clientKey = "YP6wk00vCK27pRPHZocscOwUR20YJesaQ9QKCYZV";
	
	public void onCreate()
	{
		super.onCreate();
		
		//the User object allows for abstracted Parse connectivity.
		ParseObject.registerSubclass(User.class);
		
				
		Parse.initialize(this, applicationId, clientKey);
	}
	
}
