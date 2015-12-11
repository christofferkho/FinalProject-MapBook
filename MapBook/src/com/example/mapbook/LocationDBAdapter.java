package com.example.mapbook;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @class LocationDBAdapter
 * @author Kho, Purswani, Ramos, Tiongson
 * allows connectivity with SQLite
 *
 */
public class LocationDBAdapter {
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	private static final int DATABASE_VERSION = 1;

	private final Context mCtx;

	private static final String DATABASE_NAME = "CS123A";
		
	//Table names
	public static final String TABLE_LOC = "Locations";
	public static final String TABLE_PATH_LIST = "Coordinates";
	
	//Primary key
	public static final String KEY_ROWID = "_id";
	
	//Parse primary key for user
	public static final String KEY_USER = "iduser";
	
	//Parse primary key for location and coordinates
	public static final String KEY_OBJID = "objectId";

	//Columns for location table
	public static final String KEY_LOC_NAME = "locname";
	public static final String KEY_LOC_ADD = "address";
	public static final String KEY_LOC_IMG = "image";
	public static final String KEY_LOC_NOTES = "notes";
	
	//Columns for path list table
	public static final String KEY_LOCATION_ID = "idlocation";
	public static final String KEY_LOCATION_OBJID = "locObjectId";
	public static final String KEY_LAT = "latitude";
	public static final String KEY_LONG = "longitude";
	public static final String KEY_TIME = "time";

	//create table queries
	private static final String CREATE_TABLE_LOC = 
			
			"CREATE TABLE if not exists " + TABLE_LOC + " ("  + KEY_ROWID + " integer PRIMARY KEY autoincrement," 
					+ KEY_LOC_NAME + "," + KEY_LOC_ADD + ","+ KEY_LOC_IMG + "," 
					+ KEY_USER + "," + KEY_LOC_NOTES + "," + KEY_OBJID + ");";
	
	private static final String CREATE_TABLE_PATH_LIST = 

			"CREATE TABLE if not exists " + TABLE_PATH_LIST + " ("  + KEY_ROWID + " integer PRIMARY KEY autoincrement," 
					+ KEY_LOCATION_ID + "," + KEY_LONG + "," + KEY_LAT + "," + KEY_TIME + "," + KEY_USER + ","
					+ KEY_LOCATION_OBJID + "," + KEY_OBJID + ");";

	
	/**
	 * 
	 * @class DatabaseHelper
	 *
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		//create the database with corresponding tables
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_TABLE_LOC);
			db.execSQL(CREATE_TABLE_PATH_LIST);
		}

		//database changing
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOC);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_PATH_LIST);
			onCreate(db);
		}
	}
	
	
	/**
	 * @constructor LocationDBAdapter
	 * @param ctx
	 */
	public LocationDBAdapter(Context ctx) {
		this.mCtx = ctx;
	}

	/**
	 * @method open allows database connectivity
	 * @return 
	 * @throws SQLException
	 */
	public LocationDBAdapter open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		
		
