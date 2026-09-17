package net.jandie1505.datastorage;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractSet;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;

public final class CDSEntrySet extends AbstractSet<Map.Entry<String, Object>> {
    private final @NotNull DSEntrySet delegate;
    private final @NotNull ReadWriteLock cdsLock;

    public CDSEntrySet(@NotNull DSEntrySet delegate, @NotNull ReadWriteLock cdsLock) {
        this.delegate = delegate;
        this.cdsLock = cdsLock;
    }

    @Override
    public @NotNull CDSEntryIterator iterator() {
        this.cdsLock.readLock().lock();
        try {
            return new CDSEntryIterator(this.delegate.iterator(), this.cdsLock);
        } finally {
            this.cdsLock.readLock().unlock();
        }
    }

    @Override
    public int size() {
        this.cdsLock.readLock().lock();
        try {
            return this.delegate.size();
        } finally {
            this.cdsLock.readLock().unlock();
        }
    }

}
