/*
 * Projet 	: Permission Explorer
 * Auteur 	: Carlo Criniti
 * Date   	: 2011.06.10
 * 
 * Classe ApplicationDetail
 * Activit� d'affichage du d�tail d'une application
 * avec les permissions qu'elle utilise
 */

package com.carlocriniti.android.permission_explorer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.R.string;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class ApplicationDetail extends Activity {
	private ListView permissionList; // Composant graphique g�rant la liste de permissions
	private ImageButton manageButton; // Bouton pemrettant d'ouvrir l'application manager
	private String packageName;
	private Context context;
	private Button exportButton;
	private ArrayList<String> mArrayList = new ArrayList<String>();
	private ArrayList<String> mArrayListAll = new ArrayList<String>();
	/*
	 * onCreate :
	 * Ex�cut� � la cr�ation de l'activit�. R�cup�re
	 * les informations sur l'application re�ue par
	 * l'Intent et les inscrits dans les composants
	 * graphiques
	 */
	@Override
    public void onCreate(Bundle savedInstanceState) {        
    	super.onCreate(savedInstanceState);
    	
    	// Cr�ation de l'interface graphique et r�cup�ration de l'Intent
        setContentView(R.layout.application_detail);
        
        this.context = this;
    	Intent thisIntent = getIntent();
        String applicationId = Long.toString(thisIntent.getExtras().getLong("applicationId"));
        
        // R�cup�ration des donn�es
        Cursor data = Tools.database.database.query("application", new String[]{"label", "name", "version_code", "version_name", "system"}, "id = ?", new String[]{applicationId}, null, null, null);
        if (data.getCount() == 1) {
        	data.moveToFirst();
        	
        	packageName = data.getString(1);
        	
        	// Affichage du nom de l'application, du package et de la version
        	((TextView)findViewById(R.id.application_detail_label)).setText(data.getString(0));
        	((TextView)findViewById(R.id.application_detail_name)).setText(data.getString(1));
        	((TextView)findViewById(R.id.application_detail_version)).setText(data.getString(2) + " / " + data.getString(3));
        	
        	if (data.getInt(4) == 1)
        		((TextView)findViewById(R.id.application_detail_system)).setVisibility(View.VISIBLE);
        	else
        		((TextView)findViewById(R.id.application_detail_system)).setVisibility(View.GONE);
        	
        	
        	manageButton = (ImageButton)findViewById(R.id.application_detail_manage_button); 
        	manageButton.setImageResource(R.drawable.ic_menu_manage);
        	manageButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
				    if (Build.VERSION.SDK_INT >= 9) {
						try {
					    	Intent i = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
						    i.addCategory(Intent.CATEGORY_DEFAULT);
					    	i.setData(Uri.parse("package:" + packageName));
					        startActivity(i);
					    } catch (ActivityNotFoundException anfe) {
					    	Intent i = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
						    i.addCategory(Intent.CATEGORY_DEFAULT);
					        startActivity(i);
					    }
				    } else {
				    	Toast.makeText(ApplicationDetail.this, context.getText(R.string.application_detail_manager_unavailable), Toast.LENGTH_LONG).show();
				    }
					
				}
            });
        	
        	// R�cup�ration du nombre de permissions utilis�es et affichage
        	data = Tools.database.database.rawQuery("SELECT Count(*) AS number " +
        											"FROM relation_application_permission " +
        											"WHERE application = ?;", new String[]{applicationId});
        	data.moveToFirst();
        	((TextView)findViewById(R.id.application_detail_permission_count)).setText(data.getString(0));
        	
        	// R�cup�ration des permissions et cr�ation de la liste
        	//Cursor permissionListCursor = Tools.database.database.rawQuery("SELECT permission.id AS _id, permission.name AS name FROM relation_application_permission INNER JOIN permission ON relation_application_permission.permission = permission.id WHERE relation_application_permission.application = ? ORDER BY permission.name COLLATE NOCASE ASC;", new String[] {applicationId});
        	Cursor permissionListCursor = Tools.database.database.rawQuery("SELECT permission.id AS _id, application.label as appName, permission.name AS name FROM relation_application_permission INNER JOIN permission ON relation_application_permission.permission = permission.id INNER JOIN application ON relation_application_permission.application = application.id WHERE application.system = 0 ORDER BY appName COLLATE NOCASE ASC;", null);
        	startManagingCursor(permissionListCursor);
        	//startManagingCursor(permissionListCursorAll);
        	ListAdapter permissionAdapter = new SimpleCursorAdapter(this, R.layout.permission_list_item, permissionListCursor, new String[] {"name"}, new int[]{R.id.listviewpermissiontext});
        	permissionList = (ListView)findViewById(R.id.application_detail_permission_list);
        	permissionList.setAdapter(permissionAdapter);
        	
        	
			/*permissionListCursor.moveToFirst();
			while(!permissionListCursor.isAfterLast()) {
			     mArrayList.add(permissionListCursor.getString(permissionListCursor.getColumnIndex("name"))); //add the item
			     permissionListCursor.moveToNext();
			}*/
			
			permissionListCursor.moveToFirst();
			while(!permissionListCursor.isAfterLast()) {
				 String thePermission = "\"" + permissionListCursor.getString(permissionListCursor.getColumnIndex("appName")) + "\",\"" + permissionListCursor.getString(permissionListCursor.getColumnIndex("name")) + "\"";
			     mArrayList.add(thePermission); //add the item
			     permissionListCursor.moveToNext();
			}
        	
        	exportButton = (Button)findViewById(R.id.application_export_csv_button);
        	exportButton.setOnClickListener(new OnClickListener() {
				
				public void onClick(View v) {
					// TODO Auto-generated method stub
					
					
					writeCSV(mArrayList);
					Toast.makeText(getApplicationContext(), "CSV Created", Toast.LENGTH_SHORT).show();
				}
			});
        	
        	// Evenement au clic sur la liste
        	permissionList.setOnItemClickListener(new OnItemClickListener() {
    			public void onItemClick(AdapterView<?> a, View v, int position, long id) {
    				// Ouverture de l'activite d�tail de la permission selectionnee
    				Intent intent = new Intent(getBaseContext() , PermissionDetail.class);     
    				intent.putExtra("permissionId",id); 			  		  
    				startActivity(intent); 
    			}
            });
        	
        } else {
        	// Application non trouv�e dans la base de donnees
        	((TextView)findViewById(R.id.application_detail_label)).setText(getString(R.string.application_detail_nodata));
        }
        // Fermeture de l'acc�s a la base de donnees
        data.close();
	}
	

	
	public void writeCSV(ArrayList<String> data){
		String columnString =   "\"Status\",\"Value";
		StringBuilder strBuilder = new StringBuilder();
	
		for(int i=0;i<(data.size());i++){
			strBuilder.append(data.get(i)+"\n");
		}
	
		String combinedString = columnString+ "\n" + strBuilder;
		
		
		File file   = null;
		File root   = Environment.getExternalStorageDirectory();
		Log.i("datax", "writeCSV");
		Toast.makeText(getApplicationContext(), "CSV Created bla"+root.getAbsolutePath(), Toast.LENGTH_SHORT).show();
		if (root.canWrite()){
		    File dir    =   new File (root.getAbsolutePath());
		    Log.i("datax", root.getAbsolutePath().toString());
		     dir.mkdirs();
		     file   =   new File(dir, "permission.csv");
		     FileOutputStream out   =   null;
		     Toast.makeText(getApplicationContext(), "CSV Created"+root.getAbsolutePath(), Toast.LENGTH_SHORT).show();
		    try {
		        out = new FileOutputStream(file);
		        Log.i("datax", "FileOutputStream");
	        } catch (FileNotFoundException e) {
	            e.printStackTrace();
	        }
		    
	        try {
	            out.write(combinedString.getBytes());
	            Log.i("datax", "columnString");
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        
	        try {
	            out.close();
	            Log.i("datax", "close");
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		}
	}
	
	public void writeCSV1(ArrayList<String> data){
		String columnString =   "\"Status\",\"Value";
		StringBuilder strBuilder = new StringBuilder();
	
		for(int i=0;i<(data.size());i++){
			strBuilder.append("\""+data.get(i)+"\n");
		}
	
		String combinedString = columnString+ "\n" + strBuilder;
		
		
		File file   = null;
		File root   = Environment.getExternalStorageDirectory();
		Log.i("datax", "writeCSV");
		Toast.makeText(getApplicationContext(), "CSV Created bla"+root.getAbsolutePath(), Toast.LENGTH_SHORT).show();
		if (root.canWrite()){
		    File dir    =   new File (root.getAbsolutePath());
		    Log.i("datax", root.getAbsolutePath().toString());
		     dir.mkdirs();
		     file   =   new File(dir, "permission.csv");
		     FileOutputStream out   =   null;
		     Toast.makeText(getApplicationContext(), "CSV Created"+root.getAbsolutePath(), Toast.LENGTH_SHORT).show();
		    try {
		        out = new FileOutputStream(file);
		        Log.i("datax", "FileOutputStream");
	        } catch (FileNotFoundException e) {
	            e.printStackTrace();
	        }
		    
	        try {
	            out.write(combinedString.getBytes());
	            Log.i("datax", "columnString");
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        
	        try {
	            out.close();
	            Log.i("datax", "close");
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		}
	}
}
