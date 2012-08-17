package org.fooshare;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.fooshare.events.Event;
import org.fooshare.network.AlljoynService;
import org.fooshare.network.Download;
import org.fooshare.network.DownloadService;
import org.fooshare.network.FileServer.Upload;
import org.fooshare.network.FileServerService;
import org.fooshare.network.IPeerService.AlljoynFileItem;
import org.fooshare.predicates.Predicate;
import org.fooshare.storage.IStorage;
import org.fooshare.storage.Storage;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

public class FooshareApplication extends Application {
    private static final String TAG = "FooshareApplication";

    public static final String APPNAME = "fooshare";
    public static final String PROGRESS = "progress";

    // TODO remove this
    /*
    private static final String _id = "p" + UUID.randomUUID().toString().replace("-", "");
    private static final String _name = UUID.randomUUID().toString().substring(0, 5);
    */

    private final Object             _peerslock = new Object();
    private final Map<String, IPeer> _peers = new HashMap<String, IPeer>();

    private final Object               _downloadsLock = new Object();
    private final Collection<Download> _downloads = new LinkedList<Download>();

    private final Object             _uploadsLock = new Object();
    private final Collection<Upload> _uploads = new LinkedList<Upload>();

    // Determines how many concurrent downloads/uploads are allowed
    private int    _uploadSlots = 10;
    private int    _downloadSlots = 10;

    // This service facilitates all network related activities.
    private AlljoynService    _alljoynService;
    private FileServerService _fileServerService;
    private IStorage          _storage;

    // This is the peer that was selected in the Peers activity.
    // This is needed in order to show only this peers files in the
    // Search activty.
    private IPeer _selectedPeer = null;

    public void setSelectedPeer(IPeer peer) {
        _selectedPeer = peer;
    }

    public IPeer getSelectedPeer() {
        return _selectedPeer;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate()");

        _storage = new Storage(getApplicationContext());
        Log.i(TAG, "Generated id: " + _storage.getUID());

        checkin();
    }

    /**
     * When the alljoyn service is up and running it registers itself with
     * FooshareApplication, so that FooshareApplication will be able to use it
     * for networking related tasks (finding peers / transferring files, etc.)
     * @param s - The alljoyn service object.
     */
    public void registerAlljoynService(AlljoynService s) {
        assert(_alljoynService == null);
        _alljoynService = s;
    }

    public void registerFileServer(FileServerService fs) {
        assert(_fileServerService == null);
        _fileServerService = fs;
    }

    /**
     * Should be called to ensure that all background threads/services are up and
     * running. For example - the Alljoyn Service and the File Server.
     */
    public void checkin() {
        initAlljoynService();
        initFileServerService();
    }

    /**
     * Checks if alljoyn service requires restart, and restarts if it does.
     * @return false if no restart was required, true otherwise
     */
    private void initAlljoynService() {
        if (_alljoynService == null)
            startService(new Intent(this, AlljoynService.class));
        else
            _alljoynService.init();
    }

    private void initFileServerService() {
        if (_fileServerService == null)
            startService(new Intent(this, FileServerService.class));
        else
            _fileServerService.init();
    }

    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    public final Event<IPeer>       onPeerDiscovered = new Event<IPeer>();
    public final Event<IPeer>       onPeerLost = new Event<IPeer>();

    public final Event<List<IPeer>>    onPeerListChanged = new Event<List<IPeer>>();
    public final Event<List<Download>> onDownloadsListChanged = new Event<List<Download>>();
    public final Event<List<Upload>>   onUploadsListChanged = new Event<List<Upload>>();

    /**
     * On first startup, the application will generate a unique id.
     * @return The unique id of this instance of the application
     */
    public String myId() {
        return _storage.getUID();
    }

    public String myName() {
        return _storage.getNickName();
    }

    public File[] myFiles() {
        return _storage.getMySharedFiles();
    }

    public AlljoynFileItem createAlljoynFileItem(File f) {
        AlljoynFileItem fitem = new AlljoynFileItem();

        if (f == null || !f.exists() || !f.isFile())
            return null;

        try {
            fitem.fullName = f.getCanonicalPath();
        }
        catch (IOException e) {
            Log.i(TAG, Log.getStackTraceString(e));
        }

        fitem.sizeInBytes = f.length();
        fitem.ownerId = _storage.getUID();

        return fitem;
    }

    public void addPeer(IPeer peer) {
        synchronized (_peerslock) {
            _peers.put(peer.id(), peer);
            onPeerDiscovered.trigger(peer);
            onPeerListChanged.trigger(new ArrayList<IPeer>(_peers.values()));
        }
    }

    /**
     * Finds the correct peer according to peerFinder predicate and removes it (if it exists).
     * @param peerFinder - A predicate which should return true for the peer to remove, false
     * for all other peers.
     */
    public void removePeer(Predicate<IPeer> peerFinder) {
        synchronized (_peerslock) {
            IPeer target = findPeer(peerFinder);
            if (target == null)
                return;

            _peers.remove(target.id());
            onPeerLost.trigger(target);
            onPeerListChanged.trigger(new ArrayList<IPeer>(_peers.values()));
        }
    }

