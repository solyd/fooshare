package org.fooshare.events;

public interface Delegate<T> {
    public void invoke(T data);
}
