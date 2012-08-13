package org.fooshare;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.fooshare.predicates.Predicate;
import org.fooshare.predicates.SubStringPredicate;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

class SearchListEntryAdapter extends ArrayAdapter<FileItem> {

    private static final String TAG = "inSearchListEntryAdapter";
    Context context;
    int layoutResourceId;
    List<FileItem> data ;
    List<FileItem> checkedFiles;

    // Context - reference of the activity in which we will use the Adapter
    // class
    // Resource id of the layout file we want to use for displaying each
    // ListView item
    // An array of FileItem class objects that will be used by the Adapter to
    // display data.
    public SearchListEntryAdapter(Context context, int layoutResourceId, List<FileItem> arr) {
        super(context, layoutResourceId, arr);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = arr;
        this.checkedFiles = new ArrayList<FileItem>();
    }

    public List<FileItem> getCheckedFiles() {
        return checkedFiles;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            final FileEntryHolder holder = new FileEntryHolder();
            holder.fileSize = (TextView) row.findViewById(R.id.search_entry_fileSize);
            holder.fileName = (TextView) row.findViewById(R.id.search_entry_fileName);
            holder.fileType = (TextView) row.findViewById(R.id.search_entry_fileType);
            holder.checkBox = (CheckBox) row.findViewById(R.id.checkBox);

            holder.checkBox
                    .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            FileItem file = (FileItem) holder.checkBox.getTag();
                            file.setSelected(buttonView.isChecked());
                            if (file.isSelected())
                            	checkedFiles.add(file);
                            else checkedFiles.remove(file);
                        }
                    });
            row.setTag(holder);
            holder.checkBox.setTag(data.get(position));
        }
        else {
            row = convertView;
            ((FileEntryHolder) row.getTag()).checkBox.setTag(data.get(position));
        }

        // FileItem entry = data.get(position);
        FileEntryHolder holder = (FileEntryHolder) row.getTag();

        holder.fileSize.setText(data.get(position).getAdjustedSize());
        holder.fileName.setText(data.get(position).getName());
        holder.fileType.setText(data.get(position).getType());
        holder.checkBox.setChecked(data.get(position).isSelected());
        return row;
    }

    /* This function adds an element to the array of entries */
    @Override
    public void add(FileItem newEntry) {
        Log.i(TAG, ("array size  = %d" + (data.size())));
        data.add(newEntry);
    }

    static class FileEntryHolder {
        CheckBox checkBox;
        TextView fileName;
        TextView fileSize;
        TextView fileType;
    }
}

public class SearchActivity extends Activity {
private static final String TAG = "SearchActivity";

    private static final String NAME = "File Name";
    private static final String TYPE = "Type";
    private static final String SIZE = "Size";
    private static final String RNAME = "RFile Name";
    private static final String RTYPE = "RType";
    private static final String RSIZE = "RSize";

    private FooshareApplication _fooshare;

    private SearchListEntryAdapter mAdapter;
    private ListView mSearchListView;
    private EditText searchBar;
    // mList always holds the list of ALL available files
    private List<FileItem> mList;
    // filteredList holds the list as it appears on the screen
    private List<FileItem> filteredList;
    // specifies how to sort the list
    private String sortFlag = NAME;

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume of search activity");
        super.onResume();
    }


    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate of search activity");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);
        findViewById(R.id.arrow2).setVisibility(View.INVISIBLE);
        findViewById(R.id.arrow3).setVisibility(View.INVISIBLE);
        _fooshare = (FooshareApplication) getApplication();

        mList = _fooshare.getAllSharedFiles(new Predicate<FileItem>() {
            public boolean pred(FileItem ele) {
                return true;
            }
        });

        filteredList = new ArrayList<FileItem>(mList);
        sortFilteredList();
        Log.d(TAG, "after sort");
        mAdapter = new SearchListEntryAdapter(this, R.layout.search_list_entry, filteredList);
        Log.d(TAG, "after creating adapter");
        mSearchListView = (ListView) findViewById(R.id.search_list);

        mSearchListView.setAdapter(mAdapter);
        Log.d(TAG, "after setting adapter");
        searchBar = (EditText) findViewById(R.id.search_field);

    }

    // Runs when the user presses S. It filters the list according to what is
    // written in the search field.
    public void searchList(View btn) {
        String text = searchBar.getText().toString();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);

        SubStringPredicate subStringPredicate = new SubStringPredicate(text);

        // Here should be a line calling to Alex's function that will return a
        // list with all files.
        // I will have to invoke getApplication and ask for the updated list
        // in getSharedFiles i will only pass the predicate

        //filteredList = getSharedFiles(predicate, mList);
        filteredList = _fooshare.getAllSharedFiles(subStringPredicate);

        sortFilteredList();
        mAdapter = new SearchListEntryAdapter(this, R.layout.search_list_entry, filteredList);
        mSearchListView.setAdapter(mAdapter);
    }
