package com.example.mapbook;

public class Location {
	
	int id;
	String locName;
	String locAdd;
	String locImg;
	String locNotes;
	int idUser;
	int idPath;
	
	public Location(){
		
	}
	
	public Location(int id, String locName, String locAdd, String locImg, String locNotes,
			int idUser, int idPath){
		this.id = id;
		this.locName = locName;
		this.locAdd = locAdd;
		this.locImg = locImg;
		this.locNotes = locNotes;
		this.idUser = idUser;
		this.idPath = idPath;
	}
	
	public Location(String locName, String locAdd, String locImg, String locNotes,
			int idUser, int idPath){
		this.locName = locName;
		this.locAdd = locAdd;
		this.locImg = locImg;
		this.locNotes = locNotes;
		this.idUser = idUser;
		this.idPath = idPath;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getLocName() {
		return locName;
	}
	public void setLocName(String locName) {
		this.locName = locName;
	}
	public String getLocAdd() {
		return locAdd;
	}
	public void setLocAdd(String locAdd) {
		this.locAdd = locAdd;
	}
	public String getLocImg() {
		return locImg;
	}
	public void setLocImg(String locImg) {
		this.locImg = locImg;
	}
	public int getIdUser() {
		return idUser;
	}

	public void setIdUser(int idUser) {
		this.idUser = idUser;
	}

	public int getIdPath() {
		return idPath;
	}

	public void setIdPath(int idPath) {
		this.idPath = idPath;
	}
	public String getLocNotes() {
		return locNotes;
	}
	public void setLocNotes(String locNotes) {
		this.locNotes = locNotes;
	}

}
