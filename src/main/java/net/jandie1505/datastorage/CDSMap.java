package net.jandie1505.datastorage;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.concurrent.locks.ReadWriteLock;

public final class CDSMap extends AbstractMap<String, Object> {
    private final @NotNull DSMap delegate;
    private final @NotNull ReadWriteLock cdsLock;

    public CDSMap(@NotNull DSMap delegate, @NotNull ReadWriteLock cdsLock) {
        this.delegate = delegate;
        this.cdsLock = cdsLock;
    }

    @Override
    public @NotNull CDSEntrySet entrySet() {
        this.cdsLock.readLock().lock();
        try {
            return new CDSEntrySet(this.delegate.entrySet(), this.cdsLock);
        } finally {
            this.cdsLock.readLock().unlock();
        }
    }

}
