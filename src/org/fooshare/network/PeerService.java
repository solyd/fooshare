package org.fooshare.network;

import java.io.File;

import org.alljoyn.bus.BusException;
import org.alljoyn.bus.BusObject;
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


    public FileItem[] peerFiles() {
        File[] sharedFiles = _fooshare.myFiles();
        FileItem[] res = new FileItem[sharedFiles.length];
        for (int i = 0, len = res.length; i < len; ++i)
            res[i] = _fooshare.createFileItem(sharedFiles[i]);

        return res;
    }

    public FileServerInfo fileServerDetails() throws BusException {
        FileServerInfo info = new FileServerInfo();
        info.hostName = _fooshare.myHostName();
        info.port = _fooshare.getFileServerPort();

        return info;
    }
}
