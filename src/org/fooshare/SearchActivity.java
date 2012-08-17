package org.fooshare;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.fooshare.R;
import org.fooshare.R.drawable;
import org.fooshare.R.id;
import org.fooshare.R.layout;
import org.fooshare.R.string;
import org.fooshare.predicates.PeerIdFilePredicate;
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
import android.widget.TabHost;
import android.widget.TextView;

class SearchListEntryAdapter extends ArrayAdapter<FileItem> {
    private static final String TAG = "SearchListEntryAdapter";

    private Context context;
    private int layoutResourceId;
    private List<FileItem> data ;
    private List<FileItem> checkedFiles;

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
    public void clear() {
        super.clear();

        this.checkedFiles.clear();
        this.data.clear();
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

            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    FileItem file = (FileItem) holder.checkBox.getTag();
                    file.setSelected(buttonView.isChecked());
                    if (file.isSelected())
                        checkedFiles.add(file);
                    else
                        checkedFiles.remove(file);
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
        holder.fileName.setText(data.get(position).name());
        holder.fileType.setText(data.get(position).type());
        holder.checkBox.setChecked(data.get(position).isSelected());
        return row;
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

    private static final String NAME  = "File Name";
    private static final String TYPE  = "Type";
    private static final String SIZE  = "Size";
    private static final String RNAME = "RFile Name";
    private static final String RTYPE = "RType";
    private static final String RSIZE = "RSize";

    private FooshareApplication _fooshare;

    private SearchListEntryAdapter mSearchListAdapter;
    private ListView               mSearchListView;
    private EditText               mSearchBar;
    private String                 mLastSearchString;

    // filteredList holds the list as it appears on the screen
    private List<FileItem> mFileList;

    // specifies how to sort the list
    private String sortFlag = NAME;

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();

        IPeer selectedPeer = _fooshare.getSelectedPeer();
        if (selectedPeer != null) {
            _fooshare.setSelectedPeer(null);
            mFileList = _fooshare.getAllSharedFiles(new PeerIdFilePredicate(selectedPeer));
        }
        else {
            mFileList = _fooshare.getAllSharedFiles(new Predicate<FileItem>() {
                public boolean pred(FileItem ele) {
                    return true;
                }
            });
        }

        sortFileList();
        mSearchListAdapter = new SearchListEntryAdapter(this, R.layout.search_list_entry, mFileList);
        mSearchListView.setAdapter(mSearchListAdapter);
        mSearchListView.requestFocus();
    }

    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);
        findViewById(R.id.arrow2).setVisibility(View.INVISIBLE);
        findViewById(R.id.arrow3).setVisibility(View.INVISIBLE);
        _fooshare = (FooshareApplication) getApplication();

        mFileList = _fooshare.getAllSharedFiles(new Predicate<FileItem>() {
            public boolean pred(FileItem ele) {
                return true;
            }
        });
        sortFileList();

        mSearchListAdapter = new SearchListEntryAdapter(this, R.layout.search_list_entry, mFileList);
        mSearchListView = (ListView) findViewById(R.id.search_list);
        mSearchListView.setAdapter(mSearchListAdapter);
        mSearchListView.requestFocus();

        mSearchBar = (EditText) findViewById(R.id.search_field);
    }

    // Runs when the user presses S. It filters the list according to what is
    // written in the search field.
    public void onSearchListClick(View btn) {
        mLastSearchString = mSearchBar.getText().toString();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mSearchBar.getWindowToken(), 0);

        mFileList = _fooshare.getAllSharedFiles(new SubStringPredicate(mLastSearchString));

        sortFileList();
        mSearchListAdapter = new SearchListEntryAdapter(this, R.layout.search_list_entry, mFileList);
        mSearchListView.setAdapter(mSearchListAdapter);
    }


    public void sortFileList() {
        if (sortFlag.equals(NAME))
            Collections.sort(mFileList, new ComparatorByName());
        else if (sortFlag.equals(SIZE))
            Collections.sort(mFileList, new ComparatorBySize());
        else if (sortFlag.equals(TYPE))
            Collections.sort(mFileList, new ComparatorByType());
        else
            Collections.reverse(mFileList);
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

        sortFileList();
        mSearchListAdapter = new SearchListEntryAdapter(this, R.layout.search_list_entry, mFileList);
        mSearchListView.setAdapter(mSearchListAdapter);

    }

    public void downloadCheckedClicked(View view) {
        final List<FileItem> checkedItems = mSearchListAdapter.getCheckedFiles();
        if (checkedItems.size() == 0)
            return;

        for (FileItem fi : checkedItems) {
            _fooshare.startDownloadService(fi);
            fi.setSelected(false);
        }
        mSearchListAdapter.notifyDataSetChanged();

        ((TabHost) getParent().findViewById(android.R.id.tabhost)).setCurrentTabByTag(getResources().getString(R.string.DOWNLOADS_TAB_TAG));
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
}

class ComparatorByName implements Comparator<FileItem> {
    public int compare(FileItem f1, FileItem f2) {
        return f1.name().compareTo(f2.name());
    }
}

class ComparatorByType implements Comparator<FileItem> {
    public int compare(FileItem f1, FileItem f2) {
        return f1.category().compareTo(f2.category());
    }
}

class ComparatorBySize implements Comparator<FileItem> {

    public int compare(FileItem f1, FileItem f2) {
        if (f1.sizeInBytes() < f2.sizeInBytes())
            return -1;
        else if (f1.sizeInBytes() == f2.sizeInBytes())
            return 0;
        else
            return 1;
    }
}


