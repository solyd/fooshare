package org.fooshare;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import org.alljoyn.bus.BusException;
import org.fooshare.network.IPeerService;
import org.fooshare.network.IPeerService.AlljoynFileItem;

import android.util.Log;

public class AlljoynPeer implements IPeer {
    private static final String TAG = "AlljoynPeer";

    private String               _id;
    private String               _name;
    private int                  _sessionId;
    private IPeerService         _remotePeerProxy;
    private Collection<FileItem> _sharedFiles = new ArrayList<FileItem>();
    private Object               _filesLock = new Object();

    public AlljoynPeer(String id, int sessionId, IPeerService remotePeerProxy) {
        _id = id;
        _sessionId = sessionId;
        _remotePeerProxy = remotePeerProxy;

        try {
            _name = _remotePeerProxy.peerName();
            AlljoynFileItem[] peerFiles = _remotePeerProxy.peerFiles();
            for (AlljoynFileItem pf : peerFiles)
                _sharedFiles.add(new FileItem(pf.fullName, pf.sizeInBytes, pf.ownerId));
        }
        catch (BusException ignore) {
            Log.e(TAG, Log.getStackTraceString(ignore));
        }
    }

    public String id() {
        return _id;
    }

    public String name() {
        return _name;
    }

    public Collection<FileItem> files() {
        synchronized (_filesLock) {
            return _sharedFiles;
        }
    }

    public void updateFiles() {
        synchronized (_filesLock) {
            _sharedFiles.clear();
            try {
                AlljoynFileItem[] peerFiles = _remotePeerProxy.peerFiles();
                for (AlljoynFileItem pf : peerFiles)
                    _sharedFiles.add(new FileItem(pf.fullName, pf.sizeInBytes, pf.ownerId));
            }
            catch (BusException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }
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