/*
    // My function for testing. I use it to create lists
    public ArrayList<FileItem> creatNewList() {

        ArrayList<FileItem> list = new ArrayList<FileItem>();
        list.add(new FileItem("///BradabcdrfghaksiurjnvhfydnhvjdsisVerylongFileName.avi",
                              (long) 1024, "1234"));
        list.add(new FileItem("/Tom.mp3", (long) 1025, "2345"));
        list.add(new FileItem("//Barbara.mobi", (long) 1023, "67324"));
        list.add(new FileItem("////Tobi.avi", (long) 848483932, "1234"));
        list.add(new FileItem("/BaronCohen.avi", (long) 12434512, "16732"));
        list.add(new FileItem("/Timor.mp3", (long) 56345523, "547"));
        list.add(new FileItem("/London.apk", (long) 12, "2545"));
        list.add(new FileItem("\\//Shmulic.avi", (long) 569062, "98736"));
        list.add(new FileItem("\\//Barbican.avi", (long) 324523465, "5285"));
        list.add(new FileItem("\\//Tom.mp4", (long) 356747, "1234"));
        list.add(new FileItem("\\///Paris.mobi", (long) 234234, "1234"));
        list.add(new FileItem("\\///Pipi.Kaki.avi", (long) 3429095, "264"));
        return list;

    }

    // This function is only a simulation of what is going to be in the
    // Application object.
    // It will actually receive only the predicate.
    public ArrayList<FileItem> getSharedFiles(Predicate<FileItem> predicate, ArrayList<FileItem> list) {
        ArrayList<FileItem> newList = new ArrayList<FileItem>();
        for (FileItem file : list) {
            if (predicate.pred(file)) newList.add(file);
        }
        return newList;
    }
    */

    public void sortFilteredList() {
        Log.d(TAG, "in sort");
        if (sortFlag.equals(NAME))
            Collections.sort(filteredList, new ComparatorByName());
        else if (sortFlag.equals(SIZE))
            Collections.sort(filteredList, new ComparatorBySize());
        else if (sortFlag.equals(TYPE))
            Collections.sort(filteredList, new ComparatorByType());
        else
            Collections.reverse(filteredList);
    }

    public void sortButtonClicked(View view) {

        String txt = ((TextView) view).getText().toString();
       
        // Here it is decided what kind of sort should occur and the arrows adjust
         if (txt.equals(NAME)) {
            if (sortFlag.equals(NAME)) {
                sortFlag = RNAME;
                ((ImageView) findViewById(R.id.arrow1)).setImageResource(R.drawable.arrow_down);
            }
            else {
                sortFlag = NAME;
                ((ImageView) findViewById(R.id.arrow1)).setImageResource(R.drawable.arrow_up);
                findViewById(R.id.arrow1).setVisibility(View.VISIBLE);
                findViewById(R.id.arrow2).setVisibility(View.INVISIBLE);
                findViewById(R.id.arrow3).setVisibility(View.INVISIBLE);
            }
        }
        else if (txt.equals(TYPE)) {
            if (sortFlag.equals(TYPE)) {
                sortFlag = RTYPE;
                ((ImageView) findViewById(R.id.arrow3)).setImageResource(R.drawable.arrow_down);
            }
            else {
                sortFlag = TYPE;
                ((ImageView) findViewById(R.id.arrow3)).setImageResource(R.drawable.arrow_up);
                findViewById(R.id.arrow3).setVisibility(View.VISIBLE);
                findViewById(R.id.arrow2).setVisibility(View.INVISIBLE);
                findViewById(R.id.arrow1).setVisibility(View.INVISIBLE);
            }
        }
        else {
            if (sortFlag.equals(SIZE)) {
                sortFlag = RSIZE;
                ((ImageView) findViewById(R.id.arrow2)).setImageResource(R.drawable.arrow_down);
            }
            else {
                ((ImageView) findViewById(R.id.arrow2)).setImageResource(R.drawable.arrow_up);
                findViewById(R.id.arrow2).setVisibility(View.VISIBLE);
                findViewById(R.id.arrow3).setVisibility(View.INVISIBLE);
                findViewById(R.id.arrow1).setVisibility(View.INVISIBLE);
                sortFlag = SIZE;
            }
        }

        sortFilteredList();
        mAdapter = new SearchListEntryAdapter(this, R.layout.search_list_entry, filteredList);
        mSearchListView.setAdapter(mAdapter);

    }

    public void downloadCheckedClicked(View view) {
       // List<FileItem> newList = new ArrayList<FileItem>();
        final List<FileItem> list = mAdapter.getCheckedFiles();
        //here i'm sending it to Alex
        for (int i = 0; i <  list.size(); i++) {
            FileItem file =  list.get(i);
            Log.i(TAG, file.getName());
           
        }
        // here i'm calling Alex's function /cancel
    }
}

class ComparatorByName implements Comparator<FileItem> {

    public int compare(FileItem f1, FileItem f2) {
        return f1.getName().compareTo(f2.getName());
    }

}

class ComparatorByType implements Comparator<FileItem> {

    public int compare(FileItem f1, FileItem f2) {
        return f1.getType().compareTo(f2.getType());
    }
}

class ComparatorBySize implements Comparator<FileItem> {

    public int compare(FileItem f1, FileItem f2) {
        if (f1.getSizeInBytes() < f2.getSizeInBytes())
            return -1;
        else if (f1.getSizeInBytes() == f2.getSizeInBytes())
            return 0;
        else
            return 1;
    }
}
