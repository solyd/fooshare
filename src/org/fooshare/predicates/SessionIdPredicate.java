package org.fooshare.predicates;

import org.fooshare.AlljoynPeer;
import org.fooshare.IPeer;

public class SessionIdPredicate implements Predicate<IPeer> {
    private final int _sessionId;

    public SessionIdPredicate(int sessionId) {
        _sessionId = sessionId;
    }

    public boolean pred(IPeer ele) {
        if (ele instanceof AlljoynPeer == false)
            return false;

        return ((AlljoynPeer) ele).sessionId() == _sessionId;
    }

}