    /**
     * usage example:
     * _fooshare.getPeers(new Predicate<IPeer>() {
     *      public boolean pred(IPeer ele) {
     *          return ele instanceof AlljoynPeer;
     *      }
     *  });
     *
     *
     * @param peerFinder
     * @return
     */
    public List<IPeer> getPeers(Predicate<IPeer> peerFinder) {
        synchronized (_peerslock) {
            List<IPeer> res = new ArrayList<IPeer>();
            for (IPeer p : _peers.values()) {
                if (peerFinder.pred(p))
                    res.add(p);
            }

            return res;
        }
    }

    public IPeer findPeer(String peerId) {
        synchronized (_peerslock) {
            return _peers.get(peerId);
        }
    }

    public IPeer findPeer(Predicate<IPeer> peerFinder) {
        synchronized (_peerslock) {
            for (IPeer p : _peers.values())
                if (peerFinder.pred(p))
                    return p;

            return null;
        }
    }

    public List<FileItem> getAllSharedFiles(Predicate<FileItem> fileFilter) {
        List<FileItem> res = new LinkedList<FileItem>();
        synchronized (_peerslock) {
            for (IPeer p : _peers.values()) {
                for (FileItem f : p.files()) {
                    if (fileFilter.pred(f))
                        res.add(f);
                }
            }
        }

        return res;
    }

    public void startDownloadService(FileItem fileItem) {
        Intent intent = new Intent(this, DownloadService.class);

        intent.putExtra(DownloadService.FILENAME, fileItem.getFullPath());
        intent.putExtra(DownloadService.FILESIZE, fileItem.sizeInBytes());
        intent.putExtra(DownloadService.OWNERID, fileItem.ownerId());

        startService(intent);
    }

    public void addDownload(Download dlItem) {
        assert(dlItem != null);
        synchronized (_downloadsLock) {
            _downloads.add(dlItem);
            onDownloadsListChanged.trigger(new ArrayList<Download>(_downloads));
        }
    }

    public void removeDownload(Download dlItem) {
        assert(dlItem != null);
        synchronized (_downloadsLock) {
            _downloads.remove(dlItem);
            onDownloadsListChanged.trigger(new ArrayList<Download>(_downloads));
        }
    }

    public void addUpload(Upload upload) {
        assert(upload != null);
        synchronized (_uploadsLock) {
            _uploads.add(upload);
            onUploadsListChanged.trigger(new ArrayList<Upload>(_uploads));
        }
    }

    public void removeUpload(Upload upload) {
        assert(upload != null);
        synchronized (_uploadsLock) {
            _uploads.remove(upload);
            onUploadsListChanged.trigger(new ArrayList<Upload>(_uploads));
        }
    }

    public List<Download> getDownloads(Predicate<Download> dlfilter) {
        synchronized (_downloadsLock) {
            List<Download> res = new LinkedList<Download>();
            for (Download d : _downloads) {
                if (dlfilter.pred(d))
                    res.add(d);
            }
            return res;
        }
    }

    public List<Upload> getUploads(Predicate<Upload> filter) {
        synchronized (_uploadsLock) {
            List<Upload> res = new LinkedList<Upload>();
            for (Upload d : _uploads) {
                if (filter.pred(d))
                    res.add(d);
            }
            return res;
        }
    }

    public void stopAllDownloads() {
        synchronized (_downloadsLock) {
            for (Download di : _downloads)
                di.cancel();
        }
    }

    public AlljoynService getAlljoynService() {
        return _alljoynService;
    }

    public IStorage storage() {
        return _storage;
    }

    public String myHostName() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress() && inetAddress.isSiteLocalAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e(TAG, Log.getStackTraceString(ex));
        }

        return null;
    }

    public int getFileServerPort() {
        assert(_fileServerService != null);
        return _fileServerService.fileServerPort();
    }

    public void quit() {
        onPeerDiscovered.clear();
        onPeerLost.clear();

        onPeerListChanged.clear();
        onDownloadsListChanged.clear();
        onUploadsListChanged.clear();

        _alljoynService = null;
        _fileServerService = null;

        stopAllDownloads();
        _downloads.clear();
        _uploads.clear();

        stopService(new Intent(this, AlljoynService.class));
        stopService(new Intent(this, FileServerService.class));
    }

    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    // TODO - ensure that service isn't null

    /**
     * Begin advertising this device on the network, enabling other peers to find it.
     */
    public void startSelfAdvertising() {
        _alljoynService.startAdvertising();
    }

    /**
     * Cancel the advertising of this device - other devices will no longer be able to
     * see it.
     */
    public void stopSelfAdvertising() {
        _alljoynService.stopAdvertising();
    }

    /**
     * Start looking for other devices
     */
    public void startPeerDiscovery() {
        _alljoynService.startPeerDiscovery();
    }

    /**
     * Stop looking for other devices
     */
    public void stopPeerDiscovery() {
        _alljoynService.stopPeerDiscovery();
    }
}
