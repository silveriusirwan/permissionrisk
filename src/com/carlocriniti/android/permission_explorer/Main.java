/*
 * Projet 	: Permission Explorer
 * Auteur 	: Carlo Criniti
 * Date   	: 2011.06.10
 * 
 * Classe Main
 * Activit� d'affichage principale avec les onglets
 * et les 3 listes
 */
package com.carlocriniti.android.permission_explorer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.TelephonyManager;
import android.text.InputFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendPosition;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;




//import com.example.securityscanner.R;
//import com.example.securityscanner.MainActivity.LockType;
import java.util.HashMap;
import java.util.Map;

public class Main extends Activity {
	final String TAG = "securityscanner";
	
	private ListView permissionList; //BN add for listing application
	private ArrayList<String> mArrayList = new ArrayList<String>(); //BN add for arraylist on exportCSV
	private ArrayList<String> mArrayListAll = new ArrayList<String>(); //BN add for arraylist on exportCSV
	private Context context; //BN add for get context this activity
	private String packageName; //BN add for get context application list
	private long id;
	
	//BN add to merge with security scanner
	static TextView rootStatus;
	static TextView nfcStatus;
	static TextView bluetoothStatus;
	static TextView lockStatus;
	static TextView unknownStatus;
	static TextView encryptedStatus;
	static TextView locationStatus;
	static TextView simlockStatus;
	static TextView wifiHistory;
	static TextView readResult;
	static RadarChart schart;
	static RadarChart mchart;
	static TextView appList;
	static RadioGroup verifyradio;
	static RadioGroup bootloaderradio;
	static RadioGroup sdencryptradio;
	static RadioGroup simpinradio;
	static Button assessbutton;
	static TextView configDetail;
	static TextView sectitle;
	static LinearLayout ll;
	static ScrollView sv;
	private ProgressDialog loading;  
	
	Typeface tf;
	private List<String> data;
	private List<String> dataApplication;
	private List<String> dataWifi;
	private HashMap<Integer,String> checkedIgnoreList;
	
	public static final int SECURITY_NONE = 0;
	public static final int SECURITY_WEP = 1;
	public static final int SECURITY_PSK = 2;
	public static final int SECURITY_EAP = 3;
	// BN add - END
	
	// Listes affich�es � l'utilisateur
	/*private ListView lstCategory;
	private ListView lstApplication;
	private ListView lstPermission;
	
	private TextView tabCategory;
	private TextView tabApplication;
	private TextView tabPermission;
	
	private enum VIEWS {Category, Application, Permission};
	private VIEWS currentView;*/
	
	// Code de retour de l'�cran de pr�f�rences
	private final int ACTIVITY_RESULT_PREFERENCE = 1000;
	
	/*
	 * onCreate :
	 * Ex�cut� � la cr�ation de l'activit�. Cr��
	 * les onglets et charge les listes apr�s
	 * avoir v�rifi� la base de donn�es
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {        
    	super.onCreate(savedInstanceState);
    	
    	setContentView(R.layout.main);
    	// Chargement de l'interface graphique
      //  setContentView(R.layout.main);
        
        //BN add to merge with security scanner
        Log.i("datax", this.getApplicationContext().getPackageCodePath().toString());
		rootStatus = (TextView)findViewById(R.id.id_root_status);
		nfcStatus = (TextView)findViewById(R.id.id_nfc_status);
		bluetoothStatus = (TextView)findViewById(R.id.id_bluetooth_status);
		lockStatus = (TextView)findViewById(R.id.id_lock_status);
		unknownStatus = (TextView)findViewById(R.id.id_unknown_status);
		encryptedStatus = (TextView)findViewById(R.id.id_encrypted_status);
		locationStatus = (TextView)findViewById(R.id.id_location_status);
		simlockStatus = (TextView)findViewById(R.id.id_simlock_status);
		wifiHistory = (TextView) findViewById(R.id.id_wifi_history);
		sectitle = (TextView) findViewById(R.id.id_seccon_title);
		TextView apptitle = (TextView) findViewById(R.id.id_apprisk_title);
		appList = (TextView) findViewById(R.id.id_app_list);
		configDetail = (TextView) findViewById(R.id.id_config_detail);
		verifyradio = (RadioGroup) findViewById(R.id.id_verify_radio);
		bootloaderradio = (RadioGroup) findViewById(R.id.id_bootloader_radio);
		sdencryptradio = (RadioGroup) findViewById(R.id.id_sdencrypt_radio);
		simpinradio = (RadioGroup) findViewById(R.id.id_simpin_radio);
		assessbutton = (Button) findViewById(R.id.id_assess_button);
		schart = (RadarChart) findViewById(R.id.id_seccon_chart);
		mchart = (RadarChart) findViewById(R.id.id_chart);
		
		//hide configuration status textview
		rootStatus.setVisibility(View.GONE);
		nfcStatus.setVisibility(View.GONE);
		bluetoothStatus.setVisibility(View.GONE);
		lockStatus.setVisibility(View.GONE);
		unknownStatus.setVisibility(View.GONE);
		encryptedStatus.setVisibility(View.GONE);
		locationStatus.setVisibility(View.GONE);
		simlockStatus.setVisibility(View.GONE);
//		wifiHistory.setVisibility(View.GONE);
		sectitle.setVisibility(View.GONE);
		schart.setVisibility(View.GONE);
		
		ll = (LinearLayout) findViewById(R.id.id_main_layout);
		

		
		data = new ArrayList<String>();
		checkedIgnoreList = new HashMap<Integer, String>();
		
		//turn on wifi			
		WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE); 		
		boolean wifiEnabled = wifiManager.isWifiEnabled();
		if (wifiEnabled==false){
			wifiManager.setWifiEnabled(true);
		}
		
		tf = Typeface.createFromAsset(getAssets(), "TIMES.TTF");
//		NfcAdapter nfcAdpt = NfcAdapter.getDefaultAdapter(this.getApplicationContext());	
//		if(nfcAdpt!=null)
//		{
//			
//		if(nfcAdpt.isEnabled())
//			{
//				nfcStatus.setText("NFC status = true");
//			}
//			else
//			{
//				nfcStatus.setText("NFC status = false");
//			}
//		} else nfcStatus.setText("NFC status = no NFC adapter");
		
//		boolean rootstatus2 = isRooted();
//		rootStatus.setText("Root status= "+String.valueOf(rootstatus2));
//		
//		//add new line
//		data.add("Root Status");
//		data.add(String.valueOf(rootstatus2));
		
		// Check for available NFC Adapter
        /*PackageManager pm = getPackageManager();
        if(pm.hasSystemFeature(PackageManager.FEATURE_NFC) && NfcAdapter.getDefaultAdapter(this) != null) {
        	nfcStatus.setText("NFC status = true");
        } else {
        	nfcStatus.setText("NFC status = false");
        }*/
		
//		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//		//add new line
//		data.add("Bluetooth Status");
//		
//		if (mBluetoothAdapter == null) {
//			bluetoothStatus.setText("Bluetooth status = null");
//		} else {
//		    if (!mBluetoothAdapter.isEnabled()) {
//		    	bluetoothStatus.setText("Bluetooth status = false");
//		    	data.add("false");
//		    }else{
//		    	bluetoothStatus.setText("Bluetooth status = true");
//		    	data.add("true");
//		    }
//		}
		
//		int lockType = LockType.getCurrent(getContentResolver());
//		String lockType2;
//		switch(lockType) {
//	    case 1:
//	        lockType2 = "NONE or SLIDE";
//	        break;
//	    case 3:
//	    	lockType2 = "FACE WITH PATTERN";
//	        break;
//	    case 4:
//	    	lockType2 = "FACE WITH PIN";
//	        break;
//	    case 10:
//	    	lockType2 = "PATTERN";
//	        break;    
//	    case 11:
//	    	lockType2 = "PIN";
//	        break;
//	    case 12:
//	    	lockType2 = "PASSWORD ALPHABETIC";
//	        break;
//	    case 13:
//	    	lockType2 = "PASSWORD ALPHANUMERIC";
//	        break;
//	    default:
//	    	lockType2 = "ERROR";
//		}
//		
//		
//		lockStatus.setText("Lock status= "+String.valueOf(lockType2));
//		data.add("Lock screen status");
//		data.add(String.valueOf(lockType2));
		
//		boolean isNonPlayAppAllowed = isTrustUnknownSource();
//		unknownStatus.setText("Unknown sources status= "+String.valueOf(isNonPlayAppAllowed));
//		data.add("Unknown sources status");
//		data.add(String.valueOf(isNonPlayAppAllowed));
		
//		boolean encryptedStatus2 = isEncrypted(this.getApplicationContext());
//		encryptedStatus.setText("Encrypted status= "+String.valueOf(encryptedStatus2));
//		data.add("Phone Encryption status");
//		data.add(String.valueOf(encryptedStatus2));
		
//		boolean locationStatus2 = isGpsEnabled();
//		locationStatus.setText("Location status= "+String.valueOf(locationStatus2));
//		data.add("Location status");
//		data.add(String.valueOf(locationStatus2));
		
//		boolean simLockStatus2 = isSimPinRequired(getApplicationContext());
//		simlockStatus.setText("Sim lock status= "+String.valueOf(simLockStatus2));
//		data.add("Sim lock status");
//		data.add(String.valueOf(simLockStatus2));
		
