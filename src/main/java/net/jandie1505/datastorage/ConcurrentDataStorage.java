package net.jandie1505.datastorage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A thread-safe version of the {@link DataStorage}.
 */
public class ConcurrentDataStorage implements IDataStorage {
    @NotNull private final DataStorage delegate;
    @NotNull private final ReadWriteLock lock;

    /**
     * Creates a new empty ConcurrentDataStorage.
     */
    public ConcurrentDataStorage() {
        this.delegate = new DataStorage();
        this.lock = new ReentrantReadWriteLock();
    }

    /**
     * Creates a new ConcurrentDataStorage from another DataStorage.
     * @param storage DataStorage to clone
     */
    public ConcurrentDataStorage(@NotNull IDataStorage storage) {
        this();
        this.delegate.merge(storage, true);
    }

    /**
     * Creates a DatStorage from the specified map.
     * @param storage data storage
     */
    public ConcurrentDataStorage(@NotNull Map<?, ?> storage) {
        this();

        for (Map.Entry<?, ?> entry : storage.entrySet()) {
            this.delegate.set(entry.getKey().toString(), entry.getValue());
        }
    }

    // ----- BASIC OPERATIONS -----

    public final @Nullable Object get(String key) {
        this.lock.readLock().lock();
        try {
            return this.delegate.get(key);
        } finally {
            this.lock.readLock().unlock();
        }
    }

    public final void set(@NotNull String key, @Nullable Object value) {
        this.lock.writeLock().lock();
        try {
            this.delegate.set(key, value);
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    public final @Nullable Object remove(String key) {
        this.lock.writeLock().lock();
        try {
            return this.delegate.remove(key);
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    public final @NotNull Map<String, Object> asMap() {
        this.lock.readLock().lock();
        try {
            return this.delegate.asMap();
        } finally {
            this.lock.readLock().unlock();
        }
    }

    public final void clear() {
        this.lock.writeLock().lock();
        try {
            this.delegate.clear();
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    // ----- SECTIONS -----

    /**
     * Gets a subsection from the DataStorage as a new DataStorage.<br/>
     * The returned section is independent and <b>not</b> linked to this DataStorage object.<br/>
     * The returned new section is also <b>not thread-safe</b>.
     * You need to wrap it into a {@link ConcurrentDataStorage} using <code>new ConcurrentDataStorage(section);</code> if you need it thread-safe.
     * @param key key
     * @return independent subsection
     */
    public @NotNull DataStorage getSection(@NotNull String key) {
        this.lock.readLock().lock();
        try {
            return this.delegate.getSection(key);
        } finally {
            this.lock.readLock().unlock();
        }
    }

    public void mergeSection(@NotNull String key, @NotNull IDataStorage section, boolean overwrite) {
        section = new DataStorage(section); // create snapshot to prevent deadlocks
        this.lock.writeLock().lock();
        try {
            this.delegate.mergeSection(key, section, overwrite);
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    public void mergeSection(@NotNull String key, @NotNull IDataStorage section) {
        section = new DataStorage(section);
        this.lock.writeLock().lock();
        try {
            this.delegate.mergeSection(key, section);
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    /**
     * Returns all sections of the DataStorage.<br/>
     * The returned sections are independent and <b>not</b> linked to this DataStorage object.<br/>
     * The returned sections are also <b>not thread-safe</b>.
     * You need to wrap them into a {@link ConcurrentDataStorage} using <code>new ConcurrentDataStorage(section);</code> if you need them thread-safe.
     * @return map of independent sections
     */
    public Map<String, IDataStorage> getSections() {
        this.lock.readLock().lock();
        try {
            return this.delegate.getSections();
        } finally {
            this.lock.readLock().unlock();
        }
    }

    /**
     * Returns a DataStorage only containing values from the first level (no subsections).<br/>
     * Example:<br/>
     * - 'exampleSection.exampleValue' -> Not on the top level<br/>
     * - 'exampleValue' -> On the top level<br/>
     * The returned new section is also <b>not thread-safe</b>.
     * You need to wrap it into a {@link ConcurrentDataStorage} using <code>new ConcurrentDataStorage(section);</code> if you need it thread-safe.
     * @return data storage
     */
    public DataStorage getTopLevelEntryStorage() {
        this.lock.readLock().lock();
        try {
            return this.delegate.getTopLevelEntryStorage();
        } finally {
            this.lock.readLock().unlock();
        }
    }

    // --- MERGE ---

    /**
     * Merge the values of another DataStorage into this DataStorage.
     * @param other other data storage
     * @param overwrite when true, existing values will be replaced (recommended)
     */
    public void merge(@NotNull IDataStorage other, boolean overwrite) {
        other = new DataStorage(other);
        this.lock.writeLock().lock();
        try {
            this.delegate.merge(other, overwrite);
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    /**
     * Merge the values of another DataStorage into this DataStorage.<br/>
     * This method has "overwrite" set to true.
     * @param other other data storage
     */
    public void merge(@NotNull IDataStorage other) {
        other = new DataStorage(other);
        this.lock.writeLock().lock();
        try {
            this.delegate.merge(other);
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    // --- ITERABLE ---

    /**
     * Returns an iterator of map entries for each value.
     * @return iterator
     */
    @Override
    public @NotNull Iterator<Map.Entry<String, Object>> iterator() {
        this.lock.readLock().lock();
        try {
            return this.delegate.iterator();
        } finally {
            this.lock.readLock().unlock();
        }
    }

    // --- SETS ---

    /**
     * Returns a key set of this storage.
     * @return key set
     */
    @NotNull
    public Set<String> keySet() {
        this.lock.readLock().lock();
        try {
            return this.delegate.keySet();
        } finally {
            this.lock.readLock().unlock();
        }
    }

    /**
     * Returns an entry set of this storage.
     * @return entry set
     */
    public @NotNull Set<Map.Entry<String, Object>> entrySet() {
        this.lock.readLock().lock();
        try {
            return this.delegate.entrySet();
        } finally {
            this.lock.readLock().unlock();
        }
    }

    // --- CLONE ---

    /**
     * Clones the data storage.<br/>
     * Since all values of it are immutable, the data has not to be copied.
     * @return cloned DataStorage
     */
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public ConcurrentDataStorage clone() {
        this.lock.readLock().lock();
        try {
            return new ConcurrentDataStorage(this.delegate.clone());
        } finally {
            this.lock.readLock().unlock();
        }
    }

}
