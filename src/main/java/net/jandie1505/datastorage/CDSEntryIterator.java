package net.jandie1505.datastorage;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Consumer;

public final class CDSEntryIterator implements Iterator<Map.Entry<String, Object>> {
    private final @NotNull DSEntryIterator delegate;
    private final @NotNull ReadWriteLock cdsLock;

    public CDSEntryIterator(@NotNull DSEntryIterator delegate, @NotNull ReadWriteLock cdsLock) {
        this.delegate = delegate;
        this.cdsLock = cdsLock;
    }

    @Override
    public boolean hasNext() {
        this.cdsLock.readLock().lock();
        try {
            return this.delegate.hasNext();
        } finally {
            this.cdsLock.readLock().unlock();
        }
    }

    @Override
    public CDSEntry next() {
        this.cdsLock.writeLock().lock();
        try {
            return new CDSEntry(this.delegate.next(), this.cdsLock);
        } finally {
            this.cdsLock.writeLock().unlock();
        }
    }

    @Override
    public void remove() {
        this.cdsLock.writeLock().lock();
        try {
            this.delegate.remove();
        } finally {
            this.cdsLock.writeLock().unlock();
        }
    }

    @Override
    public void forEachRemaining(Consumer<? super Map.Entry<String, Object>> action) {
        this.cdsLock.writeLock().lock();
        try {
            this.delegate.forEachRemaining(entry -> action.accept(new CDSEntry((DSEntry) entry, this.cdsLock)));
        } finally {
            this.cdsLock.writeLock().unlock();
        }
    }

}
