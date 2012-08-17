package org.fooshare;

import java.util.List;

import org.fooshare.events.Delegate;
import org.fooshare.network.Download;
import org.fooshare.network.FileServer.Upload;
import org.fooshare.predicates.Predicate;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DownloadsActivity extends Activity {
    private final static String TAG = "DownloadsActivity";

    private DownloadItemAdapter _downloadsListAdapter;
    private ListView            _downloadsListView;
    private List<Download>      _downloadsList;

    private UploadItemAdapter _uploadsListAdapter;
    private ListView          _uploadsListView;
    private List<Upload>      _uploadsList;

    private FooshareApplication _fooshare;

    private Handler                _uiHandler = new Handler(Looper.getMainLooper());
    private Handler                _handler = new Handler();
    private DownloadUpdateReceiver _downloadUpdateReceiver = new DownloadUpdateReceiver(_handler);
    private UploadUpdateReceiver   _uploadUpdateReceiver = new UploadUpdateReceiver(_handler);

    private class DownloadListChanged implements Delegate<List<Download>> {
        public void invoke(final List<Download> newDownloadsList) {
            _uiHandler.post(new Runnable() {
                public void run() {
                    _downloadsList.clear();
                    for (Download di : newDownloadsList) {
                        di.setUpdateReceiver(_downloadUpdateReceiver);
                        _downloadsList.add(di);
                    }

                    _downloadsListAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    private class UploadListChanged implements Delegate<List<Upload>> {
        public void invoke(final List<Upload> newUploadsList) {
            _uiHandler.post(new Runnable() {
                public void run() {
                    _uploadsList.clear();
                    for (Upload di : newUploadsList) {
                        di.setUpdateReceiver(_uploadUpdateReceiver);
                        _uploadsList.add(di);
                    }

                    _uploadsListAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    private class DownloadItemAdapter extends ArrayAdapter<Download> {
        private Context        _context;
        private int            _layoutResourceId;
        private List<Download> _data;

        public DownloadItemAdapter(Context context, int layoutResourceId, List<Download> arr) {
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
                //holder.icon = (ImageView) row.findViewById(R.id.image);
                holder.fileName = (TextView) row.findViewById(R.id.fileName);
                holder.eta = (TextView) row.findViewById(R.id.eta);
                holder.percentage = (TextView) row.findViewById(R.id.percentage);
                holder.progressBar = (ProgressBar) row.findViewById(R.id.progressbar);

                row.setTag(holder);
            }
            else {
                holder = (DownloadEntryHolder) row.getTag();
            }

            Download dlitem = getItem(position);
            FileItem fitem = dlitem.getFile();
            int progressInPercent = dlitem.getPercentageProgress();

            //holder.icon.setImageResource(determineIconId(fitem));
            holder.fileName.setText(fitem.name());
            holder.eta.setText("ETA_TODO");
            holder.percentage.setText(String.format("%d%%", progressInPercent));
            holder.progressBar.setMax(100);
            holder.progressBar.setProgress(progressInPercent);

            return row;
        }


        class DownloadEntryHolder {
            ImageButton imageButton;
            TextView fileName;
            TextView eta;
            TextView percentage;
            ProgressBar progressBar;
        }
    }

    private class UploadItemAdapter extends ArrayAdapter<Upload> {
        private Context        _context;
        private int            _layoutResourceId;
        private List<Upload> _data;

        public UploadItemAdapter(Context context, int layoutResourceId, List<Upload> arr) {
            super(context, layoutResourceId, arr);

            _layoutResourceId = layoutResourceId;
            _context = context;
            _data = arr;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            UploadEntryHolder holder = null;

            if (row == null) {
                LayoutInflater inflater = ((Activity) _context).getLayoutInflater();
                row = inflater.inflate(_layoutResourceId, parent, false);

                holder = new UploadEntryHolder();
                //holder.icon = (ImageView) row.findViewById(R.id.image);
                holder.fileName = (TextView) row.findViewById(R.id.fileName);
                holder.percentage = (TextView) row.findViewById(R.id.percentage);
                holder.progressBar = (ProgressBar) row.findViewById(R.id.progressbar);

                row.setTag(holder);
            }
            else {
                holder = (UploadEntryHolder) row.getTag();
            }

            Upload item = getItem(position);
            String filename = item.getFileName();
            int progressInPercent = item.getPercentageProgress();

            //holder.icon.setImageResource(determineIconId(fitem));
            holder.fileName.setText(filename);
            holder.percentage.setText(String.format("%d%%", progressInPercent));
            holder.progressBar.setMax(100);
            holder.progressBar.setProgress(progressInPercent);

            return row;
        }


        class UploadEntryHolder {
            TextView fileName;
            TextView percentage;
            ProgressBar progressBar;
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.downloads_activity);
        _fooshare = (FooshareApplication) getApplication();

        _downloadsList = _fooshare.getDownloads(new Predicate<Download>() {
            public boolean pred(Download ele) {
                return true;
            }
        });

        for (Download d : _downloadsList)
            d.setUpdateReceiver(_downloadUpdateReceiver);

        _downloadsListAdapter = new DownloadItemAdapter(this, R.layout.list_downloads_entry, _downloadsList);
        _downloadsListView = (ListView) findViewById(R.id.list_downloads);
        _downloadsListView.setAdapter(_downloadsListAdapter);

        // ++++++++++++++++++++++++++++++++++++++++

        _uploadsList = _fooshare.getUploads(new Predicate<Upload>() {
            public boolean pred(Upload ele) {
                return true;
            }
        });

        for (Upload d : _uploadsList)
            d.setUpdateReceiver(_uploadUpdateReceiver);

        _uploadsListAdapter = new UploadItemAdapter(this, R.layout.list_uploads_entry, _uploadsList);
        _uploadsListView = (ListView) findViewById(R.id.list_uploads);
        _uploadsListView.setAdapter(_uploadsListAdapter);

        // +++++++++++++++++++++++++++++++++++++++++

        _fooshare.onDownloadsListChanged.subscribe(new DownloadListChanged());
        _fooshare.onUploadsListChanged.subscribe(new UploadListChanged());
    }

    @Override
    protected void onResume() {
        super.onResume();
        _fooshare.checkin();

        _downloadsList = _fooshare.getDownloads(new Predicate<Download>() {
            public boolean pred(Download ele) {
                return true;
            }
        });
        for (Download d : _downloadsList)
            d.setUpdateReceiver(_downloadUpdateReceiver);

        _downloadsListAdapter = new DownloadItemAdapter(this, R.layout.list_downloads_entry, _downloadsList);
        _downloadsListView.setAdapter(_downloadsListAdapter);

        // ++++++++++++++++++++++++++++++++++++++++

        _uploadsList = _fooshare.getUploads(new Predicate<Upload>() {
            public boolean pred(Upload ele) {
                return true;
            }
        });
        for (Upload d : _uploadsList)
            d.setUpdateReceiver(_uploadUpdateReceiver);

        _uploadsListAdapter = new UploadItemAdapter(this, R.layout.list_uploads_entry, _uploadsList);
        _uploadsListView.setAdapter(_uploadsListAdapter);
    }

    public void onCancelDownloadClick(View view) {
        int position = _downloadsListView.getPositionForView((View) view.getParent());
        Download dl = _downloadsListAdapter.getItem(position);
        //_downloadsListAdapter.remove(dl);
        _fooshare.removeDownload(dl);
        dl.cancel();
    }

    public void onDownloadEntryClick(View view) {
        int position = _downloadsListView.getPositionForView((View) view.getParent());
        Download dlclicked = _downloadsListAdapter.getItem(position);
        switch (dlclicked.status()) {
        case FINISHED:
        case CANCELED:
        case FAILED:
            _fooshare.removeDownload(dlclicked);
        }
    }

    public class DownloadUpdateReceiver extends ResultReceiver {

        public DownloadUpdateReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            _downloadsListAdapter.notifyDataSetChanged();
        }
    }


    public class UploadUpdateReceiver extends ResultReceiver {
        public UploadUpdateReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            _uploadsListAdapter.notifyDataSetChanged();
        }
    }
}
