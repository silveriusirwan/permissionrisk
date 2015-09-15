package com.carlocriniti.android.permission_explorer;

import java.util.HashMap;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class IgnoreList extends Activity{
	
	static LinearLayout ll;
	static HashMap<Integer, String> checkedIgnoreList;
	
	private ProgressDialog loading;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ignore_list);
		
		ll = (LinearLayout) findViewById(R.id.layout_ignore);
		checkedIgnoreList = new HashMap<Integer, String>();
		createIgnoredList(1);
	}
	
	public void createIgnoredList(int ignoredCode){
		int checkBoxId = 1000;
    	for(int i = 0; i<11;i++){
    		String query = "SELECT appName, marketSource FROM ignore_list WHERE isIgnore = " + ignoredCode + " AND categoryCode = "+ i+" ORDER BY appName asc";
        	Cursor ignoredListCursor = Tools.database.database.rawQuery(query, null);
        	startManagingCursor(ignoredListCursor);
        	ignoredListCursor.moveToFirst(); 
    		
    		String sencat;
	    	switch(i) {
			    case 0:
			        sencat = "Account Information Category :";
			        break;
			    case 1:
			    	sencat = "Browser History and Bookmarks Category :";
			        break;
			    case 2:
			    	sencat = "Calendar Information Category :";
			        break;
			    case 3:
			    	sencat = "Calling Information Category :";
			        break;
			    case 4:
			    	sencat = "Contacts and Profile Category :";
			        break;
			    case 5:
			    	sencat = "Location Category :";
			        break;
			    case 6:
			    	sencat = "Media Category :";
			        break;
			    case 7:
			    	sencat = "Messages Category :";
			        break;
			    case 8:
			    	sencat = "Network Information Category :";
			        break;
			    case 9:
			    	sencat = "Phone Information Category :";
			        break;
			    case 10:
			    	sencat = "External Storage Data Risk";
			        break;
			    default:
			        sencat = "category?";
				}
	    	
		if (ignoredListCursor.getCount()!=0){
			TextView tv = new TextView(this);
			tv.setText(" \n"+sencat);
			ll.addView(tv);
			
    		while(!ignoredListCursor.isAfterLast()) {
        		CheckBox cb = new CheckBox(this);
        		String appName = ignoredListCursor.getString(ignoredListCursor.getColumnIndex("appName"));
        		String marketSource = ignoredListCursor.getString(ignoredListCursor.getColumnIndex("marketSource"));
                cb.setText(appName+" > "+ marketSource);
               // cb.setPadding(0, 0, 0, 0);
                cb.setId(checkBoxId);
                cb.setTag(appName);
                checkBoxId++;
                cb.setScaleX(0.80f);
                cb.setScaleY(0.80f);
                cb.setOnCheckedChangeListener(handleCheck(cb));
                ll.addView(cb);
                ignoredListCursor.moveToNext();
        	}
    		checkBoxId = (int) (Math.floor(checkBoxId/1000)+1)*1000;
    		
    		Button btnSubmit = new Button(this);
    		btnSubmit.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
    		btnSubmit.setText("Remove from Exception List");
    		btnSubmit.setId(i);
    		btnSubmit.setOnClickListener(updateIgnoreList(checkedIgnoreList));
            ll.addView(btnSubmit);
    	} else {
			checkBoxId = (int) (Math.floor(checkBoxId/1000)+1)*1000;
		}
        	
    	}
	}
	
	private OnCheckedChangeListener handleCheck (final CheckBox chk)
    {
        return new OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
            	int checkBoxItem = chk.getId();
                if(!isChecked){                           
                	Object value = checkedIgnoreList.get(checkBoxItem);
                	if (value != null) {
                		checkedIgnoreList.remove(checkBoxItem);
                	}
                }
                else
                {   
                	checkedIgnoreList.put(checkBoxItem, chk.getText().toString());
                }
            }
        };
    }
	
	private OnClickListener updateIgnoreList(final HashMap<Integer, String> checkedIgnoreList){
    	return new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				SQLiteDatabase db;
				int theCategoryCode = v.getId();

		        db = openOrCreateDatabase(
		            "application_permission.db"
		            , SQLiteDatabase.CREATE_IF_NECESSARY
		            , null
		            );
		        
		        ContentValues data=new ContentValues();
		        
		        for(Integer checkBoxId: checkedIgnoreList.keySet()){
		    		if((firstDigit(checkBoxId)-1)==theCategoryCode){
		    			String appName = checkedIgnoreList.get(checkBoxId);
		    			String[] parts = appName.split(">");
		    			appName = parts[0].trim();
		    			data.put("isIgnore",0);
		    			String whereClause = "categoryCode = " + theCategoryCode + " AND appName = '" + appName +"'";
		    			db.update("ignore_list", data, whereClause, null);
		    		}
		        }
		        db.close();
		        //loading = ProgressDialog.show(IgnoreList.this, "","Loading...", true);
//		        startActivity(getIntent());
//		        finish();
		        finish();
		        Intent refresh = new Intent(IgnoreList.this, IgnoreList.class);
		        startActivity(refresh);
		         //
			}
    		
    	};
    
    }
	
	public int firstDigit(int x) {
    	if (x<10000){
    		return Integer.parseInt(Integer.toString(x).substring(0, 1));
    	} else {
    		return Integer.parseInt(Integer.toString(x).substring(0, 2));
    	}
	}
	
	@Override
	public void onBackPressed() {
	    // do something on back.
		loading = ProgressDialog.show(IgnoreList.this, "","Loading...", true);
		Intent myIntent = new Intent(IgnoreList.this, Main.class);	
		//myIntent.putExtra("key", value); //Optional parameters
		startActivity(myIntent);
		finish();
	    return;
	}
}
