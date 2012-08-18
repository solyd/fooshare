package org.fooshare.network;

import java.io.BufferedOutputStream;
import java.io.File;

import org.alljoyn.bus.BusException;
import org.fooshare.AlljoynPeer;
import org.fooshare.FileItem;
import org.fooshare.FooshareApplication;
import org.fooshare.IPeer;
import org.fooshare.network.Download.DownloadStatus;
import org.fooshare.network.IPeerService.FileServerInfo;
import org.fooshare.predicates.PeerIdPredicate;

import android.app.IntentService;
import android.content.Intent;
import android.os.ResultReceiver;
import android.util.Log;

public class DownloadService extends IntentService {
    private static final String TAG = "DownloadService";

    // data the download service expects to find in the intent
    public static final String FILENAME        = "fileName";
    public static final String FILESIZE        = "fileSize";
    public static final String OWNERID         = "ownerId";

    public DownloadService() {
        super("DownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        long fileSize = intent.getLongExtra(FILESIZE, 0);
        String fileName = intent.getStringExtra(FILENAME);
        String ownerId = intent.getStringExtra(OWNERID);
        FileItem fileItem = new FileItem(fileName, fileSize, ownerId);
        FooshareApplication fooshare = (FooshareApplication) getApplication();

        Log.i(TAG, String.format("(DL Service) Downloading %s from %s", fileName, ownerId));

        IPeer owner = fooshare.findPeer(new PeerIdPredicate(ownerId));
        if (owner == null || owner instanceof AlljoynPeer == false)
            return;

        IPeerService peerProxy = ((AlljoynPeer) owner).proxyObject();
        FileServerInfo remoteFileServerInfo;
        try {
            remoteFileServerInfo = peerProxy.fileServerDetails();
        }
        catch (BusException e) {
            Log.i(TAG, String.format("Couldn't get host of %s", ownerId));
            Download failed = new Download(fooshare, null, 0, fileItem, null);
            failed.setStatus(DownloadStatus.FAILED);
            fooshare.addDownload(failed);
            return;
        }

        String remoteHost = remoteFileServerInfo.hostName;
        int remotePort = remoteFileServerInfo.port;

        // Write received bytes to this
        File originalFile = new File(fileName);
        File downloadedFile = originalFile;
        int i = 1;
        while (downloadedFile.exists()) {
            downloadedFile = getNextIndexedFile(originalFile, i++);
        }

        BufferedOutputStream dlFileBuffed = fooshare.storage().getStream4Download(downloadedFile.getName());
        if (dlFileBuffed == null) {
            Log.e(TAG, "Couldn't open OutputStream for writing file " + fileName);
            return;
        }

        // Perform the actual download on a seperate thread
        Download download = new Download(fooshare, remoteHost, remotePort, fileItem, dlFileBuffed);
        fooshare.addDownload(download);
        download.start();
    }

    /**
     * Creates indexed file name. For example:
     * /tmp/song.mp3
     * /tmp/song(1).mp3
     * /tmp/song(2).mp3
     * @param fileName
     * @param index
     * @return
     */
    private File getNextIndexedFile(File file, int i) {

        String name = file.getName();
        int lastDotIndex = name.contains(".") ? name.lastIndexOf('.') : name.length();
        String dstName =
                name.substring(0, lastDotIndex) +
                String.format("(%d)", i) +
                name.substring(lastDotIndex);
        return new File(file.getParent(), dstName);
    }
}
