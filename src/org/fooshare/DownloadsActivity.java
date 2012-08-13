package org.fooshare;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

class DownloadsListEntry {
    // private static final String TAG = "FileEntry Tag";

    public int icon;
    public String fileName;
    public String eta;
    public String percentage;

    /*
     * The constructor for an entry in the list of downloads name - the name of
     * the file fileType - "video","audio" or "text" - the icon of the file in
     * the list will be set according to this variable
     */
    public DownloadsListEntry(String name, String fileType) {

        fileName = name;
        eta = "ETA : Unknown ";
        percentage = "0%";

        if (fileType == "video") {
            icon = R.drawable.video_icon;
        }
        else if (fileType == "audio") {
            icon = R.drawable.audio_icon;
        }
        else if (fileType == "text") {
            icon = R.drawable.text_icon;
        }
    }

}

class DownloadsListEntryAdapter extends ArrayAdapter<DownloadsListEntry> {

    private static final String TAG = "inArrayAdapter";
    Context context;
    int layoutResourceId;
    ArrayList<DownloadsListEntry> data = null;

    // Context - reference of the activity in which we will use the Adapter
    // class
    // Resource id of the layout file we want to use for displaying each
    // ListView item
    // An array of DownloadsListUnitEntry class objects that will be used by the
    // Adapter to display data.
    public DownloadsListEntryAdapter(Context context, int layoutResourceId,
            ArrayList<DownloadsListEntry> arr) {
        super(context, layoutResourceId, arr);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = arr;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        FileEntryHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new FileEntryHolder();
            holder.icon = (ImageView) row.findViewById(R.id.image);
            holder.fileName = (TextView) row.findViewById(R.id.fileName);
            holder.eta = (TextView) row.findViewById(R.id.eta);
            holder.percentage = (TextView) row.findViewById(R.id.percentage);
            row.setTag(holder);
        }
        else {
            holder = (FileEntryHolder) row.getTag();
        }

        DownloadsListEntry entry = data.get(position);
        holder.icon.setImageResource(entry.icon);
        holder.fileName.setText(entry.fileName);
        holder.eta.setText(entry.eta);
        holder.percentage.setText(entry.percentage);

        return row;
    }

    /* This function adds an element to the array of entries */
    @Override
    public void add(DownloadsListEntry newEntry) {
        Log.i(TAG, ("array size  = %d" + (data.size())));
        data.add(newEntry);
    }

    static class FileEntryHolder {
        ImageView icon;
        TextView fileName;
        TextView eta;
        TextView percentage;
    }
}

public class DownloadsActivity extends Activity {

    public DownloadsListEntryAdapter mAdapter;
    public ListView mDownloadsListView;
    public ArrayList<DownloadsListEntry> mList;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.downloads_activity);

        mList = new ArrayList<DownloadsListEntry>();
        mList.add(new DownloadsListEntry("FileName.something", "video"));
        mAdapter = new DownloadsListEntryAdapter(this, R.layout.list_downloads_entry, mList);
        mDownloadsListView = (ListView) findViewById(R.id.list_downloads);
        mDownloadsListView.setAdapter(mAdapter);
    }

    public void updateDownloadsList(View view) {
        final String TAG = "updateDownloadsList Tag";
        Log.d(TAG, "updateDownloadsList is running ");

        new EntryAdditionClass().execute(new DownloadsListEntry("some file", "text"));
    }

    /*
     * This class when initiated and it's execute method is ran, opens a new
     * thread which adds another entry to the list that is managed by the
     * Downloads activity
     */
    private class EntryAdditionClass extends
            AsyncTask<DownloadsListEntry, Integer, DownloadsListEntry> {

        final String TAG = "AsyncTask Tag";
        int i = 0;

        protected void onPostExecute(DownloadsListEntry newEntry) {
            mAdapter.add(newEntry);
            mAdapter.notifyDataSetChanged();
            Log.d(TAG, "onPostExecute is running");
            /*
             * TextView textView = (TextView)findViewById(R.id.count_label);
             * Log.d(TAG, textView.getText().toString());
             * textView.setText(String.valueOf(i));
             */
        }

        @Override
        protected DownloadsListEntry doInBackground(DownloadsListEntry... params) {
            for (i = 0; i < 14; i++) {
                try {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e) {
                    Log.d(TAG, "There is an exception in doInBackground");
                }
                Log.d(TAG, "i = " + String.valueOf(i));
            }
            return params[0];
        }
        /*
         * @Override protected void onProgressUpdate (Integer... values){
         * TextView textView = (TextView)this.findViewById(R.id.count_label);
         *
         * }
         */
    }
}
