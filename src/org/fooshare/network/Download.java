package org.fooshare.network;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.fooshare.DownloadsActivity.DownloadUpdateReceiver;
import org.fooshare.FileItem;
import org.fooshare.FooshareApplication;

import android.os.Bundle;
import android.util.Log;

public class Download implements Runnable {
    private static final String TAG = "Download";

    protected static final int BUFFER_SIZE = 4096; // bytes

    private String                  _remoteHost;
    private int                     _remotePort;
    private FileItem                _fileItem;
    private volatile DownloadStatus _status;
    private Object                  _statusLock = new Object();

    private BufferedOutputStream   _dlFileStream;
    private Socket                 _dlSocket;
    private Thread                 _dlThread;

    // DownloadReciever is used to publish updates about the download
    // In order to be able to publish the updates to some thread it first
    // must obtain a reference to this download item. Then, create a
    // DownloadReciever object and register it with this download item
    // object.
    private long                   _progressInBytes;
    private DownloadUpdateReceiver _updateReceiver;
    private Object                 _updateLock = new Object();

    private FooshareApplication    _fooshare;

    public static enum DownloadStatus {
        NOT_STARTED,    // User requested the file to be download, but fooshare didn't start the download yet
        PASUED,         // user has paused the download
        DOWNLOADING,    // The download is in active progress
        FINISHED,       // Download has completed successfully
        CANCELED,       // Download was canceled by user
        FAILED          // Download has failed after start due to some reason
    }

    public Download(FooshareApplication fooshare,
                    String remoteHost,
                    int remotePort,
                    FileItem fileItem,
                    BufferedOutputStream resultStream) {

        _fooshare = fooshare;
        _remoteHost = remoteHost;
        _remotePort = remotePort;
        _fileItem = fileItem;
        _dlFileStream = resultStream;
        _status = DownloadStatus.NOT_STARTED;
    }

    public void setProgress(long progressInBytes) {
        _progressInBytes = progressInBytes;
    }

    public int getPercentageProgress() {
        return (int) (_progressInBytes * 100.0 / _fileItem.sizeInBytes());
    }

    public void setUpdateReceiver(DownloadUpdateReceiver updateReceiver) {
        synchronized (_updateLock) {
            _updateReceiver = updateReceiver;
        }
    }

    public void setStatus(DownloadStatus status) {
        synchronized (_statusLock) {
            switch (_status) {
            case PASUED:
                if (status == DownloadStatus.NOT_STARTED ||
                status == DownloadStatus.FINISHED)
                    return;
            case DOWNLOADING:
                if (status == DownloadStatus.NOT_STARTED)
                    return;
            case FAILED:
            case FINISHED:
            case CANCELED:
                return;
            }
            _status = status;
        }
    }

    public DownloadStatus status() {
        return _status;
    }

    public String remoteHost() {
        return _remoteHost;
    }

    public int remotePort() {
        return _remotePort;
    }

    public FileItem getFile() {
        return _fileItem;
    }

    public void start() {
        setStatus(DownloadStatus.DOWNLOADING);
        _dlThread = new Thread(this);
        _dlThread.setDaemon(true);
        _dlThread.start();
    }

    public void stop() {
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
            setStatus(DownloadStatus.CANCELED);
        }
    }

    public void pause() {
        // TODO
    }

    public void resume() {
        // TODO
    }

    public void run() {
        try {
            _dlSocket = new Socket(_remoteHost, _remotePort);

            // Outgoing comm channel to file server
            BufferedOutputStream outBuffed = new BufferedOutputStream(_dlSocket.getOutputStream(), BUFFER_SIZE);

            // Receive file through this
            BufferedInputStream inBuffed = new BufferedInputStream(_dlSocket.getInputStream(), BUFFER_SIZE);

            // Request the file from the file server running on remote peer
            outBuffed.write(_fileItem.getFullPath().getBytes());
            outBuffed.flush();

            byte[] buf = new byte[BUFFER_SIZE];
            int bytesRead = 0;
            _progressInBytes = 0;
            while ((bytesRead = inBuffed.read(buf)) > 0) {
                _dlFileStream.write(buf, 0, bytesRead);
                _progressInBytes += bytesRead;


                // TODO determien when its best to update and remove magic numbers
                if (_progressInBytes % 4096 == 0 || _fileItem.sizeInBytes() < 32768) {

                    // publish progress to ui
                    synchronized (_updateLock) {
                        if (_updateReceiver != null) {
                            _updateReceiver.send(0, null);
                        }
                    }
                }
            }

            _dlFileStream.close();
            _dlSocket.close();

            Log.i(TAG, String.format("Finished downloading %s, total = %d (expected: %d)", _fileItem.name(), _progressInBytes, _fileItem.sizeInBytes()));
        }
        catch (UnknownHostException e) {
            Log.i(TAG, Log.getStackTraceString(e));
        }
        catch (IOException e) {
            Log.i(TAG, Log.getStackTraceString(e));
        }
        finally {
            if (_progressInBytes == _fileItem.sizeInBytes())
                setStatus(DownloadStatus.FINISHED);
            else
                setStatus(DownloadStatus.FAILED);
        }
    }
}
