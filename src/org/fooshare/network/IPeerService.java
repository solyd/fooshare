package org.fooshare.network;

import org.alljoyn.bus.BusException;
import org.alljoyn.bus.annotation.BusInterface;
import org.alljoyn.bus.annotation.BusMethod;
import org.alljoyn.bus.annotation.Position;


@BusInterface
public interface IPeerService {
    public class AlljoynFileItem {
        @Position(0)
        public String fullName;
        @Position(1)
        public long sizeInBytes;
        @Position(2)
        public String ownerId;

        @Override
        public String toString() {
            String[] parts = fullName.split("/");
            return parts[parts.length - 1];
        }
    }

    public class FileServerInfo {
        @Position(0)
        public String hostName;
        @Position(1)
        public int port;
    }

    @BusMethod
    String peerName() throws BusException;

    @BusMethod(replySignature="a(sxs)")
    AlljoynFileItem[] peerFiles() throws BusException;

    @BusMethod(replySignature="(si)")
    FileServerInfo fileServerDetails() throws BusException;

}

