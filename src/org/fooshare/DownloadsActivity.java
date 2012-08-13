package org.fooshare;

import java.util.ArrayList;
import java.util.List;

import org.fooshare.network.DownloadItem;
import org.fooshare.predicates.Predicate;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DownloadsActivity extends Activity {

    private DownloadItemAdapter _downloadsListAdapter;
    private ListView            _downloadsListView;
    private List<DownloadItem>  _downloadsList;
    private FooshareApplication _fooshare;

    class DownloadItemAdapter extends ArrayAdapter<DownloadItem> {
        private static final String TAG = "DownloadItemAdapter";

        private Context            _context;
        private int                _layoutResourceId;
        private List<DownloadItem> _data;

        public DownloadItemAdapter(Context context, int layoutResourceId, List<DownloadItem> arr) {
            super(context, layoutResourceId, arr);

            _layoutResourceId = layoutResourceId;
            _context = context;
            _data = arr;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            DownloadEntryHolder holder = null;

            if (row == null) {
                LayoutInflater inflater = ((Activity) _context).getLayoutInflater();
                row = inflater.inflate(_layoutResourceId, parent, false);

                holder = new DownloadEntryHolder();
                holder.icon = (ImageView) row.findViewById(R.id.image);
                holder.fileName = (TextView) row.findViewById(R.id.fileName);
                holder.eta = (TextView) row.findViewById(R.id.eta);
                holder.percentage = (TextView) row.findViewById(R.id.percentage);
                holder.progressBar = (ProgressBar) row.findViewById(R.id.progressbar);

                row.setTag(holder);
            }
            else {
                holder = (DownloadEntryHolder) row.getTag();
            }

            DownloadItem entry = getItem(position);

            holder.icon.setImageResource(entry.iconTypeId);
            holder.fileName.setText(entry.fileName);
            holder.eta.setText("ETA_TODO");
            holder.percentage.setText(String.format("%d", entry.getPercentageProgress()));
            holder.progressBar.setMax((int) entry.fileSize);
            holder.progressBar.setProgress(entry.progress);

            return row;
        }

        class DownloadEntryHolder {
            ImageView icon;
            TextView fileName;
            TextView eta;
            TextView percentage;
            ProgressBar progressBar;
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.downloads_activity);
        _fooshare = (FooshareApplication) getApplication();

        _downloadsList = _fooshare.getDownloads(new Predicate<DownloadItem>() {
            public boolean pred(DownloadItem ele) {
                return true;
            }
        });
        _downloadsListAdapter = new DownloadItemAdapter(this, R.layout.list_downloads_entry, _downloadsList);
        _downloadsListView = (ListView) findViewById(R.id.list_downloads);
        _downloadsListView.setAdapter(_downloadsListAdapter);
    }

    @Override
    protected void onResume() {


    }

    public void onDemoClick(View view) {
    }



    private class DownloadReceiver extends ResultReceiver {
        private static final String TAG = "DownloadReceiver";

        public DownloadReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            _downloadsListAdapter.notifyDataSetChanged();
        }
    }


    /*
     * This class when initiated and it's execute method is ran, opens a new
     * thread which adds another entry to the list that is managed by the
     * Downloads activity
     */

    private class EntryAdditionClass extends
            AsyncTask<DownloadItem, Integer, DownloadItem> {

        final String TAG = "AsyncTask Tag";
        int i = 0;

        private DownloadReceiver _dlrecv;

        public EntryAdditionClass(DownloadReceiver dlrecv) {
            _dlrecv = dlrecv;
        }

        protected void onPostExecute(DownloadItem newEntry) {
            //mAdapter.add(newEntry);
            //mAdapter.notifyDataSetChanged();
            Log.d(TAG, "onPostExecute is running");
            /*
             * TextView textView = (TextView)findViewById(R.id.count_label);
             * Log.d(TAG, textView.getText().toString());
             * textView.setText(String.valueOf(i));
             */
        }

        @Override
        protected DownloadItem doInBackground(DownloadItem... params) {
            int progress = 0;
            for (i = 0; i < 19; i++) {
                try {
                    Thread.sleep(1000);
                    progress += 10;

                    Bundle data = new Bundle();
                    data.putInt("progress", progress);
                    _dlrecv.send(0, data);
                }
                catch (InterruptedException e) {
                    Log.d(TAG, "There is an exception in doInBackground");
                }
                Log.d(TAG, "i = " + String.valueOf(i));
            }
            return null;
        }
        /*
         * @Override protected void onProgressUpdate (Integer... values){
         * TextView textView = (TextView)this.findViewById(R.id.count_label);
         *
         * }
         */
    }
}
