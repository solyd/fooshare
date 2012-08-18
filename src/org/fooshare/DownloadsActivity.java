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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SlidingDrawer;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;
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
                holder.fileName = (TextView) row.findViewById(R.id.downloads_fileName);
                holder.status = (TextView) row.findViewById(R.id.downloads_status);
                holder.percentage = (TextView) row.findViewById(R.id.downloads_percentage);
                holder.progressBar = (ProgressBar) row.findViewById(R.id.downloads_progressbar);
                holder.totalSize = (TextView) row.findViewById(R.id.downloads_activity_file_size);

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
            holder.status.setText(dlitem.status().toString());
            holder.percentage.setText(String.format("%d%%", progressInPercent));
            holder.progressBar.setMax(100);
            holder.progressBar.setProgress(progressInPercent);
            holder.totalSize.setText(fitem.getAdjustedSize());

            return row;
        }


        class DownloadEntryHolder {
            ImageButton imageButton;
            TextView fileName;
            TextView status;
            TextView percentage;
            TextView totalSize;
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
                holder.fileName = (TextView) row.findViewById(R.id.uploads_fileName);
                holder.percentage = (TextView) row.findViewById(R.id.uploads_percentage);
                holder.progressBar = (ProgressBar) row.findViewById(R.id.uploads_progressbar);
                holder.status = (TextView) row.findViewById(R.id.uploads_status);
                holder.totalSize = (TextView) row.findViewById(R.id.uploads_file_size);

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
            holder.status.setText(item.status().toString());
            holder.totalSize.setText(item.getAdjustedSize());

            return row;
        }


        class UploadEntryHolder {
            TextView fileName;
            TextView status;
            TextView percentage;
            TextView totalSize;
            ProgressBar progressBar;
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.downloads_activity);
        _fooshare = (FooshareApplication) getApplication();
        _uploadsSlideDrawer = (SlidingDrawer) findViewById(R.id.uploads_drawer);
        _uploadsSlideDrawer.setOnDrawerOpenListener(onClick_DrawerOpened);
        _uploadsSlideDrawer.setOnDrawerCloseListener(onClick_DrawerClosed);

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
    protected void onDestroy() {
        super.onDestroy();

        _fooshare.quit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        _fooshare.checkin();
        _uploadsSlideDrawer.close();

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
        try {
            int position = _downloadsListView.getPositionForView((View) view.getParent());
            if (position < 0)
                return;

            Log.d(TAG, "onDownloadEntryClick() position: " + position);
            Download dlclicked = _downloadsListAdapter.getItem(position);
            switch (dlclicked.status()) {
            case FINISHED:
            case CANCELED:
            case FAILED:
                _fooshare.removeDownload(dlclicked);
                //            _downloadsList.remove(dlclicked);
                //            _downloadsListAdapter.notifyDataSetChanged();
            }
        }
        catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    public void onUploadEntryClick(View view) {
        try {
            int position = _uploadsListView.getPositionForView((View) view.getParent());
            if (position < 0)
                return;

            Log.d(TAG, "onUploadEntryClick() position: " + position);
            Upload dlclicked = _uploadsListAdapter.getItem(position);
            switch (dlclicked.status()) {
            case FINISHED:
            case CANCELED:
            case FAILED:
                _fooshare.removeUpload(dlclicked);
                //            _uploadsList.remove(dlclicked);
                //            _uploadsListAdapter.notifyDataSetChanged();
            }
        }
        catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    public void onUploadsClearListClick(View view) {
        Log.d(TAG, "on upload clear list click");
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

    private SlidingDrawer _uploadsSlideDrawer;

    //sliderdrawer close
    private OnDrawerCloseListener onClick_DrawerClosed = new OnDrawerCloseListener() {

        public void onDrawerClosed() {
            _uploadsSlideDrawer.setClickable(false);
        }
    };

    //sliderdrawer open
    private OnDrawerOpenListener onClick_DrawerOpened = new OnDrawerOpenListener() {

        public void onDrawerOpened() {
            _uploadsSlideDrawer.setClickable(true);
        }
    };


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
