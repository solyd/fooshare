package org.fooshare.events;

import java.util.ArrayList;
import java.util.Collection;


public class Event<T> {
    private final Collection<Delegate<T>> _subs = new ArrayList<Delegate<T>>();

    public synchronized boolean subscribe(Delegate<T> delegate) {
        if (delegate == null)
            return false;

        return _subs.add(delegate);
    }

    public synchronized boolean unsubscribe(Delegate<T> delegate) {
        if (delegate == null)
            return false;

        return _subs.remove(delegate);
    }

    public synchronized void clear() {
        _subs.clear();
    }

    public synchronized void trigger(T data) {
        // TODO - make this happen in a separate thread
        for (Delegate<T> d : _subs)
            d.invoke(data);
    }
}
