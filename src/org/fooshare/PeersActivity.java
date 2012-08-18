package org.fooshare;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.fooshare.R;
import org.fooshare.R.drawable;
import org.fooshare.R.id;
import org.fooshare.R.layout;
import org.fooshare.R.string;
import org.fooshare.events.Delegate;
import org.fooshare.predicates.Predicate;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

class DPeer implements IPeer{
	String mName;
	Collection<FileItem> mFiles;

	public DPeer (String _name, Collection<FileItem> _files) {
		mName = _name;
		mFiles = _files;
	}

	public String id() {
		return null;
	}

	public String name() {
		return mName;
	}

	public Collection<FileItem> files() {
		return mFiles;
	}
}


class PeerListEntryAdapter extends ArrayAdapter<IPeer> {

    private static final String TAG = "PeerListEntryAdapter";
    Context context;
    int layoutResourceId;
    List<IPeer> data;
    List<IPeer> checkedFiles;

    public PeerListEntryAdapter(Context context, int layoutResourceId, List<IPeer> arr) {
        super(context, layoutResourceId, arr);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = arr;
        this.checkedFiles = new ArrayList<IPeer>();
    }

    public List<IPeer> getCheckedFiles() {
        return checkedFiles;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            final PeerEntryHolder holder = new PeerEntryHolder();
            holder.size = (TextView) row.findViewById(R.id.peer_entry_size);
            holder.name = (TextView) row.findViewById(R.id.peer_entry_name);

            row.setTag(holder);
        }
        else {
            row = convertView;
        }

        PeerEntryHolder holder = (PeerEntryHolder) row.getTag();

        int t = (data.get(position)).files().size();
        String tt = Integer.toString(t);
        holder.size.setText(tt);
        holder.name.setText(data.get(position).name());
        return row;
    }

    /* This function adds an element to the array of entries */
    @Override
    public void add(IPeer newEntry) {
        //Log.i(TAG, ("array size  = %d" + (data.size())));
        data.add(newEntry);
    }

    static class PeerEntryHolder {
        TextView name;
        TextView size;
    }
}

public class PeersActivity extends Activity {
    private static final String TAG = "PeersActivity";

    private static final String NAME = "File Name";
    private static final String SIZE = "Size";
    private static final String RNAME = "RFile Name";
    private static final String RSIZE = "RSize";

    private Handler _uiHandler = new Handler(Looper.getMainLooper());

    private class PeerListChanged implements Delegate<List<IPeer>> {
        public void invoke(final List<IPeer> newPeerList) {
            _uiHandler.post(new Runnable() {
                public void run() {
                    mPeerList.clear();
                    mPeerList.addAll(newPeerList);
                    mPeersListAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    private FooshareApplication _fooshare;

    private PeerListEntryAdapter mPeersListAdapter;
    private ListView mPeersListView;


    private List<IPeer> mPeerList;
    // specifies how to sort the list
    private String sortFlag = NAME;

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume of peers activity");
        _fooshare.checkin();

        mPeerList.clear();
        mPeerList.addAll(_fooshare.getPeers(new Predicate<IPeer>() {
            public boolean pred(IPeer ele) {
                return true;
            }
        }));
        mPeersListAdapter.notifyDataSetChanged();
    }

    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate of peer activity");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.peers_activity);
        findViewById(R.id.p_arrow2).setVisibility(View.INVISIBLE);
        _fooshare = (FooshareApplication) getApplication();

        mPeerList = _fooshare.getPeers(new Predicate<IPeer>() {
            public boolean pred(IPeer ele) {
                return true;
            }
        });

        sortPeerList();
        mPeersListAdapter = new PeerListEntryAdapter(this, R.layout.peer_list_entry, mPeerList);
        mPeersListView = (ListView) findViewById(R.id.peers_list);
        mPeersListView.setAdapter(mPeersListAdapter);

        mPeersListView.setOnItemClickListener(new OnItemClickListener() {
        	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

        		IPeer peer = (IPeer) mPeersListAdapter.getItem(position);
        		_fooshare.setSelectedPeer(peer);

        		TabHost tabhost = ((TabHost) getParent().findViewById(android.R.id.tabhost));
        		tabhost.setCurrentTabByTag(getResources().getString (R.string.SEARCH_TAB_TAG));
        	}
		});

        _fooshare.onPeerListChanged.subscribe(new PeerListChanged());
    }

    public void sortPeerList() {
        Log.d(TAG, "Sorting peers list");

        if (sortFlag.equals(NAME))
            Collections.sort(mPeerList, new PeerComparatorByName());
        else if (sortFlag.equals(SIZE))
            Collections.sort(mPeerList, new PeerComparatorBySize());
        else
            Collections.reverse(mPeerList);
    }

    public void sortButtonClicked(View view) {

        int viewID =((TextView) view).getId();

        // Here it is decided what kind of sort should occur and the arrows adjust
         if (viewID == R.id.peers_activity_name) {
            if (sortFlag.equals(NAME)) {
                sortFlag = RNAME;
                ((ImageView) findViewById(R.id.p_arrow1)).setImageResource(R.drawable.arrow_down);
            }
            else {
                sortFlag = NAME;
                ((ImageView) findViewById(R.id.p_arrow1)).setImageResource(R.drawable.arrow_up);
                findViewById(R.id.p_arrow1).setVisibility(View.VISIBLE);
                findViewById(R.id.p_arrow2).setVisibility(View.INVISIBLE);
            }
        }
        else if (viewID == R.id.peers_activity_size){
            if (sortFlag.equals(SIZE)) {
                sortFlag = RSIZE;
                ((ImageView) findViewById(R.id.p_arrow2)).setImageResource(R.drawable.arrow_down);
            }
            else {
                ((ImageView) findViewById(R.id.p_arrow2)).setImageResource(R.drawable.arrow_up);
                findViewById(R.id.p_arrow2).setVisibility(View.VISIBLE);
                findViewById(R.id.p_arrow1).setVisibility(View.INVISIBLE);
                sortFlag = SIZE;
            }
        }

        sortPeerList();
        mPeersListAdapter = new PeerListEntryAdapter(this, R.layout.peer_list_entry, mPeerList);
        mPeersListView.setAdapter(mPeersListAdapter);
    }

}

class PeerComparatorByName implements Comparator<IPeer> {
    public int compare(IPeer f1, IPeer f2) {
        return f1.name().compareTo(f2.name());
    }
}

class PeerComparatorBySize implements Comparator<IPeer> {
    public int compare(IPeer f1, IPeer f2) {
        if (f1.files().size() < f2.files().size())
            return -1;
        else if (f1.files().size() == f2.files().size())
            return 0;
        else
            return 1;
    }
}
