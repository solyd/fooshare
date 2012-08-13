package org.fooshare;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;


public class RegistrationActivity extends Activity {

	private static final String TAG = "RegistrationActivity";
	private static final int REQUEST_CODE_PICK_DIR = 101;
	private static final int REQUEST_CODE_PICK_UPLOAD_DIR = 102;
	private String mInitalPath = "/";


	protected FooshareApplication mFooshare;

	//list items for upload directories
	protected ArrayList<String> listItems;
	//string adapter to handle listview
	ArrayAdapter<String> adapter;

	private AlertDialog.Builder builder;


	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreat is running");
    	super.onCreate(savedInstanceState);

        setContentView(R.layout.registration_activity);

        builder = new AlertDialog.Builder(this);

        mFooshare = (FooshareApplication) getApplication();

        listItems = new ArrayList<String>(Arrays.asList(mFooshare.storage().getSharedDir()));
        adapter = new ArrayAdapter<String>(this, R.layout.small_list_text, listItems);
        ListView lv_sharedDir = (ListView)findViewById(R.id.listView_uDir);
        lv_sharedDir.setAdapter(adapter);
        lv_sharedDir.setClickable(true);
		lv_sharedDir.setOnItemLongClickListener(OnUDirItemClickListener);

        EditText editText;
		editText = (EditText)findViewById(R.id.name_field);
		editText.setText(mFooshare.storage().getNickName());
		editText = (EditText)findViewById(R.id.downloads_folder_field);
		if (mFooshare.storage().getDownloadDir() != "") {
			editText.setText(mFooshare.storage().getDownloadDir());
			editText.setTextColor(Color.BLACK);
		}

    	String sdcard = Environment.getExternalStorageDirectory().getAbsolutePath();
    	if (new File(sdcard).exists()) {
    		mInitalPath = sdcard;
    	}
	}


	OnItemLongClickListener OnUDirItemClickListener = new OnItemLongClickListener() {
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
			ListView lv_sharedDir = (ListView)findViewById(R.id.listView_uDir);
		    final Object o = lv_sharedDir.getItemAtPosition(position);

			builder.setMessage("Are you sure you want to remove " + o.toString() + " ?")
		       .setCancelable(false)
		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		   		    adapter.remove(o.toString());
				    UpdateSharedDir();
		           }
		       })
		       .setNegativeButton("No", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		           }
		       });

			AlertDialog alert = builder.create();
			alert.show();
			return true;
		  }
	   };


	public void OnClick_UploadDir(View view){
		Log.d(TAG, "OnClick_UploadDir is running");

    	Intent fileExploreIntent = new Intent(org.fooshare.FileBrowserActivity.INTENT_ACTION_SELECT_DIR,
								null,this , org.fooshare.FileBrowserActivity.class );
    	fileExploreIntent.putExtra(org.fooshare.FileBrowserActivity.startDirectoryParameter, mInitalPath);
     	startActivityForResult( fileExploreIntent, REQUEST_CODE_PICK_UPLOAD_DIR );
	}

	public void nickNameOkClicked(View view){

		EditText editText = (EditText)findViewById(R.id.name_field);
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
		Log.d(TAG, editText.getText().toString());

        String newNickname = editText.getText().toString();
        mFooshare.storage().setNickname(newNickname);
	}

	public void downloadsFolderFieldClicked(View view){

		Log.d(TAG, "downloadsFolderFieldClicked is running");

    	Intent fileExploreIntent = new Intent(org.fooshare.FileBrowserActivity.INTENT_ACTION_SELECT_DIR,
								null,this , org.fooshare.FileBrowserActivity.class );

    	fileExploreIntent.putExtra(org.fooshare.FileBrowserActivity.startDirectoryParameter, mInitalPath);
     	startActivityForResult( fileExploreIntent, REQUEST_CODE_PICK_DIR );
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		String newDir = "";
    	if(resultCode == RESULT_OK) {
    		newDir = data.getStringExtra(org.fooshare.FileBrowserActivity.returnDirectoryParameter);
    	} else {
    		Log.d(TAG, "no result from the activity");
    		return;
    	}

		if (requestCode == REQUEST_CODE_PICK_DIR) {
			TextView tv = (TextView) findViewById(R.id.downloads_folder_field);
			tv.setText(newDir);
			tv.setTextColor(Color.BLACK);

			// update download_dir in storage
	        mFooshare.storage().setDownloadDir(newDir);
        }

		if (requestCode == REQUEST_CODE_PICK_UPLOAD_DIR) {
			adapter.add(newDir);

	     	// update upload_dir in storage
			UpdateSharedDir();
		}
	}

	public void registrationDoneClicked(View view){
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

	private void UpdateSharedDir()	{
     	String[] arr_UDirs = new String[adapter.getCount()];
     	for (int i=0; i < adapter.getCount(); i++) {
     		arr_UDirs[i] = adapter.getItem(i);
     	}
     	mFooshare.storage().setSharedDir(arr_UDirs);
	}
}

