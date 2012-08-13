package org.fooshare;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;

public class RegistrationActivity extends FragmentActivity {

	private static final String TAG = "RegistrationActivity";
	protected FooshareApplication mFooshare;
	private RegistrationFragment mRegFragment = null;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.registration_activity);
        mFooshare = (FooshareApplication) getApplication();
        
        FragmentManager fm = getSupportFragmentManager();
        mRegFragment = (RegistrationFragment)fm.findFragmentById(R.id.fragment_reg_content); 
        
        if (mRegFragment == null) {

            FragmentTransaction ft = fm.beginTransaction();
            mRegFragment = new RegistrationFragment();
            ft.add(R.id.fragment_reg_content, mRegFragment);
            ft.commit();
        }
    }
	
	public void registrationDoneClicked(View view) {
		Log.d(TAG, "registrationDoneClicked is running");
	
		if (RegistrationActivity.this.mFooshare.storage().isRegistrationNeeded()) {
	
			AlertDialog.Builder popupBuilder = new AlertDialog.Builder(this);
			popupBuilder.setMessage("Some registration Data is missing");
			popupBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) { }
		       });
			popupBuilder.show();
	
		} else {
			RegistrationActivity.this.finish();
		}
	}
}
