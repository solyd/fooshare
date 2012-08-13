package org.fooshare;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;


class  SearchListEntryAdapter extends ArrayAdapter <FileItem> {

    private static final String TAG = "inSearchListEntryAdapter";
	Context context;
    int layoutResourceId;
    ArrayList <FileItem> data = null;

    //Context - reference of the activity in which we will use the Adapter class
    //Resource id of the layout file we want to use for displaying each ListView item
    //An array of  FileItem class objects that will be used by the Adapter to display data.
    public SearchListEntryAdapter(Context context, int layoutResourceId, ArrayList <FileItem> arr) {
        super(context, layoutResourceId,arr);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = arr;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        FileEntryHolder holder = null;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new FileEntryHolder();
            holder.fileSize = (TextView)row.findViewById(R.id.search_entry_fileSize);
            holder.fileName = (TextView)row.findViewById(R.id.search_entry_fileName);
            holder.fileType = (TextView)row.findViewById(R.id.search_entry_fileType);
            row.setTag(holder);
        }
        else
        {
            holder = (FileEntryHolder)row.getTag();
        }

        FileItem entry = data.get(position);
        holder.fileSize.setText(String.format("%d", entry.getSizeInBytes()));
        holder.fileName.setText(entry.getName());
        holder.fileType.setText(entry.getType());
        return row;
    }

    /*This function adds an element to the array of entries*/
    @Override
    public void add (FileItem newEntry){
    	Log.i(TAG,("array size  = %d" + (data.size())));
    	data.add(newEntry);
    }

    static class FileEntryHolder
    {
        TextView fileName;
        TextView fileSize;
        TextView fileType;
    }
}



public class SearchActivity extends Activity {

	private static final String TAG = "SearchActivity";
	private SearchListEntryAdapter mAdapter ;
	private ListView mSearchListView;
	private EditText searchBar;
	private ArrayList<FileItem> mList;

	public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);
        mList = creatNewList();
        mAdapter = new SearchListEntryAdapter(this,R.layout.search_list_entry,mList);
        mSearchListView = (ListView)findViewById(R.id.search_list);
        mSearchListView.setAdapter(mAdapter);
        searchBar = (EditText) findViewById(R.id.search_field);

	}

	//Runs when the user presses S. It filters the list according to what is written in the search field.
	public void searchList(View btn){
	    /*
		String text = searchBar.getText().toString();
		Predicate predicate = new Predicate(text);
		ArrayList<FileItem> filteredList = getSharedFiles(predicate,mList );

		mAdapter = new SearchListEntryAdapter(this,R.layout.search_list_entry,filteredList);
	    mSearchListView.setAdapter(mAdapter);
		*/
	}

	//My function for testing that I use to create lists
	public ArrayList<FileItem> creatNewList(){

		ArrayList<FileItem> list = new ArrayList<FileItem>();
/*
        list.add(new FileItem("FileName1.avi-Path","Brad.avi","23k","1234"));
        list.add(new FileItem("FileName2.avi-Path","Tom.mp3","23k","2345"));
        list.add(new FileItem("FileName3.avi-Path","Barbara.mobi","23k","67324"));
        list.add(new FileItem("FileName4.avi-Path","Tobi.avi","23k","1234"));
        list.add(new FileItem("FileName1.avi-Path","BaronCohen.avi","23k","16732"));
        list.add(new FileItem("FileName2.avi-Path","Timor.mp3","23k","547"));
        list.add(new FileItem("FileName3.avi-Path","London.apk","102k","2545"));
        list.add(new FileItem("FileName4.avi-Path","Shmulic.avi","23k","98736"));
        list.add(new FileItem("FileName1.avi-Path","Barbican.avi","23k","5285"));
        list.add(new FileItem("FileName2.avi-Path","Tom.mp4","30MB","1234"));
        list.add(new FileItem("FileName3.avi-Path","Paris.mobi","23k","1234"));
        list.add(new FileItem("FileName4.avi-Path","Pipi.Kaki.avi","564T","264"));
*/
        return list;

	}

		/*
	public ArrayList<FileItem> getSharedFiles(Predicate predicate, ArrayList<FileItem> list ){
		ArrayList<FileItem> newList = new ArrayList<FileItem> ();
		newList.clear();
		for (FileItem file : list){
			if (predicate.filter(file))
				newList.add(file);
		}
		return newList;
	}




	public void startService(View btn){

		Log.d(TAG, "startService is running");
		/*
		TextView textView = (TextView)this.findViewById(R.id.search_activity_label);
		//The intent is a message.
		Intent myIntent = new Intent(getApplicationContext(), SimpleService.class);
		if(startService(myIntent) != null)
			textView.setText("The service is on");
		else
			 textView.setText("The service is still off");

		FileItem newEntry = new FileItem("FileName.mp3","3MB","audio","666");
		mAdapter.add(newEntry);
	 	mAdapter.notifyDataSetChanged();
	}

	//This function turns the service on/off on a push of the button
	public void stopService(View btn){
		Log.d(TAG, "stoptService is running");
		//The intent is a message.
		 Intent myIntent = new Intent(getApplicationContext(), SimpleService.class);
		 TextView textView = (TextView)this.findViewById(R.id.search_activity_label);
		 if(stopService(myIntent))
			textView.setText("The service is off");
		 else
			 textView.setText("The service is still on");
	}
	*/
}