		return this;
	}

	/**
	 * @method close ceases database connectivity
	 */
	public void close() {
		if (mDbHelper != null) {
			mDbHelper.close();
		}
	}
	
	// ACTIONS FOR LOCATION:
	
	/**
	 * @method createLoc creates a location
	 * @param locName
	 * @param locadd
	 * @param img
	 * @param user
	 * @param notes
	 * @return primary key of newly created location
	 */
	public long createLoc(String locName, String locadd, String img, String user, String notes, String objId) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_LOC_NAME, locName);
		initialValues.put(KEY_LOC_ADD, locadd);
		initialValues.put(KEY_LOC_IMG, img);
		initialValues.put(KEY_USER, user);
		initialValues.put(KEY_LOC_NOTES, notes);
		initialValues.put(KEY_OBJID, objId);
		
		return mDb.insert(TABLE_LOC, null, initialValues);
	}
	
	/**
	 * @method udpateLoc updates information of location
	 * @param id
	 * @param locName
	 * @param locadd
	 * @param img
	 * @param user
	 * @param notes
	 * @param objId
	 * @return
	 */
	public long updateLoc(String id, String locName, String locadd, String img, String user, String notes, String objId) {
		ContentValues newValues = new ContentValues();
		newValues.put(KEY_LOC_NAME, locName);
		newValues.put(KEY_LOC_ADD, locadd);
		newValues.put(KEY_LOC_IMG, img);
		newValues.put(KEY_USER, user);
		newValues.put(KEY_LOC_NOTES, notes);
		newValues.put(KEY_OBJID, objId);
		
		return mDb.update(TABLE_LOC, newValues, "_id="+ id, null);
	}

	
	/**
	 * @method deleteAllInfo for locations
	 * @return true if there are rows deleted
	 */
	//deleting paths (and thus GPS coordinates) with locations NOT YET IMPLEMENTED!!!
	public boolean deleteAllInfo() {
		int doneDelete = 0;
		doneDelete = mDb.delete(TABLE_LOC, null, null);
		mDb.delete(TABLE_PATH_LIST, null, null);
		return doneDelete > 0;
	}
	
	/**
	 * @method editLocation edits only user inputed location information
	 * @param locName
	 * @param locAdd
	 * @param notes
	 * @param id
	 * @return number of rows affected
	 */
	public int editLocation(String locName, String locAdd, String notes, String id){
		ContentValues newValues = new ContentValues();
		newValues.put(KEY_LOC_NAME, locName);
		newValues.put(KEY_LOC_ADD, locAdd);
		newValues.put(KEY_LOC_NOTES, notes);
		
		return mDb.update(TABLE_LOC, newValues, "_id="+ id, null);
	}
	
	/**
	 * @method editObjIdLoc changes a locations objectId.
	 * @param objId
	 * @param id
	 * @return number of rows affected
	 */
	public int editLocObjId(String objId, String id){
		ContentValues newValues = new ContentValues();
		newValues.put(KEY_OBJID, objId);
		
		return mDb.update(TABLE_LOC, newValues, "_id="+ id, null);
	}
	
	/**
	 * @method editUser. used when dedicating information from merely local to a specific user
	 * @param user
	 * @param id
	 * @return
	 */
	public int editLocUser(String user, String id){
		ContentValues newValues = new ContentValues();
		newValues.put(KEY_USER, user);
		
		return mDb.update(TABLE_LOC, newValues, "_id="+ id, null);
	}
	
	/**
	 * @method fetchInfoById
	 * @param tableName
	 * @param inputText
	 * @return
	 * @throws SQLException
	 */
	public Cursor fetchLocById(String tableName, String inputText) throws SQLException {
		Cursor mCursor = null;
		if (inputText == null || inputText.length() == 0) {
			
			mCursor = mDb.query(TABLE_LOC, 
								new String[] { KEY_ROWID,
					
											   // CHANGE COLUMNS -- May be all or just a few
					KEY_LOC_NAME, KEY_LOC_ADD, KEY_LOC_IMG, KEY_USER, KEY_LOC_NOTES, KEY_OBJID}, 
											   
								null, null, null, null, null);

		} else {
			mCursor = mDb.query(true, 
								TABLE_LOC, 
								new String[] { KEY_ROWID,
					
											   // CHANGE COLUMNS -- May be all or just a few					
					KEY_LOC_NAME, KEY_LOC_ADD, KEY_LOC_IMG, KEY_USER, KEY_LOC_NOTES, KEY_OBJID},
											   
								// searching similar			   
								KEY_ROWID + " like '%" + inputText+ "%'",
								
								// DO NOT CHANGE THIS
								null, null, null, null, null);
		}
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;

	}

	/**
	 * @method fetchLocByLocName
	 * @param tableName
	 * @param inputText
	 * @return
	 * @throws SQLException
	 */
	public Cursor fetchLocByLocName(String tableName, String inputText) throws SQLException {
		Cursor mCursor = null;
		if (inputText == null || inputText.length() == 0) {
			
			mCursor = mDb.query(TABLE_LOC, 
								new String[] { KEY_ROWID,
					
											   // CHANGE COLUMNS -- May be all or just a few
					KEY_LOC_NAME, KEY_LOC_ADD, KEY_LOC_IMG, KEY_USER, KEY_LOC_NOTES, KEY_OBJID}, 
											   
								null, null, null, null, null);

		} else {
			mCursor = mDb.query(true, 
								TABLE_LOC, 
								new String[] { KEY_ROWID,
					
											   // CHANGE COLUMNS -- May be all or just a few					
					KEY_LOC_NAME, KEY_LOC_ADD, KEY_LOC_IMG, KEY_USER, KEY_LOC_NOTES, KEY_OBJID},
											   
								// searching similar			   
								KEY_LOC_NAME + " like '%" + inputText+ "%'",
								
								// DO NOT CHANGE THIS
								null, null, null, null, null);
		}
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;

	}
	
	/**
	 * @method fetchInfoByUser
	 * @param tableName
	 * @param inputText
	 * @return
	 * @throws SQLException
	 */
	public Cursor fetchLocByUser(String tableName, String inputText) throws SQLException {
		Cursor mCursor = null;
		if (inputText == null || inputText.length() == 0) {
			
			mCursor = mDb.query(TABLE_LOC, 
								new String[] { KEY_ROWID,
					
											   // CHANGE COLUMNS -- May be all or just a few
					KEY_LOC_NAME, KEY_LOC_ADD,KEY_LOC_IMG, KEY_USER, KEY_LOC_NOTES, KEY_OBJID}, 
											   
								null, null, null, null, null);

		} else {
			mCursor = mDb.query(true, 
								TABLE_LOC, 
								new String[] { KEY_ROWID,
					
											   // CHANGE COLUMNS -- May be all or just a few					
					KEY_LOC_NAME, KEY_LOC_ADD,KEY_LOC_IMG, KEY_USER, KEY_LOC_NOTES, KEY_OBJID},
											   
								// searching similar			   
								KEY_USER + " like '%" + inputText+ "%'",
								
								// DO NOT CHANGE THIS
								null, null, null, null, null);
		}
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;

	}
	
	/**
	 * @method fetchInfoByObjId
	 * @param tableName
	 * @param inputText
	 * @return
	 * @throws SQLException
	 */
	public Cursor fetchLocByObjId(String tableName, String inputText) throws SQLException {
		Cursor mCursor = null;
		if (inputText == null || inputText.length() == 0) {
			
			mCursor = mDb.query(TABLE_LOC, 
								new String[] { KEY_ROWID,
					
											   // CHANGE COLUMNS -- May be all or just a few
					KEY_LOC_NAME, KEY_LOC_ADD,KEY_LOC_IMG, KEY_USER, KEY_LOC_NOTES, KEY_OBJID}, 
											   
								null, null, null, null, null);

		} else {
			mCursor = mDb.query(true, 
								TABLE_LOC, 
								new String[] { KEY_ROWID,
					
											   // CHANGE COLUMNS -- May be all or just a few					
					KEY_LOC_NAME, KEY_LOC_ADD,KEY_LOC_IMG, KEY_USER, KEY_LOC_NOTES, KEY_OBJID},
											   
								// searching similar			   
								KEY_OBJID + " like '%" + inputText+ "%'",
								
								// DO NOT CHANGE THIS
								null, null, null, null, null);
		}
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;

	}

	/**
	 * @method fetchAllLoc get all locations
	 * @return mCursor
	 */
	public Cursor fetchAllLoc() {

		// parameter descriptions
		// mDb.query(table, columns, selection, selectionArgs, groupBy, having, orderBy)

		Cursor mCursor = mDb.query(TABLE_LOC, new String[] { KEY_ROWID,
				KEY_LOC_NAME, KEY_LOC_ADD,KEY_LOC_IMG, KEY_USER, KEY_LOC_NOTES, KEY_OBJID},
				null, null, null, null, null);

		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
	//ACTIONS FOR Coordinates
	/**
	 * @method createPath creates coordinates with the same location primary key, indicating a path
	 * @param id
	 * @param latitude
	 * @param longitude
	 * @return primary key
	 */
	public long createPath(long id, double latitude, double longitude, String time, String user, String locObjId, String objId) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_LOCATION_ID, id);
		initialValues.put(KEY_LAT, latitude);
		initialValues.put(KEY_LONG, longitude);
		initialValues.put(KEY_TIME, time);
		initialValues.put(KEY_USER, user);
		initialValues.put(KEY_LOCATION_OBJID, locObjId);
		initialValues.put(KEY_OBJID, objId);
		return mDb.insert(TABLE_PATH_LIST, null, initialValues);
	}
	
	/**
	 * @method fetchPathByLocId finds paths with a specific location primary key
	 * @param inputText
	 * @return mCursor
	 * @throws SQLException
	 */
	public Cursor fetchPathByLocId(String inputText) throws SQLException {
		Cursor mCursor = null;
		if (inputText == null || inputText.length() == 0) {
			
			mCursor = mDb.query(TABLE_PATH_LIST, 
								new String[] { KEY_ROWID, KEY_LOCATION_ID, KEY_LONG, KEY_LAT, KEY_TIME, KEY_USER, KEY_LOCATION_OBJID, KEY_OBJID}, 
								null, null, null, null, null);

		} else {
			mCursor = mDb.query(true, 
								TABLE_PATH_LIST, 
								new String[] { KEY_ROWID, KEY_LOCATION_ID, KEY_LONG, KEY_LAT, KEY_TIME, KEY_USER, KEY_LOCATION_OBJID, KEY_OBJID},
								//search for similar
								KEY_LOCATION_ID + " like '%" + inputText+ "%'",
								null, null, null, null, null);
		}
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;

	}
	
	/**
	 * @method fetchPathByObjId
	 * @param inputText
	 * @return
	 * @throws SQLException
	 */
	public Cursor fetchPathByObjId(String inputText) throws SQLException {
		Cursor mCursor = null;
		if (inputText == null || inputText.length() == 0) {
			
			mCursor = mDb.query(TABLE_PATH_LIST, 
								new String[] { KEY_ROWID, KEY_LOCATION_ID, KEY_LONG, KEY_LAT, KEY_TIME, KEY_USER, KEY_LOCATION_OBJID, KEY_OBJID}, 
								null, null, null, null, null);

		} else {
			mCursor = mDb.query(true, 
								TABLE_PATH_LIST, 
								new String[] { KEY_ROWID, KEY_LOCATION_ID, KEY_LONG, KEY_LAT, KEY_TIME, KEY_USER, KEY_LOCATION_OBJID, KEY_OBJID},
								//search for similar
								KEY_OBJID + " like '%" + inputText+ "%'",
								null, null, null, null, null);
		}
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;

	}
	
	/**
	 * @method fetchPathByLocObjId fetch a path by a location's object Id
	 * @param inputText
	 * @return cursor containing paths with specified locObjId
	 * @throws SQLException
	 */
	public Cursor fetchPathByLocObjId(String inputText) throws SQLException {
		Cursor mCursor = null;
		if (inputText == null || inputText.length() == 0) {
			
			mCursor = mDb.query(TABLE_PATH_LIST, 
								new String[] { KEY_ROWID, KEY_LOCATION_ID, KEY_LONG, KEY_LAT, KEY_TIME, KEY_USER, KEY_LOCATION_OBJID, KEY_OBJID}, 
								null, null, null, null, null);

		} else {
			mCursor = mDb.query(true, 
								TABLE_PATH_LIST, 
								new String[] { KEY_ROWID, KEY_LOCATION_ID, KEY_LONG, KEY_LAT, KEY_TIME, KEY_USER, KEY_LOCATION_OBJID, KEY_OBJID},
								//search for similar
								KEY_LOCATION_ID + " like '%" + inputText+ "%'",
								null, null, null, null, null);
		}
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;

	}
	
	/**
	 * @method editPathLocId. used when getting locations/paths from Parse
	 * @param locId
	 * @param id
	 * @return
	 */
	public int editPathLocId(String locId, String id){
		ContentValues newValues = new ContentValues();
		newValues.put(KEY_LOCATION_ID, locId);
		
		return mDb.update(TABLE_PATH_LIST, newValues, "_id="+ id, null);
	}
	
	/**
	 * @method editPathObjId used when getting locations/paths from Parse
	 * @param objId
	 * @param id
	 * @return number of rows affected
	 */
	public int editPathObjId(String objId, String id){
		ContentValues newValues = new ContentValues();
		newValues.put(KEY_OBJID, objId);
		
		return mDb.update(TABLE_PATH_LIST, newValues, "_id="+ id, null);
	}
	
	/**
	 * @method editPathLocObjId used when getting locations/paths from Parse
	 * @param locObjId
	 * @param id
	 * @return number of rows affected
	 */
	public int editPathLocObjId(String locObjId, String id){
		ContentValues newValues = new ContentValues();
		newValues.put(KEY_LOCATION_OBJID, locObjId);
		
		return mDb.update(TABLE_PATH_LIST, newValues, "_id="+ id, null);
	}

}
