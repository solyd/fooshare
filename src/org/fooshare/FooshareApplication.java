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
import org.fooshare.network.DownloadItem;
import org.fooshare.network.DownloadService;
import org.fooshare.network.FileServerService;
import org.fooshare.network.IPeerService.AlljoynFileItem;
import org.fooshare.predicates.Predicate;
import org.fooshare.storage.IStorage;
import org.fooshare.storage.Storage;

import android.app.Application;
import android.content.Intent;
import android.os.ResultReceiver;
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

    private final Object                   _dlItemsLock = new Object();
    private final Collection<DownloadItem> _downloads = new LinkedList<DownloadItem>();

    // Determines how many concurrent downloads/uploads are allowed
    private int    _uploadSlots = 10;
    private int    _downloadSlots = 10;


    // This service facilitates all network related activities.
    private AlljoynService    _alljoynService;
    private FileServerService _fileServerService;
    private IStorage          _storage;

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
        //initAlljoynService();
        //initFileServerService();
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
    public final Event<List<IPeer>> onPeerListChanged = new Event<List<IPeer>>();

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

    private void onPeerListChanged() {
        List<IPeer> peersCopy = new ArrayList<IPeer>();
        peersCopy.addAll(_peers.values());
        onPeerListChanged.trigger(peersCopy);
    }

    public void addPeer(IPeer peer) {
        synchronized (_peerslock) {
            _peers.put(peer.id(), peer);
            onPeerDiscovered.trigger(peer);
            onPeerListChanged();
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
            onPeerListChanged();
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
            for (IPeer p : _peers.values())
                res.addAll(p.files());
        }

        return res;
    }

    public void startDownloadService(FileItem fileItem, ResultReceiver resultReceiver) {
        Intent intent = new Intent(this, DownloadService.class);

        intent.putExtra(DownloadService.FILENAME, fileItem.getFullPath());
        intent.putExtra(DownloadService.FILESIZE, fileItem.getSizeInBytes());
        intent.putExtra(DownloadService.OWNERID, fileItem.getOwnerId());
        intent.putExtra(DownloadService.RESULT_RECEIVER, resultReceiver);

        startService(intent);
    }

    public void addDownloadItem(DownloadItem dlItem) {
        assert(dlItem != null);
        synchronized (_dlItemsLock) {
            _downloads.add(dlItem);
        }
    }

    public void removeDownloadItem(DownloadItem dlItem) {
        assert(dlItem != null);
        synchronized (_dlItemsLock) {
            _downloads.remove(dlItem);
        }
    }

    public List<DownloadItem> getDownloads(Predicate<DownloadItem> dlfilter) {
        synchronized (_dlItemsLock) {
            List<DownloadItem> res = new LinkedList<DownloadItem>();
            for (DownloadItem d : _downloads) {
                res.add(new DownloadItem(d));
            }
            return res;
        }
    }

    public void stopAllDownloads() {
        synchronized (_dlItemsLock) {
            for (DownloadItem di : _downloads)
                di.stopDownload();
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
        //_alljoynService.shutdown();
        _alljoynService = null;
        _fileServerService = null;
        stopAllDownloads();
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
