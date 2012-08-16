package org.fooshare.predicates;

import org.fooshare.FileItem;
import org.fooshare.IPeer;

public class PeerIdFilePredicate implements Predicate<FileItem> {
    private static final String TAG = "PeerIdFilePredicate";

    private String _peerID = null;

    public PeerIdFilePredicate(IPeer peer) {
    	if (peer != null) {
    		_peerID = peer.id();
    	}
    }

    public boolean pred(FileItem fileItem) {
        return (fileItem.getOwnerId() == _peerID);
    }
}

