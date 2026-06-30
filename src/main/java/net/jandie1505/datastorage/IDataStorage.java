package net.jandie1505.datastorage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

/**
 * An interface for DataStorages.
 */
public interface IDataStorage extends Iterable<Map.Entry<String, Object>> {

    // --- BASIC OPERATIONS ---

    /**
     * Returns a value for the specified key.
     * @param key key
     * @return value
     */
    @Nullable Object get(String key);

    /**
     * Sets a value for the specified key.<br/>
     * Values which are not primitive or strings are converted to a string.
     * @param key key
     * @param value value
     */
    void set(@NotNull String key, @Nullable Object value);

    /**
     * Removes the value with the specified key.
     * @param key key
     * @return value
     */
    @Nullable Object remove(String key);

    /**
     * Converts the data storage to an unmodifiable map and returns it.
     * @return map
     */
    @NotNull Map<String, Object> asMap();

    /**
     * Clears the data storage.
     */
    void clear();

    // --- SECTIONS ---

    /**
     * Gets a subsection from the DataStorage as a new DataStorage.<br/>
     * The returned section is independent and <b>not</b> linked to this DataStorage object.
     * @param key key
     * @return independent subsection
     */
    @NotNull IDataStorage getSection(@NotNull String key);

    /**
     * Merge another DataStorage into this DataStorage as a section.<br/>
     * This means if you merge "section1.section2" into this DataStorage with key "section0", the value in this DataStorage will be "section0.section1.section2".
     * @param key section key
     * @param section section
     * @param overwrite when true, existing values will be replaced (recommended)
     */
    void mergeSection(@NotNull String key, @NotNull IDataStorage section, boolean overwrite);

    /**
     * Merge another DataStorage into this DataStorage as a section.<br/>
     * This means if you merge "section1.section2" into this DataStorage with key "section0", the value in this DataStorage will be "section0.section1.section2".<br/>
     * This method has "overwrite" set to true.
     * @param key key
     * @param section section
     */
    void mergeSection(@NotNull String key, @NotNull IDataStorage section);

    /**
     * Returns all sections of the DataStorage.<br/>
     * The returned sections are independent and <b>not</b> linked to this DataStorage object.
     * @return map of independent sections
     */
    Map<String, IDataStorage> getSections();

    /**
     * Returns a DataStorage only containing values from the first level (no subsections).<br/>
     * Example:<br/>
     * - 'exampleSection.exampleValue' -> Not on the top level<br/>
     * - 'exampleValue' -> On the top level<br/>
     * The returned section is independent and <b>not</b> linked to this DataStorage object.
     * @return data storage
     */
    IDataStorage getTopLevelEntryStorage();

    // --- MERGE ---

    /**
     * Merge the values of another DataStorage into this DataStorage.
     * @param other other data storage
     * @param overwrite when true, existing values will be replaced (recommended)
     */
    void merge(@NotNull IDataStorage other, boolean overwrite);

    /**
     * Merge the values of another DataStorage into this DataStorage.<br/>
     * This method has "overwrite" set to true.
     * @param other other data storage
     */
    void merge(@NotNull IDataStorage other);

    // --- SETS ---

    /**
     * Returns a key set of this storage.
     * @return key set
     */
    @NotNull Set<String> keySet();

    /**
     * Returns an entry set of this storage.
     * @return entry set
     */
    @NotNull Set<Map.Entry<String, Object>> entrySet();

    // --- GET SPECIFIC TYPES ---

    /**
     * Returns the specific type from the storage.<br/>
     * If the specific type does not exist, the default value is returned.
     * @param key key
     * @param defaultValue default value
     * @return value
     */
    default int optInt(@NotNull String key, int defaultValue) {
        Object value = this.get(key);
        if (value instanceof Integer v) return v;
        return defaultValue;
    }

    /**
     * Returns the specific type from the storage.<br/>
     * If the specific type does not exist, the default value is returned.
     * @param key key
     * @param defaultValue default value
     * @return value
     */
    default long optLong(@NotNull String key, long defaultValue) {
        Object value = this.get(key);
        if (value instanceof Long v) return v;
        return defaultValue;
    }

    /**
     * Returns the specific type from the storage.<br/>
     * If the specific type does not exist, the default value is returned.
     * @param key key
     * @param defaultValue default value
     * @return value
     */
    default double optDouble(@NotNull String key, double defaultValue) {
        Object value = this.get(key);
        if (value instanceof Double v) return v;
        return defaultValue;
    }

    /**
     * Returns the specific type from the storage.<br/>
     * If the specific type does not exist, the default value is returned.
     * @param key key
     * @param defaultValue default value
     * @return value
     */
    default float optFloat(@NotNull String key, float defaultValue) {
        Object value = this.get(key);
        if (value instanceof Float v) return v;
        return defaultValue;
    }

    /**
     * Returns the specific type from the storage.<br/>
     * If the specific type does not exist, the default value is returned.
     * @param key key
     * @param defaultValue default value
     * @return value
     */
    default boolean optBoolean(@NotNull String key, boolean defaultValue) {
        Object value = this.get(key);
        if (value instanceof Boolean v) return v;
        return defaultValue;
    }

    /**
     * Returns the specific type from the storage.<br/>
     * If the specific type does not exist, the default value is returned.
     * @param key key
     * @param defaultValue default value
     * @return value
     */
    default String optString(@NotNull String key, @Nullable String defaultValue) {
        Object value = this.get(key);
        if (value instanceof String v) return v;
        return defaultValue;
    }

    /**
     * Returns the specific type from the storage.<br/>
     * If the specific type does not exist, the default value is returned.
     * @param key key
     * @param defaultValue default value
     * @return value
     */
    default byte optByte(@NotNull String key, byte defaultValue) {
        Object value = this.get(key);
        if (value instanceof Byte v) return v;
        return defaultValue;
    }

    /**
     * Returns the specific type from the storage.<br/>
     * If the specific type does not exist, the default value is returned.
     * @param key key
     * @param defaultValue default value
     * @return value
     */
    default short optShort(@NotNull String key, short defaultValue) {
        Object value = this.get(key);
        if (value instanceof Short v) return v;
        return defaultValue;
    }

    /**
     * Returns the specific type from the storage.<br/>
     * If the specific type does not exist, the default value is returned.
     * @param key key
     * @param defaultValue default value
     * @return value
     */
    default char optChar(@NotNull String key, char defaultValue) {
        Object value = this.get(key);
        if (value instanceof Character v) return v;
        return defaultValue;
    }

    // --- CLONE ---

    /**
     * Clones the data storage.<br/>
     * Since all values of it are immutable, the data has not to be copied.
     * @return cloned DataStorage
     */
    IDataStorage clone();

    // --- TOOLS ---

    /**
     * Converts any object to an object which can be stored in the data storage.<br/>
     * This is normally used internally.
     * @param o object
     * @return converted object
     */
    @NotNull
    static Object convertObject(@NotNull Object o) {

        return switch (o) {
            case Integer i -> i;
            case Long l -> l;
            case Double d -> d;
            case Float f -> f;
            case Boolean b -> b;
            case String s -> s;
            case Byte b -> b;
            case Short s -> s;
            case Character c -> c;
            default -> o.toString();
        };

    }

}
