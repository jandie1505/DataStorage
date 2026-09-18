package net.jandie1505.datastorage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A DataStorage stores data.<br/>
 * It is way more restricted, but that makes it easier to manage the values.<br/>
 * DataStorage can only save primitive types and strings. All other specified objects will be converted to strings.<br/>
 * DataStorage uses a section format, which means Section1.Section2.Section3.
 * Section2 is a subsection of Section1. Subsections can be extracted, sections can be merged into this DataStorage as subsections or directly.
 */
public class DataStorage implements IDataStorage {
    @NotNull private final Map<String, Object> storage;

    /**
     * Creates a new empty DataStorage.
     */
    public DataStorage() {
        this.storage = new HashMap<>();
    }

    /**
     * Creates a new DataStorage from another DataStorage.
     * @param section DataStorage to clone
     */
    public DataStorage(@NotNull IDataStorage section) {
        this();
        this.merge(section, true);
    }

    /**
     * Creates a DatStorage from the specified map.
     * @param storage data storage
     */
    public DataStorage(@NotNull Map<?, ?> storage) {
        this();

        for (Map.Entry<?, ?> entry : storage.entrySet()) {
            this.set(entry.getKey().toString(), entry.getValue());
        }
    }

    // --- BASIC OPERATIONS ---

    /**
     * Returns a value for the specified key.
     * @param key key
     * @return value
     */
    @Nullable
    public final Object get(String key) {
        return this.storage.get(key);
    }

    /**
     * Sets a value for the specified key.<br/>
     * Values which are not primitive or strings are converted to a string.
     * @param key key
     * @param value value
     */
    public final void set(@NotNull String key, @Nullable Object value) {

        if (key.contains(" ")) {
            throw new IllegalArgumentException("Key must not contain spaces");
        }
        
        if (!key.matches("^[a-zA-Z0-9.,:;\\-_]+$")) {
            throw new IllegalArgumentException("key must only contain letters, numbers, points or underscores");
        }

        if (value == null) {
            this.storage.remove(key);
            return;
        }

        if (value instanceof IDataStorage s) {
            this.mergeSection(key, s);
            return;
        }

        if (value instanceof Map<?, ?> m) {
            this.mergeSection(key, new DataStorage(m));
            return;
        }

        this.storage.put(key, IDataStorage.convertObject(value));
    }

    /**
     * Removes the value with the specified key.
     * @param key key
     * @return value
     */
    @Nullable
    public final Object remove(String key) {
        return this.storage.remove(key);
    }

    /**
     * Returns a snapshot (copy) of the internal map.
     * @return snapshot of the internal map
     */
    @NotNull
    public final Map<String, Object> snapshotMap() {
        return Map.copyOf(this.storage);
    }

    /**
     * Clears the data storage.
     */
    public final void clear() {
        this.storage.clear();
    }

    @Override
    public final int size() {
        return this.storage.size();
    }

    // --- SECTIONS ---

    /**
     * Gets a subsection from the DataStorage as a new DataStorage.
     * @param key key
     * @return subsection
     */
    @NotNull
    public DataStorage getSection(@NotNull String key) {
        DataStorage sectionStorage = new DataStorage();

        for (Map.Entry<String, Object> entry : Map.copyOf(this.storage).entrySet()) {
            String entryKey = entry.getKey();

            if (entryKey.startsWith(key + ".")) {
                String newKey = entryKey.substring(key.length() + 1);
                sectionStorage.set(newKey, entry.getValue());
            }
        }

        return sectionStorage;
    }

    /**
     * Merge another DataStorage into this DataStorage as a section.<br/>
     * This means if you merge "section1.section2" into this DataStorage with key "section0", the value in this DataStorage will be "section0.section1.section2".
     * @param key section key
     * @param section section
     * @param overwrite when true, existing values will be replaced (recommended)
     */
    public void mergeSection(@NotNull String key, @NotNull IDataStorage section, boolean overwrite) {
        for (Map.Entry<String, Object> entry : section.entrySet()) {
            String sectionKey = entry.getKey();
            Object value = entry.getValue();

            String mergedKey = key + "." + sectionKey;

            if (!overwrite && this.storage.containsKey(mergedKey)) continue;
            this.set(mergedKey, value);
        }
    }

