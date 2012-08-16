package org.fooshare.predicates;

import org.fooshare.FileItem;

import android.util.Log;

public class SubStringPredicate implements Predicate<FileItem> {
    private static final String TAG = "SubStringPredicate";

    private final String _substring;

    public SubStringPredicate(String substring) {
        _substring = substring;
    }

    public boolean pred(FileItem fileItem) {
        return fileItem.name().toUpperCase().contains(_substring.toUpperCase());
    }
}

