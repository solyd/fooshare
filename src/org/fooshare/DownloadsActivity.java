package org.fooshare;

import java.util.List;

import org.fooshare.events.Delegate;
import org.fooshare.network.Download;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DownloadsActivity extends Activity {
    private DownloadItemAdapter _downloadsListAdapter;
    private ListView            _downloadsListView;
    private List<Download>      _downloadsList;
    private FooshareApplication _fooshare;

    private Handler _uiHandler = new Handler(Looper.getMainLooper());
    private DownloadUpdateReceiver _updateReceiver = new DownloadUpdateReceiver(new Handler());

    private class DownloadListChanged implements Delegate<List<Download>> {
        public void invoke(final List<Download> newDownloadsList) {
            _uiHandler.post(new Runnable() {
                public void run() {
                    _downloadsList.clear();
                    for (Download di : newDownloadsList) {
                        di.setUpdateReceiver(_updateReceiver);
                        _downloadsList.add(di);
                    }

                    _downloadsListAdapter.notifyDataSetChanged();
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

            Download dlitem = getItem(position);
            FileItem fitem = dlitem.getFile();
            int progressInPercent = dlitem.getPercentageProgress();

            holder.icon.setImageResource(determineIconId(fitem));
            holder.fileName.setText(fitem.name());
            holder.eta.setText("ETA_TODO");
            holder.percentage.setText(String.format("%d", progressInPercent));
            holder.progressBar.setMax(100);
            holder.progressBar.setProgress(progressInPercent);

            return row;
        }

        private int determineIconId(FileItem fileItem) {
            switch (fileItem.category()) {
            case VIDEO:
                return R.drawable.video_icon;
            case TEXT:
                return R.drawable.text_icon;
            case AUDIO:
                return R.drawable.audio_icon;
            default:
                // TODO have a binary icon
                return R.drawable.text_icon;
            }
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

        _downloadsList = _fooshare.getDownloads(new Predicate<Download>() {
            public boolean pred(Download ele) {
                return true;
            }
        });

        for (Download d : _downloadsList)
            d.setUpdateReceiver(_updateReceiver);

        _downloadsListAdapter = new DownloadItemAdapter(this, R.layout.list_downloads_entry, _downloadsList);
        _downloadsListView = (ListView) findViewById(R.id.list_downloads);
        _downloadsListView.setAdapter(_downloadsListAdapter);

        _fooshare.onDownloadsListChanged.subscribe(new DownloadListChanged());
    }

    @Override
    protected void onResume() {
        super.onResume();

        _downloadsList = _fooshare.getDownloads(new Predicate<Download>() {
            public boolean pred(Download ele) {
                return true;
            }
        });
        for (Download d : _downloadsList)
            d.setUpdateReceiver(_updateReceiver);

        _downloadsListAdapter = new DownloadItemAdapter(this, R.layout.list_downloads_entry, _downloadsList);
        _downloadsListView.setAdapter(_downloadsListAdapter);
    }

    public void onDemoClick(View view) {
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
}
