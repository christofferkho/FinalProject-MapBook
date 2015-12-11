package com.example.mapbook;

import com.parse.ParseClassName;
import com.parse.ParseObject;
/**
 * @class User
 * @author Kho, Purswani, Ramos, Tiongson
 *
 */
@ParseClassName("User")
public class User extends ParseObject {
	
	public String getObjectId() {
		return getString("objectId");
	}

	public String getUsername() {
		return getString("username");
	}

	/**
	 * @method setUsername when setting, we use put instead of replacing the actual string. This sets up
	 * Parse for an update regarding user information. This holds true for all setters.
	 * @param username
	 */
	public void setUsername(String username) {
		put("username", username);
	}

	public String getPassword() {
		return getString("password");
	}

	public void setPassword(String password) {
		put("password", password);
	}

	public String getOsmUser() {
		return getString("osmuser");
	}

	public void setOsmUser(String osmuser) {
		put("osmuser", osmuser);
	}

	public String getOsmPass() {
		return getString("osmpass");
	}

	public void setOsmPass(String osmpass) {
		put("osmpass", osmpass);
	}

	public String getFirstName() {
		return getString("firstName");
	}

	public void setFirstName(String firstName) {
		put("firstName", firstName);
	}

	public String getLastName() {
		return getString("lastName");
	}

	public void setLastName(String lastName) {
		put("lastName", lastName);
	}

	public String getCountry() {
		return getString("country");
	}

	public void setCountry(String country) {
		put("country", country);
	}

	public String getCity() {
		return getString("city");
	}

	public void setCity(String city) {
		put("city", city);
	}

}
