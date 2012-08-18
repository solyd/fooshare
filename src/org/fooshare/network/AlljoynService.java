package org.fooshare.network;

import java.util.Collection;

import org.alljoyn.bus.BusListener;
import org.alljoyn.bus.SessionListener;
import org.alljoyn.bus.SessionOpts;
import org.alljoyn.bus.SessionPortListener;
import org.fooshare.AlljoynPeer;
import org.fooshare.FooshareApplication;
import org.fooshare.IPeer;
import org.fooshare.events.Delegate;
import org.fooshare.network.AlljoynBusHandler.SessionInfo;
import org.fooshare.predicates.Predicate;
import org.fooshare.predicates.SessionIdPredicate;

import android.app.Service;
import android.content.Intent;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

public class AlljoynService extends Service {
    private static final String TAG = "AlljoynService";

    private String              _myServiceName;
    private FooshareApplication _fooshare;
    private boolean             _isReady = false;

    private static final short  PEER_PORT        = 42;
    private static final String PEER_PREFIX      = "fooshare.peers";

    // TODO - keep track of all current open raw services in order to be able to gracefully
    // shut them down in case of sudden app exit

    static {
        Log.i(TAG, "Loading the alljoyn_java library");
        System.loadLibrary("alljoyn_java");
    }

    private AlljoynBusHandler _busHandler;

    @Override
    public void onCreate() {
        Log.i(TAG, "AlljoynService created");

        _fooshare = (FooshareApplication) getApplication();
        _fooshare.registerAlljoynService(this);
        _myServiceName = idToServicename(_fooshare.myId());

        // Start up the thread running the service. Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.
        HandlerThread busThread = new HandlerThread("AlljoynBusHandler");
        busThread.start();
        _busHandler = new AlljoynBusHandler(busThread.getLooper(), _fooshare);

        init();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "AlljoynService destroyed");

        shutdown();
        _busHandler.exit();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    public boolean init() {
        if (_isReady)
            return false;

        _busHandler.connect();
        startPeerDiscovery();
        startAdvertising();

        _isReady = true;
        return true;
    }

    public void shutdown() {
        if (!_isReady)
            return;

        // Leaves sessions with all peers
        Collection<IPeer> peers = _fooshare.getPeers(new Predicate<IPeer>() {
            public boolean pred(IPeer ele) {
                return ele instanceof AlljoynPeer;
            }
        });
        for (IPeer p : peers)
            _busHandler.leaveSession(((AlljoynPeer) p).sessionId());

        stopAdvertising();
        stopPeerDiscovery();

        _busHandler.disconnect();

        _isReady = false;
    }

    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    public static String idToServicename(String id) {
        return PEER_PREFIX + "." + id;
    }


    public static  String servicenameToId(String serviceName) {
        String[] parts = serviceName.split("\\.");
        return parts[parts.length - 1];
    }

    public void startAdvertising() {
        _busHandler.requestName(_myServiceName);
        _busHandler.bindSession(new SessionPortListener() {
                                    public boolean acceptSessionJoiner(short sessionPort, String joiner, SessionOpts sessionOpts) {
                                        Log.i(TAG, "acceptSessionJoiner: " + sessionPort + ", " + joiner + ", " + sessionOpts.toString());
                                        if (sessionPort == PEER_PORT)
                                            return true;
                                        return false;
                                    }

                                    /**
                                     * If we return true in acceptSessionJoiner, we admit a new client
                                     * into our session.  The session does not really exist until a
                                     * client joins, at which time the session is created and a session
                                     * ID is assigned.  This method communicates to us that this event
                                     * has happened, and provides the new session ID for us to use.
                                     */
                                    public void sessionJoined(short sessionPort, int id, String joiner) {
                                        Log.i(TAG, "sessionJoined: " + sessionPort + ", " + id + ", " + joiner);
                                    }
                                },
                                PEER_PORT,
                                AlljoynBusHandler.TYPE_REGULAR);
        _busHandler.advertise(_myServiceName);
    }

    public void stopAdvertising() {
        _busHandler.cancelAdvertise(_myServiceName);
        _busHandler.releaseName(_myServiceName);
        _busHandler.unbindSession(PEER_PORT);
    }

    public void startPeerDiscovery() {
        _busHandler.onJoinSession.subscribe(_peerDiscovered);
        _busHandler.startDiscovery(PEER_PREFIX);
    }

    public void stopPeerDiscovery() {
        _busHandler.onJoinSession.unsubscribe(_peerDiscovered);
        _busHandler.stopDiscovery(PEER_PREFIX);
    }


    // This is one of the possible delegates to be invoked when we join a session
    // with remote service. Note that usually this means we found a peer service,
    // but not always! So we need to check first what service name we joined...
    // Also - we keep a reference to this object because we need to be able
    // to unsubscribe from the event of join session
    protected PeerDiscovered _peerDiscovered = new PeerDiscovered();
    protected class PeerDiscovered implements Delegate<SessionInfo> {
        public void invoke(SessionInfo info) {
            if (info.type != AlljoynBusHandler.TYPE_REGULAR || !info.serviceName.startsWith(PEER_PREFIX))
                return;

            IPeerService remotePeer = _busHandler.getProxyObject(info.serviceName,
                                                                 AlljoynBusHandler.PEER_SERVICENAME,
                                                                 info.sessionId,
                                                                 IPeerService.class);
            _fooshare.addPeer(new AlljoynPeer(servicenameToId(info.serviceName),
                                              info.sessionId,
                                              remotePeer));
        }

    }

    public class FooshareBusListener extends BusListener {
        public void foundAdvertisedName(String serviceName, short transport, String namePrefix) {
            Log.i(TAG, "foundAdvertisedName: " + serviceName);

            if (_myServiceName.equals(serviceName))
                return;

            _busHandler.joinSession(new SessionListener() {
                                        @Override
                                        public void sessionLost(int sessionId) {
                                            Log.i(TAG, String.format("sessionLost, id: %d", sessionId));
                                            _fooshare.removePeer(new SessionIdPredicate(sessionId));
                                        }

                                    },
                                    serviceName,
                                    PEER_PORT,
                                    AlljoynBusHandler.TYPE_REGULAR);
        }

        public void lostAdvertisedName(String name, short transport, String namePrefix) {
            Log.i(TAG, "lostAdvertisedName: " + name);
        }
    }
}










