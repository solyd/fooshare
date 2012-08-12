package org.fooshare;

import org.fooshare.network.IPeerService.FileItem;


public interface IPeer {
    String id();
    String name();
    FileItem[] files();
}