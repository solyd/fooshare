package org.fooshare.network;

import org.alljoyn.bus.BusAttachment;
import org.alljoyn.bus.BusListener;
import org.alljoyn.bus.Mutable;
import org.alljoyn.bus.ProxyBusObject;
import org.alljoyn.bus.SessionListener;
import org.alljoyn.bus.SessionOpts;
import org.alljoyn.bus.SessionPortListener;
import org.alljoyn.bus.Status;
import org.fooshare.FooshareApplication;
import org.fooshare.events.Event;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class AlljoynBusHandler extends Handler {
    private static final String TAG = "AlljoynBusHandler";

    protected BusAttachment       _bus;
    protected BusListener         _busListener;
    protected FooshareApplication _fooshare;
    protected AlljoynService      _alljoynService;
    protected PeerService         _peerService;

    public static final int    SESSION_TIMEOUT  = 30; // seconds
    public static final String PEER_SERVICENAME = "/PeerService";
    public static final String RAW_SERVICENAME  = "/RawPeerService";
    public static final int TYPE_REGULAR        = 0;
    public static final int TYPE_RAW            = 1;

    protected static final int CONNECT          = 0;
    protected static final int REQUEST_NAME     = 1;
    protected static final int BIND_SESSION     = 2;
    protected static final int BIND_RAW_SESSION = 3;
    protected static final int ADVERTISE        = 4;
    protected static final int CANCEL_ADVERTISE = 5;
    protected static final int UNBIND_SESSION   = 6;
    protected static final int RELEASE_NAME     = 7;
    protected static final int START_DISCOVERY  = 8;
    protected static final int STOP_DISCOVERY   = 9;
    protected static final int JOIN_SESSION     = 10;
    protected static final int JOIN_RAW_SESSION = 11;
    protected static final int LEAVE_SESSION    = 12;
    protected static final int DISCONNECT       = 13;
    protected static final int EXIT             = 14;

    public class SessionInfo {
        public final String serviceName;
        public final int port;
        public final int sessionId;
        // In case this is a raw session, the socked fd
        public final int sockFd;
        public final int type;

        public SessionInfo(int type, String serviceName, int sessionId) {
            this.type = type;
            this.serviceName = serviceName;
            this.sessionId = sessionId;

            this.port = -1;
            this.sockFd = -1;
        }

        public SessionInfo(int type, String serviceName, int sessionId, int port, int sockFd) {
            this.type = type;
            this.serviceName = serviceName;
            this.sessionId = sessionId;
            this.port = port;
            this.sockFd = sockFd;
        }
    }

    public final Event<SessionInfo> onJoinSession = new Event<SessionInfo>();

    public synchronized <T> T getProxyObject(String serviceName,
                                             String objPath,
                                             int sessionId,
                                             Class<T> serviceInterface) {
        ProxyBusObject proxyObj = _bus.getProxyBusObject(serviceName,
                                                         objPath,
                                                         sessionId,
                                                         new Class<?>[] { serviceInterface });
        return proxyObj.getInterface(serviceInterface);
    }

    public synchronized int getSessionFd(int sessionId) {
        Mutable.IntegerValue sockFd = new Mutable.IntegerValue();
        Status status = _bus.getSessionFd(sessionId, sockFd);
        Log.i(TAG, "getSessionFd(): " + status);

        return sockFd.value;
    }

    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    public void connect()                     { sendEmptyMessage(CONNECT); }
    public void disconnect()                  { sendEmptyMessage(DISCONNECT); }
    public void exit()                        { sendEmptyMessage(EXIT); }

    public void requestName(String name) {
        Message msg = obtainMessage(REQUEST_NAME);
        msg.obj = name;
        sendMessage(msg);
    }

    public void releaseName(String name) {
        Message msg = obtainMessage(RELEASE_NAME);
        msg.obj = name;
        sendMessage(msg);
    }

    public void bindSession(SessionPortListener sessionPortListener, short port, int sessionType) {
        Message msg = obtainMessage(BIND_SESSION);
        msg.obj = sessionPortListener;
        Bundle data = new Bundle();
        data.putShort("port", port);
        data.putInt("type", sessionType);
        msg.setData(data);

        sendMessage(msg);
    }

    public void unbindSession(int port) {
        Message msg = obtainMessage(UNBIND_SESSION);
        msg.obj = new Short(new Short(Integer.toString(port)));
        sendMessage(msg);
    }

    public void advertise(String name) {
        Message msg = obtainMessage(ADVERTISE);
        msg.obj = name;
        sendMessage(msg);
    }

    public void cancelAdvertise(String name) {
        Message msg = obtainMessage(CANCEL_ADVERTISE);
        msg.obj = name;
        sendMessage(msg);
    }

    public void startDiscovery(String prefix) {
        Message msg = obtainMessage(START_DISCOVERY);
        msg.obj = prefix;
        sendMessage(msg);
    }

    public void stopDiscovery(String prefix) {
        Message msg = obtainMessage(STOP_DISCOVERY);
        msg.obj = prefix;
        sendMessage(msg);
    }

    /**
     * Connects to a remote service offered by some device, creating a session.
     * @param sessionListener used for monitoring leaves/joins to the session.
     * @param serviceName well known name of the remote service we want to connect to.
     * @param contactPort the port on which the remote service is hosted on.
     * @param sessionType
     */
    public void joinSession(SessionListener sessionListener,
                            String serviceName,
                            short contactPort,
                            int sessionType) {
        Message msg = obtainMessage(JOIN_SESSION);
        msg.obj = sessionListener;
        Bundle data = new Bundle();
        data.putString("name", serviceName);
        data.putShort("port", contactPort);
        data.putInt("type", sessionType);
        msg.setData(data);

        sendMessage(msg);
    }

    public void leaveSession(int sessionId) {
        Message msg = obtainMessage(LEAVE_SESSION);
        msg.obj = sessionId;
        sendMessage(msg);
    }

    public AlljoynBusHandler(Looper looper, FooshareApplication fooshare) {
        super(looper);
        _fooshare = fooshare;
        _peerService = new PeerService( fooshare);
        _alljoynService = _fooshare.getAlljoynService();
        _busListener = _alljoynService.new FooshareBusListener();
    }

    @Override
    public void handleMessage(Message msg) {
        Bundle data = msg.getData();
        switch(msg.what) {
        case CONNECT:
            doConnect();
            break;
        case REQUEST_NAME:
            doRequestName((String) msg.obj);
            break;
        case  RELEASE_NAME:
            doReleaseName((String) msg.obj);
            break;
        case BIND_SESSION:
            doBindSession((SessionPortListener) msg.obj,
                          (Short) data.get("port"),
                          (Integer) data.get("type"));
            break;
        case UNBIND_SESSION:
            doUnbindSession((Short) msg.obj);
            break;
        case ADVERTISE:
            doAdvertise((String) msg.obj);
            break;
        case CANCEL_ADVERTISE:
            doCancelAdvertise((String) msg.obj);
            break;
        case START_DISCOVERY:
            doStartDiscovery((String) msg.obj);
            break;
        case STOP_DISCOVERY:
            doStopDiscovery((String) msg.obj);
            break;
        case JOIN_SESSION:
            doJoinSession((SessionListener) msg.obj,
                          (String) data.get("name"),
                          (Short) data.get("port"),
                          (Integer) data.get("type"));
            break;
        case LEAVE_SESSION:
            doLeaveSession((Integer) msg.obj);
            break;
        case DISCONNECT:
            doDisconnect();
            break;
        case EXIT:
            _alljoynService.stopService(new Intent(_fooshare.getApplicationContext(),
                                                   org.alljoyn.bus.alljoyn.BundleDaemonService.class));
            getLooper().quit();
            break;

        default:
            Log.i(TAG, "Alljoyn service handler recieved unknown message");
            break;
        }

    }

    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    protected synchronized void doConnect() {
        Log.d(TAG, "doConnect()");

        org.alljoyn.bus.alljoyn.DaemonInit.PrepareDaemon(_alljoynService.getApplicationContext());
        /*
        try {
            Thread.sleep(300);
        }
        catch (InterruptedException e) {
            Log.i(TAG, Log.getStackTraceString(e));
        }
        */

        _bus = new BusAttachment(_alljoynService.getApplicationContext().getPackageName(), BusAttachment.RemoteMessage.Receive);
        //_bus = new BusAttachment(FooshareApplication.APPNAME, BusAttachment.RemoteMessage.Receive);
        _bus.registerBusListener(_busListener);

        /*
         * To make a service available to other AllJoyn peers, first
         * register a BusObject with the BusAttachment at a specific
         * object path.
         */
        Status status = _bus.registerBusObject(_peerService, PEER_SERVICENAME);
        Log.i(TAG, "registerBusObject (peer service) status: " + status);

    	status = _bus.connect();
    	Log.i(TAG, "Bus connection status: " + status);
    }

    protected synchronized void doRequestName(String name) {
        Status status = _bus.requestName(name, BusAttachment.ALLJOYN_REQUESTNAME_FLAG_DO_NOT_QUEUE);
        Log.i(TAG, "requestName " + name + ": " + status);
    }

    protected synchronized void doBindSession(SessionPortListener sessionPortListener, short port, int sessionType) {
        Mutable.ShortValue contactPort = new Mutable.ShortValue(port);
        SessionOpts sessionOpts = new SessionOpts();

        switch (sessionType) {
        case TYPE_REGULAR:
            sessionOpts.traffic = SessionOpts.TRAFFIC_MESSAGES;
            sessionOpts.isMultipoint = true;
            sessionOpts.proximity = SessionOpts.PROXIMITY_ANY;
            sessionOpts.transports = SessionOpts.TRANSPORT_ANY;
            break;

        case TYPE_RAW:
            sessionOpts.traffic = SessionOpts.TRAFFIC_RAW_RELIABLE;
            break;

        default:
            Log.i(TAG, "Unknown session type bind");
            return;
        }

        Status status = _bus.bindSessionPort(contactPort, sessionOpts, sessionPortListener);
        Log.i(TAG, "bindSessionPort " + port + ": " + status);
    }

    protected synchronized void doUnbindSession(short port) {
        Log.i(TAG, "Unbinding session port " + port);
        _bus.unbindSessionPort(port);
    }

    protected synchronized void doAdvertise(String name) {
        Status status = _bus.advertiseName(name, SessionOpts.TRANSPORT_ANY);
        Log.i(TAG, String.format("advertiseName %s: ", name) +  status);
    }

    protected synchronized void doCancelAdvertise(String name) {
        Status status = _bus.cancelAdvertiseName(name, SessionOpts.TRANSPORT_ANY);
        Log.i(TAG, "cancelAdvertiseName " + name + ": " + status);
    }

    protected synchronized void doReleaseName(String name) {
        Log.i(TAG, "Releasing name " + name);
        _bus.releaseName(name);
    }

    protected synchronized void doStartDiscovery(String prefix) {
        Status status = _bus.findAdvertisedName(prefix);
        Log.i(TAG, "findAdvertisedName " + prefix + ": " + status);
    }

    protected synchronized void doStopDiscovery(String prefix) {
        Status status = _bus.cancelFindAdvertisedName(prefix);
        Log.i(TAG, "cancelFindAdvertisedName " + prefix + ": " + status);
    }

    protected synchronized void doDisconnect() {
        Log.i(TAG, "Disconnecting bus");
        _bus.unregisterBusListener(_busListener);
        _bus.unregisterBusObject(_peerService);
        _bus.disconnect();
    }

    protected synchronized void doJoinSession(SessionListener sessionListener,
                                              String serviceName,
                                              short contactPort,
                                              int sessionType) {

        Mutable.IntegerValue sessionId = new Mutable.IntegerValue();
        SessionOpts sessionOpts = new SessionOpts();
        if (sessionType == TYPE_RAW)
            sessionOpts.traffic = SessionOpts.TRAFFIC_RAW_RELIABLE;

        Status status = _bus.joinSession(serviceName, contactPort, sessionId, sessionOpts, sessionListener);

        Log.i(TAG, "joinSession() - sessionId: " + sessionId.value + ", " + status);
        if (status != Status.OK)
            return;

        SessionInfo sessionInfo = null;
        switch(sessionType) {
        case TYPE_REGULAR:
            // set how much seconds until session considered lost
            _bus.setLinkTimeout(sessionId.value, new Mutable.IntegerValue(SESSION_TIMEOUT));
            sessionInfo = new SessionInfo(TYPE_REGULAR, serviceName, sessionId.value);
            break;

        case TYPE_RAW:
            Mutable.IntegerValue sockFd = new Mutable.IntegerValue();
            status = _bus.getSessionFd(sessionId.value, sockFd);
            Log.i(TAG, "getSessionFd(): " + status);

            sessionInfo = new SessionInfo(TYPE_RAW, serviceName, sessionId.value, contactPort, sockFd.value);
            break;

        default:
            Log.i(TAG, "Unknown session type joined");
        }

        onJoinSession.trigger(sessionInfo);
    }

    protected synchronized void doLeaveSession(int sessionId) {
        _bus.leaveSession(sessionId);
    }
}
