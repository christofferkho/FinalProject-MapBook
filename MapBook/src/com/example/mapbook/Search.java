package com.example.mapbook;

import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * @class Search
 * @author Kho, Purswani, Ramos, Tiongson
 *
 */
public class Search extends Activity {
	
	private Button searchButton;
	private RadioButton locNameButton;
	private RadioButton addressButton;
	private EditText editTextSearch;
	private CustomSearchAdapter searchAdapter;
	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		
		
		locNameButton = (RadioButton) findViewById(R.id.radioLocName);
		addressButton = (RadioButton) findViewById(R.id.radioAddress);
		editTextSearch = (EditText) findViewById(R.id.editTextSearch);
		
		context = this;
		
		searchButton = (Button) findViewById(R.id.search);
		searchButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	search();
            }
        });
	}
	
	/**
	 * @method search searches Parse for corresponding information
	 */
	public void search(){
		final String searchText = editTextSearch.getText().toString();
		ListView listView = (ListView) findViewById(R.id.searchListView);
		//set the listener for the listView
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> listView, View view,
					int position, long id) {
				ParseObject loc = searchAdapter.getItem(position);
				String locObjId = loc.getObjectId();
				Toast.makeText(getApplicationContext(), "Accessing location/path with ID: " 
				+ locObjId, Toast.LENGTH_LONG).show();
				//go to the SearchItem view
				Intent intent = new Intent(context, SearchItem.class);
				intent.putExtra("locObjId", locObjId);
				startActivity(intent);
			}
		});
		if(addressButton.isChecked()){
			//search Parse for addresses
			searchAdapter = new CustomSearchAdapter(this, "address", searchText);
			listView.setAdapter(searchAdapter);
			
			
		}
		else if(locNameButton.isChecked()){
			//search parse for location names
			searchAdapter = new CustomSearchAdapter(this, "locName", searchText);
			listView.setAdapter(searchAdapter);
		}
		else{
			//no filter is selected.
			Toast.makeText(getApplicationContext(), "Select what filter to search in!", Toast.LENGTH_SHORT).show();
		}
	}
}