		/*WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		List<WifiConfiguration> arraylist = wifiManager.getConfiguredNetworks();
		Log.i("WifiPreference","No of Networks "+wifiManager.getDhcpInfo().toString());//+arraylist.size());
		*/
		//wifi = new ArrayList<String>();
		/*
		WifiManager wifiManager=(WifiManager)getSystemService(Context.WIFI_SERVICE);
		List<WifiConfiguration> networks=wifiManager.getConfiguredNetworks();
		for (WifiConfiguration config : networks) {
			Log.i("Wifi",config.SSID + " " +getSecurity(config));
			
		}*/
	
/*		
		StringBuilder strWifiStatus = new StringBuilder(); 
		dataWifi = new ArrayList<String>();
		WifiManager wifiManager=(WifiManager)getSystemService(Context.WIFI_SERVICE); 
		List<WifiConfiguration> networks=wifiManager.getConfiguredNetworks(); 
		int openwifi=0;
		int sumwifi=0;
		float percentwifi;
		//strWifiStatus.append("Wifi History:\n"); 
		for (WifiConfiguration config : networks) 
		{
			//Log.i("Wifi",config.SSID + " " +getSecurity(config)); 
//			strWifiStatus.append(config.SSID+" ("+getSecurity(config)+")\n");
			if (getSecurity(config)=="NONE"){
				openwifi=openwifi+1;
			}
			sumwifi=sumwifi+1;
//			dataWifi.add(config.SSID);
//			dataWifi.add(getSecurity(config));
		}
//		strWifiStatus.append("Jumlah Open Wifi = "+openwifi);
//		strWifiStatus.append("Jumlah Wifi = "+sumwifi);
		percentwifi = ((float) openwifi/sumwifi)*100;
		strWifiStatus.append("Persentase Open Wifi = "+ percentwifi + " ("+ openwifi + "/" + sumwifi +")");

		wifiHistory.setText(strWifiStatus);
*/		
		dataApplication = new ArrayList<String>(); 
		final PackageManager pm = getPackageManager(); 
		List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA); 
		for (ApplicationInfo appInfo : packages) 
		{
			String marketName = getMarket(appInfo.packageName);
			ApplicationInfo lApplicationInfo = null;
			try {
				lApplicationInfo = pm.getApplicationInfo(appInfo.packageName, 0);
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String appName = (String) (lApplicationInfo != null ? pm.getApplicationLabel(lApplicationInfo) : "Unknown"); 			
			//Log.i("PackageMarket", marketName); 
			dataApplication.add(marketName); 
			dataApplication.add(appName);
		}
        //BN add - END
        
        // R�cup�ration des listes et ajout des �venements de clic
        /*lstCategory = (ListView)findViewById(R.id.listviewcategory);
        lstCategory.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> a, View v, int position, long id) {
				// Ouverture du d�tail d'une cat�gorie
				Intent intent = new Intent(getBaseContext() , CategoryDetail.class);     
				intent.putExtra("categoryId",id); 			  		  
				startActivity(intent); 
			}
        });
        
        lstApplication = (ListView)findViewById(R.id.listviewapplication);
        lstApplication.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> a, View v, int position, long id) {
				// Ouverture du d�tail d'une application
				Intent intent = new Intent(getBaseContext() , ApplicationDetail.class);     
				intent.putExtra("applicationId",id); 			  		  
				startActivity(intent); 
			}
        });
        
        lstPermission = (ListView)findViewById(R.id.listviewpermission);
        lstPermission.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> a, View v, int position, long id) {
				// Ouverture du d�tail d'une permission
				Intent intent = new Intent(getBaseContext() , PermissionDetail.class);     
				intent.putExtra("permissionId",id); 			  		  
				startActivity(intent); 
			}
        });
        
        tabCategory = (TextView)findViewById(R.id.tab_category);
        tabCategory.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (currentView != VIEWS.Category) {
					switchTo(VIEWS.Category);
				}
			}
        });
        
        tabApplication = (TextView)findViewById(R.id.tab_application);
        tabApplication.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (currentView != VIEWS.Application) {
					switchTo(VIEWS.Application);
				}
			}
        });
        
        tabPermission = (TextView)findViewById(R.id.tab_permission);
        tabPermission.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (currentView != VIEWS.Permission) {
					switchTo(VIEWS.Permission);
				}
			}
        });
        
        switchTo(VIEWS.Category);*/
        
        // Cr�ation/ouverture de la base de donn�es
        Tools.database = new Database(this);
        // V�rification de la base de donn�es
        Tools.database.isUpToDate();
        
        assessbutton.setOnClickListener( new OnClickListener() {
            public void onClick(View v) {
                // TODO Auto-generated method stub
            	StringBuilder textHasil = new StringBuilder();
            	int verifyid = verifyradio.getCheckedRadioButtonId();
            	int bootloaderid = bootloaderradio.getCheckedRadioButtonId();
            	int sdencryptid = sdencryptradio.getCheckedRadioButtonId();
            	int simpinid = simpinradio.getCheckedRadioButtonId();
            	int lockType = LockType.getCurrent(getContentResolver());
            	float conlevel = 0;
            	
            	boolean locstat = isGpsEnabled();
            	boolean verstat;
            	boolean unkstat = isTrustUnknownSource();
            	boolean rootstat = isRooted();
            	boolean bootstat;
            	boolean scrstat;
            	boolean phenstat=isEncrypted(getApplicationContext());
            	boolean sdenstat;
            	boolean simstat;
            	
            	StringBuilder strWifiStatus = new StringBuilder(); 
        		dataWifi = new ArrayList<String>();
        		WifiManager wifiManager=(WifiManager)getSystemService(Context.WIFI_SERVICE); 
        		List<WifiConfiguration> networks=wifiManager.getConfiguredNetworks(); 
        		int openwifi=0;
        		int sumwifi=0;
        		float percentwifi;

        		for (WifiConfiguration config : networks) 
        		{
        			if (getSecurity(config)=="NONE"){
        				openwifi=openwifi+1;
        			}
        			sumwifi=sumwifi+1;
        		}
        		percentwifi = ((float) openwifi/sumwifi)*100;
        		
        		
            	String lockDetail ="";
            	switch(lockType) {
        	    case 1:
        	    	lockDetail = "NONE or SLIDE";
        	        break;
        	    case 3:
        	    	lockDetail = "FACE WITH PATTERN";
        	        break;
        	    case 4:
        	    	lockDetail = "FACE WITH PIN";
        	        break;
        	    case 10:
        	    	lockDetail = "PATTERN";
        	        break;    
        	    case 11:
        	    	lockDetail = "PIN";
        	        break;
        	    case 12:
        	    	lockDetail = "PASSWORD ALPHABETIC";
        	        break;
        	    case 13:
        	    	lockDetail = "PASSWORD ALPHANUMERIC";
        	        break;
        	    default:
        	    	lockDetail = "ERROR";
        		}
            	
            	if (lockType!=1){
            		scrstat = true;
            	}else scrstat = false;
            	if (verifyid == R.id.id_verify_true){
        	    	
        	    	verstat = true;
        	    } else {
        	    	verstat = false;
        	    }
        	    
        	    if (bootloaderid == R.id.id_bootloader_true){
        	    	bootstat=true;
        	    } else {
        	    	bootstat=false;
        	    }
        	    
        	    if (sdencryptid == R.id.id_sdencrypt_true){
        	    	sdenstat=true;	
        	    } else {
        	    	sdenstat=false;	
        	    }
        	     
        	    if (simpinid == R.id.id_simpin_true){
        	    	simstat=true;
        	    } else {
        	    	simstat=false;    	    	
        	    }

        	    schart.setDescription("");

            	schart.setWebLineWidth(1.5f);
            	schart.setWebLineWidthInner(0.75f);
            	schart.setWebAlpha(100);
            	
            	ArrayList<String> xValue = new ArrayList<String>();
            	ArrayList<Entry> yValue = new ArrayList<Entry>();
            	
            	xValue.add("Network");
            	xValue.add("Application");
            	xValue.add("Operating System");
            	xValue.add("Device");
            	
            	for(int i=0;i<4;i++){
            		if(i==0){
            			if (locstat==false){
            				if(percentwifi==0){
            					conlevel=5;
            				}else if(percentwifi>0 && percentwifi<=10 ){
            					conlevel=4;
            				}else if(percentwifi>10 && percentwifi<=30 ){
            					conlevel=3;
            				}else if(percentwifi>30){
            					conlevel=2;
            				}
            			}else if(locstat==true){
            				if(percentwifi==0){
            					conlevel=4;
            				}else if(percentwifi>0 && percentwifi<=10 ){
            					conlevel=3;
            				}else if(percentwifi>10 && percentwifi<=30 ){
            					conlevel=2;
            				}else if(percentwifi>30){
            					conlevel=1;
            				}
            			}
            			yValue.add(new Entry(conlevel,i));
            			
            			if (Float.isNaN(percentwifi)){
            				textHasil.append("Network configuration level = -");
            				textHasil.append(System.getProperty("line.separator"));
                			textHasil.append(" > Open Wi-Fi Usage = (unable to get wifi history)");
                		}else{
                			textHasil.append("Network configuration level = "+conlevel);
                			textHasil.append(System.getProperty("line.separator"));
                			textHasil.append(" > Open Wi-Fi Usage = "+ percentwifi + "% ("+ openwifi + "/" + sumwifi +")");
                		}
            			textHasil.append(System.getProperty("line.separator"));
            			textHasil.append(" > Location status = "+ locstat);
                		textHasil.append(System.getProperty("line.separator"));
                		textHasil.append(System.getProperty("line.separator"));
            		}
            		if (i==1){
            			if (verstat==true){
                	    	if (unkstat==false) {
                	    		conlevel = 5;
                	    	}else if (unkstat == true){
                	    		conlevel = 2;
                	    	}
                	    }else if (verstat==false){
                	    	if (unkstat == true){
                	    		conlevel = 1;
                	    	}else if (unkstat == false){
                	    		conlevel = 3;
                	    	}
                	    }
            			yValue.add(new Entry(conlevel,i));
            			textHasil.append("Application configuration level = "+conlevel);
            			textHasil.append(System.getProperty("line.separator"));
            			
            			textHasil.append(" > Verify apps status = "+ String.valueOf(verstat));
                	    textHasil.append(System.getProperty("line.separator"));
                    	
                	    textHasil.append(" > Unknown sources status= "+String.valueOf(unkstat));
                	    textHasil.append(System.getProperty("line.separator"));
                	    textHasil.append(System.getProperty("line.separator"));
            		}
            		conlevel=0;
            		if(i==2){
            			if (rootstat==true){
                	    	if (bootstat==true) {
                	    		conlevel = 2;
                	    	}else {
                	    		conlevel = 1;
                	    	}
                	    }else if (rootstat==false){
                	    	conlevel = 5;
                	    }
            			yValue.add(new Entry(conlevel,i));
            			textHasil.append("Operating system configuration level = "+conlevel);
            			textHasil.append(System.getProperty("line.separator"));
            			
            			textHasil.append(" > Root status= "+String.valueOf(rootstat));
                	    textHasil.append(System.getProperty("line.separator"));
                	    
                	    textHasil.append(" > Bootloader status = "+ String.valueOf(bootstat));
                	    textHasil.append(System.getProperty("line.separator"));
                	    textHasil.append(System.getProperty("line.separator"));
            		}
            		conlevel=0;
            		if(i==3){
            			if(scrstat==true){
            				if (phenstat==true){
            					if(sdenstat==true){
            						if(simstat==true){
            							conlevel=5;
            						}else if(simstat==false){
            							conlevel=4;
            						}         						
            					}else if(sdenstat==false){
            						if(simstat==true){
            							conlevel=4;
            						}else if(simstat==false){
            							conlevel=3;
            						}
            					}
            				}else if(phenstat==false){
            					if(sdenstat==true){
            							conlevel=0;         	         						
            					}else if(sdenstat==false){
            						if(simstat==true){
            							conlevel=3;
            						}else if(simstat==false){
            							conlevel=2;
            						}
            					}
            				}
            			}else if(scrstat==false){
            				if (phenstat==true){
            					conlevel=0;
            				}else if(phenstat==false){
            					if(sdenstat==true){
            							conlevel=0;         	         						
            					}else if(sdenstat==false){
            						if(simstat==true){
            							conlevel=2;
            						}else if(simstat==false){
            							conlevel=1;
            						}
            					}
            				}
            			}	
            			yValue.add(new Entry(conlevel,i));
            			textHasil.append("Device configuration level = "+conlevel);
            			textHasil.append(System.getProperty("line.separator"));
            			
            			textHasil.append(" > Screen lock status = "+ lockDetail);
                	    textHasil.append(System.getProperty("line.separator"));
                	    
                	    textHasil.append(" > Phone Encryption status = "+ String.valueOf(phenstat));
                	    textHasil.append(System.getProperty("line.separator"));
                	    
                	    textHasil.append(" > SD card encryption status = "+String.valueOf(sdenstat));
                	    textHasil.append(System.getProperty("line.separator"));
                	    
                	    textHasil.append(" > SIM card PIN status = "+String.valueOf(simstat));
            		}
            	}
            	
            	RadarDataSet set1 = new RadarDataSet(yValue, "Configuration Level");
                set1.setColor(ColorTemplate.VORDIPLOM_COLORS[0]);
                set1.setDrawFilled(true);
                set1.setLineWidth(2f);
                
                RadarData data = new RadarData(xValue, set1);
                data.setValueTypeface(tf);
                data.setValueTextSize(8f);
                data.setDrawValues(false);
                
                schart.setData(data);
                schart.invalidate();
                
                XAxis xAxis = schart.getXAxis();
                xAxis.setTypeface(tf);
                xAxis.setTextSize(9f);

                YAxis yAxis = schart.getYAxis();
                yAxis.setAxisMaxValue(5f);
                yAxis.setTypeface(tf);
                yAxis.setTextSize(9f);
                yAxis.setStartAtZero(true);
                yAxis.setLabelCount(6, true);
              
                schart.notifyDataSetChanged();
        		schart.invalidate();
                
                Legend l = schart.getLegend();
                l.setPosition(LegendPosition.RIGHT_OF_CHART);
                l.setTypeface(tf);
                l.setXEntrySpace(7f);
                l.setYEntrySpace(5f);
            
                configDetail.setText(textHasil);
                sectitle.setVisibility(View.VISIBLE);
        		schart.setVisibility(View.VISIBLE);
            }
        });
        
//        readResult = (TextView)findViewById(R.id.id_hello_world);
        /*
        String FILE_NAME = "permission.csv";
        String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
        File file = new File(baseDir, FILE_NAME);

        String line = "";
        String date="";

        StringBuilder textHasil = new StringBuilder();

        try {
            FileReader fReader = new FileReader(file);
            BufferedReader bReader = new BufferedReader(fReader);

            
        	
                while ((line = bReader.readLine()) != null) {
                 String[] RowData = line.split(",");
                 date = RowData[0];
               
				String date2 = date.replaceAll("\"", "");
                 String value = RowData[1];
                 String value2 = value.replaceAll("\"", "");
                 textHasil.append(date2+" = "+value2);
                 
                 textHasil.append(System.getProperty("line.separator"));
                 
                	// do something with "data" and "value"
            }
        } catch (IOException e) {
            e.printStackTrace();
        }           
            readResult.setText(textHasil);
            readResult.setMovementMethod(new ScrollingMovementMethod());
        */
        
        exportCSV();

//        spinner.setVisibility(View.GONE);
    }
    

    
    //BN add method from security scanner
    public String getMarket(String packageName){ 
		String market = ""; 
		String installer = getPackageManager().getInstallerPackageName(packageName);
		if (installer == null) 
		{ // change to samsung app store link 
			if (packageName.contains("samsung")) 
			{ 
				market = "Samsung Vendor"; 
			} 
			else if (packageName.contains("lge")) 
			{ 
				market = "LG Vendor"; 
			}
			else if (packageName.contains("sony")) 
			{ 
				market = "Sony Vendor"; 
			}
			else if (packageName.contains("asus")) 
			{ 
				market = "Asus Vendor"; 
			}
			else if (packageName.contains("android")) 
			{ 
				market = "Android Default"; 
			}
			else 
			{
				market = "Unknown Sources";
			}
		}
		else if (installer.contains("android")) 
		{ // change to amazon app store link 
			market = "Google Play"; 
		} 
		else if (installer.contains("amazon")) 
		{ // change to amazon app store link 
			market = "Amazon App"; 
		} else
		{
			market = "Other Market";
		}
		
		return market; 		
	}
	
	public static String getSecurity(WifiConfiguration config) {
	    if (config.allowedKeyManagement.get(KeyMgmt.WPA_PSK)) 
	        return "WPA/WPA2 PSK";

	    if (config.allowedKeyManagement.get(KeyMgmt.WPA_EAP) || config.allowedKeyManagement.get(KeyMgmt.IEEE8021X)) 
	    	return "WPA-EAP";

	    return (config.wepKeys[0] != null) ? "WEP" : "NONE";
	}
	
	public  boolean isSimPinRequired(Context context){
	    TelephonyManager m = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
	    if (m.getSimState() == TelephonyManager.SIM_STATE_PIN_REQUIRED) 
	    	{return true;} else
	    {return false;}
	}

	public boolean isGpsEnabled()
	{
	    LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
	    return service.isProviderEnabled(LocationManager.GPS_PROVIDER)&&service.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
	}
	
	private static boolean isRooted() {
	    return findBinary("su");
	}
	
	public  boolean isTrustUnknownSource(){
		boolean res = false;
		try {
			res = Settings.Secure.getInt(getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS) == 1;
		} catch (SettingNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	return res;
	}
	
	@SuppressLint("NewApi")
    private boolean isEncrypted(Context context) {
		boolean res= false;
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context
                .getSystemService(Context.DEVICE_POLICY_SERVICE);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            int status = devicePolicyManager.getStorageEncryptionStatus();
            if (DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE == status) {
                res= true;
            }
        }
        return res;
    }

	public static boolean findBinary(String binaryName) {
	    boolean found = false;
	    if (!found) {
	        String[] places = {"/sbin/", "/system/bin/", "/system/xbin/", "/data/local/xbin/",
	                "/data/local/bin/", "/system/sd/xbin/", "/system/bin/failsafe/", "/data/local/"};
	        for (String where : places) {
	            if ( new File( where + binaryName ).exists() ) {
	            	found = true;
	                break;
	            }
	        }
	    }
	    
	    return found;
	}
	
	public static class LockType
	{
	    private final static String PASSWORD_TYPE_KEY = "lockscreen.password_type";

	    /**
	     * This constant means that android using some unlock method not described here.
	     * Possible new methods would be added in the future releases.
	     */
	    public final static int SOMETHING_ELSE = 0;

	    /**
	     * Android using "None" or "Slide" unlock method. It seems there is no way to determine which method exactly used.
	     * In both cases you'll get "PASSWORD_QUALITY_SOMETHING" and "LOCK_PATTERN_ENABLED" == 0.
	     */
	    public final static int NONE_OR_SLIDER = 1;

	    /**
	     * Android using "Face Unlock" with "Pattern" as additional unlock method. Android don't allow you to select
	     * "Face Unlock" without additional unlock method.
	     */
	    public final static int FACE_WITH_PATTERN = 3;

	    /**
	     * Android using "Face Unlock" with "PIN" as additional unlock method. Android don't allow you to select
	     * "Face Unlock" without additional unlock method.
	     */
	    public final static int FACE_WITH_PIN = 4;

	    /**
	     * Android using "Face Unlock" with some additional unlock method not described here.
	     * Possible new methods would be added in the future releases. Values from 5 to 8 reserved for this situation.
	     */
	    public final static int FACE_WITH_SOMETHING_ELSE = 9;

	    /**
	     * Android using "Pattern" unlock method.
	     */
	    public final static int PATTERN = 10;

	    /**
	     * Android using "PIN" unlock method.
	     */
	    public final static int PIN = 11;

	    /**
	     * Android using "Password" unlock method with password containing only letters.
	     */
	    public final static int PASSWORD_ALPHABETIC = 12;

	    /**
	     * Android using "Password" unlock method with password containing both letters and numbers.
	     */
	    public final static int PASSWORD_ALPHANUMERIC = 13;

	    /**
	     * Returns current unlock method as integer value. You can see all possible values above
	     * @param contentResolver we need to pass ContentResolver to Settings.Secure.getLong(...) and
	     *                        Settings.Secure.getInt(...)
	     * @return current unlock method as integer value
	     */
	    public static int getCurrent(ContentResolver contentResolver)
	    {
	        long mode = android.provider.Settings.Secure.getLong(contentResolver, PASSWORD_TYPE_KEY,
	                DevicePolicyManager.PASSWORD_QUALITY_SOMETHING);
	        if (mode == DevicePolicyManager.PASSWORD_QUALITY_SOMETHING)
	        {
	            if (android.provider.Settings.Secure.getInt(contentResolver, Settings.Secure.LOCK_PATTERN_ENABLED, 0) == 1)
	            {
	                return LockType.PATTERN;
	            }
	            else return LockType.NONE_OR_SLIDER;
	        }
	        else if (mode == DevicePolicyManager.PASSWORD_QUALITY_BIOMETRIC_WEAK)
	        {
	            String dataDirPath = Environment.getDataDirectory().getAbsolutePath();
	            if (nonEmptyFileExists(dataDirPath + "/system/gesture.key"))
	            {
	                return LockType.FACE_WITH_PATTERN;
	            }
	            else if (nonEmptyFileExists(dataDirPath + "/system/password.key"))
	            {
	                return LockType.FACE_WITH_PIN;
	            }
	            else return FACE_WITH_SOMETHING_ELSE;
	        }
	        else if (mode == DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC)
	        {
	            return LockType.PASSWORD_ALPHANUMERIC;
	        }
	        else if (mode == DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC)
	        {
	            return LockType.PASSWORD_ALPHABETIC;
	        }
	        else if (mode == DevicePolicyManager.PASSWORD_QUALITY_NUMERIC)
	        {
	            return LockType.PIN;
	        }
	        else return LockType.SOMETHING_ELSE;
	    }

	    private static boolean nonEmptyFileExists(String filename)
	    {
	        File file = new File(filename);
	        return file.exists() && file.length() > 0;
	    }
	}
	
	public void writeWifiCSV(List<String> data){
		String columnString =   "\"SSID\",\"Security";
		StringBuilder strBuilder = new StringBuilder();
		for(int i=0;i<(data.size()-1);i=i+2){
			strBuilder.append("\""+data.get(i)+"\",\""+data.get(i+1)+"\n");
		}
		String combinedString = columnString+ "\n" + strBuilder;
		
		File file   = null;
		File root   = Environment.getExternalStorageDirectory();
		Log.i("datax", "writeCSV");
		if (root.canWrite()){
		    File dir    =   new File (root.getAbsolutePath());
		    Log.i("datax", root.getAbsolutePath().toString());
		     dir.mkdirs();
		     file   =   new File(dir, "DataWifi.csv");
		     FileOutputStream out   =   null;
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
	
	public void writeMarketCSV(List<String> data){
		String columnString =   "\"App Source\",\"App Name";
		StringBuilder strBuilder = new StringBuilder();
		for(int i=0;i<(data.size()-1);i=i+2){
			strBuilder.append("\""+data.get(i)+"\",\""+data.get(i+1)+"\n");
		}
		String combinedString = columnString+ "\n" + strBuilder;
		
		File file   = null;
		File root   = Environment.getExternalStorageDirectory();
		Log.i("datax", "writeCSV");
		if (root.canWrite()){
		    File dir    =   new File (root.getAbsolutePath());
		    Log.i("datax", root.getAbsolutePath().toString());
		     dir.mkdirs();
		     file   =   new File(dir, "DataMarket.csv");
		     FileOutputStream out   =   null;
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
    //BN add - END
    
    /*private void switchTo(VIEWS newView) {
    	currentView = newView;
    	
    	if (currentView == VIEWS.Category) {
    		lstCategory.setVisibility(View.VISIBLE);
    		tabCategory.setTextColor(getResources().getColor(R.color.text_tab_selected));
    	} else {
    		lstCategory.setVisibility(View.INVISIBLE);
    		tabCategory.setTextColor(getResources().getColor(R.color.text_tab));
    	}
    	
    	if (currentView == VIEWS.Application) {
    		lstApplication.setVisibility(View.VISIBLE);
    		tabApplication.setTextColor(getResources().getColor(R.color.text_tab_selected));
    	} else {
    		lstApplication.setVisibility(View.INVISIBLE);
    		tabApplication.setTextColor(getResources().getColor(R.color.text_tab));
    	}
    	
    	if (currentView == VIEWS.Permission) {
    		lstPermission.setVisibility(View.VISIBLE);
    		tabPermission.setTextColor(getResources().getColor(R.color.text_tab_selected));
    	} else {
    		lstPermission.setVisibility(View.INVISIBLE);
    		tabPermission.setTextColor(getResources().getColor(R.color.text_tab));
    	}
    }*/
    
    /*
	 * onDestroy :
	 * Fin du programme, on ferme l'acc�s � la base
	 * de donn�es
	 */
    @Override    
    protected void onDestroy() {        
        super.onDestroy();
        Tools.database.database.close();
    }
    
    /*
     * onActivityResult
     * Retour d'une activit�
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
    	super.onActivityResult(requestCode, resultCode, data);
    	
    	// R�cup�ration du code de l'activit� lanc�e
    	switch(requestCode) {
    		case ACTIVITY_RESULT_PREFERENCE: // si on revient des pr�f�rences, on raffraichit les listes
    			refreshData();
    	}
    	
    }
    
    /*
     * onCreateOptionsMenu
     * Cr�ation du menu en fonction de la ressource menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    
    /*
     * onOptionsItemSelected
     * Gestion du clic sur le menu
     */
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
    	switch(item.getItemId()){
    		case R.id.menu_update:
    			Tools.database.updateDatabase(this);
    			return true;
    		case R.id.menu_preferences:
    			Intent intent = new Intent(getBaseContext() , Preference.class);			  		  
    			startActivityForResult(intent, ACTIVITY_RESULT_PREFERENCE);
    			return true;
    		case R.id.menu_exportcsv:
    			//finish();
    			loading = ProgressDialog.show(Main.this, "","Loading...", true);
    			Intent intentIgnore = new Intent(Main.this, IgnoreList.class);
                startActivity(intentIgnore);
                finish();
               // exportCSV();
                //BN added to start exporting CSV
    			//writeWifiCSV(dataWifi);
    			//writeMarketCSV(dataApplication);
    			return true;
    		default:
    			return super.onOptionsItemSelected(item);
    	}
    	/*int id = item.getItemId();
		if (id == R.id.menu_update) {
			Tools.database.updateDatabase(this);
			return true;
		}else if(id == R.id.menu_preferences){
			Intent intent = new Intent(getBaseContext() , Preference.class);			  		  
			startActivityForResult(intent, ACTIVITY_RESULT_PREFERENCE); 
            return true;
		}else if(id == R.id.menu_exportcsv){
			Log.i(TAG,"exportCSV");
        	exportCSV(); //BN added to start exporting CSV
            return true;
		}*/
	}
    
    /*
     * refreshData
     * On raffraichit les listes de cat�gories, applications et permissions
     */
    private void refreshData()
    {
    	// R�cup�ration des pr�f�rences
    	SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
    	boolean categoryOrder = pref.getBoolean("category_order", false); // Order category by true:name / false:count
    	boolean applicationName = pref.getBoolean("application_name", true); // Display true:label / false:package 
    	boolean applicationOrder = pref.getBoolean("application_order", false); // Order app by true:name / false:count
    	boolean permissionOrder = pref.getBoolean("permission_order", false); // Order perm by true:name / false:count
    	boolean hideSystemApp = pref.getBoolean("hide_system_app", false); // Hide system applications
    	
    	String systemAppWhere = "";
    	if (hideSystemApp)
    		systemAppWhere = "WHERE system = 0 ";
    	
    	// Champ � afficher pour le nom
    	String nameField;
    	if (applicationName)
    		nameField = "application.label";
    	else
    		nameField = "application.name";
    	
    	// Champ pour order la liste d'applications
    	String orderField;
    	if (applicationOrder)
    		orderField = "name COLLATE NOCASE ASC";
    	else
    		orderField = "Count(relation_application_permission.permission) COLLATE NOCASE DESC";
    	
    	// On r�cup�re et affiche les applications
    	Cursor applicationCursor = Tools.database.database.rawQuery("SELECT id AS _id, " + nameField + " || ' (' || Count(permission) || ')' AS name, application.name AS package " +
    																"FROM application " +
    																"LEFT OUTER JOIN relation_application_permission ON application.id = relation_application_permission.application " + 
    																systemAppWhere + 
    																"GROUP BY application.id " +
    																"ORDER BY " + orderField + ";", null);
    	startManagingCursor(applicationCursor);
    	 
    	List<ApplicationListItem> items = new ArrayList<ApplicationListItem>();
    	
    	PackageManager pm = getPackageManager();
    	try {
	    	for(applicationCursor.moveToFirst(); !applicationCursor.isAfterLast(); applicationCursor.moveToNext()) {
				items.add(new ApplicationListItem(applicationCursor.getLong(0), pm.getApplicationIcon(applicationCursor.getString(2)), applicationCursor.getString(1)));
	    	}
    	} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
    	
    	ApplicationListAdapter applicationAdapter = new ApplicationListAdapter(this, items);
    	//lstApplication.setAdapter(applicationAdapter);
    	
    	// Champ pour order la liste de permissions
    	if (permissionOrder)
    		orderField = "permission.name COLLATE NOCASE ASC";
    	else
    		orderField = "Count(relation_application_permission.application) COLLATE NOCASE DESC";
    	
    	// On r�cup�re et affiche les permissions
    	Cursor permissionCursor = Tools.database.database.rawQuery("SELECT permission.id AS _id, permission.name || ' (' || Count(application) || ')' AS name " +
    															   "FROM permission " +
    															   "LEFT OUTER JOIN relation_application_permission ON permission.id = relation_application_permission.permission " +
    															   "LEFT OUTER JOIN application ON application.id = relation_application_permission.application " +
    															   systemAppWhere + 
    															   "GROUP BY permission.id " +
    															   "HAVING Count(application) > 0 " +
    															   "ORDER BY " + orderField + ";", null);
    	startManagingCursor(permissionCursor);
    	ListAdapter permissionAdapter = new SimpleCursorAdapter(this, R.layout.permission_list_item, permissionCursor, new String[] {"name"}, new int[]{R.id.listviewpermissiontext});
    	//lstPermission.setAdapter(permissionAdapter);
    	
    	// Champ pour order la liste des cat�gories
    	if (categoryOrder)
    		orderField = "category.name COLLATE NOCASE ASC";
    	else
    		orderField = "Count(DISTINCT relation_application_permission.application) COLLATE NOCASE DESC";
    	
    	// On r�cup�re et affiche les cat�gories
    	Cursor categoryCursor = Tools.database.database.rawQuery("SELECT category.id AS _id, category.name || ' (' || Count(DISTINCT application) || ')' AS name " +
    															 "FROM category " +
    															 "LEFT OUTER JOIN relation_category_permission ON category.id = relation_category_permission.category " +
    															 "INNER JOIN relation_application_permission ON relation_category_permission.permission = relation_application_permission.permission " +
    															 "LEFT OUTER JOIN application ON application.id = relation_application_permission.application " +
    															 systemAppWhere + 
    															 "GROUP BY category.id " +
    															 "HAVING Count(DISTINCT application) > 0 " +
    															 "ORDER BY " + orderField + ";", null);
    	startManagingCursor(categoryCursor);
    	ListAdapter categoryAdapter = new SimpleCursorAdapter(this, R.layout.category_list_item, categoryCursor, new String[] {"name"}, new int[]{R.id.listviewcategorytext});
    	//lstCategory.setAdapter(categoryAdapter);
    }
    
    /*
     * databaseUpdated
     * Retour de la fonction de mise � jour de la base de donn�es
     */
    public void databaseUpdated()
    {
    	// On raffraichit les listes
    	refreshData();
    }
    
    /*
     * isUpToDateResult
     * Retour de la fonction de contr�le de la baes de donn�nes
     */
    public void isUpToDateResult(boolean upToDate) {
    	// On prepare un avertissement pour l'utilisateur
    	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int which) {
    	        switch (which){
	    	        case DialogInterface.BUTTON_POSITIVE:
	    	        	// Mettre � jour la bdd
	    	            Tools.database.updateDatabase(Main.this);
	    	            break;
	
	    	        case DialogInterface.BUTTON_NEGATIVE:
	    	        	// Afficher les r�sultats sans mise � jour
	    	            refreshData();
	    	            break;
    	        }
    	    }
    	};
    	
    	// Si la base est � jour, on affiche les listes
    	if (upToDate) {
    		refreshData();
    	} else {
    		// Sinon on demande � l'utilisateur s'il souhaite mettre � jour la base de donn�es
    		AlertDialog.Builder alert = new AlertDialog.Builder(this);
    		alert.setMessage(getString(R.string.alert_database_nottodate_text));
    		alert.setPositiveButton(getString(R.string.alert_database_nottodate_yes), dialogClickListener);
    		alert.setNegativeButton(getString(R.string.alert_database_nottodate_no), dialogClickListener);
    		alert.show();
    	}
    }
    
    public void exportCSV(){
        this.context = this;
        //Cursor data = Tools.database.database.query("application", new String[]{"label", "name", "version_code", "version_name", "system"}, "id = ?", new String[]{applicationId}, null, null, null);
        //Cursor data = Tools.database.database.query("application", new String[]{"name"}, null, null, null, null, null);
        //if (data.getCount() == 1) {
        	Log.i(TAG,"after if");
        	//data.moveToFirst();
        	
        	//packageName = data.getString(1);
        	
        	// Affichage du nom de l'application, du package et de la version
        	/*((TextView)findViewById(R.id.application_detail_label)).setText(data.getString(0));
        	((TextView)findViewById(R.id.application_detail_name)).setText(data.getString(1));
        	((TextView)findViewById(R.id.application_detail_version)).setText(data.getString(2) + " / " + data.getString(3));
        	
        	if (data.getInt(4) == 1)
        		((TextView)findViewById(R.id.application_detail_system)).setVisibility(View.VISIBLE);
        	else
        		((TextView)findViewById(R.id.application_detail_system)).setVisibility(View.GONE);
        	*/
        	// R�cup�ration du nombre de permissions utilis�es et affichage
        	/*data = Tools.database.database.rawQuery("SELECT Count(*) AS number " +
        											"FROM relation_application_permission " +
        											"WHERE application = ?;", new String[]{applicationId});
        	data.moveToFirst();
        	((TextView)findViewById(R.id.application_detail_permission_count)).setText(data.getString(0));
        	*/
        	// R�cup�ration des permissions et cr�ation de la liste
        	//Cursor permissionListCursor = Tools.database.database.rawQuery("SELECT permission.id AS _id, permission.name AS name FROM relation_application_permission INNER JOIN permission ON relation_application_permission.permission = permission.id WHERE relation_application_permission.application = ? ORDER BY permission.name COLLATE NOCASE ASC;", new String[] {applicationId});
        	Cursor permissionListCursor = Tools.database.database.rawQuery("SELECT permission.id AS _id, application.label as appName, permission.name AS name FROM relation_application_permission INNER JOIN permission ON relation_application_permission.permission = permission.id INNER JOIN application ON relation_application_permission.application = application.id WHERE application.system = 0 OR application.system = 1 ORDER BY appName COLLATE NOCASE ASC;", null);
        	startManagingCursor(permissionListCursor);
        	//startManagingCursor(permissionListCursorAll);
        	//ListAdapter permissionAdapter = new SimpleCursorAdapter(this, R.layout.permission_list_item, permissionListCursor, new String[] {"name"}, new int[]{R.id.listviewpermissiontext});
        	//permissionList = (ListView)findViewById(R.id.application_detail_permission_list);
        	//permissionList.setAdapter(permissionAdapter);
        	
        	//create Map to store app and its permission
        	Map<String, List<String>> multimapPermission = new HashMap<String, List<String>>();
        	String prevKey = new String();
        	String key = new String();
        	List<String> permission = new ArrayList<String>();
        	int count = 0;
        	
			permissionListCursor.moveToFirst();
			while(!permissionListCursor.isAfterLast()) {
				//<begin for csv> 
				String thePermission = "\"" + permissionListCursor.getString(permissionListCursor.getColumnIndex("appName")) + "\",\"" + permissionListCursor.getString(permissionListCursor.getColumnIndex("name")) + "\"";
			    mArrayList.add(thePermission); //add the item
			    //<end for csv>
			    
			    //<begin for calculation>
			    key = permissionListCursor.getString(permissionListCursor.getColumnIndex("appName"));
			    key = key.replace("'", "");
			    
			    if (!prevKey.equals(key)){
			    	if(count > 0){
			    		multimapPermission.put(prevKey, permission);
			    	}
			    	prevKey = key;
			    	count++;
			    	permission = new ArrayList<String>();
			    }
	        	permission.add(permissionListCursor.getString(permissionListCursor.getColumnIndex("name")));
			    //<end for calculation>
			    
	        	permissionListCursor.moveToNext();
			}
			//permissionListCursor.moveToFirst();
			//Log.i(TAG,"move to first");
			/*while(!permissionListCursor.isAfterLast()) {
				 String thePermission = "\"" + permissionListCursor.getString(permissionListCursor.getColumnIndex("appName")) + "\",\"" + permissionListCursor.getString(permissionListCursor.getColumnIndex("name")) + "\"";
			     mArrayList.add(thePermission); //add the item
			     permissionListCursor.moveToNext();
			}*/
			countRisk(multimapPermission);
			writeCSV(mArrayList);
			permissionListCursor.close();
			
//			mchart.getYAxis().setAxisMaxValue(5f);
//			mchart.getYAxis().setLabelCount(6, true);
			
        // Fermeture de l'acc�s a la base de donnees
        //data.close();
    }
    
    public void countRisk(Map<String,List<String>> multiMapPermission){
    	//Map<String, List<Integer>> multimapRisk = new HashMap<String, List<Integer>>();
    	//List<Integer> risk;
    	
//    	List<List<String>> riskCategory = new ArrayList<List<String>>();
//    	List<String> categoryLevel = new ArrayList<String>();
//    	for(int i=0; i<6;i++){
//    		categoryLevel.add(Integer.toString(i));
//    	}
//    	for(int i = 0; i< 11; i++){
//    		riskCategory.add(categoryLevel);
//    	}
    	
    	
    	for(String appName: multiMapPermission.keySet()){
    		//risk = new ArrayList<Integer>();
            List<String> permissions = multiMapPermission.get(appName);
            int riskLevel = 0;
            
            //Account Informations Risk
            if(permissions.contains("GET_ACCOUNTS") || permissions.contains("USE CREDENTIALS")){
            	riskLevel = 1;
            	if(permissions.contains("GET_ACCOUNTS") && permissions.contains("USE CREDENTIALS")){
            		riskLevel = 2;
                }
            	if ((permissions.contains("BLUETOOTH") || permissions.contains("NFC"))){
            		riskLevel = 3;
            		if ((permissions.contains("INTERNET") || permissions.contains("SEND_SMS"))){
            			riskLevel = 5;
                	}
            	} else if ((permissions.contains("INTERNET") || permissions.contains("SEND_SMS"))){
            		riskLevel = 4;
            	}
            }  
           // risk.add(riskLevel);
            if(!isRiskDetailExist(0, riskLevel, appName)){
            	addToIgnoreList(0,riskLevel,appName,0);
            }
            riskLevel = 0;
//            System.out.println("Account Risk = " + riskLevel + " for " + appName);
            
            //Browser Risk
            if(permissions.contains("READ_HISTORY_BOOKMARKS")){
            	riskLevel = 2;
            	if ((permissions.contains("BLUETOOTH") || permissions.contains("NFC"))){
            		riskLevel = 3;
            		if ((permissions.contains("INTERNET") || permissions.contains("SEND_SMS"))){
            			riskLevel = 5;
                	}
            	} else if ((permissions.contains("INTERNET") || permissions.contains("SEND_SMS"))){
            		riskLevel = 4;
            	}
            } 
            //risk.add(riskLevel);
            if(!isRiskDetailExist(1, riskLevel, appName)){
            	addToIgnoreList(1,riskLevel,appName,0);
            }
            riskLevel = 0;
            
            //Calendar Risk
            if(permissions.contains("READ_CALENDAR")){
            	riskLevel = 2;
            	if ((permissions.contains("BLUETOOTH") || permissions.contains("NFC"))){
            		riskLevel = 3;
            		if ((permissions.contains("INTERNET") || permissions.contains("SEND_SMS"))){
            			riskLevel = 5;
                	}
            	} else if ((permissions.contains("INTERNET") || permissions.contains("SEND_SMS"))){
            		riskLevel = 4;
            	}
            } 
            //risk.add(riskLevel);
            if(!isRiskDetailExist(2, riskLevel, appName)){
            	addToIgnoreList(2,riskLevel,appName,0);
            }
            riskLevel = 0;
            
            //Calling Risk
            if(permissions.contains("READ_CALL_LOG")){
            	riskLevel = 2;
            	if ((permissions.contains("BLUETOOTH") || permissions.contains("NFC"))){
            		riskLevel = 3;
            		if ((permissions.contains("INTERNET") || permissions.contains("SEND_SMS"))){
            			riskLevel = 5;
                	}
            	} else if ((permissions.contains("INTERNET") || permissions.contains("SEND_SMS"))){
            		riskLevel = 4;
            	}
            } 
          //  risk.add(riskLevel);
            if(!isRiskDetailExist(3, riskLevel, appName)){
            	addToIgnoreList(3,riskLevel,appName,0);
            }
            riskLevel = 0;
            
            //Contact Risk
            if(permissions.contains("GET_CONTACTS") || permissions.contains("READ_PROFILE")){
            	riskLevel = 1;
            	if(permissions.contains("GET_CONTACTS") && permissions.contains("READ_PROFILE")){
            		riskLevel = 2;
                }
            	if ((permissions.contains("BLUETOOTH") || permissions.contains("NFC"))){
            		riskLevel = 3;
            		if ((permissions.contains("INTERNET") || permissions.contains("SEND_SMS"))){
            			riskLevel = 5;
                	}
            	} else if ((permissions.contains("INTERNET") || permissions.contains("SEND_SMS"))){
            		riskLevel = 4;
            	}
            }  
          //  risk.add(riskLevel);
            if(!isRiskDetailExist(4, riskLevel, appName)){
            	addToIgnoreList(4,riskLevel,appName,0);
            }
            riskLevel = 0;
//            System.out.println("Contact Risk= " + riskLevel + " for " + appName);
            
            //Location Risk
            if(permissions.contains("ACCESS_FINE_LOCATION") || permissions.contains("ACCESS_COARSE_LOCATION")){
            	riskLevel = 1;
            	if(permissions.contains("ACCESS_FINE_LOCATION") && permissions.contains("ACCESS_COARSE_LOCATION")){
            		riskLevel = 2;
                }
            	if ((permissions.contains("BLUETOOTH") || permissions.contains("NFC"))){
            		riskLevel = 3;
            		if ((permissions.contains("INTERNET") || permissions.contains("SEND_SMS"))){
            			riskLevel = 5;
                	}
            	} else if ((permissions.contains("INTERNET") || permissions.contains("SEND_SMS"))){
            		riskLevel = 4;
            	}
            }
            //risk.add(riskLevel);
            if(!isRiskDetailExist(5, riskLevel, appName)){
            	addToIgnoreList(5,riskLevel,appName,0);
            }
            riskLevel = 0;
            
            //Media Risk
            if(permissions.contains("CAMERA") || permissions.contains("RECORD_AUDIO")){
            	riskLevel = 1;
            	if(permissions.contains("CAMERA") && permissions.contains("RECORD_AUDIO")){
            		riskLevel = 2;
                }
            	if ((permissions.contains("BLUETOOTH") || permissions.contains("NFC"))){
            		riskLevel = 3;
            		if ((permissions.contains("INTERNET") || permissions.contains("SEND_SMS"))){
            			riskLevel = 5;
                	}
            	} else if ((permissions.contains("INTERNET") || permissions.contains("SEND_SMS"))){
            		riskLevel = 4;
            	}
            }
          //  risk.add(riskLevel);
            if(!isRiskDetailExist(6, riskLevel, appName)){
            	addToIgnoreList(6,riskLevel,appName,0);
            }
            riskLevel = 0;
            
            //Message Risk
            if(permissions.contains("READ_SMS") || permissions.contains("READ_VOICE_MAIL")){
            	riskLevel = 1;
            	if(permissions.contains("READ_SMS") && permissions.contains("READ_VOICE_MAIL")){
            		riskLevel = 2;
            	}
                if ((permissions.contains("BLUETOOTH") || permissions.contains("NFC"))){
                	riskLevel = 3;
                	if ((permissions.contains("INTERNET") || permissions.contains("SEND_SMS"))){
            			riskLevel = 5;
                	}
                }else if ((permissions.contains("INTERNET") || permissions.contains("SEND_SMS"))){
            		riskLevel = 4;
                }
            }
            if(permissions.contains("RECEIVE_SMS") || permissions.contains("RECEIVE_MMS") || permissions.contains("RECEIVE_WAP_PUSH")){
            	riskLevel = 2;
            	if ((permissions.contains("BLUETOOTH") || permissions.contains("NFC"))){
                	riskLevel = 3;
            		if ((permissions.contains("INTERNET") || permissions.contains("SEND_SMS"))){
            			riskLevel = 5;
            		}
            		}else if ((permissions.contains("INTERNET") || permissions.contains("SEND_SMS"))){
            		riskLevel = 4;
            	}
            }
    
          //  risk.add(riskLevel);
            if(!isRiskDetailExist(7, riskLevel, appName)){
            	addToIgnoreList(7,riskLevel,appName,0);
            }
            riskLevel = 0;
            
            //Network Risk
            if(permissions.contains("ACCESS_NETWORK_STATE") || permissions.contains("ACCESS_WIFI_STATE")){
            	riskLevel = 1;
            	if(permissions.contains("ACCESS_NETWORK_STATE") && permissions.contains("ACCESS_WIFI_STATE")){
            		riskLevel = 2;
                }
            	if ((permissions.contains("BLUETOOTH") || permissions.contains("NFC"))){
            		riskLevel = 3;
            		if ((permissions.contains("INTERNET") || permissions.contains("SEND_SMS"))){
            			riskLevel = 5;
                	}
            	} else if ((permissions.contains("INTERNET") || permissions.contains("SEND_SMS"))){
            		riskLevel = 4;
            	}
            }
           // risk.add(riskLevel);
            if(!isRiskDetailExist(8, riskLevel, appName)){
            	addToIgnoreList(8,riskLevel,appName,0);
            }
            riskLevel = 0;
            
            //Phone Risk
            if(permissions.contains("READ_PHONE_STATE")){
            	riskLevel = 2;
            	if ((permissions.contains("BLUETOOTH") || permissions.contains("NFC"))){
            		riskLevel = 3;
            		if ((permissions.contains("INTERNET") || permissions.contains("SEND_SMS"))){
            			riskLevel = 5;
                	}
            	} else if ((permissions.contains("INTERNET") || permissions.contains("SEND_SMS"))){
            		riskLevel = 4;
            	}
            } 
           // risk.add(riskLevel);
            if(!isRiskDetailExist(9, riskLevel, appName)){
            	addToIgnoreList(9,riskLevel,appName,0);
            }
            riskLevel = 0;
            
            //External Risk
            if(permissions.contains("READ_EXTERNAL_STORAGE") || permissions.contains("MANAGE_DOCUMENTS")){
            	riskLevel = 1;
            	if(permissions.contains("READ_EXTERNAL_STORAGE") && permissions.contains("MANAGE_DOCUMENTS")){
            		riskLevel = 2;
                }
            	if ((permissions.contains("BLUETOOTH") || permissions.contains("NFC"))){
            		riskLevel = 3;
            		if ((permissions.contains("INTERNET") || permissions.contains("SEND_SMS"))){
            			riskLevel = 5;
                	}
            	} else if ((permissions.contains("INTERNET") || permissions.contains("SEND_SMS"))){
            		riskLevel = 4;
            	}
            }
         //   risk.add(riskLevel);
            if(!isRiskDetailExist(10, riskLevel, appName)){
            	addToIgnoreList(10,riskLevel,appName,0);
            }
            
          //  multimapRisk.put(appName, risk);
        }
    	
    	//get risk detail 0-5 for each category
    	//getRiskDetail(multimapRisk);
    	getRiskDetail(0);
    	
    }
    
    protected boolean isRiskDetailExist(int _categoryCode, int _levelCode, String _appName){
    	String query = "SELECT * FROM ignore_list WHERE categoryCode= "+ _categoryCode + " AND levelCode= "+ _levelCode + " AND appName= '"+ _appName +"'";
    	Cursor applicationCursor = Tools.database.database.rawQuery(query, null);
    	startManagingCursor(applicationCursor);
        boolean isExist = applicationCursor.getCount()>0;
        applicationCursor.close();
    	if(isExist){
    		return true;
    	}
//    	riskDetailCursor.moveToFirst();
//    	while(!riskDetailCursor.isAfterLast()) {
//    		
//    		riskDetailCursor.moveToNext();
//    	}
    	return false;
    }
    
    protected void addToIgnoreList(int _categoryCode, int _levelCode, String _appName, int _ignore){
    	SQLiteDatabase db;

        db = openOrCreateDatabase(
            "application_permission.db"
            , SQLiteDatabase.CREATE_IF_NECESSARY
            , null
            );
        
        ContentValues insertValues = new ContentValues();
        insertValues.put("categoryCode", _categoryCode);
        insertValues.put("levelCode", _levelCode);
        insertValues.put("appName", _appName);
        insertValues.put("isIgnore", _ignore);
        db.insert("ignore_list", null, insertValues);
        db.close();
    }
    
    protected void getRiskDetail(int ignoredCode){
//    	List<List<List<String>>> riskListDetail = new ArrayList<List<List<String>>>();
//    	List<List<String>> riskLevelDetail;
//    	List<String> appNameDetail;
//    	StringBuilder sbAppList = new StringBuilder();
    	
    	
    	
//    	for(int i=0;i<11;i++){
//    		riskLevelDetail = new ArrayList<List<String>>();
//    		for(int j=0; j<6; j++){
//    			appNameDetail = new ArrayList<String>();
//    			for(String appName: multimapRisk.keySet()){
//    				List<Integer> riskList = multimapRisk.get(appName);
//	    			if(riskList.get(i)==j){
//						appNameDetail.add(appName);
//					}
//	    		}	
//    			riskLevelDetail.add(appNameDetail);
//			}
//    		riskListDetail.add(riskLevelDetail);
//    	}
    	
    	//data prep for chart
    	
    	mchart.setDescription("");

    	mchart.setWebLineWidth(1.5f);
    	mchart.setWebLineWidthInner(0.75f);
    	mchart.setWebAlpha(100);
    	
    	 // create a custom MarkerView (extend MarkerView) and specify the layout
        // to use for it
        MyMarkerView mv = new MyMarkerView(this, R.layout.custom_marker_view);
        
        // set the marker to the chart
        mchart.setMarkerView(mv);
    	    	
    	ArrayList<String> xVals = new ArrayList<String>();
    	ArrayList<Entry> yVals1 = new ArrayList<Entry>();
    	
    	xVals.add("Account Info");
    	xVals.add("Browser History & Bookmarks");
    	xVals.add("Calendar Info");
    	xVals.add("Calling Info");
    	xVals.add("Contact & Profile");
    	xVals.add("Location");
    	xVals.add("Media");
    	xVals.add("Message");
    	xVals.add("Network Info");
    	xVals.add("Phone Info");
    	xVals.add("External Storage Data");
    	
    	Map<Integer, Integer> categoryRiskLevel = new HashMap<Integer, Integer>();
    	//get yVals1
    	String query = "SELECT MAX(levelCode) as theLevelCode, categoryCode FROM ignore_list WHERE isIgnore = " + ignoredCode + " GROUP BY categoryCode";
    	Cursor riskDetailCursor = Tools.database.database.rawQuery(query, null);
    	startManagingCursor(riskDetailCursor);
    	riskDetailCursor.moveToFirst();
    	while(!riskDetailCursor.isAfterLast()) {
    		int levelCode = riskDetailCursor.getInt(riskDetailCursor.getColumnIndex("theLevelCode"));
    		int categoryCode = riskDetailCursor.getInt(riskDetailCursor.getColumnIndex("categoryCode"));
    		categoryRiskLevel.put(categoryCode, levelCode);
    		yVals1.add(new Entry(levelCode,categoryCode));
    		riskDetailCursor.moveToNext();
    	}
    	int checkBoxId = 1000;
    	for(int categoryCodeDetail: categoryRiskLevel.keySet()){
    		int levelCodeDetail = categoryRiskLevel.get(categoryCodeDetail);
    		String riskinfo="blank";
    		String sencat;
	    	switch(categoryCodeDetail) {
			    case 0:
			        sencat = "Account Information Risk";
					if (levelCodeDetail==5){
						riskinfo="Aplikasi berikut dapat menyalahgunakan request authtokens dari Account Manager atau membocorkan daftar akun pada Account Service melalui sumber daya konektivitas jarak jauh maupun jarak dekat :";
					}else if(levelCodeDetail==4){
						riskinfo="Aplikasi berikut dapat menyalahgunakan request authtokens dari Account Manager atau membocorkan daftar akun pada Account Service melalui sumber daya konektivitas jarak jauh :";
					}else if(levelCodeDetail==3){
						riskinfo="Aplikasi berikut dapat menyalahgunakan request authtokens dari Account Manager atau membocorkan daftar akun pada Account Service melalui sumber daya konektivitas jarak dekat :";
					}else if(levelCodeDetail==2){
						riskinfo="Aplikasi berikut dapat membaca daftar akun dan melakukan request authtokens :";
					}else if(levelCodeDetail==1){
						riskinfo="Aplikasi berikut hanya dapat membaca daftar akun atau melakukan request authtokens :";
					}
			        break;
			    case 1:
			    	sencat = "Browser History and Bookmarks Risk";
			    	if (levelCodeDetail==5){
						riskinfo="Aplikasi berikut dapat membocorkan daftar riwayat dan bookmark dari browser pengguna melalui semua sumber daya konektivitas jarak jauh maupun jarak dekat :";
					}else if(levelCodeDetail==4){
						riskinfo="Aplikasi berikut dapat membocorkan daftar riwayat dan bookmark dari browser pengguna melalui sumber daya konektivitas jarak jauh :";
					}else if(levelCodeDetail==3){
						riskinfo="Aplikasi berikut dapat membocorkan daftar riwayat dan bookmark dari browser pengguna melalui sumber daya konektivitas jarak dekat :";
					}else if(levelCodeDetail==2){
						riskinfo="Aplikasi berikut hanya dapat membaca daftar riwayat dan bookmark dari browser pengguna :";
					}
			        break;
			    case 2:
			    	sencat = "Calendar Information Risk";
			    	if (levelCodeDetail==5){
						riskinfo="Aplikasi berikut dapat membocorkan seluruh data kalender melalui semua sumber daya konektivitas jarak jauh maupun jarak dekat :";
					}else if(levelCodeDetail==4){
						riskinfo="Aplikasi berikut dapat membocorkan data kalender melalui sumber daya konektivitas jarak jauh :";
					}else if(levelCodeDetail==3){
						riskinfo="Aplikasi berikut dapat membocorkan data kalender melalui sumber daya konektivitas jarak dekat :";
					}else if(levelCodeDetail==2){
						riskinfo="Aplikasi berikut hanya dapat membaca data kalender pengguna :";
					}
			        break;
			    case 3:
			    	sencat = "Calling Information Risk";
			    	if (levelCodeDetail==5){
						riskinfo="Aplikasi berikut dapat membocorkan seluruh informasi mengenai panggilan masuk dan keluar melalui semua sumber daya konektivitas jarak jauh maupun jarak dekat :";
					}else if(levelCodeDetail==4){
						riskinfo="Aplikasi berikut dapat membocorkan seluruh informasi mengenai panggilan masuk dan keluar melalui sumber daya konektivitas jarak jauh :";
					}else if(levelCodeDetail==3){
						riskinfo="Aplikasi berikut dapat membocorkan seluruh informasi mengenai panggilan masuk dan keluar melalui sumber daya konektivitas jarak dekat :";
					}else if(levelCodeDetail==2){
						riskinfo="Aplikasi berikut hanya dapat membaca seluruh informasi mengenai panggilan masuk dan keluar :";
					}
			        break;
			    case 4:
			    	sencat = "Contacts and Profile Risk";
			    	if (levelCodeDetail==5){
						riskinfo="Aplikasi berikut dapat membocorkan seluruh daftar contact atau profile pengguna melalui semua sumber daya konektivitas jarak jauh maupun jarak dekat :";
					}else if(levelCodeDetail==4){
						riskinfo="Aplikasi berikut dapat membocorkan seluruh daftar contact atau profile pengguna melalui sumber daya konektivitas jarak jauh :";
					}else if(levelCodeDetail==3){
						riskinfo="Aplikasi berikut dapat membocorkan seluruh daftar contact atau profile pengguna melalui sumber daya konektivitas jarak dekat :";
					}else if(levelCodeDetail==2){
						riskinfo="Aplikasi berikut dapat membaca daftar contact dan profile pengguna:";
					}else if(levelCodeDetail==1){
						riskinfo="Aplikasi berikut hanya dapat membaca daftar contact atau profile pengguna :";
					}
			        break;
			    case 5:
			    	sencat = "Location Risk";
			    	if (levelCodeDetail==5){
						riskinfo="Aplikasi berikut dapat membocorkan seluruh lokasi akurat atau lokasi kasar pengguna melalui semua sumber daya konektivitas jarak jauh maupun jarak dekat :";
					}else if(levelCodeDetail==4){
						riskinfo="Aplikasi berikut dapat membocorkan seluruh lokasi akurat atau lokasi kasar pengguna melalui sumber daya konektivitas jarak jauh :";
					}else if(levelCodeDetail==3){
						riskinfo="Aplikasi berikut dapat membocorkan seluruh lokasi akurat atau lokasi kasar pengguna melalui sumber daya konektivitas jarak dekat :";
					}else if(levelCodeDetail==2){
						riskinfo="Aplikasi berikut dapat mengakses data lokasi akurat dan lokasi kasar pengguna :";
					}else if(levelCodeDetail==1){
						riskinfo="Aplikasi berikut dapat mengakses data lokasi akurat atau lokasi kasar pengguna :";
					}
			        break;
			    case 6:
			    	sencat = "Media Risk";
			    	if (levelCodeDetail==5){
						riskinfo="Aplikasi berikut dapat membocorkan seluruh foto, video, atau rekaman suara pengguna melalui semua sumber daya konektivitas jarak jauh maupun jarak dekat :";
					}else if(levelCodeDetail==4){
						riskinfo="Aplikasi berikut dapat membocorkan seluruh foto, video, atau rekaman suara pengguna melalui sumber daya konektivitas jarak jauh :";
					}else if(levelCodeDetail==3){
						riskinfo="Aplikasi berikut dapat membocorkan seluruh foto, video, atau rekaman suara pengguna melalui sumber daya konektivitas jarak dekat :";
					}else if(levelCodeDetail==2){
						riskinfo="Aplikasi berikut dapat mengambil foto / video dan merekam suara pengguna :";
					}else if(levelCodeDetail==1){
						riskinfo="Aplikasi berikut hanya dapat mengambil foto / video atau merekam suara pengguna :";
					}
			        break;
			    case 7:
			    	sencat = "Messages Risk";
			    	if (levelCodeDetail==5){
						riskinfo="Aplikasi berikut dapat membocorkan lebih dari 1 jenis pesan (SMS, MMS, voice mail) pengguna melalui semua sumber daya konektivitas jarak jauh maupun jarak dekat :";
					}else if(levelCodeDetail==4){
						riskinfo="Aplikasi berikut dapat membocorkan SMS, MMS, atau voice mail pengguna melalui sumber daya konektivitas jarak jauh :";
					}else if(levelCodeDetail==3){
						riskinfo="Aplikasi berikut dapat membocorkan SMS, MMS, atau voice mail pengguna melalui sumber daya konektivitas jarak dekat :";
					}else if(levelCodeDetail==2){
						riskinfo="Aplikasi berikut dapat melakukan monitoring terhadap pesan SMS / MMS / voice mail yang masuk, merekam, atau memprosesnya :";
					}else if(levelCodeDetail==1){
						riskinfo="Aplikasi berikut dapat membaca pesan SMS / vouce mail :";
					}
			        break;
			    case 8:
			    	sencat = "Network Information Risk";
			    	if (levelCodeDetail==5){
						riskinfo="Aplikasi berikut dapat membocorkan informasi tentang jaringan atau Wi-Fi yang sedang digunakan melalui semua sumber daya konektivitas jarak jauh maupun jarak dekat :";
					}else if(levelCodeDetail==4){
						riskinfo="Aplikasi berikut dapat membocorkan informasi tentang jaringan atau Wi-Fi yang sedang digunakan melalui sumber daya konektivitas jarak jauh :";
					}else if(levelCodeDetail==3){
						riskinfo="Aplikasi berikut dapat membocorkan informasi tentang jaringan atau Wi-Fi yang sedang digunakan melalui sumber daya konektivitas jarak dekat :";
					}else if(levelCodeDetail==2){
						riskinfo="Aplikasi berikut dapat mengakses semua informasi tentang jaringan atau Wi-Fi yang sedang digunakan :";
					}else if(levelCodeDetail==1){
						riskinfo="Aplikasi berikut hanya dapat mengakses informasi tentang jaringan atau Wi-Fi yang sedang digunakan :";
					}
			        break;
			    case 9:
			    	sencat = "Phone Information Risk";
			    	if (levelCodeDetail==5){
						riskinfo="Aplikasi berikut dapat membocorkan seluruh informasi mengenai status perangkat melalui semua sumber daya konektivitas jarak jauh maupun jarak dekat :";
					}else if(levelCodeDetail==4){
						riskinfo="Aplikasi berikut dapat membocorkan seluruh informasi mengenai status perangkat melalui sumber daya konektivitas jarak jauh :";
					}else if(levelCodeDetail==3){
						riskinfo="Aplikasi berikut dapat membocorkan seluruh informasi mengenai status perangkat melalui sumber daya konektivitas jarak dekat :";
					}else if(levelCodeDetail==2){
						riskinfo="Aplikasi berikut hanya dapat membaca seluruh informasi mengenai status perangkat :";
					}
			        break;
			    case 10:
			    	sencat = "External Storage Data Risk";
			    	if (levelCodeDetail==5){
						riskinfo="Aplikasi berikut dapat membocorkan seluruh berkas pada memori eksternal atau melakukan akses ke dokumen melalui semua sumber daya konektivitas jarak jauh maupun jarak dekat :";
					}else if(levelCodeDetail==4){
						riskinfo="Aplikasi berikut dapat membocorkan seluruh berkas pada memori eksternal atau melakukan akses ke dokumen  melalui sumber daya konektivitas jarak jauh :";
					}else if(levelCodeDetail==3){
						riskinfo="Aplikasi berikut dapat membocorkan seluruh berkas pada memori eksternal atau melakukan akses ke dokumen melalui sumber daya konektivitas jarak dekat :";
					}else if(levelCodeDetail==2){
						riskinfo="Aplikasi berikut dapat mengakses seluruh berkas pada memori eksternal dan melakukan akses ke dokumen :";
					}else if(levelCodeDetail==1){
						riskinfo="Aplikasi berikut hanya dapat mengakses seluruh berkas pada memori eksternal atau melakukan akses ke dokumen :";
					}
			        break;
			    default:
			        sencat = "category?";
				}
	    	TextView tv = new TextView(this);
			tv.setText(" \n"+sencat+" = "+levelCodeDetail);
			tv.setTypeface(null, Typeface.BOLD);
			ll.addView(tv);
			
			
	    	
			if (levelCodeDetail!=0){
				TextView tvinfo = new TextView(this);
				tvinfo.setText(riskinfo);
//				tvinfo.setFilters( new InputFilter[] { new InputFilter.LengthFilter(1000)});
				ll.addView(tvinfo);
				
	    		query = "SELECT appName FROM ignore_list WHERE isIgnore = " + ignoredCode + " AND categoryCode = " + categoryCodeDetail + " AND levelCode = " + levelCodeDetail ;
	        	Cursor categoryRiskCursor = Tools.database.database.rawQuery(query, null);
	        	startManagingCursor(categoryRiskCursor);
	        	categoryRiskCursor.moveToFirst();
	        	
	        	while(!categoryRiskCursor.isAfterLast()) {
	        		CheckBox cb = new CheckBox(this);
	        		String appName = categoryRiskCursor.getString(categoryRiskCursor.getColumnIndex("appName"));
	                cb.setText(appName);
	               // cb.setPadding(0, 0, 0, 0);
	                cb.setId(checkBoxId);
	                cb.setTag(appName);
	                cb.setHint(Integer.toString(levelCodeDetail));
	                checkBoxId++;
	                cb.setScaleX(0.80f);
	                cb.setScaleY(0.80f);
	                cb.setOnCheckedChangeListener(handleCheck(cb));
	                ll.addView(cb);
	        		categoryRiskCursor.moveToNext();
	        	}
	        	checkBoxId = (int) (Math.floor(checkBoxId/1000)+1)*1000;
	        	
	        	Button btnSubmit = new Button(this);
				btnSubmit.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
				btnSubmit.setText("Add to Ignore List");
				btnSubmit.setTag(levelCodeDetail);
				btnSubmit.setId(categoryCodeDetail);
				btnSubmit.setOnClickListener(updateIgnoreList(checkedIgnoreList));
		        ll.addView(btnSubmit);
			} else {
				checkBoxId = (int) (Math.floor(checkBoxId/1000)+1)*1000;
				TextView tv2 = new TextView(this);
				tv2.setText("Semua aplikasi tidak memiliki permission yang terkait dengan kategori risiko ini.");
				ll.addView(tv2);
			}	
    	}
    	
    	
    	
//    	List<List<String>> tempRiskLevelDetail = new ArrayList<List<String>>();
//    	List<String> tempAppNameDetail = new ArrayList<String>();
//    	String sencat;
//    	for(int i=0;i<11;i++){
//    		switch(i) {
//		    case 0:
//		        sencat = "Account Information Risk";
//		        break;
//		    case 1:
//		    	sencat = "Browser History and Bookmarks Risk";
//		        break;
//		    case 2:
//		    	sencat = "Calendar Information Risk";
//		        break;
//		    case 3:
//		    	sencat = "Calling Information Risk";
//		        break;
//		    case 4:
//		    	sencat = "Contacts and Profile Risk";
//		        break;
//		    case 5:
//		    	sencat = "Location Risk";
//		        break;
//		    case 6:
//		    	sencat = "Media Risk";
//		        break;
//		    case 7:
//		    	sencat = "Messages Risk";
//		        break;
//		    case 8:
//		    	sencat = "Network Information Risk";
//		        break;
//		    case 9:
//		    	sencat = "Phone Information Risk";
//		        break;
//		    case 10:
//		    	sencat = "External Storage Data Risk";
//		        break;
//		    default:
//		        sencat = "category?";
//			}
//    		sbAppList.append(sencat+" = ");
//    		
//    		for(int j=5;j>=0;j--){
//    			if(!isAppNameEmpty(riskListDetail,i,j)){
//    				//put your code here for graph
//    				TextView tv = new TextView(this);
//    				tv.setText(sencat+" = "+j);
//    				ll.addView(tv);
//    				
//    				sbAppList.append(j+".\n");
//    				yVals1.add(new Entry(j,i));
//    				tempRiskLevelDetail = new ArrayList<List<String>>();
//					tempRiskLevelDetail = riskListDetail.get(i);
//					tempAppNameDetail = new ArrayList<String>();
//					tempAppNameDetail = tempRiskLevelDetail.get(j);
//
//					if(j!=0){
//						for(String appName: tempAppNameDetail){
//							//sbAppList.append(appName+", ");
//							
//							CheckBox cb = new CheckBox(this);
//			                cb.setText(appName);
//			               // cb.setPadding(0, 0, 0, 0);
//			                cb.setScaleX(0.80f);
//			                cb.setScaleY(0.80f);
//			                
//			                ll.addView(cb);
//						}
//					}else{
//						TextView tv2 = new TextView(this);
//	    				tv2.setText("Semua aplikasi tidak memiliki permission yang terkait dengan kategori risiko ini.");
//	    				ll.addView(tv2);
//						//sbAppList.append("Semua aplikasi tidak memiliki permission yang terkait dengan kategori risiko ini.");
//					}
//					sbAppList.append("\n\n");
//    				break;
//    			}
//    		}
//    	}
//    	
//    	appList.setText(sbAppList);
    	
    	RadarDataSet set1 = new RadarDataSet(yVals1, "Risk Level");
        set1.setColor(ColorTemplate.VORDIPLOM_COLORS[4]);
        set1.setDrawFilled(true);
        set1.setLineWidth(2f);
        
        RadarData data = new RadarData(xVals, set1);
        data.setValueTypeface(tf);
        data.setValueTextSize(8f);
        data.setDrawValues(false);
        
        mchart.setData(data);
        mchart.invalidate();
        
        
        XAxis xAxis = mchart.getXAxis();
        xAxis.setTypeface(tf);
        xAxis.setTextSize(9f);

        YAxis yAxis = mchart.getYAxis();
        yAxis.setAxisMaxValue(5f);
        yAxis.setAxisMinValue(-1f);
        yAxis.setTypeface(tf);
        yAxis.setTextSize(9f);
        yAxis.setStartAtZero(false);
        yAxis.setLabelCount(7, true);
      
        mchart.notifyDataSetChanged();
		mchart.invalidate();
        
        Legend l = mchart.getLegend();
        l.setPosition(LegendPosition.RIGHT_OF_CHART);
        l.setTypeface(tf);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(5f);

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

	protected Boolean isAppNameEmpty(List<List<List<String>>> riskListDetail,int appRiskCategory, int riskLevel) {
		List<List<String>> riskLevelDetail;
		List<String> appNameDetail;
		riskLevelDetail = new ArrayList<List<String>>();
    	riskLevelDetail = riskListDetail.get(appRiskCategory);
    	appNameDetail = new ArrayList<String>();
    	appNameDetail = riskLevelDetail.get(riskLevel);
    	if(appNameDetail.isEmpty()){
    		System.out.println("ini kosong");
    		return true;
    	} else { 
	    	for(String appName: appNameDetail){
	    		System.out.println(appName);
	    	}
	    	return false;
    	}
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
		//Toast.makeText(getApplicationContext(), "CSV Created bla"+root.getAbsolutePath(), Toast.LENGTH_SHORT).show();
		if (root.canWrite()){
		    File dir    =   new File (root.getAbsolutePath());
		    Log.i("datax", root.getAbsolutePath().toString());
		     dir.mkdirs();
		     file   =   new File(dir, "permission.csv");
		     FileOutputStream out   =   null;
		     //Toast.makeText(getApplicationContext(), "CSV Created"+root.getAbsolutePath(), Toast.LENGTH_SHORT).show();
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

    
    
    private OnClickListener updateIgnoreList(final HashMap<Integer, String> checkedIgnoreList){
    	return new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				SQLiteDatabase db;
				int theCategoryCode = v.getId();
				int theLevelCode = Integer.parseInt(v.getTag().toString());

		        db = openOrCreateDatabase(
		            "application_permission.db"
		            , SQLiteDatabase.CREATE_IF_NECESSARY
		            , null
		            );
		        
		        ContentValues data=new ContentValues();
		        
		        for(Integer checkBoxId: checkedIgnoreList.keySet()){
		    		if((firstDigit(checkBoxId)-1)==theCategoryCode){
		    			String appName = checkedIgnoreList.get(checkBoxId);
		    			data.put("isIgnore",1);
		    			String whereClause = "categoryCode = " + theCategoryCode + " AND levelCode = " + theLevelCode + " AND appName = '" + appName +"'";
		    			db.update("ignore_list", data, whereClause, null);
		    		}
		        }
		        db.close();
		        //loading = ProgressDialog.show(Main.this, "","Loading...", true);
		        //finish();
		        //startActivity(getIntent());
		        loading = ProgressDialog.show(Main.this, "","Loading...", true);
		        Intent refresh = new Intent(Main.this, Main.class);
		        startActivity(refresh);
		        //finish(); //
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
    	finish();
   	moveTaskToBack(true);
    	
//    	   Intent intent = new Intent(Intent.ACTION_MAIN);
//    	   intent.addCategory(Intent.CATEGORY_HOME);
//    	   intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//    	   startActivity(intent);	   
    	return;
	}
    
    @Override
    public void onResume()
        {  // After a pause OR at startup
        super.onResume();
        
//        Intent refresh = new Intent(Main.this, Main.class);
//        refresh.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(refresh);
//        startActivity(refresh);
        //Refresh your stuff here
         }
    
    @Override
    public void onRestart() { 
        super.onRestart();
        Intent refresh = new Intent(Main.this, Main.class);
        startActivity(refresh);
        //When BACK BUTTON is pressed, the activity on the stack is restarted
        //Do what you want on the refresh procedure here
    }
}