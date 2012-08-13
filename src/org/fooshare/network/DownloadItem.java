package org.fooshare.network;

import org.fooshare.R;
import org.fooshare.network.DownloadService.Downloader;

import android.os.ResultReceiver;

public class DownloadItem {
    public String remoteHost;
    public int remotePort;
    public String fileName;
    public long fileSize;
    public String fileType;
    public ResultReceiver resRecv;
    public int progress;
    public int iconTypeId;

    private Downloader _downloader;

    public DownloadItem(String remoteHost,
                        int remotePort,
                        String fileName,
                        long fileSize,
                        String fileType,
                        ResultReceiver resRecv) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileType = fileType;
        this.resRecv = resRecv;
        this.progress = 0;

        this.iconTypeId = getIconTypeId(this.fileType);
    }

    public DownloadItem(String remoteHost,
                        int remotePort,
                        String fileName,
                        long fileSize,
                        ResultReceiver resRecv) {
        this(remoteHost, remotePort, fileName, fileSize, "text", resRecv);
    }

    public DownloadItem(DownloadItem other) {
        this.remoteHost = other.remoteHost;
        this.remotePort = other.remotePort;
        this.fileName = other.fileName;
        this.fileSize = other.fileSize;
        this.fileType = other.fileType;
        this.resRecv = other.resRecv;
        this.progress = other.progress;
        this.iconTypeId = other.iconTypeId;
        this._downloader = other._downloader;
    }

    public void registerDownloader(Downloader dl) {
        _downloader = dl;
    }

    public void stopDownload() {
        _downloader.stop();
    }

    public int getPercentageProgress() {
        return (int) (progress * 1.0 / fileSize);
    }

    public boolean isDownloading() {
        return _downloader.isDownloading();
    }

    private int getIconTypeId(String fileType) {
        if (fileType.equals("video"))
            return R.drawable.video_icon;
        if (fileType.equals("audio"))
            return R.drawable.audio_icon;
        if (fileType.equals("text"))
            return R.drawable.text_icon;

        return 0;
    }
}
