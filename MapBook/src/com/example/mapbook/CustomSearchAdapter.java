package com.example.mapbook;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

public class CustomSearchAdapter extends ParseQueryAdapter<ParseObject> {

	public CustomSearchAdapter(Context context, final String filter, final String searchText) {
		// Use the QueryFactory to construct a PQA that will only show
		// Todos marked as high-pri
		super(context, new ParseQueryAdapter.QueryFactory<ParseObject>() {
			public ParseQuery create() {
				if(filter.equals("address")){
					ParseQuery<ParseObject> queryPoints = ParseQuery
							.getQuery(LocationDBAdapter.TABLE_LOC);
					queryPoints.whereContains("address", searchText);
					return queryPoints;
				}
				else{
					ParseQuery<ParseObject> queryPoints = ParseQuery
							.getQuery(LocationDBAdapter.TABLE_LOC);
					queryPoints.whereContains("locname", searchText);
					return queryPoints;
				}
			}
		});
	}

	// Customize the layout by overriding getItemView
	@Override
	public View getItemView(ParseObject object, View v, ViewGroup parent) {
		if (v == null) {
			v = View.inflate(getContext(), R.layout.searchrowlayout, null);
		}

		super.getItemView(object, v, parent);

		// Add the locName view
		TextView locNameView = (TextView) v.findViewById(R.id.loctextsearch);
		locNameView.setText(object.getString("locname"));
		
		// Add the address view
		TextView addressView = (TextView) v.findViewById(R.id.addtextsearch);
		addressView.setText(object.getString("address"));
		
		// Add the user view
		TextView userView = (TextView) v.findViewById(R.id.usertextsearch);
		userView.setText(object.getString("iduser"));
		return v;
	}

}