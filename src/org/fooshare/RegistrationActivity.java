package org.fooshare;

import org.fooshare.R;
import org.fooshare.R.id;
import org.fooshare.R.layout;
import org.fooshare.storage.IStorage.RegistrationItem;

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

    @Override
    public void onBackPressed() {
        // TODO
    }

	public void registrationDoneClicked(View view) {
		Log.d(TAG, "registrationDoneClicked is running");

		RegistrationItem missingItem = RegistrationActivity.this.mFooshare.storage().isRegistrationNeeded();
		if (missingItem != null) {
			AlertDialog.Builder popupBuilder = new AlertDialog.Builder(this);
			popupBuilder.setMessage(missingItem.toString());
			popupBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) { }
		       });
			popupBuilder.show();

		} else {
			RegistrationActivity.this.finish();
		}
	}

    public void SharedFolderEntryRemove_OnClick(View view) {
    	mRegFragment.RemoveSharedFolder_OnClick(view);
    }
}
