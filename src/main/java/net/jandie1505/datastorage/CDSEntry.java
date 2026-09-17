package net.jandie1505.datastorage;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;

public final class CDSEntry implements Map.Entry<String, Object> {
    private final @NotNull DSEntry delegate;
    private final @NotNull ReadWriteLock cdsLock;

    public CDSEntry(@NotNull DSEntry delegate, @NotNull ReadWriteLock cdsLock) {
        this.delegate = delegate;
        this.cdsLock = cdsLock;
    }

    @Override
    public String getKey() {
        this.cdsLock.readLock().lock();
        try {
            return this.delegate.getKey();
        } finally {
            this.cdsLock.readLock().unlock();
        }
    }

    @Override
    public Object getValue() {
        this.cdsLock.readLock().lock();
        try {
            return this.delegate.getValue();
        } finally {
            this.cdsLock.readLock().unlock();
        }
    }

    @Override
    public Object setValue(Object value) {
        this.cdsLock.writeLock().lock();
        try {
            return this.delegate.setValue(value);
        } finally {
            this.cdsLock.writeLock().unlock();
        }
    }

    @Override
    public int hashCode() {
        return this.delegate.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this.delegate.equals(obj);
    }

}
