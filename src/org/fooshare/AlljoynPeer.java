package org.fooshare;

import org.alljoyn.bus.BusException;
import org.fooshare.network.IPeerService;
import org.fooshare.network.IPeerService.FileItem;

import android.util.Log;

public class AlljoynPeer implements IPeer {
    private static final String TAG = "AlljoynPeer";

    protected String       _id;
    protected String       _name;
    protected int          _sessionId;
    protected IPeerService _remotePeerProxy;
    protected FileItem[]   _sharedFiles;

    public AlljoynPeer(String id, int sessionId, IPeerService remotePeerProxy) {
        _id = id;
        _sessionId = sessionId;
        _remotePeerProxy = remotePeerProxy;

        try {
            _name = _remotePeerProxy.peerName();
            _sharedFiles = _remotePeerProxy.peerFiles();
            int x = 5;
        }
        catch (BusException ignore) {
            Log.i(TAG, Log.getStackTraceString(ignore));
        }
    }

    public String id() {
        return _id;
    }

    public String name() {
        return _name;
    }

    public FileItem[] files() {
        return _sharedFiles;
    }

    public int sessionId() {
        return _sessionId;
    }

    public IPeerService proxyObject() {
        return _remotePeerProxy;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof AlljoynPeer == false)
            return false;

        AlljoynPeer other = (AlljoynPeer) o;
        return _id.equals(other.id());
    }
}
