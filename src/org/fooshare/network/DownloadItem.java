package org.fooshare.network;

import org.fooshare.network.DownloadService.Downloader;

import android.os.ResultReceiver;

public class DownloadItem {
    public String remoteHost;
    public int remotePort;
    public String fileName;
    public long fileSize;
    public ResultReceiver resRecv;

    private Downloader _downloader;

    public DownloadItem(String remoteHost,
                        int remotePort,
                        String fileName,
                        long fileSize,
                        ResultReceiver resRecv) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.resRecv = resRecv;
    }

    public void registerDownloader(Downloader dl) {
        _downloader = dl;
    }

    public void stopDownload() {
        _downloader.stop();
    }
}
