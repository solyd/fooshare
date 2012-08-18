package org.fooshare;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.alljoyn.bus.BusException;
import org.fooshare.predicates.Predicate;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

class UDirListEntryAdapter extends ArrayAdapter<String> {

    private static final String TAG = "UDirListEntryAdapter";
    Context context;
    private int layoutResourceId;
    private List<String> data ;

    public UDirListEntryAdapter(Context context, int layoutResourceId, List<String> arr) {
        super(context, layoutResourceId, arr);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = arr;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            final UDirEntryHolder holder = new UDirEntryHolder();
            holder.folderName = (TextView) row.findViewById(R.id.shared_folder_name);
            row.setTag(holder);

        } else {
            row = convertView;
        }

        UDirEntryHolder holder = (UDirEntryHolder) row.getTag();
        holder.folderName.setText(data.get(position));
        return row;
    }

    /* This function adds an element to the array of entries */
    @Override
    public void add(String newEntry) {
        Log.i(TAG, ("array size  = %d" + (data.size())));
        data.add(newEntry);
    }

    static class UDirEntryHolder {
        Button removeBtn;
        TextView folderName;
    }
}

public class RegistrationFragment extends Fragment {

	private static final String TAG = "RegistrationFragment";
	private static final int REQUEST_CODE_PICK_DIR = 101;
	private static final int REQUEST_CODE_PICK_UPLOAD_DIR = 102;
	private String mInitalPath = "/";

	protected FooshareApplication mFooshare;

	//list items for upload directories
	protected ArrayList<String> listItems;
	//string adapter to handle listview
	UDirListEntryAdapter adapter;

	private AlertDialog.Builder builder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	Log.d(TAG, "onCreateView is running");

        View view = inflater.inflate(R.layout.registration_fragment, container, false);


        Activity curr_activity = getActivity();
        builder = new AlertDialog.Builder(curr_activity);

        mFooshare = (FooshareApplication) curr_activity.getApplication();

        listItems = new ArrayList<String>(Arrays.asList(mFooshare.storage().getSharedDir()));
        //adapter = new ArrayAdapter<String>(curr_activity, R.layout.small_list_text, listItems);
        adapter = new UDirListEntryAdapter(curr_activity, R.layout.shared_folder_entry, listItems);

        EditText editText;
		editText = (EditText)view.findViewById(R.id.name_field);
		editText.setText(mFooshare.storage().getNickName());
		editText = (EditText)view.findViewById(R.id.downloads_folder_field);

		if (mFooshare.storage().getDownloadDir() != "") {
			editText.setText(mFooshare.storage().getDownloadDir());
			editText.setTextColor(Color.BLACK);
		}

    	String sdcard = Environment.getExternalStorageDirectory().getAbsolutePath();
    	if (new File(sdcard).exists()) {
    		mInitalPath = sdcard;
    	}
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        ListView lv_sharedDir = (ListView)getActivity().findViewById(R.id.listView_uDir);
        lv_sharedDir.setAdapter(adapter);
//      lv_sharedDir.setClickable(true);
//		lv_sharedDir.setOnItemLongClickListener(OnUDirItemClickListener);


		Button nickNameOK = (Button)getActivity().findViewById(R.id.nick_ok);
		nickNameOK.setOnClickListener(OnNicknameOKListener);

		EditText downloadsFolder = (EditText)getActivity().findViewById(R.id.downloads_folder_field);
		downloadsFolder.setOnClickListener(OnDownloadFolderOKListener);

		Button addUDir = (Button)getActivity().findViewById(R.id.button_download);
		addUDir.setOnClickListener(OnAddUDirOKListener);
    }

    OnClickListener OnAddUDirOKListener = new OnClickListener(){

		public void onClick(View arg0) {
			Log.d(TAG, "OnAddUDirOKListener is running");

	    	Intent fileExploreIntent = new Intent(FileBrowserActivity.INTENT_ACTION_SELECT_DIR,
					null, getActivity(), FileBrowserActivity.class );
			fileExploreIntent.putExtra(FileBrowserActivity.startDirectoryParameter, mInitalPath);
			startActivityForResult( fileExploreIntent, REQUEST_CODE_PICK_UPLOAD_DIR );
		}};

    OnClickListener OnDownloadFolderOKListener = new OnClickListener(){

		public void onClick(View arg0) {
			Log.d(TAG, "OnDownloadFolderOKListener is running");

	    	Intent fileExploreIntent = new Intent(FileBrowserActivity.INTENT_ACTION_SELECT_DIR,
									null, getActivity(), FileBrowserActivity.class );

	    	fileExploreIntent.putExtra(FileBrowserActivity.startDirectoryParameter, mInitalPath);
	     	startActivityForResult( fileExploreIntent, REQUEST_CODE_PICK_DIR );
		}};

    OnClickListener OnNicknameOKListener = new OnClickListener(){

		public void onClick(View arg0) {
			Log.d(TAG, "OnNicknameOKListener is running");
			EditText editText = (EditText) getActivity().findViewById(R.id.name_field);
			editText.clearFocus();

			InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
			Log.d(TAG, editText.getText().toString());

	        String newNickname = editText.getText().toString();
	        mFooshare.storage().setNickname(newNickname);
		}};

	OnClickListener OnSharedFolderEntryListener = new OnClickListener(){
		public void onClick(View view) {

			ListView lv_sharedDir = (ListView)getActivity().findViewById(R.id.listView_uDir);
			int position = lv_sharedDir.getPositionForView((View) view.getParent());
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
		}
	};

