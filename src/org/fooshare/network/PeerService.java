package org.fooshare.network;

import java.io.File;

import org.alljoyn.bus.BusException;
import org.alljoyn.bus.BusObject;
import org.fooshare.AlljoynPeer;
import org.fooshare.FooshareApplication;

public class PeerService implements IPeerService, BusObject {
    private static final String TAG = "PeerService";

    protected FooshareApplication _fooshare;

    public PeerService(FooshareApplication fooshare) {
        _fooshare = fooshare;
    }

    public String peerName() {
        return _fooshare.myName();
    }


    public AlljoynFileItem[] peerFiles() {
        File[] sharedFiles = _fooshare.myFiles();
        AlljoynFileItem[] res = new AlljoynFileItem[sharedFiles.length];
        for (int i = 0, len = res.length; i < len; ++i)
            res[i] = _fooshare.createAlljoynFileItem(sharedFiles[i]);

        return res;
    }

    public String peerFilesHash() {
        return _fooshare.storage().filesHash();
    }

    public int notifyFilesChanged(String peerId) {
        final AlljoynPeer p = (AlljoynPeer) _fooshare.findPeer(peerId);
        Thread fileUpdater = new Thread(new Runnable() {
            public void run() {
                p.updateFiles();
            }
        });
        fileUpdater.setDaemon(true);
        fileUpdater.start();

        return 0;
    }

    public FileServerInfo fileServerDetails() throws BusException {
        FileServerInfo info = new FileServerInfo();
        info.hostName = _fooshare.myHostName();
        info.port = _fooshare.getFileServerPort();

        return info;
    }
}
