package net.jandie1505.datastorage;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;

/**
 * An entry set for DataStorage.
 */
public class DSEntrySet extends AbstractSet<Map.Entry<String, Object>> {
    @NotNull private final Map<String, Object> internalMap;

    /**
     * Creates a new entry set.
     * @param internalMap internal map from DataStorage
     */
    public DSEntrySet(@NotNull Map<String, Object> internalMap) {
        this.internalMap = internalMap;
    }

    @Override
    public @NotNull Iterator<Map.Entry<String, Object>> iterator() {
        return new DSEntryIterator(this.internalMap.entrySet().iterator());
    }

    @Override
    public int size() {
        return this.internalMap.size();
    }

}
