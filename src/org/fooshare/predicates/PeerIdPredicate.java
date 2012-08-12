package org.fooshare.predicates;

import org.fooshare.IPeer;

public class PeerIdPredicate implements Predicate<IPeer> {
    private String _targetId;

    public PeerIdPredicate(String targetId) {
        _targetId = targetId;
    }

    public boolean pred(IPeer ele) {
        if (ele == null)
            return false;

        return _targetId.equals(ele.id());
    }

}
