package org.fooshare;

import java.util.Collection;
import java.util.List;

import org.fooshare.events.Delegate;
import org.fooshare.network.DownloadService;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TabHost;

public class DemoActivity extends Activity {
private static final String TAG = "DemoActivity";

    static final boolean flag = true;

    protected FooshareApplication _fooshare;

    private ArrayAdapter<String> _peerAdapter;
    private ArrayAdapter<FileItem> _fileAdapter;
    private ListView _peerListView;
    private ListView _fileListView;

    Handler handler = new Handler(Looper.getMainLooper());

    protected class PeerDiscovered implements Delegate<IPeer> {
        public void invoke(IPeer peer) {
            final String peerId = peer.id();
            final String peerName = peer.name();
            final Collection<FileItem> peerFiles = peer.files();
            handler.post(new Runnable() {
                public void run() {
                    _peerAdapter.add(peerId);

                    for (FileItem fi : peerFiles)
                        _fileAdapter.add(fi);
                }
            });
        }
    }

    protected class PeerLost implements Delegate<IPeer> {
        public void invoke(IPeer peer) {
            final String peerId = peer.id();
            final String peerName = peer.name();
            final Collection<FileItem> peerFiles = peer.files();
            handler.post(new Runnable() {
                public void run() {
                    _peerAdapter.remove(peerId);

                    for (FileItem fi : peerFiles)
                        _fileAdapter.remove(fi);
                }
            });
        }
    }

    protected class PeerListChanged implements Delegate<List<IPeer>> {
        public void invoke(final List<IPeer> newPeerList) {
            handler.post(new Runnable() {
                public void run() {
                    _peerAdapter.clear();
                    _fileAdapter.clear();

                    for (IPeer p : newPeerList) {
                        _peerAdapter.add(p.id());
                        for (FileItem fi : p.files())
                            _fileAdapter.add(fi);
                    }
                }
            });
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_main);

        _fooshare = (FooshareApplication) getApplication();

        _peerAdapter = new ArrayAdapter<String>(this, R.layout.list_item);
        _fileAdapter = new ArrayAdapter<FileItem>(this, R.layout.list_item);
//
//        _fileAdapter = new FileItemAdapter(this, R.layout.list_item, new ArrayList<FileItem>());


        _peerListView = (ListView) findViewById(R.id.PeerListView);
        _fileListView = (ListView) findViewById(R.id.FileListView);
        _peerListView.setAdapter(_peerAdapter);
        _fileListView.setAdapter(_fileAdapter);

        /*
        _fooshare.onPeerDiscovered.subscribe(new PeerDiscovered());
        _fooshare.onPeerLost.subscribe(new PeerLost());
        */
        _fooshare.onPeerListChanged.subscribe(new PeerListChanged());
        _fooshare.checkin();

        _peerAdapter.add("click me");
        // ++++++++++++++++++++++++++++++++++++++++

        _peerListView.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                String item = (String) _peerListView.getItemAtPosition(position);
            }
        });

        _fileListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                FileItem f = (FileItem) _fileAdapter.getItem(position);

                /*
                _progressDialog = new ProgressDialog(DemoActivity.this);
                _progressDialog.setCancelable(true);
                _progressDialog.setMessage("Downloading " + f.toString());
                _progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                _progressDialog.setProgress(0);
                _progressDialog.setMax(0);
                _progressDialog.show();
                 */

                _fooshare.startDownloadService(f, new DownloadReceiver(new Handler()));
            }
        });
        
        _peerListView.setOnItemClickListener(new OnItemClickListener() {
        	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        		TabHost tabhost = ((TabHost) getParent().findViewById(android.R.id.tabhost));
        		tabhost.setCurrentTab(0);
        	}
		});
    }

    ProgressDialog _progressDialog;

    private class DownloadReceiver extends ResultReceiver {
        public DownloadReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);

            long downloaded = resultData.getLong(DownloadService.PROGRESS_DOWN);
            long total = resultData.getLong(DownloadService.PROGRESS_LEFT);
            _progressDialog.setMax((int) total);
            _progressDialog.setProgress((int) downloaded);


            /*
            if (downloaded == total) {
                _progressDialog.dismiss();

            }
            */
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    public void onQuit(View v) {
        _fooshare.quit();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

}
