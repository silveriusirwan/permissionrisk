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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

//import com.example.securityscanner.R;
//import com.example.securityscanner.MainActivity.LockType;





import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

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
	private List<String> data;
	private List<String> dataApplication;
	private List<String> dataWifi;
	
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
    	
    	// Chargement de l'interface graphique
        setContentView(R.layout.main);
        
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
		
		data = new ArrayList<String>();
		
		
		NfcAdapter nfcAdpt = NfcAdapter.getDefaultAdapter(this.getApplicationContext());	
		if(nfcAdpt!=null)
		{
			
		if(nfcAdpt.isEnabled())
			{
				nfcStatus.setText("NFC status = true");
			}
			else
			{
				nfcStatus.setText("NFC status = false");
			}
		} else nfcStatus.setText("NFC status = no NFC adapter");
		
		boolean rootstatus2 = isRooted();
		rootStatus.setText("Root status= "+String.valueOf(rootstatus2));
		
		//add new line
		data.add("Root Status");
		data.add(String.valueOf(rootstatus2));
		
		// Check for available NFC Adapter
        /*PackageManager pm = getPackageManager();
        if(pm.hasSystemFeature(PackageManager.FEATURE_NFC) && NfcAdapter.getDefaultAdapter(this) != null) {
        	nfcStatus.setText("NFC status = true");
        } else {
        	nfcStatus.setText("NFC status = false");
        }*/
		
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		//add new line
		data.add("Bluetooth Status");
		
		if (mBluetoothAdapter == null) {
			bluetoothStatus.setText("Bluetooth status = null");
		} else {
		    if (!mBluetoothAdapter.isEnabled()) {
		    	bluetoothStatus.setText("Bluetooth status = false");
		    	data.add("false");
		    }else{
		    	bluetoothStatus.setText("Bluetooth status = true");
		    	data.add("true");
		    }
		}
		
		int lockType = LockType.getCurrent(getContentResolver());
		String lockType2;
		switch(lockType) {
	    case 1:
	        lockType2 = "NONE or SLIDE";
	        break;
	    case 3:
	    	lockType2 = "FACE WITH PATTERN";
	        break;
	    case 4:
	    	lockType2 = "FACE WITH PIN";
	        break;
	    case 10:
	    	lockType2 = "PATTERN";
	        break;    
	    case 11:
	    	lockType2 = "PIN";
	        break;
	    case 12:
	    	lockType2 = "PASSWORD ALPHABETIC";
	        break;
	    case 13:
	    	lockType2 = "PASSWORD ALPHANUMERIC";
	        break;
	    default:
	    	lockType2 = "ERROR";
		}
		
		
		lockStatus.setText("Lock status= "+String.valueOf(lockType2));
		data.add("Lock screen status");
		data.add(String.valueOf(lockType2));
		
		boolean isNonPlayAppAllowed = isTrustUnknownSource();
		unknownStatus.setText("Unknown sources status= "+String.valueOf(isNonPlayAppAllowed));
		data.add("Unknown sources status");
		data.add(String.valueOf(isNonPlayAppAllowed));
		
		boolean encryptedStatus2 = isEncrypted(this.getApplicationContext());
		encryptedStatus.setText("Encrypted status= "+String.valueOf(encryptedStatus2));
		data.add("Phone Encryption status");
		data.add(String.valueOf(encryptedStatus2));
		
		boolean locationStatus2 = isGpsEnabled();
		locationStatus.setText("Location status= "+String.valueOf(locationStatus2));
		data.add("Location status");
		data.add(String.valueOf(locationStatus2));
		
		boolean simLockStatus2 = isSimPinRequired(getApplicationContext());
		simlockStatus.setText("Sim lock status= "+String.valueOf(simLockStatus2));
		data.add("Sim lock status");
		data.add(String.valueOf(simLockStatus2));
		
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
	
		
		StringBuilder strWifiStatus = new StringBuilder(); 
		dataWifi = new ArrayList<String>();
		WifiManager wifiManager=(WifiManager)getSystemService(Context.WIFI_SERVICE); 
		List<WifiConfiguration> networks=wifiManager.getConfiguredNetworks(); 
		strWifiStatus.append("Wifi Status:\n"); 
		for (WifiConfiguration config : networks) 
		{
			//Log.i("Wifi",config.SSID + " " +getSecurity(config)); 
			strWifiStatus.append(config.SSID+" ("+getSecurity(config)+")\n");
			dataWifi.add(config.SSID);
			dataWifi.add(getSecurity(config));
		} 
		wifiHistory.setText(strWifiStatus);
		
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
			Log.i("PackageMarket", marketName); 
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
        
        
        readResult = (TextView)findViewById(R.id.id_hello_world);
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
    			exportCSV(); //BN added to start exporting CSV
    			writeWifiCSV(dataWifi);
    			writeMarketCSV(dataApplication);
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
			Log.i(TAG,"move to first");
			/*while(!permissionListCursor.isAfterLast()) {
				 String thePermission = "\"" + permissionListCursor.getString(permissionListCursor.getColumnIndex("appName")) + "\",\"" + permissionListCursor.getString(permissionListCursor.getColumnIndex("name")) + "\"";
			     mArrayList.add(thePermission); //add the item
			     permissionListCursor.moveToNext();
			}*/
			countRisk(multimapPermission);
			writeCSV(mArrayList);
			permissionListCursor.close();
        
        // Fermeture de l'acc�s a la base de donnees
        //data.close();
    }
    
    public void countRisk(Map<String,List<String>> multiMapPermission){
    	Map<String, List<Integer>> multimapRisk = new HashMap<String, List<Integer>>();
    	List<Integer> risk;
    	
//    	List<List<String>> riskCategory = new ArrayList<List<String>>();
//    	List<String> categoryLevel = new ArrayList<String>();
//    	for(int i=0; i<6;i++){
//    		categoryLevel.add(Integer.toString(i));
//    	}
//    	for(int i = 0; i< 11; i++){
//    		riskCategory.add(categoryLevel);
//    	}
    	
    	
    	for(String appName: multiMapPermission.keySet()){
    		risk = new ArrayList<Integer>();
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
            risk.add(riskLevel);
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
            risk.add(riskLevel);
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
            risk.add(riskLevel);
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
            risk.add(riskLevel);
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
            risk.add(riskLevel);
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
            risk.add(riskLevel);
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
            risk.add(riskLevel);
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
    
            risk.add(riskLevel);
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
            risk.add(riskLevel);
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
            risk.add(riskLevel);
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
            risk.add(riskLevel);
            
            multimapRisk.put(appName, risk);
        }
    	
    	//get risk detail 0-5 for each category
    	getRiskDetail(multimapRisk);
    	
    }
    
    protected void getRiskDetail(Map<String, List<Integer>> multimapRisk){
    	List<List<List<String>>> riskListDetail = new ArrayList<List<List<String>>>();
    	List<List<String>> riskLevelDetail;
    	List<String> appNameDetail;
    	
    	for(int i=0;i<11;i++){
    		riskLevelDetail = new ArrayList<List<String>>();
    		for(int j=0; j<6; j++){
    			appNameDetail = new ArrayList<String>();
    			for(String appName: multimapRisk.keySet()){
    				List<Integer> riskList = multimapRisk.get(appName);
	    			if(riskList.get(i)==j){
						appNameDetail.add(appName);
					}
	    		}	
    			riskLevelDetail.add(appNameDetail);
			}
    		riskListDetail.add(riskLevelDetail);
    	}
    	
    	for(int i=0;i<11;i++){
    		for(int j=5;j>=0;j--){
    			if(!isAppNameEmpty(riskListDetail,i,j)){
    				//put your code here for graph
    				break;
    			}
    		}
    	}
    	
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