package org.fooshare;

import java.util.Collection;


public interface IPeer {
    String id();
    String name();
    Collection<FileItem> files();
}