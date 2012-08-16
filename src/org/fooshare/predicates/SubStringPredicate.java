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
        if (fileItem.getName().toUpperCase().contains(_substring.toUpperCase())) {
            Log.d(TAG, fileItem.getName() + " contains " + _substring);
            return true;
        }

        Log.d(TAG, fileItem.getName() + " does not contain " + _substring);
        return false;
    }
}

