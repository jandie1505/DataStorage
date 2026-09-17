package net.jandie1505.datastorage;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * A wrapper for an Entry of DataStorage which prevents adding unallowed values to the DataStorage.
 */
public class DSEntry implements Map.Entry<String, Object> {
    @NotNull private final Map.Entry<String, Object> delegate;

    /**
     * Creates a new entry.
     * @param delegate entry from DataStorage
     */
    public DSEntry(@NotNull Map.Entry<String, Object> delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getKey() {
        return this.delegate.getKey();
    }

    @Override
    public Object getValue() {
        return this.delegate.getValue();
    }

    @Override
    public Object setValue(Object value) {
        if (value instanceof IDataStorage) throw new IllegalArgumentException("Merging DataStorages is not supported here. If you want to add it as String, use String.valueOf.");
        if (value instanceof Map) throw new IllegalArgumentException("Merging Maps is not supported here. If you want to add it as String, use String.valueOf.");
        return this.delegate.setValue(IDataStorage.convertObject(value));
    }

}