//	OnItemLongClickListener OnUDirItemClickListener = new OnItemLongClickListener() {
//		public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
//
//			ListView lv_sharedDir = (ListView)getActivity().findViewById(R.id.listView_uDir);
//		    final Object o = lv_sharedDir.getItemAtPosition(position);
//
//			builder.setMessage("Are you sure you want to remove " + o.toString() + " ?")
//		       .setCancelable(false)
//		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//		           public void onClick(DialogInterface dialog, int id) {
//		   		    adapter.remove(o.toString());
//				    UpdateSharedDir();
//		           }
//		       })
//		       .setNegativeButton("No", new DialogInterface.OnClickListener() {
//		           public void onClick(DialogInterface dialog, int id) {
//		                dialog.cancel();
//		           }
//		       });
//
//			AlertDialog alert = builder.create();
//			alert.show();
//			return true;
//		  }
//	   };

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		String newDir = "";
    	getActivity();
		if(resultCode == Activity.RESULT_OK) {
    		newDir = data.getStringExtra(FileBrowserActivity.returnDirectoryParameter);
    	} else {
    		Log.d(TAG, "no result from the activity");
    		return;
    	}

		if (newDir == null) {
			Log.d(TAG, "newDir is null");
			return;
		}

		File dir = new File(newDir);
		if (!dir.isDirectory()) {
			Log.d(TAG, "newDir not a directory");
			return;
		}

		if (requestCode == REQUEST_CODE_PICK_DIR) {
		    TextView tv = (TextView) getActivity().findViewById(R.id.downloads_folder_field);
		    tv.setText(newDir);
		    tv.setTextColor(Color.BLACK);

		    // update download_dir in storage
		    mFooshare.storage().setDownloadDir(newDir);

        }

		if (requestCode == REQUEST_CODE_PICK_UPLOAD_DIR) {
			if (adapter.getPosition(newDir) == -1) { //directory not in list
				adapter.add(newDir);
				adapter.notifyDataSetChanged();
				//update upload_dir in storage
				UpdateSharedDir();
			}
		}
	}

	private void UpdateSharedDir()	{
	    Thread updatePeers = new Thread(new Runnable() {
	        public void run() {
	            List<IPeer> peers = mFooshare.getPeers(new Predicate<IPeer>() {
	                public boolean pred(IPeer ele) {
	                    return true;
	                }
	            });

	            for (IPeer p : peers) {
	                try {
	                    ((AlljoynPeer) p).proxyObject().notifyFilesChanged(mFooshare.myId());
	                }
	                catch (BusException e) {
	                    Log.e(TAG, Log.getStackTraceString(e));
	                }
	            }
	        }
	    });
	    updatePeers.setDaemon(true);
	    updatePeers.start();

     	String[] arr_UDirs = new String[adapter.getCount()];
     	for (int i=0; i < adapter.getCount(); i++) {
     		arr_UDirs[i] = adapter.getItem(i);
     	}
     	mFooshare.storage().setSharedDir(arr_UDirs);
	}

	public void RemoveSharedFolder_OnClick(View view) {

		ListView lv_sharedDir = (ListView)getActivity().findViewById(R.id.listView_uDir);
		int position = lv_sharedDir.getPositionForView((View) view.getParent());
		final Object o = lv_sharedDir.getItemAtPosition(position);

		builder.setMessage("Are you sure you want to remove " + o.toString() + " ?")
	       .setCancelable(false)
	       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	   		    adapter.remove(o.toString());
	   		    adapter.notifyDataSetChanged();
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
	  }





}
