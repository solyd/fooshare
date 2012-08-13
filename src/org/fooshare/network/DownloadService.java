package org.fooshare.network;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
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

        // Write received bytes to this
        File downloadedFile = new File(fileName);
        BufferedOutputStream dlFileBuffed = fooshare.storage().getStream4Download(downloadedFile.getName());
        if (dlFileBuffed == null) {
            Log.e(TAG, "Couldn't open OutputStream for writing file " + fileName);
            return;
        }

        DownloadItem dlItem = new DownloadItem(remoteHost, remotePort, fileName, fileSize, resultReceiver);
        Downloader dler = new Downloader(dlItem, dlFileBuffed);
        dler.start();
    }

    public class Downloader implements Runnable {
        private static final String TAG = "Downloader";

        private DownloadItem         _dlItem;
        private BufferedOutputStream _dlFileStream;
        private Socket               _dlSocket;
        private Thread               _dlThread;
        private boolean              _isDownloading = false;

        public Downloader(DownloadItem dlItem, BufferedOutputStream resultStream) {
            _dlItem = dlItem;
            _dlItem.registerDownloader(this);
            _dlFileStream = resultStream;
        }

        public void start() {
            try {
                _dlSocket = new Socket(_dlItem.remoteHost, _dlItem.remotePort);
                _isDownloading = true;
                ((FooshareApplication) getApplication()).addDownloadItem(_dlItem);

                _dlThread = new Thread(this);
                _dlThread.setDaemon(true);
                _dlThread.start();
            }
            catch (UnknownHostException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
            catch (IOException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }

        public void stop() {
            if (!isDownloading())
                return;

            try {
                _dlSocket.close();
                _dlThread.join();
            }
            catch (IOException e) {
                Log.i(TAG, Log.getStackTraceString(e));
            }
            catch (InterruptedException e) {
                Log.i(TAG, Log.getStackTraceString(e));
            }
            finally {
                _isDownloading = false;
            }
        }

        public boolean isDownloading() {
            return _isDownloading;
        }

        public void run() {
            try {
                // Outgoing comm channel to file server
                BufferedOutputStream outBuffed = new BufferedOutputStream(_dlSocket.getOutputStream(), BUFFER_SIZE);

                // Receive file through this
                BufferedInputStream inBuffed = new BufferedInputStream(_dlSocket.getInputStream(), BUFFER_SIZE);

                // Request the file from the file server running on remote peer
                outBuffed.write(_dlItem.fileName.getBytes());
                outBuffed.flush();

                byte[] buf = new byte[BUFFER_SIZE];
                int bytesRead = 0;
                long totalBytesRead = 0;
                while ((bytesRead = inBuffed.read(buf)) > 0) {
                    _dlFileStream.write(buf, 0, bytesRead);
                    totalBytesRead += bytesRead;

                    if (totalBytesRead % 4096 == 0 || _dlItem.fileSize < 32768) {
                        // publish progress to ui
                        Bundle progressData = new Bundle();
                        progressData.putLong(PROGRESS_DOWN ,totalBytesRead);
                        progressData.putLong(PROGRESS_LEFT, _dlItem.fileSize);
                        _dlItem.resRecv.send(0, progressData);
                    }
                }

                _dlFileStream.close();
                _dlSocket.close();

                Log.i(TAG, String.format("Finished downloading %s, total = %d (expected: %d)", _dlItem.fileName, totalBytesRead, _dlItem.fileSize));
            }
            catch (UnknownHostException e) {
                Log.i(TAG, Log.getStackTraceString(e));
            }
            catch (IOException e) {
                Log.i(TAG, Log.getStackTraceString(e));
            }
            finally {
                ((FooshareApplication) getApplication()).removeDownloadItem(_dlItem);
            }
        }
    }
}
