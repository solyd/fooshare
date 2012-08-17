package org.fooshare;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class SettingsActivity extends FragmentActivity {

	private static final String TAG = "SettingsActivity";
	protected FooshareApplication mFooshare;
	private BroadcastReceiver mBroadcastReceiver;
	private RegistrationFragment mRegFragment;
	
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
            mRegFragment = new RegistrationFragment();
            ft.add(R.id.fragment_reg_content, mRegFragment);
            ft.commit();
        }
        
        
        mBroadcastReceiver = new BroadcastReceiver() {			
			@Override
			public void onReceive(Context context, Intent intent) {
				
		        // set WiFi network name
		        WifiManager wifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
		        String name = wifiInfo.getSSID();
		        
		        if (name == null) {
		        	name = getResources().getString(R.string.WiFi_not_connected);
		        }
		        
		        TextView tvSSID = (TextView)findViewById(R.id.SSID_field);
		        tvSSID.setText(name);
			}
		};
		
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);

		registerReceiver(mBroadcastReceiver, intentFilter);
    }
    
    public void SharedFolderEntryRemove_OnClick(View view) {
    	mRegFragment.RemoveSharedFolder_OnClick(view);	
    }
}
