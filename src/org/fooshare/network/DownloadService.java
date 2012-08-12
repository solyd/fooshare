package org.fooshare.network;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.alljoyn.bus.BusException;
import org.fooshare.AlljoynPeer;
import org.fooshare.FooshareApplication;
import org.fooshare.IPeer;
import org.fooshare.network.IPeerService.FileServerInfo;
import org.fooshare.predicates.PeerIdPredicate;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

public class DownloadService extends IntentService {
    private static final String TAG = "DownloadService";

    protected static final int BUFFER_SIZE = 4096; // bytes

    // data the download service expects to find in the intent
    public static final String FILENAME        = "fileName";
    public static final String FILESIZE        = "fileSize";
    public static final String OWNERID         = "ownerId";
    public static final String RESULT_RECEIVER = "resultReceiver";

    public static final String PROGRESS_DOWN = "downloaded";
    public static final String PROGRESS_LEFT = "leftToDownload";

    public DownloadService() {
        super("DownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        long fileSize = intent.getLongExtra(FILESIZE, 0);
        String fileName = intent.getStringExtra(FILENAME);
        String ownerId = intent.getStringExtra(OWNERID);
        FooshareApplication fooshare = (FooshareApplication) getApplication();
        ResultReceiver resultReceiver = (ResultReceiver) intent.getParcelableExtra(RESULT_RECEIVER);

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
            return;
        }

        String remoteHost = remoteFileServerInfo.hostName;
        int remotePort = remoteFileServerInfo.port;

        try {
            Socket sock = new Socket(remoteHost, remotePort);

            // Outgoing comm channel to file server
            BufferedOutputStream outBuffed = new BufferedOutputStream(sock.getOutputStream(), BUFFER_SIZE);

            // Receive file through this
            BufferedInputStream inBuffed = new BufferedInputStream(sock.getInputStream(), BUFFER_SIZE);

            // Write received bytes to this
            File downloadedFile = new File(fileName);
            BufferedOutputStream dlFileBuffed = fooshare.storage().getStream4Download(downloadedFile.getName());
            //BufferedOutputStream dlFileBuffed = new BufferedOutputStream(new FileOutputStream(downloadedFile), BUFFER_SIZE * 4);

            // Request the file from the file server running on remote peer
            outBuffed.write(fileName.getBytes());
            outBuffed.flush();

            byte[] buf = new byte[BUFFER_SIZE];
            int bytesRead = 0;
            long totalBytesRead = 0;
            while ((bytesRead = inBuffed.read(buf)) > 0) {
                dlFileBuffed.write(buf, 0, bytesRead);
                totalBytesRead += bytesRead;

                // publish progress to ui
                Bundle progressData = new Bundle();
                progressData.putLong(PROGRESS_DOWN ,totalBytesRead);
                progressData.putLong(PROGRESS_LEFT, fileSize);
                resultReceiver.send(0, progressData);
            }

            dlFileBuffed.close();
            sock.close();

            Log.i(TAG, String.format("Finished downloading %s, total = %d (expected: %d)", fileName, totalBytesRead, fileSize));
        }
        catch (UnknownHostException e) {
            Log.i(TAG, Log.getStackTraceString(e));
        }
        catch (IOException e) {
            Log.i(TAG, Log.getStackTraceString(e));
        }
    }
}
