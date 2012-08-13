package org.fooshare;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

public class SettingsActivity extends FragmentActivity {

	private static final String TAG = "SettingsActivity";
	protected FooshareApplication mFooshare;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.d(TAG, "onCreate is running");
        
        setContentView(R.layout.settings_activity);
        mFooshare = (FooshareApplication) getApplication();
        
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_reg_content); 

        if (fragment == null) {
            
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.fragment_reg_content, new RegistrationFragment());
            ft.commit();
        }
        
        
//        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
//			
//			@Override
//			public void onReceive(Context context, Intent intent) {
//				
//		        // set WiFi network name
//		        WifiManager wifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
//		        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
//		        String name = wifiInfo.getSSID();
//		        
//		        EditText editText = (EditText)findViewById(R.id.textViewSSID);
//		        editText.setText(name);
//			}
//		};
//		
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
//		registerReceiver(broadcastReceiver, intentFilter);

    }
    
}