    /**
     * Merge another DataStorage into this DataStorage as a section.<br/>
     * This means if you merge "section1.section2" into this DataStorage with key "section0", the value in this DataStorage will be "section0.section1.section2".<br/>
     * This method has "overwrite" set to true.
     * @param key key
     * @param section section
     */
    public void mergeSection(@NotNull String key, @NotNull IDataStorage section) {
        this.mergeSection(key, section, true);
    }

    /**
     * Returns all sections of the DataStorage.
     * @return map of sections
     */
    public Map<String, IDataStorage> getSections() {
        Map<String, IDataStorage> sections = new HashMap<>();

        for (String key : this.storage.keySet()) {
            String[] sectionKey  = key.split("\\.");
            if (sectionKey.length <= 1) continue;
            sections.put(sectionKey[0], this.getSection(sectionKey[0]));
        }

        return sections;
    }

    /**
     * Returns a DataStorage only containing values from the first level (no subsections).<br/>
     * Example:<br/>
     * - 'exampleSection.exampleValue' -> Not on the top level<br/>
     * - 'exampleValue' -> On the top level<br/>
     * @return data storage
     */
    public DataStorage getTopLevelEntryStorage() {
        DataStorage currentLevelSection = new DataStorage();

        for (Map.Entry<String, Object> entry : this.storage.entrySet()) {
            if (entry.getKey().contains(".")) continue;
            currentLevelSection.set(entry.getKey(), entry.getValue());
        }

        return currentLevelSection;
    }

    // --- MERGE ---

    /**
     * Merge the values of another DataStorage into this DataStorage.
     * @param other other data storage
     * @param overwrite when true, existing values will be replaced (recommended)
     */
    public void merge(@NotNull IDataStorage other, boolean overwrite) {
        for (Map.Entry<String, Object> entry : other) {
            if (!overwrite && this.storage.containsKey(entry.getKey())) continue;
            this.set(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Merge the values of another DataStorage into this DataStorage.<br/>
     * This method has "overwrite" set to true.
     * @param other other data storage
     */
    public void merge(@NotNull IDataStorage other) {
        this.merge(other, true);
    }

    // --- ITERABLE ---

    /**
     * Returns an iterator of map entries for each value.
     * @return iterator
     */
    @Override
    @NotNull
    public Iterator<Map.Entry<String, Object>> iterator() {
        return this.snapshotMap().entrySet().iterator();
    }

    // --- SETS ---

    /**
     * Returns a key set of this storage.
     * @return key set
     */
    @NotNull
    public Set<String> keySet() {
        return this.snapshotMap().keySet();
    }

    /**
     * Returns an entry set of this storage.
     * @return entry set
     */
    @NotNull
    public Set<Map.Entry<String, Object>> entrySet() {
        return this.snapshotMap().entrySet();
    }

    // --- CLONE ---

    /**
     * Clones the data storage.<br/>
     * Since all values of it are immutable, the data has not to be copied.
     * @return cloned DataStorage
     */
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public DataStorage clone() {
        DataStorage storage = new DataStorage();
        storage.storage.putAll(this.storage);
        return storage;
    }

    // --- INTERNAL ACCESS ---

    /**
     * Returns the internal storage.<br/>
     * Use with caution!
     * @return internal storage
     */
    protected final @NotNull Map<String, Object> storage() {
        return this.storage;
    }

    // --- STATIC ---

    /**
     * Converts any object to an object which can be stored in the data storage.<br/>
     * This is normally used internally.
     * @param o object
     * @return converted object
     * @deprecated This is now located in {@link IDataStorage#convertObject(Object)}.
     */
    @Deprecated
    public static @NotNull Object convertObject(@NotNull Object o) {
        return IDataStorage.convertObject(o);
    }

}
